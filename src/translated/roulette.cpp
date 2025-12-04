//
//  roulette.cpp
//
//  Created by Eliana Fernandes
//  Copyright (c) 2015 Eliana Fernandes. All rights reserved.
//
#include <iostream>
#include <math.h>       /* sqrt,pow */
#include <time.h>

#include "roulette.h"
#include "readdata.h"
#include "acovar.h"
#include "unifrand.h"
#include "hvis.h"

using namespace std;

ind RouletteWheel (int& ant_life,data dados, aco a_param, ant& ants, pre_prc pre_p,heur_vis hvis){
    
    int p,i,j;
    
    ind indices;
    
    double sum_tau=0.0;
    
    double cum_prob=0.0;
    
    double beta=BETA;
    
    int flag=0;
    
    //  PROBABILITIES (p(i,j,p,k) ----->  initialized to -1)
    for(p=0;p<dados.nbProducts;p++)
        for(i=0;i<dados.nbNodes;i++)
            for(j=0;j<dados.nbNodes;j++)
                ants.prob[i][j][p]=-1;
    
    // !!! ROULETTE WHEEL (B3.pdf, pg 122)
    // CALCULATE THE PROBABILITIES FOR CHOSING SOLUTION COMPONENT
	double multiplier;
    // denominator
    for(p=0;p<dados.nbProducts;p++)
        for(i=0;i<dados.nbNodes;i++)
            for(j=0;j<dados.nbNodes;j++){
				 if(ants.avail_tau[i][j][p]>0 && ants.avail_cap[j][p]>dados.O[i][p])
					sum_tau=sum_tau+ants.avail_tau[i][j][p]*a_param.tau[i][j][p]*(pow(hvis.eta[i][j][p],beta));
			}
			cum_prob=0;
			flag=0;
    // probabilities
    for(p=0;p<dados.nbProducts;p++)
        for(i=0;i<dados.nbNodes;i++)
            for(j=0;j<dados.nbNodes;j++)
                if(ants.avail_tau[i][j][p]>0 && ants.avail_cap[j][p]>dados.O[i][p]){
                    ants.prob[i][j][p]=a_param.tau[i][j][p]*ants.avail_tau[i][j][p]*(pow(hvis.eta[i][j][p],beta))/sum_tau;
                    cum_prob=cum_prob+ants.prob[i][j][p];
                    ants.prob[i][j][p]=cum_prob;
#ifdef HISTORY
						ofstream myfile;
						myfile.open ("history1.txt",ios::app);
						myfile << "cum_prob =" << cum_prob<< endl;
						myfile.close();
#endif
					flag=1;                    
                }else{
                    ants.prob[i][j][p]=-1; // to be sure that an unavailable solution component is not chosen
                }

    // Roulette Wheel
    if(flag>0){
        double r;
        r = unifRand()*cum_prob;
#ifdef HISTORY
		ofstream myfile;
		myfile.open ("history1.txt",ios::app);
		myfile << "r " << r << endl;
		myfile.close();
#endif
        for(p=dados.nbProducts-1;p>=0;p--)
            for(i=dados.nbNodes-1;i>=0;i--)
                for(j=dados.nbNodes-1;j>=0;j--)
                    if(ants.prob[i][j][p]>=r){
#ifdef HISTORY
						ofstream myfile;
						myfile.open ("history1.txt",ios::app);
						myfile << "ants.prob["<<i<<"]["<<j<<"]["<<p<<"] =" << ants.prob[i][j][p]<<" >= "<< r << endl;
						myfile.close();
#endif
                        indices.prod=p;
                        indices.node=i;
                        indices.hub=j;
                    }
    }
    else{
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






