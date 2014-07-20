package com.example.config

import com.example.impl.ComplexCCProcessor
import com.example.impl.FraudDetectorImpl
import com.example.impl.LoggingServiceImpl
import com.example.interfaces.CreditCardProcessor
import com.example.interfaces.FraudDetectionService
import com.example.interfaces.LoggingService
import com.example.interfaces.RefundProcessor
import net.ijus.nidi.bindings.Scope
import net.ijus.nidi.builder.ContextBuilder
import net.ijus.nidi.ContextConfig
import org.slf4j.Logger
import org.slf4j.LoggerFactory;


/**
 * Created by pfried on 6/17/14.
 */

public class ExampleConfigScript implements ContextConfig {
	static final Logger log = LoggerFactory.getLogger(ExampleConfigScript)

	@Override
	void configure(ContextBuilder ctx) {
		//Binds the CreditCardProcessor (interface) to the ComplexCCProcessor (implementation)
		ctx.bind(CreditCardProcessor).to(ComplexCCProcessor).withScope(Scope.SINGLETON)

		//tells it to use the binding for CreditCardProcessor also as the RefundProcessor (will check to make sure the impl implements both
		ctx.bind(RefundProcessor).reference(CreditCardProcessor)
		//Binds the interface to the implementation and additionally sets up an instance property that isn't specified in the constructor
		ctx.bind(FraudDetectionService).to(FraudDetectorImpl).setupInstance {FraudDetectorImpl instance->
			instance.setWhoYaGonnCall("911")
		}

		ctx.bind(LoggingService).to(LoggingServiceImpl)

	}
}