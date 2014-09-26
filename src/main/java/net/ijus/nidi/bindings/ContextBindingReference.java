package net.ijus.nidi.bindings;

import net.ijus.nidi.Context;
import net.ijus.nidi.InvalidConfigurationException;
import net.ijus.nidi.instantiation.InstanceGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Represents a reference to another binding in the Context. Every time a Class' constructor requires another class, a new
 * Binding must be created. Say we have a Class, 'Foo' that is bound in the main context. That's one binding. If we also have
 * Class, 'Bar' that requires a Foo instance, then we will create a separate binding for it. Since we would normally want to
 * respect the Scoping of the original Binding, we use a ContextBindingReference to reference the original binding.
 *
 * This can also be used to handle situations where a single concrete implementation is used for multiple base classes. In
 * order to respect the Scope, a ContextBindingReference should be used. This is why there are two fields, one for referencedClass, and another for provides.
 */
public class ContextBindingReference<T> implements Binding<T> {
    private static final Logger log = LoggerFactory.getLogger(ContextBindingReference.class);
    /**
     * referencedClass can be anything. It doesn't necessarily have to extend T
     */
    private Class referencedClass;
    /**
     * The context to get the binding from
     */
    private Context ctx;
    /**
     * The resolved Binding must provide a T as its impl class in order to be valid
     */
    private Binding<T> resolvedBinding;
    /**
     * This is the baseClass
     */
    private Class<T> provides;


    public ContextBindingReference(Class refClass, Context ctx, Class<T> provides) {
        this.referencedClass = refClass;
        this.ctx = ctx;
        this.provides = provides;

    }

    @Override
    public T getInstance() {
        return getResolvedBinding().getInstance();
    }

    @Override
    public Class<? extends T> getImplClass() {
        return getResolvedBinding().getImplClass();
    }

    @Override
    public Class<T> getBoundClass() {
        return this.provides;
    }

    @Override
    public Scope getScope() {
        return getResolvedBinding().getScope();
    }

    @Override
    public InstanceGenerator<T> getInstanceGenerator() {
        return getResolvedBinding().getInstanceGenerator();
    }

    @Override
    public void validate() {
        Binding b = getResolvedBinding();
        b.validate();
    }

    public Binding<T> getResolvedBinding() {
        if (resolvedBinding == null) {
            this.resolvedBinding = createResolvedBinding();
        }

        return resolvedBinding;
    }

    public Binding createResolvedBinding() {
        Binding b = ctx.getBinding(this.referencedClass);
        if (b == null) {
            throw new InvalidConfigurationException("The Context does not contain a Binding for class: " + referencedClass.getName() + ". Perhaps the referenced Binding trying to be created to early");
        }

        final Scope s = b.getScope();
        log.debug("Resolving binding for class: {} with scope: {}", this.referencedClass.getName(), s);

        /*
        Depending on the Scope, we may need to wrap the resolved binding in a CachingBinding
         */
        if (s.equals(Scope.ONE_PER_BINDING)) {
            b = new CachingBinding(b.getInstanceGenerator(), this.referencedClass, b.getImplClass(), Scope.ONE_PER_BINDING);
        }

        return b;
    }

    public static Logger getLog() {
        return log;
    }

    public Class getReferencedClass() {
        return referencedClass;
    }

    public void setReferencedClass(Class referencedClass) {
        this.referencedClass = referencedClass;
    }

    public Context getCtx() {
        return ctx;
    }

    public void setCtx(Context ctx) {
        this.ctx = ctx;
    }

    public void setResolvedBinding(Binding<T> resolvedBinding) {
        this.resolvedBinding = resolvedBinding;
    }

    public Class<T> getProvides() {
        return provides;
    }

    public void setProvides(Class<T> provides) {
        this.provides = provides;
    }

}
