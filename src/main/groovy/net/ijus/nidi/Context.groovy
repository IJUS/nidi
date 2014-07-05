package net.ijus.nidi;

import groovy.transform.CompileStatic

/**
 * Created by pfried on 6/16/14.
 */

@CompileStatic
public class Context {

	Map<Class, Binding> bindingsMap = [:]


	Binding getBindingForClass(Class clazz) {
		return bindingsMap.get(clazz)
	}

	def getInstance(Class clazz){
		return null //TODO: write method
	}
}