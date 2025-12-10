package antcolony;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import antcolony.ReadData.Data;
import antcolony.constants.AcoVar;

public class CloseHub {

	private CloseHub() {
		// default constructor
	}

	private static final Logger log = LogManager.getLogger(CloseHub.class);
	private static final boolean PRINT_LOGS = AcoVar.CLOSEHIST;

	/**
	 * Attempts to close underutilized hubs in the current best solution
	 * to reduce fixed costs while maintaining feasibility.
	 */
	public static boolean closeHub(Data dados, Ant ants, Iteration iter, double max_cost) {

		if(PRINT_LOGS) {
			log.error("CloseHub called");
		}

		if (ants.life <= 0) {
			return false; // Ant is dead → no local search
		}

		Counters nr = Counting.countAll(dados.nbProducts, dados.nbNodes, iter);

		double[][] hub_cost = new double[dados.nbProducts][dados.nbNodes];
		int[] temp_list_hubs = new int[dados.nbNodes];

		int min_connects = dados.nbNodes;
		double max_hub_cost;
		int prod = -1;
		int hub = -1;
		int new_hub = -1;
		double min_cost_difference = max_cost;

		if(PRINT_LOGS) {
			log.error("CURRENT SOLUTION");
			log.error("Variables z equal to 1:");
			for (int j = 0; j < dados.nbNodes; j++) {
				if (iter.z_best[j] == 1) {
					log.error("z[{}]", j);
				}
			}
			log.error("Variables x assignments:");
			for (int p = 0; p < dados.nbProducts; p++) {
				for (int i = 0; i < dados.nbNodes; i++) {
					int j = iter.x_best[p][i];
					if (j != -1) {
						if(PRINT_LOGS) {
							log.error("x[{},{},{}]", i, j, p);
						}

					}
				}
			}
		}

		// Find minimum number of connections any dedicated hub has
		for (int p = 0; p < dados.nbProducts; p++) {
			for (int j = 0; j < dados.nbNodes; j++) {
				if (nr.pconnects[p][j] > 0 && nr.pconnects[p][j] < min_connects) {
					min_connects = nr.pconnects[p][j];
				}
			}
		}

		if(PRINT_LOGS) {
			log.info("Phase 3: Identifying closure candidates - hubs with ≤ {} connections", 
					min_connects);
		}


		// Only try to close hubs that are connected only to themselves (min_connects == 1)
		if (min_connects == 1) {
			if(PRINT_LOGS) {
				log.error("Possible to close isolated dedicated hubs");
			}

			// Compute cost of keeping each dedicated hub (f + possibly g)
			for (int p = 0; p < dados.nbProducts; p++) {
				for (int j = 0; j < dados.nbNodes; j++) {
					if (nr.pconnects[p][j] == 1) {
						if (nr.pprods[j] == 1) {
							// This hub serves only one product → closing it removes g[j] too
							hub_cost[p][j] = dados.f[p][j] + dados.g[j];
						} else {
							// Only remove product-specific fixed cost
							hub_cost[p][j] = dados.f[p][j];
						}
					}
				}
			}

			// Find most expensive such hub
			max_hub_cost = -1;
			for (int p = 0; p < dados.nbProducts; p++) {
				for (int j = 0; j < dados.nbNodes; j++) {
					if (nr.pconnects[p][j] == 1 && hub_cost[p][j] > max_hub_cost) {
						max_hub_cost = hub_cost[p][j];
						prod = p;
						hub = j;
					}
				}
			}

			if (prod == -1 || hub == -1) {
				if(PRINT_LOGS) {
					log.error("No valid hub found to close.");
				}

				return false;
			}

			if(PRINT_LOGS) {
				log.error("Close candidate: hub {} for product {} (cost = {})",
						hub, prod, hub_cost[prod][hub]);
			}

			// Build temporary list of other hubs for this product (excluding the one to close)
			int temp_idx = 0;
			for (int l = 0; l < nr.phubs[prod]; l++) {
				int h = nr.list_phubs[prod][l];
				if (h != hub) {
					temp_list_hubs[temp_idx++] = h;
				}
			}
			int original_count = temp_idx;

			// Filter by capacity: can they accept the flow from hub node itself?
			int valid_hubs = 0;
			for (int l = 0; l < original_count; l++) {
				int j = temp_list_hubs[l];
				if (ants.avail_cap[prod][j] >= dados.originatedFlow[prod][hub]) {
					temp_list_hubs[valid_hubs++] = j;
					if(PRINT_LOGS) {
						log.error("Hub {} has enough capacity ({} >= {})", j, ants.avail_cap[prod][j], dados.originatedFlow[prod][hub]);
					}

				} else {
					if(PRINT_LOGS) {
						log.error("Hub {} lacks capacity ({} < {})", j, ants.avail_cap[prod][j], dados.originatedFlow[prod][hub]);
					}

					temp_list_hubs[l] = -1;
				}
			}

			if (valid_hubs == 0) {
				log.error("No hub has enough capacity to absorb node {} for product {}", 
						hub, prod);
				log.error("Cannot close this hub.");
				return false;
			}

			log.error("There are {} candidate hubs to relocate to", valid_hubs);

			// Evaluate cost difference for each candidate new hub
			min_cost_difference = 0;
			new_hub = -1;

			for (int l = 0; l < valid_hubs; l++) {
				int candidate = temp_list_hubs[l];

				double cost_plus = dados.d[hub][candidate] *
						(dados.chi[prod] * dados.originatedFlow[prod][hub] + dados.delta[prod] * dados.destinedFlow[prod][hub]);

				double cost_subtract = hub_cost[prod][hub] +
						dados.d[hub][hub] * (dados.chi[prod] * dados.originatedFlow[prod][hub] + dados.delta[prod] * dados.destinedFlow[prod][hub]);

				double cost_difference = cost_plus - cost_subtract;

				log.error("Relocate to hub {}: +cost = {}, -cost = {} → Δ = {}",
						candidate, cost_plus, cost_subtract, cost_difference);

				if (cost_difference < min_cost_difference) {
					min_cost_difference = cost_difference;
					new_hub = candidate;
				}
			}

			// If savings are positive → perform closure and reassignment
			if (min_cost_difference < 0) {
				if (PRINT_LOGS) {
					log.error("Savings of {} by closing hub {} and reassigning to hub {}",
							-min_cost_difference, hub, new_hub);
				}

				// If this was the last product using this hub → close it fully
				if (nr.connects[hub] == 1) {
					iter.z_best[hub] = 0;
					if (PRINT_LOGS) {
						log.error("Hub {} fully closed (z[{}] = 0)", hub, hub);
					}

				}

				// Reassign the hub node itself to new hub
				iter.x_best[prod][hub] = new_hub;

				// Update cost
				iter.best_cost += min_cost_difference;

				// Update capacities
				ants.avail_cap[prod][hub] += dados.originatedFlow[prod][hub];     // free up old
				ants.avail_cap[prod][new_hub] -= dados.originatedFlow[prod][hub]; // consume in new
				if(PRINT_LOGS) {
					log.error("Reassignment done: x[{},{},{}]", hub, new_hub, prod);
					log.error("New best_cost = {}", iter.best_cost);
				}


				return true; // Success: improvement made
			} else {
				if(PRINT_LOGS) {
					log.error("No cost improvement possible. Best Δ = {}", min_cost_difference);
				}
			}
		} else {
			if(PRINT_LOGS) {
				log.error("No isolated dedicated hubs (min_connects = {}). Nothing to close.", 
						min_connects);
			}
			return false;
		}

		if(PRINT_LOGS) {
			log.error("------------------");
		}

		return false;
	}

}
