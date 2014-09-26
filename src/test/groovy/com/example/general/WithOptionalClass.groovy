package com.example.general

import com.example.interfaces.CreditCardProcessor
import com.example.interfaces.LoggingService
import com.example.misc.Transaction
import net.ijus.nidi.Optional

/**
 * Shows how to use the @Optional annotation on a normal Constructor Parameter.
 * In this case, the LoggingService can be null, but should be passed in correctly if it is bound.
 */
class WithOptionalClass implements CreditCardProcessor {

    LoggingService loggingService;

    WithOptionalClass(@Optional LoggingService loggingService) {
        this.loggingService = loggingService
    }

    @Override
    String process(String input) {
        return null
    }

    @Override
    boolean process(Transaction transaction) {
        return false
    }
}
