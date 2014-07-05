package net.ijus.nidi

/**
 * Created by pfried on 6/16/14.
 */
public interface Binding<T> {

	T getInstance()

	Class<T> getImplClass()

	Class getBoundClass()
}