//package antcolony;
//
//import java.io.FileWriter;
//import java.io.IOException;
//import java.io.PrintWriter;
//
//import antcolony.ReadData.Data;
///**
// * RunCplex.java
// * CPLEX refinement of ACO solution using the PEK model
// * Translated from runcplex.cpp + runcplex.h
// * Original by Eliana Fernandes
// * Copyright (c) 2015 Eliana Fernandes. All rights reserved.
// */
//import ilog.concert.*;
//import ilog.cplex.*;
//
//public class RunCplex {
//
//    /**
//     * Solves the PEK model using CPLEX to refine the ACO solution
//     */
//    public static void solve(Data dados, GetSolutions.Solution sol, double iter_cost,
//                             String fichIn, String fichOut) {
//
//        int n = dados.nbNodes;
//        int m = dados.nbProducts;
//
//        try (IloCplex cplex = new IloCplex()) {
//
//            // ===================================================================
//            // CPLEX Parameters
//            // ===================================================================
//            cplex.setParam(IloCplex.Param.TimeLimit, 10800);     // 3 hours
//            cplex.setParam(IloCplex.Param.ClockType, 1);         // CPU time
//            cplex.setOut(null);  // Suppress CPLEX output
//
//            IloTimer timer = new IloTimer(cplex);
//
//            // ===================================================================
//            // Decision Variables: y[i][k][l][p] (inter-hub flows)
//            // ===================================================================
//            IloNumVar[][][][] y = new IloNumVar[n][n][n][m];
//            for (int i = 0; i < n; i++) {
//                for (int k = 0; k < n; k++) {
//                    for (int l = 0; l < n; l++) {
//                        y[i][k][l] = new IloNumVar[m];
//                        for (int p = 0; p < m; p++) {
//                            y[i][k][l][p] = cplex.numVar(0, Double.POSITIVE_INFINITY, "y_" + i + "_" + k + "_" + l + "_" + p);
//                        }
//                    }
//                }
//            }
//
//            // ===================================================================
//            // Model PEK
//            // ===================================================================
//            IloLinearNumExpr obj = cplex.linearNumExpr();
//
//            // 1. Flow divergence constraints
//            for (int p = 0; p < m; p++) {
//                for (int i = 0; i < n; i++) {
//                    for (int k = 0; k < n; k++) {
//                        IloLinearNumExpr inflow = cplex.linearNumExpr();
//                        IloLinearNumExpr outflow = cplex.linearNumExpr();
//
//                        for (int l = 0; l < n; l++) {
//                            if (l != k) {
//                                inflow.addTerm(1.0, y[i][l][k][p]);
//                                outflow.addTerm(1.0, y[i][k][l][p]);
//                            }
//                        }
//
//                        double rhs = dados.O[i][p] * (sol.x[i][k][p] == 1 ? 1 : 0);
//                        for (int j = 0; j < n; j++) {
//                            rhs -= dados.w[i][j][p] * (sol.x[j][k][p] == 1 ? 1 : 0);
//                        }
//
//                        cplex.addEq(cplex.diff(outflow, inflow), rhs);
//                    }
//                }
//            }
//
//            // 2. Missing cuts: outflow from i to hubs ≠ k
//            for (int i = 0; i < n; i++) {
//                for (int k = 0; k < n; k++) {
//                    for (int p = 0; p < m; p++) {
//                        IloLinearNumExpr lhs = cplex.linearNumExpr();
//                        for (int l = 0; l < n; l++) {
//                            if (l != k) {
//                                lhs.addTerm(1.0, y[i][k][l][p]);
//                            }
//                        }
//                        double rhs = dados.O[i][p] * (sol.x[i][k][p] == 1 ? 1 : 0);
//                        cplex.addLe(lhs, rhs);
//                    }
//                }
//            }
//
//            // 3. Objective function
//            // Fixed hub opening costs
//            for (int k = 0; k < n; k++) {
//                if (sol.z[k] == 1) {
//                    obj.addTerm(dados.g[k], 1.0);
//                }
//            }
//
//            // Product dedication costs
//            for (int p = 0; p < m; p++) {
//                for (int k = 0; k < n; k++) {
//                    if (sol.x[k][k][p] == 1) {
//                        obj.addTerm(dados.f[k][p], 1.0);
//                    }
//                }
//            }
//
//            // Access costs (fixed by ACO x)
//            for (int p = 0; p < m; p++) {
//                for (int i = 0; i < n; i++) {
//                    for (int k = 0; k < n; k++) {
//                        if (sol.x[i][k][p] == 1) {
//                            double cost = dados.d[i][k] * (dados.chi[p] * dados.O[i][p] + dados.delta[p] * dados.D[i][p]);
//                            obj.addTerm(cost, 1.0);
//                        }
//                    }
//                }
//            }
//
//            // Inter-hub transfer costs (variable)
//            for (int p = 0; p < m; p++) {
//                for (int i = 0; i < n; i++) {
//                    for (int k = 0; k < n; k++) {
//                        for (int l = 0; l < n; l++) {
//                            if (k != l) {
//                                obj.addTerm(dados.alpha[p] * dados.d[k][l], y[i][k][l][p]);
//                            }
//                        }
//                    }
//                }
//            }
//
//            cplex.addMinimize(obj);
//
//            // ===================================================================
//            // Solve
//            // ===================================================================
//            timer.start();
//            boolean solved = cplex.solve();
//            timer.stop();
//
//            double vOpt = solved ? cplex.getObjValue() : Double.NaN;
//            double tOpt = timer.getTime();
//
//            // ===================================================================
//            // Extract y values
//            // ===================================================================
//            if (solved) {
//                for (int i = 0; i < n; i++) {
//                    for (int k = 0; k < n; k++) {
//                        for (int l = 0; l < n; l++) {
//                            for (int p = 0; p < m; p++) {
//                                if (k != l) {
//                                    double val = cplex.getValue(y[i][k][l][p]);
//                                    if (val > 1e-6) {
//                                        sol.y[i][k][l][p] = val;
//                                    }
//                                }
//                            }
//                        }
//                    }
//                }
//
//                sol.cost = vOpt;
//                iter_cost = vOpt;
//
//                // Count opened hubs
//                int nz = 0;
//                for (int k = 0; k < n; k++) if (sol.z[k] == 1) nz++;
//                appendToFile(fichOut, String.format("%12d  ", nz));
//            }
//
//            // ===================================================================
//            // Logging
//            // ===================================================================
//            if (AcoVar.LOGG) {
//                try (PrintWriter log = new PrintWriter(new FileWriter("output_cplex.log", true))) {
//                    log.println(fichIn);
//                    log.println("MODEL - ACO + CPLEX PEK");
//                    log.printf("Optimal value : %.2f%n", vOpt);
//                    log.printf("CPU           : %.2f seconds%n%n", tOpt);
//
//                    log.println("Variables z=1:");
//                    for (int k = 0; k < n; k++) if (sol.z[k] == 1) log.println("z[" + k + "]");
//
//                    log.println("Variables x=1:");
//                    for (int i = 0; i < n; i++)
//                        for (int k = 0; k < n; k++)
//                            for (int p = 0; p < m; p++)
//                                if (sol.x[i][k][p] == 1)
//                                    log.println("x[" + i + "][" + k + "][" + p + "]");
//
//                    log.println("Variables y>0:");
//                    for (int i = 0; i < n; i++)
//                        for (int k = 0; k < n; k++)
//                            for (int l = 0; l < n; l++)
//                                for (int p = 0; p < m; p++)
//                                    if (k != l && sol.y[i][k][l][p] > 1e-6)
//                                        log.printf("y[%d][%d][%d][%d] = %.6f%n", i, k, l, p, sol.y[i][k][l][p]);
//                }
//            }
//
//        } catch (IloException e) {
//            System.err.println("CPLEX Error: " + e.getMessage());
//            appendToFile(fichOut, "model PEK               " + e.getMessage());
//        } catch (Exception e) {
//            System.err.println("Unexpected error: " + e.getMessage());
//            appendToFile(fichOut, "model PEK               Error");
//        }
//    }
//
//    private static void appendToFile(String file, String text) {
//        try (PrintWriter out = new PrintWriter(new FileWriter(file, true))) {
//            out.println(text);
//        } catch (IOException e) {
//            System.err.println("Failed to write to " + file);
//        }
//    }
//}
