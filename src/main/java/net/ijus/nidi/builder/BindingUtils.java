package net.ijus.nidi.builder;

import net.ijus.nidi.bindings.Binding;

import java.util.HashSet;
import java.util.Set;

/**
 * Utility methods for dealing with bindings
 */
public class BindingUtils {

	public static Set<Binding> mergeBindingSets(Set<Binding> ctxBindings, Set<Binding> innerBindings) {
		Set<Class> classes = new HashSet<>();
		Set<Binding> merged = new HashSet<>();

		addBindingsToSet(merged, innerBindings, classes);
		addBindingsToSet(merged, ctxBindings, classes);
		return merged;
	}

	private static void addBindingsToSet(Set<Binding> collector, Set<Binding> toAdd, Set<Class> alreadyAdded) {
		for (Binding b : toAdd) {
			if (alreadyAdded.add(b.getBoundClass())) {
				//only add the binding if the class hasn't already been added to the set
				collector.add(b);
			}
		}
	}
}
