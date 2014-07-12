package net.ijus.nidi

import groovy.transform.CompileStatic

/**
 * Created by pfried on 7/11/14.
 */

@CompileStatic
public class CacheingBinding implements Binding {

	def cachedInstance
	InstanceGenerator instanceGenerator
	Class boundClass
	Scope scope

	CacheingBinding(InstanceGenerator instanceGenerator, Class boundClass, Scope scope) {
		this.instanceGenerator = instanceGenerator
		this.boundClass = boundClass
		validateScope(scope)
		this.scope = scope
	}

	@Override
	def getInstance() {
		if (!cachedInstance) {
			this.cachedInstance = instanceGenerator.createNewInstance()
		}
		return cachedInstance
	}

	@Override
	Class getImplClass() {
		return this.instanceGenerator.clazz
	}

	@Override
	Class getBoundClass() {
		return this.boundClass
	}

	@Override
	Scope getScope() {
		this.scope
	}

	@Override
	void validate() {

	}

	@Override
	InstanceGenerator getInstanceGenerator(){
		this.instanceGenerator
	}

	void validateScope(Scope scope) throws InvalidConfigurationException {
		if (!scope) {
			throw new InvalidConfigurationException("The Scope for this binding cannot be null: CacheingScopedBinding for Class: ${boundClass.getCanonicalName()} to: ${instanceGenerator.getClazz().getCanonicalName()}")

		} else if (scope == Scope.ALWAYS_CREATE_NEW) {
			throw new InvalidConfigurationException("The scope: ${scope} is not compatible with t")
		}
	}
}