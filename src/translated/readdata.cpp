//
//  readdata.cpp
//
//  Created by Eliana Fernandes
//  Copyright (c) 2015 Eliana Fernandes. All rights reserved.
//
#include <iostream>
#include <fstream> // file handling
#include <math.h> // sqrt

#include "readdata.h"

using namespace std;

///////// ///////// ///////// ///////// ///////// ///////// ///////// ///////// ///////// ///////// //////////
///////// ///////// ///////// /////////              FUNCTIONS        ///////// ///////// ///////// /////////
data readData(const char *fichIn){
    //cout.precision(3);
    data dados;
    double aux;
    ///////// ///////// ///////// ///////// ///////// ///////// ///////// ///////// ///////// /////////
    // read input from file
    ///////// ///////// ///////// ///////// ///////// ///////// ///////// ///////// ///////// /////////
    ifstream inFile(fichIn);
    // Check input file
    if ( !inFile ) {
        cerr<<"Input file could not be opened\n";
        system ("pause");
	}
    
    // Read number of nodes and number of products
	inFile >>  dados.nbNodes >> dados.nbProducts;
    point *coordenadas;
    
    int i,j,p;
    
	// Memory allocation
    coordenadas = new (nothrow) point[dados.nbNodes];
    if (coordenadas == 0)
        cout<<"Error: memory could not be allocated\n";
    
    dados.w = new (nothrow) double** [dados.nbNodes];
    if (dados.w == 0)
        cout<<"Error: memory could not be allocated\n";
    for (i=0; i<dados.nbNodes; ++i) {
        dados.w[i]= new (nothrow) double* [dados.nbNodes];
        if (dados.w [i] == 0)
            cout<<"Error: memory could not be allocated\n";
        for (j=0; j<dados.nbNodes; ++j){
            dados.w[i][j]= new (nothrow) double [dados.nbProducts];
            if (dados.w[i][j] == 0)
                cout<<"Error: memory could not be allocated\n";
        }
        
    }
    dados.chi = new (nothrow) double [dados.nbProducts];
    if (dados.chi == 0)
        cout<<"Error: memory could not be allocated\n";
    dados.alpha = new (nothrow) double [dados.nbProducts];
    if (dados.alpha == 0)
        cout<<"Error: memory could not be allocated\n";
    dados.delta = new (nothrow) double [dados.nbProducts];
    if (dados.delta == 0)
        cout<<"Error: memory could not be allocated\n";
    
    dados.d = new (nothrow) double* [dados.nbNodes];
    if (dados.d == 0)
        cout<<"Error: memory could not be allocated\n";
    for (i=0; i<dados.nbNodes; ++i){
        dados.d[i]= new (nothrow) double [dados.nbNodes];
        if (dados.d[i] == 0)
            cout<<"Error: memory could not be allocated\n";
    }
    
    dados.g = new (nothrow) double [dados.nbNodes];
    if (dados.g == 0)
        cout<<"Error: memory could not be allocated\n";
    
    dados.L = new (nothrow) int [dados.nbNodes];
    if (dados.L == 0)
        cout<<"Error: memory could not be allocated\n";
    
    dados.f = new (nothrow) double * [dados.nbNodes];
    if (dados.f == 0)
        cout<<"Error: memory could not be allocated\n";
    for (i=0; i<dados.nbNodes; ++i){
        dados.f[i] = new (nothrow) double [dados.nbProducts];
        if (dados.f[i] == 0)
            cout<<"Error: memory could not be allocated\n";
    }
    dados.Gamma = new (nothrow) double * [dados.nbNodes];
    if (dados.Gamma == 0)
        cout<<"Error: memory could not be allocated\n";
    for (i=0; i<dados.nbNodes; ++i){
        dados.Gamma[i] = new (nothrow) double [dados.nbProducts];
        if (dados.Gamma [i] == 0)
            cout<<"Error: memory could not be allocated\n";
    }
    dados.O = new (nothrow) double * [dados.nbNodes];
    if (dados.O == 0)
        cout<<"Error: memory could not be allocated\n";
    for (i=0; i<dados.nbNodes; ++i){
        dados.O[i] = new (nothrow) double [dados.nbProducts];
        if (dados.O[i] == 0)
            cout<<"Error: memory could not be allocated\n";
    }
    dados.D = new (nothrow) double * [dados.nbNodes];
    if (dados.D == 0)
        cout<<"Error: memory could not be allocated\n";
    for (i=0; i<dados.nbNodes; ++i){
        dados.D[i] = new (nothrow) double [dados.nbProducts];
        if (dados.D[i] == 0)
            cout<<"Error: memory could not be allocated\n";
    }
    
    // Read coordinates
	for (i=0; i<dados.nbNodes; i++)
        inFile >> coordenadas[i].x >> coordenadas[i].y;
    
	// Calculate distances
	for (i=0; i< dados.nbNodes; i++)
		for (int j=0; j< dados.nbNodes; j++) {
			aux=(coordenadas[i].x-coordenadas[j].x)*(coordenadas[i].x-coordenadas[j].x);
            aux+=(coordenadas[i].y-coordenadas[j].y)*(coordenadas[i].y-coordenadas[j].y);
			dados.d[i][j]=sqrt(aux)/1000.0;
		}
    
	// Read flows
	for (p=0; p<dados.nbProducts; ++p)
		for (i=0; i< dados.nbNodes; ++i)
			for (j=0; j< dados.nbNodes; ++j)
				inFile >> dados.w[i][j][p];
    
	// Read collection, discount and distribution costs
	for (p=0; p<dados.nbProducts; ++p)
		inFile >> dados.chi[p];
	for (p=0; p<dados.nbProducts; ++p)
		inFile >> dados.alpha[p];
	for (p=0; p<dados.nbProducts; ++p)
		inFile >> dados.delta[p];
    
	// Read set-up costs
	for (i=0; i< dados.nbNodes; i++)
		inFile >> dados.g[i];
    
	// Read maximum numbder of products that can be handled in each node
	for (i=0; i< dados.nbNodes; i++)
		inFile >> dados.L[i];
    
	// Read capacities
	for (i=0; i< dados.nbNodes; i++)
		for (p=0; p<dados.nbProducts; ++p){
			inFile >> dados.Gamma[i][p];

#ifdef DAT_HIST
			ofstream myfile;
			myfile.open ("history1.txt",ios::app);
			myfile << "dados.Gamma["<<i<<"]["<<p<<"]= " << dados.Gamma[i][p] << endl;
			myfile.close();
#endif
		}
    // Read set up costs per product
	for (i=0; i< dados.nbNodes; i++)
		for (p=0; p<dados.nbProducts; ++p)
			inFile >> dados.f[i][p];
    
    // Update the g[i] values according with the paper
	for (i=0; i<dados.nbNodes; i++){
		dados.g[i]=0.0;
		for (p=0; p<dados.nbProducts; ++p)
			dados.g[i] = dados.g[i] + dados.f[i][p];
		dados.g[i]= 2*dados.g[i]/(dados.nbProducts*1.0);
	}
    
    // Close file for reading
	inFile.close();
    
    // Calculate total flows originated at the nodes
	for (i=0; i<dados.nbNodes; ++i)
		for (p=0; p<dados.nbProducts; ++p) {
			dados.O[i][p]=0.0;
			for (j=0; j<dados.nbNodes; ++j)
				dados.O[i][p]+=dados.w[i][j][p];
#ifdef DAT_HIST
			ofstream myfile;
			myfile.open ("history1.txt",ios::app);
			myfile << "dados.O["<<i<<"]["<<p<<"]= " << dados.O[i][p] << endl;
			myfile.close();
#endif
        }
    
	// Calculate total flows destined to the nodes
	for (i=0; i<dados.nbNodes; ++i)
		for (p=0; p<dados.nbProducts; ++p) {
			dados.D[i][p]=0.0;
			for (j=0; j<dados.nbNodes; ++j)
				dados.D[i][p]+=dados.w[j][i][p];
        }
    
    
    delete [] coordenadas;
    return (dados);
}
///////// ///////// ///////// ///////// ///////// ///////// ///////// ///////// ///////// ///////// //////////
