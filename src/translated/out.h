//
//  out.h
//
//  Created by Eliana Fernandes
//  Copyright (c) 2015 Eliana Fernandes. All rights reserved.
//

#ifndef __ACO13_13__out__
#define __ACO13_13__out__

#include <iostream>
#include <fstream>
#include <iomanip>
#include "acovar.h"

using namespace std;

int prepareOutputFile(const char *fichIn, const char *fichOut,double scl_prm,double time_LR);

int OutputFile(int n_iter, int best_iteration,const char *fichOut,double vOpt, double tOpt, double vACO, double tACO, double totaltACO, double gap);

#endif /* defined(__ACO13_13__out__) */
