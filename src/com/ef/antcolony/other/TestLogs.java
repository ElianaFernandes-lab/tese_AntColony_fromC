package com.ef.antcolony.other;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestLogs {
    private static final Logger logger = LoggerFactory.getLogger(TestLogs.class);
    
    public static void someMethod() {
        logger.debug("Debug message");
        logger.info("Info message");
        logger.warn("Warning message");
        logger.error("Error message");
    }
    
    public static void main(String[] args) {
    	TestLogs.someMethod();
    }
}