package net.ijus.nidi;

import groovy.transform.CompileStatic;
import net.ijus.nidi.builder.ContextBuilder;

/**
 * Created by pfried on 6/16/14.
 */
@CompileStatic
public interface ContextConfig {
    public abstract void configure(ContextBuilder builder);
}
