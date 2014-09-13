package net.ijus.nidi.bindings;

import net.ijus.nidi.instantiation.InstanceGenerator;

/**
 * The simplest binding of all.
 */
public class BasicBinding<T> implements Binding<T> {
    public BasicBinding(Class<T> boundClass, Class<? extends T> implClass, InstanceGenerator instanceGenerator) {
        this.bound = boundClass;
        this.instanceGenerator = instanceGenerator;
        this.impl = implClass;
    }

    @Override
    public T getInstance() {
        return instanceGenerator.createNewInstance();
    }

    @Override
    public Class<? extends T> getImplClass() {
        return impl;
    }

    @Override
    public Class<T> getBoundClass() {
        return bound;
    }

    @Override
    public Scope getScope() {
        return SCOPE;
    }

    @Override
    public void validate() {
        throw new RuntimeException("method validate() has not been implemented yet. write me!");
    }

    @Override
    public InstanceGenerator<T> getInstanceGenerator() {
        return instanceGenerator;
    }

    public void setInstanceGenerator(InstanceGenerator<T> instanceGenerator) {
        this.instanceGenerator = instanceGenerator;
    }

    public Class<T> getBound() {
        return bound;
    }

    public void setBound(Class<T> bound) {
        this.bound = bound;
    }

    public Class<? extends T> getImpl() {
        return impl;
    }

    public void setImpl(Class<? extends T> impl) {
        this.impl = impl;
    }

    public static final Scope SCOPE = Scope.ALWAYS_CREATE_NEW;
    private InstanceGenerator<T> instanceGenerator;
    private Class<T> bound;
    private Class<? extends T> impl;
}
