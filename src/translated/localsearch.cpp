//
//  localsearch.cpp
//
//  Created by Eliana Fernandes
//  Copyright (c) 2015 Eliana Fernandes. All rights reserved.
//

#include "localsearch.h"

int LocalSearch(data dados, ant& ants, iteration& iter, int iterat){
    
#ifdef LSHIST
    ofstream myfile;
    myfile.open ("LShistory.txt",ios::app);
    myfile<<"LocalSearch"<<endl;
	myfile << "iteration: " << iterat << endl;
    myfile.close();
#endif
    
    int i,j,p,l;
    
    double compare_cost=0.0;
   
#ifdef LSHIST
	// WRITE CURRENT SOLUTION
	// "Variables z equal to 1: "
	myfile.open ("LShistory.txt",ios::app);
	myfile << "CURRENT SOLUTION" <<endl;
	myfile << "Variables z equal to 1: " << endl;
	for (j=0; j<dados.nbNodes; j++)
		if (iter.z_best[j] == 1)
			myfile << "z[" << j << "]" << endl;
	myfile << endl;
    
	//"Variables x equal to 1: "
    
	myfile << "Variables x equal to 1: " << endl;
for (p=0; p<dados.nbProducts; p++)
		for (i=0; i<dados.nbNodes; i++)
			for (j=0; j<dados.nbNodes; j++)
				if(iter.x_best[i][p]==j){
					myfile << "x[" << i << "][" << j << "][" << p << "]" << endl;
				}
	myfile.close();
#endif
    
   int ls_iter=0; //used to count local search iterations
    
   int max_ls_iter=500; //maximum local search iterations allowed
    
    /// to see if relocating a node is cheaper
    double temp_cost=0.0;
    // cost to add
    double cost_plus;
    // cost to subtract
    double cost_subtract;
    
    if(ants.life>0){
        // first Admissible
        // 1.Relocate Node -- a node randomly chosen is reassigned to a diferent hub
        ///////// /////////         DECLARATIONS AND INITIALIZATIONS           ///////// ///////// /////////
        // Stopping Criteria
        int first_admissible=0;
        int stop,flag=0;
        /// counter
        int count;
		int relocation;
        // values to be randomly generated for relocation
        int prod, hub, new_hub, node;
        ///////// /////////          END DECLARATIONS AND INITIALIZATIONS          ///// ///////// /////////
        do{
            count = 0;
			relocation=0;
            do{
                // 1st choose a randomly a product
                prod = unifRand()*(dados.nbProducts-1);
				//cout <<"prod "<< prod << endl;
                // for that product, choose randomly a node to be relocated
                node = unifRand()*(dados.nbNodes-1);
				//cout <<"node "<< node << endl;
                // get the hub to which that node is allocated
                hub = iter.x_best[node][prod];
#ifdef LSHIST
                ofstream myfile;
                myfile.open ("LShistory.txt",ios::app);
                myfile<<"x_best["<<node <<"]["<<prod<<"]= "<<hub<<" is candidate to be reassigned.\n";
                myfile.close();
#endif
                // choose randomly another hub for relocation
                // try for a finite number of times, node might be not relocatable
                do{
                    new_hub = unifRand()*(dados.nbNodes-1);
                    if((new_hub!=hub && ants.avail_cap[new_hub][prod]>dados.O[node][prod])){
						if(iter.x_best[new_hub][prod]==new_hub){
#ifdef LSHIST
							ofstream myfile;
							myfile.open ("LShistory.txt",ios::app);
							myfile<<"x_best["<<new_hub <<"]["<<prod<<"]= "<<new_hub<<" is hub.\n";
							myfile.close();
#endif
                        stop=1;
                        flag=1;
						relocation=1;
						}
                    }
                    count++;
                    if (count>max_ls_iter/10){
                        stop=1;
                        flag=1;
                    }
                    
                } while(stop<0);
                
            }while(flag<1);
            //// compute cost diference
            cost_plus = dados.d[node][new_hub]*(dados.chi[prod]*dados.O[node][prod]+dados.delta[prod]*dados.D[node][prod]);
            cost_subtract = dados.d[node][hub]*(dados.chi[prod]*dados.O[node][prod]+dados.delta[prod]*dados.D[node][prod]);
            /////// end cost diference
            ////// if the cost is lower, relocate the hub
            if(cost_plus-cost_subtract<0 && relocation>0){
                iter.x_best[node][prod]=new_hub;
                temp_cost=iter.best_cost;
                iter.best_cost=temp_cost+cost_plus-cost_subtract;
#ifdef LSHIST
                ofstream myfile;
                myfile.open ("LShistory.txt",ios::app);
                myfile<<"Reassignment done to hub "<<new_hub<<endl;
                myfile.close();
#endif
                ///// update capacities
                ants.avail_cap[hub][prod]=ants.avail_cap[hub][prod]+dados.O[node][prod];
                ants.avail_cap[new_hub][prod]=ants.avail_cap[new_hub][prod]-dados.O[node][prod];
                first_admissible=2;
                ls_iter++;
            }//if(cost_plus-cost_subtract<0 && relocation>0)
            else{
				ls_iter++;
#ifdef LSHIST
				ofstream myfile;
				myfile.open ("LShistory.txt",ios::app);
				if(relocation==0)
					myfile<< "no relocation done"<<endl;
				myfile <<"ls_iter: "<< ls_iter<<endl;
				myfile.close();
#endif
            }

            if(ls_iter>max_ls_iter) first_admissible=2;
        } while(first_admissible<1);
#ifdef LSHIST
		// WRITE CURRENT SOLUTION
		// "Variables z equal to 1: "
		myfile.open ("LShistory.txt",ios::app);
		myfile << "Solution AFTER LocalSearch SOLUTION" <<endl;
		myfile << "Variables z equal to 1: " << endl;
		for (j=0; j<dados.nbNodes; j++)
			if (iter.z_best[j] == 1)
				myfile << "z[" << j << "]" << endl;
		myfile << endl;

		//"Variables x equal to 1: "

		myfile << "Variables x equal to 1: " << endl;
		for (p=0; p<dados.nbProducts; p++)
			for (i=0; i<dados.nbNodes; i++)
				for (j=0; j<dados.nbNodes; j++)
					if(iter.x_best[i][p]==j){
						myfile << "x[" << i << "][" << j << "][" << p << "]" << endl;
					}
		myfile.close();
#endif
    }//if(ants.life>0)
    return 0;
}