//
//  actions.cpp
//
//  Created by Eliana Fernandes
//  Copyright (c) 2015 Eliana Fernandes. All rights reserved.
//

#include "actions.h"

int AddSolutionComponent(ind indices, data dados, ant& ants, double& temp_cost){
    // add solution component
    ants.x[indices.node][indices.prod]=indices.hub;
#ifdef HISTORY
	ofstream myfile;
	myfile.open ("history1.txt",ios::app);
	myfile<<endl<<"Solution Component Added"<<endl;
	myfile << "ants.x["<<indices.node<<"]["<<indices.prod<<"]= " << indices.hub << endl;
	myfile.close();
#endif
    // remove solution component of available solutions
    ants.avail_tau[indices.node][indices.hub][indices.prod]=0;
    // Compute transfer and collection costs
    // update costs
#ifdef HISTORY
	myfile.open ("history1.txt",ios::app);
	myfile<<endl<<"update transfer and collection costs costs from: "<<endl;
	myfile << "ants.cost "<<ants.cost << endl;
	myfile << "temp_cost "<<temp_cost << endl;
	myfile.close();
#endif
    temp_cost = ants.cost;
    ants.cost=temp_cost+dados.d[indices.node][indices.hub]*(dados.chi[indices.prod]*dados.O[indices.node][indices.prod]+dados.delta[indices.prod]*dados.D[indices.node][indices.prod]);
    temp_cost = ants.cost;
#ifdef HISTORY
	myfile.open ("history1.txt",ios::app);
	myfile<<endl<<"update transfer and collection costs costs to: "<<endl;
	myfile << "ants.cost "<<ants.cost << endl;
	myfile << "temp_cost "<<temp_cost << endl;
	myfile.close();
#endif
	// Compute fixed costs if the node is to be a hub
	if(indices.node==indices.hub){
#ifdef HISTORY
		myfile.open ("history1.txt",ios::app);
		myfile<<endl<<"update fixed costs from: "<<endl;
		myfile << "ants.cost "<<ants.cost << endl;
		myfile << "temp_cost "<<temp_cost << endl;
		myfile.close();
#endif
		temp_cost = ants.cost;
		ants.cost=temp_cost+dados.f[indices.hub][indices.prod];
		temp_cost = ants.cost;
#ifdef HISTORY
		myfile.open ("history1.txt",ios::app);
		myfile<<endl<<"update fixed costs to: "<<endl;
		myfile << "ants.cost "<<ants.cost << endl;
		myfile << "temp_cost "<<temp_cost << endl;
		myfile.close();
#endif
	}
    return 0;   
}

int LocalPheromoneUpdate(int prod,int hub, int node, aco& a_param){
    double temp_tau;
#ifdef TAU_HIST
	ofstream taufile;
	taufile.open ("tau_hist.txt",ios::app);
	taufile << "tau local updated"<< endl;
	taufile << "a.tau"<<"["<<node<<"]["<<hub<<"]["<<prod<<"] = "<< a_param.tau[hub][node][prod] << endl;
	taufile.close();
#endif
    temp_tau=a_param.tau[node][hub][prod];
    a_param.tau[node][hub][prod]=(1-RHO)*temp_tau+RHO*a_param.tau0[node][hub][prod];
#ifdef TAU_HIST
	taufile.open ("tau_hist.txt",ios::app);
	taufile << "a.tau"<<"["<<node<<"]["<<hub<<"]["<<prod<<"] = "<< a_param.tau[hub][node][prod] << endl;
	taufile.close();
#endif
    return 0;
}

int ApplySingleAllocationRules(int prod, int hub, int node, int nr_nodes, ant& ants, int is_node, int is_not_hub){
    int i,j;
#ifdef HISTORY
	ofstream myfile;
	myfile.open ("history1.txt",ios::app);
	myfile<<endl<<"SOLUTION COMPONENTS MADE UNAVAILABLE SA RULES"<<endl;
	myfile.close();
#endif
    // node SA
    // if node i is assigned to hub j, it cannot be assigned to another hub (Single Allocation)
    // prevent multiple allocation of node i for product p
	if(is_node>0)
		for(j=0;j<nr_nodes;j++){
			if(ants.avail_tau[node][j][prod]>0){
				ants.avail_tau[node][j][prod]=0;
#ifdef HISTORY
				myfile.open ("history1.txt",ios::app);
				myfile << "ants.avail_tau"<<"["<<node<<"]["<<j<<"]["<<prod<<"] = "<< ants.avail_tau[node][j][prod] << endl;
				myfile.close();
#endif
			}
		}
    // node_not_hub SA
    // once a node is allocated to a hub, it cannot become a hub because of single allocation
    // (the hubs are alocated to themselves)
    // so all connections to that node are forbidden
    // if that node is a hub, this cannot be done because hubs don't have connection limitations
		if(is_not_hub>0)
			for(i=0;i<nr_nodes;i++){
				if(ants.avail_tau[i][node][prod]>0){
					ants.avail_tau[i][node][prod]=0;
#ifdef HISTORY
					myfile.open ("history1.txt",ios::app);
					myfile << "ants.avail_tau"<<"["<<i<<"]["<<node<<"]["<<prod<<"] = "<< ants.avail_tau[i][node][prod] << endl;
					myfile.close();
#endif
				}
			}
    return 0;
}

int ApplyLkRules(int prod, int hub, data dados,ant& ants){
    
    if(dados.L[prod]<dados.nbProducts){
#ifdef HISTORY
		ofstream myfile;
		myfile.open ("history1.txt",ios::app);
		myfile<<endl<<"SOLUTION COMPONENTS MADE UNAVAILABLE Lk Rules"<<endl;
		myfile.close();
#endif
        
        int i,p;        
        hubpcount np;        
        np = checkProdConnects(dados.nbProducts, dados.nbNodes, ants);
        
        int iff=0;
        // if the hub is "full" (reached full product capacity)
        if(np.prods[hub]>=dados.L[hub]){
            iff=1;
            for(p=0;p<dados.nbProducts;p++)
				if(ants.x[hub][p]!=hub) //Se já for hub nao ha problema
                for(i=0;i<dados.nbNodes;i++){
					if(ants.avail_tau[i][hub][p]>0){
						ants.avail_tau[i][hub][p]=0;
#ifdef HISTORY
						myfile.open ("history1.txt",ios::app);
						myfile << "ants.avail_tau"<<"["<<i<<"]["<<hub<<"]["<<p<<"] = "<< ants.avail_tau[i][hub][p] << endl;
						myfile.close();
#endif
					}
				}
		}
		delete [] np.prods;
	}	
    return 0;
}


int UpdateAvailableCapacities(int prod, int hub, int node, data dados, ant& ants, int k){
#ifdef HISTORY
	ofstream myfile;
	myfile.open ("history1.txt",ios::app);
	myfile<<endl<<"CAPACITY UPDATE FROM "<<ants.avail_cap[hub][prod];
	myfile.close();
#endif
	double temp_cap;
	temp_cap=ants.avail_cap[hub][prod]; //NEW
    ants.avail_cap[hub][prod]=temp_cap-dados.O[node][prod];
#ifdef HISTORY
	myfile.open ("history1.txt",ios::app);
	myfile<<" to "<<ants.avail_cap[hub][prod]<<endl;
	myfile.close();
#endif
    return 0;
}

int OpenHub(ind indices, data dados, ant& ants, double& temp_cost){
    
	if(ants.z[indices.hub]<1){
		ants.z[indices.hub]=1;
#ifdef HISTORY
			ofstream myfile;
			myfile.open ("history1.txt",ios::app);
			myfile<<endl<<"OPEN HUB: "<< indices.hub <<endl;
			myfile<<endl<<"update fixed costs (open hub) from: "<<endl;
			myfile << "ants.cost "<<ants.cost << endl;
			myfile << "temp_cost "<<temp_cost << endl;
			myfile.close();
#endif
        // update costs
        temp_cost = ants.cost;
        ants.cost=temp_cost+dados.g[indices.hub];
        temp_cost = ants.cost;
#ifdef HISTORY
			myfile.open ("history1.txt",ios::app);
			myfile<<endl<<" to: "<<endl;
			myfile << "ants.cost "<<ants.cost << endl;
			myfile << "temp_cost "<<temp_cost << endl;
			myfile.close();
#endif
    } //if(ants.z[indices.hub]<1)
    return 0;
} //int OpenHub(ind indices, data dados, ant& ants, double& temp_cost)

int DedicateHub(ind indices, data dados, ant& ants, aco a_param, int k, double& temp_cost){
    
    if(indices.node!=indices.hub && ants.x[indices.hub][indices.prod]!=indices.hub && ants.avail_tau[indices.hub][indices.hub][indices.prod]>0){
        
        /// por causa das funções que se chamam dentro desta
        int prod=indices.prod;
        //int node=indices.node;
        int hub=indices.hub;
        
        ants.x[hub][prod]=hub;
#ifdef HISTORY
		ofstream myfile;
		myfile.open ("history1.txt",ios::app);
		myfile<<endl<<"Solution Component Added"<<endl;
		myfile << "ants.x["<<hub<<"]["<<prod<<"]= " << hub << endl;
		myfile.close();
#endif
		// update fixed costs
#ifdef HISTORY
		myfile.open ("history1.txt",ios::app);
		myfile<<endl<<"update fixed costs (dedicate hub) from: "<<endl;
		myfile<<endl<<"update fixed costs from: "<<endl;
		myfile << "ants.cost "<<ants.cost << endl;
		myfile << "temp_cost "<<temp_cost << endl;
		myfile.close();
#endif
		temp_cost = ants.cost;
		ants.cost=temp_cost+dados.f[hub][prod];
		temp_cost = ants.cost;
#ifdef HISTORY
		myfile.open ("history1.txt",ios::app);
		myfile<<endl<<"update fixed costs to: "<<endl;
		myfile << "ants.cost "<<ants.cost << endl;
		myfile << "temp_cost "<<temp_cost << endl;
		myfile << "dados.d[hub][hub] = " << dados.d[hub][hub]<<endl;
		myfile.close();
#endif

		// Compute transfer and collection costs
		// update costs
		temp_cost = ants.cost;
		ants.cost=temp_cost+dados.d[hub][hub]*(dados.chi[prod]*dados.O[hub][prod]+dados.delta[prod]*dados.D[hub][prod]);
		temp_cost = ants.cost;
        
        // remove solution component of available solutions
        ants.avail_tau[hub][hub][prod]=0;
        
        // prevent multiple allocation of node index.hub for product p
        // is_not_hub of this function is 0 because !0=1 and this is a hub
        ApplySingleAllocationRules(prod, hub, hub, dados.nbNodes, ants, 1, 0);
        
        //Apply Lk restrictions (to only allow available solutions to be added)
        ApplyLkRules(prod, hub, dados, ants);

        // local pheromone updating
        LocalPheromoneUpdate(prod, hub, hub, a_param);
        
        // update available capacity of the opened hub
        UpdateAvailableCapacities(prod, hub, hub, dados, ants, k);
        
        /// not necessary but left here just in case
        if(ants.avail_cap[hub][prod]<=0){
			cout << "actions capacity violation "<<endl;
            system ("pause");
        }
    }
    return 0;
}

int GetBestAntCost(int nr_prods, int nr_nodes, ant& ants, iteration& iter, int it, int k){
    

    if(ants.cost<=iter.best_cost){
        iter.best_cost=ants.cost;
        iter.best_ant=k;
        
		// best solution
        // write best solution
		for(int p=0;p<nr_prods;p++)
			for(int i=0;i<nr_nodes;i++){
				iter.x_best[i][p] = ants.x[i][p];
#ifdef BAHIST
				ofstream myfile;
				myfile.open ("BESTANThistory.txt",ios::app);
				myfile<<"iter.x_best["<<i<<"]["<<p<<"] = "<<iter.x_best[i][p]<<endl;
				myfile<<"ants["<<i<<"]["<<p<<"] = "<<ants.x[i][p]<<endl;
				myfile.close();
#endif
			}
	
		for(int j=0;j<nr_nodes;j++){
			iter.z_best[j]=ants.z[j];
		}
	}

    return 0;
}

int GetBestCost(data dados, iteration iter,best& bst, int it, double& global_bst, clock_t t1){
    
    clock_t t2;
    double bst_cost=iter.best_cost;
	double aux_iter_best_cost,aux_global_bst;

	aux_iter_best_cost = bst_cost;
	aux_global_bst=global_bst;

	// BEST
    if(bst_cost<=global_bst){
        global_bst=bst_cost;
        bst.cost=bst_cost;
		bst.nr_iter=it;
		t2=clock();
		//////AAA////////
		/// global best solution
		// best solution
		// save best solution
		for(int p=0;p<dados.nbProducts;p++)
			for(int i=0;i<dados.nbNodes;i++){
				bst.x[i][p] = iter.x_best[i][p];
			}

			for(int j=0;j<dados.nbNodes;j++){
				bst.z[j]=iter.z_best[j];
			}
		//////AAA////////
        double diff ((double)t2-(double)t1);
		if(aux_iter_best_cost<aux_global_bst){
			bst.nr_iter=it;
			bst.time = diff / CLOCKS_PER_SEC;
			}
    }
    return 0;
}

int GlobalPheromoneUpdate(data dados,aco& a,iteration iter,double scl_prm){
    int p,i,j;
    double temp_tau=0.0;
	
    for(p=0;p<dados.nbProducts;p++)
        for(i=0;i<dados.nbNodes;i++){
            j=iter.x_best[i][p];
			if(j==-1)
				cout <<"j= "<<j<<endl;
            temp_tau = a.tau[i][j][p];
			a.tau[i][j][p]=(1-GAMMA)*temp_tau+scl_prm*SCALING_PARAMETER*GAMMA*(1/(iter.best_cost));
			//a.tau[j][j][p]=(1-GAMMA)*temp_tau+pow(scl_prm,SCL_P)*SCALING_PARAMETER*GAMMA*(1/(iter.best_cost));
        }
    return 0;
}

int GlobalDeadPheromoneUpdate(data dados,aco& a, ant ants, double scl_prm,double glbl_best){
    int p,i,j;
    double temp_tau=0.0;
	
    for(p=0;p<dados.nbProducts;p++)
        for(j=0;j<dados.nbNodes;j++){
#ifdef TAU_HIST
			ofstream taufile;
			taufile.open ("tau_hist.txt",ios::app);
			taufile << "tau Global updated"<< endl;
			taufile << "a.tau"<<"["<<j<<"]["<<j<<"]["<<p<<"] = "<< a.tau[j][j][p] << endl;
			taufile.close();
#endif
			temp_tau = a.tau[j][j][p];
			a.tau[j][j][p]=(1-GAMMA)*temp_tau+scl_prm*SCALING_PARAMETER*GAMMA*(1/(glbl_best/10000));
			//a.tau[j][j][p]=(1-GAMMA)*temp_tau+pow(scl_prm,SCL_P)*SCALING_PARAMETER*GAMMA*(1/(glbl_best/10000));
#ifdef TAU_HIST
			taufile.open ("tau_hist.txt",ios::app);
			taufile << "a.tau"<<"["<<j<<"]["<<j<<"]["<<p<<"] = "<< a.tau[j][j][p] << endl;
			taufile.close();
#endif
		}
    return 0;
}