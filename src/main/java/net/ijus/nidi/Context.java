package net.ijus.nidi;

import net.ijus.nidi.bindings.Binding;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by pfried on 6/16/14.
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

    public <T> T getInstance(final Class<T> clazz) {
        if (!containsBinding(clazz)) {
            throw new InvalidConfigurationException("The Class: " + clazz.getCanonicalName() + " was requested from a Context, but no Binding exists for it");
        }

        return getBinding(clazz).getInstance();
    }

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
