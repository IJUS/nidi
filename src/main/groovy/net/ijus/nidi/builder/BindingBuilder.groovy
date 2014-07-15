package net.ijus.nidi.builder

import net.ijus.nidi.*
import net.ijus.nidi.bindings.*
import net.ijus.nidi.instantiation.ConstructorInstanceGenerator
import net.ijus.nidi.instantiation.InstanceGenerator
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.lang.annotation.Annotation
import java.lang.reflect.Constructor

/**
 * Created by pfried on 7/2/14.
 */

class BindingBuilder {
	static final Logger log = LoggerFactory.getLogger(BindingBuilder)
	ContextBuilder ctxBuilder
	Class from
	Class impl
	Scope scope
	Closure instanceConfigClosure

	InstanceGenerator instanceGenerator

	Binding binding

	Map<String, BindingBuilder> innerBindings = [:]

	BindingBuilder(Class clazz, ContextBuilder ctxBuilder) {
		this.from = clazz
		this.ctxBuilder = ctxBuilder
	}

	BindingBuilder setupInstance(Closure instanceSetupClosure) {
		this.instanceConfigClosure = instanceSetupClosure
		return this
	}

	BindingBuilder to(Class clazz, Closure config = null) {
		this.impl = clazz
		validateClassAssignment()

		if (config) {
			config.setDelegate(this)
			config.call()
		}
		return this
	}

	BindingBuilder toValue(Closure closure) {
		this.instanceGenerator = closure as InstanceGenerator

		try {
			def instance = closure.call()
			this.impl = instance.getClass()
			validateClassAssignment()
		} catch (InvalidConfigurationException e){
			throw e
		} catch (Exception e) {
			throw new InvalidConfigurationException("Attempted to bind ${name(from)} to the value of a closure, but the closure threw an exception", e)
		}
		return this
	}

	BindingBuilder withScope(Scope s) {
		this.scope = s
		return this
	}

	void inheritScope(Scope s) {
		if (!this.scope) {
			this.scope = s
		}
	}

	BindingBuilder bindConstructorParam(String param) {
		if (!this.impl) {
			throw new InvalidConfigurationException("Cannot create bindings for constructor params before the implementation class has been specified")
		}
		Constructor constructor = resolveConstructor(this.impl)
		String[] paramAnnots = getBoundAnnotatedParams(constructor)
		Class paramClass
		for (int i = 0; i < paramAnnots.length; i++) {
			if (paramAnnots[i] == param) {
				paramClass = constructor.getParameterTypes()[i]
			}
		}

		if (!paramClass) {
			throw new InvalidConfigurationException("Could not find a matching constructor parameter: ${param}. In order to use bindConstructorParam(${param}), the constructor parameter must be annotated with: @Bound('${param}')")
		}

		BindingBuilder bb = new BindingBuilder(paramClass, this.ctxBuilder)
		this.innerBindings.put(param, bb)
		return bb
	}

	String[] getBoundAnnotatedParams(Constructor constructor){
		Annotation[][] allAnnotations = constructor.getParameterAnnotations()

		String[] boundParams = new String[allAnnotations.length]

		for (int outer = 0; outer < allAnnotations.length; outer++) {
			Annotation[] paramAnnotations = allAnnotations[outer]
			for (int inner = 0; inner < paramAnnotations.length; inner++) {
				Annotation a = paramAnnotations[inner]
				if (a instanceof Bound) {
					boundParams[outer] = a.value()
				}
			}
		}

		return boundParams
	}

	Binding[] resolveConstructorParams(Constructor constructor) {
		Class[] constructorParams = constructor.getParameterTypes()
		if (constructorParams.length == 0) {
			return new Binding[0]
		}

		Binding[] paramBindings = new Binding[constructorParams.length]

		String[] boundAnnotationValues = getBoundAnnotatedParams(constructor)


		for (int paramIdx = 0; paramIdx < constructorParams.length; paramIdx++) {

			Class paramType = constructorParams[paramIdx]

			if (boundAnnotationValues[paramIdx]) {
				String paramName = boundAnnotationValues[paramIdx]
				BindingBuilder innerBuilder = this.innerBindings.get(paramName)
				log.debug("Constructor Param: ${paramName} for ${name(impl)} being resolved from an inner binding. Exists?= ${innerBuilder != null}")
				if (!innerBuilder) {
					throw new InvalidConfigurationException("The constructor param for class: ${name(impl)} and index: ${paramIdx} was annotated with @Bound(${paramName}), but no inner binding was specified.")
				}
				Binding resolvedBinding = innerBuilder.build()
				log.debug("Built inner binding")
				paramBindings[paramIdx] = resolvedBinding

			} else {
				paramBindings[paramIdx] = buildContextRefBinding(paramType)

			}

		}
		return paramBindings
	}

	Binding buildContextRefBinding(Class baseType) {
		log.trace("buildBindingForClass: baseType=${name(baseType)}")
		if (!ctxBuilder.containsBindingFor(baseType)) {
			throw new InvalidConfigurationException("The Constructor for Class: ${name(impl)} has a parameter ")
		}
		Context ctx = ctxBuilder.getContextRef()
		Binding b = new ContextBindingReference(baseType, ctx)
		log.debug("bindingBuilder returning ContextBindingReference for ${name(baseType)}")
		return b
	}



	Constructor resolveConstructor(Class clazz) {
		Constructor[] constructors = clazz.getConstructors()
		def constructor
		if (constructors.length == 1) {
			constructor = constructors[0]

		} else if (constructors.length > 1) {
			List<Constructor> withAnno = constructors.findAll{Constructor c-> c.isAnnotationPresent(Inject) }
			if (withAnno.size() != 1) {
				throw new InvalidConfigurationException("The Class: ${name(clazz)} has more than one constructor, so exactly one Constructor should have the @Inject annotation. Found ${withAnno.size()} Constructors with that annotation.")
			}
			constructor = withAnno.get(0)
		} else {
			throw new InvalidConfigurationException("The Class: ${name(clazz)} has no public constructors")
		}
		return constructor
	}

	String name(Class clazz) {
		String name
		if (clazz.isAnonymousClass()) {
			name = "Anonymous implementation of: ${clazz.getSuperclass().getCanonicalName()}"
		} else {
			name = clazz.getCanonicalName()
		}
		name
	}

	void validateClassAssignment() {
		if (!impl) {
			throw new InvalidConfigurationException("The Class: ${name(from)} was declared to be bound but the implementation class is null")
		} else if (!from.isAssignableFrom(impl)) {
			throw new InvalidConfigurationException("The Class: ${name(from)} was bound to ${name(impl)} but ${impl.getSimpleName()} is not a ${from.getSimpleName()}")
		}
	}

	Binding build() throws InvalidConfigurationException {
		inheritScope(this.ctxBuilder.getDefaultScope())
		InstanceGenerator gen = this.instanceGenerator

		if (!gen) {
			Constructor constructor = resolveConstructor(this.impl)
			Binding[] params = resolveConstructorParams(constructor)
			gen = new ConstructorInstanceGenerator(this.impl, params, instanceConfigClosure)
		}
		return createBinding(gen)

	}

	protected Binding createBinding(InstanceGenerator instanceGenerator) {
		Binding b
		if (!this.scope) {
			inheritScope(this.ctxBuilder.getDefaultScope())
		}
		if (this.scope == Scope.ALWAYS_CREATE_NEW) {
			b = new BasicBinding(this.from, this.impl, instanceGenerator)
		} else {
			b = new CacheingBinding(instanceGenerator, this.from, this.impl, this.scope)
		}
		return b
	}
}