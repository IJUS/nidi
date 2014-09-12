package net.ijus.nidi.bindings

import com.example.impl.BasicCCProcessor
import com.example.interfaces.CreditCardProcessor
import com.example.interfaces.RefundProcessor
import net.ijus.nidi.Configuration
import net.ijus.nidi.Context
import net.ijus.nidi.ContextTestUtils
import net.ijus.nidi.bindings.Scope
import spock.lang.*

/**
 * Created by pfried on 6/17/14.
 */
class ScopingSpec extends Specification {

	def setup(){
		ContextTestUtils.clearContextHolder()
	}

	def cleanup(){
		ContextTestUtils.clearContextHolder()
	}


	void "BindingFactory should inherit default scopes properly"(){
		setup:
		Context ctx = Configuration.configureNew{
			defaultScope = currentScope
			bind(CreditCardProcessor).to(BasicCCProcessor)
		}

		when:
		Scope result = ctx.bindingsMap.get(CreditCardProcessor).getScope()

		then:
		result == currentScope

		where:
		currentScope << Scope.values()

	}


	void "setting scope to always create new should yield a new instance every time"() {
		setup:
		Context ctx = Configuration.configureNew {
			bind(CreditCardProcessor).withScope(Scope.ALWAYS_CREATE_NEW).to(BasicCCProcessor)
		}

		when: "get a couple of instances"
		def inst1 = ctx.getInstance(CreditCardProcessor)
		def inst2 = ctx.getInstance(CreditCardProcessor)

		then: "they should be separate instances"
		!inst1.is(inst2)
		inst1 instanceof BasicCCProcessor
		inst2 instanceof BasicCCProcessor
	}


	void "Binding scope should use one instance per binding"() {
		setup:
		Context ctx1 = Configuration.configureNew {

			bind(CreditCardProcessor).withScope(Scope.ONE_PER_BINDING).to(BasicCCProcessor)
			bind(RefundProcessor).withScope(Scope.ONE_PER_BINDING).to(BasicCCProcessor)
		}

		when: "get instances of both classes"
		def ccProc = ctx1.getInstance(CreditCardProcessor)
		def refProc = ctx1.getInstance(RefundProcessor)

		then: "they should be separate insances of the same class"
		!ccProc.is(refProc)
		ccProc instanceof BasicCCProcessor
		refProc instanceof BasicCCProcessor

		when: "get another instance of each"
		def ccProc2 = ctx1.getInstance(CreditCardProcessor)

		then: "it should be the same instance as the first"
		ccProc2.is(ccProc)
	}


	void "Context global scope should result in one instace per context"() {
		setup:
		Context ctx1 = Configuration.configureNew {
			bind(CreditCardProcessor).withScope(Scope.CONTEXT_GLOBAL).to(BasicCCProcessor)
		}

		Context ctx2 = Configuration.configureNew {
			bind(CreditCardProcessor).withScope(Scope.CONTEXT_GLOBAL).to(BasicCCProcessor)
		}

		when:
		def inst1 = ctx1.getInstance(CreditCardProcessor)
		def inst2 = ctx2.getInstance(CreditCardProcessor)

		then: "they should be two separate instances"
		!inst1.is(inst2)

		when: "get another instance from ctx1"
		def anotherInst1 = ctx1.getInstance(CreditCardProcessor)

		then: "it should be the same instance as the first"
		inst1.is(anotherInst1)
	}


	void "Singleton bindings should always return the same instance"() {
		setup:
		Context ctx = Configuration.configureNew {
			bind(CreditCardProcessor).withScope(Scope.SINGLETON).to(BasicCCProcessor).setupInstance { BasicCCProcessor proc ->
				proc.someProperty = "customValue"
			}
			bind(RefundProcessor).reference(CreditCardProcessor)
		}

		when: "get instances for both implementations"
		def ccProc = ctx.getInstance(CreditCardProcessor)
		def refundProc = ctx.getInstance(RefundProcessor)

		then: "the to instances should be the same"
		ccProc.is(refundProc)

		when: "get another instance of each"
		def ccProc2 = ctx.getInstance(CreditCardProcessor)
		def refProc2 = ctx.getInstance(RefundProcessor)

		then:
		ccProc.is(ccProc2)
		refundProc.is(refProc2)
		ccProc2.is(refProc2)

	}

}

