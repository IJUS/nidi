package com.example.interfaces

import com.example.misc.CreditCardTransaction

/**
 * Created by pfried on 6/17/14.
 */
public interface FraudDetectionService {

	boolean detectFraud(CreditCardTransaction)

	void bookemDanno()

}