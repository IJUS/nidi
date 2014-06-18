package net.ijus.nidi

import com.example.impl.BasicCCProcessor
import com.example.impl.ComplexCCProcessor
import com.example.impl.LoggingServiceImpl
import com.example.interfaces.CreditCardProcessor
import com.example.interfaces.LoggingService
import com.example.interfaces.RefundProcessor;
import spock.lang.Specification

import static net.ijus.nidi.Configuration.*

/**
 * Created by pfried on 6/16/14.
 */

public class ConfigurationSpec extends Specification {

	def cleanup(){
		TestUtils.clearContextHolder()
	}

	void "Bindings that are scoped as singletons should all reference the same class instance for a given implementation"(){
		given: "create a context with two singleton bindings"
		Context ctx = configureNew {
			defaultScope = Scope.SINGLETON
			bind(CreditCardProcessor).to(BasicCCProcessor)
			bind(RefundProcessor).to(BasicCCProcessor)
		}

		when:
		def inst1 = ctx.getInstance(CreditCardProcessor)
		def inst2 = ctx.getInstance(RefundProcessor)

		then:
		inst1.is(inst2)

	}

	void "Configuration should work by specifying a class name as a system property"() {
		setup:
		System.setProperty(CONFIG_PROPERTY_NAME, 'com.example.config.ExampleConfigScript')

		when:
		Context ctx = configureNewFromSystemProperty()

		then:
		notThrown(InvalidConfigurationException)
		ctx.getBindings().size() == 3

		cleanup:
		System.clearProperty(CONFIG_PROPERTY_NAME)
	}

	void "Configuration should work by specifying a class name as a string"(){
		setup:
		Context ctx = new Context()

		when:
		configure(ctx, "com.example.config.ExampleConfigScript")

		then:
		ctx.getBindings().size() == 3
		ctx.getBinding(CreditCardProcessor).implementationClass == ComplexCCProcessor
		ctx.getBinding(LoggingService).implementationClass == LoggingServiceImpl
	}

	def "a properly configured context should provide a basic implementation of an interface"(){
		setup:
		Context ctx = new Context()
		configure(ctx) {
			bind(CreditCardProcessor).to(BasicCCProcessor)
		}

		when:
		def instance = ctx.getInstance(CreditCardProcessor)

		then:
		instance instanceof BasicCCProcessor
	}

	def "configuration should work with closures"() {
		setup:
		Context ctx = new Context()

		when: "bind basic class with a closure"
		configure(ctx){
			bind(CreditCardProcessor).to(BasicCCProcessor)
		}

		then:
		notThrown(InvalidConfigurationException)
		ctx.bindings.size() == 1
		Binding b = ctx.getBinding(CreditCardProcessor)
		b != null
		b.getImplementationClass() == BasicCCProcessor


	}

	void "attempting to configure a context using a class that doesn't exist should throw an error"() {
		given: "a dummy context"
		Context ctx = new Context()

		when: "try to configure using fqcn of non-existant class"
		configure(ctx, "com.somepackage.NonExistantClass")

		then: "Error shoulld be thrown"
		thrown(InvalidConfigurationException)

		when: "Attempt to configure with real class that does not implement ContextConfig"
		configure(ctx, BasicBinding)

		then: "Error should be thrown"
		thrown(InvalidConfigurationException)

		when: "try to conifgure with fqcn of a class that does not implement ContextConfig"
		configure(ctx, "com.example.impl.BasicCCProcessor")

		then:
		thrown(InvalidConfigurationException)
	}


}