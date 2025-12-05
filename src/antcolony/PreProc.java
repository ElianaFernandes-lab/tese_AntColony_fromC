package antcolony;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import antcolony.ReadData.Data;
import antcolony.constants.AcoVar;

/**
 * PreProc.java (continued)
 * Preprocessing to reduce search space
 */

public class PreProc {

	private static final Logger log = LogManager.getLogger(PreProc.class);

	public int[][][] allow; 


	public PreProc(int nbProds, int nbNodes) {
		this.allow = new int[nbProds][nbNodes][nbNodes]; // [p][i][j] order

		this.init(nbProds, nbNodes);
	}

	private void init(int nbProds, int nbNodes) {
		for (int i = 0; i < nbNodes; i++) {
			for (int j = 0; j < nbNodes; j++) {
				for (int p = 0; p < nbProds; p++) {
					this.allow[p][i][j] = 1;
				}
			}
		}
	}

	/**
	 * Computes preprocessing rules to forbid bad assignments
	 */
	public void compute(Data dados) {
		int nbNodes = dados.nbNodes;
		int nbProds = dados.nbProducts;

		// ===============================================================
		// 1. Capacity-based elimination (from EK 1999, constraint 13)
		// ===============================================================
		for (int p = 0; p < nbProds; p++) {
			for (int j = 0; j < nbNodes; j++) {
				for (int i = 0; i < nbNodes; i++) {
					if (i != j) {
						double remainingCap = dados.gamma[j][p] - dados.O[j][p];
						if (dados.O[i][p] > remainingCap + 1e-9) {
							this.allow[p][i][j] = 0;
						}
					}
				}
			}
		}

		// ===============================================================
		// 2. Fixed cost dominance rule (optional, controlled by flag)
		// ===============================================================
		if (AcoVar.USE_FIXED_COST_PRE) {
			log.info("FIXED COST PREPROCESSING USED");

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
							this.allow[p][i][j] = 0;
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
				if (this.allow[p][j][j] == 0) {  // j cannot be dedicated hub for p
					for (int i = 0; i < nbNodes; i++) {
						if (i != j) {
							this.allow[p][i][j] = 0;
						}
					}
				}
			}
		}
	}
}