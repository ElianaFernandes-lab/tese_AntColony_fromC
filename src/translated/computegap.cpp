//
//  computegap.cpp
//
//  Created by Eliana Fernandes
//  Copyright (c) 2015 Eliana Fernandes. All rights reserved.
//

#include "computegap.h"

gap computeGap(const char *fichIn, const char *fichCompare, double aco_cost){
	
	gap desvio;
	double aux_cost;
	double aux_time;
	char *aux_inst_name;

	aux_inst_name = new (nothrow) char[20];
	desvio.inst_name = new (nothrow) char[20];
	
	ifstream inFile(fichIn);
    // Check input file
    if ( !inFile ) {
        cerr<<"Input file could not be opened\n";
        system ("pause");
		desvio.value=1000;
		desvio.time=1000;
	}
    
    // Read number of nodes and number of products
	int i=0;
	int stop=0;
	do{
		//if()
		inFile >> aux_inst_name >> aux_cost >> aux_time;
		if(strcmp (aux_inst_name,fichCompare) == 0){
			cout << aux_inst_name <<endl;
			desvio.inst_name = aux_inst_name;
			desvio.cost = aux_cost;
			desvio.time = aux_time;
			desvio.value = 100.0*(aco_cost-desvio.cost)/desvio.cost;
			cout << "desvio.inst_name" << desvio.inst_name << endl;
			cout << " desvio.cost" << desvio.cost << endl;
			cout << " desvio.value" << desvio.value << endl;
			cout << " desvio.time" << desvio.time << endl;

			stop=1;
		}
		if(inFile.eof()){
			stop=1;
			cout << "ERROR: OPTIMAL VALUE NOT FOUND -- GAP WILL BE PUT TO 1000"<<endl;
			desvio.value=1000;
			desvio.time=1000;
		}
	} while(stop<1);
 
	return (desvio);
}