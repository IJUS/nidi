package net.ijus.nidi;

/**
 * Created by pfried on 6/16/14.
 */
public class InvalidConfigurationException extends RuntimeException {
    public InvalidConfigurationException(String reason) {
        super(reason);
    }

    public InvalidConfigurationException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
