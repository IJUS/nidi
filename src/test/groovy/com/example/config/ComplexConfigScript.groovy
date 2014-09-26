package com.example.config

import com.example.impl.ComplexCCProcessor
import com.example.impl.ComplexFraudDetector
import com.example.impl.FraudDetectorImpl
import com.example.impl.NamespacedLoggingService
import com.example.interfaces.CreditCardProcessor
import com.example.interfaces.FraudDetectionService
import com.example.interfaces.LoggingService
import com.example.interfaces.RefundProcessor;
import groovy.util.logging.Log4j
import groovy.transform.CompileStatic
import net.ijus.nidi.ContextConfig
import net.ijus.nidi.bindings.Scope
import net.ijus.nidi.builder.BindingBuilder
import net.ijus.nidi.builder.ContextBuilder
import net.ijus.nidi.instantiation.InstanceGenerator
import net.ijus.nidi.instantiation.InstanceSetupFunction

/**
 * Created by pfried on 7/19/14.
 */

public class ComplexConfigScript implements ContextConfig{

	@Override
	void configure(ContextBuilder builder) {
		builder.with{
			defaultScope = Scope.ALWAYS_CREATE_NEW

			bind(CreditCardProcessor).to(ComplexCCProcessor).withScope(Scope.SINGLETON)
			bind(RefundProcessor).reference(CreditCardProcessor)


			bind(LoggingService).to(NamespacedLoggingService)
                    .withScope(Scope.ONE_PER_BINDING)  //CCProc/RefProc should always get one instance, and FraudDet should get a different one
                    .bindConstructorParam('stringProperty').toValue({ 'custom namespace' } as InstanceGenerator)


			BindingBuilder fdsBuilder = bind(FraudDetectionService).to(ComplexFraudDetector)
                    .bindConstructorParam('fraudDetURL').toObject("www.test-url.com")
            //IMPORTANT: this sets up the instance of ComplexFraudDetector
            // if it were chained on the end of the previous line, then it would be called on the inner binding builder
            // that's created by the call to .bindConstructorParam
            fdsBuilder.setupInstance({ComplexFraudDetector cfd->
                        cfd.whoYaGonnCall = "Ghostbusters"
                    } as InstanceSetupFunction)

		}

	}
}