package antcolony.ortools;


import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import com.google.ortools.Loader;
import com.google.ortools.linearsolver.MPConstraint;
import com.google.ortools.linearsolver.MPObjective;
import com.google.ortools.linearsolver.MPSolver;
import com.google.ortools.linearsolver.MPVariable;

import antcolony.AcoVar;
import antcolony.GetSolutions;
import antcolony.GetSolutions.Solution;
import antcolony.ReadData.Data;

public class HiGHSMILP {

	public static void solve(Data dados, GetSolutions.Solution sol, double iter_cost,
			String fichIn, String fichOut) {

		Loader.loadNativeLibraries();

		int n = dados.nbNodes;
		int m = dados.nbProducts;

		long startTime = System.nanoTime();

		try {
			MPSolver solver = MPSolver.createSolver("HIGHS");
			if (solver == null) {
				System.err.println("HiGHS solver unavailable.");
				return;
			}

			// ==============================
			// Binary variables: x[i][k][p] and z[k]
			// ==============================
			MPVariable[][][] x = new MPVariable[n][n][m];
			for (int i = 0; i < n; i++)
				for (int k = 0; k < n; k++)
					for (int p = 0; p < m; p++)
						x[i][k][p] = solver.makeIntVar(0, 1, "x_" + i + "_" + k + "_" + p);

			MPVariable[] z = new MPVariable[n];
			for (int k = 0; k < n; k++)
				z[k] = solver.makeIntVar(0, 1, "z_" + k);

			// ==============================
			// Continuous variables: y[i][k][l][p]
			// ==============================
			MPVariable[][][][] y = new MPVariable[n][n][n][m];
			for (int i = 0; i < n; i++)
				for (int k = 0; k < n; k++)
					for (int l = 0; l < n; l++)
						for (int p = 0; p < m; p++)
							y[i][k][l][p] = solver.makeNumVar(0.0, Double.POSITIVE_INFINITY,
									"y_" + i + "_" + k + "_" + l + "_" + p);

			// ==============================
			// 1. Flow divergence constraints
			// outflow - inflow = O[i][p] * x[i][k][p] - sum_j w[i][j][p] * x[j][k][p]
			// ==============================
			for (int p = 0; p < m; p++) {
				for (int i = 0; i < n; i++) {
					for (int k = 0; k < n; k++) {
						MPConstraint c = solver.makeConstraint(0.0, 0.0,
								"FlowBalance_" + i + "_" + k + "_" + p);
						for (int l = 0; l < n; l++) {
							if (l != k) {
								c.setCoefficient(y[i][k][l][p], 1.0);  // outflow
								c.setCoefficient(y[i][l][k][p], -1.0); // inflow
							}
						}
						double rhs = dados.O[i][p] * sol.x[i][k][p] - sumW(dados, sol, i, k, p, n);
						c.setBounds(-rhs, -rhs);
					}
				}
			}

			// ==============================
			// 2. Flow bounds
			// sum_{l≠k} y[i][k][l][p] <= O[i][p] * x[i][k][p]
			// ==============================
			for (int i = 0; i < n; i++)
				for (int k = 0; k < n; k++)
					for (int p = 0; p < m; p++) {
						MPConstraint c = solver.makeConstraint(Double.NEGATIVE_INFINITY,
								dados.O[i][p] * sol.x[i][k][p],
								"FlowBound_" + i + "_" + k + "_" + p);
						for (int l = 0; l < n; l++)
							if (l != k) c.setCoefficient(y[i][k][l][p], 1.0);
					}

			// ==============================
			// Objective
			// ==============================
			MPObjective obj = solver.objective();

			// Fixed hub opening costs
			for (int k = 0; k < n; k++)
				if (sol.z[k] == 1) obj.setCoefficient(z[k], dados.g[k]);

			// Product dedication costs
			for (int p = 0; p < m; p++)
				for (int k = 0; k < n; k++)
					if (sol.x[k][k][p] == 1) obj.setCoefficient(x[k][k][p], dados.f[k][p]);

			// Access costs
			for (int p = 0; p < m; p++)
				for (int i = 0; i < n; i++)
					for (int k = 0; k < n; k++)
						if (sol.x[i][k][p] == 1)
							obj.setCoefficient(x[i][k][p], dados.d[i][k] * (dados.chi[p] * dados.O[i][p] + dados.delta[p] * dados.D[i][p]));

			// Inter-hub transfer costs
			for (int p = 0; p < m; p++)
				for (int i = 0; i < n; i++)
					for (int k = 0; k < n; k++)
						for (int l = 0; l < n; l++)
							if (k != l) obj.setCoefficient(y[i][k][l][p], dados.alpha[p] * dados.d[k][l]);

			obj.setMinimization();

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
				for (int i = 0; i < n; i++)
					for (int k = 0; k < n; k++)
						for (int l = 0; l < n; l++)
							for (int p = 0; p < m; p++)
								if (k != l) {
									double val = y[i][k][l][p].solutionValue();
									if (val > 1e-6) sol.y[i][k][l][p] = val;
								}

				sol.cost = vOpt;
				iter_cost = vOpt;

				int nz = 0;
				for (int k = 0; k < n; k++) if (sol.z[k] == 1) nz++;
				appendToFile(fichOut, String.format("%12d  ", nz));
			}

			// Logging
			if (AcoVar.LOGG) {
				try (PrintWriter log = new PrintWriter(new FileWriter("output_highs.log", true))) {
					log.println(fichIn);
					log.println("MODEL - ACO + HiGHS PEK");
					log.printf("Optimal value : %.2f%n", vOpt);
					log.printf("CPU           : %.2f seconds%n%n", tOpt);
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
			sum += dados.w[i][j][p] * sol.x[j][k][p];
		return sum;
	}

	private static void appendToFile(String file, String text) {
		try (PrintWriter out = new PrintWriter(new FileWriter(file, true))) {
			out.println(text);
		} catch (IOException e) {
			System.err.println("Failed to write to " + file);
		}
	}
}

