package com.example.impl

import com.example.interfaces.LoggingService
import com.example.misc.CreditCardTransaction
import net.ijus.nidi.Bound

/**
 * Created by pfried on 7/15/14.
 */

public class NamespacedLoggingService implements LoggingService {

	String stringProperty

	NamespacedLoggingService(@Bound('stringProperty')String stringProperty) {
		this.stringProperty = stringProperty
	}

	@Override
	void logTransaction(CreditCardTransaction transaction) {
		println "logging transaction"
	}
}