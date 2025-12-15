package com.ef.antcolony;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ef.antcolony.ReadData.Data;
import com.ef.antcolony.model.constants.AcoVar;
import com.ef.antcolony.utils.ArrayUtils;

public class Actions {

	private static final Logger log = LoggerFactory.getLogger(Main.class);

	// =====================================================================
	// 1. Add a solution component (x_node^prod = hub)
	// =====================================================================
	public static double addSolutionComponent(PNH indices, Data dados, Ant ant, double temp_cost) {

		// Assign node to hub for this product
		ant.x[indices.prod][indices.node] = indices.hub;

		log.error("Solution Component Added");
		log.error("ants.x[{}][{}] = {}", indices.prod, indices.node, indices.hub);

		// Remove this component from available choices
		ant.avail_tau[indices.prod][indices.node][indices.hub] = 0;

		// Update transfer + collection cost
		log.error("update transfer and collection costs from:");
		log.error("ants.cost = {}", ant.cost);
		log.error("temp_cost = {}", temp_cost);

		temp_cost = ant.cost;
		double collectionAndTransfer = dados.d[indices.node][indices.hub] *
				(dados.chi[indices.prod] * dados.originatedFlow[indices.prod][indices.node] +
						dados.delta[indices.prod] * dados.destinedFlow[indices.prod][indices.node]);

		ant.cost = temp_cost + collectionAndTransfer;
		temp_cost = ant.cost;

		log.error("update transfer and collection costs to:");
		log.error("ants.cost = {}", ant.cost);
		log.error("temp_cost = {}", temp_cost);

		// If the node is assigned to itself → it's a hub → add fixed cost per product
		if (indices.node == indices.hub) {
			log.error("update fixed costs from:");
			log.error("ants.cost = {}", ant.cost);
			log.error("temp_cost = {}", temp_cost);

			temp_cost = ant.cost;
			ant.cost = temp_cost + dados.f[indices.prod][indices.hub];
			temp_cost = ant.cost;

			log.error("update fixed costs to:");
			log.error("ants.cost = {}", ant.cost);
			log.error("temp_cost = {}", temp_cost);
		}

		return temp_cost;
	}

	// =====================================================================
	// 2. Local pheromone update (after adding a component)
	// =====================================================================
	public static void localPheromoneUpdate(int prod, int hub, int node, Aco a) {
		double oldTau = a.tau[prod][node][hub];
		log.error("tau local updated");
		log.error("a.tau[{}][{}][{}] = {}", prod, node, hub, oldTau);

		a.tau[prod][node][hub] = (1 - AcoVar.RHO) * oldTau + AcoVar.RHO * a.tau0[prod][node][hub];

		log.error("a.tau[{}][{}][{}] = {}", prod, node, hub, a.tau[prod][node][hub]);
	}

	// =====================================================================
	// 3. Apply Single Allocation Rules
	// =====================================================================
	public static void applySingleAllocationRules(int prod, int hub, int node, int nr_nodes,
			Ant ant, int is_node, int is_not_hub) {
		log.error("SOLUTION COMPONENTS MADE UNAVAILABLE SA RULES");

		// Rule 1: a node can be allocated to only one hub per product
		if (is_node > 0) {
			for (int j = 0; j < nr_nodes; j++) {
				if (ant.avail_tau[prod][node][j]> 0) {
					ant.avail_tau[prod][node][j] = 0;
					log.error("ants.avail_tau[{}][{}][{} = 0", prod, node, j);
				}
			}
		}

		// Rule 2: if a node is allocated to a hub, it cannot become a hub itself (except self-allocation)
		if (is_not_hub > 0) {
			for (int i = 0; i < nr_nodes; i++) {
				if (ant.avail_tau[prod][i][node] > 0) {
					ant.avail_tau[prod][i][node] = 0;
					log.error("ants.avail_tau[{}][{}][{}] = 0", prod, i, node);
				}
			}
		}
		
	}

	// =====================================================================
	// 4. Apply Lk (product capacity per hub) rules
	// =====================================================================
	public static void applyLkRules(int prod, int hub, Data dados, Ant ant) {
		if (dados.L[prod] >= dados.nbProducts) {
			return;  // No limit
		}

		log.error("SOLUTION COMPONENTS MADE UNAVAILABLE Lk Rules");

		HubProductCount np = HubProductCount.checkProdConnects(dados.nbProducts, dados.nbNodes, ant);

		// If this hub has reached its product capacity L[hub]
		if (np.prods[hub] >= dados.L[hub]) {
			for (int p = 0; p < dados.nbProducts; p++) {
				if (ant.x[p][hub] != hub) {  // not already a hub for product p
					for (int i = 0; i < dados.nbNodes; i++) {
						if (ant.avail_tau[p][i][hub] > 0) {
							ant.avail_tau[p][i][hub] = 0;
							log.error("ants.avail_tau[{}][{}][{}] = 0", p, i, hub);
						}
					}
				}
			}
		}
	}

	// =====================================================================
	// 5. Update available hub capacity
	// =====================================================================
	public static void updateAvailableCapacities(int prod, int hub, int node, Data dados, Ant ant, int k) {
		log.error("CAPACITY UPDATE FROM {}", ant.avail_cap[prod][hub]);
	
		double oldCap = ant.avail_cap[prod][hub];
		ant.avail_cap[prod][hub] = oldCap - dados.originatedFlow[prod][node];
		
		log.error(" to {}", ant.avail_cap[prod][hub]);
	}

	// =====================================================================
	// 6. Open a hub (pay fixed opening cost g[j])
	// =====================================================================
	public static double openHub(PNH indices, Data dados, Ant ant, double temp_cost) {

		if (ant.z[indices.hub] < 1) {
			ant.z[indices.hub] = 1;
			log.error("OPEN HUB: {}", indices.hub);
			log.error("update fixed costs (open hub) from:");
			log.error("ants.cost = {}", ant.cost);
			log.error("temp_cost = {}", temp_cost);

			temp_cost = ant.cost;
			ant.cost = temp_cost + dados.g[indices.hub];
			temp_cost = ant.cost;

			log.error(" to:");
			log.error("ants.cost = {}", ant.cost);
			log.error("temp_cost = {}", temp_cost);
		}

		return temp_cost;
	}

	// =====================================================================
	// 7. Dedicate a hub to a product (self-allocation + pay f[j][p])
	// =====================================================================
	public static double dedicateHub(PNH indices, Data dados, Ant ant, Aco a, int k, double temp_cost) {

		if (indices.node != indices.hub &&
				ant.x[indices.prod][indices.hub] != indices.hub &&
				ant.avail_tau[indices.prod][indices.hub][indices.hub] > 0) {

			int prod = indices.prod;
			int hub = indices.hub;

			ant.x[prod][hub] = hub;
			log.error("Solution Component Added (dedicate hub)");
			log.error("ants.x[{}][{}] = {}", prod, hub, hub);

			// Fixed cost for dedicating hub to product
			temp_cost = ant.cost;
			ant.cost = temp_cost + dados.f[prod][hub];

			// Transfer/collection cost for self-loop (usually 0, but kept for generality)
			temp_cost = ant.cost;
			ant.cost = temp_cost + dados.d[hub][hub] *
					(dados.chi[prod] * dados.originatedFlow[prod][hub] + dados.delta[prod] * dados.destinedFlow[prod][hub]);
			temp_cost = ant.cost;

			// Remove self-allocation from available
			ant.avail_tau[prod][hub][hub] = 0;

			// Enforce single allocation (this node is now a hub for prod)
			applySingleAllocationRules(prod, hub, hub, dados.nbNodes, ant, 1, 0);

			// Apply capacity limits
			applyLkRules(prod, hub, dados, ant);

			// Local pheromone update
			localPheromoneUpdate(prod, hub, hub, a);

			// Update capacity
			updateAvailableCapacities(prod, hub, hub, dados, ant, k);

			if (ant.avail_cap[prod][hub] <= 0) {
				log.error("actions capacity violation");
				System.exit(1);
			}
		}
		return temp_cost;
	}

	// =====================================================================
	// 8. Update iteration-best ant
	// =====================================================================
	public static void getBestAntCost(int nr_prods, int nr_nodes, Ant ant, Iteration iter, int it, int k) {
		if (ant.cost <= iter.best_cost) {
			iter.best_cost = ant.cost;
			iter.best_ant = k;

			iter.x_best = ArrayUtils.deepCopy(ant.x);
			iter.z_best = ArrayUtils.deepCopy(ant.z);
		}
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
			bst.x = ArrayUtils.deepCopy(iter.x_best);
			bst.z = ArrayUtils.deepCopy(iter.z_best);
		}

		globalBestHolder[0] = global_bst;
		return 0;
	}

	// =====================================================================
	// 10. Global pheromone update (iteration-best reinforcement)
	// =====================================================================
	public static void globalPheromoneUpdate(Data dados, Aco a, Iteration iter, double scl_prm) {
		for (int p = 0; p < dados.nbProducts; p++) {
			for (int i = 0; i < dados.nbNodes; i++) {
				int j = iter.x_best[p][i];
				if (j == -1) {
					log.error("Error: j = -1 in global update");
					continue;
				}
				double oldTau = a.tau[p][i][j];
				a.tau[p][i][j] = (1 - AcoVar.GAMMA) * oldTau +
						scl_prm * AcoVar.SCALING_PARAMETER * AcoVar.GAMMA * (1.0 / iter.best_cost);
			}
		}
	}

	// =====================================================================
	// 11. Global pheromone update on dead hubs (encourage exploration)
	// =====================================================================
	public static void globalDeadPheromoneUpdate(Data dados, Aco a, Ant ant,
			double scl_prm, double glbl_best) {
		for (int p = 0; p < dados.nbProducts; p++) {
			for (int j = 0; j < dados.nbNodes; j++) {
				log.error("tau Global updated (dead hub)");
				log.error("a.tau[{}][{}][{}] = {}", p, j, j, a.tau[p][j][j]);

				double oldTau = a.tau[p][j][j];
				a.tau[p][j][j] = (1 - AcoVar.GAMMA) * oldTau +
						scl_prm * AcoVar.SCALING_PARAMETER * AcoVar.GAMMA * (1.0 / (glbl_best / 10000));

				log.error("a.tau[{}][{}][{}] = {}", p, j, j, a.tau[p][j][j]);
			}
		}
	}

}
