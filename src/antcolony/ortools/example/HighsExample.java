package antcolony.ortools.example;

import com.google.ortools.Loader;
import com.google.ortools.linearsolver.MPSolver;
import com.google.ortools.linearsolver.MPVariable;
import com.google.ortools.linearsolver.MPObjective;

public class HighsExample {
	public static void main(String[] args) {
		Loader.loadNativeLibraries();

		// Create solver using HiGHS backend
		MPSolver solver = MPSolver.createSolver("HIGHS");

		if (solver == null) {
			System.out.println("HIGHS solver is not available in your OR-Tools build.");
			return;
		}

		// Variables
		MPVariable x = solver.makeNumVar(0.0, Double.POSITIVE_INFINITY, "x");
		MPVariable y = solver.makeNumVar(0.0, Double.POSITIVE_INFINITY, "y");

		// Constraints
		var c1 = solver.makeConstraint(0, 4, "c1");
		c1.setCoefficient(x, 1);
		c1.setCoefficient(y, 1);

		var c2 = solver.makeConstraint(0, 2, "c2");
		c2.setCoefficient(x, 1);

		// Objective: maximize x + 2y
		MPObjective obj = solver.objective();
		obj.setCoefficient(x, 1);
		obj.setCoefficient(y, 2);
		obj.setMaximization();

		// Solve
		var status = solver.solve();
		if (status == MPSolver.ResultStatus.OPTIMAL) {
			System.out.println("x = " + x.solutionValue());
			System.out.println("y = " + y.solutionValue());
			System.out.println("Objective = " + obj.value());
		} else {
			System.out.println("No optimal solution found.");
		}
	}
}


