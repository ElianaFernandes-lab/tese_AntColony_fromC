//
//  checknrprods.h
//
//  Created by Eliana Fernandes
//  Copyright (c) 2015 Eliana Fernandes. All rights reserved.
//

#ifndef __ACO13_10__checknrprods__
#define __ACO13_10__checknrprods__

#include <iostream>
#include <fstream>
#include "acovar.h"

struct hubpcount{
    int* prods;
};

hubpcount checkProdConnects(int nr_products, int nr_nodes,ant ants);

#endif /* defined(__ACO13_10__checknrprods__) */
