package net.ijus.nidi

import org.slf4j.Logger
import org.slf4j.LoggerFactory;



/**
 * Created by pfried on 6/18/14.
 */

public class ContextTestUtils {
	static final Logger log = LoggerFactory.getLogger(ContextTestUtils)

	static void clearContextHolder(){
		log.info("Resetting the ContextHolder")
		ContextHolder.setContext(null)
	}
}