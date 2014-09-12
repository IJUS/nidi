package net.ijus.nidi.bindings;

import net.ijus.nidi.instantiation.InstanceGenerator;

/**
 * Created by pfried on 6/16/14.
 */
public interface Binding {
    public abstract Object getInstance();

    public abstract Class getImplClass();

    public abstract Class getBoundClass();

    public abstract Scope getScope();

    public abstract void validate();

    public abstract InstanceGenerator getInstanceGenerator();
}
