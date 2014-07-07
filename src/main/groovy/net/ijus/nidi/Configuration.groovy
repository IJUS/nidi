package net.ijus.nidi;

import groovy.transform.CompileStatic

/**
 * Created by pfried on 6/16/14.
 */

@CompileStatic
public class Configuration {
	public static final String CONFIG_PROPERTY_NAME = "nidi.context.class"

	static void setMainContextFromClassName(String configClassName) {
		ContextHolder.setContext(configureNew(configClassName))
	}

	static void setMainContextFromClass(Class<? extends ContextConfig> clazz) {
		ContextHolder.setContext(configureNew(clazz))
	}

	static void setMainContextFromSystemProperty(){
		Context ctx = configureNewFromSystemProperty()
		ContextHolder.setContext(ctx)
	}

	static Context configureNewFromSystemProperty(){
		String ctxClass = System.getProperty(CONFIG_PROPERTY_NAME)
		if (!ctxClass) {
			throw new InvalidConfigurationException("The nidi Context could not be created because no System property was set. Set the System property \"nidi.context.class\" to the fully qualified class name of a class that implements ContextConfig")
		}

		return configureNew(ctxClass)
	}

	static Context configureNew(String fqcn) {
		Context ctx = new Context()
		configure(ctx, fqcn)
		return ctx
	}

	static Context configureNew(Class<? extends ContextConfig> clazz) {
		Context ctx = new Context()
		configure(ctx, clazz)
		return ctx
	}

	static Context configureNew(Closure closure) {
		Context ctx = new Context()
		configure(ctx, closure)
		return ctx
	}

	static Context configureNew(ContextConfig config) {
		Context ctx = new Context()
		configure(ctx, config)
		return ctx
	}


	static void configure(Context ctx, String fqcn) {
		try {
			Class configClass = Class.forName(fqcn)
			configure(ctx, configClass)
		} catch (ClassNotFoundException e) {
			throw new InvalidConfigurationException("Attempted to configure context: ${ctx} using the class: ${fqcn}, but the class was not found on the classpath")
		}
	}

	static void configure(Context ctx, Class configClass) {
		if (!ContextConfig.isAssignableFrom(configClass)) {
			throw new InvalidConfigurationException("Attempted to configure the Context: ${ctx} using the class: ${configClass.getCanonicalName()}, which is NOT a valid implementation of ContextConfig")
		}
		def config
		try {
			config = configClass.newInstance()
		} catch (Throwable t) {
			throw new InvalidConfigurationException("Error instantiating the configuration class: ${configClass.getCanonicalName()}", t)
		}
		configure(ctx, config as ContextConfig)
	}

	static void configure(Context ctx, Closure closure) {
		closure.setDelegate(ctx)
		closure.call(ctx)
	}

	static void configure(Context ctx, ContextConfig config) {
		//TODO: write me
		assert "method was written" == "false"
	}


}