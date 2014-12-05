package net.ijus.nidi.builder

import com.example.interfaces.CreditCardProcessor
import net.ijus.nidi.bindings.NullBinding
import spock.lang.Specification

class BindingUtilsSpec extends Specification {

	void "preference should be given to the inner bindings when there is a duplicate boundClass"() {
		setup:
		Set<Binding> inner = [
				new NullBinding(CreditCardProcessor)
		] as Set

	}
}
