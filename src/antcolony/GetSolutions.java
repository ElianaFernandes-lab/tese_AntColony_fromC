package antcolony;

import antcolony.ReadData.Data;

/**
 * GetSolutions.java
 * Translated from getsolutions.cpp + getsolutions.h
 * Original by Eliana Fernandes
 * Copyright (c) 2015 Eliana Fernandes. All rights reserved.
 */
public class GetSolutions {

    /**
     * Full solution with x, y, z, cost, time
     */
    public static class Solution {
        public int[][][] x;          // x[i][j][p] = 1 if node i allocated to hub j for product p
        public double[][][][] y;     // y[i][j][k][p] flow from i to j via hubs k,l for p (not populated here)
        public int[] z;              // z[j] = 1 if hub at j
        public double cost;
        public double time;

        public Solution(Data dados) {
            x = new int[dados.nbNodes][dados.nbNodes][dados.nbProducts];
            y = new double[dados.nbNodes][dados.nbNodes][dados.nbNodes][dados.nbProducts];
            z = new int[dados.nbNodes];
        }
    }

    /**
     * Solution with x and inter_x (perhaps inter-hub allocations)
     */
    public static class SolutionX {
        public int[][][] x;
        public int[][][] inter_x;
        public int count_inter;

        public SolutionX(Data dados) {
            x = new int[dados.nbNodes][dados.nbNodes][dados.nbProducts];
            inter_x = new int[dados.nbNodes][dados.nbNodes][dados.nbProducts];
            count_inter = 0;
        }
    }

    // =====================================================================
    // Get solution from ant and iteration
    // =====================================================================
    public static Solution getSolution(Data dados, Ant ants, Iteration iter) {
        Solution sol = new Solution(dados);

        // Copy z
        System.arraycopy(ants.z, 0, sol.z, 0, dados.nbNodes);

        // Set x from iter.x_best (which is [p][i] = j)
        for (int p = 0; p < dados.nbProducts; p++) {
            for (int i = 0; i < dados.nbNodes; i++) {
                int j = iter.x_best[p][i];
                if (j >= 0 && j < dados.nbNodes) {
                    sol.x[i][j][p] = 1;
                }
            }
        }

        // y remains all 0 (as in original)

        sol.cost = iter.best_cost;

        return sol;
    }

    // =====================================================================
    // Get x-solution (x and inter_x)
    // =====================================================================
    public static SolutionX getxSolution(Data dados, Iteration iter) {
        SolutionX sol = new SolutionX(dados);

        // Populate x similar to above
        for (int p = 0; p < dados.nbProducts; p++) {
            for (int i = 0; i < dados.nbNodes; i++) {
                int j = iter.x_best[p][i];
                if (j >= 0 && j < dados.nbNodes) {
                    sol.x[i][j][p] = 1;
                }
            }
        }

        // inter_x: from truncated code, assume logic for inter-hub x
        // For example, perhaps x where i != j and both are hubs
        sol.count_inter = 0;
        for (int p = 0; p < dados.nbProducts; p++) {
            for (int i = 0; i < dados.nbNodes; i++) {
                for (int j = 0; j < dados.nbNodes; j++) {
                    if (sol.x[i][j][p] == 1 && i != j && iter.z_best[j] == 1 && iter.z_best[i] == 1) { // assumption
                        sol.inter_x[i][j][p] = 1;
                        sol.count_inter++;
                    }
                }
            }
        }

        return sol;
    }

    // =====================================================================
    // Get solution from global best
    // =====================================================================
    public static Solution getIterSolution(Data dados, Best bst) {
        Solution sol = new Solution(dados);

        // Copy z
        System.arraycopy(bst.z, 0, sol.z, 0, dados.nbNodes);

        // Set x from bst.x [p][i] = j
        for (int p = 0; p < dados.nbProducts; p++) {
            for (int i = 0; i < dados.nbNodes; i++) {
                int j = bst.x[p][i];
                if (j >= 0 && j < dados.nbNodes) {
                    sol.x[i][j][p] = 1;
                }
            }
        }

        // y 0

        sol.cost = bst.cost;

        return sol;
    }

    // =====================================================================
    // Get best solution from ant and iteration (similar to getSolution)
    // =====================================================================
    public static Solution getBestSolution(Data dados, Ant ants, Iteration iter) {
        Solution sol = new Solution(dados);

        // Copy z from ants
        System.arraycopy(ants.z, 0, sol.z, 0, dados.nbNodes);

        // Set x from iter
        for (int p = 0; p < dados.nbProducts; p++) {
            for (int i = 0; i < dados.nbNodes; i++) {
                int j = iter.x_best[p][i];
                if (j >= 0 && j < dados.nbNodes) {
                    sol.x[i][j][p] = 1;
                }
            }
        }

        // y 0

        sol.cost = iter.best_cost;

        return sol;
    }
}
