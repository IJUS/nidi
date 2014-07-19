package net.ijus.nidi.bindings

import net.ijus.nidi.instantiation.InstanceGenerator

/**
 * Created by pfried on 7/5/14.
 */

public class BasicBinding implements Binding {

	public static final Scope SCOPE = Scope.ALWAYS_CREATE_NEW
	InstanceGenerator instanceGenerator
	Class bound
	Class impl

	BasicBinding(Class boundClass, Class implClass, InstanceGenerator instanceGenerator) {
		this.bound = boundClass
		this.instanceGenerator = instanceGenerator
		this.impl = implClass
	}

	@Override
	def getInstance() {
		return instanceGenerator.createNewInstance()
	}

	@Override
	Class getImplClass() {
		return impl
	}

	@Override
	Class getBoundClass() {
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
	InstanceGenerator getInstanceGenerator() {
		return instanceGenerator
	}
}