package net.ijus.nidi.builder

import com.example.config.ComplexConfigScript
import com.example.impl.*
import com.example.interfaces.CreditCardProcessor
import com.example.interfaces.FraudDetectionService
import com.example.interfaces.LoggingService
import com.example.interfaces.RefundProcessor
import net.ijus.nidi.Configuration
import net.ijus.nidi.Context
import net.ijus.nidi.ContextConfig
import net.ijus.nidi.InvalidConfigurationException
import net.ijus.nidi.bindings.Scope
import net.ijus.nidi.instantiation.InstanceGenerator
import net.ijus.nidi.instantiation.InstanceSetupFunction
import spock.lang.Specification

/**
 * Created by pfried on 7/6/14.
 */

public class ContextBuilderIntegrationTest extends Specification {

	void "test building a complex nested context"(){
		when:
		Context ctx = Configuration.configureNew(ComplexConfigScript)

		then:
		def ccProc = ctx.getInstance(CreditCardProcessor)
		ccProc instanceof ComplexCCProcessor

		def refProc = ctx.getInstance(RefundProcessor)
		refProc.is(ccProc)

		def ccProc2 = ctx.getInstance(CreditCardProcessor)
		ccProc2.is(ccProc)

		ccProc.loggingService instanceof NamespacedLoggingService
		ccProc.loggingService.stringProperty == 'custom namespace'

		def fraudDet = ccProc.fraudDetectionService
		fraudDet.loggingService instanceof NamespacedLoggingService
		!fraudDet.loggingService.is(ccProc.loggingService) //they are separate instances because of the ONE_PER_BINDING scope
	}

	void "bound Properties declared in the context should found and used in instance generation"() {
		setup:
		ContextBuilder builder = new ContextBuilder()

		when:
		builder.with{
			bindProperty('stringProperty', "customString")
			bind(LoggingService).to(NamespacedLoggingService)
		}
		def ctx = builder.build()

		then:
		def instance = ctx.getInstance(LoggingService)
		instance instanceof NamespacedLoggingService
		instance.stringProperty == 'customString'
	}

	void "properties should be bound to string keys"(){
		setup:
		ContextBuilder builder = new ContextBuilder()

		when:
		builder.bindProperty("myProperty", "myValue")
		def ctx = builder.build()

		then:
		ctx.getInstance("myProperty") == "myValue"

	}

	void "calling register with a class should create a binding from that class to itself"(){
		setup:
		ContextBuilder builder = new ContextBuilder()

		when:
		builder.bind(LoggingService).to(LoggingServiceImpl)
		builder.register(ConcreteClassNoInterface).setupInstance({
			it.stringProperty = "custom value"
		} as InstanceSetupFunction)
		Context ctx = builder.build()

		then:
		def impl = ctx.getInstance(ConcreteClassNoInterface)
		impl instanceof ConcreteClassNoInterface
		impl.stringProperty == "custom value"

	}

	void "Bindings should allow for specifying constructor params"(){
		setup:
		Context ctx = Configuration.configureNew({
			it.bind(LoggingService).to(NamespacedLoggingService).bindConstructorParam('stringProperty').toValue({ 'testString' } as InstanceGenerator)
		} as ContextConfig)

		expect:
		def instance = ctx.getInstance(LoggingService)
		instance instanceof NamespacedLoggingService
		instance.stringProperty == 'testString'
	}

	void "Bindings should inherit scope from the context builder if no override is present"(){
		setup:
		ContextBuilder builder = new ContextBuilder()
		builder.bind(LoggingService).to(LoggingServiceImpl)
		builder.setDefaultScope(Scope.ONE_PER_BINDING)

		when:
		Context ctx = builder.build()

		then:
		def binding = ctx.getBinding(LoggingService)
		binding.getScope() == Scope.ONE_PER_BINDING
	}

	void "Bindings should be created with the correct scope when one is specified"() {
		setup:
		ContextBuilder builder = new ContextBuilder()
		builder.bind(LoggingService).to(LoggingServiceImpl).withScope(Scope.SINGLETON)

		when:
		Context ctx = builder.build()
		net.ijus.nidi.bindings.Binding b = ctx.getBinding(LoggingService)

		then:
		b.getScope() == Scope.SINGLETON

		when:
		def serv1 = ctx.getInstance(LoggingService)
		def serv2 = ctx.getInstance(LoggingService)

		then:
		serv1 instanceof LoggingService
		serv1.is(serv2)

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
		net.ijus.nidi.bindings.Binding b = ctx.getBinding(CreditCardProcessor)
		b.boundClass == CreditCardProcessor
		b.implClass == BasicCCProcessor
	}

}