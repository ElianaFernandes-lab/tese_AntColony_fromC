//
//  counting.cpp
//
//  Created by Eliana Fernandes
//  Copyright (c) 2015 Eliana Fernandes. All rights reserved.
//

#include "counting.h"

// number of dedicated hubs to each product
counters counting(int nr_products, int nr_nodes,iteration iter){
    
    counters nr;
    
    int p,i,j,l;
    
    // total number of dedicated hubs to each product
    nr.phubs = new (nothrow) int [nr_products];
    if (nr.phubs == 0)
        cout<<"Error: memory could not be allocated for nr.phubs\n\n";
    
    // initialized to zero
    for(p=0;p<nr_products;p++)
        nr.phubs[p]=0;
    
    // count the number of dedicated hubs for each product
    for(p=0;p<nr_products;p++){
        for(j=0;j<nr_nodes;j++){
            if(iter.x_best[j][p]==j)
                nr.phubs[p]++;
        }
	}
    
    //total number of nodes assigned for each product
    nr.pnodes = new (nothrow) int [nr_products];
    if (nr.pnodes == 0)
        cout<<"Error: memory could not be allocated for nr.pnodes\n\n";
    
    // initialized to zero
    for(p=0;p<nr_products;p++)
        nr.pnodes[p]=0;
    
    // count the number of allocated nodes for each product
    for(p=0;p<nr_products;p++){
        nr.pnodes[p]=nr_nodes-nr.phubs[p];
    }
    
    // create a list for each of the dedicated hubs (to be will be used in relocate)
    // first, get which product has most hubs dedicated
    nr.max_phubs=0;
    
    for(p=0;p<nr_products;p++){
        if(nr.phubs[p]>=nr.max_phubs)
            nr.max_phubs=nr.phubs[p];
    }
    
    // get max_pnodes
    nr.max_pnodes=0;
    
    for(p=0;p<nr_products;p++)
        for(j=0;j<nr_nodes;j++){
            if(nr.pnodes[p]>=nr.max_pnodes)
                nr.max_pnodes=nr.pnodes[p];
        }
    
    // get the number of products handled by each hub
    nr.pprods = new (nothrow) int [nr_nodes];
    if(nr.pprods ==0)
        cout<<"Error: memory could not be allocated for nr.pprods\n";
    // initialized to zero
    for(j=0;j<nr_nodes;j++)
        nr.pprods[j]=0;
    // counting the number of products handled by each hub
    for(j=0;j<nr_nodes;j++){
        for(p=0;p<nr_products;p++){
            if(iter.x_best[j][p]==j)
                nr.pprods[j]++;
        }
    }

	// number connections to each hub
    //  (if zero, node is not hub, if > 0 node is hub)
    // it counts with the connection to itself
    
    nr.connects = new (nothrow) int[nr_nodes];
    if (nr.connects == 0)
        cout<<"Error: memory could not be allocated for nr.connects\n\n";
   
    // initialized to zero
    for(j=0;j<nr_nodes;j++)
		nr.connects[j]=0;
    
    // counting the number connections to each hub
    // is used to get, the hub nodes and the non hub nodes
    for(j=0;j<nr_nodes;j++)
		for(p=0;p<nr_products;p++)
            if(iter.x_best[j][p]==j)
				nr.connects[j]++; 
				
	// number connections of each type of product to each (hub) node
    //  (if zero, node is not hub, if > 0 node is hub)
    // it counts with the connection to itself
    
    nr.pconnects = new (nothrow) int*[nr_products];
    if (nr.pconnects == 0)
        cout<<"Error: memory could not be allocated for nr.pconnects\n\n";
    for (i=0; i<nr_products; ++i){
        nr.pconnects[i] = new (nothrow) int[nr_nodes];
        if (nr.pconnects[i] == 0)
            cout<<"Error: memory could not be allocated for nr.pconnects[i]\n";
    }
    // initialized to zero
    for(j=0;j<nr_nodes;j++)
        for(p=0;p<nr_products;p++)
            nr.pconnects[p][j]=0;
    
    // counting the number connections of each product to each hub
    // is used to get, for each product, the hub nodes and the non hub nodes
    for(p=0;p<nr_products;p++)
        for(j=0;j<nr_nodes;j++)
            if(iter.x_best[j][p]==j)
                for(i=0;i<nr_nodes;i++)
                    if(iter.x_best[i][p]==j) nr.pconnects[p][j]++;    
    
    // get the dedicated hub list
    // it's initialized to -1 because hub 0 exists and at least only one list will be full
    nr.list_phubs = new (nothrow) int *[nr_products];
    if(nr.list_phubs ==0)
        cout<<"Error: memory could not be allocated for nr.list_phubs\n";
    for (i=0;i<nr_products;++i){
        nr.list_phubs[i] = new (nothrow) int[nr_nodes];
        if(nr.list_phubs[i] == 0)
            cout<<"Error: memory could not be allocated for nr.list_phubs[i]\n";
    }
    
    for(p=0;p<nr_products;p++)
        for(l=0;l<nr_nodes;l++)
            nr.list_phubs[p][l]=-1;
    
    // list the hubs
    for(p=0;p<nr_products;p++){
        l=0;
        for(j=0;j<nr_nodes;j++)
            if(nr.pconnects[p][j]>0){
                nr.list_phubs[p][l]=j;
                l++;
            }
    }
    
    // create a list of the nodes for each product
    // initialize the node list
    // is initialized to -1 because hub 0 exists and at least only one list will be full
    nr.list_pnodes = new (nothrow) int * [nr_products];
    if(nr.list_pnodes ==0)
        cout<<"Error: memory could not be allocated for nr.list_pnodes\n";
    for (i=0;i<nr_products;++i){
        nr.list_pnodes[i] = new (nothrow) int [nr_nodes];
        if(nr.list_pnodes[i] ==0)
            cout<<"Error: memory could not be allocated for nr.list_pnodes[i]\n";
    }
    
    for(p=0;p<nr_products;p++)
        for(l=0;l<nr_nodes;l++)
            nr.list_pnodes[p][l]=-1;
    
    // list the nodes
    l=0;
    do{
        for(p=0;p<nr_products;p++){
            l=0;
            for(j=0;j<nr_nodes;j++)
                if(nr.pconnects[p][j]==0){
                    nr.list_pnodes[p][l]=j;
                    l++;
                }
        }
    } while (p<nr_products);

    return (nr);
}
