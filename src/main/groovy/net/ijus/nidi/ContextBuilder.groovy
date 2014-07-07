package net.ijus.nidi

import groovy.transform.CompileStatic
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * Created by pfried on 7/2/14.
 */

@CompileStatic
public class ContextBuilder {
	static final Logger log = LoggerFactory.getLogger(ContextBuilder)

	Scope defaultScope = Scope.ALWAYS_CREATE_NEW
	Set<BindingBuilder> ctxBindings = [] as Set

	Context ctx = new Context()

	BindingBuilder bind(Class clazz){
		BindingBuilder bb = new BindingBuilder(clazz)
		ctxBindings.add(bb)
		bb
	}


	boolean containsBindingFor(Class clazz) {
		return ctxBindings.any{BindingBuilder bb-> bb.from == clazz}
	}

	Context getContextRef(){
		return this.ctx
	}

	Context build() throws InvalidConfigurationException {
		log.debug("Building Context with ${ctxBindings.size()} Bindings in the root context")
		ctxBindings.each{BindingBuilder builder->
			if (!builder.scope) {
				builder.setScope(defaultScope)
			}
			builder.validateClassAssignment()
			Binding binding = builder.build(this)
			log.debug("Adding Binding: ${binding} to the Context")
			ctx.bindingsMap.put(binding.getBoundClass(), binding)
		}

		return ctx

	}
}