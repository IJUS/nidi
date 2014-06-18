package net.ijus.nidi;

/**
 * Created by pfried on 6/16/14.
 */

public class InvalidConfigurationException extends RuntimeException {


	InvalidConfigurationException(String reason) {
		super(reason)
	}

	InvalidConfigurationException(String msg, Throwable cause) {
		super(msg, cause)
	}
}
