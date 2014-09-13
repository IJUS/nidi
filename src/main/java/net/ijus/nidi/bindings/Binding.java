package net.ijus.nidi.bindings;

import net.ijus.nidi.instantiation.InstanceGenerator;

/**
 * Created by pfried on 6/16/14.
 */
public interface Binding<T> {
    public abstract T getInstance();

    public abstract Class<? extends T> getImplClass();

    public abstract Class<T> getBoundClass();

    public abstract Scope getScope();

    public abstract void validate();

    public abstract InstanceGenerator<T> getInstanceGenerator();
}
