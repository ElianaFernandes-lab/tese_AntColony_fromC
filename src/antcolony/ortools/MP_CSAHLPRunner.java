package antcolony.ortools;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import antcolony.ReadData;
import antcolony.ReadData.Data;
import antcolony.constants.AcoVar;

public class MP_CSAHLPRunner {

	private static final Logger log = LogManager.getLogger(MP_CSAHLPRunner.class);


	public static void main(String[] args) {

		String fileIn;
		// List of instances to run (from the repeated blocks in original)
		String[] instances = {
				"AP10LL_1_1.dat"
				//			, "AP10TT_2_1.dat", "AP10TT_2_2.dat", "AP10TT_3_1.dat", "AP10TT_3_2.dat", "AP10TT_3_3.dat",
				//			"AP10TL_1_1.dat", "AP10TL_2_1.dat", "AP10TL_2_2.dat", "AP10TL_3_1.dat", "AP10TL_3_2.dat", "AP10TL_3_3.dat",
				//			"AP10LL_1_1.dat", "AP10LL_2_1.dat", "AP10LL_2_2.dat", "AP10LL_3_1.dat", "AP10LL_3_2.dat", "AP10LL_3_3.dat",
				//			"AP10LT_1_1.dat", "AP10LT_2_1.dat", "AP10LT_2_2.dat", "AP10LT_3_1.dat", "AP10LT_3_2.dat", "AP10LT_3_3.dat"
				// Add more from original if needed, e.g., AP20* instances
		};

		for (String instance : instances) {
			fileIn = instance;


			// Read data from file
			Data dat = ReadData.readData(fileIn);

			log.info("COMPUTING MILP");
			try {
				MP_CSAHLP solver = new MP_CSAHLP(dat.nbProducts, dat.nbNodes, false);
				solver.solve(dat);
			} catch (Exception e) {
				log.error("Error while running MILP");
				e.printStackTrace();
			}  // You need to implement this
			log.error("FINISHED COMPUTING MILP");
		}
	}
}