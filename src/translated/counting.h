//
//  counting.h
//
//  Created by Eliana Fernandes
//  Copyright (c) 2015 Eliana Fernandes. All rights reserved.
//

#ifndef __ACO13_6__counting__
#define __ACO13_6__counting__

#include <iostream>

#include "acovar.h"

struct counters{
    int* phubs;
    int* pnodes;
    int* pprods;
	int* connects;
    int** pconnects;
    int** list_phubs;
    int** list_pnodes;
    int max_phubs;
    int max_pnodes;
};
counters counting(int nr_products, int nr_nodes,iteration iter);

#endif /* defined(__ACO13_6__counting__) */
