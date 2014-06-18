package net.ijus.nidi

import groovy.transform.CompileStatic
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.lang.reflect.Constructor;

/**
 * Created by pfried on 6/13/14.
 */
@CompileStatic
public class BasicBinding implements Binding {
	static final Logger log = LoggerFactory.getLogger(BasicBinding)

	Context parentContext

	Class bound;
	Class implementation;

	SingleBindingCache bindingOnlyCache

	Scope scope

	Closure setupClosure

	BasicBinding(Class bound, Class impl, Context parentContext) {
		if (!bound.isAssignableFrom(impl)) {
			throw new InvalidConfigurationException("The class binding for: ${bound.getCanonicalName()} must be assignable from ${impl.getCanonicalName()} but it is not!")
		}
		this.bound = bound
		this.implementation = impl
		this.parentContext = parentContext
	}

	@Override
	Binding setupInstance(Closure closure) {
		this.setupClosure = closure
		return this
	}

	@Override
	Class getBoundClass() {
		return this.bound
	}

	Class getImplementationClass() {
		return this.implementation;
	}

	@Override
	def getInstance() {

		ImplementationCache implCache = getCacheForScope()

		def instance = implCache?.getCachedInstance(implementation)

		if (!instance) {
			instance = createNewInstance()
			if (implCache) {
				implCache.cacheInstance(instance)
			}
		}
		return instance
	}

	private ImplementationCache getCacheForScope() {
		checkAndSetScope()
		ImplementationCache cacheToUse
		switch (scope) {
			case Scope.SINGLETON:
				cacheToUse = ContextHolder.singletonCache
				break
			case Scope.CONTEXT_GLOBAL:
				cacheToUse =  parentContext?.getCache()
				break
			case Scope.ONE_PER_BINDING:
				if (!bindingOnlyCache){ bindingOnlyCache = new SingleBindingCache() }
				cacheToUse =  bindingOnlyCache
				break
			default:
				cacheToUse = null
		}
		return cacheToUse
	}

	void checkAndSetScope() {
		Scope newScope = parentContext?.getScopeForClass(implementation)
		if (newScope && scope && newScope < scope) {

			log.warn("The Class: ${implementation.getCanonicalName()} has been scoped as ${newScope} by another Binding. The Binding for ${bound.simpleName} has been changed from ${scope} to ${newScope} in order to respect the broader scope")
			scope = newScope

		} else if (newScope && !scope) {
			scope = newScope
		}
	}

	def createNewInstance(){
		log.trace("Creating new instance of ${implementationClass.getCanonicalName()} as an implementation of: ${bound.getSimpleName()}")

		Constructor constructor = resolveConstructor()
		List<Class> parameterTypes = constructor.getParameterTypes().toList()
		String paramDesc = (parameterTypes)? parameterTypes.collect{Class c-> c.getSimpleName()}.join(", ") : "(zero-arg constructor)"
		log.trace("Constructor found with parameter types: ${paramDesc}")

		List constructorParams = parameterTypes.collect{Class type-> getConstructorParameterInstance(type) }

		def instance = (constructorParams)? implementationClass.newInstance(constructorParams as Object[]) : implementationClass.newInstance()
		log.debug("created new instance of ${implementationClass.getCanonicalName()}")

		if (null != setupClosure) {
			setupClosure.setDelegate(instance)
			setupClosure.call(instance)
		}

		return instance
	}

	def getConstructorParameterInstance(Class clazz) {
		return parentContext.getInstance(clazz)
	}

	Constructor resolveConstructor(){
		Constructor[] constructors = getImplementationClass().getConstructors()
		if (constructors.length == 1) {
			return constructors[0]
		}

		throw new InvalidConfigurationException("The Class: ${getBoundClass().getCanonicalName()} was bound to ${getImplementationClass().getCanonicalName()}, which either has 0 or 2+ public constructors")
	}




}
