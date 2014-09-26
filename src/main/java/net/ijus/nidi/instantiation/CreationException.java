package net.ijus.nidi.instantiation;

/**
 * Thrown when there's an exception creating a new instance of a class
 */
public class CreationException extends RuntimeException {

    public CreationException(String message) {
        super(message);
    }

    public CreationException(String message, Throwable cause) {
        super(message, cause);
    }
}
