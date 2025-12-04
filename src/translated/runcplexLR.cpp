//
//
//  runcplex.cpp
//
//

#include "runcplexLR.h"

/*  ==================================================================================
	Rotina solveModel_PEK
	//  Restricoes n. minimo serviços 
	================================================================================== */
aco runCplexLR(double& time_LR,double& scl_prm,data dados, aco& a_param) {

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

	// Here we start CPLEX issues 

	// Variables x_{ik}^p
	NumVar3DMatrix x(env,dados.nbNodes);
	for (i=0; i<dados.nbNodes; i++) {
		x[i] = NumVarMatrix (env,dados.nbNodes);
		for (k=0; k<dados.nbNodes; ++k) {
			x[i][k] = IloNumVarArray(env, dados.nbProducts, 0, 1, ILOFLOAT);
		}
	}

	// Variables z
	IloNumVarArray z(env,dados.nbNodes,0,1,ILOFLOAT);

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
	   Build and solve model PEK (LR)
	   -------------------------------------------------------------- */
	try {

		IloModel model_PEK(env);
		// Single-allocation
		for (i=0; i<dados.nbNodes; i++)
			for (p=0; p<dados.nbProducts; ++p) {
				IloExpr v(env);
				for (k=0; k<dados.nbNodes; ++k)
					v+=x[i][k][p];
				model_PEK.add(v == 1);
				v.end();
			}

		// Non-hub nodes can only be allocated to open hubs
		for (i=0; i<dados.nbNodes; i++) 
			for (k=0; k<dados.nbNodes; k++) 
				for (p=0; p<dados.nbProducts; ++p)
					model_PEK.add(x[i][k][p] <= x[k][k][p]);

		// Maximum number of products handled in each location
		for (k=0; k<dados.nbNodes; ++k) {
			IloExpr v(env);
			for (p=0; p<dados.nbProducts; ++p)
				v+=x[k][k][p];
			model_PEK.add(v <= ((double)dados.L[k]*z[k]) );
			v.end();
		}

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
					v2 += dados.O[i][p]*x[i][k][p];
					for (j=0; j<dados.nbNodes; j++)
						v2 -= dados.w[i][j][p]*x[j][k][p];
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
					model_PEK.add(v <= dados.O[i][p]*x[i][k][p]);
				}

				// Enhanced capacity constraints
		for (k=0; k<dados.nbNodes; k++) {
			for (p=0; p<dados.nbProducts; ++p) {
			IloExpr v(env);
			for (i=0; i<dados.nbNodes; ++i) 
					v += ((double)dados.O[i][p])*x[i][k][p];
			model_PEK.add(v <= dados.Gamma[k][p]*x[k][k][p]);
			v.end();
			}
		}

		// Minimum number of hubs to open for each category
		// Minimum number of hubs to open
		check = (int *) malloc (sizeof(int)*dados.nbNodes);
		maxR=0;
		sumR=0;
		for (p=0; p<dados.nbProducts; p++) {
			prodDemand=0.0;
			for(i=0; i<dados.nbNodes; i++) {
				prodDemand+=dados.O[i][p];
				check[i]=0;
			}
			R=0;
			totalP=0.0;
			do {
				maxGamma=0.0;
				for (i=0; i<dados.nbNodes; i++)
//
// Na instrucao abaixo deveriamos juntar dados.O[i][p]<dados.Gamma[i][p] pois caso contrario
// no local i nao pode ser aberto um hub do tipo p e nao devemos contar com ele
//
					if ((check[i]==0 && dados.Gamma[i][p]>maxGamma)&&(dados.O[i][p]<dados.Gamma[i][p])) {
						maxGamma = dados.Gamma[i][p];
						index = i;
					}
				totalP+=maxGamma;
				check[index]=1;
				++R;
			} while (totalP < prodDemand);
			IloExpr v(env);
			for (k=0; k<dados.nbNodes; k++)
				v+=x[k][k][p];
			model_PEK.add (v >= R);
			v.end();
			maxR = MAX(maxR,R);
			sumR+=R;
		}
		free(check);
		// We now check if all the Lk's are equal or not for using the appropriate inequality
		checkL=1;
		for (k=1; k<dados.nbNodes; ++k)
			if (dados.L[k]!=dados.L[0]) {
				checkL=0;
				break;
			}
		// If not all the Lk's are equal
		if (checkL==0)
			model_PEK.add ( IloSum(z) >= maxR);
		// If all the Lk's are equal
		else {
			model_PEK.add ( IloSum(z) >= MAX(maxR,ceil(1.0*sumR/dados.L[0])) );
		}


		// Objective function
		IloExpr obj(env);
		for (p=0; p<dados.nbProducts; ++p)
			for (i=0;  i<dados.nbNodes; i++)
				for (k=0; k<dados.nbNodes; k++) {
					obj += (dados.d[i][k]*(dados.chi[p]*dados.O[i][p]+dados.delta[p]*dados.D[i][p])*x[i][k][p]);
			}
		for (p=0; p<dados.nbProducts; ++p) 
			for (i=0;  i<dados.nbNodes; i++)
				for (k=0; k<dados.nbNodes; k++)
					for (l=0; l<dados.nbNodes; l++) {
						obj += (dados.alpha[p] * dados.d[k][l] * y[i][k][l][p]);
				}
		for (k=0; k<dados.nbNodes; k++)
			obj += (dados.g[k]*z[k]);
		for (p=0; p<dados.nbProducts; ++p)
			for (k=0; k<dados.nbNodes; k++)
				obj += (dados.f[k][p]*x[k][k][p]);

		model_PEK.add(IloMinimize(env,obj));
		obj.end();
		
		cplex.extract(model_PEK);
		//cplex.exportModel("PEK_ACO.lp");

		// Turn off displays on screen when CPLEX runs
		//cplex.setOut(env.getNullStream());

		timer.restart();
		cplex.solve();
		timer.stop();
		vOpt=cplex.getObjValue();
		tOpt=timer.getTime();
		scl_prm=vOpt;
		time_LR=tOpt;
		cout <<"scl_prm= "<<scl_prm<<endl;
#ifdef USE_LR	
		// copy LR x values to tau
		for (i=0; i<dados.nbNodes; i++)
			for (k=0; k<dados.nbNodes; k++) 
				for (p=0; p<dados.nbProducts; ++p) {
					//if(cplex.getValue(x[i][k][p])>0)
						a_param.tau0[i][k][p]=(double) cplex.getValue(x[i][k][p]);
				}
#ifdef HISTORY
				ofstream myfile;
				myfile.open ("history1.txt",ios::app);
				myfile<<"CPLEX LR for TAU0 COMPUTED"<<endl;
				myfile.close();
#endif
#endif		
		cplex.clearModel();
		model_PEK.end();
	
	}
	catch (IloException& e) {
		timer.stop();
		cerr << " Error: " << e << endl;
		ofstream outERROR("cplex_LR ERROR",ios::app);
		outERROR << "model PEK LR              " << e ;
		outERROR.close();
		cplex.clearModel();
	}
	catch (...) {
		timer.stop();
		cerr << " Error: " << endl;
		ofstream outERROR("cplex_LR ERROR",ios::app);
		outERROR.open("cplex_LR ERROR",ios::app);
		outERROR << "model PEK LR              " << "Error" ;
		outERROR.close();
		cplex.clearModel();
	}

	env.end();

	return (a_param);

} // runCplexLR(const char *fichIn, const char *fichOut)
