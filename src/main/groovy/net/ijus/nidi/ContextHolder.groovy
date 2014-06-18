package net.ijus.nidi

import org.slf4j.Logger
import org.slf4j.LoggerFactory;

/**
 * Created by pfried on 6/16/14.
 */
public class ContextHolder {
	private static final Logger log = LoggerFactory.getLogger(ContextHolder)

	private static Context ctx = new Context()

	public static ImplementationCache singletonCache = new BasicImplementationCache()
	public static Set<Class> singletons = [] as Set

	static boolean isSingleton(Class clazz){
		return singletons.contains(clazz)
	}

	static Context getContext(){
		return ctx
	}

	static void setContext(Context context) {
		if (ctx != null && ctx != context) {
			log.warn("The Context: ${ctx.getName()} is being replaced by the context: ${context?.getName()}")
		}
		ctx = context

	}

}
