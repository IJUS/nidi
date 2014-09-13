package net.ijus.nidi;

import net.ijus.nidi.builder.ContextBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by pfried on 6/16/14.
 */
public class Configuration {
    private static final Logger log = LoggerFactory.getLogger(Configuration.class);

    public static final String CONFIG_PROPERTY_NAME = "nidi.context.class";

    public static void setMainContextFromClassName(String configClassName) throws InvalidConfigurationException {
        ContextHolder.setContext(configureNew(configClassName));
    }

    public static void setMainContextFromClass(Class<? extends ContextConfig> clazz) throws InvalidConfigurationException {
        ContextHolder.setContext(configureNew(clazz));
    }

    public static void setMainContextFromSystemProperty() throws InvalidConfigurationException {
        Context ctx = configureNewFromSystemProperty();
        ContextHolder.setContext(ctx);
    }

    public static Context configureNewFromSystemProperty() throws InvalidConfigurationException {
        String ctxClass = System.getProperty(CONFIG_PROPERTY_NAME);
        if (ctxClass == null || ctxClass.length() == 0) {
            throw new InvalidConfigurationException("The nidi Context could not be created because no System property was set. Set the System property \"nidi.context.class\" to the fully qualified class name of a class that implements ContextConfig");
        }


        return configureNew(ctxClass);
    }

    public static Context configureNew(String fqcn) throws InvalidConfigurationException {
        ContextBuilder builder = new ContextBuilder();
        configure(builder, fqcn);
        return build(builder);
    }

    public static Context configureNew(Class<? extends ContextConfig> clazz) throws InvalidConfigurationException {
        ContextBuilder builder = new ContextBuilder();

        configure(builder, clazz);

        return build(builder);
    }

    public static Context configureNew(ContextConfig config) throws InvalidConfigurationException {
        ContextBuilder builder = new ContextBuilder();
        configure(builder, config);
        return build(builder);
    }

    /**
     * validates the builder, then builds and returns the Context
     *
     * @param builder
     * @return a validated Context
     */
    public static Context build(ContextBuilder builder) throws InvalidConfigurationException {

        //TODO: validate
        return builder.build();
    }

    public static void configure(final ContextBuilder builder, final String fqcn) throws InvalidConfigurationException {
        try {
            Class configClass = Class.forName(fqcn);
            configure(builder, configClass);
        } catch (ClassNotFoundException e) {
            throw new InvalidConfigurationException("Attempted to configure contextBuilder: " + builder.toString() + " using the class: " + fqcn + ", but the class was not found on the classpath");
        }

    }

    public static void configure(ContextBuilder builder, final Class configClass) throws InvalidConfigurationException {
        Object config;
        try {
            config = configClass.newInstance();
        } catch (Throwable t) {
            throw new InvalidConfigurationException("Error instantiating the configuration class: " + configClass.getCanonicalName(), t);
        }


        if (!(config instanceof ContextConfig)) {
            throw new InvalidConfigurationException("The Class: " + configClass.getName() + " does not implement ContextConfig");
        }

        configure(builder, (ContextConfig) config);
    }

    public static void configure(ContextBuilder builder, ContextConfig config) throws InvalidConfigurationException {
        config.configure(builder);

    }

}
