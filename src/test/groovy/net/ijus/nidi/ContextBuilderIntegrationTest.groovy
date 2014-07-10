package net.ijus.nidi

import com.example.impl.LoggingServiceImpl
import com.example.interfaces.LoggingService
import spock.lang.Specification

/**
 * Created by pfried on 7/6/14.
 */

public class ContextBuilderIntegrationTest extends Specification {

	void "Contexts should be created with bindings of the correct scope"() {
		setup:
		ContextBuilder builder = new ContextBuilder()
		builder.bind(LoggingService).to(LoggingServiceImpl).withScope(Scope.SINGLETON)

		when:
		Context ctx = builder.build()
		Binding b = ctx.getBindingForClass(LoggingService)

		then:


		when:
		def serv1 = ctx.getInstance(LoggingService)
		def serv2 = ctx.getInstance(LoggingService)

		then:
		serv1 instanceof LoggingService
		serv1.is(serv2)

	}


}