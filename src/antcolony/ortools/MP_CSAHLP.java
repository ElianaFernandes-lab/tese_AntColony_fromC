package antcolony.ortools;

import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.ortools.Loader;
import com.google.ortools.linearsolver.MPConstraint;
import com.google.ortools.linearsolver.MPObjective;
import com.google.ortools.linearsolver.MPSolver;
import com.google.ortools.linearsolver.MPVariable;

import antcolony.Aco;
import antcolony.ReadData.Data;

public class MP_CSAHLP {

	private static final Logger log = LogManager.getLogger(MP_CSAHLP.class);
	
	private int nbProducts;
	private int nbNodes;
	MPVariable[][][] x;
	MPVariable[][][][] y;
	MPVariable[] z;

	public MP_CSAHLP(int nbProducts, int nbNodes) {
		this.nbProducts = nbProducts;
		this.nbNodes = nbNodes;
	}

	public Aco solve(Data data) {
		Aco aco = new Aco(this.nbProducts, this.nbNodes);
		Loader.loadNativeLibraries();

		// Start timer
		long startTime = System.nanoTime();

		// Create HiGHS solver
		String solverId = "SCIP";//AcoVar.SOLVER_ID;
		MPSolver solver = MPSolver.createSolver(solverId);
		if (solver == null) {
			log.error("{} solver unavailable.", solverId);
			return aco;
		} else {
			log.error("{} solver used.", solverId);
		}
		
		try {
			// ==============================
			// Variables
			// ==============================
			log.error("Creating variables...");
			createVars(solver);
			log.error("Variables created. Total variables: {}", solver.numVariables());
			
			// ==============================
			// Objective function (create BEFORE constraints)
			// ==============================
			log.error("Creating objective function...");
			MPObjective obj = createObjectiveFunction(solver, data);
			log.error("Objective function created.");
			
			// ==============================
			// 1. Single allocation constraint
			// ==============================
			log.error("Adding single allocation constraints...");
			addSingleAllocationConstraints(solver);
			log.error("Added single allocation constraints. Total constraints: {}", solver.numConstraints());

			// ==============================
			// 2. Link allocation constraints
			// x[p][i][k] <= x[p][k][k]
			// ==============================
			log.error("Adding link allocation constraints...");
			addLinkAllocationConstraints(solver);
			log.error("Added link allocation constraints. Total constraints: {}", solver.numConstraints());

			// ==============================
			// 3. Maximum products at hub
			// sum_p x[k][k][p] <= L[k] * z[k]
			// ==============================
			log.error("Adding max products at hub constraints...");
			addMaxProdsAtHubConstraints(solver, data);
			log.error("Added max products at hub constraints. Total constraints: {}", solver.numConstraints());

			// ==============================
			// 4. Flow balance constraints
			// ==============================
			log.error("Adding flow balance constraints...");
			addFlowBalanceConstraints(solver, data);
			log.error("Added flow balance constraints. Total constraints: {}", solver.numConstraints());

			// ==============================
			// 5. Flow bounds
			// y[p][i][k][l] <= O[i][p] * x[i][k][p] for l != k
			// ==============================
			log.error("Adding flow bounds constraints...");
			addFlowBoundsConstraints(solver, data);
			log.error("Added flow bounds constraints. Total constraints: {}", solver.numConstraints());

			// ==============================
			// 6. Capacity per hub
			// sum_i O[i][p] * x[i][k][p] <= gamma[k][p] * x[k][k][p]
			// ==============================
			log.error("Adding capacity constraints...");
			addCapacityConstraints(solver, data);
			log.error("Added capacity constraints. Total constraints: {}", solver.numConstraints());

			// ==============================
			// 7. Minimum hubs per product
			// ==============================
			log.error("Adding minimum hubs per product constraints...");
			addMinimumHubsPerProdConstraints(solver, data);
			log.error("Added minimum hubs per product constraints. Total constraints: {}", solver.numConstraints());

			// ==============================
			// Solve
			// ==============================
			log.error("Solving model with {} variables and {} constraints...", 
					solver.numVariables(), solver.numConstraints());
			
			MPSolver.ResultStatus resultStatus = solver.solve();

			long endTime = System.nanoTime();
			double runtimeSec = (endTime - startTime) / 1.0e9;

			if (resultStatus == MPSolver.ResultStatus.OPTIMAL || resultStatus == MPSolver.ResultStatus.FEASIBLE) {
				double sclPrm = obj.value();
				double timeLR = runtimeSec;

				logSolution(solver, obj, runtimeSec);
				
				// Copy solution to tau0 for ACO
				for (int i = 0; i < this.nbNodes; i++) {
					for (int j = 0; j < this.nbNodes; j++) {
						for (int p = 0; p < this.nbProducts; p++) {
							aco.tau0[p][i][j] = this.x[p][i][j].solutionValue();
						}
					}
				}

				try (PrintWriter writer = new PrintWriter(new FileWriter("history1.txt", true))) {
					writer.println("HiGHS LR for TAU0 COMPUTED - Objective: " + obj.value() + ", Runtime: " + runtimeSec + "s");
				}

			} else {
				log.warn("No solution found. Result status: {}", resultStatus);
				try (PrintWriter writer = new PrintWriter(new FileWriter("history1.txt", true))) {
					writer.println("HiGHS LR - No solution found. Status: " + resultStatus);
				}
			}

		} catch (Exception e) {
			log.error("Error during solver execution: {}", e.getMessage(), e);
			try (PrintWriter err = new PrintWriter(new FileWriter("HiGHS_LR_ERROR.txt", true))) {
				err.println("Error: " + e.getMessage());
				e.printStackTrace(err);
			} catch (Exception ex) {
				log.error("Failed to write error log: {}", ex.getMessage(), ex);
			}
		}

		return aco;
	}

	private void logSolution(MPSolver solver, MPObjective obj, double runtimeSec) {
		log.error("Solution found. Objective = {}, Runtime = {} s", obj.value(), runtimeSec);
		logZ();
		logX();
		logY();
		log.error("");
		log.error("Objective value = {}", obj.value());
		log.error("Problem solved in {} milliseconds", + solver.wallTime());
		log.error("Problem solved in {} iterations", solver.iterations());
		log.error("Problem solved in {} branch-and-bound nodes", solver.nodes());
	}

	private void logY() {
		for (int p = 0; p < this.nbProducts; p++) {
			for (int i = 0; i < this.nbNodes; i++) {
				for (int k = 0; k < this.nbNodes; k++) {
					for (int l = 0; l < this.nbNodes; l++) {
						log.error("y[{}][{}][{}][{}] = {}", p, i, k, l, this.y[p][i][k][l].solutionValue());
					}
				}
			}
		}
	}

	private void logX() {
		for (int p = 0; p < this.nbProducts; p++) {
			for (int i = 0; i < this.nbNodes; i++) {
				for (int j = 0; j < this.nbNodes; j++) {
					log.error("x[{}][{}][{}] = {}", p, i, j, this.x[p][i][j].solutionValue());
				}
			}
		}
	}

	private void logZ() {
		for (int i = 0; i < this.nbNodes; ++i) {
			log.error("z = [{}] = {}", i, this.z[i].solutionValue());
		}
	}

	private void createVars(MPSolver solver) {
		createXVars(solver);
		createYVars(solver);
		createZVars(solver);
	}

	private void createXVars(MPSolver solver) {
		this.x = new MPVariable[this.nbProducts][this.nbNodes][this.nbNodes];
		for (int p = 0; p < this.nbProducts; p++) {
			for (int i = 0; i < this.nbNodes; i++) {
				for (int j = 0; j < this.nbNodes; j++) {
					this.x[p][i][j] = solver.makeNumVar(0.0, 1.0, "x" + p + "i" + i + "j" + j);
				}
			}
		}
	}

	private void createYVars(MPSolver solver) {
		this.y = new MPVariable[this.nbProducts][this.nbNodes][this.nbNodes][this.nbNodes];
		for (int p = 0; p < this.nbProducts; p++) {
			for (int i = 0; i < this.nbNodes; i++) {
				for (int k = 0; k < this.nbNodes; k++) {
					for (int l = 0; l < this.nbNodes; l++) {
						this.y[p][i][k][l] = solver.makeNumVar(0.0, Double.POSITIVE_INFINITY, 
								"y" + p + "i" + i + "k" + k + "l" + l);
					}
				}
			}
		}
	}

	private void createZVars(MPSolver solver) {
		this.z = new MPVariable[this.nbNodes];
		for (int j = 0; j < this.nbNodes; j++) {
			this.z[j] = solver.makeNumVar(0.0, 1.0, "z" + j);
		}
	}

	private MPObjective createObjectiveFunction(MPSolver solver, Data data) {
		MPObjective obj = solver.objective();
		
		// First part: costs related to x variables
		for (int p = 0; p < this.nbProducts; p++) {
			for (int i = 0; i < this.nbNodes; i++) {
				for (int k = 0; k < this.nbNodes; k++) {
					double coeff = data.d[i][k] * (data.chi[p] * data.O[i][p] + data.delta[p] * data.D[i][p]);
					obj.setCoefficient(this.x[p][i][k], coeff);
					
					// Second part: costs related to y variables
					for (int l = 0; l < this.nbNodes; l++) {
						obj.setCoefficient(this.y[p][i][k][l], data.alpha[p] * data.d[k][l]);
					}
				}
			}
		}
		
		// Third part: fixed costs for hubs
		for (int k = 0; k < this.nbNodes; k++) {
			obj.setCoefficient(this.z[k], data.g[k]);
			
			// Fourth part: product-specific hub costs
			for (int p = 0; p < this.nbProducts; p++) {
				obj.setCoefficient(this.x[p][k][k], data.f[k][p]);
			}
		}
		
		obj.setMinimization();
		return obj;
	}

	/**
	 * @param solver
	 */
	private void addSingleAllocationConstraints(MPSolver solver) {
		for (int p = 0; p < this.nbProducts; p++) {
			for (int i = 0; i < this.nbNodes; i++) {
				MPConstraint c = solver.makeConstraint(1.0, 1.0, "SA_" + p + i);
				
				for (int j = 0; j < this.nbNodes; j++) {
					if (this.x[p][i][j] == null) {
						throw new IllegalStateException("Variable x[" + p + "][" + i + "][" + j + "] is null");
					}
					c.setCoefficient(this.x[p][i][j], 1.0);
				}
			}
		}
	}

	private void addLinkAllocationConstraints(MPSolver solver) {
		// Constraint: x[p][i][j] <= x[p][j][j]
		// (If node i is allocated to hub j for product p, then j must be a hub for product p)
		// Rewritten as: x[p][i][j] - x[p][j][j] <= 0
		
		for (int p = 0; p < this.nbProducts; p++) {
			for (int i = 0; i < this.nbNodes; i++) {
				for (int j = 0; j < this.nbNodes; j++) {
					// Skip self-allocation: x[p][j][j] <= x[p][j][j] is trivial
					if (i == j) continue;
					
					MPConstraint c = solver.makeConstraint(Double.NEGATIVE_INFINITY, 0.0, 
							"LA" + p + "i" + i + "j" + j);
					
					c.setCoefficient(this.x[p][i][j], 1.0);
					c.setCoefficient(this.x[p][j][j], -1.0);
				}
			}
		}
	}

	private void addMaxProdsAtHubConstraints(MPSolver solver, Data data) {
		for (int j = 0; j < this.nbNodes; j++) {
			MPConstraint c = solver.makeConstraint(Double.NEGATIVE_INFINITY, 0.0, 
					"MPH" + j);
			
			for (int p = 0; p < this.nbProducts; p++) {
				c.setCoefficient(this.x[p][j][j], 1.0);
			}
			
			c.setCoefficient(this.z[j], -data.L[j]);
		}
	}

	private void addFlowBalanceConstraints(MPSolver solver, Data data) {
		for (int p = 0; p < this.nbProducts; p++) {
			for (int i = 0; i < this.nbNodes; i++) {
				for (int k = 0; k < this.nbNodes; k++) {
					MPConstraint c = solver.makeConstraint(0.0, 0.0, 
							"FB" + p + "i" + i + "k" + k);
					
					// Flow conservation at hub k for origin i
					for (int l = 0; l < this.nbNodes; l++) {
						c.setCoefficient(this.y[p][i][k][l], 1.0);   // outflow from k
						c.setCoefficient(this.y[p][i][l][k], -1.0);  // inflow to k
					}
					
					// Supply/demand terms
					c.setCoefficient(this.x[p][i][k], -data.O[i][p]);
					
					for (int j = 0; j < this.nbNodes; j++) {
						c.setCoefficient(this.x[p][j][k], data.w[p][i][j]);
					}
				}
			}
		}
	}

	private void addFlowBoundsConstraints(MPSolver solver, Data data) {
		for (int p = 0; p < this.nbProducts; p++) {
			for (int i = 0; i < this.nbNodes; i++) {
				for (int j = 0; j < this.nbNodes; j++) {
					MPConstraint c = solver.makeConstraint(Double.NEGATIVE_INFINITY, data.O[i][p], 
							"FBnd" + p + "i" + i + "j" + j);
					
					for (int l = 0; l < this.nbNodes; l++) {
						if (l != j) {
							c.setCoefficient(this.y[p][i][j][l], 1.0);
						}
					}
					
					c.setCoefficient(this.x[p][i][j], -data.O[i][p]);
				}
			}
		}
	}

	private void addCapacityConstraints(MPSolver solver, Data data) {
		// Constraint: sum_i O[i][p] * x[i][k][p] <= gamma[k][p] * x[k][k][p]
		// Rewritten as: sum_i O[i][p] * x[i][k][p] - gamma[k][p] * x[k][k][p] <= 0
		
		for (int p = 0; p < this.nbProducts; p++) {
			for (int k = 0; k < this.nbNodes; k++) {
				MPConstraint c = solver.makeConstraint(Double.NEGATIVE_INFINITY, 0.0, 
						"Cap" + p + "k" + k);
				
				// Sum of allocated demand to hub k for product p
				for (int i = 0; i < this.nbNodes; i++) {
					c.setCoefficient(this.x[p][i][k], data.O[i][p]);
				}
				
				// Hub capacity for product p
				c.setCoefficient(this.x[p][k][k], -data.gamma[k][p]);
			}
		}
	}

	private void addMinimumHubsPerProdConstraints(MPSolver solver, Data data) {
		for (int p = 0; p < this.nbProducts; p++) {
			// Calculate total demand for product p
			double prodDemand = 0.0;
			for (int i = 0; i < this.nbNodes; i++) {
				prodDemand += data.O[i][p];
			}

			// Find minimum number of hubs needed to cover demand
			List<Integer> candidateHubs = new ArrayList<>();
			for (int i = 0; i < this.nbNodes; i++) {
				candidateHubs.add(i);
			}

			double totalCovered = 0.0;
			int r = 0;
			
			// Greedy selection of hubs with largest capacity
			while (totalCovered < prodDemand && !candidateHubs.isEmpty()) {
				double bestGamma = -1.0;
				int bestIdx = -1;
				
				for (int idx : candidateHubs) {
					if (data.gamma[idx][p] > bestGamma && data.O[idx][p] < data.gamma[idx][p]) {
						bestGamma = data.gamma[idx][p];
						bestIdx = idx;
					}
				}
				
				if (bestIdx == -1) break;
				
				totalCovered += bestGamma;
				candidateHubs.remove(Integer.valueOf(bestIdx));
				r++;
			}

			// Add constraint: sum of hubs for product p >= r
			MPConstraint c = solver.makeConstraint(r, Double.POSITIVE_INFINITY, 
					"MHP" + p);
			
			for (int k = 0; k < this.nbNodes; k++) {
				c.setCoefficient(this.x[p][k][k], 1.0);
			}
			
			log.debug("Product {} requires minimum {} hubs to cover demand {}", p, r, prodDemand);
		}
	}
}