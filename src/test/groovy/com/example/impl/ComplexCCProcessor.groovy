package com.example.impl

import com.example.interfaces.CreditCardProcessor
import com.example.interfaces.FraudDetectionService
import com.example.interfaces.LoggingService
import com.example.misc.Transaction;
import groovy.transform.CompileStatic

/**
 * Created by pfried on 6/17/14.
 */

@CompileStatic
public class ComplexCCProcessor implements CreditCardProcessor {
	FraudDetectionService fraudDetectionService
	LoggingService loggingService

	ComplexCCProcessor(FraudDetectionService fraudDetectionService, LoggingService loggingService) {
		this.fraudDetectionService = fraudDetectionService
		this.loggingService = loggingService
	}

	@Override
	String process(String input) {
		return "success!"
	}

	@Override
	boolean process(Transaction transaction) {
		return false
	}
}