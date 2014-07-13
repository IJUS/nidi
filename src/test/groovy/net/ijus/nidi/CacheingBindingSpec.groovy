package net.ijus.nidi

import com.example.impl.BasicCCProcessor
import com.example.interfaces.CreditCardProcessor
import spock.lang.Specification

import static spock.lang.MockingApi.GroovyMock

/**
 * Created by pfried on 7/13/14.
 */

public class CacheingBindingSpec extends Specification {

	void "caching binding should always return the same instance of it's implementation"(){
		setup:
		InstanceGenerator generator = GroovyMock(InstanceGenerator)
		CacheingBinding binding = new CacheingBinding(generator, CreditCardProcessor, Scope.SINGLETON)

		when:
		def result1 = binding.getInstance()
		def result2 = binding.getInstance()

		then:
		1*generator.createNewInstance() >> new BasicCCProcessor()
		result1.is(result2)
	}
}