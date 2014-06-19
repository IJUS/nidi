package net.ijus.nidi;

import groovy.transform.CompileStatic

/**
 * Created by pfried on 6/16/14.
 */

@CompileStatic
public class Context {
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

	void inheritFrom(Class contextClass) {
		if (!Context.isAssignableFrom(contextClass)) {
			throw new InvalidConfigurationException("Attempted to inherit from a class that is not a Context: ${contextClass.getCanonicalName()}")
		}
		try {
			def parentCtx = contextClass.newInstance()
			inheritFrom(parentCtx as Context)
		} catch (Exception e) {
			throw new InvalidConfigurationException("Could not inherit from ${contextClass.getCanonicalName()}", e)
		}
	}

	void inheritFrom(Context ctx) {
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
	}

	void inheritFrom(String fqcn) {
		try {
			Class c = Class.forName(fqcn)

			inheritFrom(c)
		} catch (ClassNotFoundException e) {
			throw new InvalidConfigurationException("Tried to inherit from: ${fqcn}, but class could not be found", e)
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
		this.bindings.remove(clazz.getCanonicalName())
	}

	void addClassContext(ClassContext ctx) {
		ctx.setParentContext(this)
		classContexts.put(ctx.getBoundClass(), ctx)
	}

}