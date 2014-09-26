package net.ijus.nidi.instantiation;

/**
 * InstanceGenerator that always returns null.
 */
public class NullGenerator<T> implements InstanceGenerator<T> {
    private static final NullGenerator INSTANCE = new NullGenerator();

    public static NullGenerator getInstance(){
        return INSTANCE;
    }

    @Override
    public T createNewInstance() {
        return null;
    }
}
