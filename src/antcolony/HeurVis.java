package antcolony;

import antcolony.ReadData.Data;
import antcolony.constants.AcoVar;

/**
 * HeurVis.java
 * Translated from hvis.h (struct heur_vis)
 * Original by Eliana Fernandes
 * Copyright (c) 2015 Eliana Fernandes. All rights reserved.
 */
public class HeurVis {
    public double[][] tfc;    // Total Flow Cost if node i is the only hub for product p
    public double[][] hubFixedCost;    // Hub Fixed Cost (f[i][p] + g[i])
    public double[][][] eta;  // Heuristic visibility η[i][j][p]

    public HeurVis(Data data, PreProc preProc) {
    	int nbProducts = data.nbProducts;
    	int nbNodes = data.nbNodes;
    	
        this.tfc = new double[nbNodes][nbProducts];
        this.hubFixedCost = new double[nbNodes][nbProducts];
        this.eta = new double[nbNodes][nbNodes][nbProducts];
        
        this.compute(data, preProc);
    }
    
    public void compute(Data dados, PreProc pre_p) {
        int nbNodes = dados.nbNodes;
        int nbProducts = dados.nbProducts;

        // ===================================================================
        // 1. Total Flow Cost (tfc[i][p]) - cost if i is the single hub for p
        // ===================================================================
        for (int p = 0; p < nbProducts; p++) {
            for (int i = 0; i < nbNodes; i++) {
                double sum = 0.0;
                for (int j = 0; j < nbNodes; j++) {
                    for (int l = 0; l < nbNodes; l++) {
                        double flow = dados.w[j][l][p];
                        double collect = dados.chi[p] * dados.d[j][i];
                        double distrib = dados.delta[p] * dados.d[i][l];
                        sum += flow * (collect + distrib);
                    }
                }
                this.tfc[i][p] = sum;
            }
        }

        // ===================================================================
        // 2. Hub Fixed Cost (hfc[i][p]) = f[i][p] + g[i]
        // ===================================================================
        for (int p = 0; p < nbProducts; p++) {
            for (int i = 0; i < nbNodes; i++) {
                this.hubFixedCost[i][p] = dados.f[i][p] + dados.g[i];
            }
        }

        // ===================================================================
        // 3. Initialize η[i][j][p] to 100 (neutral value)
        // ===================================================================
        for (int p = 0; p < nbProducts; p++) {
            for (int i = 0; i < nbNodes; i++) {
                for (int j = 0; j < nbNodes; j++) {
                    this.eta[i][j][p] = 100.0;
                }
            }
        }

        // ===================================================================
        // 4. Compute scaling factor for diagonal (self-assignment) η[i][i][p]
        // ===================================================================
        double aux1_eta = 0.0;
        for (int p = 0; p < nbProducts; p++) {
            for (int i = 0; i < nbNodes; i++) {
                aux1_eta += this.tfc[i][p] * this.hubFixedCost[i][p];
            }
        }
        aux1_eta *= 10.0;  // same as original

        // ===================================================================
        // 5. Apply heuristic visibility only if USE_HVIS is active
        // ===================================================================
        if (AcoVar.USE_HVIS) {
            log("HEURISTIC VISIBILITY USED");

            for (int p = 0; p < nbProducts; p++) {
                for (int i = 0; i < nbNodes; i++) {
                    if (this.tfc[i][p] > 1e-9 && this.hubFixedCost[i][p] > 1e-9) {
                        double value = pre_p.allow[p][i][i] * aux1_eta / (this.tfc[i][p] * this.hubFixedCost[i][p]);
                        this.eta[i][i][p] = Math.max(value, 1e-9);  // avoid zero
                    } else {
                        this.eta[i][i][p] = 1e-9;
                    }
                }
            }

            // Off-diagonal η[i][j][p] (i≠j) is not used in current ACO version
            // Left at 100.0 as neutral
        }

    }

    // ===================================================================
    // Logging helper
    // ===================================================================
    private static void log(String msg) {
        if (AcoVar.HISTORY) {
            try (var writer = new java.io.PrintWriter(new java.io.FileWriter("history1.txt", true))) {
                writer.println(msg);
            } catch (Exception e) {
                System.err.println("Failed to write to history1.txt: " + e.getMessage());
            }
        }
    }
}