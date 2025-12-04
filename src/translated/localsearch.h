//
//  localsearch.h
//
//  Created by Eliana Fernandes
//  Copyright (c) 2015 Eliana Fernandes. All rights reserved.
//

#ifndef ACO_MPHL1_localsearch_h
#define ACO_MPHL1_localsearch_h

#include <iostream>

#include "unifrand.h"
#include "readdata.h"
#include "acovar.h"
#include "counting.h"
#include "actions.h"

int LocalSearch(data dados, ant& ants, iteration& iter, int iterat);

#endif /* defined(ACO_MPHL1_localsearch_h) */
