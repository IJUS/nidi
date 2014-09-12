package net.ijus.nidi.instantiation;

/**
 * Created by pfried on 7/15/14.
 */
public interface InstanceGenerator<T> {

    public abstract T createNewInstance();
}
