package antcolony;

/**
 * Actions.java
 * Translated from actions.cpp + actions.h
 * Original by Eliana Fernandes
 * Copyright (c) 2015 Eliana Fernandes. All rights reserved.
 */
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import antcolony.ReadData.Data;
import antcolony.constants.AcoVar;

public class Actions {
	
	private static final Logger log = LogManager.getLogger(Main.class);

	// =====================================================================
	// 1. Add a solution component (x_node^prod = hub)
	// =====================================================================
	public static double addSolutionComponent(PNH indices, Data dados, Ant ant, double temp_cost) {

		// Assign node to hub for this product
		ant.x[indices.prod][indices.node] = indices.hub;

		log.error("Solution Component Added");
		log.error("ants.x[" + indices.node + "][" + indices.prod + "] = " + indices.hub);

		// Remove this component from available choices
		ant.avail_tau[indices.prod][indices.node][indices.hub] = 0;

		// Update transfer + collection cost
		log.error("update transfer and collection costs from:");
		log.error("ants.cost = " + ant.cost);
		log.error("temp_cost = " + temp_cost);

		temp_cost = ant.cost;
		double collectionAndTransfer = dados.d[indices.node][indices.hub] *
				(dados.chi[indices.prod] * dados.originatedFlow[indices.prod][indices.node] +
						dados.delta[indices.prod] * dados.destinedFlow[indices.prod][indices.node]);

		ant.cost = temp_cost + collectionAndTransfer;
		temp_cost = ant.cost;

		log.error("update transfer and collection costs to:");
		log.error("ants.cost = " + ant.cost);
		log.error("temp_cost = " + temp_cost);

		// If the node is assigned to itself → it's a hub → add fixed cost per product
		if (indices.node == indices.hub) {
			log.error("update fixed costs from:");
			log.error("ants.cost = " + ant.cost);
			log.error("temp_cost = " + temp_cost);

			temp_cost = ant.cost;
			ant.cost = temp_cost + dados.f[indices.prod][indices.hub];
			temp_cost = ant.cost;

			log.error("update fixed costs to:");
			log.error("ants.cost = " + ant.cost);
			log.error("temp_cost = " + temp_cost);
		}

		return temp_cost;
	}

	// =====================================================================
	// 2. Local pheromone update (after adding a component)
	// =====================================================================
	public static void localPheromoneUpdate(int prod, int hub, int node, Aco a) {
		double oldTau = a.tau[prod][node][hub];
		log.error("tau local updated");
		log.error("a.tau[" + node + "][" + hub + "][" + prod + "] = " + oldTau);

		a.tau[prod][node][hub] = (1 - AcoVar.RHO) * oldTau + AcoVar.RHO * a.tau0[prod][node][hub];

		log.error("a.tau[" + node + "][" + hub + "][" + prod + "] = " + a.tau[prod][node][hub]);
	}

	// =====================================================================
	// 3. Apply Single Allocation Rules
	// =====================================================================
	public static int applySingleAllocationRules(int prod, int hub, int node, int nr_nodes,
			Ant ant, int is_node, int is_not_hub) {
		log.error("SOLUTION COMPONENTS MADE UNAVAILABLE SA RULES");

		// Rule 1: a node can be allocated to only one hub per product
		if (is_node > 0) {
			for (int j = 0; j < nr_nodes; j++) {
				if (ant.avail_tau[prod] [node][j]> 0) {
					ant.avail_tau[prod][node][j] = 0;
					log.error("ants.avail_tau[" + node + "][" + j + "][" + prod + "] = 0");
				}
			}
		}

		// Rule 2: if a node is allocated to a hub, it cannot become a hub itself (except self-allocation)
		if (is_not_hub > 0) {
			for (int i = 0; i < nr_nodes; i++) {
				if (ant.avail_tau[prod][i][node] > 0) {
					ant.avail_tau[prod][i][node] = 0;
					log.error("ants.avail_tau[" + i + "][" + node + "][" + prod + "] = 0");
				}
			}
		}
		return 0;
	}

	// =====================================================================
	// 4. Apply Lk (product capacity per hub) rules
	// =====================================================================
	public static int applyLkRules(int prod, int hub, Data dados, Ant ant) {
		if (dados.L[prod] >= dados.nbProducts) return 0;  // No limit

		log.error("SOLUTION COMPONENTS MADE UNAVAILABLE Lk Rules");

		HubProductCount np = HubProductCount.checkProdConnects(dados.nbProducts, dados.nbNodes, ant);

		// If this hub has reached its product capacity L[hub]
		if (np.prods[hub] >= dados.L[hub]) {
			for (int p = 0; p < dados.nbProducts; p++) {
				if (ant.x[hub][p] != hub) {  // not already a hub for product p
					for (int i = 0; i < dados.nbNodes; i++) {
						if (ant.avail_tau[p][i][hub] > 0) {
							ant.avail_tau[p][i][hub] = 0;
							log.error("ants.avail_tau[" + i + "][" + hub + "][" + p + "] = 0");
						}
					}
				}
			}
		}
		return 0;
	}

	// =====================================================================
	// 5. Update available hub capacity
	// =====================================================================
	public static int updateAvailableCapacities(int prod, int hub, int node, Data dados, Ant ant, int k) {
		log.error("CAPACITY UPDATE FROM " + ant.avail_cap[prod][hub]);
		double oldCap = ant.avail_cap[prod][hub];
		ant.avail_cap[prod][hub] = oldCap - dados.originatedFlow[prod][node];
		log.error(" to " + ant.avail_cap[prod][hub]);
		return 0;
	}

	// =====================================================================
	// 6. Open a hub (pay fixed opening cost g[j])
	// =====================================================================
	public static double openHub(PNH indices, Data dados, Ant ant, double temp_cost) {

		if (ant.z[indices.hub] < 1) {
			ant.z[indices.hub] = 1;
			log.error("OPEN HUB: " + indices.hub);
			log.error("update fixed costs (open hub) from:");
			log.error("ants.cost = " + ant.cost);
			log.error("temp_cost = " + temp_cost);

			temp_cost = ant.cost;
			ant.cost = temp_cost + dados.g[indices.hub];
			temp_cost = ant.cost;

			log.error(" to:");
			log.error("ants.cost = " + ant.cost);
			log.error("temp_cost = " + temp_cost);
		}
		
		return temp_cost;
	}

	// =====================================================================
	// 7. Dedicate a hub to a product (self-allocation + pay f[j][p])
	// =====================================================================
	public static double dedicateHub(PNH indices, Data dados, Ant ant, Aco a, int k, double temp_cost) {

		if (indices.node != indices.hub &&
				ant.x[indices.prod][indices.hub]!= indices.hub &&
				ant.avail_tau[indices.prod][indices.hub][indices.hub]> 0) {

			int prod = indices.prod;
			int hub = indices.hub;

			ant.x[hub][prod] = hub;
			log.error("Solution Component Added (dedicate hub)");
			log.error("ants.x[" + hub + "][" + prod + "] = " + hub);

			// Fixed cost for dedicating hub to product
			temp_cost = ant.cost;
			ant.cost = temp_cost + dados.f[prod][hub];
			temp_cost = ant.cost;

			// Transfer/collection cost for self-loop (usually 0, but kept for generality)
			temp_cost = ant.cost;
			ant.cost = temp_cost + dados.d[hub][hub] *
					(dados.chi[prod] * dados.originatedFlow[prod][hub] + dados.delta[prod] * dados.destinedFlow[prod][hub]);
			temp_cost = ant.cost;

			// Remove self-allocation from available
			ant.avail_tau[hub][hub][prod] = 0;

			// Enforce single allocation (this node is now a hub for prod)
			applySingleAllocationRules(prod, hub, hub, dados.nbNodes, ant, 1, 0);

			// Apply capacity limits
			applyLkRules(prod, hub, dados, ant);

			// Local pheromone update
			localPheromoneUpdate(prod, hub, hub, a);

			// Update capacity
			updateAvailableCapacities(prod, hub, hub, dados, ant, k);

			if (ant.avail_cap[prod][hub] <= 0) {
				System.out.println("actions capacity violation");
				System.exit(1);
			}
		}
		return temp_cost;
	}

	// =====================================================================
	// 8. Update iteration-best ant
	// =====================================================================
	public static int getBestAntCost(int nr_prods, int nr_nodes, Ant ant, Iteration iter, int it, int k) {
		if (ant.cost <= iter.best_cost) {
			iter.best_cost = ant.cost;
			iter.best_ant = k;

			// Copy allocation
			for (int p = 0; p < nr_prods; p++) {
				for (int i = 0; i < nr_nodes; i++) {
					iter.x_best[p][i] = ant.x[p][i];
				}
			}
			// Copy hub openings
			System.arraycopy(ant.z, 0, iter.z_best, 0, nr_nodes);
		}
		return 0;
	}

	// =====================================================================
	// 9. Update global best solution
	// =====================================================================
	public static int getBestCost(Data dados, Iteration iter, Best bst,
			int it, double[] globalBestHolder, long startTime) {
		double bst_cost = iter.best_cost;
		double global_bst = globalBestHolder[0];

		if (bst_cost <= global_bst) {
			global_bst = bst_cost;
			bst.cost = bst_cost;
			bst.nr_iter = it;

			long now = System.nanoTime();
			double elapsedSeconds = (now - startTime) / 1_000_000_000.0;
			bst.time = elapsedSeconds;

			// Copy solution
			for (int p = 0; p < dados.nbProducts; p++) {
				for (int i = 0; i < dados.nbNodes; i++) {
					bst.x[p][i] = iter.x_best[p][i];
				}
			}
			System.arraycopy(iter.z_best, 0, bst.z, 0, dados.nbNodes);
		}

		globalBestHolder[0] = global_bst;
		return 0;
	}

	// =====================================================================
	// 10. Global pheromone update (iteration-best reinforcement)
	// =====================================================================
	public static int globalPheromoneUpdate(Data dados, Aco a, Iteration iter, double scl_prm) {
		for (int p = 0; p < dados.nbProducts; p++) {
			for (int i = 0; i < dados.nbNodes; i++) {
				int j = iter.x_best[p][i];
				if (j == -1) {
					System.out.println("Error: j = -1 in global update");
					continue;
				}
				double oldTau = a.tau[i][j][p];
				a.tau[i][j][p] = (1 - AcoVar.GAMMA) * oldTau +
						scl_prm * AcoVar.SCALING_PARAMETER * AcoVar.GAMMA * (1.0 / iter.best_cost);
			}
		}
		return 0;
	}

	// =====================================================================
	// 11. Global pheromone update on dead hubs (encourage exploration)
	// =====================================================================
	public static int globalDeadPheromoneUpdate(Data dados, Aco a, Ant ant,
			double scl_prm, double glbl_best) {
		for (int p = 0; p < dados.nbProducts; p++) {
			for (int j = 0; j < dados.nbNodes; j++) {
				log.error("tau Global updated (dead hub)");
				log.error("a.tau[" + p + "][" + j + "][" + j + "] = " + a.tau[p][j][j]);

				double oldTau = a.tau[p][j][j];
				a.tau[p][j][j] = (1 - AcoVar.GAMMA) * oldTau +
						scl_prm * AcoVar.SCALING_PARAMETER * AcoVar.GAMMA * (1.0 / (glbl_best / 10000));

				log.error("a.tau[" + p + "][" + j + "][" + j + "] = " + a.tau[p][j][j]);
			}
		}
		return 0;
	}

}
