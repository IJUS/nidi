package net.ijus.nidi
/**
 * Created by pfried on 7/5/14.
 */

public class BasicBinding implements Binding {

	public static final Scope SCOPE = Scope.ALWAYS_CREATE_NEW
	InstanceGenerator instanceGenerator
	Class bound
	Class impl

	BasicBinding(Class boundClass, InstanceGenerator instanceGenerator) {
		this.bound = boundClass
		this.instanceGenerator = instanceGenerator
		this.impl = instanceGenerator.clazz
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
}