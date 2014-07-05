package net.ijus.nidi

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.lang.annotation.Annotation
import java.lang.reflect.Constructor

/**
 * Created by pfried on 7/2/14.
 */

class BindingBuilder {
	static final Logger log = LoggerFactory.getLogger(BindingBuilder)

	Class from
	Class impl
	Scope scope

	Set<Binding> innerBindings = [] as Set



	Binding binding

	BindingBuilder(Class clazz) {
		this.from = clazz
	}

	BindingBuilder to(Class clazz, Closure config = null) {
		this.impl = clazz

		if (config) {
			config.setDelegate(this)
			config.call(this)
		}
		return this
	}


	BindingBuilder withScope(Scope s) {
		this.scope = s
		return this
	}

	void inheritScope(Scope s) {
		if (!this.scope) {
			this.scope = s
		}
	}

	Binding[] resolveConstructorParams(Constructor constructor, ContextBuilder ctxBuilder, BindingBuilder parentBindingBuilder = null) {
		Class[] constructorParams = constructor.getParameterTypes()
		Annotation[][] allAnnotations = constructor.getParameterAnnotations()

		if (constructorParams.length == 0) {
			return new Binding[0]
		}

		Binding[] paramBindings = new Binding[constructorParams.length]

		for (int paramIdx = 0; paramIdx < constructorParams.length; paramIdx++) {

			Class paramType = constructorParams[paramIdx]
			paramBindings[paramIdx] = buildBindingForClass(paramType, ctxBuilder)
		}

	}

	Binding buildBindingForClass(Class baseType, ContextBuilder ctxBuilder, BindingBuilder parentBindingBuilder = null) {
		log.trace("buildBindingForClass: baseType=${name(baseType)}, parentBuilderExists=${parentBindingBuilder != null}")

		Binding b
		if (parentBindingBuilder) {
			b = parentBindingBuilder.buildBindingForClass(baseType, ctxBuilder)
			log.debug("parentBindingBuilder returned binding: ${(b)? b.dump() : "null"}")

		}

		if (!b) {
			Context ctx = ctxBuilder.getContextRef()
			b = new ContextBindingReference(baseType, ctx)
			log.debug("bindingBuilder returning ContextBindingReference for ${name(baseType)}")
		}


	}



	Constructor resolveConstructor(Class clazz) {
		Constructor[] constructors = clazz.getConstructors()
		def constructor
		if (constructors.length == 1) {
			constructor = constructors[0]

		} else if (constructors.length > 1) {
			List<Constructor> withAnno = constructors.findAll{Constructor c-> c.isAnnotationPresent(Inject) }
			if (withAnno.size() != 1) {
				throw new InvalidConfigurationException("The Class: ${name(clazz)} has more than one constructor, so exactly one Constructor should have the @Inject annotation. Found ${withAnno.size()} Constructors with that annotation.")
			}
			constructor = withAnno.get(0)
		} else {
			throw new InvalidConfigurationException("The Class: ${name(clazz)} has no public constructors")
		}
		return constructor
	}

	String name(Class clazz) {
		String name
		if (clazz.isAnonymousClass()) {
			name = "Anonymous implementation of: ${clazz.getSuperclass().getCanonicalName()}"
		} else {
			name = clazz.getCanonicalName()
		}
		name
	}
}