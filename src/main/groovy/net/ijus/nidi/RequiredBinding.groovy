package net.ijus.nidi

import java.lang.annotation.*

/**
 * Created by pfried on 7/2/14.
 */

@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface RequiredBinding {
	String value()

}