package net.ijus.nidi

import net.ijus.nidi.bindings.Binding
import com.example.config.ComplexConfigScript
import com.example.impl.ComplexCCProcessor
import com.example.interfaces.CreditCardProcessor
import spock.lang.Specification

class ClassCastIntegrationSpec extends Specification {
    def setupSpec() {
        Configuration.mainContextFromClass = ComplexConfigScript
    }

    def cleanupSpec() {
        ContextHolder.context = null
    }

    def "get instance"() {
        setup:
        Context context = ContextHolder.context

        when:
        CreditCardProcessor processor = context.getInstance(CreditCardProcessor)

        then:
        processor instanceof ComplexCCProcessor
    }

    def "get binding"() {
        setup:
        Context context = ContextHolder.context

        when:
        Binding<CreditCardProcessor> processorBinding = context.getBinding(CreditCardProcessor)

        then:
        processorBinding.instance instanceof ComplexCCProcessor
    }

    def "get binding equal to get instance"() {
        setup:
        Context context = ContextHolder.context

        when:
        Binding<CreditCardProcessor> processorBinding = context.getBinding(CreditCardProcessor)
        CreditCardProcessor processor = context.getInstance(CreditCardProcessor)

        then:
        processorBinding.instance == processor
    }
}