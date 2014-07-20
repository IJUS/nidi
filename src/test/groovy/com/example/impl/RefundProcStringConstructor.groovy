package com.example.impl

import com.example.interfaces.LoggingService
import com.example.interfaces.RefundProcessor
import com.example.misc.Transaction;
import groovy.util.logging.Log4j
import groovy.transform.CompileStatic
import net.ijus.nidi.RequiredBinding

/**
 * Created by pfried on 7/20/14.
 */

public class RefundProcStringConstructor implements RefundProcessor {

	String stringProperty
	LoggingService loggingService

	RefundProcStringConstructor(@RequiredBinding('stringProperty')String stringProperty, LoggingService loggingService) {
		this.stringProperty = stringProperty
		this.loggingService = loggingService
	}

	@Override
	boolean processRefund(Transaction transaction) {
		return false
	}
}