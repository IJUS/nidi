package net.ijus.nidi.builder

import com.example.general.CorrectConstAnnotation
import com.example.general.MultipleAnnotatedConstructors
import com.example.general.UnannotatedConstructors
import com.example.impl.BasicCCProcessor
import com.example.impl.ComplexCCProcessor
import com.example.impl.LoggingServiceImpl
import com.example.impl.NamespacedLoggingService
import com.example.interfaces.CreditCardProcessor
import com.example.interfaces.FraudDetectionService
import com.example.interfaces.LoggingService
import com.example.interfaces.RefundProcessor
import net.ijus.nidi.Context
import net.ijus.nidi.InvalidConfigurationException
import net.ijus.nidi.bindings.*
import spock.lang.Specification

import java.lang.reflect.Constructor

/**
 * Created by pfried on 7/5/14.
 */


public class BindingBuilderSpec extends Specification {

	void "binding to a reference to another binding should result in a context reference binding"(){
		setup:
		Context ctx = GroovyMock()
		Binding refBinding = Mock()
		refBinding.getBoundClass() >> CreditCardProcessor
		refBinding.getImplClass() >> BasicCCProcessor
		refBinding.getInstance() >> new BasicCCProcessor()
		ctx.getBinding(CreditCardProcessor) >> refBinding
		ContextBuilder ctxBuilder = GroovyMock()
		ctxBuilder.getContextRef() >> ctx
		ctxBuilder.containsBindingFor(CreditCardProcessor) >> true

		BindingBuilder builder = new BindingBuilder(RefundProcessor, ctxBuilder)

		when:
		builder.reference(CreditCardProcessor)
		Binding binding = builder.build()

		then:
		binding instanceof ContextBindingReference
		binding.getBoundClass() == RefundProcessor
		((ContextBindingReference)binding).getCtx() == ctx
		binding.getImplClass() == BasicCCProcessor
		binding.getInstance() instanceof BasicCCProcessor
	}

	void "validating class assignments should throw InvalidConfigurationException when there's a problem"(){
		when:
		BindingBuilder b1 = new BindingBuilder(LoggingService, null)
		b1.@impl = BasicCCProcessor
		b1.validateClassAssignment()

		then:
		thrown(InvalidConfigurationException)

		when:
		BindingBuilder b2 = new BindingBuilder(LoggingService, null)
		b2.@impl = LoggingServiceImpl
		b2.validateClassAssignment()

		then:
		notThrown(InvalidConfigurationException)

		when:
		BindingBuilder b3 = new BindingBuilder(LoggingService, null)
		b3.validateClassAssignment()

		then:
		thrown(InvalidConfigurationException)

		when: "set the impl to an abstract class"
		BindingBuilder b4 = new BindingBuilder(LoggingService, null)
		b4.@impl = LoggingService
		b4.validateClassAssignment()

		then:
		thrown(InvalidConfigurationException)
	}

	void "constructor params with the @Bound annotation should be correctly identified"(){
		setup:
		def builder = new BindingBuilder(LoggingService, null)
		Constructor constructor = NamespacedLoggingService.getConstructor(String)

		when:
		String[] result = builder.getBoundAnnotatedParams(constructor)

		then:
		result.length == 1
		result[0] == 'stringProperty'

	}

	void "binding to a value of an incompatible class should throw an exception"(){
		setup:
		def builder = new BindingBuilder(Map, null)

		when:
		builder.toValue {['listItemOne', 'two', 'three']}

		then:
		thrown(InvalidConfigurationException)
	}

	void "binding to a closure should create an instanceGenerator"(){
		setup:
		BindingBuilder builder = new BindingBuilder(String, null)

		when:
		builder.toValue { "testValue" }

		then:
		builder.instanceGenerator != null
		builder.instanceGenerator.createNewInstance() == "testValue"
	}

	void "setting the scope should work in a variety of ways"(){
		setup:
		BindingBuilder builder = new BindingBuilder(CreditCardProcessor, null)

		when:
		builder.withScope(Scope.SINGLETON)

		then:
		builder.scope == Scope.SINGLETON

	}

	void "Building a binding with the scope ALWAYS_CREATE_NEW should return a basic binding"(){
		setup:
		ContextBuilder ctxBuilder = Mock()
		BindingBuilder builder = new BindingBuilder(CreditCardProcessor, null)
		builder.scope = Scope.ALWAYS_CREATE_NEW
		builder.ctxBuilder = ctxBuilder
		builder.to(BasicCCProcessor)

		when:
		net.ijus.nidi.bindings.Binding result = builder.build()

		then:
		result instanceof BasicBinding
		result.getBoundClass() == CreditCardProcessor
		result.getImplClass() == BasicCCProcessor

	}


	void "Resolving constructor params for a 0-arg constructor should return a 0-length array"() {
		setup:
		BindingBuilder basicTestBuilder = new BindingBuilder(MultipleAnnotatedConstructors, null)
		ContextBuilder ctxBuilder = Mock()
		ctxBuilder.containsBindingFor(_ as Class) >> true
		ctxBuilder.getContextRef() >> Mock(Context)
		basicTestBuilder.ctxBuilder = ctxBuilder
		Constructor constructor = BasicCCProcessor.getConstructor()

		when:
		net.ijus.nidi.bindings.Binding[] result = basicTestBuilder.resolveConstructorParams(constructor)

		then:
		result.length == 0
	}

	void "Resolving constructor params should properly create ContextReferenceBindings for constructor params"() {
		setup:
		BindingBuilder basicTestBuilder = new BindingBuilder(MultipleAnnotatedConstructors, null)
		ContextBuilder ctxBuilder = Mock()
		ctxBuilder.containsBindingFor(_ as Class) >> true
		ctxBuilder.getContextRef() >> Mock(Context)
		basicTestBuilder.ctxBuilder = ctxBuilder
		Constructor constructor = ComplexCCProcessor.getConstructor(FraudDetectionService, LoggingService)

		when:
		net.ijus.nidi.bindings.Binding[] paramBindings = basicTestBuilder.resolveConstructorParams(constructor)

		then:
		paramBindings.length == 2
		paramBindings[0].getBoundClass() == FraudDetectionService
		paramBindings[1].getBoundClass() == LoggingService
	}

	void "Resolving constructor should throw an exception if multiple @Inject annotations are present"() {
		when:
		BindingBuilder basicTestBuilder = new BindingBuilder(MultipleAnnotatedConstructors, null)
		basicTestBuilder.resolveConstructor(MultipleAnnotatedConstructors)

		then:
		def exception = thrown(InvalidConfigurationException)
		exception.message == "The Class: com.example.general.MultipleAnnotatedConstructors has more than one constructor, so exactly one Constructor should have the @Inject annotation. Found 2 Constructors with that annotation."
	}

	void "Resolving constructor should throw an exception if multiple constructors are found but none have the @Inject annotation"() {
		setup:
		BindingBuilder basicTestBuilder = new BindingBuilder(MultipleAnnotatedConstructors, null)

		when:
		basicTestBuilder.resolveConstructor(UnannotatedConstructors)

		then:
		def ex = thrown(InvalidConfigurationException)
		ex.message == "The Class: com.example.general.UnannotatedConstructors has more than one constructor, so exactly one Constructor should have the @Inject annotation. Found 0 Constructors with that annotation."
	}

	void "Constructor should be resolved properly when a class has multiple constructors and one has the @Inject annotation"() {
		setup:
		BindingBuilder basicTestBuilder = new BindingBuilder(MultipleAnnotatedConstructors, null)

		when:
		def constructor = basicTestBuilder.resolveConstructor(CorrectConstAnnotation)

		then:
		notThrown(InvalidConfigurationException)
		constructor != null
		constructor.getParameterTypes().length == 2 //it's the two-arg constructor
	}


}