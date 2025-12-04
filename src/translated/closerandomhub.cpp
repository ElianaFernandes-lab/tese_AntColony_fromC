//
//  closerandomhub.cpp
//
//  Created by Eliana Fernandes
//  Copyright (c) 2015 Eliana Fernandes. All rights reserved.
//

#include "closerandomhub.h"

int CloseRandomHub(data dados, ant& ants, iteration& iter, double max_cost){
    
#ifdef CRHHIST
	ofstream myfile;
	myfile.open ("CRHhistory.txt",ios::app);
	myfile<<"CloseRandomHub"<<endl;
	myfile.close();
#endif
    
	int i,j,p,l;
	double compare_cost=0.0;
	double temp_cost=iter.best_cost;
	int max_dif=1e4;
#ifdef CRHHIST
	// WRITE CURRENT SOLUTION
	// "Variables z equal to 1: "
	myfile.open ("CRHhistory.txt",ios::app);
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
    
	if(ants.life>0){
        //list of nodes
        int nr_nodes;
        // create a temporary solution
        int** x_temp;
        x_temp = new (nothrow) int*[dados.nbNodes];
        if (x_temp == 0)
            cout<<"Error: memory could not be allocated for x_temp\n";
		for (i=0; i<dados.nbNodes; ++i){
			x_temp[i] = new (nothrow) int[dados.nbProducts];
			if (x_temp[i] == 0)
				cout<<"Error: memory could not be allocated for x_temp\n";
		}
		// copy the solution
		for(p=0;p<dados.nbProducts;p++)
			for(int i=0;i<dados.nbNodes;i++){
				x_temp[i][p] = iter.x_best[i][p];
			}
		// create temporary capacities
		double **temp_cap;
		temp_cap = new (nothrow) double * [dados.nbNodes];
		if (temp_cap == 0)
			cout<<"Error: memory could not be allocated for temp_cap\n";
        for (i=0; i<dados.nbNodes; ++i){
            temp_cap[i] = new (nothrow) double [dados.nbProducts];
            if (temp_cap[i] == 0)
                cout<<"Error: memory could not be allocated for temp_cap\n";
        }
		//copy the capacities
		for(p=0;p<dados.nbProducts;p++)
			for(j=0;j<dados.nbNodes;j++)
				temp_cap[j][p]=ants.avail_cap[j][p];
        
		// first Admissible
		// 1.Close a hub (the one with less connections to it)
		///////// ///////// /////         DECLARATIONS AND INITIALIZATIONS          ///////// ///////// /////////
		int first_admissible=0; // Stopping Criterion
		// cost to add
		double cost_plus;
		// cost to subtract
		double cost_subtract;
		// cost diference
		double cost_diference=0.0;
		// min cost diference
		double min_cost_diference=max_cost;
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
		int prod; // product
		int hub,hub_index; // hub
		///////// ///////// //////        END DECLARATIONS AND INITIALIZATIONS      ///////// ///////// /////////
		int flag=0;
		int stop=0;
        
        ///// choose a random product
        ///// try to close a random hub for that product
        //do1 try only once
		do{
            //initialize hub cost
			for(p=0;p<dados.nbProducts;p++)
				for(j=0;j<dados.nbNodes;j++)
					hub_cost[p][j]=0;
			/// compute hub fixed costs
			for(p=0;p<dados.nbProducts;p++)
				for(j=0;j<dados.nbNodes;j++){
					if(nr.pprods[j]==1){ // if the hub handles only one product, the hub is closed too
						hub_cost[p][j]=dados.f[j][p]+dados.g[j];
					}else{
                        /// if there are more pconnects, will only be closed as dedicated hub for product p
                        hub_cost[p][j]=dados.f[j][p];
					}
				}
			// choose random product
			prod = unifRand()*(dados.nbProducts-1);
#ifdef CRHHIST
			ofstream myfile;
			myfile.open ("CRHhistory.txt",ios::app);
			myfile<<"product: "<<prod<<endl;
			myfile.close();
#endif
			// get the hub list of that product
			// make a temporary copy of the hub list, so that the original list is not lost and
			// retrieve the hub to be closed
            
			// aux, to be used later
			int temp_list;
            
			//count the number of hubs for that product
			int nr_hubs=0;
			for(l=0;l<dados.nbNodes;l++)
				if(nr.list_phubs[prod][l]>=0)
					nr_hubs++;
            
#ifdef CRHHIST
			myfile.open ("CRHhistory.txt",ios::app);
			myfile<<"nr_hubs: "<<nr_hubs<<endl;
			myfile.close();
#endif
			int* temp_list_hubs;
			//memory allocation
			temp_list_hubs = new (nothrow) int [nr_hubs-1];
			if (temp_list_hubs == 0)
				cout<<"Error: memory could not be allocated\n";
            
			//copy values to the temporary list and count the nr of hubs in that list
			for(l=0;l<nr_hubs;l++){
				temp_list_hubs[l]=nr.list_phubs[prod][l];
#ifdef CRHHIST
                ofstream myfile;
                myfile.open ("CRHhistory.txt",ios::app);
                myfile<<"temp_list_hubs["<<l<<"] = "<<temp_list_hubs[l]<<endl;
                myfile.close();
#endif
			}
#ifdef CRHHIST
			myfile.open ("CRHhistory.txt",ios::app);
			myfile<<"nr_hubs = "<<nr_hubs<<endl;
			myfile.close();
#endif
			// choose randomly a hub from that list to be closed
			hub_index = unifRand()*(nr_hubs-1);
			hub = temp_list_hubs[hub_index];
#ifdef CRHHIST
			myfile.open ("CRHhistory.txt",ios::app);
			myfile<<"Close canditate: hub "<<hub<<" for product "<<prod<<" (cost = "<<hub_cost[prod][hub]<<")"<<endl;
			myfile.close();
#endif
			// get list of nodes connected to that hub
#ifdef CRHHIST
			myfile.open ("CRHhistory.txt",ios::app);
			myfile << "Nodes connected to hub " << hub <<":"<< endl;
			myfile.close();
#endif
			//list of nodes
			nr_nodes=nr.pconnects[prod][hub];
			int* temp_list_nodes;
            
			//memory allocation
			temp_list_nodes = new (nothrow) int [nr_nodes];
			if (temp_list_nodes == 0)
				cout<<"Error: memory could not be allocated\n";
			j=0;
			for(l=0;l<dados.nbNodes;l++){
				if(iter.x_best[l][prod]==hub){
					temp_list_nodes[j]=l;
					j++;
				}
				if(j>nr_nodes) break;
			}
            
#ifdef CRHHIST
			myfile.open ("CRHhistory.txt",ios::app);
			for (l=0; l<nr_nodes; l++)
				myfile << "x[" << temp_list_nodes[l] << "][" << hub << "][" << prod << "]" << endl;
			for (l=0; l<nr_nodes; l++)
				myfile<<"temp_list_nodes["<<l<<"]="<<temp_list_nodes[l]<<endl;
			myfile << "nr_nodes = " <<nr_nodes<<endl;
			myfile.close();
#endif
			// Relocate nodes connected to hub to be closed for that product
			// get hub with most available capacity and relocate nodes to that hub
			// remove the hub to be closed from the list
			for (l=0; l<nr_hubs; l++)
				if(temp_list_hubs[l]==hub)
					temp_list_hubs[l]=-1;
			// put the available hubs in the beginning of the list
			l=0;
			do{
				for(i=0;i<nr_hubs;i++)
					if(temp_list_hubs[i]>=0){
						temp_list=temp_list_hubs[i];
						temp_list_hubs[l]=temp_list;
#ifdef CRHHIST
						ofstream myfile;
						myfile.open ("CRHhistory.txt",ios::app);
						myfile<<"temp_list_hubs["<<l<<"]="<<temp_list_hubs[l]<<endl;
						myfile.close();
#endif
						l++;
					}//if(temp_list_hubs[i]>=0)
			} while(l<nr_hubs-1);
			// put the rest of the list to -1
			temp_list_hubs[nr_hubs-1]=-1;
            
            // first, relocate the hub-node to the hub with most available capacity
            // get hub with bigger available capacity
            double capacity=0.0;
            int hub_bigger;
            for (l=0; l<nr_hubs-1; l++){
#ifdef CRHHIST
                ofstream myfile;
                myfile.open ("CRHhistory.txt",ios::app);
                myfile<<"temp_cap["<<temp_list_hubs[l]<<"]["<<prod<<"] = "<<temp_cap[temp_list_hubs[l]][prod]<<endl;
                myfile.close();
#endif
                if(temp_cap[temp_list_hubs[l]][prod]>capacity){
                    capacity=temp_cap[temp_list_hubs[l]][prod];
                    hub_bigger=temp_list_hubs[l];
                }
            }
#ifdef CRHHIST
            myfile.open ("CRHhistory.txt",ios::app);
            myfile<<"The hub with bigger capacity is hub "<<hub_bigger<<" with an available capacity of "<<capacity<<endl;
            myfile.close();
#endif
            double flow=0.0;
            int node_bigger=hub;
#ifdef CRHHIST
            for (l=0; l<nr_nodes; l++){
                ofstream myfile;
                myfile.open ("CRHhistory.txt",ios::app);
                myfile<<"dados.O["<<temp_list_nodes[l]<<"]["<<prod<<"] = "<<dados.O[temp_list_nodes[l]][prod]<<endl;
                myfile.close();
            }
            flow=dados.O[node_bigger][prod];
            myfile.open ("CRHhistory.txt",ios::app);
            myfile<<"The node-hub is node "<<node_bigger<<" with a flow of "<<flow<<endl;
            myfile.close();
#endif
            // if possible and cheaper, relocate node to that hub, using the temporary solution
            if(flow<capacity){
                /////// cost diference
                cost_plus = dados.d[node_bigger][hub_bigger]*(dados.chi[prod]*dados.O[hub_bigger][prod]+dados.delta[prod]*dados.D[hub_bigger][prod]);
                /// estima-se que o hub vai fechar por isso tira-se  o custo fixo, depois eh corrigido
                cost_subtract = dados.d[node_bigger][hub]*(dados.chi[prod]*dados.O[hub][prod]+dados.delta[prod]*dados.D[hub][prod])+hub_cost[prod][hub];
                cost_diference=cost_plus-cost_subtract;
#ifdef CRHHIST
                ofstream myfile;
                myfile.open ("CRHhistory.txt",ios::app);
                myfile<<" add cost: "<<cost_plus<<endl;
                myfile<<" estimated remove cost: "<<cost_subtract<<endl;
                myfile<<" difference: "<<cost_diference<<endl;
                myfile.close();
#endif
                /// if it's cheaper to relocate the node, do it and correct the costs
                if(cost_diference<max_dif){
                    cost_plus = dados.d[node_bigger][hub_bigger]*(dados.chi[prod]*dados.O[hub_bigger][prod]+dados.delta[prod]*dados.D[hub_bigger][prod]);
                    cost_subtract = dados.d[node_bigger][hub]*(dados.chi[prod]*dados.O[hub][prod]+dados.delta[prod]*dados.D[hub][prod]);
                    cost_diference=cost_plus-cost_subtract;
#ifdef CRHHIST
                    ofstream myfile;
                    myfile.open ("CRHhistory.txt",ios::app);
                    myfile<<" add cost: "<<cost_plus<<endl;
                    myfile<<" remove cost: "<<cost_subtract<<endl;
                    myfile<<" difference: "<<cost_diference<<endl;
                    myfile.close();
#endif
                    //update solution
                    x_temp[node_bigger][prod]=hub_bigger;
                    // update capacities
                    temp_cap[hub_bigger][prod]=capacity-flow;
                    temp_cap[hub][prod]+=flow;  // its necessary for the other local search routines
#ifdef CRHHIST
                    myfile.open ("CRHhistory.txt",ios::app);
                    myfile<<"REALOCATION DONE"<<endl;
                    myfile<<"BEFORE: "<<"x_temp["<<node_bigger<<"]["<<prod<<"]= "<<hub<<endl;
                    myfile<<"ATER:   "<<"x_temp["<<node_bigger<<"]["<<prod<<"]= "<<hub_bigger<<endl;
                    myfile.close();
#endif
                    
                    // remove node from node list
                    for (l=0; l<nr_nodes; l++)
                        if(temp_list_nodes[l]==node_bigger)
                            temp_list_nodes[l]=-1;
                    // put the available nodes in the beginning of the list
                    l=0;
                    do{
                        for(i=0;i<nr_nodes;i++)
                            if(temp_list_nodes[i]>=0){
                                temp_list=temp_list_nodes[i];
                                temp_list_nodes[l]=temp_list;
#ifdef CRHHIST
                                ofstream myfile;
                                myfile.open ("CRHhistory.txt",ios::app);
                                myfile<<"temp_list_nodes["<<l<<"]="<<temp_list_nodes[l]<<endl;
                                myfile.close();
#endif
                                l++;
                            }
                    } while(l<nr_nodes-1);
                    // put the rest of the list to -1
                    temp_list_nodes[nr_nodes-1]=-1;
                    // reduce  the number of nodes to be relocated
                    nr_nodes=nr_nodes-1;
#ifdef CRHHIST
                    myfile.open ("CRHhistory.txt",ios::app);
                    myfile<<"number of nodes to be relocated "<< nr_nodes << endl;
                    myfile.close();
#endif
                }//if(cost_diference<0)
                else{
                    cost_diference=0;
                    stop=1;
                    flag=1;
                }// else if(cost_diference<0) if its not worth it including the fixed costs it might not be worth it for the others...to save computing time
            }//if(flow<capacity)
            
            // do 2 relocate the nodes
			do{
				// get hub with bigger available capacity
				double capacity=0.0;
				int hub_bigger;
				for (l=0; l<nr_hubs-1; l++){
#ifdef CRHHIST
					ofstream myfile;
					myfile.open ("CRHhistory.txt",ios::app);
					myfile<<"temp_cap["<<temp_list_hubs[l]<<"]["<<prod<<"] = "<<temp_cap[temp_list_hubs[l]][prod]<<endl;
					myfile.close();
#endif
					if(temp_cap[temp_list_hubs[l]][prod]>capacity){
						capacity=temp_cap[temp_list_hubs[l]][prod];
						hub_bigger=temp_list_hubs[l];
					}
				}
#ifdef CRHHIST
				myfile.open ("CRHhistory.txt",ios::app);
				myfile<<"The hub with bigger capacity is hub "<<hub_bigger<<" with an available capacity of "<<capacity<<endl;
				myfile.close();
#endif
				// get node with bigger flow
				if(nr_nodes>0){
                    flow=0.0;
                    for (l=0; l<nr_nodes; l++){
#ifdef CRHHIST
                        ofstream myfile;
                        myfile.open ("CRHhistory.txt",ios::app);
                        myfile<<"dados.O["<<temp_list_nodes[l]<<"]["<<prod<<"] = "<<dados.O[temp_list_nodes[l]][prod]<<endl;
                        myfile.close();
#endif
                        if(dados.O[temp_list_nodes[l]][prod]>flow){
                            flow=dados.O[temp_list_nodes[l]][prod];
                            node_bigger=temp_list_nodes[l];
                        }
                    }
#ifdef CRHHIST
                    myfile.open ("CRHhistory.txt",ios::app);
                    myfile<<"The node with bigger flow is node "<<node_bigger<<" with a flow of "<<flow<<endl;
                    myfile.close();
#endif
                    // if possible and cheaper, relocate node to that hub, using the temporary solution
                    if(flow<capacity){
                        /////// cost diference
                        cost_plus = dados.d[node_bigger][hub_bigger]*(dados.chi[prod]*dados.O[hub_bigger][prod]+dados.delta[prod]*dados.D[hub_bigger][prod]);
                        /// estima-se que o hub vai fechar por isso tira-se  o custo fixo, depois eh corrigido
                        cost_subtract = dados.d[node_bigger][hub]*(dados.chi[prod]*dados.O[hub][prod]+dados.delta[prod]*dados.D[hub][prod])+hub_cost[prod][hub];
                        cost_diference=cost_plus-cost_subtract;
#ifdef CRHHIST
                        ofstream myfile;
                        myfile.open ("CRHhistory.txt",ios::app);
                        myfile<<" add cost: "<<cost_plus<<endl;
                        myfile<<" estimated remove cost: "<<cost_subtract<<endl;
                        myfile<<" difference: "<<cost_diference<<endl;
                        myfile.close();
#endif
                        /// if it's cheaper to relocate the node, do it and correct the costs
                        if(cost_diference<max_dif){
                            cost_plus = dados.d[node_bigger][hub_bigger]*(dados.chi[prod]*dados.O[hub_bigger][prod]+dados.delta[prod]*dados.D[hub_bigger][prod]);
                            cost_subtract = dados.d[node_bigger][hub]*(dados.chi[prod]*dados.O[hub][prod]+dados.delta[prod]*dados.D[hub][prod]);
                            cost_diference=cost_plus-cost_subtract;
#ifdef CRHHIST
                            ofstream myfile;
                            myfile.open ("CRHhistory.txt",ios::app);
                            myfile<<" add cost: "<<cost_plus<<endl;
                            myfile<<" remove cost: "<<cost_subtract<<endl;
                            myfile<<" difference: "<<cost_diference<<endl;
                            myfile.close();
#endif
                            //update solution
                            x_temp[node_bigger][prod]=hub_bigger;
                            // update capacities
                            temp_cap[hub_bigger][prod]=capacity-flow;
                            temp_cap[hub][prod]+=flow;  // Ž necess‡rio para as outras rotinas de local search que se fazem depois
#ifdef CRHHIST
                            myfile.open ("CRHhistory.txt",ios::app);
                            myfile<<"REALOCATION DONE"<<endl;
                            myfile<<"BEFORE: "<<"x_temp["<<node_bigger<<"]["<<prod<<"]= "<<hub<<endl;
                            myfile<<"ATER:   "<<"x_temp["<<node_bigger<<"]["<<prod<<"]= "<<hub_bigger<<endl;
                            myfile.close();
#endif
                            // remove node from node list
                            for (l=0; l<nr_nodes; l++)
                                if(temp_list_nodes[l]==node_bigger)
                                    temp_list_nodes[l]=-1;
                            // put the available nodes in the beginning of the list
                            l=0;
                            do{
                                for(i=0;i<nr_nodes;i++)
                                    if(temp_list_nodes[i]>=0){
                                        temp_list=temp_list_nodes[i];
                                        temp_list_nodes[l]=temp_list;
#ifdef CRHHIST
                                        ofstream myfile;
                                        myfile.open ("CRHhistory.txt",ios::app);
                                        myfile<<"temp_list_nodes["<<l<<"]="<<temp_list_nodes[l]<<endl;
                                        myfile.close();
#endif
                                        l++;
                                    }
                            } while(l<nr_nodes-1);
                            // put the rest of the list to -1
                            temp_list_nodes[nr_nodes-1]=-1;
                            // reduce  the number of nodes to be relocated
                            nr_nodes=nr_nodes-1;
#ifdef CRHHIST
                            myfile.open ("CRHhistory.txt",ios::app);
                            myfile<<"number of nodes to be relocated "<< nr_nodes << endl;
                            myfile.close();
#endif
                        }//if(cost_diference<0)
                        else{
                            cost_diference=0;
                            stop=1;
                            flag=1;
                        }// else if(cost_diference<0) if its not worth it including the fixed costs it might not be worth it for the others...to save computing time
                    }//if(flow<capacity)
                    //if(flow<capacity) it means that the the node with bigger flow cannot be allocated to the hub with the bigger capacity, it is not possible to close the hub
                    else{
                        cost_diference=0;
                        stop=1;
                        flag=1;
                    } //else if(flow<capacity)
				}//if(nr_nodes>0)
				// update costs
				temp_cost=temp_cost+cost_diference;
                if (nr_nodes<=0) stop=1;
                if (nr_hubs<=0) stop=1;
				if (iter.best_cost>=temp_cost-hub_cost[prod][hub]) flag=1;
            } while (stop<1);// do 2 relocate the nodes
            flag=1;
            //delete [] temp_list_hubs;
			delete [] temp_list_nodes;
        }while (flag<1); //do1
#ifdef CRHHIST
        ofstream myfile;
        myfile.open ("CRHhistory.txt",ios::app);
		myfile<<"cost before " << iter.best_cost<<endl;
        myfile.close();
#endif
        if(nr_nodes==0){
#ifdef CRHHIST
			ofstream myfile;
			myfile.open ("CRHhistory.txt",ios::app);
			myfile<<"HUB UNDEDICATED"<<endl;
			myfile<<"cost before " << iter.best_cost<<endl;
			myfile.close();
#endif
			//copy the capacities
            for(p=0;p<dados.nbProducts;p++)
                for(j=0;j<dados.nbNodes;j++){
//#ifdef CRHHIST
//					ofstream myfile;
//					myfile.open ("CRHhistory.txt",ios::app);
//					myfile<<" BEFORE ants.avail_cap["<<j<<"]["<<p<<"]= "<<ants.avail_cap[j][p]<<endl;
//					myfile.close();
//#endif
					ants.avail_cap[j][p]=temp_cap[j][p];
//#ifdef CRHHIST
//					myfile.open ("CRHhistory.txt",ios::app);
//					myfile<<" AFTER ants.avail_cap["<<j<<"]["<<p<<"]= "<<ants.avail_cap[j][p]<<endl;
//					myfile.close();
//#endif
				}
            // copy the solution
            for(p=0;p<dados.nbProducts;p++)
                for(int i=0;i<dados.nbNodes;i++){
//#ifdef CRHHIST
//					ofstream myfile;
//					myfile.open ("CRHhistory.txt",ios::app);
//					myfile<<" BEFORE iter.x_best["<<i<<"]["<<p<<"]= "<<iter.x_best[i][p]<<endl;
//					myfile.close();
//#endif
                    iter.x_best[i][p]=x_temp[i][p];
//#ifdef CRHHIST
//					myfile.open ("CRHhistory.txt",ios::app);
//					myfile<<" AFTER iter.x_best["<<i<<"]["<<p<<"]= "<<iter.x_best[i][p]<<endl;
//					myfile.close();
//#endif
                }
            // update the cost
            iter.best_cost=temp_cost-hub_cost[prod][hub];
            if(nr.pprods[j]==1){/// close the hub too
//#ifdef CRHHIST
//					ofstream myfile;
//					myfile.open ("CRHhistory.txt",ios::app);
//					myfile<<"HUB CLOSED"<<endl;
//					myfile<<" BEFORE iter.z_best["<<hub<<"]= "<<iter.z_best[hub]<<endl;
//					myfile.close();
//#endif
				iter.z_best[hub]=0;
//#ifdef CRHHIST
//					myfile.open ("CRHhistory.txt",ios::app);
//					myfile<<" AFTER iter.z_best["<<hub<<"]= "<<iter.z_best[hub]<<endl;
//					myfile.close();
//#endif
			}
        }//if(nr_nodes==0)
#ifdef CRHHIST
        myfile.open ("CRHhistory.txt",ios::app);
		myfile<<"cost after " << iter.best_cost<<endl;
        myfile.close();
#endif
        
#ifdef CRHHIST
		// WRITE SOLUTION AFTER
		// "Variables z equal to 1: "
		myfile.open ("CRHhistory.txt",ios::app);
		myfile << "SOLUTION AFTER" <<endl;
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
        // delete
        for (i=0; i<dados.nbProducts; ++i)
            delete [] hub_cost[i];
        delete []  hub_cost;
        
        for (i=0; i<dados.nbNodes; ++i){
            delete [] x_temp[i];
        }
        delete[] x_temp;
        
        for (i=0; i<dados.nbNodes; ++i)
            delete [] temp_cap[i];
        
        delete [] temp_cap;

		delete [] nr.phubs;
		delete [] nr.pnodes;
		delete [] nr.pprods;
		delete [] nr.connects;
		for (i=0; i<dados.nbProducts; ++i)
			delete [] nr.pconnects[i];
		delete [] nr.pconnects;
		for (i=0; i<dados.nbProducts; ++i)
			delete [] nr.list_phubs[i];
		delete [] nr.list_phubs;
		for (i=0; i<dados.nbProducts; ++i)
			delete [] nr.list_pnodes[i];
		delete [] nr.list_pnodes;
    }//if(ants.life>0)
    
    
    return 0;
}//CloseRandomHub(data dados, ant& ants, iteration& iter, double max_cost)