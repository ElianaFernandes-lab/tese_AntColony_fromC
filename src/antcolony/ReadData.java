package antcolony;

/**
 * Readdata.java
 * Translated from readdata.cpp + readdata.h
 * Original by Eliana Fernandes
 * Copyright (c) 2015 Eliana Fernandes. All rights reserved.
 */
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import antcolony.constants.AcoVar;

public class ReadData {
	
	private static final Logger log = LogManager.getLogger(ReadData.class);

    /**
     * Data structure holding the problem instance
     */
    public static class Data {
        public int nbNodes = 0;
        public int nbProducts = 0;
        public double[][][] w;  // w[i][j][p] = flow from i to j for product p
        public double[][] d;    // d[i][j] = distance between i and j
        public double[] g;      // g[i] = fixed cost to open hub at i
        public double[][] f;    // f[i][p] = fixed cost to dedicate hub i to product p
        public double[][] gamma;// Gamma[i][p] = capacity of hub i for product p
        public int[] L;         // L[i] = max products hub i can handle
        public double[] chi;    // chi[p] = collection cost factor for p
        public double[] delta;  // delta[p] = distribution cost factor for p
        public double[] alpha;  // alpha[p] = inter-hub transfer cost factor for p
        public double[][] O;    // O[i][p] = total outflow from i for p
        public double[][] D;    // D[i][p] = total inflow to i for p

        public Data(int nodes, int products) {
            this.nbNodes = nodes;
            this.nbProducts = products;

            w = new double[nodes][nodes][products];
            d = new double[nodes][nodes];
            g = new double[nodes];
            f = new double[nodes][products];
            gamma = new double[nodes][products];
            L = new int[nodes];
            chi = new double[products];
            delta = new double[products];
            alpha = new double[products];
            O = new double[nodes][products];
            D = new double[nodes][products];
        }
    }

    /**
     * Temporary point for coordinates
     */
    private static class Point {
        double x;
        double y;
    }

    /**
     * Reads the problem instance from file
     */
    public static Data readData(String fichIn) {
        Data dados = null;
        String input = AcoVar.INPUT_PATH + fichIn;
        try (Scanner scanner = new Scanner(new File(input))) {

            // Read nbNodes and nbProducts
            int nbNodes = scanner.nextInt();
            int nbProducts = scanner.nextInt();

            dados = new Data(nbNodes, nbProducts);

            // Read coordinates and compute distances
            Point[] coordenadas = new Point[nbNodes];
            for (int i = 0; i < nbNodes; i++) {
                coordenadas[i] = new Point();
                coordenadas[i].x = scanner.nextDouble();
                coordenadas[i].y = scanner.nextDouble();
            }

            // Compute Euclidean distances
            for (int i = 0; i < nbNodes; i++) {
                for (int j = 0; j < nbNodes; j++) {
                    double dx = coordenadas[i].x - coordenadas[j].x;
                    double dy = coordenadas[i].y - coordenadas[j].y;
                    dados.d[i][j] = Math.sqrt(dx * dx + dy * dy);
                }
            }

            // Read flows w[i][j][p]
            for (int i = 0; i < nbNodes; i++) {
                for (int j = 0; j < nbNodes; j++) {
                    for (int p = 0; p < nbProducts; p++) {
                        dados.w[i][j][p] = scanner.nextDouble();
                    }
                }
            }

            // Read chi, alpha, delta
            for (int p = 0; p < nbProducts; p++) {
                dados.chi[p] = scanner.nextDouble();
            }
            for (int p = 0; p < nbProducts; p++) {
                dados.alpha[p] = scanner.nextDouble();
            }
            for (int p = 0; p < nbProducts; p++) {
                dados.delta[p] = scanner.nextDouble();
            }

            // Read hub opening costs g[i]
            for (int i = 0; i < nbNodes; i++) {
                dados.g[i] = scanner.nextDouble();
            }

            // Read max products per hub L[i]
            for (int i = 0; i < nbNodes; i++) {
                dados.L[i] = scanner.nextInt();
            }

            // Read capacities Gamma[i][p]
            for (int i = 0; i < nbNodes; i++) {
                for (int p = 0; p < nbProducts; p++) {
                    dados.gamma[i][p] = scanner.nextDouble();
                    if (AcoVar.DAT_HIST) {
                    	log.info("dados.Gamma[{}][{}]= " +i, p,  dados.gamma[i][p]);
                    }
                }
            }

            // Read dedication costs f[i][p]
            for (int i = 0; i < nbNodes; i++) {
                for (int p = 0; p < nbProducts; p++) {
                    dados.f[i][p] = scanner.nextDouble();
                }
            }

            // Update g[i] as per paper: average 2 * sum f[i][p] / nbProducts
            for (int i = 0; i < nbNodes; i++) {
                double sumF = 0.0;
                for (int p = 0; p < nbProducts; p++) {
                    sumF += dados.f[i][p];
                }
                dados.g[i] = 2 * sumF / nbProducts;
            }

            // Compute total outflows O[i][p]
            for (int i = 0; i < nbNodes; i++) {
                for (int p = 0; p < nbProducts; p++) {
                    dados.O[i][p] = 0.0;
                    for (int j = 0; j < nbNodes; j++) {
                        dados.O[i][p] += dados.w[i][j][p];
                    }
                    if (AcoVar.DAT_HIST) {
                    	log.info("dados.O[{}][{}]= " ,i,p, dados.O[i][p]);
                    }
                    
                }
            }

            // Compute total inflows D[i][p]
            for (int i = 0; i < nbNodes; i++) {
                for (int p = 0; p < nbProducts; p++) {
                    dados.D[i][p] = 0.0;
                    for (int j = 0; j < nbNodes; j++) {
                        dados.D[i][p] += dados.w[j][i][p];
                    }
                }
            }

        } catch (FileNotFoundException e) {
        	log.error("Input file could not be opened: {}", fichIn);
            System.exit(1);
        }

        return dados;
    }
}
