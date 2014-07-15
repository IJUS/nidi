package net.ijus.nidi.instantiation;

import groovy.transform.CompileStatic

/**
 * Created by pfried on 7/5/14.
 */

@CompileStatic
public class ConstructorInstanceGenerator {

	net.ijus.nidi.bindings.Binding[] constructorArgs
	Class clazz
	Closure setup

	ConstructorInstanceGenerator(Class clazz, net.ijus.nidi.bindings.Binding[] constructorArgs = null, Closure setup = null) {
		this.clazz = clazz
		this.constructorArgs = constructorArgs
		this.setup = setup
	}

	ConstructorInstanceGenerator(Class clazz, Closure setup) {
		this(clazz, null, setup)
	}

	def createNewInstance(){
		def instance
		if (constructorArgs && constructorArgs.length > 0) {
			def args = constructorArgs.collect{net.ijus.nidi.bindings.Binding b-> b.getInstance()}
			instance = clazz.newInstance(args as Object[])
		} else {
			instance = clazz.newInstance()
		}

		if (setup) {
			setup.setDelegate(instance)
			setup.call(instance)
		}
		instance
	}

}