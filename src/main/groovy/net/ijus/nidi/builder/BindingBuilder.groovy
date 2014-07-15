package net.ijus.nidi.builder

import net.ijus.nidi.bindings.BasicBinding
import net.ijus.nidi.bindings.CacheingBinding
import net.ijus.nidi.instantiation.ConstructorInstanceGenerator
import net.ijus.nidi.Context
import net.ijus.nidi.bindings.ContextBindingReference
import net.ijus.nidi.Inject
import net.ijus.nidi.InvalidConfigurationException
import net.ijus.nidi.bindings.Scope
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

	ConstructorInstanceGenerator instanceGenerator

	net.ijus.nidi.bindings.Binding binding

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
		this.instanceConfigClosure = config
		validateClassAssignment()
		return this
	}

	BindingBuilder toValue(Closure closure) {

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



	net.ijus.nidi.bindings.Binding[] resolveConstructorParams(Constructor constructor) {
		Class[] constructorParams = constructor.getParameterTypes()
		Annotation[][] allAnnotations = constructor.getParameterAnnotations()

		if (constructorParams.length == 0) {
			return new net.ijus.nidi.bindings.Binding[0]
		}

		net.ijus.nidi.bindings.Binding[] paramBindings = new net.ijus.nidi.bindings.Binding[constructorParams.length]

		for (int paramIdx = 0; paramIdx < constructorParams.length; paramIdx++) {
			Class paramType = constructorParams[paramIdx]

			paramBindings[paramIdx] = buildContextRefBinding(paramType)
		}
		return paramBindings
	}

	net.ijus.nidi.bindings.Binding buildContextRefBinding(Class baseType) {
		log.trace("buildBindingForClass: baseType=${name(baseType)}")
		if (!ctxBuilder.containsBindingFor(baseType)) {
			throw new InvalidConfigurationException("The Constructor for Class: ${name(impl)} has a parameter ")
		}
		Context ctx = ctxBuilder.getContextRef()
		net.ijus.nidi.bindings.Binding b = new ContextBindingReference(baseType, ctx)
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

	net.ijus.nidi.bindings.Binding build() throws InvalidConfigurationException {
		inheritScope(this.ctxBuilder.getDefaultScope())
		Constructor constructor = resolveConstructor(this.impl)
		net.ijus.nidi.bindings.Binding[] params = resolveConstructorParams(constructor)

		ConstructorInstanceGenerator gen = new ConstructorInstanceGenerator(this.impl, params, instanceConfigClosure)
		return createBinding(gen)

	}

	protected net.ijus.nidi.bindings.Binding createBinding(ConstructorInstanceGenerator instanceGenerator) {
		net.ijus.nidi.bindings.Binding b
		if (!this.scope) {
			inheritScope(this.ctxBuilder.getDefaultScope())
		}
		if (this.scope == Scope.ALWAYS_CREATE_NEW) {
			b = new BasicBinding(this.from, instanceGenerator)
		} else {
			b = new CacheingBinding(instanceGenerator, this.from, this.scope)
		}
		return b
	}
}