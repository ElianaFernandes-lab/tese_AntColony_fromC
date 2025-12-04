package antcolony;

/** Information about the best ant of a single iteration */
public class Iteration {
	public int best_ant;
	public double best_cost;

	// Iteration-best solution
	public double[][][][] y_best;   // flow variables if needed (y[p][i][j][k] or similar)
	public int[][] x_best;          // x[p][i]
	public int[] z_best;            // z[j]

	public Iteration(int nProducts, int nbNodes) {
		this.y_best = new double[nProducts][nbNodes][nbNodes][nbNodes];
		this.x_best = new int[nProducts][nbNodes];
		this.z_best = new int[nbNodes];
	}


	public void initialize(int nProducts, int nbNodes) {
		this.best_cost = RunAco.MAX_COST;
		for(int i = 0 ;i < nbNodes; i++) {
			this.z_best[i] = 0;
			for(int p = 0; p < nProducts; p++) {
				this.x_best[i][p]=-1;
			}
		}
	}
}
