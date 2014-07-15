package net.ijus.nidi.bindings

import groovy.transform.CompileStatic
import net.ijus.nidi.Context
import net.ijus.nidi.InvalidConfigurationException
import net.ijus.nidi.instantiation.ConstructorInstanceGenerator
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * Created by pfried on 7/5/14.
 */

@CompileStatic
public class ContextBindingReference implements Binding {
	static final Logger log = LoggerFactory.getLogger(ContextBindingReference)

	Class boundClass
	Context ctx

	Binding resolvedBinding

	ContextBindingReference(Class clazz, Context ctx) {
		this.boundClass = clazz
		this.ctx = ctx
	}

	@Override
	getInstance() {
		return getResolvedBinding().getInstance()
	}

	@Override
	Class getImplClass() {
		return getResolvedBinding().getImplClass()
	}

	@Override
	Class getBoundClass() {
		boundClass
	}

	@Override
	Scope getScope() {
		return getResolvedBinding().getScope()
	}

	@Override
	ConstructorInstanceGenerator getInstanceGenerator() {
		return null
	}

	@Override
	void validate() {
		Binding b = ctx.getBindingForClass(boundClass)
		if (!b) {
			throw new InvalidConfigurationException("The Context does not contain a binding for ${boundClass.getCanonicalName()} but it definitely should")
		}
		b.validate()
	}

	Binding getResolvedBinding() {
		if (!resolvedBinding) {
			this.resolvedBinding = createResolvedBinding()
		}
		return resolvedBinding
	}

	Binding createResolvedBinding(){
		Binding b = ctx.getBindingForClass(this.boundClass)
		if (!b) {
			throw new InvalidConfigurationException("The Context does not contain a Binding for class: ${boundClass.name}. Perhaps the referenced Binding trying to be created to early")
		}
		Scope s = b.getScope()
		log.debug("Resolving binding for class: ${this.boundClass.name} with scope: ${s}")

		if (s == Scope.ONE_PER_BINDING) {
			b = new CacheingBinding(b.getInstanceGenerator(), this.boundClass, Scope.ONE_PER_BINDING)
		}
		return b
	}
}