//
//  hvis.h
//
//  Created by Eliana Fernandes
//  Copyright (c) 2015 Eliana Fernandes. All rights reserved.
//

#ifndef __ACO13_3__hvis__
#define __ACO13_3__hvis__

#include <iostream>

#include "readdata.h"
#include "acovar.h"
#include "preproc.h"


struct heur_vis{
    // Total Flow Cost (tfc_ip - approximation of total flow cost for each node i acting as single hub for each
    // product in p the network)
    double **tfc;
    //Hub Fixed Cost (hfc_ip - hub fixed cost + cost of dedicating hub)
    double **hfc;
    double ***eta;
};

heur_vis HVis(data dados,pre_prc pre_p);

#endif /* defined(__ACO13_3__hvis__) */
