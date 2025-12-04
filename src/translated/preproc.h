//
//  preproc.h
//
//  Created by Eliana Fernandes
//  Copyright (c) 2015 Eliana Fernandes. All rights reserved.
//

#ifndef __ACO13_2__preproc__
#define __ACO13_2__preproc__

#include <iostream>
#include "readdata.h"
#include "acovar.h"

struct pre_prc{
    // PRE PROCESSING: pre_i,j,p --> eliminates the solution variables that cannot belong to the optimal solution or
    // that would always produce an infeasible solution (reduces the search space)
    //pre is 0 if node i for product p cannot be assigned to hub j, 1 otherwise
    int ***allow;
};

pre_prc preProc(data dados);

#endif /* defined(__ACO13_2__preproc__) */
