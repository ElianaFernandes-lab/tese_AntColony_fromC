package antcolony;

/**
 * Counting.java
 * Translated from counting.cpp
 * Original by Eliana Fernandes
 * Copyright (c) 2015 Eliana Fernandes. All rights reserved.
 */
public class Counting {

    /**
     * Analyzes the current best solution and returns detailed connectivity statistics.
     */
    public static Counters countAll(int nbProducts, int nbNodes, Iteration iter) {
        Counters nr = new Counters(nbProducts, nbNodes);

        // Initialize lists with -1
        for (int p = 0; p < nbProducts; p++) {
            for (int j = 0; j < nbNodes; j++) {
                nr.list_phubs[p][j] = -1;
                nr.list_pnodes[p][j] = -1;
            }
        }

        // 1. Count dedicated hubs per product: x[j][p] == j → node j is hub for product p
        for (int p = 0; p < nbProducts; p++) {
            for (int j = 0; j < nbNodes; j++) {
                if (iter.x_best[p][j] == j) {
                    nr.phubs[p]++;
                }
            }
        }

        // 2. Count allocated (non-hub) nodes per product
        for (int p = 0; p < nbProducts; p++) {
            nr.pnodes[p] = nbNodes - nr.phubs[p];
        }

        // 3. Count how many products each hub handles
        for (int j = 0; j < nbNodes; j++) {
            for (int p = 0; p < nbProducts; p++) {
                if (iter.x_best[p][j] == j) {
                    nr.pprods[j]++;
                }
            }
        }

        // 4. Total connections to each hub (across all products, including self)
        for (int j = 0; j < nbNodes; j++) {
            for (int p = 0; p < nbProducts; p++) {
                if (iter.x_best[p][j] == j) {
                    nr.connects[j]++;
                }
            }
        }

        // 5. Connections per product to each hub (including self-loop)
        for (int p = 0; p < nbProducts; p++) {
            for (int j = 0; j < nbNodes; j++) {
                if (iter.x_best[p][j] == j) {  // j is a hub for p
                    for (int i = 0; i < nbNodes; i++) {
                        if (iter.x_best[p][i] == j) {
                            nr.pconnects[p][j]++;
                        }
                    }
                }
            }
        }

        // 6. Build list of hubs per product
        for (int p = 0; p < nbProducts; p++) {
            int idx = 0;
            for (int j = 0; j < nbNodes; j++) {
                if (nr.pconnects[p][j] > 0) {
                    nr.list_phubs[p][idx++] = j;
                }
            }
        }

        // 7. Build list of non-hub nodes per product (nodes not assigned to themselves)
        for (int p = 0; p < nbProducts; p++) {
            int idx = 0;
            for (int j = 0; j < nbNodes; j++) {
                if (iter.x_best[p][j] != j) {  // not a hub for this product
                    nr.list_pnodes[p][idx++] = j;
                }
            }
        }

        // 8. Compute max_phubs and max_pnodes
        nr.max_phubs = 0;
        for (int p = 0; p < nbProducts; p++) {
            if (nr.phubs[p] > nr.max_phubs) {
                nr.max_phubs = nr.phubs[p];
            }
        }

        nr.max_pnodes = 0;
        for (int p = 0; p < nbProducts; p++) {
            if (nr.pnodes[p] > nr.max_pnodes) {
                nr.max_pnodes = nr.pnodes[p];
            }
        }

        return nr;
    }
}
