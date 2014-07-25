package net.ijus.nidi;

import groovy.transform.CompileStatic
import net.ijus.nidi.bindings.Binding

/**
 * Created by pfried on 6/16/14.
 */

@CompileStatic
public class Context {

	Map<Object, Binding> bindingsMap = [:]


	Binding getBinding(Object key) {
		return bindingsMap.get(key)
	}

	boolean containsBinding(Object key) {
		return bindingsMap.containsKey(key)
	}

	def getInstance(Class clazz) {
		if (!containsBinding(clazz)) {
			throw new InvalidConfigurationException("The Class: ${clazz.getCanonicalName()} was requested from a Context, but no Binding exists for it")
		}
		return getBinding(clazz).getInstance()
	}

	def getInstance(String key) {
		if (!containsBinding(key)) {
			throw new InvalidConfigurationException("The Property: ${key} was requested from a Context but no Binding exists for it")
		}
		return getBinding(key).getInstance()
	}
}