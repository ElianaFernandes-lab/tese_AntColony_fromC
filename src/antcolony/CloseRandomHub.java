package antcolony;

/**
 * CloseRandomHub.java
 * Translated from closerandomhub.cpp + closerandomhub.h
 * Original by Eliana Fernandes
 * Copyright (c) 2015 Eliana Fernandes. All rights reserved.
 */
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import antcolony.ReadData.Data;
import antcolony.constants.AcoVar;

public class CloseRandomHub {

    /**
     * Local Search: Randomly selects a hub and tries to close it completely
     * by relocating all nodes assigned to it (for a random product).
     * Accepts the move if total cost decreases (including fixed cost savings).
     */
    public static int closeRandomHub(Data dados, Ant ants, Iteration iter, double max_cost) {

        logCRH("=== CloseRandomHub started ===");

        if (ants.life <= 0) {
            logCRH("Ant is dead. Skipping.");
            return 0;
        }

        Counters nr = Counting.countAll(dados.nbProducts, dados.nbNodes, iter);

        // Temporary solution and capacities (to test move without committing)
        int[][] x_temp = new int[dados.nbProducts][dados.nbNodes];
        double[][] temp_cap = new double[dados.nbNodes][dados.nbProducts];

        // Deep copy current best solution
        for (int p = 0; p < dados.nbProducts; p++) {
            for (int i = 0; i < dados.nbNodes; i++) {
                x_temp[p][i]= iter.x_best[p][i];
            }
        }
        for (int j = 0; j < dados.nbNodes; j++) {
            for (int p = 0; p < dados.nbProducts; p++) {
                temp_cap[j][p] = ants.avail_cap[j][p];
            }
        }

        double temp_cost = iter.best_cost;
        double hub_cost_to_save = 0.0;

        // Choose random product
        int prod = (int) (Math.random() * dados.nbProducts);
        logCRH("Selected random product: " + prod);

        // Get list of hubs used for this product
        List<Integer> hubList = new ArrayList<>();
        for (int l = 0; l < nr.phubs[prod]; l++) {
            int h = nr.list_phubs[prod][l];
            if (h >= 0) hubList.add(h);
        }

        int nr_hubs = hubList.size();
        if (nr_hubs <= 1) {
            logCRH("Only one or zero hubs for product " + prod + ". Cannot close.");
            return 0;
        }

        logCRH("Product " + prod + " has " + nr_hubs + " hubs: " + hubList);

        // Choose random hub to close
        int hub_index = (int) (Math.random() * nr_hubs);
        int hub_to_close = hubList.get(hub_index);
        logCRH("Attempting to close hub " + hub_to_close + " for product " + prod);

        // Determine fixed cost savings
        if (nr.pprods[hub_to_close] == 1) {
            hub_cost_to_save = dados.f[hub_to_close][prod] + dados.g[hub_to_close];
            logCRH("This is the last product → full hub closure saves g[" + hub_to_close + "] + f");
        } else {
            hub_cost_to_save = dados.f[hub_to_close][prod];
            logCRH("Only product-specific fixed cost f[" + hub_to_close + "][" + prod + "] saved");
        }

        // Get all nodes currently assigned to this hub for this product
        List<Integer> nodesToRelocate = new ArrayList<>();
        for (int i = 0; i < dados.nbNodes; i++) {
            if (iter.x_best[prod][i] == hub_to_close) {
                nodesToRelocate.add(i);
            }
        }

        int nr_nodes = nodesToRelocate.size();
        logCRH("Nodes assigned to hub " + hub_to_close + " (product " + prod + "): " + nodesToRelocate);

        if (nr_nodes == 0) {
            logCRH("No nodes assigned. Can close for free.");
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
                double flow = dados.O[node][prod];
                if (flow > maxFlow) {
                    maxFlow = flow;
                    nodeToMove = node;
                }
            }

            // Find hub with largest remaining capacity
            double bestCap = -1;
            int bestHub = -1;
            for (int h : candidateHubs) {
                if (temp_cap[h][prod] > bestCap) {
                    bestCap = temp_cap[h][prod];
                    bestHub = h;
                }
            }

            if (maxFlow > bestCap + 1e-9) {
                logCRH("Cannot relocate node " + nodeToMove + " (flow=" + maxFlow + ") — no hub has enough capacity");
                allRelocated = false;
                break;
            }

            // Compute cost change
            double oldDistCost = dados.d[nodeToMove][hub_to_close] *
                    (dados.chi[prod] * dados.O[nodeToMove][prod] + dados.delta[prod] * dados.D[nodeToMove][prod]);
            double newDistCost = dados.d[nodeToMove][bestHub] *
                    (dados.chi[prod] * dados.O[nodeToMove][prod] + dados.delta[prod] * dados.D[nodeToMove][prod]);

            double deltaCost = newDistCost - oldDistCost;
            temp_cost += deltaCost;

            logCRH("Relocating node " + nodeToMove +
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
                logCRH("IMPROVEMENT! Cost: " + iter.best_cost + " → " + final_cost +
                       " (saved fixed cost: " + hub_cost_to_save + ")");

                // Commit solution
                for (int p = 0; p < dados.nbProducts; p++) {
                    for (int i = 0; i < dados.nbNodes; i++) {
                        iter.x_best[p][i] = x_temp[p][i];
                    }
                }
                for (int j = 0; j < dados.nbNodes; j++) {
                    for (int p = 0; p < dados.nbProducts; p++) {
                        ants.avail_cap[j][p] = temp_cap[j][p];
                    }
                }

                iter.best_cost = final_cost;

                // Close physical hub if it was the last product
                if (nr.pprods[hub_to_close] == 1) {
                    iter.z_best[hub_to_close] = 0;
                    logCRH("Physical hub " + hub_to_close + " fully closed (z=0)");
                }

                logCRH("CloseRandomHub: SUCCESS");
                logSolutionAfter(iter, dados);
                return 1;
            } else {
                logCRH("No improvement: " + iter.best_cost + " → " + final_cost);
            }
        } else {
            logCRH("Failed to relocate all nodes. Move rejected.");
        }

        logCRH("=== CloseRandomHub ended without improvement ===");
        return 0;
    }

    // ===================================================================
    // Logging
    // ===================================================================
    private static void logCRH(String msg) {
        if (AcoVar.CRHHIST || AcoVar.CRH) {
            appendToFile("CRHhistory.txt", msg);
        }
    }

    private static void logSolutionAfter(Iteration iter, Data dados) {
        logCRH("SOLUTION AFTER CLOSE RANDOM HUB");
        logCRH("Variables z = 1:");
        for (int j = 0; j < dados.nbNodes; j++) {
            if (iter.z_best[j] == 1) {
                logCRH("z[" + j + "]");
            }
        }
        logCRH("Variables x assignments:");
        for (int p = 0; p < dados.nbProducts; p++) {
            for (int i = 0; i < dados.nbNodes; i++) {
                int j = iter.x_best[p][i];
                if (j != -1) {
                    logCRH("x[" + i + "][" + j + "][" + p + "]");
                }
            }
        }
    }

    private static void appendToFile(String filename, String text) {
        try (PrintWriter out = new PrintWriter(new FileWriter(filename, true))) {
            out.println(text);
        } catch (IOException e) {
            System.err.println("Error writing to " + filename);
        }
    }
}
