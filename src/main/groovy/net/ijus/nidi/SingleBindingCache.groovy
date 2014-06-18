package net.ijus.nidi;

import groovy.transform.CompileStatic

/**
 * Created by pfried on 6/17/14.
 */


@CompileStatic
public class SingleBindingCache implements ImplementationCache {

	def cachedInstance

	@Override
	def getCachedInstance(Class clazz) {
		return this.cachedInstance
	}

	@Override
	void cacheInstance(Object instance) {
		this.cachedInstance = instance
	}

	@Override
	def removeInstance(Class clazz) {

		def toReturn = cachedInstance
		cachedInstance = null
		return toReturn
	}

	@Override
	void clearCache() {
		this.cachedInstance = null
	}
}