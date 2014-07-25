package net.ijus.nidi.bindings

import groovy.transform.CompileStatic
import net.ijus.nidi.Context
import net.ijus.nidi.InvalidConfigurationException
import net.ijus.nidi.instantiation.InstanceGenerator
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * Created by pfried on 7/5/14.
 */

@CompileStatic
public class ContextBindingReference implements Binding {
	static final Logger log = LoggerFactory.getLogger(ContextBindingReference)

	Class referencedClass
	Context ctx
	Class provides
	Binding resolvedBinding

	ContextBindingReference(Class refClass, Context ctx, Class provides = null) {
		this.referencedClass = refClass
		this.ctx = ctx
		this.provides = provides?: refClass

	}

	@Override
	def getInstance() {
		return getResolvedBinding().getInstance()
	}

	@Override
	Class getImplClass() {
		return getResolvedBinding().getImplClass()
	}

	@Override
	Class getBoundClass() {
		provides
	}

	@Override
	Scope getScope() {
		return getResolvedBinding().getScope()
	}

	@Override
	InstanceGenerator getInstanceGenerator() {
		return getResolvedBinding().getInstanceGenerator()
	}

	@Override
	void validate() {
		Binding b = ctx.getBinding(referencedClass)
		if (!b) {
			throw new InvalidConfigurationException("The Context does not contain a binding for ${referencedClass.getCanonicalName()} but it definitely should")
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
		Binding b = ctx.getBinding(this.referencedClass)
		if (!b) {
			throw new InvalidConfigurationException("The Context does not contain a Binding for class: ${referencedClass.name}. Perhaps the referenced Binding trying to be created to early")
		}
		Scope s = b.getScope()
		log.debug("Resolving binding for class: ${this.referencedClass.name} with scope: ${s}")

		if (s == Scope.ONE_PER_BINDING) {
			b = new CacheingBinding(b.getInstanceGenerator(), this.referencedClass, b.getImplClass(), Scope.ONE_PER_BINDING)
		}
		return b
	}
}