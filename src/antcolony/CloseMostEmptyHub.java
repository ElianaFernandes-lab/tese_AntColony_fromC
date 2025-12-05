package antcolony;

/**
 * CloseMostEmptyHub.java
 * Translated from closemostemptyhub.cpp + closemostemptyhub.h
 * Original by Eliana Fernandes
 * Copyright (c) 2015 Eliana Fernandes. All rights reserved.
 */
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import antcolony.ReadData.Data;
import antcolony.constants.AcoVar;

public class CloseMostEmptyHub {

    /**
     * Local search: tries to close the "most empty" dedicated hub
     * (the one with only self-connection for a product) if savings can be made.
     */
    public static int closeMostEmptyHub(Data dados, Ant ants, Iteration iter, double max_cost) {

        log("CloseMostEmptyHub called");

        if (ants.life <= 0) {
            return 0;
        }

        Counters nr = Counting.countAll(dados.nbProducts, dados.nbNodes, iter);

        double[][] hub_cost = new double[dados.nbProducts][dados.nbNodes];
        int[] temp_list_hubs = new int[dados.nbNodes];  // safe upper bound

        int min_connects = dados.nbNodes + 1;
        int prod = -1, hub = -1;
        int new_hub = -1;
        double min_cost_difference = max_cost;

        // ------------------------------------------------------------------
        // Log current best solution
        // ------------------------------------------------------------------
        log("CURRENT SOLUTION");
        log("Variables z equal to 1:");
        for (int j = 0; j < dados.nbNodes; j++) {
            if (iter.z_best[j] == 1) log("z[" + j + "]");
        }
        log("Variables x assignments:");
        for (int i = 0; i < dados.nbNodes; i++) {
            for (int p = 0; p < dados.nbProducts; p++) {
                int j = iter.x_best[p][i];
                if (j != -1) {
                    log("x[" + i + "][" + j + "][" + p + "]");
                }
            }
        }

        // Find minimum number of connections any product-specific hub has
        for (int p = 0; p < dados.nbProducts; p++) {
            for (int j = 0; j < dados.nbNodes; j++) {
                if (nr.pconnects[p][j] > 0 && nr.pconnects[p][j] < min_connects) {
                    min_connects = nr.pconnects[p][j];
                }
            }
        }

        log("Looking for hubs with " + min_connects + " connections (candidates)");

        if (min_connects != 1) {
            log("No isolated dedicated hubs found (min_connects = " + min_connects + "). Nothing to close.");
            return 0;
        }

        log("Isolated dedicated hubs found. Proceeding to cost analysis.");

        // Compute closure cost: f[j][p] + possibly g[j] if it's the last product
        for (int p = 0; p < dados.nbProducts; p++) {
            for (int j = 0; j < dados.nbNodes; j++) {
                if (nr.pconnects[p][j] == 1) {
                    if (nr.connects[j] == 1) {
                        // This hub has only one product → closing frees g[j] too
                        hub_cost[p][j] = dados.f[j][p] + dados.g[j];
                    } else {
                        hub_cost[p][j] = dados.f[j][p];
                    }
                }
            }
        }

        // Find most expensive such hub to close
        double max_hub_cost = -1;
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
            log("No valid candidate found.");
            return 0;
        }

        log("Close candidate: hub " + hub + " for product " + prod +
            " (closure cost = " + hub_cost[prod][hub] + ")");

        // Build list of alternative hubs (excluding the one we're closing)
        int temp_idx = 0;
        for (int l = 0; l < nr.phubs[prod]; l++) {
            int h = nr.list_phubs[prod][l];
            if (h != hub) {
                temp_list_hubs[temp_idx++] = h;
            } else {
                temp_list_hubs[temp_idx++] = -1;
            }
        }
        int total_candidates = temp_idx;

        // Filter: must have enough capacity AND not already at product limit L[j]
        int valid_hubs = 0;
        for (int l = 0; l < total_candidates; l++) {
            int j = temp_list_hubs[l];
            if (j >= 0) {
                boolean hasCapacity = ants.avail_cap[j][prod] >= dados.O[hub][prod];
                boolean notFull = nr.pprods[j] < dados.L[j];

                if (hasCapacity && notFull) {
                    temp_list_hubs[valid_hubs++] = j;
                    log("Hub " + j + " accepted: cap=" + ants.avail_cap[j][prod] +
                        " >= " + dados.O[hub][prod] + ", prods=" + nr.pprods[j] + " < L=" + dados.L[j]);
                } else {
                    log("Hub " + j + " rejected: cap=" + ants.avail_cap[j][prod] +
                        ", need=" + dados.O[hub][prod] + " | prods=" + nr.pprods[j] + " vs L=" + dados.L[j]);
                    temp_list_hubs[l] = -1;
                }
            }
        }

        if (valid_hubs == 0) {
            log("No feasible alternative hub found. Cannot close hub " + hub);
            return 0;
        }

        log("Found " + valid_hubs + " feasible alternative hubs");

        // Evaluate cost difference for each candidate
        min_cost_difference = 0;
        new_hub = -1;

        for (int l = 0; l < valid_hubs; l++) {
            int candidate = temp_list_hubs[l];

            double cost_plus = dados.d[hub][candidate] *
                    (dados.chi[prod] * dados.O[hub][prod] + dados.delta[prod] * dados.D[hub][prod]);

            double cost_subtract = hub_cost[prod][hub];

            double cost_difference = cost_plus - cost_subtract;

            log("Try relocate to hub " + candidate +
                " → +cost = " + cost_plus + ", -cost = " + cost_subtract + " → Δ = " + cost_difference);

            if (cost_difference < min_cost_difference) {
                min_cost_difference = cost_difference;
                new_hub = candidate;
            }
        }

        // Apply improvement if beneficial
        if (min_cost_difference < 0) {
            log("IMPROVEMENT FOUND: Δ = " + min_cost_difference +
                " by reassigning hub " + hub + " (product " + prod + ") → hub " + new_hub);

            // Close physical hub if it was the last product
            if (nr.connects[hub] == 1) {
                iter.z_best[hub] = 0;
                log("Physical hub " + hub + " fully closed (z = 0)");
            }

            // Reassign the hub node itself
            iter.x_best[prod][hub] = new_hub;

            // Update objective
            iter.best_cost += min_cost_difference;

            // Update capacities
            ants.avail_cap[hub][prod] += dados.O[hub][prod];      // free up old hub
            ants.avail_cap[new_hub][prod] -= dados.O[hub][prod];  // consume in new hub

            log("Reassignment complete. New best_cost = " + iter.best_cost);
            return 1; // success
        } else {
            log("No improvement possible. Best Δ = " + min_cost_difference);
        }

        log("------------------");
        return 0;
    }

    // ===================================================================
    // Logging (activated via AcoVar.CLOSEHIST or similar flags)
    // ===================================================================
    private static void log(String msg) {
        if (AcoVar.CLOSEHIST || AcoVar.CRH) {
            appendToFile("history.txt", msg);
        }
    }

    private static void appendToFile(String filename, String text) {
        try (PrintWriter out = new PrintWriter(new FileWriter(filename, true))) {
            out.println(text);
        } catch (IOException e) {
            System.err.println("Error writing to log: " + filename);
        }
    }
}
