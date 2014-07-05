package net.ijus.nidi

import com.example.general.CorrectConstAnnotation
import com.example.general.MultipleAnnotatedConstructors
import com.example.general.UnannotatedConstructors;
import spock.lang.Specification

import java.lang.reflect.Constructor


/**
 * Created by pfried on 7/5/14.
 */


public class BindingBuilderSpec extends Specification {

	BindingBuilder builder = new BindingBuilder(MultipleAnnotatedConstructors)

	void "Resolving constructor should throw an exception if multiple @Inject annotations are present"() {
		when:
		builder.resolveConstructor(MultipleAnnotatedConstructors)

		then:
		def exception = thrown(InvalidConfigurationException)
		exception.message == "The Class: com.example.general.MultipleAnnotatedConstructors has more than one constructor, so exactly one Constructor should have the @Inject annotation. Found 2 Constructors with that annotation."
	}

	void "Resolving constructor should throw an exception if multiple constructors are found but none have the @Inject annotation"() {
		when:
		builder.resolveConstructor(UnannotatedConstructors)

		then:
		def ex = thrown(InvalidConfigurationException)
		ex.message == "The Class: com.example.general.UnannotatedConstructors has more than one constructor, so exactly one Constructor should have the @Inject annotation. Found 0 Constructors with that annotation."
	}

	void "Constructor should be resolved properly when a class has multiple constructors and one has the @Inject annotation"() {
		when:
		def constructor = builder.resolveConstructor(CorrectConstAnnotation)

		then:
		notThrown(InvalidConfigurationException)
		constructor != null
		constructor.getParameterTypes().length == 2 //it's the two-arg constructor
	}


}