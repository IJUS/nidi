package net.ijus.nidi.builder;

import net.ijus.nidi.Inject;
import net.ijus.nidi.InvalidConfigurationException;
import net.ijus.nidi.bindings.Binding;

import java.lang.reflect.Constructor;
import java.util.HashSet;
import java.util.Set;


public class ConstructorResolution {

	public static <T, E extends T> Constructor<E> resolveConstructor(Class<E> clazz, Set<Binding> contextBindings, Set<Binding> innerBindings) throws InvalidConfigurationException {
		//merge the bindings into one set so we can pass them around more easily
		Set<Binding> allBindings = mergeBindings(contextBindings, innerBindings);

		//this is actually a safe cast
		Constructor<E>[] constructors = (Constructor<E>[]) clazz.getConstructors();
		Constructor<E> resolvedConstructor;

		if (constructors.length == 1) {
			resolvedConstructor = constructors[0];
		} else if (constructors.length > 1) {
			//first look for one that has the @Inject annotation
			// that's a developer's way of making this decision for us
			resolvedConstructor = findAnnotatedConstructor(constructors);

			if (resolvedConstructor == null) {
				//If there's nothing with an annotation, then we'll try to figure out
				//which constructor to use based on the available bindings
				resolvedConstructor = selectConstructor(constructors, contextBindings);
			}
		} else {
			throw new InvalidConfigurationException("The class: " + clazz.getName() + " has no public constructor");
		}

		validateConstructor(clazz, resolvedConstructor, contextBindings, innerBindings);
		return resolvedConstructor;
	}

	/**
	 * merges the two sets of bindings, preferring the innerBindings, if there's a binding that exists in both
	 *
	 * @param ctxBindings The set of bindings from the context builder
	 * @param innerBindings the set of bindings from the BindingBuilder
	 * @return both sets merged into one, with only one binding for each class
	 */
	static Set<Binding> mergeBindings(Set<Binding> ctxBindings, Set<Binding> innerBindings){
		Set<Binding> merged = new HashSet<>(innerBindings);
		merged.addAll(ctxBindings);
		return merged;
	}

	protected static <E> void validateConstructor(Class<E> clazz, Constructor<E> constructor, Set<Binding> contextBindings, Set<Binding> innerBindings) throws InvalidConfigurationException {
		if (constructor == null) {
			throw new InvalidConfigurationException("NiDI was unable to resolve a Constructor for class " + clazz.getName());
		}
	}

	protected static boolean canSatisfyConstructorArgs(Constructor constructor, Set<Class> classBindings){

	}

	protected static <E> Constructor<E> selectConstructor(Constructor<E>[] constructors, Set otherBindings) throws InvalidConfigurationException {


		Constructor<E> selected = null;
		for (Constructor candidate : constructors) {

		}

		return selected;
	}

	protected static <E> Constructor<E> findAnnotatedConstructor(Constructor<E>[] constructors) throws InvalidConfigurationException {
		Constructor<E> withAnno = null;
		for (Constructor<E> c : constructors) {
			if (c.getAnnotation(Inject.class) != null) {
				// We found an annotated constructor
				// check to make sure there's at most one constructor with the annotation
				if (withAnno != null) {
					throw new InvalidConfigurationException("The class: " + c.getDeclaringClass().getName() + " Has more than one constructor annotated with @Inject. This makes it impossible to decide which one to use");
				}

				withAnno = c;
			}
		}
		return withAnno;
	}


}
