package com.example.impl

import com.example.interfaces.CreditCardProcessor
import com.example.interfaces.RefundProcessor
import com.example.misc.Transaction;


/**
 * Created by pfried on 6/16/14.
 */


public class BasicCCProcessor implements CreditCardProcessor, RefundProcessor {

	String someProperty = "defaultProperty"

	@Override
	String process(String input) {
		getClass().getSimpleName()
	}

	@Override
	boolean processRefund(Transaction transaction) {
		return false
	}

	@Override
	boolean process(Transaction transaction) {
		return false
	}
}