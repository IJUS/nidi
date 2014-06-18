package net.ijus.nidi

/**
 * Created by pfried on 6/16/14.
 */
public interface Binding {

	Class getBoundClass()
	Class getImplementationClass()
	def getInstance()
	Context getParentContext()
	void setParentContext(Context ctx)
	Binding setupInstance(Closure closure)

	Scope getScope()
}