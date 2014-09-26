package net.ijus.nidi.utils

import net.ijus.nidi.InvalidConfigurationException
import spock.lang.Specification

import static net.ijus.nidi.utils.ConfigurationAssert.*;
/**
 * Created by pfried on 9/14/14.
 */
class ConfigurationAssertSpec extends Specification {

    void "passing a null to assertThat should throw an exception"(){
        when:
        assertNotNull(null, "should not be null")

        then:
        def ex = thrown(InvalidConfigurationException)
        ex.message == "should not be null"

        when:
        assertNotNull("not null", "message")

        then:
        notThrown(InvalidConfigurationException)
    }

    void "test assertThat"(){
        when:
        assertThat(false, "message")

        then:
        def ex = thrown(InvalidConfigurationException)
        ex.message == 'message'

        when:
        assertThat(true, "message")

        then:
        notThrown(InvalidConfigurationException)
    }
}
