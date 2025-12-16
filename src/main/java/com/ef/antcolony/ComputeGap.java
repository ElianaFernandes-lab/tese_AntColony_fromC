package main.java.com.ef.antcolony;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

/**
 * ComputeGap.java
 * Translated from computegap.cpp + computegap.h
 * Original by Eliana Fernandes
 * Copyright (c) 2015 Eliana Fernandes. All rights reserved.
 */
public class ComputeGap {

	/**
	 * Represents the gap result between ACO solution and known best/optimal
	 */
	public static class Gap {
		public final String instanceName;
		public final double cost;
		public final double gap;
		public final double time;

		public Gap(String name, double cost, double gap, double time) {
			this.instanceName = name;
			this.cost = cost;
			this.gap = gap;
			this.time = time;
		}

		@Override
		public String toString() {
			return String.format("Instance: %s | Best known: %.6f | Gap: %.4f%% | Time: %.2f",
					instanceName, cost, gap, time);
		}
	}

	/**
	 * Computes the gap between your ACO solution and the best known value.
	 *
	 * @param bestKnownFile   Path to file containing best known solutions (format: instance_name cost time)
	 * @param instanceName    Name of the instance you solved (exact match required)
	 * @param acoCost         Objective value found by your ACO
	 * @return Gap object with results
	 */
	public static Gap computeGap(String bestKnownFile, String instanceName, double acoCost) {

		try (BufferedReader br = new BufferedReader(new FileReader(bestKnownFile))) {
			String line;
			while ((line = br.readLine()) != null) {
				line = line.trim();
				if (line.isEmpty() || line.startsWith("#")) {
					continue; // skip comments and empty lines
				}

				String[] parts = line.split("\\s+");
				if (parts.length < 3) {
					continue; // malformed line
				}

				String nameInFile = parts[0];
				if (!nameInFile.equals(instanceName)) {
					continue;
				}

				try {
					double bestCost = Double.parseDouble(parts[1]);
					double bestTime = parts.length > 2 ? Double.parseDouble(parts[2]) : 0.0;

					double gapPercent = 100.0 * (acoCost - bestCost) / bestCost;

					System.out.println("Best known solution found:");
					System.out.println("  Instance : " + nameInFile);
					System.out.println("  Best cost: " + bestCost);
					System.out.println("  ACO cost : " + acoCost);
					System.out.println("  Gap      : " + String.format("%.6f", gapPercent) + "%");

					return new Gap(nameInFile, bestCost, bestTime, gapPercent);

				} catch (NumberFormatException e) {
					System.err.println("Warning: Invalid number format in best-known file: " + line);
				}
			}

			// Instance not found
			System.err.println("ERROR: Instance '" + instanceName + "' NOT FOUND in best-known file!");
			System.err.println("       Gap will be reported as 1000%");
			return null;

		} catch (IOException e) {
			System.err.println("ERROR: Could not read best-known file: " + bestKnownFile);
			System.err.println("       " + e.getMessage());
			return null;
		}
	}

	// ===================================================================
	// Example usage (uncomment to test)
	// ===================================================================
	/*
    public static void main(String[] args) {
        String bestFile = "best_known_solutions.txt";
        String instance = "AP10_3";  // must match exactly
        double acoCost = 1234567.89;

        Gap gap = computeGap(bestFile, instance, acoCost);
        System.out.println(gap);
    }
	 */
}
