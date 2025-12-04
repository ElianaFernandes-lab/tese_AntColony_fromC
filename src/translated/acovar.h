//
//  acovar.h
//
//  Created by Eliana Fernandes
//  Copyright (c) 2015 Eliana Fernandes. All rights reserved.
//

#define REP
#define NR_REP 10
//#define BAHIST
//#define HISTORY
//#define DAT_HIST
//#define TAU_HIST
#define LOG
//#define INTER_HUB
//#define LOGG
#define USE_LR  //tem que se activar tmb o SCAL_LR para este dar
//#define LS
//#define LSHIST
#define LSS
//#define LSSHIST
#define CLOSE
//#define CLOSEHIST
#define CRH //close random hub
//#define CRHHIST
//#define USE_HVIS

#define USE_PRE
//#define USE_FIXED_COST_PRE

//#define TIME
#define TIME_MEM
//#define USE_CPLEX
#define UPDATE_DEAD
#define MAX_DEAD 10

//#define UPDATE_BEST
//#define UPDATE_PARAM 0.2//0.175 // 0 for update only if best, estava 1 

#define NR_ITER 5000//100000
#define MAX_NO_BEST NR_ITER/10 //NR_ITER/100
#define MAX_TIME 360//180
#define NR_ANTS 10
#define TAU0 10
#define TAO 1000 
#define BETA 1

// local pheromone decay parameter
#define RHO 0.5 // 0<rho<1 0.25

// global pheromone decay parameter
#define GAMMA 0.5   // 0<gamma<1 0.1
#define SCALING_PARAMETER 5e6
#define SCL_P 1
#define SCAL_LR
// USED FOR SELECTION IN THE PSEUDO-RANDOM PROPORTIONAL RULE
#define Q0 0.1 //0.1

//uniform in [0,1]
#define myrand() 1.0*rand()/RAND_MAX
//continuous uniform in [min, max]
#define getrandom_c(min,max)  (min) + (myrand()*((max)-(min)))

#ifndef __ACO13_2__acovar__
#define __ACO13_2__acovar__

#include <iostream>
#include <fstream>

using namespace std;

struct aco{
    // INITIAL PHEROMONE TRAIL: tau0_i,j,p --> initial amount of pheromone on each of the solution components
    double ***tau0;
    // PHEROMONE TRAIL: tau_i,j,p --> represents the desirability of allocating node i to node j for product p
    double ***tau;
};

struct ant{
    int life;
    double cost;
    /// to start solution constructions with solution components that killed an ant
    int prod;
    int node;
    int hub;
    // available solution components (the use of avail_tau makes it simpler for solution construction)
    int ***avail_tau;
    double **avail_cap;
    //  PROBABILITIES: prob_i,j,p,k --> represents the probability that ant k will choose to allocate node i to node j for
    // product p (these probabilities are accumulated sums for the roulette wheel selection)
    double ***prob;
    // z_j=1 if hub node ant k installed at hub at node j, 0 otherwise
    int *z;
    // x_pih are the solution components
    // the variable x_pik represents the hub to which node i is allocated for product p for the kth ant
    int **x;
};

struct ind {
    int prod;
    int node;
    int hub;
};

struct iteration{
    int best_ant;
    // iteration best solution
	double ****y_best;
    int **x_best;
	int *z_best;
    // iteration best cost
    double best_cost;
};
// Global Best
struct best{
    int nr_iter;
    double cost;
    double time;
	// global best solution
	double ****y;
    int **x;
	int *z;
};

#endif /* defined(__ACO13_2__acovar__) */
