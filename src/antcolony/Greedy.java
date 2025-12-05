package antcolony;

import antcolony.ReadData.Data;

//This would typically be in a file named Greedy.java

public class Greedy {

	/**
	 * Implements the greedy choice mechanism for the ACO algorithm.
	 * It finds the product (p), source node (i), and hub (j) combination
	 * that has the largest pheromone value (tau) among feasible options.
	 *
	 * @param dados The problem data (Data).
	 * @param a_param The ACO parameters (Aco).
	 * @param ant The current ant state (Ant).
	 * @return An 'Ind' object containing the indices of the chosen component.
	 */
	public static PNH run(int antLife, Data dados, Aco a_param, Ant ant) {

		int p, i, j;
		int flag = 0;
		double largest = 0.0;
		int prod = -1;
		int node = -1;
		int hub = -1;

		// Iterate through all possible product/node/hub combinations
		for (p = 0; p < dados.nbProducts; p++) {
			for (i = 0; i < dados.nbNodes; i++) {
				for (j = 0; j < dados.nbNodes; j++) {

					// Feasibility Check:
					if (a_param.tau[i][j][p] >= largest &&
							ant.avail_tau[p][i][j]> 0 &&
							ant.avail_cap[j][p] > dados.O[i][p]) {

						largest = a_param.tau[i][j][p];
						prod = p;
						node = i;
						hub = j;
						flag = 1;
					}
				}
			}
		}

		// Check if a feasible solution component was found
		if (flag == 0) {
			ant.life = 0; // Set ant life to 0 (death)

			// If no feasible component is available, find the first available to
			// initialize the ant's state for the next solution attempt.
			for (p = 0; p < dados.nbProducts; p++) {
				for (i = 0; i < dados.nbNodes; i++) {
					for (j = 0; j < dados.nbNodes; j++) {
						if (ant.avail_tau[p][i][j] > 0) {
							ant.prod = p;
							ant.node = i;
							ant.hub = j;

							// Break out of all loops
							p = dados.nbProducts;
							i = dados.nbNodes;
							break;
						}
					}
				}
			}
		}

		return new PNH(prod, node, hub);
	}
}
