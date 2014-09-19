package net.ijus.nidi.instantiation;

import net.ijus.nidi.InvalidConfigurationException;
import net.ijus.nidi.bindings.Binding;
import net.ijus.nidi.utils.ClassUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Constructor;

/**
 * An instance generator that creates instances of the given class, using an optional array of Bindings to provide the
 * constructor parameters. An optional InstanceSetupFunction can also be provided. If it is, then this function will be
 * called every time a new instance is generated.
 */
public class ConstructorInstanceGenerator<T> implements InstanceGenerator<T> {
    private static final Logger log = LoggerFactory.getLogger(ConstructorInstanceGenerator.class);

    Binding[] constructorArgs;
    Class<T> clazz;
    InstanceSetupFunction<T> setup;
    MethodHandle constructorHandle;

    public ConstructorInstanceGenerator(Class<T> clazz, Binding[] constructorArgs, InstanceSetupFunction<T> setup) throws InvalidConfigurationException {
        this.clazz = clazz;
        this.constructorArgs = (constructorArgs != null)? constructorArgs: new Binding[0];
        this.setup = setup;
        this.constructorHandle = createConstructorHandle();
    }

    public ConstructorInstanceGenerator(Class<T> clazz, Binding[] constructorArgs) {
        this(clazz, constructorArgs, null);
    }

    public ConstructorInstanceGenerator(Class<T> clazz) {
        this(clazz, null, null);
    }

    public ConstructorInstanceGenerator(Class<T> clazz, InstanceSetupFunction<T> setup) {
        this(clazz, null, setup);
    }

    /**
     * Creates the MethodHandle that will be used to create new instances.
     *
     * @return
     * @throws net.ijus.nidi.InvalidConfigurationException if the Constructor cannot be resolved
     */
    protected MethodHandle createConstructorHandle() throws InvalidConfigurationException {
        MethodHandles.Lookup lookup = MethodHandles.lookup();
        try {
            Class[] args;
            if (constructorArgs != null && constructorArgs.length > 0) {
                args = new Class[constructorArgs.length];
                for (int i = 0; i < constructorArgs.length; i++) {
                    args[i] = constructorArgs[i].getBoundClass();
                }

            } else {
                args = new Class[0];
            }
            /*
            We lookup the Constructor using the standard Reflection api since we already have a reference to the
            Class we want to instantiate, and we only need to do the lookup once so speed isn't as critical.
            When we actually create the instance, we want to use a MethodHandle, though.
             */
            Constructor<T> resolvedConstructor = clazz.getConstructor(args);

            //Nidi only uses public constructors, but setting accessible will skip access checks.
            // this can help speed things up
            resolvedConstructor.setAccessible(true);
            return lookup.unreflectConstructor(resolvedConstructor);

        } catch (IllegalAccessException | NoSuchMethodException e) {
            //Throw an unchecked exception here, because this should never happen
            throw new InvalidConfigurationException("Could not create MethodHandle for Constructor" + clazz.getSimpleName(), e);
        } catch (SecurityException e) {
            throw new InvalidConfigurationException("Got a security exception when resolving constructor for " + clazz.getSimpleName(), e);
        }
    }

    @Override
    public T createNewInstance() {
        Object[] args = new Object[constructorArgs.length];

        for (int i = 0; i < constructorArgs.length; i++) {
            args[i] = constructorArgs[i].getInstance();
        }

        T instance;
        try {
            Object uncast;
            if (args.length > 0) {
                uncast = constructorHandle.invokeWithArguments(args);
            } else {
                uncast = constructorHandle.invoke();
            }
            instance = clazz.cast(uncast);

        } catch (Throwable e) { //yeah, methodHandles are a little scary
            String msg = "Error creating a new instance of: " + clazz.getName();
            log.error(msg, e);
            throw new CreationException(msg, e);
        }


        if (setup != null) {
            setup.setup(instance);
        }

        return instance;
    }

    public Binding[] getConstructorArgs() {
        return constructorArgs;
    }

    public void setConstructorArgs(Binding[] constructorArgs) {
        this.constructorArgs = constructorArgs;
    }

    public Class<T> getClazz() {
        return clazz;
    }

    public void setClazz(Class<T> clazz) {
        this.clazz = clazz;
    }

    public InstanceSetupFunction<T> getSetup() {
        return setup;
    }

    public void setSetup(InstanceSetupFunction<T> setup) {
        this.setup = setup;
    }


}
