package net.ijus.nidi.builder;

import net.ijus.nidi.Context;
import net.ijus.nidi.bindings.Binding;
import net.ijus.nidi.ContextConfig;
import net.ijus.nidi.InvalidConfigurationException;
import net.ijus.nidi.bindings.Scope;
import net.ijus.nidi.instantiation.InstanceGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Modifier;
import java.util.LinkedHashMap;
import java.util.Map;

import static net.ijus.nidi.utils.ConfigurationAssert.*;
/**
 * Builder for creating a Context. This is the main class used for configuration.
 */

public class ContextBuilder {
	static final Logger log = LoggerFactory.getLogger(ContextBuilder.class);

	boolean defaultScopeChanged = false;

	Scope defaultScope = Scope.ALWAYS_CREATE_NEW;
	Map<Object, BindingBuilder> ctxBindings = new LinkedHashMap<Object ,BindingBuilder>();

	protected Context ctx = new Context();

	/**
	 * alias for newBinding()
	 * @param clazz
	 * @return
	 */
    public <E> BindingBuilder<E> bind(Class<E> clazz){
		return newBinding(clazz);
	}

	/**
	 * Creates a new BindingBuilder for this class, with this ContextBuilder as it's parent.
	 * @param clazz the base class for this binding, typically an Interface or Abstract Class
	 * @return a new BindingBuilder, ready to be configured.
	 */
	public <E> BindingBuilder<E> newBinding(Class<E> clazz) {
        return newBinding(null, clazz);
    }

    /**
     * alias for newBinding(String, Class)
     * @param name
     * @param baseClass
     * @param <E>
     * @return
     */
    public <E> BindingBuilder<E> bind(String name, Class<E> baseClass) {
        return newBinding(name, baseClass);
    }

    /**
     * Creates a new BindingBuilder and registers it with the ContextBuilder. If name is not null, then the name is
     * used to register the bindingBuilder and it can be refered to by name ONLY. If name is null, then the base class
     * is instead used to refer to the Binding
     *
     * @param name the name to be used to reference the Binding.
     * @param baseClass The base class that will be provided. required
     * @param <E> the type of the baseClass
     * @return a new BindingBuilder, registered with this ContextBuilder
     */
    public <E> BindingBuilder<E> newBinding(String name, Class<E> baseClass) {
        if (baseClass == null) {
            throw new InvalidConfigurationException("The base Class cannot be null when creating a new BingingBuilder");
        }
        BindingBuilder<E> bb = new BindingBuilder<>(baseClass, this);
        if (name != null && name.length() > 0) {
            ctxBindings.put(name, bb);
        } else {
            ctxBindings.put(baseClass, bb);
        }
        return bb;
    }

	/**
	 * Binds a Class to itself. This is useful for letting the context handle instantiation of concrete classes with
	 * dependencies on other classes in the context
	 * @param clazz concrete class to bind to itself
	 * @return
	 * @throws InvalidConfigurationException if the class passed in is an interface or abstract class
	 */
	public <E> BindingBuilder<E> register(Class<E> clazz) throws InvalidConfigurationException {
		if (Modifier.isAbstract(clazz.getModifiers())) {
			throw new InvalidConfigurationException("Attempted to register the abstract class: " + clazz.getName() +". Only Concrete classes can be registered in this way.");
		}
		BindingBuilder<E> bb = newBinding(clazz);
		bb.bindTo(clazz);
		return bb;
	}

    public BindingBuilder bindProperty(final String propertyName, Object value){
        assertNotNull(value, "Cannot bind a property to null using the 2-arg method. Instead use: <E> bindProperty(String, Class<E>, E value");
        BindingBuilder bb = bind(propertyName, value.getClass());
        return bb.toObject(value);
    }

	/**
	 * Sets a property that will be bound to any Constructor parameters annotated with @Require('myProperty')
	 *
	 * @param propertyName the property name that matches the value of a Require
	 * @param value the value to be bound to. Can be null, in which case a NullBinding will be produced
	 * @return the BindingBuilder, for chaining additional calls
	 */
	public <E> BindingBuilder<E> bindProperty(final String propertyName, final Class<E> clazz, final E value) {
        return bind(propertyName, clazz).toObject(value);
    }

	/**
	 * Sets the default scope for this contextBuilder. All bindings will inherit this scope, unless one is
	 * explicitly set on it.
	 * @param newScope
	 */
	public void setDefaultScope(Scope newScope) {
		this.defaultScope = newScope;
		this.defaultScopeChanged = true;
	}

	/**
	 * {@see #inheritFrom(ContextConfig)}
	 * @param fqcn fully-qualified class name of the ContextConfig class
	 */
	public void inheritFrom(String fqcn){
		Class configClass;
		try {
			configClass = Class.forName(fqcn);

        } catch (ClassNotFoundException e) {
			throw new InvalidConfigurationException("Tried to inherit from: "+fqcn+", but the class could not be found", e);
		}
		inheritFrom(configClass);
	}

	/**
	 * {@see #inheritFrom(ContextConfig)}
	 * @param configClass a class that implements ContextConfig, which will be created by simply calling newInstance() on it
	 */
	public void inheritFrom(Class configClass) {
		if (!ContextConfig.class.isAssignableFrom(configClass)) {
			throw new InvalidConfigurationException("Tried to inherit from: "+configClass.getName()+", but that class does not implement ContextConfig");
		}
		ContextConfig config;
		try {
			config = (ContextConfig) configClass.newInstance();

		} catch (InstantiationException | IllegalAccessException e) {
			throw new InvalidConfigurationException("Tried to inherit, but failed to create a new instance of class: " +configClass.getName(), e);
		}
		inheritFrom(config);
	}

	/**
	 * Inherits from another ContextConfig. Allows for having a default ContextConfig and allows other Contexts to inherit
	 * from it and override only the functionality they need to.
	 * @param config
	 */
	void inheritFrom(ContextConfig config) {
		ContextBuilder parentBuilder = new ContextBuilder();
		config.configure(parentBuilder);
		doInheritance(parentBuilder);
	}

    public boolean containsNonNullBinding(Object key){
        return containsBindingFor(key) && !ctxBindings.get(key).isBoundToNull();
    }

	public boolean containsBindingFor(Object key) {
		return ctxBindings.containsKey(key);
	}

	public Context getContextRef(){
		return this.ctx;
	}

	public Context build() throws InvalidConfigurationException {
		log.debug("Building Context with {} Bindings in the root context", ctxBindings.size());
        for (Object key : ctxBindings.keySet()) {
            BindingBuilder bb = ctxBindings.get(key);
            bb.inheritScope(defaultScope);
            bb.validateClassAssignment();
            Binding binding = bb.build();
            log.debug("Adding Binding: {} to the Context", binding);
            ctx.getBindingsMap().put(key, binding);
        }

		return ctx;

	}

    public Scope getDefaultScope() {
        return defaultScope;
    }

    public Map<Object, BindingBuilder> getCtxBindings() {
        return ctxBindings;
    }


    /**
     * Consumes the parent context builder and adds it's bindings to this builder. Always preserves the bindings from this
     * builder if there is a conflict.
     * Don't call this method directly, and the parentBuilder is expected to be a throw-away created only for the purpose
     * of inheriting it's bindings.
     * @param parentBuilder
     */
    private void doInheritance(ContextBuilder parentBuilder) {
        Map<Object, BindingBuilder> parentBindings = parentBuilder.ctxBindings;
        log.debug("Inheriting from ContextBuilder with class: {} containing {} bindings", parentBuilder.getClass().getName(), parentBindings.size());
        parentBindings.putAll(ctxBindings); //Add this builders bindings to the parent's, overriding the parents' if there are any conflicts
        this.ctxBindings = parentBindings;

        //also inherit the default scope if needed
        if (!defaultScopeChanged && this.defaultScope != parentBuilder.defaultScope) {
            log.debug("Inheriting the default scope specified in the parent context builder: {}", parentBuilder.defaultScope);
            setDefaultScope(parentBuilder.defaultScope);
        }
    }

}