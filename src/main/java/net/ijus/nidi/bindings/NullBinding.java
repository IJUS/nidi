package net.ijus.nidi.bindings;

import net.ijus.nidi.instantiation.InstanceGenerator;
import net.ijus.nidi.instantiation.NullGenerator;

/**
 * Created by pfried on 9/13/14.
 */
public class NullBinding<T> implements Binding<T> {

    Class<T> baseClass;

    public NullBinding(Class<T> baseClass) {
        this.baseClass = baseClass;
    }

    @Override
    public T getInstance() {
        return null;
    }

    @Override
    public Class getImplClass() {
        return null;
    }

    @Override
    public Class<T> getBoundClass() {
        return baseClass;
    }

    @Override
    public Scope getScope() {
        return Scope.CONTEXT_GLOBAL;
    }

    @Override
    public void validate() {

    }

    @Override
    public InstanceGenerator getInstanceGenerator() {
        return NullGenerator.getInstance();
    }
}
