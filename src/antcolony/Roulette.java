package antcolony;

/**
 * Roulette.java
 * Translated from roulette.cpp + roulette.h
 * Original by Eliana Fernandes
 * Copyright (c) 2015 Eliana Fernandes. All rights reserved.
 */
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import antcolony.ReadData.Data;
import antcolony.constants.AcoVar;

public class Roulette {

    /**
     * Performs roulette wheel selection to choose the next solution component (i→j for product p)
     * using pheromone τ[i][j][p] and heuristic η[i][j][p]
     *
     * @return selected assignment (node i → hub j for product p)
     */
    public static PNH run(
            int antLife,
            Data dados,
            Aco a_param,
            Ant ants,
            PreProc pre_p,
            HeurVis hvis) {

        PNH indices = new PNH();
        double beta = AcoVar.BETA;

        // Reset probabilities
        for (int p = 0; p < dados.nbProducts; p++) {
            for (int i = 0; i < dados.nbNodes; i++) {
                for (int j = 0; j < dados.nbNodes; j++) {
                    ants.prob[i][j][p] = -1.0;
                }
            }
        }

        // ===================================================================
        // 1. Compute denominator: sum of τ * η^β for feasible components
        // ===================================================================
        double sum_tau_eta = 0.0;

        for (int p = 0; p < dados.nbProducts; p++) {
            for (int i = 0; i < dados.nbNodes; i++) {
                for (int j = 0; j < dados.nbNodes; j++) {
                    if (ants.avail_tau[p][i][j] > 0 && 
                        ants.avail_cap[p][j] >= dados.originatedFlow[p][i] + 1e-9) {

                        double tau = a_param.tau[p][i][j];
                        double eta = hvis.eta[i][j][p];
                        sum_tau_eta += tau * Math.pow(eta, beta);
                    }
                }
            }
        }

        // If no feasible move → ant dies
        if (sum_tau_eta < 1e-9) {
            antLife = 0;
            log("NO FEASIBLE MOVE → ANT DIES");

            // Record one last available component for next solution start
            for (int p = 0; p < dados.nbProducts; p++) {
                for (int i = 0; i < dados.nbNodes; i++) {
                    for (int j = 0; j < dados.nbNodes; j++) {
                        if (ants.avail_tau[p][i][j] > 0) {
                            ants.prod = p;
                            ants.node = i;
                            ants.hub = j;
                            return indices; // will be ignored
                        }
                    }
                }
            }
            return indices;
        }

        // ===================================================================
        // 2. Compute cumulative probabilities
        // ===================================================================
        double cum_prob = 0.0;

        for (int p = 0; p < dados.nbProducts; p++) {
            for (int i = 0; i < dados.nbNodes; i++) {
                for (int j = 0; j < dados.nbNodes; j++) {
                    if (ants.avail_tau[p][i][j] > 0 && 
                        ants.avail_cap[p][j] >= dados.originatedFlow[p][i] + 1e-9) {

                        double tau = a_param.tau[p][i][j];
                        double eta = hvis.eta[i][j][p];
                        double prob = tau * Math.pow(eta, beta) / sum_tau_eta;

                        cum_prob += prob;
                        ants.prob[i][j][p] = cum_prob;

                        log("cum_prob = " + cum_prob);
                    } else {
                        ants.prob[i][j][p] = -1.0;
                    }
                }
            }
        }

        // ===================================================================
        // 3. Spin the wheel
        // ===================================================================
        double r = AcoVar.myrand() * cum_prob;
        log("r = " + r);

        // Search backwards (as in original) for first prob >= r
        for (int p = dados.nbProducts - 1; p >= 0; p--) {
            for (int i = dados.nbNodes - 1; i >= 0; i--) {
                for (int j = dados.nbNodes - 1; j >= 0; j--) {
                    if (ants.prob[i][j][p] >= r - 1e-9) {
                        log("SELECTED: prob[" + i + "][" + j + "][" + p + "] = " +
                            ants.prob[i][j][p] + " >= " + r);

                        return new PNH(p, i, j);
                    }
                }
            }
        }

        // Fallback (should not happen)
        log("WARNING: Roulette fallback — using first available");
        for (int p = 0; p < dados.nbProducts; p++) {
            for (int i = 0; i < dados.nbNodes; i++) {
                for (int j = 0; j < dados.nbNodes; j++) {
                    if (ants.prob[i][j][p] >= 0) {
                        return new PNH(p, i, j);
                    }
                }
            }
        }

        antLife = 0;
        return indices;
    }

    // ===================================================================
    // Logging (controlled by HISTORY flag)
    // ===================================================================
    private static void log(String msg) {
        if (AcoVar.HISTORY) {
            try (PrintWriter out = new PrintWriter(new FileWriter("history1.txt", true))) {
                out.println(msg);
            } catch (IOException e) {
                System.err.println("Error writing to history1.txt: " + e.getMessage());
            }
        }
    }
}