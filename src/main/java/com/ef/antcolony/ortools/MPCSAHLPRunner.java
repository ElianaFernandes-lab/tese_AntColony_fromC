package main.java.com.ef.antcolony.ortools;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import main.java.com.ef.antcolony.Aco;
import main.java.com.ef.antcolony.ReadData;
import main.java.com.ef.antcolony.GetSolutions.Solution;
import main.java.com.ef.antcolony.ReadData.Data;

public class MPCSAHLPRunner {

	private static final Logger log = LoggerFactory.getLogger(MPCSAHLPRunner.class);
	private static final boolean forceSolution = false;


	public static void main(String[] args) {

		log.info( "Max heap = {} MB" ,Runtime.getRuntime().maxMemory() / (1024L * 1024L));

		String fileIn;
		// List of instances to run (from the repeated blocks in original)
		String[] instances = {
				"AP20LL_1_1"
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
				if(forceSolution) {
					MPCSAHLP solver = new MPCSAHLP(data.nbProducts, data.nbNodes, false);
					Aco aco = new Aco(data.nbProducts, data.nbNodes);
					Solution sol = ReadSolutionFromFile.run(data, fileIn);
					solver.solve(data, aco, sol);
				} else {
					boolean isLinearRelaxation = false;
					MPCSAHLP solver = new MPCSAHLP(data.nbProducts, data.nbNodes, isLinearRelaxation);
					solver.solve(data);
				}

			} catch (Exception e) {
				log.error("Error while running MILP");
				e.printStackTrace();
			}

			log.info("FINISHED COMPUTING MILP");
		}
	}
}