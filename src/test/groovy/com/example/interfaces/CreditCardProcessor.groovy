package com.example.interfaces

/**
 * Created by pfried on 6/16/14.
 */
public interface CreditCardProcessor extends PaymentProcessor {

	String process(String input)

}