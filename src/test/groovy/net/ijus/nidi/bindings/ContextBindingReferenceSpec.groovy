package net.ijus.nidi.bindings

import com.example.impl.ComplexCCProcessor
import com.example.interfaces.CreditCardProcessor
import com.example.interfaces.LoggingService
import com.example.interfaces.RefundProcessor
import net.ijus.nidi.Context
import net.ijus.nidi.instantiation.InstanceGenerator
import spock.lang.Specification

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
        binding.getScope() >> Scope.SINGLETON

        Context ctx = Mock()
        ctx.getBinding(CreditCardProcessor) >> binding

        ContextBindingReference bindingRef = new ContextBindingReference(CreditCardProcessor, ctx, RefundProcessor)

        when:
        Binding result = bindingRef.getResolvedBinding()

        then:
        result == binding

    }

    void "referencing a binding with scope.ONE_PER_BINDING should create a new CachingBinding"(){
        setup:
        Binding binding = Mock()
        binding.getBoundClass() >> CreditCardProcessor
        binding.getImplClass() >> ComplexCCProcessor
        binding.getScope() >> Scope.ONE_PER_BINDING
        binding.getInstanceGenerator() >> new InstanceGenerator() {
            @Override
            Object createNewInstance() {
                return "123"
            }
        }

        Context ctx = Mock()
        ctx.getBinding(CreditCardProcessor) >> binding

        ContextBindingReference bindingRef = new ContextBindingReference(CreditCardProcessor, ctx, RefundProcessor)

        when:
        Binding result = bindingRef.getResolvedBinding()

        then:
        result instanceof CachingBinding
        result.getInstance() == '123'
    }

}