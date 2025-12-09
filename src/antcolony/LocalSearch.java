package antcolony;

/**
 * LocalSearch.java
 * Translated from localsearch.cpp + localsearch.h
 * Original by Eliana Fernandes
 * Copyright (c) 2015 Eliana Fernandes. All rights reserved.
 */
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import antcolony.ReadData.Data;
import antcolony.constants.AcoVar;


public class LocalSearch {

    /**
     * Performs local search on the current iteration-best solution
     * by attempting to relocate randomly selected nodes to new hubs.
     */
    public static int localSearch(Data dados, Ant ants, Iteration iter, int iterat) {

        logLS("LocalSearch");
        logLS("iteration: " + iterat);

        // Log initial solution
        logSolution("CURRENT SOLUTION", dados, iter);

        if (ants.life <= 0) {
            return 0;  // No LS for dead ants
        }

        int ls_iter = 0;
        int max_ls_iter = 500;

        int first_admissible = 0;

        while (first_admissible < 1) {
            int count = 0;
            int relocation = 0;
            int flag = 0;
            int stop = 0;
            
            int prod = 0;
            int node = 0;
            int hub = 0;
            int new_hub = 0;

            while (flag < 1) {
                count = 0;
                relocation = 0;
                stop = 0;

                while (stop < 1) {
                    // Random product
                    prod = (int) (AcoVar.myrand() * dados.nbProducts);

                    // Random node
                    node = (int) (AcoVar.myrand() * dados.nbNodes);

                    // Current hub for this node/product
                    hub = iter.x_best[prod][node];

                    logLS("x_best[" + node + "][" + prod + "]= " + hub + " is candidate to be reassigned.");

                    // Try to find a valid new hub
                    new_hub = (int) (AcoVar.myrand() * dados.nbNodes);

                    if (new_hub != hub &&
                        iter.z_best[new_hub] == 1 &&
                        ants.avail_cap[prod][new_hub] >= dados.originatedFlow[prod][node] &&
                        iter.x_best[prod][new_hub] == new_hub) {  // new_hub is a hub for prod

                        logLS("x_best[" + new_hub + "][" + prod + "]= " + new_hub + " is hub.");

                        stop = 1;
                        flag = 1;
                        relocation = 1;
                    }

                    count++;
                    if (count > max_ls_iter / 10) {
                        stop = 1;
                        flag = 1;
                    }
                }  // inner while (stop < 1)
            }  // outer while (flag < 1)

            // Compute cost difference
            double cost_plus = dados.d[node][new_hub] *
                    (dados.chi[prod] * dados.originatedFlow[prod][node] + dados.delta[prod] * dados.destinedFlow[prod][node]);

            double cost_subtract = dados.d[node][hub] *
                    (dados.chi[prod] * dados.originatedFlow[prod][node] + dados.delta[prod] * dados.destinedFlow[prod][node]);

            double delta = cost_plus - cost_subtract;

            // If improvement and valid relocation
            if (delta < 0 && relocation > 0) {
                iter.x_best[prod][node] = new_hub;
                iter.best_cost += delta;

                logLS("Reassignment done to hub " + new_hub);

                // Update capacities
                ants.avail_cap[prod][hub] += dados.originatedFlow[prod][node];
                ants.avail_cap[prod][new_hub] -= dados.originatedFlow[prod][node];

                first_admissible = 2;
                ls_iter++;
            } else {
                ls_iter++;
                logLS((relocation == 0 ? "no relocation done" : "") + "ls_iter: " + ls_iter);
            }

            if (ls_iter > max_ls_iter) {
                first_admissible = 2;
            }

        }  // outer while (first_admissible < 1)

        // Log final solution
        logSolution("Solution AFTER LocalSearch SOLUTION", dados, iter);

        return 0;
    }

    // ===================================================================
    // Logging helpers (active if LSHIST)
    // ===================================================================
    private static void logLS(String msg) {
        if (AcoVar.LSHIST) {
            appendToFile("LShistory.txt", msg);
        }
    }

    private static void logSolution(String title, Data dados, Iteration iter) {
        if (AcoVar.LSHIST) {
            logLS(title);

            logLS("Variables z equal to 1:");
            for (int j = 0; j < dados.nbNodes; j++) {
                if (iter.z_best[j] == 1) {
                    logLS("z[" + j + "]");
                }
            }

            logLS("Variables x equal to 1:");
            for (int p = 0; p < dados.nbProducts; p++) {
                for (int i = 0; i < dados.nbNodes; i++) {
                    int j = iter.x_best[p][i];
                    if (j >= 0) {  // assuming -1 for unassigned
                        logLS("x[" + i + "][" + j + "][" + p + "]");
                    }
                }
            }
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
