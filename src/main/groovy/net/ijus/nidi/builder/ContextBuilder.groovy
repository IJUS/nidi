package net.ijus.nidi.builder

import groovy.transform.CompileStatic
import net.ijus.nidi.Configuration
import net.ijus.nidi.Context
import net.ijus.nidi.ContextConfig
import net.ijus.nidi.InvalidConfigurationException
import net.ijus.nidi.bindings.Scope
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * Created by pfried on 7/2/14.
 */

@CompileStatic
public class ContextBuilder {
	static final Logger log = LoggerFactory.getLogger(ContextBuilder)

	boolean defaultScopeChanged = false

	Scope defaultScope = Scope.ALWAYS_CREATE_NEW
	Map<Class, BindingBuilder> ctxBindings = [:]

	Context ctx = new Context()

	BindingBuilder bind(Class clazz){
		BindingBuilder bb = new BindingBuilder(clazz, this)
		ctxBindings.put(clazz, bb)
		bb
	}

	void setDefaultScope(Scope newScope) {
		this.defaultScope = newScope
		this.defaultScopeChanged = true
	}

	void inheritFrom(String fqcn){
		Class configClass
		try {
			configClass = Class.forName(fqcn)
		} catch (ClassNotFoundException e) {
			throw new InvalidConfigurationException("Tried to inherit from: ${fqcn}, but the class could not be found", e)
		}
		inheritFrom(configClass)
	}

	void inheritFrom(Class configClass) {
		if (!ContextConfig.isAssignableFrom(configClass)) {
			throw new InvalidConfigurationException("Tried to inherit from: ${configClass.name}, but that class does not immplement ContextConfig")
		}
		ContextConfig config
		try {
			config = configClass.newInstance() as ContextConfig
		} catch (InstantiationException | IllegalAccessException e) {
			throw new InvalidConfigurationException("Tried to inherit, but failed to create a new instance of class: ${configClass.name}", e)
		}
		inheritFrom(config)
	}

	void inheritFrom(ContextConfig config) {
		ContextBuilder parentBuilder = new ContextBuilder()
		config.configure(parentBuilder)
		doInheritance(parentBuilder)
	}

	private void doInheritance(ContextBuilder parentBuilder) {
		Map parentBindings = parentBuilder.ctxBindings
		log.debug("Inheriting from ContextBuilder with class: ${parentBuilder.getClass().getName()} containing ${parentBindings.size()} bindings")
		parentBindings.putAll(ctxBindings) //Add this builders bindings to the parent's, overriding the parents' if there are any conflicts
		this.ctxBindings = parentBindings

		//also inherit the default scope if needed
		if (!defaultScopeChanged && this.defaultScope != parentBuilder.defaultScope) {
			log.debug("Inheriting the default scope specified in the parent context builder: ${parentBuilder.defaultScope}")
			setDefaultScope(parentBuilder.defaultScope)
		}
	}

	boolean containsBindingFor(Class clazz) {
		return ctxBindings.containsKey(clazz)
	}

	Context getContextRef(){
		return this.ctx
	}

	Context build() throws InvalidConfigurationException {
		log.debug("Building Context with ${ctxBindings.size()} Bindings in the root context")
		ctxBindings.each{Class key, BindingBuilder builder->
			builder.inheritScope(defaultScope)
			builder.validateClassAssignment()
			net.ijus.nidi.bindings.Binding binding = builder.build()
			log.debug("Adding Binding: ${binding} to the Context")
			ctx.bindingsMap.put(binding.getBoundClass(), binding)
		}

		return ctx

	}
}