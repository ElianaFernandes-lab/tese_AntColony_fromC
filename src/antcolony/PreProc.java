package antcolony;

import antcolony.ReadData.Data;

/**
 * PreProc.java (continued)
 * Preprocessing to reduce search space
 */

public class PreProc {

	public int[][][] allow; 

	/**
	 * Computes preprocessing rules to forbid bad assignments
	 */
	public static PreProc compute(Data dados) {
		int nbNodes = dados.nbNodes;
		int nbProds = dados.nbProducts;

		// Initialize all assignments as allowed
		PreProc preProcessor = allowAll(nbNodes, nbProds);

		// ===============================================================
		// 1. Capacity-based elimination (from EK 1999, constraint 13)
		// ===============================================================
		for (int p = 0; p < nbProds; p++) {
			for (int j = 0; j < nbNodes; j++) {
				for (int i = 0; i < nbNodes; i++) {
					if (i != j) {
						double remainingCap = dados.gamma[j][p] - dados.O[j][p];
						if (dados.O[i][p] > remainingCap + 1e-9) {
							preProcessor.allow[i][j][p] = 0;
						}
					}
				}
			}
		}

		// ===============================================================
		// 2. Fixed cost dominance rule (optional, controlled by flag)
		// ===============================================================
		if (AcoVar.USE_FIXED_COST_PRE) {
			log("FIXED COST PREPROCESSING USED");

			for (int p = 0; p < nbProds; p++) {
				for (int i = 0; i < nbNodes; i++) {
					for (int j = 0; j < nbNodes; j++) {
						if (i == j) continue;

						double flowCost = dados.d[i][j] *
								(dados.chi[p] * dados.O[i][p] + dados.delta[p] * dados.D[i][p]);

						double selfHubCost = dados.f[i][p] + dados.g[i] +
								dados.alpha[p] * dados.d[i][j] *
								(dados.O[i][p] + dados.D[i][p] - 2 * dados.w[i][i][p]);

						// If routing via j is more expensive than opening self-hub i
						if (dados.O[i][p] < dados.gamma[i][p] && flowCost > selfHubCost + 1e-9) {
							preProcessor.allow[i][j][p] = 0;
						}
					}
				}
			}
		}

		// ===============================================================
		// 3. Inference rule: if j cannot be hub for p, no node can route to it
		// ===============================================================
		for (int p = 0; p < nbProds; p++) {
			for (int j = 0; j < nbNodes; j++) {
				if (preProcessor.allow[j][j][p] == 0) {  // j cannot be dedicated hub for p
					for (int i = 0; i < nbNodes; i++) {
						if (i != j) {
							preProcessor.allow[i][j][p] = 0;
						}
					}
				}
			}
		}

		return preProcessor;
	}

	public static PreProc allowAll(int nbNodes, int nbProds) {
		PreProc pre_p = new PreProc();
		for (int i = 0; i < nbNodes; i++) {
			for (int j = 0; j < nbNodes; j++) {
				for (int p = 0; p < nbProds; p++) {
					pre_p.allow[i][j][p] = 1;
				}
			}
		}

		return pre_p;
	}

	// ===================================================================
	// Logging (optional)
	// ===================================================================
	private static void log(String msg) {
		if (AcoVar.HISTORY) {
			try (var writer = new java.io.PrintWriter(
					new java.io.FileWriter("history1.txt", true))) {
				writer.println(msg);
			} catch (Exception e) {
				System.err.println("Failed to write preprocessing log: " + e.getMessage());
			}
		}
	}
}