package com.example.impl

import com.example.interfaces.LoggingService;
import groovy.transform.CompileStatic

/**
 * Created by pfried on 6/18/14.
 */

@CompileStatic
public class ConcreteClassNoInterface {

	int numProperty
	String stringProperty
	LoggingService loggingService

	ConcreteClassNoInterface(LoggingService loggingService) {
		this.loggingService = loggingService
	}

	String helloWorld(){
		return stringProperty?: "Hello World"
	}

}