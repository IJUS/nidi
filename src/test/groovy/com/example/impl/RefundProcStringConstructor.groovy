package com.example.impl

import com.example.interfaces.LoggingService
import com.example.interfaces.RefundProcessor
import com.example.misc.Transaction
import net.ijus.nidi.Require

/**
 * Created by pfried on 7/20/14.
 */

public class RefundProcStringConstructor implements RefundProcessor {

	String stringProperty
	LoggingService loggingService

	RefundProcStringConstructor(@Require('stringProperty')String stringProperty, LoggingService loggingService) {
		this.stringProperty = stringProperty
		this.loggingService = loggingService
	}

	@Override
	boolean processRefund(Transaction transaction) {
		return false
	}
}