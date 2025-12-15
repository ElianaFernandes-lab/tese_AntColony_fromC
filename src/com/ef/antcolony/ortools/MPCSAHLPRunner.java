package com.ef.antcolony.ortools;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ef.antcolony.ReadData;
import com.ef.antcolony.ReadData.Data;

public class MPCSAHLPRunner {

	private static final Logger log = LoggerFactory.getLogger(MPCSAHLPRunner.class);


	public static void main(String[] args) {

		String fileIn;
		// List of instances to run (from the repeated blocks in original)
		String[] instances = {
				"AP10TT_1_1.dat"
				//			, "AP10TT_2_1.dat", "AP10TT_2_2.dat", "AP10TT_3_1.dat", "AP10TT_3_2.dat", "AP10TT_3_3.dat",
				//			"AP10TL_1_1.dat", "AP10TL_2_1.dat", "AP10TL_2_2.dat", "AP10TL_3_1.dat", "AP10TL_3_2.dat", "AP10TL_3_3.dat",
				//			"AP10LL_1_1.dat", "AP10LL_2_1.dat", "AP10LL_2_2.dat", "AP10LL_3_1.dat", "AP10LL_3_2.dat", "AP10LL_3_3.dat",
				//			"AP10LT_1_1.dat", "AP10LT_2_1.dat", "AP10LT_2_2.dat", "AP10LT_3_1.dat", "AP10LT_3_2.dat", "AP10LT_3_3.dat"
				// Add more from original if needed, e.g., AP20* instances
		};

		for (String instance : instances) {
			fileIn = instance;

			// Read data from file
			Data data = ReadData.readData(fileIn);

			log.info("COMPUTING MILP");
			try {
				MPCSAHLP solver = new MPCSAHLP(data.nbProducts, data.nbNodes, false);
				solver.solve(data);
			} catch (Exception e) {
				log.error("Error while running MILP");
				e.printStackTrace();
			}  // You need to implement this
			log.info("FINISHED COMPUTING MILP");
		}
	}
}