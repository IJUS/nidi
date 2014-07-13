package net.ijus.nidi;

import groovy.transform.CompileStatic

/**
 * Created by pfried on 6/16/14.
 */

@CompileStatic
public class Configuration {
	public static final String CONFIG_PROPERTY_NAME = "nidi.context.class"

	static void setMainContextFromClassName(String configClassName) throws InvalidConfigurationException {
		ContextHolder.setContext(configureNew(configClassName))
	}

	static void setMainContextFromClass(Class<? extends ContextConfig> clazz) throws InvalidConfigurationException {
		ContextHolder.setContext(configureNew(clazz))
	}

	static void setMainContextFromSystemProperty() throws InvalidConfigurationException {
		Context ctx = configureNewFromSystemProperty()
		ContextHolder.setContext(ctx)
	}

	static Context configureNewFromSystemProperty() throws InvalidConfigurationException {
		String ctxClass = System.getProperty(CONFIG_PROPERTY_NAME)
		if (!ctxClass) {
			throw new InvalidConfigurationException("The nidi Context could not be created because no System property was set. Set the System property \"nidi.context.class\" to the fully qualified class name of a class that implements ContextConfig")
		}

		return configureNew(ctxClass)
	}

	static Context configureNew(String fqcn) throws InvalidConfigurationException {
		ContextBuilder builder = new ContextBuilder()
		configure(builder, fqcn)
		return build(builder)
	}

	static Context configureNew(Class<? extends ContextConfig> clazz) throws InvalidConfigurationException {
		ContextBuilder builder = new ContextBuilder()

		configure(builder, clazz)

		return build(builder)
	}

	static Context configureNew(Closure closure) throws InvalidConfigurationException {
		ContextBuilder builder = new ContextBuilder()
		configure(builder, closure)
		return build(builder)
	}

	static Context configureNew(ContextConfig config) throws InvalidConfigurationException {
		ContextBuilder builder = new ContextBuilder()
		configure(builder, config)
		return build(builder)
	}

	/**
	 * validates the builder, then builds and returns the Context
	 * @param builder
	 * @return a validated Context
	 */
	static Context build(ContextBuilder builder) throws InvalidConfigurationException {

		//TODO: validate
		return builder.build()
	}


	//////////////////////////////////////////////////////////////////////////
	////////   Void configure methods
	/////////////////////////////////////////////////////////////////////////


	static void configure(ContextBuilder builder, String fqcn) throws InvalidConfigurationException {
		try {
			Class configClass = Class.forName(fqcn)
			configure(builder, configClass)
		} catch (ClassNotFoundException e) {
			throw new InvalidConfigurationException("Attempted to configure contextBuilder: ${builder} using the class: ${fqcn}, but the class was not found on the classpath")
		}
	}

	static void configure(ContextBuilder builder, Class configClass) throws InvalidConfigurationException {
		def config
		try {
			config = configClass.newInstance()
		} catch (Throwable t) {
			throw new InvalidConfigurationException("Error instantiating the configuration class: ${configClass.getCanonicalName()}", t)
		}

		if (!(config instanceof ContextConfig)) {
			throw new InvalidConfigurationException("The Class: ${configClass.name} does not implement ContextConfig")
		}
		configure(builder, config as ContextConfig)
	}

	static void configure(ContextBuilder builder, Closure closure) throws InvalidConfigurationException {
		closure.setDelegate(builder)
		closure.call(builder)
	}

	static void configure(ContextBuilder builder, ContextConfig config) throws InvalidConfigurationException {
		config.configure(builder)

	}


}