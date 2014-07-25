package net.ijus.nidi.builder

import com.example.impl.BasicCCProcessor
import com.example.impl.ComplexCCProcessor
import com.example.impl.ConcreteClassNoInterface
import com.example.impl.FraudDetectorImpl
import com.example.impl.LoggingServiceImpl
import com.example.impl.NamespacedLoggingService
import com.example.interfaces.CreditCardProcessor
import com.example.interfaces.FraudDetectionService
import com.example.interfaces.LoggingService
import net.ijus.nidi.Context
import net.ijus.nidi.InvalidConfigurationException
import net.ijus.nidi.bindings.Scope
import spock.lang.Specification

import java.awt.event.MouseAdapter

/**
 * Created by pfried on 7/6/14.
 */

public class ContextBuilderSpec extends Specification {

	void "calling register and passing in an abstract class should throw an exception"(){
		setup:
		ContextBuilder builder = new ContextBuilder()

		when: "register an interface"
		builder.register(Map)

		then:
		thrown(InvalidConfigurationException)

		when: "register an abstract class"
		builder.register(MouseAdapter)

		then:
		thrown(InvalidConfigurationException)
	}

	void "inheriting from a parent should not override the default scope if it has been specified"(){
		setup:
		def parentConfig = {
			defaultScope = Scope.SINGLETON
		}
		ContextBuilder builder = new ContextBuilder()

		when:
		builder.with{
			defaultScope = Scope.ONE_PER_BINDING
			inheritFrom(parentConfig)
		}

		then:
		builder.defaultScope == Scope.ONE_PER_BINDING
	}

	void "inheriting from a parent should set the default scope if it has not been specified"(){
		setup:
		def parentConfig = {
			defaultScope = Scope.SINGLETON
		}
		ContextBuilder builder = new ContextBuilder()

		when:
		builder.inheritFrom(parentConfig)

		then:
		builder.defaultScope == Scope.SINGLETON
	}

	void "ContextBuilder should throw InvalidConfigurationException when attempting to inherit from an invalid class"(){
		setup:
		ContextBuilder builder = new ContextBuilder()

		when:
		builder.inheritFrom(current)

		then:
		thrown(InvalidConfigurationException)

		where:
		current << ['non.existant.class.MyClass', BasicCCProcessor]
	}


}