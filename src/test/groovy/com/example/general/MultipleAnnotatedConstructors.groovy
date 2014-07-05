package com.example.general

import net.ijus.nidi.Inject;


/**
 * Created by pfried on 7/5/14.
 */


public class MultipleAnnotatedConstructors {

	String one
	String two

	@Inject
	MultipleAnnotatedConstructors(String one){
		this(one, null)
	}

	@Inject
	MultipleAnnotatedConstructors(String one, String two) {
		this.one = one
		this.two = two
	}
}