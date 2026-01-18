package main.java.com.ef.antcolony.ortools;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.ortools.Loader;
import com.google.ortools.linearsolver.MPConstraint;
import com.google.ortools.linearsolver.MPObjective;
import com.google.ortools.linearsolver.MPSolver;
import com.google.ortools.linearsolver.MPVariable;
import com.google.ortools.modelbuilder.LinearArgument;
import com.google.ortools.modelbuilder.LinearConstraint;
import com.google.ortools.modelbuilder.LinearExpr;
import com.google.ortools.modelbuilder.LinearExprBuilder;

import main.java.com.ef.antcolony.Aco;
import main.java.com.ef.antcolony.GetSolutions.Solution;
import main.java.com.ef.antcolony.ReadData.Data;
import main.java.com.ef.antcolony.constants.AcoVar;

public class MPCSAHLP {

	private static final String LINE_SEPARATOR = "=========================================";
	private static final String S_S = "%s_%s";
	private static final String S_S_S = "%s_%s_%s";
	private static final String S_S_S_S = "%s_%s_%s_%s";
	private static final String FLOW_ALLOC_LINK_HUB_CSTRNT_NAME = "FALH";
	private static final String HUB_ASSIGNENT_CSTRNT_NAME = "HA";
	private static final String CAPACITY_CSTRNT_NAME = "CAP";
	private static final String HUB_MAX_PROD_CSTRNT_NAME = "HMP";
	private static final String SINGLE_ALLOCATION_CSTRNT_NAME = "SA";
	private static final String FLOW_DIVERGENCE_CSTRNT_NAME = "FD";
	private static final String MINIMUM_HUBS_PER_PRODS_CSTRNT_NAME = "MHP";

	private static final Logger log = LoggerFactory.getLogger(MPCSAHLP.class);

	private int nbProducts;
	private int nbNodes;

	private boolean isLinearRelaxation = false;
	private boolean isForcingSolution = false;

	String solverId = AcoVar.SOLVER_ID;

	MPVariable[][][] x;
	MPVariable[][][][] y;
	MPVariable[] z;

	public MPCSAHLP(int nbProducts, int nbNodes, boolean isLinearRelaxation) {
		this.nbProducts = nbProducts;
		this.nbNodes = nbNodes;
		this.isLinearRelaxation = isLinearRelaxation;
	}

	public MPCSAHLP(int nbProducts, int nbNodes, String solverId, boolean isLinearRelaxation) {
		this.nbProducts = nbProducts;
		this.nbNodes = nbNodes;
		this.solverId = solverId;
		this.isLinearRelaxation = isLinearRelaxation;
	}

	public Aco solve(Data data) {

		// Start timer
		long startTime = System.nanoTime();

		MPSolver solver = createSolver();
		if(solver == null) {
			return null;
		}

		Aco aco = new Aco(this.nbProducts, this.nbNodes); 

		try {
			// ==============================
			// Variables
			// ==============================
			log.debug("Creating variables...");
			createVars(solver);
			log.debug("Variables created. Total variables: {}", solver.numVariables());

			log.debug("Creating constraints ...");
			createConstraints(data, solver);
			log.debug("Constraints created.");

			log.debug("Creating objective function...");
			MPObjective obj = createObjectiveFunction(solver, data);
			log.debug("Objective function created.");

			// ==============================
			// Solve
			// ==============================
			log.debug("Solving model with {} variables and {} constraints...",  solver.numVariables(), solver.numConstraints());

			MPSolver.ResultStatus resultStatus = solver.solve();

			long endTime = System.nanoTime();
			double runtimeSec = (endTime - startTime) / 1.0e9;

			if (resultStatus == MPSolver.ResultStatus.OPTIMAL || resultStatus == MPSolver.ResultStatus.FEASIBLE) {
				aco.scalingParameter = obj.value();

				logSolution(solver, obj, runtimeSec);

				// Copy solution to tau0 for ACO
				for (int i = 0; i < this.nbNodes; i++) {
					for (int j = 0; j < this.nbNodes; j++) {
						for (int p = 0; p < this.nbProducts; p++) {
							aco.tau0[p][i][j] = this.x[i][j][p].solutionValue();
						}
					}
				}

				log.debug("{} LR for TAU0 COMPUTED - Objective: {}, Runtime: {}s", solverId, obj.value(), runtimeSec);

			} else {
				log.error("No solution found. Result status: {}", resultStatus);
				if (resultStatus == MPSolver.ResultStatus.INFEASIBLE && AcoVar.LOG_MPCSAHLP) {
					logInfeasibleConstraints(solver);
				}
			}

		} catch (Exception e) {
			log.error("Error during solver execution: {}", e.getMessage(), e);
		}

		return aco;
	}

	public boolean solve(Data data, Aco aco, Solution sol) {

		// Start timer
		long startTime = System.nanoTime();

		MPSolver solver = createSolver();
		if(solver == null) {
			return false;
		}

		if (sol != null) {
			this.isForcingSolution = true;
		}

		try {
			// ==============================
			// Variables
			// ==============================
			log.debug("Creating variables...");
			createVars(solver, sol);
			log.debug("Variables created. Total variables: {}", solver.numVariables());

			validateXConstraingsDELETE(sol);

			// ==============================
			// Objective function (create BEFORE constraints)
			// ==============================
			log.debug("Creating objective function...");
			MPObjective obj = createObjectiveFunction(solver, data);
			log.debug("Objective function created.");

			log.debug("Creating constraints ...");
			createConstraints(data, solver);
			log.debug("Constraints created.");

			// ==============================
			// Solve
			// ==============================
			log.debug("Solving model with {} variables and {} constraints...", 
					solver.numVariables(), solver.numConstraints());

			MPSolver.ResultStatus resultStatus = solver.solve();

			long endTime = System.nanoTime();
			double runtimeSec = (endTime - startTime) / 1.0e9;

			if (resultStatus == MPSolver.ResultStatus.OPTIMAL || resultStatus == MPSolver.ResultStatus.FEASIBLE) {
				aco.scalingParameter = obj.value();

				logSolution(solver, obj, runtimeSec);

				// Copy solution to tau0 for ACO
				for (int i = 0; i < this.nbNodes; i++) {
					for (int j = 0; j < this.nbNodes; j++) {
						for (int p = 0; p < this.nbProducts; p++) {
							aco.tau0[p][i][j] = this.x[i][j][p].solutionValue();
						}
					}
				}

				log.debug("{} LR for TAU0 COMPUTED - Objective: {}, Runtime: {}s", solverId, obj.value(), runtimeSec);
				return true;
			} else {
				log.error("No solution found. Result status: {}", resultStatus);
				if (resultStatus == MPSolver.ResultStatus.INFEASIBLE && AcoVar.LOG_MPCSAHLP) {
					logInfeasibleConstraints(solver, sol);
				}
				return false;
			}

		} catch (Exception e) {
			log.error("Error during solver execution: {}", e.getMessage(), e);
		}

		return false;
	}

	protected void validateXConstraingsDELETE(Solution sol) {
		for (int p = 0; p < this.nbProducts; p++) {
			for (int i = 0; i < this.nbNodes; i++) {
				for (int j = 0; j < this.nbNodes; j++) {
					if (i == j) continue;
					// Skip self-allocation: x[p][j][j] <= x[p][j][j] is trivial
					if(sol.x[p][i][j] <= sol.x[p][j][j]) {
						log.debug("sol.x[{}][{}][{}] OK ", p, i, j);
					} else {
						log.debug("sol.x[{}][{}][{}] NOK ", p, i, j);
					}
				}
			}
		}
	}

	protected void createConstraints(Data data, MPSolver solver) {

		// ==============================
		// 3.13. Single allocation constraint
		// ==============================
		log.debug("Adding single allocation constraints...");
		addSingleAllocationConstraints(solver);
		log.debug("Added single allocation constraints. Total constraints: {}", solver.numConstraints());

		// ==============================
		// 3.4. hub assignment constraints
		// x[i][k][p] <= x[p][k][k]
		// ==============================
		log.debug("Adding hub assignment constraints...");
		addHubAssignmentConstraints(solver);
		log.debug("Added ub assignment constraints. Total constraints: {}", solver.numConstraints());

		// ==============================
		// 3.6 Maximum products at hub
		// sum_p x[k][k][p] <= L[k] * z[k]
		// ==============================
		log.debug("Adding max products at hub constraints...");
		addMaxHubProdConstraints(solver, data);
		log.debug("Added max products at hub constraints. Total constraints: {}", solver.numConstraints());


		// ==============================
		// 3.14. Flow divergence constraints
		// sum in l ypikl − sum in l ypilk = Opi * xpik − wpij * xpjk ,i,k∈N,p∈P,
		// ==============================
		log.debug("Adding flow divergence constraints...");
		addFlowDivergenceConstraints(solver, data);
		log.debug("Added flow divergence constraints. Total constraints: {}", solver.numConstraints());


		// ==============================
		// 3.15. Flow allocation linking hub
		// sum in l y[p][i][k][l] <= O[i][p] * x[i][k][p]
		// ==============================
		log.debug("Adding flow allocation linking hub constraints...");
		addFlowAllocationLinkingHubConstraints(solver, data);
		log.debug("Added flow allocation linking hub. Total constraints: {}", solver.numConstraints());

		// ==============================
		// 3.5. Capacity per hub
		// sum_i O[i][p] * x[i][k][p] <= gamma[k][p] * x[k][k][p]
		// ==============================
		log.debug("Adding capacity constraints...");
		addCapacityConstraints(solver, data);
		log.debug("Added capacity constraints. Total constraints: {}", solver.numConstraints());


		// ==============================
		// Enhancements
		// ==============================
		log.debug("Adding minimum hubs per product constraints...");
		addMinimumHubsPerProdConstraints(solver, data);
		log.debug("Added minimum hubs per product constraints. Total constraints: {}", solver.numConstraints());
	}

	private MPSolver createSolver() {
		log.info("start loadNativeLibraries");
		Loader.loadNativeLibraries();
		log.info(" end loadNativeLibraries");

		// Create HiGHS solver
		MPSolver solver = null;
		try {
			solver = MPSolver.createSolver(solverId);
		} catch (Exception e) {
			log.error(e.getMessage());
		}

		if (solver == null) {
			log.error("{} solver unavailable.", solverId);
			return null;
		} else {
			log.debug("{} solver used.", solverId);
		}

		return solver;
	}

	private void createVars(MPSolver solver) {
		this.createVars(solver, null);
	}

	private void createVars(MPSolver solver, Solution sol) {
		if(sol != null) {
			createXVars(solver, sol.x);
			createYVars(solver);
			createZVars(solver, sol.z);
		} else {
			createXVars(solver);
			createYVars(solver);
			createZVars(solver);
		}

	}
	private void createXVars(MPSolver solver) {
		this.createXVars(solver, null);
	}

	private void createXVars(MPSolver solver, int[][][] solutionX) {
		this.x = new MPVariable[this.nbNodes][this.nbNodes][this.nbProducts];
		for (int i = 0; i < this.nbNodes; i++) {
			for (int j = 0; j < this.nbNodes; j++) {
				for (int p = 0; p < this.nbProducts; p++) {
					if(this.isLinearRelaxation) {
						this.x[i][j][p] = solver.makeNumVar(0.0, 1.0, "x" + i + j + p);
					} else if (this.isForcingSolution) {
						double fixedBound = solutionX[p][i][j];
						this.x[i][j][p] = solver.makeIntVar(fixedBound, fixedBound,"x" + i + j + p);
					} else {
						this.x[i][j][p] = solver.makeIntVar(0.0, 1.0, "x" + i + j + p);
					}
				}
			}
		}
	}

	private void createYVars(MPSolver solver) {
		this.y = new MPVariable[this.nbNodes][this.nbNodes][this.nbNodes][this.nbProducts];
		for (int i = 0; i < this.nbNodes; i++) {
			for (int k = 0; k < this.nbNodes; k++) {
				for (int l = 0; l < this.nbNodes; l++) {
					for (int p = 0; p < this.nbProducts; p++) {
						this.y[i][k][l][p] = solver.makeNumVar(0.0, Double.POSITIVE_INFINITY, "y" + i + k + l + p);
					}
				}
			}
		}
	}

	private void createZVars(MPSolver solver) {
		this.createZVars(solver, null);
	}

	private void createZVars(MPSolver solver, int[] solutionZ) {
		this.z = new MPVariable[this.nbNodes];
		for (int i = 0; i < this.nbNodes; i++) {
			if(this.isForcingSolution) {
				if(solutionZ != null) {
					double fixedBound = solutionZ[i];
					this.z[i] = solver.makeIntVar(fixedBound, fixedBound, "z" + i);
				} else {
					log.error("isForcingSolution and solutionZ is null");
				}

			} else {
				this.z[i] = solver.makeIntVar(0.0, 1.0, "z" + i);
			}
		}
	}

	private MPObjective createObjectiveFunction(MPSolver solver, Data data) {
		MPObjective obj = solver.objective();

		// First part: costs related to x variables
		addFlowRoutingOrigDestCosts(data, obj);
		addFlowRoutingCosts(data, obj);
		addOpenHubCosts(data, obj);
		addDedicateHubCosts(data, obj);

		obj.setMinimization();
		return obj;
	}

	private void addOpenHubCosts(Data data, MPObjective obj) {
		for (int i = 0; i < this.nbNodes; i++) {
			double coeff = data.g[i];
			double existingCoeff = obj.getCoefficient(this.z[i]);
			obj.setCoefficient(this.z[i], coeff + existingCoeff);
		}
	}

	private void addDedicateHubCosts(Data data, MPObjective obj) {
		for (int p = 0; p < this.nbProducts; p++) {
			for (int i = 0; i < this.nbNodes; i++) {
				double coeff = data.f[p][i];
				double existingCoeff = obj.getCoefficient(this.x[i][i][p]);
				obj.setCoefficient(this.x[i][i][p], coeff + existingCoeff); // Note: i appears twice for diagonal
			}
		}
	}

	private void addFlowRoutingCosts(Data data, MPObjective obj) {
		for (int p = 0; p < this.nbProducts; p++) {
			for (int i = 0; i < this.nbNodes; i++) {  // Assuming I nodes
				for (int k = 0; k < this.nbNodes; k++) {  // Assuming K nodes
					for (int l = 0; l < this.nbNodes; l++) {  // Assuming L nodes
						double coeff = data.alpha[p] * data.d[k][l];
						double existingCoeff = obj.getCoefficient(this.y[i][k][l][p]);
						obj.setCoefficient(this.y[i][k][l][p], coeff + existingCoeff);
					}
				}
			}
		}
	}

	private void addFlowRoutingOrigDestCosts(Data data, MPObjective obj) {
		for (int p = 0; p < this.nbProducts; p++) {
			for (int i = 0; i < this.nbNodes; i++) {
				for (int k = 0; k < this.nbNodes; k++) {
					double collection = data.chi[p] * data.originatedFlow[p][i];  
					double distribution = data.delta[p] * data.destinedFlow[p][i];   
					double coeff = data.d[i][k] * (collection + distribution);
					double existingCoeff = obj.getCoefficient(this.x[i][k][p]);
					obj.setCoefficient(this.x[i][k][p], coeff + existingCoeff);
				}
			}
		}
	}

	/**
	 * @apiNote
	 * Adds single allocation constraints to the optimization model.
	 * 
	 * For each node i and product p, ensures that node i is assigned to exactly one hub k.
	 * 
	 * Mathematical formulation:
	 *   sum(x[i][k][p] for all k ∈ nodes) = 1  ∀ i ∈ nodes, p ∈ products
	 * 
	 * Where:
	 *   x[i][k][p] = 1 if node i is assigned to hub k for product p
	 *              = 0 otherwise
	 * 
	 * This constraint guarantees that:
	 * 1. Each node receives each product from exactly one hub (single sourcing)
	 * 2. No splitting of demand for a product across multiple hubs
	 * 3. Self-assignment is allowed (node can serve as its own hub when i = k)
	 * @param solver
	 */
	private void addSingleAllocationConstraints(MPSolver solver) {
		for (int i = 0; i < this.nbNodes; i++) {
			for (int p = 0; p < this.nbProducts; p++) {
				MPConstraint c = solver.makeConstraint(1.0, 1.0);

				// Sum x[i][k][p] over all k in K
				for (int k = 0; k < this.nbNodes; k++) {
					double existingCoeff = c.getCoefficient(this.x[i][k][p]);
					c.setCoefficient(this.x[i][k][p], existingCoeff + 1.0);
				}
			}
		}
	}

	private void addHubAssignmentConstraints(MPSolver solver) {
		// Constraint: x[p][i][j] <= x[p][j][j]
		// (If node i is allocated to hub j for product p, then j must be a hub for product p)
		// Rewritten as: x[p][i][j] - x[p][j][j] <= 0
		for (int i = 0; i < this.nbNodes; i++) {
			for (int k = 0; k < this.nbNodes; k++) {
				for (int p = 0; p < this.nbProducts; p++) {

					// Skip self-allocation: x[p][j][j] <= x[p][j][j] is trivial
					if (i == k) {
						continue;
					}

					MPConstraint c = solver.makeConstraint(Double.NEGATIVE_INFINITY, 0.0);

					double existingCoeff1 = c.getCoefficient(this.x[i][k][p]);
					c.setCoefficient(this.x[i][k][p], 1.0 + existingCoeff1);

					double existingCoeff2 = c.getCoefficient(this.x[k][k][p]);
					c.setCoefficient(this.x[k][k][p], -1.0 + existingCoeff2);
				}
			}
		}
	}

	/**
	 * @apiNote The constraint is created as: expression ≤ 0
	 * Which means: sum(x[p][j][j]) - L[j] * z[j] ≤ 0
	 * Rearranged: sum(x[p][j][j]) ≤ L[j] * z[j]
	 * @param solver
	 * @param data
	 */
	private void addMaxHubProdConstraints(MPSolver solver, Data data) {
		for (int k = 0; k < this.nbNodes; k++) {
			MPConstraint c = solver.makeConstraint(Double.NEGATIVE_INFINITY, 0.0);

			for (int p = 0; p < this.nbProducts; p++) {
				double existingCoeff = c.getCoefficient(this.x[k][k][p]);
				c.setCoefficient(this.x[k][k][p], 1.0 + existingCoeff);
			}

			double existingCoeff = c.getCoefficient(this.z[k]);
			c.setCoefficient(this.z[k], -data.L[k] + existingCoeff);
		}
	}

	/**
	 * @apiNote flow divergence
	 * divergence equations for commodities ip at node k - flow conservation
	 * sum in l ypikl − sum in l ypilk = Opi * xpik − wpij * xpjk ,i,k∈N,p∈P,
	 * @param solver
	 * @param data
	 */
	private void addFlowDivergenceConstraints(MPSolver solver, Data data) {
		for (int i = 0; i < this.nbNodes; i++) {
			for (int k = 0; k < this.nbNodes; k++) {
				for (int p = 0; p < this.nbProducts; p++) {
					MPConstraint c = solver.makeConstraint(0.0, 0.0);

					// Flow conservation at hub k for origin i
					for (int l = 0; l < this.nbNodes; l++) {
						double currentCoeff1 = c.getCoefficient(this.y[i][k][l][p]);
						c.setCoefficient(this.y[i][k][l][p], 1.0 + currentCoeff1);   // outflow from k

						double currentCoeff2 = c.getCoefficient(this.y[i][l][k][p]);
						c.setCoefficient(this.y[i][l][k][p], - 1.0 + currentCoeff2);  // inflow to k
					}

					double currentCoeff = c.getCoefficient(this.x[i][k][p]);
					// Supply/demand terms
					c.setCoefficient(this.x[i][k][p], - data.originatedFlow[p][i] + currentCoeff);

					for (int j = 0; j < this.nbNodes; j++) {
						currentCoeff = c.getCoefficient(this.x[j][k][p]);
						c.setCoefficient(this.x[j][k][p], data.w[p][i][j] + currentCoeff);
					}
				}
			}
		}
	}

	/**
	 * sum in l y[p][i][k][l] <= O[i][p] * x[i][k][p]
	 * @param solver
	 * @param data
	 */
	private void addFlowAllocationLinkingHubConstraints(MPSolver solver, Data data) {
		for (int i = 0; i < this.nbNodes; i++) {
			for (int k = 0; k < this.nbNodes; k++) {
				for (int p = 0; p < this.nbProducts; p++) {
					MPConstraint c = solver.makeConstraint(Double.NEGATIVE_INFINITY, 0.0);

					for (int l = 0; l < this.nbNodes; l++) {
						if(l != k) {
							double currentCoeff = c.getCoefficient(this.y[i][k][l][p]);
							c.setCoefficient(this.y[i][k][l][p], 1.0 + currentCoeff);
						}
					}

					double currentCoeff = c.getCoefficient(this.x[i][k][p]);
					c.setCoefficient(this.x[i][k][p], -data.originatedFlow[p][i] + currentCoeff);
				}
			}
		}
	}

	/**
	 * Adds the capacity constraints
	 *   sum_{i} orig_f[p][i] * x[i][k][p]  <=  gamma[p][k] * x[k][k][p]
	 * for every node k and every product p.
	 *
	 * Assumes the model already contains:
	 *   - MPSolver solver
	 *   - MPVariable[][][] x  (indexed [i][k][p])
	 *   - int nbNodes, nbProducts
	 *   - double[][] originatedFlow   (orig_f)
	 *   - double[][] gamma
	 */
	private void addCapacityConstraints(MPSolver solver, Data data) {
		// Constraint: sum_i O[i][p] * x[i][k][p] <= gamma[k][p] * x[k][k][p]
		// Rewritten as: sum_i O[i][p] * x[i][k][p] - gamma[k][p] * x[k][k][p] <= 0
		for (int k = 0; k < this.nbNodes; k++) {
			for (int p = 0; p < this.nbProducts; p++) {
				MPConstraint c = solver.makeConstraint(Double.NEGATIVE_INFINITY, 0.0);

				// Sum of allocated demand to hub k for product p
				for (int i = 0; i < this.nbNodes; i++) {
					double currentCoeff = c.getCoefficient(this.x[i][k][p]);
					c.setCoefficient(this.x[i][k][p], data.originatedFlow[p][i] + currentCoeff);
				}

				double currentCoeff = c.getCoefficient(this.x[k][k][p]);
				// Hub capacity for product p
				c.setCoefficient(this.x[k][k][p], - data.gamma[p][k] + currentCoeff);

			}
		}
	}


	private void addMinimumHubsPerProdConstraints(MPSolver solver, Data data) {
		int maxR = 0;
		int sumR = 0;

		for (int p = 0; p < this.nbProducts; p++) {
			int[] check = new int[this.nbNodes];

			// Calculate total demand for product p
			double prodDemand = 0.0;
			for (int i = 0; i < this.nbNodes; i++) {
				prodDemand += data.originatedFlow[p][i];
			}

			int r = 0;
			double totalP = 0.0;
			int index = 0;

			// Greedy selection of hubs with largest capacity
			while (totalP < prodDemand) {
				double maxGamma = 0.0;
				index = -1;

				for (int i = 0; i < this.nbNodes; i++) {
					if (check[i] == 0 && 
							data.gamma[p][i] > maxGamma && 
							data.originatedFlow[p][i] < data.gamma[p][i]) {
						maxGamma = data.gamma[p][i];
						index = i;
					}
				}

				if (index == -1) break;  // No more valid hubs found

				totalP += maxGamma;
				check[index] = 1;
				r += 1;
			}

			// Add constraint: sum of x[k][k][p] for all k >= r
			MPConstraint c = solver.makeConstraint(r, Double.POSITIVE_INFINITY);
			for (int k = 0; k < this.nbNodes; k++) {
				c.setCoefficient(this.x[k][k][p], 1.0);
			}

			maxR = Math.max(maxR, r);
			sumR += r;

			log.debug("Product {} requires minimum {} hubs to cover demand {}", p, r, prodDemand);
		}

		// Check if all L[k] values are equal
		int checkL = 1;
		for (int k = 0; k < this.nbNodes; k++) {
			if (data.L[k] != data.L[0]) {
				checkL = 0;
				break;
			}
		}

		// Add constraint on z variables
		MPConstraint zConstraint;
		if (checkL == 0) {
			// Not all L[k] are equal
			zConstraint = solver.makeConstraint(maxR, Double.POSITIVE_INFINITY);
		} else {
			// All L[k] are equal
			double minHubs = Math.max(maxR, Math.ceil(1.0 * sumR / data.L[0]));
			zConstraint = solver.makeConstraint(minHubs, Double.POSITIVE_INFINITY);
		}

		// Set coefficients for z variables
		for (int i = 0; i < this.nbNodes; i++) {
			zConstraint.setCoefficient(this.z[i], 1.0);
		}

		log.debug("Z constraint: sum(z[i]) >= {}", 
				checkL == 0 ? maxR : Math.max(maxR, Math.ceil(1.0 * sumR / data.L[0])));
	}

	private void logSolution(MPSolver solver, MPObjective obj, double runtimeSec) {
		log.debug("Solution found. Objective = {}, Runtime = {} s", obj.value(), runtimeSec);
		logZ();
		logX();
		logY();
		log.debug("Objective value = {}", obj.value());
		log.debug("Problem solved in {} milliseconds", + solver.wallTime());
		log.debug("Problem solved in {} iterations", solver.iterations());
		log.debug("Problem solved in {} branch-and-bound nodes", solver.nodes());
		if (AcoVar.LOG_MPCSAHLP) {
			String lpFormat = solver.exportModelAsLpFormat(false);
			log.debug("Model in LP format:\n{}", lpFormat);

			try {
				Files.writeString(
						Path.of("java.lp"),
						solver.exportModelAsLpFormat(false)
						);
			} catch (IOException e) {
				e.printStackTrace();
			}

		}

	}

	private void logY() {
		for (int p = 0; p < this.nbProducts; p++) {
			for (int i = 0; i < this.nbNodes; i++) {
				for (int k = 0; k < this.nbNodes; k++) {
					for (int l = 0; l < this.nbNodes; l++) {
						log.debug("y[{}][{}][{}][{}] = {}", p, i, k, l, this.y[i][k][l][p].solutionValue());
					}
				}
			}
		}
	}

	private void logX() {
		for (int p = 0; p < this.nbProducts; p++) {
			for (int i = 0; i < this.nbNodes; i++) {
				for (int j = 0; j < this.nbNodes; j++) {
					log.debug("x[{}][{}][{}] = {}", p, i, j, this.x[i][j][p].solutionValue());
				}
			}
		}
	}

	private void logZ() {
		for (int i = 0; i < this.nbNodes; ++i) {
			log.debug("z = [{}] = {}", i, this.z[i].solutionValue());
		}
	}

	/**
	 * Logs detailed information about all constraints when the model is infeasible.
	 * This helps identify which constraints might be causing the infeasibility.
	 * 
	 * @param solver The MPSolver instance
	 */
	private void logInfeasibleConstraints(MPSolver solver) {
		log.info(LINE_SEPARATOR);
		log.info("INFEASIBLE MODEL - Constraint Analysis");
		log.info(LINE_SEPARATOR);
		log.info("Total constraints: {}", solver.numConstraints());
		log.info("Total variables: {}", solver.numVariables());

		// Export model to LP format for external analysis
		try {
			String lpFormat = solver.exportModelAsLpFormat(false);
			log.info("Model in LP format:\n{}", lpFormat);
			try {
				Files.writeString(
						Path.of("java.lp"),
						solver.exportModelAsLpFormat(false)
						);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} catch (Exception e) {
			log.error("Could not export LP format: {}", e.getMessage());
		}

		log.info(LINE_SEPARATOR);
		log.info("Constraint Details:");
		log.info(LINE_SEPARATOR);

		// Iterate through all constraints
		for (int i = 0; i < solver.numConstraints(); i++) {
			try {
				MPConstraint constraint = solver.constraint(i);
				String name = constraint.name();
				double lb = constraint.lb();
				double ub = constraint.ub();

				// Build constraint expression string using stored variables
				StringBuilder expr = new StringBuilder();
				boolean firstTerm = true;
				int nonZeroCoeffs = 0;

				// Check x variables
				if (this.x != null) {
					for (int p = 0; p < this.nbProducts; p++) {
						for (int i_idx = 0; i_idx < this.nbNodes; i_idx++) {
							for (int j = 0; j < this.nbNodes; j++) {
								if (this.x[p][i_idx][j] != null) {
									double coeff = constraint.getCoefficient(this.x[p][i_idx][j]);
									if (Math.abs(coeff) > 1e-10) {
										if (!firstTerm) {
											expr.append(coeff >= 0 ? " + " : " - ");
										} else if (coeff < 0) {
											expr.append("-");
										}
										expr.append(String.format("%.6f", Math.abs(coeff)))
										.append(" * x[").append(p).append("][").append(i_idx).append("][").append(j).append("]");
										firstTerm = false;
										nonZeroCoeffs++;
									}
								}
							}
						}
					}
				}

				// Check y variables
				if (this.y != null) {
					for (int p = 0; p < this.nbProducts; p++) {
						for (int i_idx = 0; i_idx < this.nbNodes; i_idx++) {
							for (int k = 0; k < this.nbNodes; k++) {
								for (int l = 0; l < this.nbNodes; l++) {
									if (this.y[p][i_idx][k][l] != null) {
										double coeff = constraint.getCoefficient(this.y[p][i_idx][k][l]);
										if (Math.abs(coeff) > 1e-10) {
											if (!firstTerm) {
												expr.append(coeff >= 0 ? " + " : " - ");
											} else if (coeff < 0) {
												expr.append("-");
											}
											expr.append(String.format("%.6f", Math.abs(coeff)))
											.append(" * y[").append(p).append("][").append(i_idx).append("][").append(k).append("][").append(l).append("]");
											firstTerm = false;
											nonZeroCoeffs++;
										}
									}
								}
							}
						}
					}
				}

				// Check z variables
				if (this.z != null) {
					for (int j = 0; j < this.nbNodes; j++) {
						if (this.z[j] != null) {
							double coeff = constraint.getCoefficient(this.z[j]);
							if (Math.abs(coeff) > 1e-10) {
								if (!firstTerm) {
									expr.append(coeff >= 0 ? " + " : " - ");
								} else if (coeff < 0) {
									expr.append("-");
								}
								expr.append(String.format("%.6f", Math.abs(coeff)))
								.append(" * z[").append(j).append("]");
								firstTerm = false;
								nonZeroCoeffs++;
							}
						}
					}
				}

				// Format constraint bounds
				String boundsStr;
				if (lb == ub) {
					boundsStr = String.format(" = %.6f", lb);
				} else if (lb == Double.NEGATIVE_INFINITY && ub == Double.POSITIVE_INFINITY) {
					boundsStr = " (unbounded)";
				} else if (lb == Double.NEGATIVE_INFINITY) {
					boundsStr = String.format(" <= %.6f", ub);
				} else if (ub == Double.POSITIVE_INFINITY) {
					boundsStr = String.format(" >= %.6f", lb);
				} else {
					boundsStr = String.format(" in [%.6f, %.6f]", lb, ub);
				}

				log.info("Constraint [{}]: {}{}{}", i, name, 
						expr.length() > 0 ? ": " + expr.toString() : " (empty - " + nonZeroCoeffs + " coefficients)", 
								boundsStr);

				// Check for potentially problematic constraints
				if (lb > ub) {
					log.error("  *** INVALID: Lower bound ({}) > Upper bound ({}) ***", lb, ub);
				}
				if (lb == Double.POSITIVE_INFINITY || ub == Double.NEGATIVE_INFINITY) {
					log.error("  *** INVALID: Impossible bounds ***");
				}
			} catch (Exception e) {
				log.error("Error accessing constraint [{}]: {}", i, e.getMessage());
			}
		}

		log.info(LINE_SEPARATOR);
		log.info("Variable Bounds Summary:");
		log.info(LINE_SEPARATOR);

		// Log variable bounds that might be problematic
		int invalidVars = 0;
		if (this.x != null) {
			for (int p = 0; p < this.nbProducts; p++) {
				for (int i = 0; i < this.nbNodes; i++) {
					for (int j = 0; j < this.nbNodes; j++) {
						if (this.x[i][j][p] != null) {
							double lb = this.x[i][j][p].lb();
							double ub = this.x[i][j][p].ub();
							if (lb > ub || lb == Double.POSITIVE_INFINITY || ub == Double.NEGATIVE_INFINITY) {
								log.info("Variable x[{}][{}][{}]: INVALID bounds [{} to {}]", p, i, j, lb, ub);
								invalidVars++;
							}
						}
					}
				}
			}
		}

		if (this.z != null) {
			for (int j = 0; j < this.nbNodes; j++) {
				if (this.z[j] != null) {
					double lb = this.z[j].lb();
					double ub = this.z[j].ub();
					if (lb > ub || lb == Double.POSITIVE_INFINITY || ub == Double.NEGATIVE_INFINITY) {
						log.info("Variable z[{}]: INVALID bounds [{} to {}]", j, lb, ub);
						invalidVars++;
					}
				}
			}
		}

		if (invalidVars == 0) {
			log.info("No invalid variable bounds found.");
		} else {
			log.error("Found {} variables with invalid bounds.", invalidVars);
		}

		log.info(LINE_SEPARATOR);
	}

	/**
	 * Logs detailed information about all constraints when the model is infeasible.
	 * This helps identify which constraints might be causing the infeasibility.
	 * 
	 * @param solver The MPSolver instance
	 */
	private void logInfeasibleConstraints(MPSolver solver, Solution sol) {
		log.debug(LINE_SEPARATOR);
		log.error("INFEASIBLE MODEL - Constraint Analysis");
		log.debug(LINE_SEPARATOR);
		log.debug("Total constraints: {}", solver.numConstraints());
		log.debug("Total variables: {}", solver.numVariables());

		// Export model to LP format for external analysis
		try {
			String lpFormat = solver.exportModelAsLpFormat(false);
			log.debug("Model in LP format:\n{}", lpFormat);
		} catch (Exception e) {
			log.debug("Could not export LP format: {}", e.getMessage());
		}

		log.debug(LINE_SEPARATOR);
		log.debug("Constraint Details:");
		log.debug(LINE_SEPARATOR);

		// Iterate through all constraints
		if(false) {
			printConstraints(solver, sol);
		}

		log.debug(LINE_SEPARATOR);
		log.debug("Variable Bounds Summary:");
		log.debug(LINE_SEPARATOR);

		// Log variable bounds that might be problematic
		int invalidVars = 0;
		if (this.x != null) {
			for (int p = 0; p < this.nbProducts; p++) {
				for (int i = 0; i < this.nbNodes; i++) {
					for (int j = 0; j < this.nbNodes; j++) {
						if (this.x[i][j][p] != null) {
							double lb = this.x[i][j][p].lb();
							double ub = this.x[i][j][p].ub();
							if (lb > ub || lb == Double.POSITIVE_INFINITY || ub == Double.NEGATIVE_INFINITY) {
								log.error("Variable x[{}][{}][{}]: INVALID bounds [{} to {}]", p, i, j, lb, ub);
								invalidVars++;
							}
						}
					}
				}
			}
		}

		if (this.z != null) {
			for (int j = 0; j < this.nbNodes; j++) {
				if (this.z[j] != null) {
					double lb = this.z[j].lb();
					double ub = this.z[j].ub();
					if (lb > ub || lb == Double.POSITIVE_INFINITY || ub == Double.NEGATIVE_INFINITY) {
						log.error("Variable z[{}]: INVALID bounds [{} to {}]", j, lb, ub);
						invalidVars++;
					}
				}
			}
		}

		if (invalidVars == 0) {
			log.error("No invalid variable bounds found.");
		} else {
			log.error("Found {} variables with invalid bounds.", invalidVars);
		}

		log.debug(LINE_SEPARATOR);
	}

	protected void printConstraints(MPSolver solver, Solution sol) {
		for (int i = 0; i < solver.numConstraints(); i++) {
			try {
				MPConstraint constraint = solver.constraint(i);
				String name = constraint.name();
				double lb = constraint.lb();
				double ub = constraint.ub();

				// Build constraint expression string using stored variables
				StringBuilder expr = new StringBuilder();
				boolean firstTerm = true;
				int nonZeroCoeffs = 0;

				// Check x variables
				if (this.x != null) {
					for (int p = 0; p < this.nbProducts; p++) {
						for (int i_idx = 0; i_idx < this.nbNodes; i_idx++) {
							for (int j = 0; j < this.nbNodes; j++) {
								double coeff = constraint.getCoefficient(this.x[p][i_idx][j]);
								if (Math.abs(coeff) > 1e-10) {
									if (!firstTerm) {
										expr.append(coeff >= 0 ? " + " : " - ");
									} else if (coeff < 0) {
										expr.append("-");
									}
									expr.append(String.format("%.6f", Math.abs(coeff)))
									.append(" * x[").append(p).append("][").append(i_idx).append("][").append(j).append("]")
									.append("(").append(sol.x[p][i_idx][j]).append(")");

									firstTerm = false;
									nonZeroCoeffs++;
								}
							}
						}
					}
					if(name.startsWith("CAP")) {
						for (int p = 0; p < this.nbProducts; p++) {
							for (int i_idx = 0; i_idx < this.nbNodes; i_idx++) {
								StringBuilder cap_expr = new StringBuilder();
								int sum = 0;
								double coeff = constraint.getCoefficient(this.x[p][i_idx][i_idx]);
								cap_expr.append("(").append(sol.x[p][i_idx][i_idx] * coeff).append(")");
								for (int j = 0; j < this.nbNodes; j++) {
									coeff = constraint.getCoefficient(this.x[p][i_idx][j]);
									if (Math.abs(coeff) > 1e-10) {
										if(i_idx != j) {
											sum += sol.x[p][i_idx][j] * coeff;
										}
									}
								}

								cap_expr.append(" <= (").append(sum).append(")");
								log.debug("{}", cap_expr);
							}
						}
					}

				}

				// Check y variables
				if (this.y != null) {
					for (int p = 0; p < this.nbProducts; p++) {
						for (int i_idx = 0; i_idx < this.nbNodes; i_idx++) {
							for (int k = 0; k < this.nbNodes; k++) {
								for (int l = 0; l < this.nbNodes; l++) {
									if (this.y[p][i_idx][k][l] != null) {
										double coeff = constraint.getCoefficient(this.y[p][i_idx][k][l]);
										if (Math.abs(coeff) > 1e-10) {
											if (!firstTerm) {
												expr.append(coeff >= 0 ? " + " : " - ");
											} else if (coeff < 0) {
												expr.append("-");
											}
											expr.append(String.format("%.6f", Math.abs(coeff)))
											.append(" * y[").append(p).append("][").append(i_idx).append("][").append(k).append("][").append(l).append("]");
											firstTerm = false;
											nonZeroCoeffs++;
										}
									}
								}
							}
						}
					}
				}

				// Check z variables
				if (this.z != null) {
					for (int j = 0; j < this.nbNodes; j++) {
						if (this.z[j] != null) {
							double coeff = constraint.getCoefficient(this.z[j]);
							if (Math.abs(coeff) > 1e-10) {
								if (!firstTerm) {
									expr.append(coeff >= 0 ? " + " : " - ");
								} else if (coeff < 0) {
									expr.append("-");
								}
								expr.append(String.format("%.6f", Math.abs(coeff)))
								.append(" * z[").append(j).append("]")
								.append("(").append(sol.z[j]).append(")");;
								firstTerm = false;
								nonZeroCoeffs++;
							}
						}
					}
				}

				// Format constraint bounds
				String boundsStr;
				if (lb == ub) {
					boundsStr = String.format(" = %.6f", lb);
				} else if (lb == Double.NEGATIVE_INFINITY && ub == Double.POSITIVE_INFINITY) {
					boundsStr = " (unbounded)";
				} else if (lb == Double.NEGATIVE_INFINITY) {
					boundsStr = String.format(" <= %.6f", ub);
				} else if (ub == Double.POSITIVE_INFINITY) {
					boundsStr = String.format(" >= %.6f", lb);
				} else {
					boundsStr = String.format(" in [%.6f, %.6f]", lb, ub);
				}

				log.debug("Constraint [{}]: {}{}{}", i, name, 
						expr.length() > 0 ? ": " + expr.toString() : " (empty - " + nonZeroCoeffs + " coefficients)", 
								boundsStr);

				// Check for potentially problematic constraints
				if (lb > ub) {
					log.error("  *** INVALID: Lower bound ({}) > Upper bound ({}) ***", lb, ub);
				}
				if (lb == Double.POSITIVE_INFINITY || ub == Double.NEGATIVE_INFINITY) {
					log.error("  *** INVALID: Impossible bounds ***");
				}
			} catch (Exception e) {
				log.error("Error accessing constraint [{}]: {}", i, e.getMessage());
			}
		}
	}

}