package net.ijus.nidi

import groovy.transform.CompileStatic;

/**
 * Created by pfried on 6/16/14.
 */
@CompileStatic
public class BindingFactory {

	Context context

	Class<?> from;
	Class<?> to;

	Scope bindingScope



	BindingFactory(Context context) {
		this.context = context
	}

	BindingFactory bind(Class<?> clazz) {
		this.from = clazz
		return this
	}

	BindingFactory withScope(Scope scope) {
		this.bindingScope = scope
		return this
	}


	BasicBinding to(Class<?> impl) throws InvalidConfigurationException {
		BasicBinding b = new BasicBinding(from, impl, context)
		setScopeState(b)
		context.addBinding(b)
		return b
	}

	BasicBinding toItself() throws InvalidConfigurationException {
		if (!from) {
			throw new InvalidConfigurationException("Cannot bind null to itself! Must first specify a class to bind")
		}
		if (from.isInterface()) {
			throw new InvalidConfigurationException("Cannot Bind an interface to itself: ${name(from)}")
		}
		BasicBinding b = new BasicBinding(from, from, context)
		setScopeState(b)
		context.addBinding(b)

		return b
	}



	void setScopeState(BasicBinding b) {
		Scope s = bindingScope?: context.getDefaultScope()
		if (s == Scope.SINGLETON) {
			ContextHolder.singletons.add(b.getImplementationClass())
		} else if (s == Scope.CONTEXT_GLOBAL) {
			context.ctxGlobalClasses.add(b.getImplementationClass())
		}
		b.setScope(s)
	}

	String name(Class clazz) {
		String name
		if (clazz.isAnonymousClass()) {
			name = "Anonymous implementation of: ${clazz.getSuperclass().getCanonicalName()}"
		} else {
			name = clazz.getCanonicalName()
		}
		name
	}

}
