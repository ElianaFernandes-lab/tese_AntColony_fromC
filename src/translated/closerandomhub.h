//
//  closerandomhub.h
//
//  Created by Eliana Fernandes
//  Copyright (c) 2015 Eliana Fernandes. All rights reserved.
//

#ifndef __ACO__closerandomhub__
#define __ACO__closerandomhub__

#include <iostream>

#include "unifrand.h"
#include "readdata.h"
#include "acovar.h"
#include "counting.h"

int CloseRandomHub(data dados, ant& ants, iteration& iter, double max_cost);

#endif /* defined(__ACO__closerandomhub__) */