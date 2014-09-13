package net.ijus.nidi.bindings

import groovy.transform.CompileStatic
import net.ijus.nidi.InvalidConfigurationException
import net.ijus.nidi.instantiation.ConstructorInstanceGenerator
import net.ijus.nidi.instantiation.InstanceGenerator
import net.ijus.nidi.bindings.Binding

/**
 * Created by pfried on 7/11/14.
 */

@CompileStatic
public class CacheingBinding<T> implements Binding<T> {

	T cachedInstance
	InstanceGenerator<T> instanceGenerator
	Class<T> boundClass
	Class<? extends T> implClass
	Scope scope

	CacheingBinding(InstanceGenerator<T> instanceGenerator, Class<T> boundClass, Class<? extends T> implClass, Scope scope) {
		this.instanceGenerator = instanceGenerator
		this.boundClass = boundClass
		this.implClass = implClass
		validateScope(scope)
		this.scope = scope
	}

	@Override
	T getInstance() {
		if (!cachedInstance) {
			this.cachedInstance = instanceGenerator.createNewInstance()
		}
		return cachedInstance
	}

	@Override
	Class<? extends T> getImplClass() {
		return this.implClass
	}

	@Override
	Class<T> getBoundClass() {
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
	InstanceGenerator<T> getInstanceGenerator(){
		this.instanceGenerator
	}

	void validateScope(Scope scope) throws InvalidConfigurationException {
		if (!scope) {
			throw new InvalidConfigurationException("The Scope for this binding cannot be null: CacheingScopedBinding for Class: ${boundClass.getCanonicalName()} to: ${implClass.getCanonicalName()}")

		} else if (scope == Scope.ALWAYS_CREATE_NEW) {
			throw new InvalidConfigurationException("The scope: ${scope} is not compatible with t")
		}
	}
}