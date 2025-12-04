//
//
//  runcplex.cpp
//
//

#include "runcplex.h"

/*  ==================================================================================
	Rotina solveModel_PEK
	//  Restricoes n. minimo serviços 
	================================================================================== */
int runCplex(data dados, solution& sol, double& iter_cost,const char *fichIn, const char *fichOut) {

	IloEnv 	env;

	// Counters
	int		i,j,k,l,p,nz;

	// Auxiliar variables
	double	aux;

	// Cplex results
	double	vOpt, tOpt;

	// Variables for the enhanced models
	int		qLim;
	int		q;
	int		R,maxR;
	double	prodDemand;
	double	totalP,maxGamma;
	int		*check;
	int		checkL,sumR;
	int		index;
	double	sumO;

	// Concert timer
	IloTimer timer(env);

	// Log file if LOGG is defined
#ifdef LOGG
	const char* fLog = "output_cplex.log";
#endif

	// output files
	ofstream outFile(fichOut,ios::app);
	outFile.close();

	// Log file if LOGG is defined
#ifdef LOGG
	ofstream logFile(fLog,ios::app);
	logFile.setf(ios::fixed);
	logFile << setw(12) << fichIn << endl;
	logFile.close();
#endif
							
	// Here we start CPLEX issues 
		// Variables y_{ikl}^p
	NumVar4DMatrix y(env,dados.nbNodes);
	for (i=0; i<dados.nbNodes; i++) {
		y[i] = NumVar3DMatrix (env,dados.nbNodes);
		for (k=0; k<dados.nbNodes; k++) {
			y[i][k] = NumVarMatrix (env,dados.nbNodes);
			for (l=0; l<dados.nbNodes; ++l)
				y[i][k][l] = IloNumVarArray(env, dados.nbProducts, 0, IloInfinity, ILOFLOAT);
		}
	}

	// Ambiente CPLEX
	IloCplex cplex(env);

	// CPLEX parameters
	cplex.setParam(IloCplex::TiLim, 10800); //3 wall clock hours
	cplex.setParam(IloCplex::ClockType,  1);   // 1 - CPU time is measured, 2 - Wall Clock Time is measured
	//cplex.setParam(IloCplex::ParallelMode, 1);
//	cplex.setParam(IloCplex::EpGap, 0.00001);
//	cplex.setParam(IloCplex::EpInt, 0.00001);
	// cplex.setParam(IloCplex::NumericalEmphasis, 1);
	// tolerance = cplex.getParam(IloCplex::EpInt); // Get current tolerance defined
	
	/* --------------------------------------------------------------
	   Build and solve model PEK
	   -------------------------------------------------------------- */
	try {

		IloModel model_PEK(env);

		// Flow divergence
		for (p=0; p<dados.nbProducts; ++p)		
			for (i=0; i<dados.nbNodes; i++)
				for (k=0; k<dados.nbNodes; k++) {
					IloExpr v1(env);
					IloExpr v2(env);
					for (l=0; l<dados.nbNodes; l++)
						v1 += y[i][k][l][p];
					for (l=0; l<dados.nbNodes; l++)
						v1 -= y[i][l][k][p];
					v2 += dados.O[i][p]*sol.x[i][k][p];
					for (j=0; j<dados.nbNodes; j++)
						v2 -= dados.w[i][j][p]*sol.x[j][k][p];
					model_PEK.add(v1 == v2);
					v1.end();
					v2.end();
				}

		// Missing cuts
		for (i=0; i<dados.nbNodes; i++)
			for (k=0; k<dados.nbNodes; k++)
				for (p=0; p<dados.nbProducts; ++p) {
					IloExpr v(env);
					for (l=0; l<dados.nbNodes; ++l) {
						if (l != k) v+=y[i][k][l][p];
					}
					model_PEK.add(v <= dados.O[i][p]*sol.x[i][k][p]);
				}

		// Objective function
		IloExpr obj(env);
		for (p=0; p<dados.nbProducts; ++p)
			for (i=0;  i<dados.nbNodes; i++)
				for (k=0; k<dados.nbNodes; k++) {
					obj += (dados.d[i][k]*(dados.chi[p]*dados.O[i][p]+dados.delta[p]*dados.D[i][p])*sol.x[i][k][p]);
			}
		for (p=0; p<dados.nbProducts; ++p) 
			for (i=0;  i<dados.nbNodes; i++)
				for (k=0; k<dados.nbNodes; k++)
					for (l=0; l<dados.nbNodes; l++) {
						obj += (dados.alpha[p] * dados.d[k][l] * y[i][k][l][p]);
				}
		for (k=0; k<dados.nbNodes; k++)
			obj += (dados.g[k]*sol.z[k]);
		for (p=0; p<dados.nbProducts; ++p)
			for (k=0; k<dados.nbNodes; k++)
				obj += (dados.f[k][p]*sol.x[k][k][p]);

		model_PEK.add(IloMinimize(env,obj));
		obj.end();
		
		cplex.extract(model_PEK);
		//cplex.exportModel("PEK_ACO.lp");

		// Turn off displays on screen when CPLEX runs
		cplex.setOut(env.getNullStream());

		timer.restart();
		cplex.solve();
		timer.stop();

		vOpt=cplex.getObjValue();
		tOpt=timer.getTime();

		// get y values
		for (i=0; i<dados.nbNodes; i++) 
			for (k=0; k<dados.nbNodes; k++) 
				for (l=0; l<dados.nbNodes; l++) 
					for(p=0; p<dados.nbProducts; p++) {
						if (l != k) {
							if ( cplex.getValue(y[i][k][l][p]) > 0.000001) {
								sol.y[i][k][l][p]=cplex.getValue(y[i][k][l][p]);
								//cout << "y[" << i << "][" << k << "][" << l << "][" << p << "] = " << sol.y[i][k][l][p] << endl;
							}
						}
					}

#ifdef LOGG
//		// Write in the log file
		logFile.open(fLog,ios::app);
		logFile.setf(ios::fixed);
		logFile << "MODEL - ACO" << endl;
		logFile << "Optimal value : ";
		logFile << setw(7) << setprecision(2) << vOpt << endl;
		logFile << "CPU           : ";
		logFile << setw(7) << setprecision(2) << tOpt << " seconds" << endl;
		logFile.close();
#endif

		sol.cost=vOpt;
		iter_cost=vOpt;

		nz=0;
		for (k=0; k<dados.nbNodes; k++)
			if (sol.z[k] == 1)
				nz=nz+1;
		outFile << setw(12) << nz << "  ";
		outFile << endl;
		outFile.close();

#ifdef LOGG
		// Write in the log file
		logFile.open(fLog,ios::app);
		logFile.setf(ios::fixed);
		logFile << endl;
		logFile << "MODEL PEK" << endl;
		logFile << "Optimal value : ";
		logFile << setw(7) << setprecision(2) << vOpt << endl;
		logFile << "CPU           : ";
		logFile << setw(7) << setprecision(2) << tOpt << " seconds" << endl;
		logFile << endl;
		logFile << "Variables z equal to 1: " << endl;
		for (k=0; k<dados.nbNodes; k++)
			if (sol.z[k] == 1)
				logFile << "z[" << k << "]" << endl;
		logFile << endl;
		logFile << "Variables x equal to 1: " << endl;
		for (i=0; i<dados.nbNodes; i++)
			for (k=0; k<dados.nbNodes; k++) 
				for (p=0; p<dados.nbProducts; ++p) {
					if (sol.x[i][k][p] == 1)
						logFile << "x[" << i << "][" << k << "][" << p << "]" << endl;
				}
		logFile << endl;
		logFile << "Variables y: " << endl;
		for (i=0; i<dados.nbNodes; i++) 
			for (k=0; k<dados.nbNodes; k++) 
				for (l=0; l<dados.nbNodes; l++) 
					for(p=0; p<dados.nbProducts; ++p) {
						if (l != k) {
							if ( cplex.getValue(y[i][k][l][p]) > 0.000001) {
								logFile << "y[" << i << "][" << k << "][" << l << "][" << p << "] = ";
								logFile << setw(7) << setprecision(2) << cplex.getValue(y[i][k][l][p]) << endl ;
							}
						}
					}
		logFile << endl;
		logFile.close();

#endif

		cplex.clearModel();
		model_PEK.end();
	
	}
	catch (IloException& e) {
		timer.stop();
		cerr << " Error: " << e << endl;
		outFile.open(fichOut,ios::app);
		outFile << "model PEK               " << e ;
		outFile.close();
		cplex.clearModel();
	}
	catch (...) {
		timer.stop();
		cerr << " Error: " << endl;
		outFile.open(fichOut,ios::app);
		outFile << "model PEK               " << "Error" ;
		outFile.close();
		cplex.clearModel();
	}

	env.end();

	return 0;

} // runCplex(data dados, solution& sol, double& iter_cost,const char *fichIn, const char *fichOut)

