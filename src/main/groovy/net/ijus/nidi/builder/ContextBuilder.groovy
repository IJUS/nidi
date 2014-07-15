package net.ijus.nidi.builder

import groovy.transform.CompileStatic
import net.ijus.nidi.Context
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

	Scope defaultScope = Scope.ALWAYS_CREATE_NEW
	Map<Class, BindingBuilder> ctxBindings = [:]

	Context ctx = new Context()

	BindingBuilder bind(Class clazz){
		BindingBuilder bb = new BindingBuilder(clazz, this)
		ctxBindings.put(clazz, bb)
		bb
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