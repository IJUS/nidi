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

			bind(LoggingService).to(NamespacedLoggingService){
				scope = Scope.ONE_PER_BINDING //CCProc/RefProc should always get one instance, and FraudDet should get a different one
				bindConstructorParam('stringProperty').toValue({ 'custom namespace' } as InstanceGenerator)
			}

			bind(FraudDetectionService).to(ComplexFraudDetector){
				bindConstructorParam('fraudDetURL').toValue({ 'www.test.com' } as InstanceGenerator)
				setupInstance({
					it.whoYaGonnCall = "Ghostbusters"
				} as InstanceSetupFunction)
			}

		}

	}
}