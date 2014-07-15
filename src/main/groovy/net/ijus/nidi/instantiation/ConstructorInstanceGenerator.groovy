package net.ijus.nidi.instantiation;

import groovy.transform.CompileStatic

import net.ijus.nidi.bindings.Binding
import groovy.transform.CompileStatic

/**
 * Created by pfried on 7/5/14.
 */

@CompileStatic
public class ConstructorInstanceGenerator implements InstanceGenerator {

	Binding[] constructorArgs
	Class clazz
	Closure setup

	ConstructorInstanceGenerator(Class clazz, Binding[] constructorArgs = null, Closure setup = null) {
		this.clazz = clazz
		this.constructorArgs = constructorArgs
		this.setup = setup
	}

	ConstructorInstanceGenerator(Class clazz, Closure setup) {
		this(clazz, null, setup)
	}

	@Override
	def createNewInstance(){
		def instance
		if (constructorArgs && constructorArgs.length > 0) {
			def args = constructorArgs.collect{Binding b-> b.getInstance()}
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