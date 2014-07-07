package net.ijus.nidi;

import groovy.transform.CompileStatic

import java.lang.reflect.Constructor

/**
 * Created by pfried on 7/5/14.
 */

@CompileStatic
public class InstanceGenerator {

	Binding[] constructorArgs
	Class clazz
	Closure setup

	InstanceGenerator(Class clazz, Binding[] constructorArgs = null, Closure setup = null) {
		this.clazz = clazz
		this.constructorArgs = constructorArgs
		this.setup = setup
	}

	InstanceGenerator(Class clazz, Closure setup) {
		this(clazz, null, setup)
	}

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