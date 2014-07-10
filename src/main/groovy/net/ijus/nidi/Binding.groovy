package net.ijus.nidi

/**
 * Created by pfried on 6/16/14.
 */
public interface Binding {

	def getInstance()

	Class getImplClass()

	Class getBoundClass()

	Scope getScope()

	void validate()
}