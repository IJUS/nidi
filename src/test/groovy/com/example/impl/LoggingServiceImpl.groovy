package com.example.impl

import com.example.interfaces.LoggingService
import com.example.misc.CreditCardTransaction;
import groovy.transform.CompileStatic

/**
 * Created by pfried on 6/17/14.
 */

@CompileStatic
public class LoggingServiceImpl implements LoggingService {

	String logFilePath

	@Override
	void logTransaction(CreditCardTransaction transaction) {
		println "Logging transaction"
	}
}