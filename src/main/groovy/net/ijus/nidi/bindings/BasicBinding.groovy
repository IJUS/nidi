package net.ijus.nidi.bindings

import net.ijus.nidi.instantiation.InstanceGenerator
import net.ijus.nidi.bindings.Binding

/**
 * Created by pfried on 7/5/14.
 */

public class BasicBinding<T> implements Binding<T> {

	public static final Scope SCOPE = Scope.ALWAYS_CREATE_NEW
	InstanceGenerator<T> instanceGenerator
	Class<T> bound
	Class<? extends T> impl

	BasicBinding(Class<T> boundClass, Class<? extends T> implClass, InstanceGenerator instanceGenerator) {
		this.bound = boundClass
		this.instanceGenerator = instanceGenerator
		this.impl = implClass
	}

	@Override
	T getInstance() {
		return instanceGenerator.createNewInstance()
	}

	@Override
	Class<? extends T> getImplClass() {
		return impl
	}

	@Override
	Class<T> getBoundClass() {
		return bound
	}

	@Override
	Scope getScope() {
		SCOPE
	}

	@Override
	void validate() {
		throw new Exception("method validate() has not been implemented yet. write me!")
	}

	@Override
	InstanceGenerator<T> getInstanceGenerator() {
		return instanceGenerator
	}
}