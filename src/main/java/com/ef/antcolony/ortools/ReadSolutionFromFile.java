package main.java.com.ef.antcolony.ortools;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Scanner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import main.java.com.ef.antcolony.GetSolutions.Solution;
import main.java.com.ef.antcolony.ReadData.Data;
import main.java.com.ef.antcolony.constants.AcoVar;

public class ReadSolutionFromFile {

	private ReadSolutionFromFile() {
		// default constructor
	}

	private static final Logger log = LoggerFactory.getLogger(ReadSolutionFromFile.class);


	/**
	 * Reads the problem instance from file
	 */
	public static Solution run(Data data, String fileName) {
		Solution sol = new Solution(data);
		String input = AcoVar.INPUT_SOL_PATH + fileName + "_OUT_MILP.txt";
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(input));
		} catch (FileNotFoundException e) {
			log.error(e.getMessage());
		}

		String line;

		// Store results
		double totalCost = 0;
		double timeSeconds = 0;
		double timeMinutes = 0;

		try {
			while ((line = reader.readLine()) != null) {
				line = line.trim();
				if (line.isEmpty()) continue;

				if (line.contains("=")) {
					String[] parts = line.split("=");
					String left = parts[0].trim();
					String right = parts[1].trim();

					if (left.equals("TotalCost")) {
						totalCost = Double.parseDouble(right);
					} else if (left.startsWith("z[")) {
						// Extract index from z[0], z[1], etc.
						int index = Integer.parseInt(left.substring(2, left.length() - 1));
						sol.z[index] = Integer.parseInt(right);
					} else if (left.startsWith("x[")) {
						// Extract indices from x[0,0,0]
						String[] indices = left.substring(2, left.length() - 1).split(",");
						int i = Integer.parseInt(indices[0].trim());
						int j = Integer.parseInt(indices[1].trim());
						int k = Integer.parseInt(indices[2].trim());
						sol.x[i][j][k] = Integer.parseInt(right);
					}
					else if (left.startsWith("y[")) {
						// Extract indices from y[0,0,4,3]
						String[] indices = left.substring(2, left.length() - 1).split(",");
						int i = Integer.parseInt(indices[0].trim());
						int j = Integer.parseInt(indices[1].trim());
						int k = Integer.parseInt(indices[2].trim());
						int l = Integer.parseInt(indices[3].trim());
						sol.y[i][j][k][l] = Double.parseDouble(right);
					} else if (line.contains("time elapsed")) {
						// Parse time values
						Scanner timeScanner = new Scanner(line);
						while (timeScanner.hasNext()) {
							if (timeScanner.hasNextDouble()) {
								double time = timeScanner.nextDouble();
								if (timeSeconds == 0) {
									timeSeconds = time;
								} else {
									timeMinutes = time;
								}
							} else {
								timeScanner.next();
							}
						}
						timeScanner.close();
					}
				}
			}
		} catch (NumberFormatException | IOException e) {
			log.error(e.getMessage());
		} 

		try {
			reader.close();
		} catch (IOException e) {
			log.error(e.getMessage());
		}

		// Print some results
		log.debug("TotalCost: {}", totalCost);
		log.debug("solution: {}", sol);
		log.debug("Time: {} seconds, {} minutes", timeSeconds, timeMinutes);

		return sol;
	}
}
