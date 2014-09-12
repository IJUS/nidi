package net.ijus.nidi.builder

import net.ijus.nidi.*
import net.ijus.nidi.bindings.Binding
import net.ijus.nidi.bindings.*
import net.ijus.nidi.instantiation.ConstructorInstanceGenerator
import net.ijus.nidi.instantiation.InstanceGenerator
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.lang.annotation.Annotation
import java.lang.reflect.Constructor
import java.lang.reflect.Modifier

/**
 * Created by pfried on 7/2/14.
 */

class BindingBuilder {
	static final Logger log = LoggerFactory.getLogger(BindingBuilder)

	/**
	 * if the bindingBuilder is finalized, then no other modifications may be made to it.
	 */
	boolean isFinalized = false

	/**
	 * always has a reference to the parent context builder
	 */
	ContextBuilder ctxBuilder

	/**
	 * The base class for this binding. In most cases, an Interface or Abstract class
	 */
	Class baseClass

	/**
	 * Specified scope for the binding. Will inherit from the context builder if not specified
	 */
	Scope scope

	/**
	 * If this property is set, this is the closure that will get called to setup properties on a newly created instance
	 */
	Closure instanceConfigClosure

	/**
	 * Holds BindingBuilders that are meant to override the bindings in the parent context.
	 */
	Map<Object, BindingBuilder> innerBindings = [:]


	////////// Ways to specify an implementation. One of these must be set in order to create a valid binding ///////////
	/**
	 * Instance Generator simply provides an instance of the implementation, whatever it may be
	 */
	InstanceGenerator instanceGenerator

	/**
	 * The Class of whatever will be filling the roll of the base class. baseClass.isAssignableFrom(impl) must be true!
	 */
	Class impl

	/**
	 * If this binding simply references another binding in the context, then this property will be set. Either the impl,
	 * or the bindingReferenceClass can be set, but never both. Referencing another Binding is for situations where a single
	 * concrete implementation will be bound to two separate base classes, and it is important to preserve the scope of that
	 * instance, usually when a singleton is desired.
	 */
	Class bindingReferenceClass //set if reference() is called to reference another binding

	Binding binding

	BindingBuilder(Class clazz, ContextBuilder ctxBuilder) {
		this.baseClass = clazz
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
		checkFinalization()
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
		checkFinalization()
		this.impl = clazz
		validateClassAssignment()

		if (config) {
			config.setDelegate(this)
			config.call()
		}
		return this
	}

	/**
	 * Used when a single concrete implementation is to be used for two separate interfaces.
	 * Say we have two Interfaces, ChargeProcessor and Refund Processor, and one Concrete class, ComplexCCProcessor
	 * that implements both. If we want NiDI to use a single instance of our ComplexCCProcessor for both roles, then
	 * we would create a normal binding for one, and reference that for the other.
	 * <code>
	 *     //in context config
	 *     bind(ChargeProcessor).to(ComplexCCProcessor).withScope(Scope.SINGLETON)
	 *
	 *     //this way you will be guaranteed to always have only a single instance of ComplexCCProcessor that will get used for both roles
	 *     bind(RefundProcessor).reference(ChargeProcessor)
	 *
	 *     //this way you would end up with with two instances of ComplexCCProcessor, one for each binding
	 *     bind(RefundProcessor).to(ComplexCCProcessor)
	 * </code>
	 * Calling reference will effectively finalize the binding builder. You cannot modify it further.
	 *
	 * @param otherBaseClass The base class to reference the binding for.
	 */
	void reference(Class otherBaseClass) {
		checkFinalization()
		if (this.impl || this.instanceGenerator) {
			throw new InvalidConfigurationException("The BindingBuilder for ${name(baseClass)} attempted to reference another Binding for ${name(otherBaseClass)}, but the Implementation class was already specified as: ${name(impl)}")
		}

		this.bindingReferenceClass = otherBaseClass
		this.isFinalized = true
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
		checkFinalization()
		this.instanceGenerator = closure as InstanceGenerator

		try {
			def instance = closure.call()
			this.impl = instance.getClass()
			validateClassAssignment()
		} catch (InvalidConfigurationException e){
			throw e
		} catch (Exception e) {
			throw new InvalidConfigurationException("Attempted to bind ${name(baseClass)} to the value of a closure, but the closure threw an exception", e)
		}
		return this
	}

	/**
	 * Sets the scope, overriding any default scope set in the parent ContextBuilder
	 * @param s
	 * @return
	 */
	BindingBuilder withScope(Scope s) {
		checkFinalization()
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
		checkFinalization()
		if (!this.impl) {
			throw new InvalidConfigurationException("Cannot create bindings for constructor params before the implementation class has been specified")
		}
		/*
		 * Since this method is binding to a named property, we'll have to resolve what that is immediately, since
		 * we have no other way of knowing the class
		 */
		Constructor constructor = resolveConstructor(this.impl)
		String[] paramAnnots = getBoundAnnotatedParams(constructor)
		Class paramClass
		for (int i = 0; i < paramAnnots.length; i++) {
			if (paramAnnots[i] == param) {
				paramClass = constructor.getParameterTypes()[i]
			}
		}

		if (!paramClass) {
			throw new InvalidConfigurationException("Could not find a matching constructor parameter: ${param}. In order to use bindConstructorParam(${param}), the constructor parameter must be annotated with: @Require('${param}')")
		}

		BindingBuilder bb = new BindingBuilder(paramClass, this.ctxBuilder)
		bb.scope = this.scope
		this.innerBindings.put(param, bb)
		return bb
	}

	/**
	 * Creates a binding just for this constructor parameter. The constructor must only have one parameter of the given type.
	 * If the constructor has 2 or more parameters of one type, then the named property style of binding should be used, and
	 * the parameters themselves should be annotated with @Require("<property-name>")
	 *
	 * @param paramType the class of the constructor parameter
	 * @return
	 */
	BindingBuilder bindConstructorParam(Class paramType){
		checkFinalization()
		if (!impl) {
			throw new InvalidConfigurationException("Cannot call bindConstructorParam() yet because the implementation class has not been set")
		}
		Constructor constructor = resolveConstructor(this.impl)
		Class[] types = constructor.getParameterTypes()

		//check to make sure the parameters have exactly one occurrence of the specified class
		if (!types.contains(paramType)) {
			throw new InvalidConfigurationException("called bindConstructorParam() with an invalid Class argument. The class: ${name(paramType)} is not a parameter in the resolved constructor for: ${name(impl)}")

		} else if (types.count { it == paramType } > 1) {
			throw new InvalidConfigurationException("The Constructor for ${name(impl)} takes multiple parameters of type: ${name(paramType)}. Cannot use Class to identify params, must use @Require('<name>') instead")
		}

		// ok, now that that's out of the way, we can just return a new bindingBuilder
		BindingBuilder bb = new BindingBuilder(paramType, this.ctxBuilder)
		bb.scope = this.scope
		this.innerBindings.put(paramType, bb)
		return bb

	}

	////////////////// porcelain (above) /////////////////////

	////////////////// plumbing (below) //////////////////////

	/**
	 * Sets the specified scope only if the current scope is null
	 * @param s
	 */
	protected void inheritScope(Scope s) {
		if (!this.scope) {
			this.scope = s
		}
	}

	/// stuff for resolving constructor params (below)

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
				if (a instanceof Require) {
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
				//This Constructor parameter has a @Require annotation
				String paramName = boundAnnotationValues[paramIdx]

				paramBindings[paramIdx] = buildPropertyBinding(paramName)

			} else if (innerBindings.containsKey(paramType)) {
				//Binding for this constructor param has been overridden
				paramBindings[paramIdx] = buildNormalInnerBinding(paramType)

			} else if (ctxBuilder.containsBindingFor(paramType)) {
				//This constructor param is not annotated and is not overridden in the innerBindings
				//This means we have to look in the context for the correct binding
				paramBindings[paramIdx] = buildContextRefBinding(paramType)

			} else {
				//Oh no!
				throw new InvalidConfigurationException("The Constructor for ${name(impl)} requires a parameter of type: ${name(paramType)}, but no Binding for this class could be found")
			}

		}
		return paramBindings
	}

	/**
	 * Builds an inner binding for the specified class.
	 * When the constructor for the impl class is to use bindings that have been overridden for this class.
	 * @param clazz
	 * @return
	 */
	protected Binding buildNormalInnerBinding(Class clazz) {
		BindingBuilder bb = this.innerBindings.get(clazz)
		if (!bb) {
			throw new InvalidConfigurationException("Expected to find an inner binding for ${name(clazz)}, but none was found")
		}
		innerBindings.remove(clazz)
		return bb.build()
	}

	protected Binding buildPropertyBinding(String key){

		BindingBuilder propertyBuilder
		if (innerBindings.containsKey(key)) {
			log.debug("Resolving Property binding for ${key} using an inner binding")
			propertyBuilder = innerBindings.get(key)
		} else if (ctxBuilder.containsBindingFor(key)) {
			log.debug("resolving property binding for ${key} using a binding found in the context")
			propertyBuilder = ctxBuilder.ctxBindings.get(key)
		}

		if (!propertyBuilder) {
			throw new InvalidConfigurationException("The constructor param: ${key} for class: ${name(impl)} could not be resolved. Expected to find an inner Binding, but none was found")
		}

		Binding resolvedBinding = propertyBuilder.build()
		log.debug("Built inner binding")
		this.innerBindings.remove(key)
		return resolvedBinding
	}

	/**
	 * Builds a binding for a constructor param that references a binding in the constructor.
	 * @param refClass
	 * @param provides
	 * @return
	 */
	protected Binding buildContextRefBinding(Class refClass, Class provides = null) {
		log.trace("buildContextRefBinding: baseType=${name(refClass)}")
		if (!ctxBuilder.containsBindingFor(refClass)) {
			throw new InvalidConfigurationException("Attempted to reference a Binding for ${refClass} in the ContextBuilder, but no Binding for that class has been declared")
		}
		Context ctx = ctxBuilder.getContextRef()
		Binding b = new ContextBindingReference(refClass, ctx, provides)
		log.debug("bindingBuilder returning ContextBindingReference for ${name(refClass)}")
		return b
	}

	/// end stuff for resolving constructor params


	/**
	 * For a Normal binding, figures out which constructor to use for the given class
	 * @param clazz
	 * @return
	 */
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

	/**
	 * makes sure nobody tried to bind to an incompatible class
	 */
	void validateClassAssignment() {
		if (!impl && !bindingReferenceClass) {
			throw new InvalidConfigurationException("The Class: ${name(baseClass)} was declared to be bound but the implementation class is null")

		} else if (impl && !baseClass.isAssignableFrom(impl)) {
			throw new InvalidConfigurationException("The Class: ${name(baseClass)} was bound to ${name(impl)} but it is not a ${baseClass.getSimpleName()}")

		} else if (impl && Modifier.isAbstract(impl.getModifiers())) {
			throw new InvalidConfigurationException("The Class ${name(baseClass)} was bound to the abstract class: ${name(impl)}. The implementation MUST be a concrete class")
		}

	}

	/**
	 * Builds into a Binding. This causes all constructor parameters to be resolved into Bindings.
	 * @return
	 * @throws InvalidConfigurationException
	 */
	Binding build() throws InvalidConfigurationException {
		log.trace("Started building Binding for ${name(this.baseClass)}")
		inheritScope(this.ctxBuilder.getDefaultScope())
		this.isFinalized = true
		Binding b
		if (this.bindingReferenceClass) {
			b = buildContextRefBinding(this.bindingReferenceClass, this.baseClass)

		} else if (this.impl) {
			b = buildNormalBinding()

		} else {
			//oh no! what do we do?
			throw new InvalidConfigurationException("Attempted to build the binding for ${name(this.baseClass)} but no implementation has been specified")
		}
		log.debug("Finished building Binding for ${name(this.baseClass)}, result= ${b}")
		return b
	}

	/**
	 * If the target of this BindingBuilder is determined to be a class, then this is what will build the binding for it
	 * @return
	 */
	protected Binding buildNormalBinding(){
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
			throw new InvalidConfigurationException("The Binding for class: ${name(baseClass)} has extra ${innerBindings.size()} innerBinding(s) specified. The innerBindings: ${innerBindings.keySet()} are not used by anything. This likely indicates an error in the Context Configuration")
		}

		return createBindingForInstanceGenerator(gen)
	}

	protected Binding createBindingForInstanceGenerator(InstanceGenerator instanceGenerator) {
		Binding b

		if (this.scope == Scope.ALWAYS_CREATE_NEW) {
			b = new BasicBinding(this.baseClass, this.impl, instanceGenerator)
		} else {
			b = new CacheingBinding(instanceGenerator, this.baseClass, this.impl, this.scope)
		}
		return b
	}

	protected void checkFinalization() throws InvalidConfigurationException {
		if (this.isFinalized) {
			throw new InvalidConfigurationException("Attempted to modify a BindingBuilder that has already been finalized. No means no!")
		}
	}
}