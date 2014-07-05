package net.ijus.nidi

import groovy.transform.CompileStatic

/**
 * Created by pfried on 7/5/14.
 */

@CompileStatic
public class ContextBindingReference {

	Class clazz
	Context ctx

	ContextBindingReference(Class clazz, Context ctx) {
		this.clazz = clazz
		this.ctx = ctx
	}



}