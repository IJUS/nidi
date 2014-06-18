package com.example.impl

import com.example.interfaces.FraudDetectionService;



/**
 * Created by pfried on 6/17/14.
 */


public class FraudDetectorImpl implements FraudDetectionService {

	String whoYaGonnCall = "Ghost Busters!"

	@Override
	boolean detectFraud(Object CreditCardTransaction) {
		return true
	}

	@Override
	void bookemDanno() {
		println "Just the facts"
	}
}