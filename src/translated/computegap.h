//
//  computegap.h
//
//  Created by Eliana Fernandes
//  Copyright (c) 2015 Eliana Fernandes. All rights reserved.
//

#ifndef ACO_MPHL1_computegap_h
#define ACO_MPHL1_computegap_h

#include <iostream>
#include <fstream> // file handling
#include <string.h>

using namespace std;

struct gap{
	double cost;
	double value;
	double time;
	char* inst_name;
};

gap computeGap(const char *fichIn, const char *fichCompare, double aco_cost);

#endif /* ACO_MPHL1_computegap_h*/
