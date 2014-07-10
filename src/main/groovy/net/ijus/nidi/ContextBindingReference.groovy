package net.ijus.nidi

import groovy.transform.CompileStatic

/**
 * Created by pfried on 7/5/14.
 */

@CompileStatic
public class ContextBindingReference implements Binding {

	Class clazz
	Context ctx

	ContextBindingReference(Class clazz, Context ctx) {
		this.clazz = clazz
		this.ctx = ctx
	}

	@Override
	getInstance() {
		return ctx.getBindingForClass(clazz).getInstance()
	}

	@Override
	Class getImplClass() {
		return ctx.getBindingForClass(clazz).getImplClass()
	}

	@Override
	Class getBoundClass() {
		clazz
	}

	@Override
	Scope getScope() {
		return ctx.getBindingForClass(clazz).getScope()
	}

	@Override
	void validate() {
		Binding b = ctx.getBindingForClass(clazz)
		if (!b) {
			throw new InvalidConfigurationException("The Context does not contain a binding for ${clazz.getCanonicalName()} but it definitely should")
		}
		b.validate()
	}
}