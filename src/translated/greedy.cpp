//
//  greedy.cpp
//
//  Created by Eliana Fernandes
//  Copyright (c) 2015 Eliana Fernandes. All rights reserved.
//

#include <iostream>

#include "greedy.h"
#include "readdata.h"
#include "acovar.h"

using namespace std;

ind Greedy (int& ant_life,data dados, aco a_param, ant& ants){
    
    int p,i,j;
    int flag;
    ind indices;
    double largest = 0.0;
    flag=0;
    
    for(p=0;p<dados.nbProducts;p++)
        for(i=0;i<dados.nbNodes;i++)
            for(j=0;j<dados.nbNodes;j++)
                if(a_param.tau[i][j][p]>=largest && ants.avail_tau[i][j][p]>0 && ants.avail_cap[j][p]>dados.O[i][p]){
                    largest = a_param.tau[i][j][p];//!!! HVIS!!!
                    indices.prod=p;
                    indices.node=i;
                    indices.hub=j;
					//double temp_cap;
					//temp_cap=ants.avail_cap[j][p]; //NEW
					//ants.avail_cap[j][p]=temp_cap-dados.O[i][p];
                    flag=1;
                }
    if(flag==0){
        ants.life=0;
        /// when solution components available give origin to a unfeasible solution, begin next solution with
        /// one of the solution components available
        for(p=0;p<dados.nbProducts;p++)
            for(i=0;i<dados.nbNodes;i++)
                for(j=0;j<dados.nbNodes;j++)
                    if(ants.avail_tau[i][j][p]>0){
                        ants.prod=p;
                        ants.node=j;
                        ants.hub=j;
                    }
    }

    return (indices);
}