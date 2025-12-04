//
//  runcplex.h
//
//  Created by Eliana Fernandes
//  Copyright (c) 2015 Eliana Fernandes. All rights reserved.
//

#ifndef ACO_MPHL1_runcplexLR_h
#define ACO_MPHL1_runcplexLR_h

#include <ilcplex/ilocplex.h>
#include <time.h>

#include "readdata.h"
#include "getsolutions.h"
#include "acovar.h"

ILOSTLBEGIN //similar to using namespace std; in c++ but here for cplex

typedef IloArray<IloNumArray>		doubleMatrix;
typedef IloArray<IloNumVarArray>	NumVarMatrix;
typedef IloArray<NumVarMatrix>		NumVar3DMatrix;
typedef IloArray<NumVar3DMatrix>	NumVar4DMatrix;
typedef IloArray<NumVar4DMatrix>    NumVar5DMatrix;

#define MAX(x,y) ((x) > (y) ? (x): (y))

aco runCplexLR(double& time_LR,double& scl_prm,data dados, aco& a_param);

#endif /* defined(ACO_MPHL1_runcplex_h) */
