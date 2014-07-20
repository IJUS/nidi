package net.ijus.nidi.builder

import groovy.transform.CompileStatic
import net.ijus.nidi.Context
import net.ijus.nidi.bindings.Binding
import net.ijus.nidi.ContextConfig
import net.ijus.nidi.InvalidConfigurationException
import net.ijus.nidi.bindings.Scope
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.lang.reflect.Modifier

/**
 * Created by pfried on 7/2/14.
 */

@CompileStatic
public class ContextBuilder {
	static final Logger log = LoggerFactory.getLogger(ContextBuilder)

	boolean defaultScopeChanged = false

	Scope defaultScope = Scope.ALWAYS_CREATE_NEW
	Map<Object, BindingBuilder> ctxBindings = [:]

	protected Context ctx = new Context()

	/**
	 * alias for newBinding()
	 * @param clazz
	 * @return
	 */
	BindingBuilder bind(Class clazz){
		return newBinding(clazz)
	}

	/**
	 * Creates a new BindingBuilder for this class, with this ContextBuilder as it's parent.
	 * @param clazz the base class for this binding, typically an Interface or Abstract Class
	 * @return a new BindingBuilder, ready to be configured.
	 */
	BindingBuilder newBinding(Class clazz){
		BindingBuilder bb = new BindingBuilder(clazz, this)
		ctxBindings.put(clazz, bb)
		bb
	}

	/**
	 * Binds a Class to itself. This is useful for letting the context handle instantiation of concrete classes with
	 * dependencies on other classes in the context
	 * @param clazz concrete class to bind to itself
	 * @return
	 * @throws InvalidConfigurationException if the class passed in is an interface or abstract class
	 */
	BindingBuilder register(Class clazz) throws InvalidConfigurationException {
		if (Modifier.isAbstract(clazz.getModifiers())) {
			throw new InvalidConfigurationException("Attemted to register the abstract class: ${clazz.name}. Only Concrete classes can be registered in this way.")
		}
		BindingBuilder bb = newBinding(clazz)
		bb.to(clazz)
		return bb
	}

	/**
	 * Sets a property that will be bound to any Constructor parameters annotated with @RequiredBinding('myProperty')
	 *
	 * @param propertyName the property name that matches the value of a RequiredBinding
	 * @param value the value to be bound to
	 * @return the BindingBuilder, for chaining additional calls
	 */
	BindingBuilder bindProperty(String propertyName, Object value){
		return bindProperty(propertyName){ value }
	}

	BindingBuilder bindProperty(String propertyName, Closure returnsValue) {
		Class valueClass

		try {
			valueClass = returnsValue.call().getClass()
		} catch (Exception e) {
			throw new InvalidConfigurationException("Attempted to bind the property: ${propertyName} to the return value of a Closure, but the closure threw: ${e.getClass().getSimpleName()} when it was called", e)
		}

		BindingBuilder bb = new BindingBuilder(valueClass, this)
		bb.toValue(returnsValue)
		this.ctxBindings.put(propertyName, bb)
		return bb
	}

	/**
	 * Sets the default scope for this contextBuilder. All bindings will inherit this scope, unless one is
	 * explicitly set on it.
	 * @param newScope
	 */
	void setDefaultScope(Scope newScope) {
		this.defaultScope = newScope
		this.defaultScopeChanged = true
	}

	/**
	 * {@see #inheritFrom(ContextConfig)}
	 * @param fqcn fully-qualified class name of the ContextConfig class
	 */
	void inheritFrom(String fqcn){
		Class configClass
		try {
			configClass = Class.forName(fqcn)
		} catch (ClassNotFoundException e) {
			throw new InvalidConfigurationException("Tried to inherit from: ${fqcn}, but the class could not be found", e)
		}
		inheritFrom(configClass)
	}

	/**
	 * {@see #inheritFrom(ContextConfig)}
	 * @param configClass a class that implements ContextConfig, which will be created by simply calling newInstance() on it
	 */
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

	/**
	 * Inherits from another ContextConfig. Allows for having a default ContextConfig and allows other Contexts to inherit
	 * from it and override only the functionality they need to.
	 * @param config
	 */
	void inheritFrom(ContextConfig config) {
		ContextBuilder parentBuilder = new ContextBuilder()
		config.configure(parentBuilder)
		doInheritance(parentBuilder)
	}

	/**
	 * {@see #inheritFrom(ContextConfig)}
	 * @param config closure that configures a contextBuilder
	 */
	void inheritFrom(Closure config){
		ContextBuilder parentBuilder = new ContextBuilder()
		config.setDelegate(parentBuilder)
		config.call(parentBuilder)
		doInheritance(parentBuilder)
	}

	/**
	 * Consumes the parent context builder and adds it's bindings to this builder. Always preserves the bindings from this
	 * builder if there is a conflict.
	 * Don't call this method directly, and the parentBuilder is expected to be a throw-away created only for the purpose
	 * of inheriting it's bindings.
	 * @param parentBuilder
	 */
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
		ctxBindings.each{Object key, BindingBuilder builder->
			builder.inheritScope(defaultScope)
			builder.validateClassAssignment()
			Binding binding = builder.build()
			log.debug("Adding Binding: ${binding} to the Context")
			ctx.bindingsMap.put(key, binding)
		}

		return ctx

	}
}