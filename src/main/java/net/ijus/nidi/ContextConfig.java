package net.ijus.nidi;

import net.ijus.nidi.builder.ContextBuilder;

/**
 * Interface for configuring a Context using a ContextBuilder. Allows for easy automation of context setup.
 * Any class that implements this interface can simply be passed to one of the methods in the <code>Configuration</code>
 * class to create a Context.
 */
public interface ContextConfig {
    public abstract void configure(ContextBuilder builder);
}
