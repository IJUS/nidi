package net.ijus.nidi

import org.slf4j.Logger
import org.slf4j.LoggerFactory;

/**
 * Created by pfried on 6/16/14.
 */
public class ContextHolder {
	private static final Logger log = LoggerFactory.getLogger(ContextHolder)

	public static ImplementationCache singletonCache = new BasicImplementationCache()
	public static Set<Class> singletons = [] as Set

	/*
	The declaration of the ctx must be below all other static fields, or things will seriously break.
	Yes, this is a shitty solution. Yes, it will get fixed.
	If you are reading this, please just fix it now.
	 */
	//TODO: initialize context in the getter instead of in static field initialization
	private static Context ctx = initContext()

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

	private static Context initContext() {
		String configClass = System.getProperty(Configuration.CONFIG_PROPERTY_NAME)
		Context toReturn = null
		if (configClass) {
			log.info("Initializing nidi context from System property: ${configClass}")
			try {
				toReturn = Configuration.configureNewFromSystemProperty()
			} catch (Exception e) {
				String msg = "Error initializing NiDI Context using class: ${configClass}. The Context failed to initialize and the ContextHolder context will be null"
				log.error(msg, e)
				System.err.println(msg)
			}
		} else {
			log.debug("initializing empty nidi context")
			toReturn = new Context()
		}

		return toReturn
	}

}
