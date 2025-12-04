//
//  runaco.h
//
//  Created by Eliana Fernandes
//  Copyright (c) 2015 Eliana Fernandes. All rights reserved.
//

#ifndef ACO_MPHL1_runaco_h
#define ACO_MPHL1_runaco_h

#include <iostream>

#include <fstream>      /* using data from files*/
#include <algorithm>    /* max_element, sort, find */
#include <math.h>       /* sqrt */
#include <stdlib.h>     /* srand, rand */
#include <time.h>       /* time elapsed */


#include "unifrand.h"
#include "readdata.h"
#include "greedy.h"
#include "roulette.h"
#include "preproc.h"
#include "actions.h"
#include "closehub.h"
#include "closerandomhub.h"
#include "localsearch.h"
#include "ls.h"
#include "acovar.h"
#include "getsolutions.h"
#include "out.h"
#include "runcplex.h"
#include "runcplexLR.h"
#include "computegap.h"

using namespace std;

int runAco(data dat, const char* fichIn, const char* fichOut);

#endif /* defined(ACO_MPHL1_runaco_h) */
