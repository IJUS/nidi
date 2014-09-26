package net.ijus.nidi.builder

import com.example.general.WithOptionalClass
import com.example.interfaces.CreditCardProcessor
import com.example.interfaces.LoggingService
import net.ijus.nidi.bindings.NullBinding
import net.ijus.nidi.bindings.Scope
import net.ijus.nidi.instantiation.NullGenerator
import spock.lang.Specification
import net.ijus.nidi.bindings.Binding

import static spock.lang.MockingApi.Mock
/**
 * Created by pfried on 9/14/14.
 */
class BindingBuilderIntegrationTest extends Specification {

    void "builder should create a NullBinding when the Optional annotation is present and no binding is present"(){
        setup:
        ContextBuilder ctxBuilder = Mock()
        ctxBuilder.containsBindingFor(LoggingService) >> false
        BindingBuilder<CreditCardProcessor> bb = new BindingBuilder<>(CreditCardProcessor, ctxBuilder).withScope(Scope.SINGLETON)

        when:
        bb.bindTo(WithOptionalClass)
        Binding result = bb.build()

        then:
        result.getImplClass() == WithOptionalClass
        WithOptionalClass instance = (WithOptionalClass) result.getInstance()
        instance.loggingService == null
    }

}
