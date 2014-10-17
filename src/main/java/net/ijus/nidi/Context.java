package net.ijus.nidi;

import net.ijus.nidi.bindings.Binding;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Context is the main point of contact between NiDI and the rest of the application. Context handles generating and managing all of the classes that are bound within it's configuration.
 * A Context should never be instantiated directly, but only by using a <code>ContextBuilder</code> or else one of the <code>Configuration</code> methods.
 * Once a Context is created, the application would simply request instances of concrete classes from the context instead of instantiating them directly. For instance, if the interface
 * <code>Foo</code> is 'bound' to the concrete implementation of <code>FooImpl</code>, then you would simply call <code>Foo foo = Context.getInstance(Foo)</code>. This will provide an instance
 * of <code>FooImpl</code>. This works even if the concrete Implementation itself depends on other classes.
 *
 * Once the Context is created, the important methods are <code>getInstance(Class or Object)</code>. These will provide an instance of the correct class, all ready to go.
 */
public class Context {
    private Map<Object, Binding> bindingsMap = new LinkedHashMap<Object, Binding>();


    public <T> Binding<T> getBinding(Class<T> key) {
        return (Binding<T>) bindingsMap.get(key);
    }

	public Binding getBinding(String key) {
		return bindingsMap.get(key);
	}

    public boolean containsBinding(Object key) {
        return bindingsMap.containsKey(key);
    }

	/**
	 * Returns a concrete implementation of the given class. If a Binding for the given class doesn't exist, then an exception will be thrown. The exact instance returned will depend on
	 * the specific Binding that was setup in the ContextConfig. In particular, it will depend on the Scope of the Binding, which determines it's instance generation strategy. If a
	 * Binding has <code>Scope.SINGLETON</code>, for example, then the same instance will be returned each time it is requested. But, if the scope is <code>ALWAYS_CREATE_NEW</code> then
	 * a new instance will be created each time one is requested.
	 *
	 * @param clazz The type of instance being requested
	 * @param <T> the type of requested class
	 * @return an instance of the class requested, already cast to the correct type.
	 */
    public <T> T getInstance(final Class<T> clazz) {
        if (!containsBinding(clazz)) {
            throw new InvalidConfigurationException("The Class: " + clazz.getCanonicalName() + " was requested from a Context, but no Binding exists for it");
        }

        return getBinding(clazz).getInstance();
    }

	/**
	 * The same as <code>getInstance(Class)</code> except that the returned instance will not be automatically cast to the correct type. This method is typically only called by other
	 * Bindings in the context when they require constructor parameters that are identified by property Strings.
	 *
	 * @param key the name of the property. This should match with the property name used in any @Require(String) or @Optional(String) annotations.
	 * @return the requested instance
	 */
    public Object getInstance(final String key) {
        if (!containsBinding(key)) {
            throw new InvalidConfigurationException("The Property: " + key + " was requested from a Context but no Binding exists for it");
        }

        return getBinding(key).getInstance();
    }

    public Map<Object, Binding> getBindingsMap() {
        return bindingsMap;
    }

    public void setBindingsMap(Map<Object, Binding> bindingsMap) {
        this.bindingsMap = bindingsMap;
    }

}
