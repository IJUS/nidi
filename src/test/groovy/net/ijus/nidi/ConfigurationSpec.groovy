package net.ijus.nidi

import com.example.config.ComplexConfigScript
import com.example.config.ExampleConfigScript
import com.example.impl.BasicCCProcessor
import com.example.impl.ComplexCCProcessor
import com.example.impl.FraudDetectorImpl
import com.example.impl.LoggingServiceImpl
import com.example.impl.NamespacedLoggingService
import com.example.interfaces.CreditCardProcessor
import com.example.interfaces.FraudDetectionService
import com.example.interfaces.LoggingService
import com.example.interfaces.RefundProcessor
import net.ijus.nidi.bindings.BasicBinding
import net.ijus.nidi.bindings.Scope
import net.ijus.nidi.builder.BindingBuilder
import net.ijus.nidi.builder.ContextBuilder;
import spock.lang.Specification

import static net.ijus.nidi.Configuration.*

/**
 * Created by pfried on 6/16/14.
 */

public class ConfigurationSpec extends Specification {

	def cleanup(){
		ContextTestUtils.clearContextHolder()
	}

	void "configurations should inherit from one another"() {
		when:
		Context ctx = configureNew({
			it.inheritFrom(ExampleConfigScript.getName())
			it.bind(CreditCardProcessor).to(BasicCCProcessor)
		} as ContextConfig)

		then:
		def ccProc = ctx.getInstance(CreditCardProcessor)
		ccProc instanceof BasicCCProcessor

		def logger = ctx.getInstance(LoggingService)
		logger instanceof LoggingServiceImpl
	}

	void "configurations with references should be handled correctly"(){
		when:
		Context ctx = configureNew(ExampleConfigScript)

		then:
		def ccProc = ctx.getInstance(CreditCardProcessor)
		ccProc instanceof ComplexCCProcessor

		def refProc = ctx.getInstance(RefundProcessor)
		refProc.is(ccProc)


	}

	void "Binding references should provide the same implementation and scope as the referenced binding"(){
		given: "create a context with two singleton bindings"
		Context ctx = configureNew({
			it.defaultScope = Scope.SINGLETON
			it.bind(CreditCardProcessor).to(BasicCCProcessor)
			it.bind(RefundProcessor).reference(CreditCardProcessor)
		} as ContextConfig)

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
		ctx.bindingsMap.size() == 4

		cleanup:
		System.clearProperty(CONFIG_PROPERTY_NAME)
	}

	void "Configuration should work by specifying a class name as a string"(){
		setup:
		ContextBuilder builder = new ContextBuilder()

		when:
		configure(builder, "com.example.config.ExampleConfigScript")

		then:
		builder.ctxBindings.size() == 4
		builder.ctxBindings.get(CreditCardProcessor).impl == ComplexCCProcessor
		builder.ctxBindings.get(LoggingService).impl == LoggingServiceImpl

	}

	def "a properly configured context should provide a basic implementation of an interface"(){
		setup:
		Context ctx = configureNew({
			it.bind(CreditCardProcessor).to(BasicCCProcessor)
		} as ContextConfig)

		when:
		def instance = ctx.getInstance(CreditCardProcessor)

		then:
		instance instanceof BasicCCProcessor
	}

	void "attempting to configure a context using a class that doesn't exist should throw an error"() {
		given: "a dummy context"
		ContextBuilder builder = new ContextBuilder()

		when: "try to configure using fqcn of non-existant class"
		configure(builder, "com.somepackage.NonExistantClass")

		then: "Error should be thrown"
		thrown(InvalidConfigurationException)

		when: "Attempt to configure with real class that does not implement ContextConfig"
		configure(builder, BasicBinding)

		then: "Error should be thrown"
		thrown(InvalidConfigurationException)

		when: "try to conifgure with fqcn of a class that does not implement ContextConfig"
		configure(builder, "com.example.impl.BasicCCProcessor")

		then:
		thrown(InvalidConfigurationException)
	}


}