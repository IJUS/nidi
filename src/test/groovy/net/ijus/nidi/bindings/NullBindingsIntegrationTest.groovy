package net.ijus.nidi.bindings

import com.example.general.WithOptionalClass
import com.example.impl.ComplexFraudDetector
import com.example.interfaces.CreditCardProcessor
import com.example.interfaces.FraudDetectionService
import com.example.interfaces.LoggingService
import net.ijus.nidi.Context
import net.ijus.nidi.InvalidConfigurationException
import net.ijus.nidi.builder.ContextBuilder
import spock.lang.Specification

/**
 * Created by pfried on 9/14/14.
 */
class NullBindingsIntegrationTest extends Specification {

    ContextBuilder ctxBuilder = new ContextBuilder()

    void "missing bindings should throw an exception if the field is not marked as optional"(){
        setup:
        ctxBuilder.bind(FraudDetectionService).to(ComplexFraudDetector)
        ctxBuilder.bindProperty("fraudDetURL", "www.svcUrl.com")

        when:
        ctxBuilder.build()

        then:
        def ex = thrown(InvalidConfigurationException)
        ex.message.contains("LoggingService")
    }

    void "nested null bindings should throw an exception if the field is not marked as Optional"(){
        setup:
        ctxBuilder.bind(FraudDetectionService).to(ComplexFraudDetector)
        ctxBuilder.bindProperty("fraudDetURL", String, null)
        ctxBuilder.bind(LoggingService).toNull()

        when:
        ctxBuilder.build()

        then:
        def ex = thrown(InvalidConfigurationException)
        ex.message.contains("LoggingService")
    }

    void "null bindings should resolve properly when they are allowed"(){
        setup:
        ContextBuilder ctxBuilder = new ContextBuilder()
        ctxBuilder.bind(CreditCardProcessor).to(WithOptionalClass)
        ctxBuilder.bind(LoggingService).toNull()

        when:
        Context ctx = ctxBuilder.build()

        then:
        CreditCardProcessor svc = ctx.getInstance(CreditCardProcessor)
        svc instanceof WithOptionalClass
        ((WithOptionalClass)svc).loggingService == null

    }
}
