package net.ijus.nidi.instantiation;

/**
 * Created by pfried on 9/12/14.
 */
public interface InstanceSetupFunction<T> {

    public abstract void setup(T instance);
}
