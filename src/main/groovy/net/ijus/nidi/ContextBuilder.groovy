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
		return new BindingBuilder(clazz)
	}

	void validate() throws InvalidConfigurationException {
		ctxBindings.each{BindingBuilder builder->
			if (!builder.scope) {
				builder.setScope(defaultScope)
			}
			//TODO: build bindings
		}
	}

	Context getContextRef(){
		return this.ctx
	}

	Context build() throws InvalidConfigurationException {
		validate()

	}
}