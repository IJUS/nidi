package com.example.general

import net.ijus.nidi.Optional
import net.ijus.nidi.Require;


/**
 * Created by pfried on 7/5/14.
 */

public class UnannotatedConstructors {

	String one
	String two

	UnannotatedConstructors(@Require("requiredString") String one) {
		this(one, "default")
	}

	UnannotatedConstructors(@Require("requiredString") String one, @Optional("optionalString") String two) {
		this.one = one
		this.two = two
	}
}