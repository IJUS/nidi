package net.ijus.nidi.builder

import com.example.impl.LoggingServiceImpl
import com.example.impl.NamespacedLoggingService
import com.example.interfaces.LoggingService
import net.ijus.nidi.Configuration
import net.ijus.nidi.Context
import net.ijus.nidi.bindings.Scope
import net.ijus.nidi.builder.ContextBuilder
import spock.lang.Specification

/**
 * Created by pfried on 7/6/14.
 */

public class ContextBuilderIntegrationTest extends Specification {

	void "Bindings should allow for specifying constructor params"(){
		setup:
		Context ctx = Configuration.configureNew{
			bind(LoggingService).to(NamespacedLoggingService){
				bindConstructorParam('stringProperty').toValue{ 'testString' }
			}
		}

		expect:
		def instance = ctx.getInstance(LoggingService)
		instance instanceof NamespacedLoggingService
		instance.stringProperty == 'testString'
	}

	void "Bindings should inherit scope from the context builder if no override is present"(){
		setup:
		ContextBuilder builder = new ContextBuilder()
		builder.bind(LoggingService).to(LoggingServiceImpl)
		builder.setDefaultScope(Scope.ONE_PER_BINDING)

		when:
		Context ctx = builder.build()

		then:
		def binding = ctx.getBindingForClass(LoggingService)
		binding.getScope() == Scope.ONE_PER_BINDING
	}

	void "Bindings should be created with the correct scope when one is specified"() {
		setup:
		ContextBuilder builder = new ContextBuilder()
		builder.bind(LoggingService).to(LoggingServiceImpl).withScope(Scope.CONTEXT_GLOBAL)

		when:
		Context ctx = builder.build()
		net.ijus.nidi.bindings.Binding b = ctx.getBindingForClass(LoggingService)

		then:
		b.getScope() == Scope.CONTEXT_GLOBAL

		when:
		def serv1 = ctx.getInstance(LoggingService)
		def serv2 = ctx.getInstance(LoggingService)

		then:
		serv1 instanceof LoggingService
		serv1.is(serv2)

	}


}