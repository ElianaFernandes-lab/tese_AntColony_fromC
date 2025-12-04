//
//  ls.cpp
//
//  Created by Eliana Fernandes
//  Copyright (c) 2015 Eliana Fernandes. All rights reserved.
//

#include "ls.h"

int LocalSearch1(data dados, ant& ants, iteration& iter, int iterat){
    
#ifdef LSSHIST
    ofstream myfile;
    myfile.open ("LSShistory.txt",ios::app);
    myfile<<"LSS"<<endl;
	myfile << "iteration: " << iterat << endl;
    myfile.close();
#endif
    
    int i,j,p,l;
    
    double compare_cost=0.0;
    
#ifdef LSSHIST
	// WRITE CURRENT SOLUTION
	// "Variables z equal to 1: "
	myfile.open ("LSShistory.txt",ios::app);
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
    
	//int max_ls_iter=1000000;
    
    ///////// /////////         DECLARATIONS           ///////// ///////// /////////
    
    int ls_iter; // local search iteration counter
    int first_admissible; // Stopping Criterion
    int nr_avail_hubs;
	int nr_avail_nodes;
    
    // cost to add
    double cost_plus;
    // cost to subtract
    double cost_subtract;
    
    
    int flag=0;
    int flag_avail_hub=0;
    int flag_avail_node=0;
    int hub,new_hub_index,new_hub,node,node_index;
    int prod,nr_avail_prod,temp_prod,prod_index;
    int temp_list;
    
    ///////// /////////          END DECLARATIONS         ///// ///////// /////////
    
    if(ants.life>0){
		
		counters nr;
		nr = counting(dados.nbProducts,dados.nbNodes,iter);
        //only needed if ant is alive
        int* temp_list_phubs;
        int* avail_prod;  // get the products that have available relocations in a list (to be used later)
        // first Admissible
        // 1.Relocate Node -- a node randomly chosen is reassigned to a diferent hub
        ///////// /////////         INITIALIZATIONS           ///////// ///////// /////////
        
        ls_iter=0; // local search iteration counter
        first_admissible=0; // Stopping Criterion
        
        // memory allocation
        avail_prod = new (nothrow) int [dados.nbProducts];  // get the products that have available relocations in a list (to be used later)
        
		temp_list_phubs = new (nothrow) int [dados.nbNodes];
		if(temp_list_phubs == 0)
			cout<<"Error: memory could not be allocated\n";
		// initialized to -1
		for(l=0;l<dados.nbNodes;l++)
			temp_list_phubs[l]=-1;
        
        for(p=0;p<dados.nbProducts;p++){
            avail_prod[p]=p;
        }
        
        nr = counting(dados.nbProducts,dados.nbNodes,iter);
        
        ///////// /////////          INITIALIZATIONS          ///// ///////// /////////
        // 1 do while(first_admissible<1)
        do{
            do{ // 2 do while(flag<1)
                /// local search relocate node using lists of available hubs and nodes
                // choose a random product with available relocations to be made
                // count the number of products with available connections to generate
                // a random choice of product
                nr_avail_prod=0;
                for(p=0;p<dados.nbProducts;p++)
                    if(avail_prod[p]>=0) nr_avail_prod++;
                //cout << "nr_avail_prod " << nr_avail_prod << endl;
				if(nr_avail_prod>0){
					// put the available products in the beginning of the list
					l=0;
                    
					do{ // 3 do list of the available products
						for(i=0;i<dados.nbProducts;i++){
							if(avail_prod[i]>=0){
								// rearrange the product list !!!
								// put the available products in the beginning of the list
								temp_prod=avail_prod[i];
								avail_prod[l]=temp_prod;
#ifdef LSSHIST
								ofstream myfile;
								myfile.open ("LSShistory.txt",ios::app);
								myfile<<"temp_prod["<<l<<"]="<<avail_prod[l]<<endl;
								myfile.close();
#endif
								l++;
							} //if(avail_prod[i]>=0)
						}
					} while(l<nr_avail_prod); // 3 do
                    
					// put the rest of the list to -1
					for(l=nr_avail_prod;l<dados.nbProducts;l++)
						avail_prod[l]=-1;
                    
					prod_index = unifRand()*(nr_avail_prod-1);
					prod=avail_prod[prod_index];
                    
					// for that product, choose randomly a node from the list to be relocated
					// count the number of nodes in the list, for the random generator
					int nr_nodes=0;
                    
					for(l=0;l<dados.nbNodes;l++)
						if(nr.list_pnodes[prod][l]>=0) nr_nodes++;
                    if(nr_nodes>0){
#ifdef LSSHIST
                        ofstream myfile;
                        myfile.open ("LSShistory.txt",ios::app);
                        myfile << "Product Chosen: " << prod<<endl;
                        myfile<<"There are "<<nr_nodes<<" available nodes."<<endl;
                        for(i=0;i<dados.nbNodes;i++)
                            myfile<<"nr.list_pnodes["<<prod<<"]["<<i<<"]= "<< nr.list_pnodes[prod][i]<<endl;
                        myfile.close();
#endif
						if(nr_nodes<nr.pnodes[prod]){
#ifdef LSSHIST
							ofstream myfile;
							myfile.open ("LSShistory.txt",ios::app);
							myfile << "Rearrange the node list" <<endl;
							myfile.close();
#endif
							// rearrange the node list !!!
							// put the available hubs in the beginning of the list
							l=0;
							do{
								for(i=0;i<dados.nbNodes;i++){
									if(nr.list_pnodes[prod][i]>=0){
										temp_list=nr.list_pnodes[prod][i];
										nr.list_pnodes[prod][l]=temp_list;
#ifdef LSSHIST
										ofstream myfile;
										myfile.open ("LSShistory.txt",ios::app);
										myfile<<"ordered node list["<<l<<"]="<< nr.list_pnodes[prod][l]<<endl;
										myfile.close();
#endif
										l++;
									}
								}
							} while(l<nr_nodes);
							// put the rest of the list to -1
							for(l=nr_nodes;l<dados.nbNodes;l++)
								nr.list_pnodes[prod][l]=-1;
						}// end rearrange
                        
						// choose a node from that list
						node_index = unifRand()*(nr_nodes-1); //REVER
						//node_index = unifRand()*nr_nodes;
						node = nr.list_pnodes[prod][node_index];
						// remove the node from the list of nodes to be relocated
						nr.list_pnodes[prod][node_index]=-1;
						flag_avail_node=1; ///AAA///
                        
						// get the hub to which that node is allocated
						hub = iter.x_best[node][prod];
                        
#ifdef LSSHIST
						myfile.open ("LSShistory.txt",ios::app);
						myfile<<"x_best["<<node <<"]["<<prod<<"]["<<iterat<<"]= "<<hub<<" will be reassigned.\n";
						myfile.close();
#endif
						// to choose randomly another dedicated hub to prod from the list, for the node to be allocated
						// make a temporary copy of the hub list, so the that original list is not lost and
						// retrieve the hub to which the node was allocated from that list
                        
						for(l=0;l<dados.nbNodes;l++)
							if(nr.list_phubs[prod][l]!=hub){
								temp_list_phubs[l]=nr.list_phubs[prod][l];
#ifdef LSSHIST
								ofstream myfile;
								myfile.open ("LSShistory.txt",ios::app);
								myfile<<"temp_list_phubs["<<l<<"] = "<<temp_list_phubs[l]<<endl;
								myfile.close();
#endif
							}
							else{
								temp_list_phubs[l]=-1;
#ifdef LSSHIST
								ofstream myfile;
								myfile.open ("LSShistory.txt",ios::app);
								myfile<<"temp_list_phubs["<<l<<"] = "<<temp_list_phubs[l]<<endl;
								myfile.close();
#endif
							}
                        
                        // refine the list using only hubs with enough available capacity
                        for(l=0;l<dados.nbNodes;l++){
                            j=temp_list_phubs[l];
                            if (temp_list_phubs[l]>=0)
                                if(ants.avail_cap[j][prod]<dados.O[node][prod]){
                                    temp_list_phubs[l]=-1;
#ifdef LSSHIST
                                    ofstream myfile;
                                    myfile.open ("LSShistory.txt",ios::app);
                                    myfile<<"cap("<<j<<","<<prod<<"): "<<ants.avail_cap[j][prod]<<"<"<<dados.O[node][prod]<<" dados.O["<<node<<"]["<<prod<<"]"<<endl;
                                    myfile.close();
#endif
                                }
                        }
#ifdef LSSHIST
                        for(l=0;l<dados.nbNodes;l++){
                            j=temp_list_phubs[l];
                            if (temp_list_phubs[l]>=0){
                                if(ants.avail_cap[j][prod]>=dados.O[node][prod]){
                                    ofstream myfile;
                                    myfile.open ("LSShistory.txt",ios::app);
                                    myfile<<"cap("<<j<<","<<prod<<"): "<<ants.avail_cap[j][prod]<<">="<<dados.O[node][prod]<<" dados.O["<<node<<"]["<<prod<<"]"<<endl;
                                    myfile.close();
                                }
                            }
                        }
#endif
                        nr_avail_hubs=0;
                        // count the number of available hubs in the list
                        for(l=0;l<dados.nbNodes;l++)
                            if(temp_list_phubs[l]>=0) nr_avail_hubs++;
#ifdef LSSHIST
                        myfile.open ("LSShistory.txt",ios::app);
                        myfile<<"There are "<<nr_avail_hubs<<" available hubs."<<endl;
                        myfile.close();
#endif
                        // rearrange the list
                        if(nr_avail_hubs>0){
                            // put the available hubs in the beginning of the list
                            l=0;
                            do{
                                for(i=0;i<dados.nbNodes;i++){
                                    if(temp_list_phubs[i]>=0){
                                        temp_list=temp_list_phubs[i];
                                        temp_list_phubs[l]=temp_list;
#ifdef LSSHIST
                                        ofstream myfile;
                                        myfile.open ("LSShistory.txt",ios::app);
                                        myfile<<"temp_list_phubs["<<l<<"]="<<temp_list_phubs[l]<<endl;
                                        myfile.close();
#endif
                                        l++;
                                    }// if(temp_list_phubs[i]>=0)
                                }// for(i=0;i<dados.nbNodes;i++)
                            } while(l<nr_avail_hubs);
                            
                            // put the rest of the list to -1
                            for(l=nr_avail_hubs;l<dados.nbNodes;l++)
                                temp_list_phubs[l]=-1;
#ifdef LSSHIST
                            ofstream myfile;
                            myfile.open ("LSShistory.txt",ios::app);
                            myfile<<"The available hubs are:\n";
                            for(l=0;l<nr_avail_hubs;l++)
                                myfile<<temp_list_phubs[l] <<endl;
                            myfile.close();
#endif
                            // choose randomly a hub from that list
                            new_hub_index = unifRand()*(nr_avail_hubs-1);
                            new_hub = temp_list_phubs[new_hub_index];
							temp_list_phubs[new_hub_index]=-1;
                            flag_avail_hub=1;
                        }//if(nr_avail_hubs>0)
                        else{
                            // remove the node from the list of nodes to be relocated
                            nr.list_pnodes[prod][node_index]=-1;
#ifdef LSSHIST
                            ofstream myfile;
                            myfile.open ("LSShistory.txt",ios::app);
                            myfile<<"There are no available hubs to relocate this node to.\n";
                            myfile<<"Choose another node.\n";
                            myfile.close();
#endif
                        }
					}//if(nr_nodes>0)
					else{
#ifdef LSSHIST
						ofstream myfile;
						myfile.open ("LSShistory.txt",ios::app);
						myfile<<"No relocatable nodes for product"<<prod<<".\n";
						myfile.close();
#endif
                        flag_avail_node=1;
						avail_prod[prod_index]=-1;
					}
				} //if(nr_avail_prod>0)
                else{
#ifdef LSSHIST
                    ofstream myfile;
                    myfile.open ("LSShistory.txt",ios::app);
                    myfile<<"All neighbourhood has been searched with no sucess\n due to unfeasibility issues.\n";
#endif
                    flag=2;
					first_admissible=2;
                } // else if(nr_avail_prod>0 && nr_avail_prod<dados.nbProducts)
            }while(flag<1); // 2 do search the whole space
            if(flag_avail_node>0 && flag_avail_hub>0){
#ifdef LSSHIST
                ofstream myfile;
                myfile.open ("LSShistory.txt",ios::app);
                myfile<<"to dedicated hub "<<new_hub<<endl;
                myfile<<"x_best["<<new_hub<<"]["<<prod<<"] = "<<iter.x_best[new_hub][prod]<<"?"<< endl;
                myfile<<"cap("<<new_hub<<","<<prod<<"): "<<ants.avail_cap[new_hub][prod]<< " >= "<<dados.O[node][prod]<<" : flux("<<node<<") ?"<< endl;
                myfile.close();
#endif
                /////// cost diference
                cost_plus = dados.d[node][new_hub]*(dados.chi[prod]*dados.O[node][prod]+dados.delta[prod]*dados.D[node][prod]);
#ifdef LSSHIST
                myfile.open ("LSShistory.txt",ios::app);
                myfile<<" add cost: "<<cost_plus<<endl;
                myfile.close();
#endif
                cost_subtract = dados.d[node][hub]*(dados.chi[prod]*dados.O[node][prod]+dados.delta[prod]*dados.D[node][prod]);
#ifdef LSSHIST
                myfile.open ("LSShistory.txt",ios::app);
                myfile<<" remove cost: "<<cost_subtract<<endl;
				myfile <<"cost diference: "<<cost_plus-cost_subtract<<endl;
                myfile.close();
#endif
                /////// end cost diference
                ////// if the cost is lower, relocate the hub
                if(cost_plus-cost_subtract<0 && ants.avail_cap[hub][prod]>dados.O[node][prod]){
                    iter.x_best[node][prod]=new_hub;
                    compare_cost=iter.best_cost;
                    iter.best_cost=compare_cost+cost_plus-cost_subtract;
#ifdef LSSHIST
                    ofstream myfile;
                    myfile.open ("LSShistory.txt",ios::app);
                    myfile<<"Reassignment done to hub "<<new_hub<< endl;
                    myfile.close();
#endif
			        ///// update capacities
                    ants.avail_cap[hub][prod]=ants.avail_cap[hub][prod]+dados.O[node][prod];
                    ants.avail_cap[new_hub][prod]=ants.avail_cap[new_hub][prod]-dados.O[node][prod];
                    first_admissible=2;
                }else{
					nr.list_pnodes[prod][node_index]=-1;
#ifdef LSSHIST
					ofstream myfile;
					myfile.open ("LSShistory.txt",ios::app);
					myfile<< "nr.list_pnodes["<<prod<<"]["<<node_index<<"] =" << nr.list_pnodes[prod][node_index]<< endl;
					myfile.close();
#endif
                }
            } //  if(flag_avail_node>0 && flag_avail_hub>0)
            else{
#ifdef LSSHIST
                ofstream myfile;
                myfile.open ("LSShistory.txt",ios::app);
                myfile<<"Exiting Local Search with no results."<<endl;
                myfile.close();
#endif
                first_admissible=2;
            } // else //  if(flag_avail_node>0 && flag_avail_hub>0)
            ls_iter++;
#ifdef LSSHIST
            ofstream myfile;
            myfile.open ("LSShistory.txt",ios::app);
            myfile <<"ls_iter: "<< ls_iter<<endl;
            myfile.close();
#endif
			//if(ls_iter>max_ls_iter) first_admissible=2;
		} while(first_admissible<1); // 1 do bigger loop
        
		#ifdef LSSHIST
	// WRITE FINAL SOLUTION
	// "Variables z equal to 1: "
	myfile.open ("LSShistory.txt",ios::app);
	myfile << "FINAL SOLUTION" <<endl;
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
        delete[] nr.phubs;
        
        delete[] nr.pnodes;
        
        delete[] nr.pprods;
        
        for (i=0;i<dados.nbProducts;++i)
            delete [] nr.list_phubs[i];
        delete [] nr.list_phubs;
        
		for (i=0;i<dados.nbProducts;++i)
            delete [] nr.list_pnodes[i];
        delete [] nr.list_pnodes;
        
        for (i=0;i<dados.nbProducts;++i)
            delete [] nr.pconnects[i];
        delete [] nr.pconnects;
        
        delete [] avail_prod;
		
        delete [] temp_list_phubs;
    } //if(ants.life>0)
    
    return 0;
}//int LocalSearch1(data dados, ant& ants, iteration& iter, int iterat)