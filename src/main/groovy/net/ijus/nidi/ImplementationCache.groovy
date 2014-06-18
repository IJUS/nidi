package net.ijus.nidi

/**
 * Created by pfried on 6/17/14.
 */
public interface ImplementationCache {

	def getCachedInstance(Class clazz)

	void cacheInstance(Object instance)

	def removeInstance(Class clazz)

	void clearCache()

}