package net.ijus.nidi;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Used to indicate that a Constructor parameter does not need to be specified.
 * When resolving constructor parameters, the BindingBuilder will, by default, assume that every parameter is required
 * to be non-null, and it will do it's best to enforce that. If a parameter is annotated @Optional, then the binding builder
 * will allow null values, (or even missing Bindings) for that parameter. @Optional bindings can either be bound to null
 * explicitly, or else just not bound at all.
 *
 * This annotation can also take a single String parameter to indicate an optional binding to a String key. These get
 * resolved in exactly the same way as any other binding.
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface Optional {

    public abstract String value() default "";
}
