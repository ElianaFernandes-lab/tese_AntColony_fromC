package main.java.com.ef.antcolony.constants;
/**
 * acovar.java
 * Translated from acovar.h (originally by Eliana Fernandes)
 * Java port for ACO algorithm (Ant Colony Optimization) - 2025
 */

import java.util.Random;

public class AcoVar {

	// =================================================================
	// CONFIGURATION MACROS (converted to static final constants)
	// =================================================================

	public static final boolean REP = true;
	public static final int NR_REP = 10;

	public static final boolean LOG = true;

	// logs
	public static final boolean LOG_MPCSAHLP = true;
	
	public static final boolean USE_LR = true;      // also requires SCAL_LR
	public static final boolean LS = true;
	public static final boolean LSHIST = true;
	public static final boolean HISTORY = true;
	public static final boolean BAHIST = true;
	public static final boolean LOG_DATA = false;
	public static final boolean TAU_HIST = true;

	public static final boolean LSS = true;
	public static final boolean LSSHIST = true;

	public static final boolean CLOSE = true;
	public static final boolean CLOSEHIST = true;

	public static final boolean CRH = true;        // close random hub
	public static final boolean CRHHIST = true;

	public static final boolean USE_HVIS = true;

	public static final boolean USE_PRE = true;
	public static final boolean USE_FIXED_COST_PRE = true;

	public static final boolean TIME = true;
	public static final boolean TIME_MEM = true;

	public static final boolean USE_CPLEX = true;

	public static final boolean UPDATE_DEAD = true;
	public static final int MAX_DEAD = 10;

	public static final boolean UPDATE_BEST = true;
	public static final double UPDATE_PARAM = 0.2;   // 0 for update only if best

	public static final int NR_ITER = 100;//5000;          // 100000 in some versions
	public static final int MAX_NO_BEST = NR_ITER / 10;
	public static final int MAX_TIME = 360;          // seconds
	public static final int NR_ANTS = 10;
	public static final int TAU0 = 10;
	public static final int TAO = 1000;
	public static final int BETA = 1;

	// Local pheromone decay parameter
	public static final double RHO = 0.5;            // 0 < rho < 1

	// Global pheromone decay parameter
	public static final double GAMMA = 0.5;          // 0 < gamma < 1

	public static final double SCALING_PARAMETER = 5e6;
	public static final int SCL_P = 1;
	public static final boolean SCAL_LR = true;

	public static final String INPUT_PATH = "/Users/elianafernandes/Documents/Eclipse_projs/tese2025/tese_AntColony_fromC/src/inputfiles/";
	public static final String INPUT_SOL_PATH = "/Users/elianafernandes/Documents/Eclipse_projs/tese2025/tese_AntColony_fromC/src/inputfiles/sol/";
	// Used for selection in the pseudo-random proportional rule
	public static final double Q0 = 1.0; //0.1;
	
	//MILP time limit (mili seconds)
	public static final int MILP_MAX_TIME_MILLIS = 6 * 60 * 60 * 1000; // (6 hours as in thesis)
	public static final String SOLVER_ID = "HIGHS"; 

	// Random number generator (thread-safe if you use ThreadLocalRandom or synchronize)
	private static final Random rnd = new Random();

	/** uniform in [0,1) */
	public static double myrand() {
		return rnd.nextDouble();
	}

	/** continuous uniform in [min, max) */
	public static double getrandom_c(double min, double max) {
		return min + myrand() * (max - min);
	}
}