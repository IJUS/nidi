package net.ijus.nidi.bindings

import com.example.impl.ComplexCCProcessor
import com.example.interfaces.CreditCardProcessor
import com.example.interfaces.LoggingService
import com.example.interfaces.RefundProcessor
import net.ijus.nidi.Context
import spock.lang.Specification

import static spock.lang.MockingApi.*

/**
 * Created by pfried on 7/18/14.
 */

public class ContextBindingReferenceSpec extends Specification {

	void "ContextBindingReference should return the correct class for getBoundClass"() {
		setup:
		Context ctx = GroovyMock()

		when:
		ContextBindingReference b1 = new ContextBindingReference(LoggingService, ctx, LoggingService)

		then:
		b1.getBoundClass() == LoggingService

		when:
		def b2 = new ContextBindingReference(CreditCardProcessor, ctx, RefundProcessor)

		then:
		b2.getBoundClass() == RefundProcessor

	}

    void "referenced bindings should be resolved correctly"() {
        setup: "mock a binding and context"
        Binding binding = Mock()
        binding.getBoundClass() >> CreditCardProcessor
        binding.getImplClass() >> ComplexCCProcessor

        Context ctx = Mock()
        ctx.getBinding(CreditCardProcessor) >> binding

        ContextBindingReference bindingRef = new ContextBindingReference(CreditCardProcessor, ctx, RefundProcessor)

        when:
        Binding result = bindingRef.getResolvedBinding()

        then:
        result == binding

    }

}