package antcolony.ortools;

import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import com.google.ortools.Loader;
import com.google.ortools.linearsolver.MPConstraint;
import com.google.ortools.linearsolver.MPObjective;
import com.google.ortools.linearsolver.MPSolver;
import com.google.ortools.linearsolver.MPVariable;

import antcolony.Aco;
import antcolony.ReadData.Data;

public class HiGHSLR {

    public static Aco run(double timeLR, double sclPrm, Data dados, Aco aParam) {

        Loader.loadNativeLibraries();

        // Start timer
        long startTime = System.nanoTime();

        // Create HiGHS solver
        MPSolver solver = MPSolver.createSolver("HIGHS");
        if (solver == null) {
            System.err.println("HiGHS solver unavailable.");
            return aParam;
        }

        int nbNodes = dados.nbNodes;
        int nbProducts = dados.nbProducts;

        // ==============================
        // Variables
        // ==============================
        MPVariable[][][] x = new MPVariable[nbNodes][nbNodes][nbProducts];
        for (int i = 0; i < nbNodes; i++) {
            for (int k = 0; k < nbNodes; k++) {
                for (int p = 0; p < nbProducts; p++) {
                    x[i][k][p] = solver.makeNumVar(0.0, 1.0, "x_" + i + "_" + k + "_" + p);
                }
            }
        }

        MPVariable[] z = new MPVariable[nbNodes];
        for (int k = 0; k < nbNodes; k++) {
            z[k] = solver.makeNumVar(0.0, 1.0, "z_" + k);
        }

        MPVariable[][][][] y = new MPVariable[nbNodes][nbNodes][nbNodes][nbProducts];
        for (int i = 0; i < nbNodes; i++) {
            for (int k = 0; k < nbNodes; k++) {
                for (int l = 0; l < nbNodes; l++) {
                    for (int p = 0; p < nbProducts; p++) {
                        y[i][k][l][p] = solver.makeNumVar(0.0, Double.POSITIVE_INFINITY,
                                "y_" + i + "_" + k + "_" + l + "_" + p);
                    }
                }
            }
        }

        try {
            // ==============================
            // 1. Single allocation constraint
            // ==============================
            for (int i = 0; i < nbNodes; i++) {
                for (int p = 0; p < nbProducts; p++) {
                    MPConstraint c = solver.makeConstraint(1.0, 1.0, "SingleAlloc_" + i + "_" + p);
                    for (int k = 0; k < nbNodes; k++) {
                        c.setCoefficient(x[i][k][p], 1.0);
                    }
                }
            }

            // ==============================
            // 2. Link allocation constraints
            // x[i][k][p] <= x[k][k][p]
            // ==============================
            for (int i = 0; i < nbNodes; i++) {
                for (int k = 0; k < nbNodes; k++) {
                    for (int p = 0; p < nbProducts; p++) {
                        MPConstraint c = solver.makeConstraint(Double.NEGATIVE_INFINITY, 0.0,
                                "LinkAlloc_" + i + "_" + k + "_" + p);
                        c.setCoefficient(x[i][k][p], 1.0);
                        c.setCoefficient(x[k][k][p], -1.0);
                    }
                }
            }

            // ==============================
            // 3. Maximum products at hub
            // sum_p x[k][k][p] <= L[k] * z[k]
            // ==============================
            for (int k = 0; k < nbNodes; k++) {
                MPConstraint c = solver.makeConstraint(Double.NEGATIVE_INFINITY, 0.0, "MaxProdAtHub_" + k);
                for (int p = 0; p < nbProducts; p++) {
                    c.setCoefficient(x[k][k][p], 1.0);
                }
                c.setCoefficient(z[k], -dados.L[k]);
            }

            // ==============================
            // 4. Flow balance constraints
            // ==============================
            for (int p = 0; p < nbProducts; p++) {
                for (int i = 0; i < nbNodes; i++) {
                    for (int k = 0; k < nbNodes; k++) {
                        MPConstraint c = solver.makeConstraint(0.0, 0.0, "FlowBalance_" + i + "_" + k + "_" + p);
                        for (int l = 0; l < nbNodes; l++) {
                            c.setCoefficient(y[i][k][l][p], 1.0);      // outflow
                            c.setCoefficient(y[i][l][k][p], -1.0);     // inflow
                        }
                        c.setCoefficient(x[i][k][p], -dados.O[i][p]);
                        for (int j = 0; j < nbNodes; j++) {
                            c.setCoefficient(x[j][k][p], dados.w[i][j][p]);
                        }
                    }
                }
            }

            // ==============================
            // 5. Flow bounds
            // y[i][k][l][p] <= O[i][p] * x[i][k][p] for l != k
            // ==============================
            for (int i = 0; i < nbNodes; i++) {
                for (int k = 0; k < nbNodes; k++) {
                    for (int p = 0; p < nbProducts; p++) {
                        MPConstraint c = solver.makeConstraint(Double.NEGATIVE_INFINITY, dados.O[i][p], 
                                "FlowBound_" + i + "_" + k + "_" + p);
                        for (int l = 0; l < nbNodes; l++) {
                            if (l != k) c.setCoefficient(y[i][k][l][p], 1.0);
                        }
                        c.setCoefficient(x[i][k][p], -dados.O[i][p]);
                    }
                }
            }

            // ==============================
            // 6. Capacity per hub
            // sum_i O[i][p] * x[i][k][p] <= gamma[k][p] * x[k][k][p]
            // ==============================
            for (int k = 0; k < nbNodes; k++) {
                for (int p = 0; p < nbProducts; p++) {
                    MPConstraint c = solver.makeConstraint(Double.NEGATIVE_INFINITY, 0.0, 
                            "Capacity_" + k + "_" + p);
                    for (int i = 0; i < nbNodes; i++) {
                        c.setCoefficient(x[i][k][p], dados.O[i][p]);
                    }
                    c.setCoefficient(x[k][k][p], -dados.gamma[k][p]);
                }
            }

            // ==============================
            // 7. Minimum hubs per product
            // ==============================
            for (int p = 0; p < nbProducts; p++) {
                double prodDemand = 0.0;
                for (int i = 0; i < nbNodes; i++) prodDemand += dados.O[i][p];

                List<Integer> candidateHubs = new ArrayList<>();
                for (int i = 0; i < nbNodes; i++) candidateHubs.add(i);

                double totalCovered = 0.0;
                int R = 0;
                while (totalCovered < prodDemand && !candidateHubs.isEmpty()) {
                    double bestGamma = -1.0;
                    int bestIdx = -1;
                    for (int idx : candidateHubs) {
                        int i = idx;
                        if (dados.gamma[i][p] > bestGamma && dados.O[i][p] < dados.gamma[i][p]) {
                            bestGamma = dados.gamma[i][p];
                            bestIdx = i;
                        }
                    }
                    if (bestIdx == -1) break;
                    totalCovered += bestGamma;
                    candidateHubs.remove(Integer.valueOf(bestIdx));
                    R++;
                }

                MPConstraint sumHubsP = solver.makeConstraint(R, Double.POSITIVE_INFINITY, "MinHubsProd_" + p);
                for (int k = 0; k < nbNodes; k++) {
                    sumHubsP.setCoefficient(x[k][k][p], 1.0);
                }
            }

            // ==============================
            // Objective function
            // ==============================
            MPObjective obj = solver.objective();
            for (int p = 0; p < nbProducts; p++) {
                for (int i = 0; i < nbNodes; i++) {
                    for (int k = 0; k < nbNodes; k++) {
                        double coeff = dados.d[i][k] * (dados.chi[p] * dados.O[i][p] + dados.delta[p] * dados.D[i][p]);
                        obj.setCoefficient(x[i][k][p], coeff);
                        for (int l = 0; l < nbNodes; l++) {
                            obj.setCoefficient(y[i][k][l][p], dados.alpha[p] * dados.d[k][l]);
                        }
                    }
                }
            }
            for (int k = 0; k < nbNodes; k++) {
                obj.setCoefficient(z[k], dados.g[k]);
                for (int p = 0; p < nbProducts; p++) {
                    obj.setCoefficient(x[k][k][p], dados.f[k][p]);
                }
            }
            obj.setMinimization();

            // ==============================
            // Solve
            // ==============================
            MPSolver.ResultStatus resultStatus = solver.solve();

            long endTime = System.nanoTime();
            double runtimeSec = (endTime - startTime) / 1.0e9;

            if (resultStatus == MPSolver.ResultStatus.OPTIMAL || resultStatus == MPSolver.ResultStatus.FEASIBLE) {
                sclPrm = obj.value();
                timeLR = runtimeSec;

                System.out.println("Solution found. Objective = " + obj.value() + " Runtime = " + runtimeSec + "s");

                // Copy solution to tau0 for ACO
                for (int i = 0; i < nbNodes; i++) {
                    for (int k = 0; k < nbNodes; k++) {
                        for (int p = 0; p < nbProducts; p++) {
                            aParam.tau0[i][k][p] = x[i][k][p].solutionValue();
                        }
                    }
                }

                try (PrintWriter writer = new PrintWriter(new FileWriter("history1.txt", true))) {
                    writer.println("HiGHS LR for TAU0 COMPUTED");
                }

            } else {
                System.out.println("No solution found.");
            }

        } catch (Exception e) {
            e.printStackTrace();
            try (PrintWriter err = new PrintWriter(new FileWriter("HiGHS_LR_ERROR.txt", true))) {
                err.println(e.getMessage());
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        return aParam;
    }
}
