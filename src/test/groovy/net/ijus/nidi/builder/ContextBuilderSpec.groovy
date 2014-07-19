package net.ijus.nidi.builder

import com.example.impl.BasicCCProcessor
import com.example.impl.ComplexCCProcessor
import com.example.impl.FraudDetectorImpl
import com.example.impl.LoggingServiceImpl
import com.example.interfaces.CreditCardProcessor
import com.example.interfaces.FraudDetectionService
import com.example.interfaces.LoggingService
import net.ijus.nidi.Context
import net.ijus.nidi.InvalidConfigurationException
import net.ijus.nidi.bindings.Scope
import net.ijus.nidi.builder.BindingBuilder
import net.ijus.nidi.builder.ContextBuilder
import spock.lang.Specification

/**
 * Created by pfried on 7/6/14.
 */

public class ContextBuilderSpec extends Specification {

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

	void "ContextBuilder should set the default scope on bindings with unspecified scopes"() {
		setup:
		ContextBuilder builder = new ContextBuilder()

		when:
		builder.setDefaultScope(Scope.SINGLETON)
		builder.bind(CreditCardProcessor).to(BasicCCProcessor)
		builder.build()

		then:
		BindingBuilder bb = builder.ctxBindings.get(CreditCardProcessor)
		bb.scope == Scope.SINGLETON
	}

	void "A context with nested dependencies should be created"() {
		setup:
		ContextBuilder builder = new ContextBuilder()

		when:
		builder.bind(CreditCardProcessor).to(ComplexCCProcessor)
		builder.bind(FraudDetectionService).to(FraudDetectorImpl)
		builder.bind(LoggingService).to(LoggingServiceImpl)
		Context ctx = builder.build()

		then:
		notThrown(InvalidConfigurationException)
		ctx.containsBinding(CreditCardProcessor)
		ctx.containsBinding(FraudDetectionService)
		ctx.containsBinding(LoggingService)

	}

	void "A basic context should be built properly"() {
		setup:
		ContextBuilder builder = new ContextBuilder()

		when:
		builder.bind(CreditCardProcessor).to(BasicCCProcessor)
		Context ctx = builder.build()

		then:
		ctx.containsBinding(CreditCardProcessor)
		net.ijus.nidi.bindings.Binding b = ctx.getBindingForClass(CreditCardProcessor)
		b.boundClass == CreditCardProcessor
		b.implClass == BasicCCProcessor
	}
}