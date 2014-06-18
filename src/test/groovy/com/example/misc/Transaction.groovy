package com.example.misc;

import groovy.transform.CompileStatic

/**
 * Created by pfried on 6/17/14.
 */

@CompileStatic
public class Transaction {

	String type = "generic"

	Number amount
	String customer
	Date date
}