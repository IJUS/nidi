package net.ijus.nidi

import com.example.impl.BasicCCProcessor
import com.example.interfaces.CreditCardProcessor;
import spock.lang.Specification

/**
 * Created by pfried on 6/17/14.
 */

public class BasicBindingSpec extends Specification {

	def cleanup(){
		TestUtils.clearContextHolder()
	}

	void "Bindings should create new instances of classes"() {
		setup:
		Context ctx = Mock()
		BasicBinding b = new BasicBinding(CreditCardProcessor, BasicCCProcessor, ctx)

		when:
		def instance = b.getInstance()

		then:
		notThrown(InvalidConfigurationException)
		instance instanceof BasicCCProcessor

	}

	void "Bindings should call the setup closure if present"(){
		setup: "create a basic binding"
		BasicBinding binding = new BasicBinding(CreditCardProcessor, BasicCCProcessor, null)
		binding.setupClosure = {BasicCCProcessor instance->
			instance.someProperty = "newValue"
		}

		when: "create a new instance"
		def instance = binding.getInstance()

		then: "the instance should have the correct property set"
		instance.someProperty == "newValue"

	}

}