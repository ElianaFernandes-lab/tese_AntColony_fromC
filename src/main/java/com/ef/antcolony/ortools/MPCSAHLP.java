package main.java.com.ef.antcolony.ortools;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.ortools.Loader;
import com.google.ortools.linearsolver.MPConstraint;
import com.google.ortools.linearsolver.MPObjective;
import com.google.ortools.linearsolver.MPSolver;
import com.google.ortools.linearsolver.MPVariable;

import main.java.com.ef.antcolony.Aco;
import main.java.com.ef.antcolony.GetSolutions.Solution;
import main.java.com.ef.antcolony.ReadData.Data;
import main.java.com.ef.antcolony.constants.AcoVar;

public class MPCSAHLP {

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
							aco.tau0[p][i][j] = this.x[p][i][j].solutionValue();
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
							aco.tau0[p][i][j] = this.x[p][i][j].solutionValue();
						}
					}
				}

				log.debug("{} LR for TAU0 COMPUTED - Objective: {}, Runtime: {}s", solverId, obj.value(), runtimeSec);
				return true;
			} else {
				log.error("No solution found. Result status: {}", resultStatus);
				if (resultStatus == MPSolver.ResultStatus.INFEASIBLE && AcoVar.LOG_MPCSAHLP) {
					logInfeasibleConstraints(solver);
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
		// 3.4. hub assignment constraints
		// x[p][i][k] <= x[p][k][k]
		// ==============================
		log.debug("Adding hub assignment constraints...");
		addHubAssignmentConstraints(solver);
		log.debug("Added ub assignment constraints. Total constraints: {}", solver.numConstraints());

		// ==============================
		// 3.5. Capacity per hub
		// sum_i O[i][p] * x[i][k][p] <= gamma[k][p] * x[k][k][p]
		// ==============================
		log.debug("Adding capacity constraints...");
		addCapacityConstraints(solver, data);
		log.debug("Added capacity constraints. Total constraints: {}", solver.numConstraints());

		// ==============================
		// 3.6 Maximum products at hub
		// sum_p x[k][k][p] <= L[k] * z[k]
		// ==============================
		log.debug("Adding max products at hub constraints...");
		addMaxHubProdConstraints(solver, data);
		log.debug("Added max products at hub constraints. Total constraints: {}", solver.numConstraints());

		// ==============================
		// 3.13. Single allocation constraint
		// ==============================
		log.debug("Adding single allocation constraints...");
		addSingleAllocationConstraints(solver);
		log.debug("Added single allocation constraints. Total constraints: {}", solver.numConstraints());

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
		this.x = new MPVariable[this.nbProducts][this.nbNodes][this.nbNodes];
		for (int p = 0; p < this.nbProducts; p++) {
			for (int i = 0; i < this.nbNodes; i++) {
				for (int j = 0; j < this.nbNodes; j++) {
					if(this.isLinearRelaxation) {
						this.x[p][i][j] = solver.makeNumVar(0.0, 1.0, "x_" + p + "_" + i + "_" + j);
					} else if (this.isForcingSolution) {
						double fixedBound = solutionX[p][i][j];
						this.x[p][i][j] = solver.makeNumVar(fixedBound, fixedBound , "x_" + p + "_" + i + "_" + j);
					} else {
						this.x[p][i][j] = solver.makeBoolVar("x_" + p + "_" + i + "_" + j);
					}
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
						this.y[p][i][k][l] = solver.makeNumVar(0.0, Double.POSITIVE_INFINITY, "y_" + p + "_" + i + "_" + k + "_" + l);
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
		for (int j = 0; j < this.nbNodes; j++) {
			if(this.isForcingSolution) {
				if(solutionZ != null) {
					double fixedBound = solutionZ[j];
					this.z[j] = solver.makeIntVar(fixedBound, fixedBound, "z_" + j);
				} else {
					log.error("isForcingSolution and solutionZ is null");
				}

			} else {
				this.z[j] = solver.makeBoolVar("z_" + j);
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
			obj.setCoefficient(this.z[i], coeff);
		}
	}

	private void addDedicateHubCosts(Data data, MPObjective obj) {
		for (int p = 0; p < this.nbProducts; p++) {
			for (int i = 0; i < this.nbNodes; i++) {
				double coeff = data.f[p][i];
				obj.setCoefficient(this.x[p][i][i], coeff); // Note: i appears twice for diagonal
			}
		}
	}

	private void addFlowRoutingCosts(Data data, MPObjective obj) {
		for (int p = 0; p < this.nbProducts; p++) {
			for (int i = 0; i < this.nbNodes; i++) {  // Assuming I nodes
				for (int k = 0; k < this.nbNodes; k++) {  // Assuming K nodes
					for (int l = 0; l < this.nbNodes; l++) {  // Assuming L nodes
						double coeff = data.alpha[p] * data.d[k][l];
						obj.setCoefficient(this.y[p][i][k][l], coeff);
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
					obj.setCoefficient(this.x[p][i][k], coeff);
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
				String name = String.format(S_S_S, SINGLE_ALLOCATION_CSTRNT_NAME, p, i);
				MPConstraint c = solver.makeConstraint(1.0, 1.0, name);

				// Sum x[i][k][p] over all k in K
				for (int k = 0; k < this.nbNodes; k++) {
					c.setCoefficient(this.x[p][i][k], 1.0);
				}
			}
		}
	}

	private void addHubAssignmentConstraints(MPSolver solver) {
		// Constraint: x[p][i][j] <= x[p][j][j]
		// (If node i is allocated to hub j for product p, then j must be a hub for product p)
		// Rewritten as: x[p][i][j] - x[p][j][j] <= 0
		for (int p = 0; p < this.nbProducts; p++) {
			for (int i = 0; i < this.nbNodes; i++) {
				for (int k = 0; k < this.nbNodes; k++) {

					// Skip self-allocation: x[p][j][j] <= x[p][j][j] is trivial
					if (i == k) {
						continue;
					}
					String name = String.format(S_S_S_S, HUB_ASSIGNENT_CSTRNT_NAME, p, i, k);
					MPConstraint c = solver.makeConstraint(Double.NEGATIVE_INFINITY, 0.0, name);

					c.setCoefficient(this.x[p][i][k], 1.0);
					c.setCoefficient(this.x[p][k][k], -1.0);
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
		for (int j = 0; j < this.nbNodes; j++) {
			String name = String.format(S_S, HUB_MAX_PROD_CSTRNT_NAME, j);
			MPConstraint c = solver.makeConstraint(Double.NEGATIVE_INFINITY, 0.0, name);

			for (int p = 0; p < this.nbProducts; p++) {
				c.setCoefficient(this.x[p][j][j], 1.0);
			}

			c.setCoefficient(this.z[j], -data.L[j]);
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
		for (int p = 0; p < this.nbProducts; p++) {
			for (int i = 0; i < this.nbNodes; i++) {
				for (int k = 0; k < this.nbNodes; k++) {
					String name = String.format(S_S_S_S, FLOW_DIVERGENCE_CSTRNT_NAME, p, i, k);
					MPConstraint c = solver.makeConstraint(0.0, 0.0, name);

					// Flow conservation at hub k for origin i
					for (int l = 0; l < this.nbNodes; l++) {
						c.setCoefficient(this.y[p][i][k][l], 1.0);   // outflow from k
						c.setCoefficient(this.y[p][i][l][k], -1.0);  // inflow to k
					}

//					for (int l = 0; l < this.nbNodes; l++) {
//						
//					}

					// Supply/demand terms
					c.setCoefficient(this.x[p][i][k], -data.originatedFlow[p][i]);

					for (int j = 0; j < this.nbNodes; j++) {
						c.setCoefficient(this.x[p][j][k], data.w[p][i][j]);
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
		for (int p = 0; p < this.nbProducts; p++) {
			for (int i = 0; i < this.nbNodes; i++) {
				for (int k = 0; k < this.nbNodes; k++) {
					String name = String.format(S_S_S_S, FLOW_ALLOC_LINK_HUB_CSTRNT_NAME, p, i, k);
					MPConstraint c = solver.makeConstraint(Double.NEGATIVE_INFINITY, 0.0, name);

					
					for (int l = 0; l < this.nbNodes; l++) {
						c.setCoefficient(this.y[p][i][k][l], 1.0);
					}

					c.setCoefficient(this.x[p][i][k], -data.originatedFlow[p][i]);
				}
			}
		}
	}

	private void addCapacityConstraints(MPSolver solver, Data data) {
		// Constraint: sum_i O[i][p] * x[i][k][p] <= gamma[k][p] * x[k][k][p]
		// Rewritten as: sum_i O[i][p] * x[i][k][p] - gamma[k][p] * x[k][k][p] <= 0

		for (int p = 0; p < this.nbProducts; p++) {
			for (int k = 0; k < this.nbNodes; k++) {

				String name = String.format(S_S_S, CAPACITY_CSTRNT_NAME, p, k);
				MPConstraint c = solver.makeConstraint(Double.NEGATIVE_INFINITY, 0.0, name);

				// Sum of allocated demand to hub k for product p
				for (int i = 0; i < this.nbNodes; i++) {
					c.setCoefficient(this.x[p][i][k], data.originatedFlow[p][i]);
				}

				// Hub capacity for product p
				c.setCoefficient(this.x[p][k][k], -data.gamma[p][k]);
			}
		}
	}

	private void addMinimumHubsPerProdConstraints(MPSolver solver, Data data) {
		for (int p = 0; p < this.nbProducts; p++) {
			// Calculate total demand for product p
			double prodDemand = 0.0;
			for (int i = 0; i < this.nbNodes; i++) {
				prodDemand += data.originatedFlow[p][i];
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
					if (data.gamma[p][idx] > bestGamma && data.originatedFlow[p][idx] < data.gamma[p][idx]) {
						bestGamma = data.gamma[p][idx];
						bestIdx = idx;
					}
				}

				if (bestIdx == -1) break;

				totalCovered += bestGamma;
				candidateHubs.remove(Integer.valueOf(bestIdx));
				r++;
			}

			// Add constraint: sum of hubs for product p >= r
			String name = String.format(S_S, MINIMUM_HUBS_PER_PRODS_CSTRNT_NAME, p);
			MPConstraint c = solver.makeConstraint(r, Double.POSITIVE_INFINITY,  name);

			for (int k = 0; k < this.nbNodes; k++) {
				c.setCoefficient(this.x[p][k][k], 1.0);
			}

			log.debug("Product {} requires minimum {} hubs to cover demand {}", p, r, prodDemand);
		}
	}

	private void logSolution(MPSolver solver, MPObjective obj, double runtimeSec) {
		log.debug("Solution found. Objective = {}, Runtime = {} s", obj.value(), runtimeSec);
//		logZ();
		logX();
//		logY();
		log.debug("Objective value = {}", obj.value());
		log.debug("Problem solved in {} milliseconds", + solver.wallTime());
		log.debug("Problem solved in {} iterations", solver.iterations());
		log.debug("Problem solved in {} branch-and-bound nodes", solver.nodes());
		if (AcoVar.LOG_MPCSAHLP) {
			String lpFormat = solver.exportModelAsLpFormat(false);
			log.debug("Model in LP format:\n{}", lpFormat);
		}
		
	}

	private void logY() {
		for (int p = 0; p < this.nbProducts; p++) {
			for (int i = 0; i < this.nbNodes; i++) {
				for (int k = 0; k < this.nbNodes; k++) {
					for (int l = 0; l < this.nbNodes; l++) {
						log.debug("y[{}][{}][{}][{}] = {}", p, i, k, l, this.y[p][i][k][l].solutionValue());
					}
				}
			}
		}
	}

	private void logX() {
		for (int p = 0; p < this.nbProducts; p++) {
			for (int i = 0; i < this.nbNodes; i++) {
				for (int j = 0; j < this.nbNodes; j++) {
					log.debug("x[{}][{}][{}] = {}", p, i, j, this.x[p][i][j].solutionValue());
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
		log.error("=========================================");
		log.error("INFEASIBLE MODEL - Constraint Analysis");
		log.error("=========================================");
		log.error("Total constraints: {}", solver.numConstraints());
		log.error("Total variables: {}", solver.numVariables());
		
		// Export model to LP format for external analysis
		try {
			String lpFormat = solver.exportModelAsLpFormat(false);
			log.error("Model in LP format:\n{}", lpFormat);
		} catch (Exception e) {
			log.error("Could not export LP format: {}", e.getMessage());
		}
		
		log.error("----------------------------------------");
		log.error("Constraint Details:");
		log.error("----------------------------------------");
		
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
				
				log.error("Constraint [{}]: {}{}{}", i, name, 
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
		
		log.error("----------------------------------------");
		log.error("Variable Bounds Summary:");
		log.error("----------------------------------------");
		
		// Log variable bounds that might be problematic
		int invalidVars = 0;
		if (this.x != null) {
			for (int p = 0; p < this.nbProducts; p++) {
				for (int i = 0; i < this.nbNodes; i++) {
					for (int j = 0; j < this.nbNodes; j++) {
						if (this.x[p][i][j] != null) {
							double lb = this.x[p][i][j].lb();
							double ub = this.x[p][i][j].ub();
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
		
		log.error("=========================================");
	}

}