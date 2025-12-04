//
//  acctions.h
//
//  Created by Eliana Fernandes
//  Copyright (c) 2015 Eliana Fernandes. All rights reserved.
//

#ifndef __ACO13_5__addsolcomp__
#define __ACO13_5__addsolcomp__

#include <iostream>
#include <fstream>
#include <time.h>

#include "acovar.h"
#include "readdata.h" 
#include "checknrprods.h"

int AddSolutionComponent(ind indices, data dados, ant& ants, double& temp_cost);

int LocalPheromoneUpdate(int prod,int hub, int node, aco& a_param);

int ApplySingleAllocationRules(int prod, int hub, int node, int nr_nodes, ant& ants, int is_node, int is_not_hub);

int ApplyLkRules(int prod, int hub, data dados,ant& ants);

int UpdateAvailableCapacities(int prod, int hub, int node, data dados, ant& ants, int k);

int OpenHub(ind indices, data dados, ant& ants, double& temp_cost);

int DedicateHub(ind indices, data dados, ant& ants, aco a_param, int k, double& temp_cost);

int GetBestAntCost(int nr_prods, int nr_nodes, ant& ants, iteration& iter, int it, int k);

int GetBestCost(data dados, iteration iter,best& bst, int it, double& global_bst, clock_t t1);

int GlobalPheromoneUpdate(data dados,aco& a,iteration iter, double scl_prm);

int GlobalDeadPheromoneUpdate(data dados,aco& a, ant ants, double scl_prm, double glbl_best);

#endif /* defined(__ACO13_5__addsolcomp__) */
