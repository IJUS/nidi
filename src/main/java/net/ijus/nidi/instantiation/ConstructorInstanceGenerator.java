package net.ijus.nidi.instantiation;

import net.ijus.nidi.InvalidConfigurationException;
import net.ijus.nidi.bindings.Binding;
import net.ijus.nidi.utils.ClassUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;

/**
 * Created by pfried on 7/5/14.
 */
public class ConstructorInstanceGenerator<T> implements InstanceGenerator<T> {
    private static final Logger log = LoggerFactory.getLogger(ConstructorInstanceGenerator.class);

    Binding[] constructorArgs;
    Class<? extends T> clazz;
    InstanceSetupFunction<T> setup;
    Constructor<? extends T> resolvedConstructor;

    public ConstructorInstanceGenerator(Class<? extends T> clazz, Binding[] constructorArgs, InstanceSetupFunction<T> setup) {
        this.clazz = clazz;
        this.constructorArgs = (constructorArgs != null)? constructorArgs: new Binding[0];
        this.setup = setup;
        this.resolvedConstructor = resolveConstructor();
    }

    public ConstructorInstanceGenerator(Class<? extends T> clazz, Binding[] constructorArgs) {
        this(clazz, constructorArgs, null);
    }

    public ConstructorInstanceGenerator(Class<? extends T> clazz) {
        this(clazz, null, null);
    }

    public ConstructorInstanceGenerator(Class<? extends T> clazz, InstanceSetupFunction<T> setup) {
        this(clazz, null, setup);
    }

    protected Constructor<? extends T> resolveConstructor(){
        Class[] args;
        if (constructorArgs != null && constructorArgs.length > 0) {
            args = new Class[constructorArgs.length];
            for (int i = 0; i < constructorArgs.length; i++) {
                args[i] = constructorArgs[i].getBoundClass();
            }

        } else {
            args = new Class[0];
        }

        try {
            Constructor<? extends T> resolved = this.clazz.getConstructor(args);

            /*
            Even though nidi only uses public constructors, setting it accessible causes java to skip
            the access checks altogether. This improves performance, especially if we're going to be generating a lot
            of instances.
             */
            resolved.setAccessible(true);
            return resolved;

        } catch (NoSuchMethodException e){
            throw new InvalidConfigurationException("The Class " + clazz.getName() + " does not have an accessible constructor for arguments: " + ClassUtils.classNames(args));
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
            instance = resolvedConstructor.newInstance(args);
        } catch (Exception e) {
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

    public Class<? extends T> getClazz() {
        return clazz;
    }

    public void setClazz(Class<? extends T> clazz) {
        this.clazz = clazz;
    }

    public InstanceSetupFunction<T> getSetup() {
        return setup;
    }

    public void setSetup(InstanceSetupFunction<T> setup) {
        this.setup = setup;
    }


}
