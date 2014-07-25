package com.example.general

import net.ijus.nidi.Inject;


/**
 * Created by pfried on 7/5/14.
 */

public class CorrectConstAnnotation {

	String one
	String two

	CorrectConstAnnotation(String one) {
		this(one, "default")
	}

	@Inject
	CorrectConstAnnotation(String one, String two) {
		this.one = one
		this.two = two
	}
}