package antcolony;

/**
 * Part of Acovar.Aco class
 * Pheromone matrices: tau0[i][j][p] and tau[i][j][p]
 */

public class Aco {
    // INITIAL PHEROMONE TRAIL: tau0[i][j][p]
    public double[][][] tau0;

    // CURRENT PHEROMONE TRAIL: tau[i][j][p]
    public double[][][] tau;

    public Aco(int nNodes, int nHubs, int nProducts) {
        tau0 = new double[nNodes][nHubs][nProducts];
        tau  = new double[nNodes][nHubs][nProducts];
    }
    
    
    public void initPheromones() {
    	// Initialize initial pheromone matrix tau0 to 0.0
        for (int i = 0; i < tau0.length; i++) {
            for (int j = 0; j < tau0[i].length; j++) {
                for (int p = 0; p < tau0[i][j].length; p++) {
                    tau0[i][j][p] = 0.0;
                    tau[i][j][p] = 0.0;
                }
            }
        }
    }
}
