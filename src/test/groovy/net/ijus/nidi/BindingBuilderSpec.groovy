package net.ijus.nidi

import com.example.general.CorrectConstAnnotation
import com.example.general.MultipleAnnotatedConstructors
import com.example.general.UnannotatedConstructors
import com.example.impl.BasicCCProcessor
import com.example.impl.ComplexCCProcessor
import com.example.interfaces.CreditCardProcessor
import com.example.interfaces.FraudDetectionService
import com.example.interfaces.LoggingService;
import spock.lang.Specification

import java.lang.reflect.Constructor

/**
 * Created by pfried on 7/5/14.
 */


public class BindingBuilderSpec extends Specification {

	void "setting the scope should work in a variety of ways"(){
		setup:
		BindingBuilder builder = new BindingBuilder(CreditCardProcessor, null)

		when:
		builder.to(BasicCCProcessor){
			scope = Scope.ONE_PER_BINDING
		}

		then:
		builder.scope == Scope.ONE_PER_BINDING

		when:
		builder.withScope(Scope.SINGLETON)

		then:
		builder.scope == Scope.SINGLETON

	}

	void "Building a binding with a 0-arg constructor should return a basic binding"(){
		setup:
		ContextBuilder ctxBuilder = Mock()
		BindingBuilder builder = new BindingBuilder(CreditCardProcessor, null)
		builder.to(BasicCCProcessor)

		when:
		Binding result = builder.build()

		then:
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
		Binding[] result = basicTestBuilder.resolveConstructorParams(constructor, ctxBuilder)

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
		Binding[] paramBindings = basicTestBuilder.resolveConstructorParams(constructor, ctxBuilder)

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