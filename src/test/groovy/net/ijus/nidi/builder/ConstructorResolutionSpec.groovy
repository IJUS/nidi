package net.ijus.nidi.builder

import com.example.general.CorrectConstAnnotation
import com.example.general.MultipleAnnotatedConstructors
import com.example.impl.BasicCCProcessor
import com.example.interfaces.CreditCardProcessor
import net.ijus.nidi.InvalidConfigurationException
import spock.lang.Specification

import java.lang.reflect.Constructor
import static net.ijus.nidi.builder.ConstructorResolution.*

class ConstructorResolutionSpec extends Specification {

	void "constructor resolution should return the default 0-arg constructor if that's all the class has"(){
		setup:
		BindingBuilder<CreditCardProcessor> bindingBuilder = Mock(BindingBuilder)
		ContextBuilder ctxBuilder = Mock(ContextBuilder)

		when:
		Constructor<BasicCCProcessor> constructor = resolveConstructor(BasicCCProcessor, new HashSet(), new HashSet())

		then:
		constructor
		constructor.getParameterTypes().length == 0
	}

	void "validateConstructor should throw an exception if the constructor is null"(){
		when:
		validateConstructor(getClass(), null, null, null)

		then:
		InvalidConfigurationException ex = thrown(InvalidConfigurationException)
		ex.getMessage().startsWith("NiDI was unable to resolve a Constructor")
	}

	void "validateConstructor should not throw an exception for a non-null 0-arg constructor"(){
		when:
		validateConstructor(BasicCCProcessor, BasicCCProcessor.getConstructor(), null, null)

		then:
		notThrown(InvalidConfigurationException)
	}

	void "a constructor annotated with @Inject should be found"(){
		when:
		Constructor c = findAnnotatedConstructor(CorrectConstAnnotation.getConstructors())

		then:
		c != null
	}

	void "an exception should be throw when there are multiple constructors that bear the @Inject annotation"(){
		when:
		Constructor c = findAnnotatedConstructor(MultipleAnnotatedConstructors.getConstructors())

		then:
		InvalidConfigurationException ex = thrown(InvalidConfigurationException)
		ex.getMessage().contains("Has more than one constructor annotated with @Inject")
	}
}
