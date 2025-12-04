//
//  unifrand.cpp
//
//  Created by Eliana Fernandes
//  Copyright (c) 2015 Eliana Fernandes. All rights reserved.
//

#include <iostream>
#include <fstream>      /* using data from files*/
#include <algorithm>    /* max_element, sort, find */
#include <math.h>       /* sqrt */
#include <stdlib.h>     /* srand, rand */
#include <time.h>       /* time elapsed */

using namespace std;

#include "unifrand.h"

// Uniform (0,1) generator
double unifRand()
{
    return rand() / double(RAND_MAX);
};
