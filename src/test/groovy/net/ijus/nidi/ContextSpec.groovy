package net.ijus.nidi

import com.example.config.ExampleConfigScript
import com.example.impl.BasicCCProcessor
import com.example.impl.ComplexCCProcessor
import com.example.impl.ConcreteClassNoInterface
import com.example.impl.FraudDetectorImpl
import com.example.impl.LoggingServiceImpl
import com.example.interfaces.CreditCardProcessor
import com.example.interfaces.FraudDetectionService
import com.example.interfaces.LoggingService;
import spock.lang.Specification

import static net.ijus.nidi.Configuration.*

/**
 * Created by pfried on 6/17/14.
 */

public class ContextSpec extends Specification {

	def cleanup(){
		ContextTestUtils.clearContextHolder()
	}

	void "context should handle instantiating objects with nested dependencies"(){
		setup:
		Context ctx = configureNew({
			it.bind(CreditCardProcessor).to(ComplexCCProcessor)
			it.bind(FraudDetectionService).to(FraudDetectorImpl)
			it.bind(LoggingService).to(LoggingServiceImpl)
		} as ContextConfig)

		when: "get the complex instance"
		def instance = ctx.getInstance(CreditCardProcessor)

		then: "the nested dependencies should be correct"
		notThrown(InvalidConfigurationException)
		instance instanceof ComplexCCProcessor
		def proc = (ComplexCCProcessor) instance
		proc.fraudDetectionService instanceof FraudDetectorImpl
		proc.loggingService instanceof LoggingServiceImpl
	}

	void "context should get basic instance correctly"() {
		setup:
		Context ctx = configureNew({
			it.bind(CreditCardProcessor).to(BasicCCProcessor)
		} as ContextConfig)

		when:
		def instance = ctx.getInstance(CreditCardProcessor)

		then:
		notThrown(InvalidConfigurationException)
		instance instanceof BasicCCProcessor

	}
}