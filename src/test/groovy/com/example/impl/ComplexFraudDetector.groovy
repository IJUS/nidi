package com.example.impl

import com.example.interfaces.LoggingService
import net.ijus.nidi.Require

/**
 * Created by pfried on 7/20/14.
 */

public class ComplexFraudDetector extends FraudDetectorImpl {

	LoggingService loggingService
	String serviceURL

	ComplexFraudDetector(LoggingService loggingService, @Require("fraudDetURL")String url) {
		this.loggingService = loggingService
		this.serviceURL = url
	}
}