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

	static long time(Closure work, long startTime = System.currentTimeMillis()){
		work.call()
		return System.currentTimeMillis() - startTime
	}

	static long timeAndLog(String taskName, Closure work){
		long startTime = System.currentTimeMillis()
		log.debug("Started ${taskName} at: ${startTime}")
		long t = time(work, startTime)
		log.debug("Finished ${taskName} in ${t} milliseconds")
		return t
	}
}