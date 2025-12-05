//package antcolony;
//
//import java.io.FileWriter;
//import java.io.PrintWriter;
//import java.util.ArrayList;
//import java.util.List;
//
//import antcolony.ReadData.Data;
//import ilog.concert.*;
//import ilog.cplex.*;
//
//public class CplexLRRunner {
//
//    public static Aco run(double timeLR, double sclPrm, Data dados, Aco aParam) throws IloException {
//
//        IloCplex cplex = new IloCplex();
//        IloTimer timer = new IloTimer(cplex);
//
//        double scl_prm;
//		double time_LR;
//		try {
//            // Suppress CPLEX output (optional)
//            cplex.setOut(null);
//
//            // CPLEX parameters
//            cplex.setParam(IloCplex.Param.TimeLimit, 10800);     // 3 hours wall clock
//            cplex.setParam(IloCplex.Param.MIP.Tolerances.MIPGap, 1e-5);
//            cplex.setParam(IloCplex.Param.Emphasis.Numerical, true);
//
//            int nbNodes = dados.nbNodes;
//            int nbProducts = dados.nbProducts;
//
//            // Variables x[i][k][p] : allocation of node i to hub k for product p
//            IloNumVar[][][] x = new IloNumVar[nbNodes][][];
//            for (int i = 0; i < nbNodes; i++) {
//                x[i] = new IloNumVar[nbNodes][];
//                for (int k = 0; k < nbNodes; k++) {
//                    x[i][k] = cplex.numVarArray(nbProducts, 0.0, 1.0, IloNumVarType.Float);
//                }
//            }
//
//            // Variables z[k] : whether node k is open as a hub
//            IloNumVar[] z = cplex.numVarArray(nbNodes, 0.0, 1.0, IloNumVarType.Float);
//
//            // Variables y[i][k][l][p] : flow from i to l via hubs k->l for product p
//            IloNumVar[][][][] y = new IloNumVar[nbNodes][][][];
//            for (int i = 0; i < nbNodes; i++) {
//                y[i] = new IloNumVar[nbNodes][][];
//                for (int k = 0; k < nbNodes; k++) {
//                    y[i][k] = new IloNumVar[nbNodes][];
//                    for (int l = 0; l < nbNodes; l++) {
//                        y[i][k][l] = cplex.numVarArray(nbProducts, 0.0, Double.POSITIVE_INFINITY, IloNumVarType.Float);
//                    }
//                }
//            }
//
//            IloLinearNumExpr obj = cplex.linearNumExpr();
//
//            // ================================================
//            // 1. Single allocation constraint
//            // ================================================
//            for (int i = 0; i < nbNodes; i++) {
//                for (int p = 0; p < nbProducts; p++) {
//                    IloLinearNumExpr sumX = cplex.linearNumExpr();
//                    for (int k = 0; k < nbNodes; k++) {
//                        sumX.addTerm(1.0, x[i][k][p]);
//                    }
//                    cplex.addEq(sumX, 1.0, "SingleAlloc_i" + i + "_p" + p);
//                }
//            }
//
//            // ================================================
//            // 2. Non-hub nodes can only be allocated to open hubs
//            // ================================================
//            for (int i = 0; i < nbNodes; i++) {
//                for (int k = 0; k < nbNodes; k++) {
//                    for (int p = 0; p < nbProducts; p++) {
//                        cplex.addLe(x[i][k][p], x[k][k][p], "LinkAlloc_i" + i + "_k" + k + "_p" + p);
//                    }
//                }
//            }
//
//            // ================================================
//            // 3. Maximum number of products handled at each hub (depends on L[k])
//            // ================================================
//            for (int k = 0; k < nbNodes; k++) {
//                IloLinearNumExpr sumSelf = cplex.linearNumExpr();
//                for (int p = 0; p < nbProducts; p++) {
//                    sumSelf.addTerm(1.0, x[k][k][p]);
//                }
//                cplex.addLe(sumSelf, cplex.prod(dados.L[k], z[k]), "MaxProdAtHub_k" + k);
//            }
//
//            // ================================================
//            // 4. Flow balance (divergence) constraints
//            // ================================================
//            for (int p = 0; p < nbProducts; p++) {
//                for (int i = 0; i < nbNodes; i++) {
//                    for (int k = 0; k < nbNodes; k++) {
//                        IloLinearNumExpr outflow = cplex.linearNumExpr();
//                        IloLinearNumExpr inflow = cplex.linearNumExpr();
//                        for (int l = 0; l < nbNodes; l++) {
//                            outflow.addTerm(1.0, y[i][k][l][p]);
//                            inflow.addTerm(-1.0, y[i][l][k][p]);
//                        }
//
//                        IloLinearNumExpr rhs = cplex.linearNumExpr();
//                        rhs.addTerm(dados.O[i][p], x[i][k][p]);
//                        for (int j = 0; j < nbNodes; j++) {
//                            rhs.addTerm(-dados.w[i][j][p], x[j][k][p]);
//                        }
//
//                        cplex.addEq(cplex.sum(outflow, inflow), rhs, "FlowBalance_i" + i + "_k" + k + "_p" + p);
//                    }
//                }
//            }
//
//            // ================================================
//            // 5. Missing cuts: flow only if allocated
//            // ================================================
//            for (int i = 0; i < nbNodes; i++) {
//                for (int k = 0; k < nbNodes; k++) {
//                    for (int p = 0; p < nbProducts; p++) {
//                        IloLinearNumExpr flowOut = cplex.linearNumExpr();
//                        for (int l = 0; l < nbNodes; l++) {
//                            if (l != k) {
//                                flowOut.addTerm(1.0, y[i][k][l][p]);
//                            }
//                        }
//                        cplex.addLe(flowOut, cplex.prod(dados.O[i][p], x[i][k][p]),
//                                "FlowBound_i" + i + "_k" + k + "_p" + p);
//                    }
//                }
//            }
//
//            // ================================================
//            // 6. Enhanced capacity constraints per hub and product
//            // ================================================
//            for (int k = 0; k < nbNodes; k++) {
//                for (int p = 0; p < nbProducts; p++) {
//                    IloLinearNumExpr load = cplex.linearNumExpr();
//                    for (int i = 0; i < nbNodes; i++) {
//                        load.addTerm(dados.O[i][p], x[i][k][p]);
//                    }
//                    cplex.addLe(load, cplex.prod(dados.gamma[k][p], x[k][k][p]),
//                            "Capacity_k" + k + "_p" + p);
//                }
//            }
//
//            // ================================================
//            // 7. Minimum number of hubs per product (lower bound computation)
//            // ================================================
//            int maxR = 0;
//            int sumR = 0;
//            boolean allLEqual = true;
//            for (int k = 1; k < nbNodes; k++) {
//                if (dados.L[k] != dados.L[0]) {
//                    allLEqual = false;
//                    break;
//                }
//            }
//
//            for (int p = 0; p < nbProducts; p++) {
//                double prodDemand = 0.0;
//                for (int i = 0; i < nbNodes; i++) {
//                    prodDemand += dados.O[i][p];
//                }
//
//                List<Integer> candidateHubs = new ArrayList<>();
//                for (int i = 0; i < nbNodes; i++) candidateHubs.add(i);
//
//                double totalCovered = 0.0;
//                int R = 0;
//
//                while (totalCovered < prodDemand && !candidateHubs.isEmpty()) {
//                    double bestGamma = -1.0;
//                    int bestIdx = -1;
//
//                    for (int idx : candidateHubs) {
//                        int i = idx;
//                        if (dados.gamma[i][p] > bestGamma && dados.O[i][p] < dados.gamma[i][p]) {
//                            bestGamma = dados.gamma[i][p];
//                            bestIdx = i;
//                        }
//                    }
//
//                    if (bestIdx == -1) break;
//
//                    totalCovered += bestGamma;
//                    candidateHubs.remove(Integer.valueOf(bestIdx));
//                    R++;
//                }
//
//                IloLinearNumExpr sumHubsP = cplex.linearNumExpr();
//                for (int k = 0; k < nbNodes; k++) {
//                    sumHubsP.addTerm(1.0, x[k][k][p]);
//                }
//                cplex.addGe(sumHubsP, R, "MinHubsProd_p" + p);
//
//                maxR = Math.max(maxR, R);
//                sumR += R;
//            }
//
//            // Global minimum number of hubs
//            IloLinearNumExpr totalHubs = cplex.sum(z);
//            if (!allLEqual) {
//                cplex.addGe(totalHubs, maxR, "MinTotalHubs_VarL");
//            } else {
//                int minFromAvg = (int) Math.ceil((double) sumR / dados.L[0]);
//                cplex.addGe(totalHubs, Math.max(maxR, minFromAvg), "MinTotalHubs_FixedL");
//            }
//
//            // ================================================
//            // Objective Function
//            // ================================================
//            for (int p = 0; p < nbProducts; p++) {
//                for (int i = 0; i < nbNodes; i++) {
//                    for (int k = 0; k < nbNodes; k++) {
//                        double coeff = dados.d[i][k] * (dados.chi[p] * dados.O[i][p] + dados.delta[p] * dados.D[i][p]);
//                        obj.addTerm(coeff, x[i][k][p]);
//                    }
//                }
//            }
//
//            for (int p = 0; p < nbProducts; p++) {
//                for (int i = 0; i < nbNodes; i++) {
//                    for (int k = 0; k < nbNodes; k++) {
//                        for (int l = 0; l < nbNodes; l++) {
//                            double coeff = dados.alpha[p] * dados.d[k][l];
//                            obj.addTerm(coeff, y[i][k][l][p]);
//                        }
//                    }
//                }
//            }
//
//            for (int k = 0; k < nbNodes; k++) {
//                obj.addTerm(dados.g[k], z[k]);
//            }
//
//            for (int p = 0; p < nbProducts; p++) {
//                for (int k = 0; k < nbNodes; k++) {
//                    obj.addTerm(dados.f[k][p], x[k][k][p]);
//                }
//            }
//
//            cplex.addMinimize(obj);
//
//            // ================================================
//            // Solve
//            // ================================================
//            timer.restart();
//            boolean solved = cplex.solve();
//            timer.stop();
//
//            if (solved) {
//                double vOpt = cplex.getObjValue();
//                double tOpt = timer.getTime();
//
//                scl_prm = vOpt;
//                time_LR = tOpt;
//
//                System.out.println("scl_prm = " + scl_prm);
//
//                // Copy solution to tau0 (for ACO pheromone update)
//                for (int i = 0; i < nbNodes; i++) {
//                    for (int k = 0; k < nbNodes; k++) {
//                        for (int p = 0; p < nbProducts; p++) {
//                            aParam.tau0[i][k][p] = cplex.getValue(x[i][k][p]);
//                        }
//                    }
//                }
//
//                // Optional: write history
//                try (PrintWriter writer = new PrintWriter(new FileWriter("history1.txt", true))) {
//                    writer.println("CPLEX LR for TAU0 COMPUTED");
//                }
//            } else {
//                System.out.println("CPLEX did not find a solution.");
//            }
//
//        } catch (IloException e) {
//            timer.stop();
//            System.err.println("CPLEX Error: " + e.getMessage());
//            e.printStackTrace();
//            try (PrintWriter err = new PrintWriter(new FileWriter("cplex_LR_ERROR.txt", true))) {
//                err.println("model PEK LR: " + e.getMessage());
//            }
//        } catch (Exception e) {
//            System.err.println("Unexpected error: " + e.getMessage());
//            e.printStackTrace();
//        } finally {
//            timeLR = time_LR;
//            sclPrm = scl_prm;
//            cplex.end();
//        }
//
//        return aParam;
//    }
//}
