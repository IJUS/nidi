package net.ijus.nidi

import com.example.impl.BasicCCProcessor
import com.example.impl.ComplexCCProcessor
import com.example.impl.FraudDetectorImpl
import com.example.impl.LoggingServiceImpl
import com.example.interfaces.CreditCardProcessor
import com.example.interfaces.FraudDetectionService
import com.example.interfaces.LoggingService
import spock.lang.Specification

/**
 * Created by pfried on 7/6/14.
 */

public class ContextBuilderSpec extends Specification {

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
		Binding b = ctx.getBindingForClass(CreditCardProcessor)
		b.boundClass == CreditCardProcessor
		b.implClass == BasicCCProcessor
	}
}