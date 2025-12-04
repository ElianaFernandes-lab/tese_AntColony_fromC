//
//  getsolutions.h
//
//  Created by Eliana Fernandes
//  Copyright (c) 2015 Eliana Fernandes. All rights reserved.
//

#ifndef __ACO13_8__GetSolution__
#define __ACO13_8__GetSolution__

#include <iostream>

#include "acovar.h"
#include "readdata.h"


struct solution{
    int*** x;
	double **** y;
    int* z;
    double cost;
    double time;
};

struct solution_x{
    int*** x;
	int*** inter_x;
	int count_inter;
};


solution getSolution(data dados,ant ants, iteration iter);

solution_x getxSolution(data dados, iteration iter);

solution getIterSolution(data dados, best bst);

solution getBestSolution(data dados,ant ants, iteration iter);

#endif /* defined(__ACO13_8__GetSolution__) */
