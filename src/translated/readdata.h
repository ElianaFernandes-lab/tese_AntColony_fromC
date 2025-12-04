//
//  readdata.h
//
//  Created by Eliana Fernandes
//  Copyright (c) 2015 Eliana Fernandes. All rights reserved.
//

#ifndef __ACO13_1__readdata__
#define __ACO13_1__readdata__

#include <iostream>
#include "acovar.h"

using namespace std;

struct data {
    int nbNodes, nbProducts;
    double ***w,**d,*g,**f,**Gamma;
    int *L;
    double *chi,*delta,*alpha;
    double **O,**D;
};

struct point {
    //public:
    double x;
    double y;
};

data readData(const char *fichIn);
#endif /* defined(__ACO13_1__readdata__) */
