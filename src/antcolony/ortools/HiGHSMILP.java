package antcolony.ortools;


import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.ortools.Loader;
import com.google.ortools.linearsolver.MPConstraint;
import com.google.ortools.linearsolver.MPObjective;
import com.google.ortools.linearsolver.MPSolver;
import com.google.ortools.linearsolver.MPVariable;

import antcolony.GetSolutions;
import antcolony.RunAco;
import antcolony.GetSolutions.Solution;
import antcolony.ReadData.Data;
import antcolony.constants.AcoVar;

public class HiGHSMILP {

	private static final Logger log = LogManager.getLogger(HiGHSMILP.class);

	private HiGHSMILP() {
		// default constructor
	}

	public static void solve(Data dados, GetSolutions.Solution sol, double iter_cost,
			String fichIn, String fichOut) {

		Loader.loadNativeLibraries();

		int nbNodes = dados.nbNodes;
		int nbProds = dados.nbProducts;

		long startTime = System.nanoTime();

		try {
			MPSolver solver = MPSolver.createSolver(AcoVar.SOLVER_ID);
			if (solver == null) {
				log.error("HiGHS solver unavailable.");
				return;
			}

			// ==============================
			// Binary variables: x[p][i][j] and z[j]
			// ==============================
			MPVariable[][][] x = new MPVariable[nbProds][nbNodes][nbNodes];
			for (int i = 0; i < nbNodes; i++)
				for (int j = 0; j < nbNodes; j++)
					for (int p = 0; p < nbProds; p++)
						x[p][i][j] = solver.makeIntVar(0, 1, "x_" + p + "_" + i + "_" + j);

			MPVariable[] z = new MPVariable[nbNodes];
			for (int j = 0; j < nbNodes; j++)
				z[j] = solver.makeIntVar(0, 1, "z_" + j);

			// ==============================
			// Continuous variables: y[p][i][k][l]
			// ==============================
			MPVariable[][][][] y = new MPVariable[nbProds][nbNodes][nbNodes][nbNodes];
			for (int i = 0; i < nbNodes; i++)
				for (int k = 0; k < nbNodes; k++)
					for (int l = 0; l < nbNodes; l++)
						for (int p = 0; p < nbProds; p++)
							y[p][i][k][l] = solver.makeNumVar(0.0, Double.POSITIVE_INFINITY,
									"y_" + p + "_" + i + "_" + k + "_" + l);

			// ==============================
			// 1. Flow divergence constraints
			// outflow - inflow = O[i][p] * x[[p]i][k] - sum_j w[i][j][p] * x[p][j][k]
			// ==============================
			for (int p = 0; p < nbProds; p++) {
				for (int i = 0; i < nbNodes; i++) {
					for (int j = 0; j < nbNodes; j++) {
						MPConstraint c = solver.makeConstraint(0.0, 0.0,
								"FlowBalance_" + p + "_" + i + "_" + j);
						for (int l = 0; l < nbNodes; l++) {
							if (l != j) {
								c.setCoefficient(y[p][i][j][l], 1.0);  // outflow
								c.setCoefficient(y[p][i][l][j], -1.0); // inflow
							}
						}
						double rhs = dados.O[i][p] * sol.x[p][i][j] - sumW(dados, sol, i, j, p, nbNodes);
						c.setBounds(-rhs, -rhs);
					}
				}
			}

			// ==============================
			// 2. Flow bounds
			// sum_{l≠k} y[i][k][l][p] <= O[i][p] * x[i][k][p]
			// ==============================
			for (int i = 0; i < nbNodes; i++)
				for (int j = 0; j < nbNodes; j++)
					for (int p = 0; p < nbProds; p++) {
						MPConstraint c = solver.makeConstraint(Double.NEGATIVE_INFINITY,
								dados.O[i][p] * sol.x[i][j][p],
								"FlowBound_" + p + "_" + j + "_" + i);
						for (int l = 0; l < nbNodes; l++)
							if (l != j) c.setCoefficient(y[p][i][j][l], 1.0);
					}

			// ==============================
			// Objective
			// ==============================
			MPObjective obj = solver.objective();

			// Fixed hub opening costs
			for (int j = 0; j < nbNodes; j++)
				if (sol.z[j] == 1) obj.setCoefficient(z[j], dados.g[j]);

			// Product dedication costs
			for (int p = 0; p < nbProds; p++)
				for (int j = 0; j < nbNodes; j++)
					if (sol.x[p][j][j] == 1) obj.setCoefficient(x[p][j][j], dados.f[j][p]);

			// Access costs
			for (int p = 0; p < nbProds; p++)
				for (int i = 0; i < nbNodes; i++)
					for (int j = 0; j < nbNodes; j++)
						if (sol.x[i][j][p] == 1)
							obj.setCoefficient(x[p][i][j], dados.d[i][j] * (dados.chi[p] * dados.O[i][p] + dados.delta[p] * dados.D[i][p]));

			// Inter-hub transfer costs
			for (int p = 0; p < nbProds; p++)
				for (int i = 0; i < nbNodes; i++)
					for (int k = 0; k < nbNodes; k++)
						for (int l = 0; l < nbNodes; l++)
							if (k != l) obj.setCoefficient(y[p][i][k][l], dados.alpha[p] * dados.d[k][l]);

			obj.setMinimization();
			
			// Set time limit (in mili seconds)
	        solver.setTimeLimit(AcoVar.MILP_MAX_TIME_MILLIS);

			// ==============================
			// Solve
			// ==============================
			MPSolver.ResultStatus status = solver.solve();
			long endTime = System.nanoTime();
			double tOpt = (endTime - startTime) / 1.0e9;
			double vOpt = (status == MPSolver.ResultStatus.OPTIMAL || status == MPSolver.ResultStatus.FEASIBLE)
					? obj.value() : Double.NaN;

			// ==============================
			// Extract y values
			// ==============================
			if (status == MPSolver.ResultStatus.OPTIMAL || status == MPSolver.ResultStatus.FEASIBLE) {
				for (int i = 0; i < nbNodes; i++)
					for (int k = 0; k < nbNodes; k++)
						for (int l = 0; l < nbNodes; l++)
							for (int p = 0; p < nbProds; p++)
								if (k != l) {
									double val = y[i][k][l][p].solutionValue();
									if (val > 1e-6) sol.y[i][k][l][p] = val;
								}

				sol.cost = vOpt;
				iter_cost = vOpt;

				int nz = 0;
				for (int k = 0; k < nbNodes; k++) if (sol.z[k] == 1) nz++;
				appendToFile(fichOut, String.format("%12d  ", nz));
			} else {
				log.error("STATUS {}", status);
				return;
			}

			// Logging
			if (AcoVar.LOGG) {
				try (PrintWriter pw = new PrintWriter(new FileWriter("output_highs.log", true))) {
					pw.println(fichIn);
					pw.println("MODEL - ACO + HiGHS PEK");
					pw.printf("Optimal value : %.2f%n", vOpt);
					pw.printf("CPU           : %.2f seconds%n%n", tOpt);
				}
			}

		} catch (Exception e) {
			System.err.println("HiGHS error: " + e.getMessage());
			appendToFile(fichOut, "model PEK               Error");
		}
	}

	private static double sumW(Data dados, Solution sol, int i, int k, int p, int n) {
		double sum = 0.0;
		for (int j = 0; j < n; j++)
			sum += dados.w[i][j][p] * sol.x[p][j][k];
		return sum;
	}

	private static void appendToFile(String file, String text) {
		try (PrintWriter out = new PrintWriter(new FileWriter(file, true))) {
			out.println(text);
		} catch (IOException e) {
			log.error("Failed to write to {}", file);
		}
	}
}

