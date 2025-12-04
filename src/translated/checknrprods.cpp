//
//  checknrprods.cpp
//
//  Created by Eliana Fernandes
//  Copyright (c) 2015 Eliana Fernandes. All rights reserved.
//

#include "checknrprods.h"

// number of dedicated hubs to each product
hubpcount checkProdConnects(int nr_products, int nr_nodes,ant ants){
    
    int j,p;
    hubpcount n;
    
    // get the number of products handled by each hub
    n.prods = new (nothrow) int [nr_nodes];
    if(n.prods ==0)
        cout<<"Error: memory could not be allocated for n.prods\n";

    // initialized to zero
    for(j=0;j<nr_nodes;j++)
        n.prods[j]=0;
    // counting the number of products handled by each hub
    for(j=0;j<nr_nodes;j++){
        for(p=0;p<nr_products;p++){
            if(ants.x[j][p]==j)
                n.prods[j]++;
        }
    } 

    return (n);
}
