package com.ef.antcolony;

/** One ant in the colony */
public class Ant {
	public int life;
	public double cost;

	// To start solution construction with components that killed an ant
	public int prod;
	public int node;
	public int hub;

	// Availability matrices (used during construction)
	public int[][][] avail_tau;      // avail_tau[p][i][j]
	public double[][] avail_cap;     // avail_cap[p][j]

	// Probabilities for roulette wheel (accumulated)
	public double[][][] prob;        // prob[i][j][p]  (for this ant only, or shared?)

	// Solution representation
	public int[] z;                  // z[j] = 1 if hub j is open
	public int[][] x;                // x[p][i] = hub to which node i is assigned for product p

	public Ant(int nProducts, int nNodes) {
		this.avail_tau = new int[nProducts][nNodes][nNodes];
		this.avail_cap = new double[nProducts][nNodes];
		this.prob = new double[nNodes][nNodes][nProducts];

		this.z = new int[nNodes];
		this.x = new int[nProducts][nNodes];
	}

	public void initialize(PreProc pre, int nProducts, int nNodes, double[][] gamma) {
		this.life = 1;
		this.cost = 0.0;

		for(int i = 0; i < nNodes; i++) {
			this.z[i] = 0;
			for(int p = 0; p < nProducts; p++) {
				this.x[p][i]=-1;
				this.avail_cap[p][i] = gamma[p][i];

				for(int j = 0; j < nNodes; j++) {
					this.avail_tau[p][i][j] = pre.allow[p][i][j];
				}
			}
		}
	}

}