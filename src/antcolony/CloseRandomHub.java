package antcolony;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import antcolony.ReadData.Data;
import antcolony.constants.AcoVar;
import antcolony.utils.DeepCopyArray;

public class CloseRandomHub {

	private static final Logger log = LogManager.getLogger(CloseRandomHub.class);
	private static final boolean PRINT_LOGS = AcoVar.CRHHIST ;

	private CloseRandomHub() {
		// default constructor
	}

	/**
	 * Local Search: Randomly selects a hub and tries to close it completely
	 * by relocating all nodes assigned to it (for a random product).
	 * Accepts the move if total cost decreases (including fixed cost savings).
	 */
	public static boolean closeRandomHub(Data dados, Ant ants, Iteration iter, double max_cost) {

		log.error("=== CloseRandomHub started ===");

		if (ants.life <= 0) {
			log.error("Ant is dead. Skipping.");
			return false;
		}

		Counters nr = Counting.countAll(dados.nbProducts, dados.nbNodes, iter);

		// Temporary solution and capacities (to test move without committing)
		double[][] temp_cap =  DeepCopyArray.deepCopy(ants.avail_cap);

		// Deep copy current best solution
		int[][] x_temp = DeepCopyArray.deepCopy(iter.x_best);

		double temp_cost = iter.best_cost;
		double hub_cost_to_save = 0.0;

		// Choose random product
		int prod = (int) (Math.random() * dados.nbProducts);
		log.error("Selected random product: " + prod);

		// Get list of hubs used for this product
		List<Integer> hubList = new ArrayList<>();
		for (int l = 0; l < nr.phubs[prod]; l++) {
			int h = nr.list_phubs[prod][l];
			if (h >= 0) hubList.add(h);
		}

		int nr_hubs = hubList.size();
		if (nr_hubs <= 1) {
			log.error("Only one or zero hubs for product " + prod + ". Cannot close.");
			return false;
		}

		log.error("Product " + prod + " has " + nr_hubs + " hubs: " + hubList);

		// Choose random hub to close
		int hub_index = (int) (Math.random() * nr_hubs);
		int hub_to_close = hubList.get(hub_index);
		log.error("Attempting to close hub " + hub_to_close + " for product " + prod);

		// Determine fixed cost savings
		if (nr.pprods[hub_to_close] == 1) {
			hub_cost_to_save = dados.f[prod][hub_to_close] + dados.g[hub_to_close];
			log.error("This is the last product → full hub closure saves g[" + hub_to_close + "] + f");
		} else {
			hub_cost_to_save = dados.f[prod][hub_to_close];
			log.error("Only product-specific fixed cost f[" + hub_to_close + "][" + prod + "] saved");
		}

		// Get all nodes currently assigned to this hub for this product
		List<Integer> nodesToRelocate = new ArrayList<>();
		for (int i = 0; i < dados.nbNodes; i++) {
			if (iter.x_best[prod][i] == hub_to_close) {
				nodesToRelocate.add(i);
			}
		}

		int nr_nodes = nodesToRelocate.size();
		log.error("Nodes assigned to hub " + hub_to_close + " (product " + prod + "): " + nodesToRelocate);

		if (nr_nodes == 0) {
			log.error("No nodes assigned. Can close for free.");
		}

		// Remove hub_to_close from candidate relocation list
		List<Integer> candidateHubs = new ArrayList<>(hubList);
		candidateHubs.remove(Integer.valueOf(hub_to_close));

		boolean allRelocated = true;

		// Try to relocate each node (greedily: largest flow first, to largest capacity hub)
		for (int attempt = 0; attempt < nr_nodes; attempt++) {
			if (candidateHubs.isEmpty()) {
				allRelocated = false;
				break;
			}

			// Find node with largest flow
			double maxFlow = -1;
			int nodeToMove = -1;
			for (int node : nodesToRelocate) {
				double flow = dados.originatedFlow[prod][node];
				if (flow > maxFlow) {
					maxFlow = flow;
					nodeToMove = node;
				}
			}

			// Find hub with largest remaining capacity
			double bestCap = -1;
			int bestHub = -1;
			for (int h : candidateHubs) {
				if (temp_cap[prod][h] > bestCap) {
					bestCap = temp_cap[h][prod];
					bestHub = h;
				}
			}

			if (maxFlow > bestCap + 1e-9) {
				log.error("Cannot relocate node " + nodeToMove + " (flow=" + maxFlow + ") — no hub has enough capacity");
				allRelocated = false;
				break;
			}

			// Compute cost change
			double oldDistCost = dados.d[nodeToMove][hub_to_close] *
					(dados.chi[prod] * dados.originatedFlow[nodeToMove][prod] + dados.delta[prod] * dados.destinedFlow[prod][nodeToMove]);
			double newDistCost = dados.d[nodeToMove][bestHub] *
					(dados.chi[prod] * dados.originatedFlow[nodeToMove][prod] + dados.delta[prod] * dados.destinedFlow[prod][nodeToMove]);

			double deltaCost = newDistCost - oldDistCost;
			temp_cost += deltaCost;

			log.error("Relocating node " + nodeToMove +
					" → hub " + bestHub +
					" | Δdist = " + deltaCost +
					" | new temp_cost = " + temp_cost);

			// Apply relocation
			x_temp[prod][nodeToMove] = bestHub;
			temp_cap[bestHub][prod] -= maxFlow;
			temp_cap[hub_to_close][prod] += maxFlow;

			nodesToRelocate.remove(Integer.valueOf(nodeToMove));
		}

		// If all nodes relocated successfully → accept move
		if (allRelocated) {
			double final_cost = temp_cost - hub_cost_to_save;

			if (final_cost < iter.best_cost - 1e-9) {
				log.error("IMPROVEMENT! Cost: " + iter.best_cost + " → " + final_cost +
						" (saved fixed cost: " + hub_cost_to_save + ")");

				// Commit solution
				for (int p = 0; p < dados.nbProducts; p++) {
					for (int i = 0; i < dados.nbNodes; i++) {
						iter.x_best[p][i] = x_temp[p][i];
					}
				}
				for (int j = 0; j < dados.nbNodes; j++) {
					for (int p = 0; p < dados.nbProducts; p++) {
						ants.avail_cap[p][j] = temp_cap[j][p];
					}
				}

				iter.best_cost = final_cost;

				// Close physical hub if it was the last product
				if (nr.pprods[hub_to_close] == 1) {
					iter.z_best[hub_to_close] = 0;
					log.error("Physical hub " + hub_to_close + " fully closed (z=0)");
				}

				log.error("CloseRandomHub: SUCCESS");
				logSolutionAfter(iter, dados);
				return true;
			} else {
				log.error("No improvement: " + iter.best_cost + " → " + final_cost);
			}
		} else {
			log.error("Failed to relocate all nodes. Move rejected.");
		}

		log.error("=== CloseRandomHub ended without improvement ===");
		return false;
	}

	private static void logSolutionAfter(Iteration iter, Data dados) {
		log.error("SOLUTION AFTER CLOSE RANDOM HUB");
		log.error("Variables z = 1:");
		for (int j = 0; j < dados.nbNodes; j++) {
			if (iter.z_best[j] == 1) {
				log.error("z[" + j + "]");
			}
		}
		log.error("Variables x assignments:");
		for (int p = 0; p < dados.nbProducts; p++) {
			for (int i = 0; i < dados.nbNodes; i++) {
				int j = iter.x_best[p][i];
				if (j != -1) {
					log.error("x[" + i + "][" + j + "][" + p + "]");
				}
			}
		}
	}

}
