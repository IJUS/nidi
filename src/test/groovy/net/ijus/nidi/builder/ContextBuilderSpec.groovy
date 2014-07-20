package net.ijus.nidi.builder

import com.example.impl.BasicCCProcessor
import com.example.impl.ComplexCCProcessor
import com.example.impl.ConcreteClassNoInterface
import com.example.impl.FraudDetectorImpl
import com.example.impl.LoggingServiceImpl
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
		builder.register(ConcreteClassNoInterface).setupInstance {
			it.stringProperty = "custom value"
		}
		Context ctx = builder.build()

		then:
		def impl = ctx.getInstance(ConcreteClassNoInterface)
		impl instanceof ConcreteClassNoInterface
		impl.stringProperty == "custom value"

	}

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