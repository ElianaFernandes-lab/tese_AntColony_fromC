package antcolony;

import java.util.Arrays;

/**
 * Counters.java
 * Translated from counting.h (struct counters)
 */
public class Counters {
    public int[] phubs;        // number of dedicated hubs per product
    public int[] pnodes;       // number of allocated (non-hub) nodes per product
    public int[] pprods;       // number of products handled by each hub
    public int[] connects;     // total connections to each hub (across all products, including self)
    public int[][] pconnects;  // connections per product to each hub (including self)
    public int[][] list_phubs; // list of hubs for each product
    public int[][] list_pnodes;// list of non-hub nodes for each product (may contain bugs in original)
    public int max_phubs;
    public int max_pnodes;

    public Counters(int nbProducts, int nbNodes) {
        this.phubs = new int[nbProducts];
        this.pnodes = new int[nbProducts];
        this.pprods = new int[nbNodes];
        this.connects = new int[nbNodes];
        this.pconnects = new int[nbProducts][nbNodes];
        this.list_phubs = new int[nbProducts][nbNodes];
        this.list_pnodes = new int[nbProducts][nbNodes];
        this.max_phubs = 0;
        this.max_pnodes = 0;
    }

    public static Counters counting(int nr_products, int nr_nodes, Iteration iter) {
        Counters nr = new Counters(nr_products, nr_nodes);

        // Total number of dedicated hubs to each product
        nr.phubs = new int[nr_products];
        for (int p = 0; p < nr_products; p++) {
            nr.phubs[p] = 0;
        }

        // Count dedicated hubs per product: a node j is a dedicated hub for product p if x_best[j][p] == j
        for (int p = 0; p < nr_products; p++) {
            for (int j = 0; j < nr_nodes; j++) {
                if (iter.x_best[p][j] == j) {
                    nr.phubs[p]++;
                }
            }
        }

        // Total nodes assigned (non-dedicated) per product
        nr.pnodes = new int[nr_products];
        for (int p = 0; p < nr_products; p++) {
            nr.pnodes[p] = nr_nodes - nr.phubs[p];
        }

        // Find max_phubs
        nr.max_phubs = 0;
        for (int p = 0; p < nr_products; p++) {
            if (nr.phubs[p] > nr.max_phubs) {
                nr.max_phubs = nr.phubs[p];
            }
        }

        // Find max_pnodes
        nr.max_pnodes = 0;
        for (int p = 0; p < nr_products; p++) {
            if (nr.pnodes[p] > nr.max_pnodes) {
                nr.max_pnodes = nr.pnodes[p];
            }
        }

        // Number of products handled by each hub/node
        nr.pprods = new int[nr_nodes];
        for (int j = 0; j < nr_nodes; j++) {
            for (int p = 0; p < nr_products; p++) {
                if (iter.x_best[p][j] == j) {
                    nr.pprods[j]++;
                }
            }
        }

        // Total connections to each node (including self if it's a hub for some product)
        nr.connects = new int[nr_nodes];
        for (int j = 0; j < nr_nodes; j++) {
            for (int p = 0; p < nr_products; p++) {
                if (iter.x_best[p][j]== j) {
                    nr.connects[j]++;
                }
            }
        }

        // Product-specific connections to each node
        nr.pconnects = new int[nr_products][nr_nodes];
        for (int p = 0; p < nr_products; p++) {
            for (int j = 0; j < nr_nodes; j++) {
                if (iter.x_best[p][j] == j) {
                    // Count how many nodes assign product p to hub j
                    for (int i = 0; i < nr_nodes; i++) {
                        if (iter.x_best[p][i] == j) {
                            nr.pconnects[p][j]++;
                        }
                    }
                }
            }
        }

        // List of dedicated hubs per product (only hubs with >0 connections for that product)
        nr.list_phubs = new int[nr_products][nr_nodes];
        for (int p = 0; p < nr_products; p++) {
            Arrays.fill(nr.list_phubs[p], -1);  // initialize with -1
        }

        for (int p = 0; p < nr_products; p++) {
            int l = 0;
            for (int j = 0; j < nr_nodes; j++) {
                if (nr.pconnects[p][j] > 0) {
                    nr.list_phubs[p][l++] = j;
                }
            }
        }

        // List of non-hub nodes per product (nodes with 0 connections for that product)
        nr.list_pnodes = new int[nr_products][nr_nodes];
        for (int p = 0; p < nr_products; p++) {
            Arrays.fill(nr.list_pnodes[p], -1);
        }

        for (int p = 0; p < nr_products; p++) {
            int l = 0;
            for (int j = 0; j < nr_nodes; j++) {
                if (nr.pconnects[p][j] == 0) {
                    nr.list_pnodes[p][l++] = j;
                }
            }
        }

        return nr;
    }
}
