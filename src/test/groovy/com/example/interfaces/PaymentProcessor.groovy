package com.example.interfaces

import com.example.misc.Transaction

/**
 * Created by pfried on 6/17/14.
 */
public interface PaymentProcessor {

	boolean process(Transaction transaction)

}