package net.ijus.nidi;

import groovy.transform.CompileStatic
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * Created by pfried on 6/17/14.
 */

@CompileStatic
public class BasicImplementationCache implements ImplementationCache {
	static final Logger log = LoggerFactory.getLogger(BasicImplementationCache)

	Map<Class, Object> cache = [:]

	@Override
	def getCachedInstance(Class clazz) {
		return cache.get(clazz)
	}

	@Override
	void cacheInstance(Object instance) {
		this.cache.put(instance.getClass(), instance)
	}

	@Override
	def removeInstance(Class clazz) {
		return cache.remove(clazz)
	}

	@Override
	void clearCache() {
		log.debug("Class instance cache is being cleared, removing ${cache.size()} cached instances")
		cache.clear()
	}
}