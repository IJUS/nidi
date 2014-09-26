package net.ijus.nidi.bindings;

import net.ijus.nidi.InvalidConfigurationException;
import net.ijus.nidi.instantiation.InstanceGenerator;

/**
 * A Binding that caches the generated instance. This Binding will only ever call out to its InstanceGenerator
 * once, one the first call to getInstance(). After that, all subsequent calls will return the same instance.
 */
public class CachingBinding<T> implements Binding<T> {
    public CachingBinding(InstanceGenerator<T> instanceGenerator, Class<T> boundClass, Class<? extends T> implClass, Scope scope) {
        this.instanceGenerator = instanceGenerator;
        this.boundClass = boundClass;
        this.implClass = implClass;
        validateScope(scope);
        this.scope = scope;
    }

    @Override
    public T getInstance() {
        if (cachedInstance == null) {
            this.cachedInstance = instanceGenerator.createNewInstance();
        }

        return cachedInstance;
    }

    @Override
    public Class<? extends T> getImplClass() {
        return this.implClass;
    }

    @Override
    public Class<T> getBoundClass() {
        return this.boundClass;
    }

    @Override
    public Scope getScope() {
        return this.scope;
    }

    @Override
    public void validate() {

    }

    @Override
    public InstanceGenerator<T> getInstanceGenerator() {
        return this.instanceGenerator;
    }

    public void validateScope(final Scope scope) throws InvalidConfigurationException {
        if (scope == null) {
            throw new InvalidConfigurationException("The Scope for this binding cannot be null: CachingScopedBinding for Class: " + boundClass.getCanonicalName() + " to: " + implClass.getCanonicalName());

        } else if (scope.equals(Scope.ALWAYS_CREATE_NEW)) {
            throw new InvalidConfigurationException("The scope: " + String.valueOf(scope) + " is not compatible with t");
        }

    }

    public T getCachedInstance() {
        return cachedInstance;
    }

    public void setCachedInstance(T cachedInstance) {
        this.cachedInstance = cachedInstance;
    }

    public void setInstanceGenerator(InstanceGenerator<T> instanceGenerator) {
        this.instanceGenerator = instanceGenerator;
    }

    public void setBoundClass(Class<T> boundClass) {
        this.boundClass = boundClass;
    }

    public void setImplClass(Class<? extends T> implClass) {
        this.implClass = implClass;
    }

    public void setScope(Scope scope) {
        this.scope = scope;
    }

    private T cachedInstance;
    private InstanceGenerator<T> instanceGenerator;
    private Class<T> boundClass;
    private Class<? extends T> implClass;
    private Scope scope;
}
