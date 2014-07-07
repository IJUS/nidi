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

	boolean containsBinding(Class clazz) {
		return bindingsMap.containsKey(clazz)
	}

	def getInstance(Class clazz) {
		if (!containsBinding(clazz)) {
			throw new InvalidConfigurationException("The Class: ${clazz.getCanonicalName()} was requested from a Context, but no Binding exists for it")
		}
		return getBindingForClass(clazz).getInstance()
	}
}