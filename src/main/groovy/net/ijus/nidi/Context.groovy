package net.ijus.nidi;

import groovy.transform.CompileStatic

/**
 * Created by pfried on 6/16/14.
 */

@CompileStatic
public class Context {
	private boolean defaultScopeSpecified = false

	String name = "DefaultContext"

	ImplementationCache cache = new BasicImplementationCache()

	Map<Class, Binding> bindings = [:]

	Map<Class, ClassContext> classContexts = [:]
	Set<Class> ctxGlobalClasses = [] as Set
	Scope defaultScope = Scope.ALWAYS_CREATE_NEW

	Scope getScopeForClass(Class clazz) {
		Scope s
		if (ContextHolder.isSingleton(clazz)) {
			s = Scope.SINGLETON
		} else if (ctxGlobalClasses.contains(clazz)) {
			s = Scope.CONTEXT_GLOBAL
		} else {
			Binding bindingForClass = bindings.values().find{Binding b-> b.implementationClass == clazz}
			s = bindingForClass?.getScope()
		}
		return s
	}

	Context inheritFrom(Class contextConfigClass) throws InvalidConfigurationException {
		Context parent = Configuration.configureNew(contextConfigClass)
		doInherit(parent)
		return this
	}

	Context inheritFrom(String fqcn) throws InvalidConfigurationException {
		Context parent = Configuration.configureNew(fqcn)
		doInherit(parent)
		return this
	}

	private void doInherit(Context ctx) {
		def parentContext = ctx
		Collection<Class> keysToInherit = parentContext.getBindings().keySet().findAll{Class key-> !bindings.containsKey(key)}
		keysToInherit.each{Class toInherit->
			Binding b = parentContext.removeBinding(toInherit)
			addBinding(b)
		}

		Collection<Class> classCtxKeys = parentContext.getClassContexts().keySet().findAll{Class c -> !classContexts.containsKey(c)}
		classCtxKeys.each{Class c->
			ClassContext classCtx = parentContext.getClassContexts().remove(c)
			addClassContext(classCtx)
		}

		if (!defaultScopeSpecified) {
			this.defaultScope = ctx.getDefaultScope()
		}
	}

	BindingFactory bind(Class clazz) {
		BindingFactory f = new BindingFactory(this)
		return f.bind(clazz)
	}

	Binding getBinding(Class clazz) {
		if (classContexts.containsKey(clazz)) {
			return classContexts.get(clazz).getBinding(clazz)
		} else {
			return bindings.get(clazz)
		}
	}

	Object getInstance(Class clazz) throws InvalidConfigurationException {
		Binding binding = getBinding(clazz)
		if (binding) {
			return binding.getInstance()
		}  else {
			throw new InvalidConfigurationException("Attempted to get an instance of: ${clazz.getCanonicalName()}, but no Bindings exist for this class")
		}
	}

	BasicBinding register(Class concreteClass, Scope bindingScope = null) {
		Scope s = bindingScope?: defaultScope
		return bind(concreteClass).withScope(s).toItself()
	}

	void addBinding(Binding b) {
		b.setParentContext(this)
		this.bindings.put(b.getBoundClass(), b)
	}

	Binding removeBinding(Class clazz) {
		assert bindings.containsKey(clazz)
		Binding b = bindings.get(clazz)
		this.bindings.remove(clazz.getCanonicalName())
		b.parentContext = null
		return b
	}

	void addClassContext(ClassContext ctx) {
		ctx.setParentContext(this)
		classContexts.put(ctx.getBoundClass(), ctx)
	}

	void setDefaultScope(Scope defaultScope) {
		this.defaultScope = defaultScope
		this.defaultScopeSpecified = true
	}
}