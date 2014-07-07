package net.ijus.nidi
/**
 * Created by pfried on 7/5/14.
 */

public class BasicBinding implements Binding {

	public static Scope SCOPE = Scope.ALWAYS_CREATE_NEW
	InstanceGenerator instanceGenerator
	Class bound
	Class impl

	BasicBinding(Class boundClass, Class impl, Binding[] constructorParams = null) {
		this.bound = boundClass
		this.impl = impl
		this.instanceGenerator = new InstanceGenerator(impl, constructorParams)
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
}