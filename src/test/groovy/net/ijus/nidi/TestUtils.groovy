package net.ijus.nidi

import org.slf4j.Logger
import org.slf4j.LoggerFactory;



/**
 * Created by pfried on 6/18/14.
 */

public class TestUtils {
	static final Logger log = LoggerFactory.getLogger(TestUtils)

	static void clearContextHolder(){
		log.debug("Resetting the ContextHolder")
		ContextHolder.singletons.clear()
		ContextHolder.singletonCache.clearCache()
		ContextHolder.setContext(null)
	}
}