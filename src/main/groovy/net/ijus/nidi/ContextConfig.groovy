package net.ijus.nidi

import groovy.transform.CompileStatic;


/**
 * Created by pfried on 6/16/14.
 */
@CompileStatic
public interface ContextConfig {


	abstract void configure(Context ctx)

}