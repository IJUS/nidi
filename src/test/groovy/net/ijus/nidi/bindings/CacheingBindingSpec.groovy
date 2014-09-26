package net.ijus.nidi.bindings

import com.example.impl.BasicCCProcessor
import com.example.interfaces.CreditCardProcessor
import net.ijus.nidi.instantiation.ConstructorInstanceGenerator
import spock.lang.Specification

/**
 * Created by pfried on 7/13/14.
 */

public class CacheingBindingSpec extends Specification {

	void "caching binding should always return the same instance of it's implementation"(){
		setup:
		ConstructorInstanceGenerator generator = GroovyMock(ConstructorInstanceGenerator)
		CachingBinding binding = new CachingBinding(generator, CreditCardProcessor, BasicCCProcessor, Scope.SINGLETON)

		when:
		def result1 = binding.getInstance()
		def result2 = binding.getInstance()

		then:
		1*generator.createNewInstance() >> new BasicCCProcessor()
		result1.is(result2)
	}
}