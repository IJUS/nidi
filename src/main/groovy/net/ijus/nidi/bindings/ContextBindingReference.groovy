package net.ijus.nidi.bindings

import groovy.transform.CompileStatic
import net.ijus.nidi.Context
import net.ijus.nidi.InvalidConfigurationException
import net.ijus.nidi.instantiation.InstanceGenerator
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import net.ijus.nidi.bindings.Binding

/**
 * Created by pfried on 7/5/14.
 */

@CompileStatic
public class ContextBindingReference<T> implements Binding<T> {
	static final Logger log = LoggerFactory.getLogger(ContextBindingReference)

    /**
     * referencedClass can be anything. It doesn't necessarily have to extend T
     */
	Class referencedClass

    /**
     * The context to get the binding from
     */
	Context ctx

    /**
     * The resolved Binding must provide a T as its impl class in order to be valid
     */
	Binding<T> resolvedBinding

    /**
     * This is the baseClass
     */
    Class<T> provides

	ContextBindingReference(Class refClass, Context ctx, Class<T> provides) {
		this.referencedClass = refClass
		this.ctx = ctx
		this.provides = provides

	}

	@Override
	T getInstance() {
		return getResolvedBinding().getInstance()
	}

	@Override
	Class<? extends T> getImplClass() {
		return getResolvedBinding().getImplClass()
	}

	@Override
	Class<T> getBoundClass() {
		return this.provides;
	}

	@Override
	Scope getScope() {
		return getResolvedBinding().getScope()
	}

	@Override
	InstanceGenerator<T> getInstanceGenerator() {
		return getResolvedBinding().getInstanceGenerator()
	}

	@Override
	void validate() {
		Binding b = getResolvedBinding();
		b.validate()
	}

	Binding<T> getResolvedBinding() {
		if (!resolvedBinding) {
			this.resolvedBinding = createResolvedBinding()
		}
		return resolvedBinding
	}

	Binding<T> createResolvedBinding(){
		Binding<T> b = ctx.getBinding(this.referencedClass)
		if (!b) {
			throw new InvalidConfigurationException("The Context does not contain a Binding for class: ${referencedClass.name}. Perhaps the referenced Binding trying to be created to early")
		}
		Scope s = b.getScope()
		log.debug("Resolving binding for class: ${this.referencedClass.name} with scope: ${s}")

        /*
        Depending on the Scope, we may need to wrap the resolved binding in a CachingBinding
         */
		if (s == Scope.ONE_PER_BINDING) {
			b = new CacheingBinding(b.getInstanceGenerator(), this.referencedClass, b.getImplClass(), Scope.ONE_PER_BINDING)
		}
		return b
	}
}