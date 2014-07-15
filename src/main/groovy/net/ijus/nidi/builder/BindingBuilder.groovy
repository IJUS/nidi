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

	Map<Object, BindingBuilder> innerBindings = [:]

	BindingBuilder(Class clazz, ContextBuilder ctxBuilder) {
		this.from = clazz
		this.ctxBuilder = ctxBuilder
	}

	/**
	 * Specifies a closure to be used to setup a new instance of the concrete implementation class.
	 * The instance generator will create the instance using bindings in the context in order to resolve constructor parameters.
	 * If the implementation class still has setup work after the instance is created, then this is how to do it. This would be used
	 * in the case where a Class has a property that doesn't always need set, but occasionally needs overridden. It doesn't always
	 * make sense to have such properties declared in the constructor, so this closure setup is provided. In example:
	 * <code>
	 *     class Foo implements Bar {
	 *         String someProperty = "defaultValue"
	 *     }
	 *
	 *     //in ContextConfig
	 *     bind(Bar).to(Foo).setupInstance{Foo instance->
	 *         instance.someProperty = "override Value"
	 *     }
	 * </code>
	 *
	 * @param instanceSetupClosure closure that will be called with the newly created instance as an argument
	 * @return this BindingBuilder for chaining
	 */
	BindingBuilder setupInstance(Closure instanceSetupClosure) {
		this.instanceConfigClosure = instanceSetupClosure
		return this
	}

	/**
	 * Tells the builder to use the specified class as the implementation for the base class. Optionally, a config
	 * closure can be used to specify the scope as well as any inner bindings (overrides and special @Bound params).
	 * An example here shows it's use:
	 * <code>
	 *     Context ctx = Configuration.configureNew{
	 *         BindingBuilder builder = bind(MyInterface) //returns a BindingBuilder
	 *         builder.to(MyConcreteImpl){ // Normall would just be chained
	 *             scope = Scope.ALWAYS_CREATE_NEW
	 *             bindConstructorParam('Annotated constructor param name').toValue{ ['custom', 'list'] }
	 *             setupInstance{MyConcreteImpl instance->
	 *                 instance.setFoo("foo")
	 *                 instance.setDebugMode(true)
	 *             }
	 *         }
	 *     }
	 * </code>
	 * The Closure is just syntactic sugar. Everything done in the closure can also be done by just calling methods on the
	 * BindingBuilder directly. It just helps keep things a bit more clear in complex ContextConfig classes.
	 *
	 * @param clazz The concrete implementation to be used for this binding
	 * @param config optional configguration closure
	 * @return this BindingBuilder for chained method calls
	 */
	BindingBuilder to(Class clazz, Closure config = null) {
		this.impl = clazz
		validateClassAssignment()

		if (config) {
			config.setDelegate(this)
			config.call()
		}
		return this
	}

	/**
	 * Binds to whatever value is returned from the closure. This Closure will be called multiple times.
	 * It will get called immediately in order to validate that the return type of the closure is compatible
	 * with the base class for this binding. It will then get called again every time a new instance is created,
	 * as determined by the Scope of the Binding.
	 * @param closure returns a value to bind to.
	 * @return
	 */
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

	/**
	 * Sets the scope, overriding any default scope set in the parent ContextBuilder
	 * @param s
	 * @return
	 */
	BindingBuilder withScope(Scope s) {
		this.scope = s
		return this
	}

	/**
	 * Creates an inner binding, which will only be used for resolving constructor params for this binding.
	 *
	 * @param param
	 * @return
	 */
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

	/**
	 * Sets the specified scope only if the current scope is null
	 * @param s
	 */
	protected void inheritScope(Scope s) {
		if (!this.scope) {
			this.scope = s
		}
	}

	/**
	 * returns an array of string values for all the constructor params annotated with @Bound
	 * The array will always be the same length as the number of constructor params.
	 * For the example constructor:
	 * <code>MyClass(LoggingService logSvc, @Bound("stringProperty") String someString){...</code>
	 * the Array returned would look like: [null, 'stringProperty'] since the first param doesn't
	 * have the @Bound annotation
	 *
	 * @param constructor The constructor to get the annotation values of.
	 * @return String[] of length equal to the number of constructor params, or a String[0] for a
	 * zero-arg constructor
	 */
	protected String[] getBoundAnnotatedParams(Constructor constructor){
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

	/**
	 * returns a Binding[] containing a binding for each constructor parameter.
	 * @param constructor
	 * @return
	 */
	protected Binding[] resolveConstructorParams(Constructor constructor) {
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

				paramBindings[paramIdx] = buildInnerBinding(paramName)

			} else {
				paramBindings[paramIdx] = buildContextRefBinding(paramType)

			}

		}
		return paramBindings
	}

	protected Binding buildInnerBinding(key){

		BindingBuilder innerBuilder = this.innerBindings.get(key)
		String paramName = key.toString()
		log.debug("Constructor Param: ${paramName} for ${name(impl)} being resolved from an inner binding. Exists?= ${innerBuilder != null}")
		if (!innerBuilder) {
			throw new InvalidConfigurationException("The constructor param: ${paramName} for class: ${name(impl)} could not be resolved. Expected to find an inner Binding, but none was found")
		}
		Binding resolvedBinding = innerBuilder.build()
		log.debug("Built inner binding")
		this.innerBindings.remove(key)
		return resolvedBinding
	}

	protected Binding buildContextRefBinding(Class baseType) {
		log.trace("buildBindingForClass: baseType=${name(baseType)}")
		if (!ctxBuilder.containsBindingFor(baseType)) {
			throw new InvalidConfigurationException("The Constructor for Class: ${name(impl)} has a parameter ")
		}
		Context ctx = ctxBuilder.getContextRef()
		Binding b = new ContextBindingReference(baseType, ctx)
		log.debug("bindingBuilder returning ContextBindingReference for ${name(baseType)}")
		return b
	}



	protected Constructor resolveConstructor(Class clazz) {
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

	protected String name(Class clazz) {
		String name
		if (clazz.isAnonymousClass()) {
			name = "Anonymous implementation of: ${clazz.getSuperclass().getCanonicalName()}"
		} else {
			name = clazz.getCanonicalName()
		}
		name
	}

	protected void validateClassAssignment() {
		if (!impl) {
			throw new InvalidConfigurationException("The Class: ${name(from)} was declared to be bound but the implementation class is null")
		} else if (!from.isAssignableFrom(impl)) {
			throw new InvalidConfigurationException("The Class: ${name(from)} was bound to ${name(impl)} but ${impl.getSimpleName()} is not a ${from.getSimpleName()}")
		}
	}

	/**
	 * Builds into a Binding. This causes all constructor parameters to be resolved into Bindings.
	 * @return
	 * @throws InvalidConfigurationException
	 */
	Binding build() throws InvalidConfigurationException {
		inheritScope(this.ctxBuilder.getDefaultScope())

		/*
		The instance generator could already be set by a call to `toValue(Closure)`. If so, then we'll want to keep it.
		Otherwise, we'll create our own InstanceGenerator.
		 */
		InstanceGenerator gen = this.instanceGenerator

		if (!gen) {
			Constructor constructor = resolveConstructor(this.impl)
			Binding[] params = resolveConstructorParams(constructor)
			gen = new ConstructorInstanceGenerator(this.impl, params, instanceConfigClosure)
		}

		/*
		If there's any innerBindings, they should have been consumed by now. Having any left indicates that extra properties
		were specified in the config.
		 */
		if (!innerBindings.isEmpty()) {
			throw new InvalidConfigurationException("The Binding for class: ${name(from)} has extra ${innerBindings.size()} innerBinding(s) specified. The innerBindings: ${innerBindings.keySet()} are not used by anything. This likely indicates an error in the Context Configuration")
		}

		return createBinding(gen)

	}

	protected Binding createBinding(InstanceGenerator instanceGenerator) {
		Binding b

		if (this.scope == Scope.ALWAYS_CREATE_NEW) {
			b = new BasicBinding(this.from, this.impl, instanceGenerator)
		} else {
			b = new CacheingBinding(instanceGenerator, this.from, this.impl, this.scope)
		}
		return b
	}
}