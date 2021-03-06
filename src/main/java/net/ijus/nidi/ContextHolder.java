package net.ijus.nidi;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by pfried on 6/16/14.
 */
public class ContextHolder {
    public static Context getContext() {
        if (ctx == null) {
            initContext();
        }

        return ctx;
    }

    public static void setContext(Context context) {
        if (ctx != null) {
            log.warn("The Context in the ContextHolder is being replaced at runtime.");
        }

        ctx = context;

    }

    private static Context initContext() {
        final String configClass = System.getProperty(Configuration.CONFIG_PROPERTY_NAME);
        Context toReturn = null;
        if (configClass != null && configClass.length() > 0) {
            log.info("Initializing nidi context from System property: " + configClass);
            try {
                toReturn = Configuration.configureNewFromSystemProperty();
            } catch (Exception e) {
                String msg = "Error initializing NiDI Context using class: " + configClass + ". The Context failed to initialize and the ContextHolder context will be null";
                log.error(msg, e);
                System.err.println(msg);
            }

        } else {
            log.debug("initializing empty nidi context");
            toReturn = new Context();
        }


        return toReturn;
    }

    public static Context getCtx() {
        return ctx;
    }

    public static void setCtx(Context ctx) {
        ContextHolder.ctx = ctx;
    }

    private static final Logger log = LoggerFactory.getLogger(ContextHolder.class);
    private static Context ctx;
}
