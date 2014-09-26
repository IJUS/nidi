package net.ijus.nidi.utils;

import net.ijus.nidi.InvalidConfigurationException;

/**
 * Created by pfried on 9/14/14.
 */
public class ConfigurationAssert {


    public static void assertNotNull(Object o, String message) {
        if (o == null) {
            throw new InvalidConfigurationException(message);
        }
    }

    public static void assertThat(boolean shouldBeTrue, String message) {
        if (!shouldBeTrue) {
            throw new InvalidConfigurationException(message);
        }
    }


}
