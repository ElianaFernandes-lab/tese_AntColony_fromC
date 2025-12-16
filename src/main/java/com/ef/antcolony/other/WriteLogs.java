package main.java.com.ef.antcolony.other;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WriteLogs {
    private static final Logger logger = LoggerFactory.getLogger(WriteLogs.class);
    
    /**
     * Ensures the logs directory exists before logging starts.
     * Log4j2's RollingFileAppender does not automatically create parent directories.
     */
    private static void ensureLogsDirectory() {
        File logsDir = new File("logs");
        if (!logsDir.exists()) {
            boolean created = logsDir.mkdirs();
            if (created) {
                System.out.println("Created logs directory: " + logsDir.getAbsolutePath());
            } else {
                System.err.println("Warning: Failed to create logs directory: " + logsDir.getAbsolutePath());
            }
        }
    }
    
    public static void someMethod() {
        logger.debug("Debug message");
        logger.info("Info message");
        logger.warn("Warning message");
        logger.error("Error message");
    }
    
    public static void main(String[] args) {
        // Ensure logs directory exists before any logging occurs
        ensureLogsDirectory();
    	WriteLogs.someMethod();
    }
}