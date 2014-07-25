package com.example.general;


/**
 * Created by pfried on 7/5/14.
 */

public class UnannotatedConstructors {

	String one
	String two

	UnannotatedConstructors(String one) {
		this(one, "default")
	}

	UnannotatedConstructors(String one, String two) {
		this.one = one
		this.two = two
	}
}