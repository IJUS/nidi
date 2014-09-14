package net.ijus.nidi.builder;

import net.ijus.nidi.Context;
import net.ijus.nidi.Inject;
import net.ijus.nidi.InvalidConfigurationException;
import net.ijus.nidi.Require;
import net.ijus.nidi.bindings.*;
import net.ijus.nidi.instantiation.ConstructorInstanceGenerator;
import net.ijus.nidi.instantiation.InstanceGenerator;
import net.ijus.nidi.instantiation.InstanceSetupFunction;
import net.ijus.nidi.instantiation.NullGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.*;

/**
 * Created by pfried on 7/2/14.
 */
public class BindingBuilder<T> {
    private static final Logger log = LoggerFactory.getLogger(BindingBuilder.class);

    /**
     * if the bindingBuilder is finalized, then no other modifications may be made to it.
     */
    private boolean isFinalized = false;

    /**
     * always has a reference to the parent context builder
     */
    private ContextBuilder ctxBuilder;

    /**
     * The base class for this binding. In most cases, an Interface or Abstract class
     */
    private Class<T> baseClass;

    /**
     * Specified scope for the binding. Will inherit from the context builder if not specified
     */
    private Scope scope;

    /**
     * If this property is set, this is the closure that will get called to setup properties on a newly created instance
     */
    private InstanceSetupFunction<? extends T> instanceConfigClosure;

    /**
     * Holds BindingBuilders that are meant to override the bindings in the parent context.
     */
    private Map<Object, BindingBuilder> innerBindings = new LinkedHashMap<Object, BindingBuilder>();

    /**
     * Instance Generator simply provides an instance of the implementation, whatever it may be
     */
    private InstanceGenerator instanceGenerator;

    /**
     * The Class of whatever will be filling the roll of the base class. baseClass.isAssignableFrom(impl) must be true!
     */
    private Class<? extends T> impl;

    /**
     * If this binding simply references another binding in the context, then this property will be set. Either the impl,
     * or the bindingReferenceClass can be set, but never both. Referencing another Binding is for situations where a single
     * concrete implementation will be bound to two separate base classes, and it is important to preserve the scope of that
     * instance, usually when a singleton is desired.
     */
    private Class bindingReferenceClass;
    private Binding binding;


    public BindingBuilder(Class<T> clazz, ContextBuilder ctxBuilder) {
        this.baseClass = clazz;
        this.ctxBuilder = ctxBuilder;
    }

    /**
     * convenience method for creating BindingBuilder with the correct generic type
     * @param clazz
     * @param ctxBuilder
     * @param <E>
     * @return
     */
    public static <E> BindingBuilder<E> create(Class<E> clazz, ContextBuilder ctxBuilder) {
        return new BindingBuilder<E>(clazz, ctxBuilder);
    }

    /**
     * Specifies a closure to be used to setup a new instance of the concrete implementation class.
     * The instance generator will create the instance using bindings in the context in order to resolve constructor parameters.
     * If the implementation class still has setup work after the instance is created, then this is how to do it. This would be used
     * in the case where a Class has a property that doesn't always need set, but occasionally needs overridden. It doesn't always
     * make sense to have such properties declared in the constructor, so this closure setup is provided. In example:
     * <code>
     * class Foo implements Bar {
     * String someProperty = "defaultValue"
     * }
     * <p/>
     * //in ContextConfig
     * bind(Bar).to(Foo).setupInstance{Foo instance->
     * instance.someProperty = "override Value"
     * }
     * </code>
     *
     * @param instanceSetupClosure closure that will be called with the newly created instance as an argument
     * @return this BindingBuilder for chaining
     */
    public BindingBuilder setupInstance(InstanceSetupFunction<? extends T> instanceSetupClosure) {
        checkFinalization();
        this.instanceConfigClosure = instanceSetupClosure;
        return this;
    }

    /**
     * Tells the builder to use the specified class as the implementation for the base class. Optionally, a config
     * closure can be used to specify the scope as well as any inner bindings (overrides and special @Bound params).
     * An example here shows it's use:
     * <code>
     *  Context ctx = Configuration.configureNew{
     *      BindingBuilder builder = bind(MyInterface) //returns a BindingBuilder
     *      builder.to(MyConcreteImpl){ // Normall would just be chained
     *      scope = Scope.ALWAYS_CREATE_NEW
     *      bindConstructorParam('Annotated constructor param name').toValue({ ['custom', 'list'] } as InstanceGenerator)
     *      setupInstance{MyConcreteImpl instance->
     *      instance.setFoo("foo")
     *      instance.setDebugMode(true)
     *      }
     *  }
     * }
     * </code>
     *
     * @param clazz The concrete implementation to be used for this binding
     * @return this BindingBuilder for chained method calls
     */
    public BindingBuilder to(Class<? extends T> clazz) {
        return bindTo(clazz);
    }

    /**
     * alias for to(Class)
     *
     * @param clazz
     * @return
     */
    public BindingBuilder<T> bindTo(Class<? extends T> clazz) {
        checkFinalization();
        this.impl = clazz;
        validateClassAssignment();
        return this;
    }

    /**
     * Used when a single concrete implementation is to be used for two separate interfaces.
     * Say we have two Interfaces, ChargeProcessor and Refund Processor, and one Concrete class, ComplexCCProcessor
     * that implements both. If we want NiDI to use a single instance of our ComplexCCProcessor for both roles, then
     * we would create a normal binding for one, and reference that for the other.
     * <code>
     * //in context config
     * bind(ChargeProcessor).to(ComplexCCProcessor).withScope(Scope.SINGLETON)
     * <p/>
     * //this way you will be guaranteed to always have only a single instance of ComplexCCProcessor that will get used for both roles
     * bind(RefundProcessor).reference(ChargeProcessor)
     * <p/>
     * //this way you would end up with with two instances of ComplexCCProcessor, one for each binding
     * bind(RefundProcessor).to(ComplexCCProcessor)
     * </code>
     * Calling reference will effectively finalize the binding builder. You cannot modify it further.
     *
     * @param otherBaseClass The base class to reference the binding for.
     */
    public void reference(final Class otherBaseClass) {
        checkFinalization();
        if (this.impl != null || this.instanceGenerator != null) {
            throw new InvalidConfigurationException("The BindingBuilder for " + name(baseClass) + " attempted to reference another Binding for " + name(otherBaseClass) + ", but the Implementation class was already specified as: " + name(impl));
        }

        this.bindingReferenceClass = otherBaseClass;
        this.isFinalized = true;
    }

    /**
     * Binds to whatever value is returned from the closure. This Closure will be called multiple times.
     * It will get called immediately in order to validate that the return type of the closure is compatible
     * with the base class for this binding. It will then get called again every time a new instance is created,
     * as determined by the Scope of the Binding.
     *
     * @param generator returns a value to bind to.
     * @return
     */
    public BindingBuilder<T> toValue(InstanceGenerator<? extends T> generator) {
        checkFinalization();
        this.instanceGenerator = generator;

        try {
            T instance = generator.createNewInstance();
            this.impl = (Class<T>) instance.getClass();
            validateClassAssignment();
        } catch (InvalidConfigurationException e) {
            throw e;
        } catch (Exception e) {
            throw new InvalidConfigurationException("Attempted to bind " + name(baseClass) + " to the value of an InstanceGenerator, but the generator threw an exception", e);
        }

        return this;
    }

    /**
     * Conveneince method for binding to an object with a global scope
     * @param obj
     * @return
     */
    public BindingBuilder<T> toObject(final T obj) {
        if (obj == null) {
            return toNull();
        }
        InstanceGenerator<T> gen = new InstanceGenerator<T>() {
            @Override
            public T createNewInstance() {
                return obj;
            }
        };
        BindingBuilder<T> bb = toValue(gen);
        return bb.withScope(Scope.CONTEXT_GLOBAL);
    }

    /**
     * Binds to null. This automatically sets the scope to CONTEXT_GLOBAL and finalizes the BindingBuilder.
     * @return
     */
    public BindingBuilder<T> toNull(){
        checkFinalization();
        this.instanceGenerator = NullGenerator.getInstance();
        this.scope = Scope.CONTEXT_GLOBAL;
        this.setIsFinalized(true);
        return this;
    }

    /**
     * Sets the scope, overriding any default scope set in the parent ContextBuilder
     *
     * @param s
     * @return
     */
    public BindingBuilder<T> withScope(Scope s) {
        checkFinalization();
        this.scope = s;
        return this;
    }

    /**
     * Creates an inner binding, which will only be used for resolving constructor params for this binding.
     *
     * @param param
     * @return
     */
    public BindingBuilder bindConstructorParam(final String param) {
        checkFinalization();
        if (this.impl == null) {
            throw new InvalidConfigurationException("Cannot create bindings for constructor params before the implementation class has been specified");
        }

		/*
         * Since this method is binding to a named property, we'll have to resolve what that is immediately, since
		 * we have no other way of knowing the class
		 */
        Constructor constructor = resolveConstructor(this.impl);
        String[] paramAnnots = getBoundAnnotatedParams(constructor);
        Class paramClass = null;
        for (int i = 0; i < paramAnnots.length; i++) {
            if (paramAnnots[i] != null && paramAnnots[i].equals(param)) {
                paramClass = constructor.getParameterTypes()[i];
            }

        }


        if (paramClass == null) {
            throw new InvalidConfigurationException("Could not find a matching constructor parameter: " + param + ". In order to use bindConstructorParam(" + param + "), the constructor parameter must be annotated with: @Require(\'" + param + "\')");
        }

        BindingBuilder bb = BindingBuilder.create(paramClass, ctxBuilder);
        bb.scope = this.scope;
        this.innerBindings.put(param, bb);
        return bb;
    }

    /**
     * Creates a binding just for this constructor parameter. The constructor must only have one parameter of the given type.
     * If the constructor has 2 or more parameters of one type, then the named property style of binding should be used, and
     * the parameters themselves should be annotated with @Require("<property-name>")
     *
     * @param paramType the class of the constructor parameter
     * @return
     */
    public <E> BindingBuilder<E> bindConstructorParam(final Class<E> paramType) {
        checkFinalization();
        if (impl == null) {
            throw new InvalidConfigurationException("Cannot call bindConstructorParam() yet because the implementation class has not been set");
        }

        Constructor constructor = resolveConstructor(this.impl);
        Class[] types = constructor.getParameterTypes();

        //check to make sure the parameters have exactly one occurrence of the specified class
        int paramsOfType = 0;
        for (Class c : types) {
            if (c.equals(paramType)) {
                paramsOfType++;
            }
        }

        if (paramsOfType == 0) {
            throw new InvalidConfigurationException("called bindConstructorParam() with an invalid Class argument. The class: " + name(paramType) + " is not a parameter in the resolved constructor for: " + name(impl));

        } else if (paramsOfType > 1) {
            throw new InvalidConfigurationException("The Constructor for " + name(impl) + " takes" + paramsOfType + " parameters of type: " + name(paramType) + ". Cannot use Class to identify params, must use @Require(\'<name>\') instead");
        }


        // ok, now that that's out of the way, we can just return a new bindingBuilder
        BindingBuilder<E> bb = BindingBuilder.create(paramType, this.ctxBuilder);
        bb.scope = this.scope;
        this.innerBindings.put(paramType, bb);
        return bb;

    }

    /**
     * Sets the specified scope only if the current scope is null
     *
     * @param s
     */
    protected void inheritScope(Scope s) {
        if (this.scope == null) {
            this.scope = s;
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
    protected String[] getBoundAnnotatedParams(Constructor constructor) {
        Annotation[][] allAnnotations = constructor.getParameterAnnotations();

        String[] boundParams = new String[allAnnotations.length];

        for (int outer = 0; outer < allAnnotations.length; outer++) {
            Annotation[] paramAnnotations = allAnnotations[outer];
            for (int inner = 0; inner < paramAnnotations.length; inner++) {
                Annotation a = paramAnnotations[inner];
                if (a instanceof Require) {
                    boundParams[outer] = ((Require) a).value();
                }

            }

        }


        return boundParams;
    }

    /**
     * returns a Binding[] containing a binding for each constructor parameter.
     *
     * @param constructor
     * @return
     */
    protected Binding[] resolveConstructorParams(Constructor constructor) {
        Class[] constructorParams = constructor.getParameterTypes();
        if (constructorParams.length == 0) {
            return new Binding[0];
        }


        Binding[] paramBindings = new Binding[constructorParams.length];

        String[] boundAnnotationValues = getBoundAnnotatedParams(constructor);


        for (int paramIdx = 0; paramIdx < constructorParams.length; paramIdx++) {

            final Class paramType = constructorParams[paramIdx];

            if (boundAnnotationValues[paramIdx] != null) {
                //This Constructor parameter has a @Require annotation
                String paramName = boundAnnotationValues[paramIdx];

                paramBindings[paramIdx] = buildPropertyBinding(paramName);

            } else if (innerBindings.containsKey(paramType)) {
                //Binding for this constructor param has been overridden
                paramBindings[paramIdx] = buildNormalInnerBinding(paramType);

            } else if (ctxBuilder.containsBindingFor(paramType)) {
                //This constructor param is not annotated and is not overridden in the innerBindings
                //This means we have to look in the context for the correct binding
                paramBindings[paramIdx] = buildContextRefBinding(paramType);

            } else {
                //Oh no!
                throw new InvalidConfigurationException("The Constructor for " + name(impl) + " requires a parameter of type: " + name(paramType) + ", but no Binding for this class could be found");
            }


        }

        return paramBindings;
    }

    /**
     * Builds an inner binding for the specified class.
     * When the constructor for the impl class is to use bindings that have been overridden for this class.
     *
     * @param clazz
     * @return
     */
    protected Binding buildNormalInnerBinding(final Class clazz) {
        BindingBuilder bb = this.innerBindings.get(clazz);
        if (bb == null) {
            throw new InvalidConfigurationException("Expected to find an inner binding for " + name(clazz) + ", but none was found");
        }

        innerBindings.remove(clazz);
        return bb.build();
    }

    protected Binding buildPropertyBinding(final String key) {

        BindingBuilder propertyBuilder = null;
        if (innerBindings.containsKey(key)) {
            log.debug("Resolving Property binding for " + key + " using an inner binding");
            propertyBuilder = innerBindings.get(key);
        } else if (ctxBuilder.containsBindingFor(key)) {
            log.debug("resolving property binding for " + key + " using a binding found in the context");
            propertyBuilder = ctxBuilder.getCtxBindings().get(key);
        }


        if (propertyBuilder == null) {
            throw new InvalidConfigurationException("The constructor param: " + key + " for class: " + name(impl) + " could not be resolved. Expected to find an inner Binding, but none was found");
        }


        Binding resolvedBinding = propertyBuilder.build();
        log.debug("Built inner binding");
        this.innerBindings.remove(key);
        return resolvedBinding;
    }

    /**
     * Builds a binding for a constructor param that references a binding in the constructor.
     *
     * @param refClass
     * @return
     */
    protected <E> Binding<E> buildContextRefBinding(Class refClass, Class<E> provides) {
        log.trace("buildContextRefBinding: baseType=" + name(refClass));
        if (!ctxBuilder.containsBindingFor(refClass)) {
            throw new InvalidConfigurationException("Attempted to reference a Binding for " + String.valueOf(refClass) + " in the ContextBuilder, but no Binding for that class has been declared");
        }

        Context ctx = ctxBuilder.getContextRef();
        Binding<E> b = new ContextBindingReference<E>(refClass, ctx, provides);
        log.debug("bindingBuilder returning ContextBindingReference for " + name(refClass));
        return b;
    }

    /**
     * Builds a binding for a constructor param that references a binding in the constructor.
     * This creates a contextRefBinding that has the given refClass both as the referencedClass
     * AND as the baseClass (what it provides)
     *
     * @param refClass
     * @return
     */
    protected Binding buildContextRefBinding(Class refClass) {
        return buildContextRefBinding(refClass, refClass);
    }

    /**
     * For a Normal binding, figures out which constructor to use for the given class
     *
     * @param clazz
     * @return
     */
    protected <E> Constructor<E> resolveConstructor(final Class<E> clazz) {
        Constructor[] constructors = clazz.getConstructors();
        Constructor<E> constructor;
        if (constructors.length == 1) {
            constructor = constructors[0];

        } else if (constructors.length > 1) {
            List<Constructor> withAnno = new LinkedList<Constructor>();
            for (Constructor c : constructors) {
                if (c.isAnnotationPresent(Inject.class)) {
                    withAnno.add(c);
                }
            }

            if (withAnno.size() != 1) {
                throw new InvalidConfigurationException("The Class: " + name(clazz) + " has more than one constructor, so exactly one Constructor should have the @Inject annotation. Found " + withAnno.size() + " Constructors with that annotation.");
            }

            constructor = withAnno.get(0);
        } else {
            throw new InvalidConfigurationException("The Class: " + name(clazz) + " has no public constructors");
        }

        return constructor;
    }

    protected String name(final Class clazz) {
        String name;
        if (clazz.isAnonymousClass()) {
            name = "Anonymous implementation of: " + clazz.getSuperclass().getCanonicalName();
        } else {
            name = clazz.getCanonicalName();
        }

        return name;
    }

    /**
     * makes sure nobody tried to bind to an incompatible class
     */
    public void validateClassAssignment() {
        if (!isBoundToNull()) {
            if (impl == null && bindingReferenceClass == null) {
                throw new InvalidConfigurationException("The Class: " + name(baseClass) + " was declared to be bound but the implementation class is null");

            } else if (impl != null && !baseClass.isAssignableFrom(impl)) {
                throw new InvalidConfigurationException("The Class: " + name(baseClass) + " was bound to " + name(impl) + " but it is not a " + baseClass.getSimpleName());

            } else if (impl != null && Modifier.isAbstract(impl.getModifiers())) {
                throw new InvalidConfigurationException("The Class " + name(baseClass) + " was bound to the abstract class: " + name(impl) + ". The implementation MUST be a concrete class");
            }
        }

    }

    /**
     * Builds into a Binding. This causes all constructor parameters to be resolved into Bindings.
     *
     * @return
     * @throws net.ijus.nidi.InvalidConfigurationException
     */
    public Binding build() throws InvalidConfigurationException {
        log.trace("Started building Binding for " + name(this.baseClass));
        inheritScope(this.ctxBuilder.getDefaultScope());
        this.isFinalized = true;
        Binding b = null;
        if (this.bindingReferenceClass != null) {
            b = buildContextRefBinding(this.bindingReferenceClass, this.baseClass);

        } else if (this.impl != null) {
            b = buildNormalBinding();

        } else if (isBoundToNull()){
            b = buildNullBinding();

        } else {
            //oh no! what do we do?
            throw new InvalidConfigurationException("Attempted to build the binding for " + name(this.baseClass) + " but no implementation has been specified");
        }

        log.debug("Finished building Binding for {}, result= {}", this.baseClass.getName(), b);
        return b;
    }

    protected Binding buildNullBinding() {
        return new BasicBinding(this.baseClass, null, this.instanceGenerator);

    }

    /**
     * If the target of this BindingBuilder is determined to be a class, then this is what will build the binding for it
     *
     * @return
     */
    protected Binding buildNormalBinding() {
		/*
		The instance generator could already be set by a call to `toValue(Closure)`. If so, then we'll want to keep it.
		Otherwise, we'll create our own InstanceGenerator.
		 */
        InstanceGenerator gen = this.instanceGenerator;

        if (gen == null) {
            Constructor constructor = resolveConstructor(this.impl);
            Binding[] params = resolveConstructorParams(constructor);
            gen = new ConstructorInstanceGenerator(this.impl, params, instanceConfigClosure);
        }


		/*
		If there's any innerBindings, they should have been consumed by now. Having any left indicates that extra properties
		were specified in the config.
		 */
        if (!innerBindings.isEmpty()) {
            throw new InvalidConfigurationException("The Binding for class: " + name(baseClass) + " has extra " + String.valueOf(innerBindings.size()) + " innerBinding(s) specified. The innerBindings: " + String.valueOf(innerBindings.keySet()) + " are not used by anything. This likely indicates an error in the Context Configuration");
        }


        return createBindingForInstanceGenerator(gen);
    }

    protected Binding createBindingForInstanceGenerator(InstanceGenerator instanceGenerator) {
        Binding b;

        if (this.scope.equals(Scope.ALWAYS_CREATE_NEW)) {
            b = new BasicBinding(this.baseClass, this.impl, instanceGenerator);
        } else {
            b = new CachingBinding(instanceGenerator, this.baseClass, this.impl, this.scope);
        }

        return b;
    }

    protected void checkFinalization() throws InvalidConfigurationException {
        if (this.isFinalized) {
            throw new InvalidConfigurationException("Attempted to modify a BindingBuilder that has already been finalized. No means no!");
        }

    }

    public static Logger getLog() {
        return log;
    }

    public boolean getIsFinalized() {
        return isFinalized;
    }

    public boolean isIsFinalized() {
        return isFinalized;
    }

    public void setIsFinalized(boolean isFinalized) {
        this.isFinalized = isFinalized;
    }

    public ContextBuilder getCtxBuilder() {
        return ctxBuilder;
    }

    public void setCtxBuilder(ContextBuilder ctxBuilder) {
        this.ctxBuilder = ctxBuilder;
    }

    public Class<T> getBaseClass() {
        return baseClass;
    }

    public void setBaseClass(Class<T> baseClass) {
        this.baseClass = baseClass;
    }

    public Scope getScope() {
        return scope;
    }

    public void setScope(Scope scope) {
        this.scope = scope;
    }

    public InstanceSetupFunction<? extends T> getInstanceConfigClosure() {
        return instanceConfigClosure;
    }

    public void setInstanceConfigClosure(InstanceSetupFunction<? extends T> instanceConfigClosure) {
        this.instanceConfigClosure = instanceConfigClosure;
    }

    public Map<Object, BindingBuilder> getInnerBindings() {
        return innerBindings;
    }

    public void setInnerBindings(Map<Object, BindingBuilder> innerBindings) {
        this.innerBindings = innerBindings;
    }

    public InstanceGenerator getInstanceGenerator() {
        return instanceGenerator;
    }

    public void setInstanceGenerator(InstanceGenerator instanceGenerator) {
        this.instanceGenerator = instanceGenerator;
    }

    public boolean isBoundToNull(){
        return this.instanceGenerator != null && this.instanceGenerator instanceof NullGenerator;
    }

    public Class<? extends T> getImpl() {
        return impl;
    }

    public void setImpl(Class<? extends T> impl) {
        this.impl = impl;
    }

    public Class getBindingReferenceClass() {
        return bindingReferenceClass;
    }

    public void setBindingReferenceClass(Class bindingReferenceClass) {
        this.bindingReferenceClass = bindingReferenceClass;
    }

    public Binding getBinding() {
        return binding;
    }

    public void setBinding(Binding binding) {
        this.binding = binding;
    }


}
