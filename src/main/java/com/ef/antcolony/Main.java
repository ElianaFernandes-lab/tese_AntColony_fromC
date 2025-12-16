package main.java.com.ef.antcolony;

/**
 * Main.java
 * Translated from main.cpp
 * Original by Eliana Fernandes
 * Copyright (c) 2015 Eliana Fernandes. All rights reserved.
 */
import java.io.File;
import java.util.Scanner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import main.java.com.ef.antcolony.ReadData.Data;
import main.java.com.ef.antcolony.constants.AcoVar;

public class Main {

	private static final Logger log = LoggerFactory.getLogger(Main.class);

	/**
	 * Ensures the logs directory exists before logging starts.
	 * Logback's RollingFileAppender does not automatically create parent directories.
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

	public static void main(String[] args) {
		// Ensure logs directory exists before any logging occurs
		ensureLogsDirectory();
		
		String fileIn;
		String fileOut = "out.txt";

		// For multiple repetitions if REP is enabled
		int nrRep = AcoVar.REP ? AcoVar.NR_REP : 1;

		// List of instances to run (from the repeated blocks in original)
		String[] instances = {
				"AP10TT_1_1.dat"
//				, "AP10TT_2_1.dat", "AP10TT_2_2.dat", "AP10TT_3_1.dat", "AP10TT_3_2.dat", "AP10TT_3_3.dat",
//				"AP10TL_1_1.dat", "AP10TL_2_1.dat", "AP10TL_2_2.dat", "AP10TL_3_1.dat", "AP10TL_3_2.dat", "AP10TL_3_3.dat",
//				"AP10LL_1_1.dat", "AP10LL_2_1.dat", "AP10LL_2_2.dat", "AP10LL_3_1.dat", "AP10LL_3_2.dat", "AP10LL_3_3.dat",
//				"AP10LT_1_1.dat", "AP10LT_2_1.dat", "AP10LT_2_2.dat", "AP10LT_3_1.dat", "AP10LT_3_2.dat", "AP10LT_3_3.dat"
				// Add more from original if needed, e.g., AP20* instances
		};

		for (String instance : instances) {
			fileIn = instance;

			for (int m = 0; m < nrRep; m++) {
				long t_readdata1 = 0, t_readdata2 = 0;
				if (AcoVar.TIME) {
					t_readdata1 = System.nanoTime();
				}

				// Read data from file
				Data dat = ReadData.readData(fileIn);

				if (AcoVar.TIME) {
					t_readdata2 = System.nanoTime();
					double time_readdata = (t_readdata2 - t_readdata1) / 1_000_000_000.0;
					log.info("TIME readData: " + time_readdata);
				}

				long t_runaco1 = 0, t_runaco2 = 0;
				if (AcoVar.TIME) {
					t_runaco1 = System.nanoTime();
				}

				// Run Ant Colony Optimization
				RunAco.runAco(dat, fileIn, fileOut);

				if (AcoVar.TIME) {
					t_runaco2 = System.nanoTime();
					double time_runaco = (t_runaco2 - t_runaco1) / 1_000_000_000.0;
					log.info("TIME runAco: " + time_runaco);
				}
			}
		}

		log.info("Press Enter to continue...");
		new Scanner(System.in).nextLine();
	}
}
