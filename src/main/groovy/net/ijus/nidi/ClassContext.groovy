package net.ijus.nidi;

import groovy.transform.CompileStatic
import groovy.transform.InheritConstructors

/**
 * Created by pfried on 6/16/14.
 */
@InheritConstructors
@CompileStatic
public class ClassContext extends Context implements Binding {

	Class boundClass
	Class implementationClass

	Context parentContext

	Closure setupClosure

	Scope scope

	@Override
	String getName() {
		boundClass.getSimpleName() + getClass().getSimpleName()
	}

	@Override
	def getInstance() {
		println "Get Instance on ${getClass().getSimpleName()}"
		null
	}

	@Override
	Binding setupInstance(Closure closure) {
		this.setupClosure = closure
		return this
	}
}