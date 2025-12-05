package antcolony;

/**
 * CloseHub.java
 * Translated from closehub.cpp + closehub.h
 * Original by Eliana Fernandes
 * Copyright (c) 2015 Eliana Fernandes. All rights reserved.
 */
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import antcolony.ReadData.Data;
import antcolony.constants.AcoVar;

public class CloseHub {

    /**
     * Attempts to close underutilized hubs in the current best solution
     * to reduce fixed costs while maintaining feasibility.
     */
    public static int closeHub(Data dados, Ant ants, Iteration iter, double max_cost) {

        logClose("CloseHub called");

        if (ants.life <= 0) {
            return 0; // Ant is dead → no local search
        }

        Counters nr = Counting.countAll(dados.nbProducts, dados.nbNodes, iter);

        double[][] hub_cost = new double[dados.nbProducts][dados.nbNodes];
        int[] temp_list_hubs = new int[dados.nbNodes];

        int min_connects = dados.nbNodes;
        double max_hub_cost;
        int prod = -1, hub = -1;
        int new_hub = -1;
        double min_cost_difference = max_cost;

        logClose("CURRENT SOLUTION");
        logClose("Variables z equal to 1:");
        for (int j = 0; j < dados.nbNodes; j++) {
            if (iter.z_best[j] == 1) {
                logClose("z[" + j + "]");
            }
        }
        logClose("Variables x assignments:");
        for (int p = 0; p < dados.nbProducts; p++) {
            for (int i = 0; i < dados.nbNodes; i++) {
                int j = iter.x_best[p][i];
                if (j != -1) {
                    logClose("x[" + i + "][" + j + "][" + p + "]");
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

        logClose("Hubs with " + min_connects + " connections (candidates for closing)");

        // Only try to close hubs that are connected only to themselves (min_connects == 1)
        if (min_connects == 1) {
            logClose("Possible to close isolated dedicated hubs");

            // Compute cost of keeping each dedicated hub (f + possibly g)
            for (int p = 0; p < dados.nbProducts; p++) {
                for (int j = 0; j < dados.nbNodes; j++) {
                    if (nr.pconnects[p][j] == 1) {
                        if (nr.pprods[j] == 1) {
                            // This hub serves only one product → closing it removes g[j] too
                            hub_cost[p][j] = dados.f[j][p] + dados.g[j];
                        } else {
                            // Only remove product-specific fixed cost
                            hub_cost[p][j] = dados.f[j][p];
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
                logClose("No valid hub found to close.");
                return 0;
            }

            logClose("Close candidate: hub " + hub + " for product " + prod +
                     " (cost = " + hub_cost[prod][hub] + ")");

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
                if (ants.avail_cap[j][prod] >= dados.O[hub][prod]) {
                    temp_list_hubs[valid_hubs++] = j;
                    logClose("Hub " + j + " has enough capacity (" + ants.avail_cap[j][prod] + " >= " + dados.O[hub][prod] + ")");
                } else {
                    logClose("Hub " + j + " lacks capacity (" + ants.avail_cap[j][prod] + " < " + dados.O[hub][prod] + ")");
                    temp_list_hubs[l] = -1;
                }
            }

            if (valid_hubs == 0) {
                logClose("No hub has enough capacity to absorb node " + hub + " for product " + prod);
                logClose("Cannot close this hub.");
                return 0;
            }

            logClose("There are " + valid_hubs + " candidate hubs to relocate to");

            // Evaluate cost difference for each candidate new hub
            min_cost_difference = 0;
            new_hub = -1;

            for (int l = 0; l < valid_hubs; l++) {
                int candidate = temp_list_hubs[l];

                double cost_plus = dados.d[hub][candidate] *
                        (dados.chi[prod] * dados.O[hub][prod] + dados.delta[prod] * dados.D[hub][prod]);

                double cost_subtract = hub_cost[prod][hub] +
                        dados.d[hub][hub] * (dados.chi[prod] * dados.O[hub][prod] + dados.delta[prod] * dados.D[hub][prod]);

                double cost_difference = cost_plus - cost_subtract;

                logClose("Relocate to hub " + candidate + ": +cost = " + cost_plus + ", -cost = " + cost_subtract +
                         " → Δ = " + cost_difference);

                if (cost_difference < min_cost_difference) {
                    min_cost_difference = cost_difference;
                    new_hub = candidate;
                }
            }

            // If savings are positive → perform closure and reassignment
            if (min_cost_difference < 0) {
                logClose("Savings of " + (-min_cost_difference) + " by closing hub " + hub +
                         " and reassigning to hub " + new_hub);

                // If this was the last product using this hub → close it fully
                if (nr.connects[hub] == 1) {
                    iter.z_best[hub] = 0;
                    logClose("Hub " + hub + " fully closed (z[" + hub + "] = 0)");
                }

                // Reassign the hub node itself to new hub
                iter.x_best[prod][hub] = new_hub;

                // Update cost
                iter.best_cost += min_cost_difference;

                // Update capacities
                ants.avail_cap[hub][prod] += dados.O[hub][prod];     // free up old
                ants.avail_cap[new_hub][prod] -= dados.O[hub][prod]; // consume in new

                logClose("Reassignment done: x[" + hub + "][" + new_hub + "][" + prod + "]");
                logClose("New best_cost = " + iter.best_cost);

                return 1; // Success: improvement made
            } else {
                logClose("No cost improvement possible. Best Δ = " + min_cost_difference);
            }
        } else {
            logClose("No isolated dedicated hubs (min_connects = " + min_connects + "). Nothing to close.");
        }

        logClose("------------------");
        return 0;
    }

    // ===================================================================
    // Logging helper (only active if CLOSEHIST or similar flags are on)
    // ===================================================================
    private static void logClose(String msg) {
        if (AcoVar.CLOSEHIST || AcoVar.CRH) {
            appendToFile("CLOSEhistory.txt", msg);
        }
    }

    private static void appendToFile(String filename, String text) {
        try (PrintWriter out = new PrintWriter(new FileWriter(filename, true))) {
            out.println(text);
        } catch (IOException e) {
            System.err.println("Error writing to " + filename + ": " + e.getMessage());
        }
    }
}
