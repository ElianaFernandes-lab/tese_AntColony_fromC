
//
//  closehub.cpp
//
//  Created by Eliana Fernandes
//  Copyright (c) 2015 Eliana Fernandes. All rights reserved.
//

#include "closehub.h"

int CloseMostEmptyHub(data dados, ant& ants, iteration& iter, double max_cost){

#ifdef CLOSEHIST1
    ofstream myfile;
    myfile.open ("history.txt",ios::app);
    myfile<<"CloseHub"<<endl;
    myfile.close();
#endif
    

    int i,j,p,l;
    double compare_cost=0.0;
    
#ifdef CLOSEHIST1
	// WRITE CURRENT SOLUTION
	// "Variables z equal to 1: "
	myfile.open ("history.txt",ios::app);
	myfile << "CURRENT SOLUTION" <<endl;
	myfile << "Variables z equal to 1: " << endl;
	for (j=0; j<dados.nbNodes; j++)
		if (iter.z_best[j] == 1)
			myfile << "z[" << j << "]" << endl;
	myfile << endl;
    
	//"Variables x equal to 1: "
    
	myfile << "Variables x equal to 1: " << endl;
	for (i=0; i<dados.nbNodes; i++)
		for (j=0; j<dados.nbNodes; j++)
			for (p=0; p<dados.nbProducts; p++)
				if(iter.x_best[i][p]==j){
					myfile << "x[" << i << "][" << j << "][" << p << "]" << endl;
				}
	myfile.close();
#endif

    if(ants.life>0){
        // first Admissible
        // 1.Close a hub (the one with less connections to it)
        ///////// ///////// /////         DECLARATIONS AND INITIALIZATIONS          ///////// ///////// /////////
        int first_admissible=0; // Stopping Criterion
        // cost to add
        double cost_plus;
        // cost to subtract
        double cost_subtract;
        // cost diference
        double cost_diference;
        // min cost diference
        double min_cost_diference=max_cost;
        
        int min_connects=dados.nbNodes;
        double max_hub_cost;
        int new_hub,new_hub_temp;
        
        counters nr;
        
		double** hub_cost;
		// memory allocation
		hub_cost = new (nothrow) double* [dados.nbProducts];
		if (hub_cost == 0)
			cout<<"Error: memory could not be allocated\n";
		for (i=0; i<dados.nbProducts; ++i){
			hub_cost[i] = new (nothrow) double [dados.nbNodes];
			if (hub_cost[i] == 0)
				cout<<"Error: memory could not be allocated\n";
		}

        nr = counting(dados.nbProducts,dados.nbNodes,iter);
       
        ///////// ///////// //////        END DECLARATIONS AND INITIALIZATIONS      ///////// ///////// /////////
        // do{
        int flag=0;
        do{
            ///// get the dedicated hub with less connetions to it
            ///// and with the higher cost
            int prod; // product
            int hub; // hub
            for(p=0;p<dados.nbProducts;p++)
                for(j=0;j<dados.nbNodes;j++)
                    hub_cost[p][j]=0;
            for(p=0;p<dados.nbProducts;p++)
                for(j=0;j<dados.nbNodes;j++)
                    if(nr.pconnects[p][j]>0 && nr.pconnects[p][j]<min_connects)
                        min_connects=nr.pconnects[p][j];
#ifdef CLOSEHIST1
            ofstream myfile;
            myfile.open ("history.txt",ios::app);
            myfile<<"Close hubs with "<< min_connects<<" connections."<< endl;
            myfile<<"Get most expensive hub with "<< min_connects<<" connections."<< endl;
            myfile.close();
#endif
            ///// TEMPORARY
            //// Close hub with only one connection (to itself)
            if(min_connects==1){
#ifdef CLOSEHIST1
                ofstream myfile;
                myfile.open ("history.txt",ios::app);
                myfile<<"It is possible to close hubs.\n";
                myfile<<"Get most expensive hub with "<< min_connects<<" connections."<< endl;
                myfile.close();
#endif
                /// compute hub fixed costs
                for(p=0;p<dados.nbProducts;p++)
                    for(j=0;j<dados.nbNodes;j++){
                        if(nr.connects[j]==1){ // if there is only one connect, the hub is closed too
                            hub_cost[p][j]=dados.f[j][p]+dados.g[j];
                        }else{
                            /// if there are more pconnects, will only be closed as dedicated hub for product p
                            hub_cost[p][j]=dados.f[j][p];
                        }
                    }
                //// most expensive hub
                max_hub_cost=0;
                for(p=0;p<dados.nbProducts;p++)
                    for(j=0;j<dados.nbNodes;j++)
                        if(nr.pconnects[p][j]==min_connects && hub_cost[p][j]>=max_hub_cost){
                            prod=p; // product
                            hub=j; // hub
                        }
#ifdef CLOSEHIST1
                myfile.open ("history.txt",ios::app);
                myfile<<"Close canditate: hub "<<hub<<" for product "<<prod<<" (cost = "<<hub_cost[prod][hub]<<")"<<endl;
                myfile.close();
#endif
                
                /// See if it's possible to relocate hub as node ... get available hub list
                // make a temporary copy of the hub list, so that the original list is not lost and
                // retrieve the hub to be closed
                int* temp_list_hubs;
				//memory allocation
				temp_list_hubs = new (nothrow) int [nr.phubs[prod]];
				if (temp_list_hubs == 0)
					cout<<"Error: memory could not be allocated\n";

                int temp_list;
                for(l=0;l<nr.max_phubs;l++)
                    if(nr.list_phubs[prod][l]!=hub){
                        temp_list_hubs[l]=nr.list_phubs[prod][l];
#ifdef CLOSEHIST1
                        ofstream myfile;
                        myfile.open ("history.txt",ios::app);
                        myfile<<"temp_list_hubs["<<l<<"] = "<<temp_list_hubs[l]<<endl;
                        myfile.close();
#endif
                    }else{
                        temp_list_hubs[l]=-1;
                        #ifdef CLOSEHIST1
                        ofstream myfile;
                        myfile.open ("history.txt",ios::app);
                        myfile<<"temp_list_hubs["<<l<<"] = "<<temp_list_hubs[l]<<endl;
                        myfile.close();
                        #endif
                    }
                // refine the list using only hubs with enough available capacity
                for(l=0;l<nr.max_phubs;l++){
					j=temp_list_hubs[l];
                    if (temp_list_hubs[l]>=0){
						if(ants.avail_cap[j][prod]<dados.O[hub][prod] && nr.pprods[j]>=dados.L[j]){
#ifdef CLOSEHIST1
                            ofstream myfile;
                            myfile.open ("history.txt",ios::app);
                            myfile<<ants.avail_cap[j][prod]<<"<"<<dados.O[hub][prod]<<endl;
                            myfile.close();
#endif
                            temp_list_hubs[l]=-1;
                        }else{
#ifdef CLOSEHIST1
                            ofstream myfile;
                            myfile.open ("history.txt",ios::app);
                            myfile<<ants.avail_cap[j][prod]<<">"<<dados.O[hub][prod]<< " or " <<nr.pprods[j]<<"<"<<dados.L[j] <<endl;
                            myfile.close();
#endif
                        }
                    }
				}
                // count the number of available hubs in the list
                int nr_hubs=0;
                for(l=0;l<nr.max_phubs;l++)
                    if(temp_list_hubs[l]>=0) nr_hubs++;
#ifdef CLOSEHIST1
                myfile.open ("history.txt",ios::app);
                myfile<<"There are "<<nr_hubs<<" available hubs."<<endl;
                myfile.close();
#endif
                if(nr_hubs>0){
#ifdef CLOSEHIST1
                    ofstream myfile;
                    myfile.open ("history.txt",ios::app);
                    myfile<<"It is possible to relocate hub "<<hub<< " for product "<< prod<<endl;
                    myfile.close();
#endif
                    // put the available hubs in the beginning of the list
                    l=0;
                    do{
                        for(i=0;i<nr.max_phubs;i++)
                            if(temp_list_hubs[i]>=0){
                                temp_list=temp_list_hubs[i];
                                temp_list_hubs[l]=temp_list;
#ifdef CLOSEHIST1
                                ofstream myfile;
                                myfile.open ("history.txt",ios::app);
                                myfile<<"temp_list_hubs["<<l<<"]="<<temp_list_hubs[l]<<endl;
                                myfile.close();
#endif
                                l++;
                            }
                    } while(l<nr_hubs);
                    // put the rest of the list to -1
                    for(l=nr_hubs+1;l<nr.max_phubs;l++)
                        temp_list_hubs[l]=-1;
#ifdef CLOSEHIST1
                    myfile.open ("history.txt",ios::app);
                    myfile<<"The available hubs are:\n";
                    for(l=0;l<nr_hubs;l++)
                        myfile<<temp_list_hubs[l] <<endl;
                    myfile.close();
#endif
                    // choose a hub from that list that has gives origin to bigger savings
                    for(l=0;l<nr_hubs;l++){
                        new_hub_temp = temp_list_hubs[l];
                        
						// update cost
						/////// cost diference
						cost_plus = dados.d[hub][new_hub_temp]*(dados.chi[prod]*dados.O[hub][prod]+dados.delta[prod]*dados.D[hub][prod]);
						cost_subtract = hub_cost[prod][hub];
#ifdef CLOSEHIST1
                        ofstream myfile;
                        myfile.open ("history.txt",ios::app);
                        myfile<<" add cost: "<<cost_plus<<endl;
                        myfile<<" remove cost: "<<cost_subtract<<endl;
                        myfile.close();
#endif
                        cost_diference=cost_plus-cost_subtract;
                        /////// end cost diference
                        /// choose hub with bigger cost diference
                        if(cost_diference<min_cost_diference){
                            min_cost_diference=cost_diference;
                            new_hub=new_hub_temp;
                        }
                    }
                    ////// if the cost diference is negative, relocate the hub
                    if(min_cost_diference<0){
						if(nr.connects[hub]==1)
							iter.z_best[hub]=0;
						iter.x_best[hub][prod]=new_hub;
						compare_cost=iter.best_cost;
						iter.best_cost=compare_cost+min_cost_diference;
#ifdef CLOSEHIST1
                        ofstream myfile;
                        myfile.open ("history.txt",ios::app);
                        myfile<<"Reassignment done to hub "<< new_hub<<" ."<<endl;
                        myfile.close();
#endif
                        ///// update capacities
                        ants.avail_cap[hub][prod]=ants.avail_cap[hub][prod]+dados.O[hub][prod];
                        ants.avail_cap[new_hub][prod]=ants.avail_cap[new_hub][prod]-dados.O[hub][prod];
                        first_admissible=2;
                    }
                    flag=3;
                }
                else{
                    #ifdef CLOSEHIST1
                    ofstream myfile;
                    myfile.open ("history.txt",ios::app);
                    myfile<<"There are no available hubs to relocate this hub to.\n";
                    myfile<<"Exit close hub attempt.\n";
                    myfile.close();
#endif
                    flag=2;
                }
            }else{
#ifdef CLOSEHIST1
                ofstream myfile;
                myfile.open ("history.txt",ios::app);
                myfile<<"There are no hubs to be closed.\n";
                myfile.close();
#endif
                flag=2;
            }
        }while (flag<1);
        //}while (first_admissible<1);
#ifdef CLOSEHIST1
        ofstream myfile;
        myfile.open ("history.txt",ios::app);
        myfile<<"------------------"<<endl;
        myfile.close();
#endif
    
    }
    
    return 0;
}