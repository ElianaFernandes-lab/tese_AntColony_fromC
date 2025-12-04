//
//  GetSolution.cpp
//
//  Created by Eliana Fernandes
//  Copyright (c) 2015 Eliana Fernandes. All rights reserved.
//

#include "GetSolutions.h"

solution getSolution(data dados,ant ants, iteration iter){
    int i,j,k,p;
    solution sol;
    
    //memory allocation
    sol.z = new (nothrow) int [dados.nbNodes];
    if (sol.z == 0)
        cout<<"Error: memory could not be allocated for sol.z\n\n";
    
    sol.x = new (nothrow) int**[dados.nbNodes];
    if (sol.x == 0)
        cout<<"Error: memory could not be allocated\n";
    for (i=0; i<dados.nbNodes; ++i){
        sol.x[i] = new (nothrow) int*[dados.nbNodes];
        if (sol.x[i] == 0)
            cout<<"Error: memory could not be allocated\n";
        for (j=0; j<dados.nbNodes; ++j){
            sol.x[i][j] = new (nothrow) int[dados.nbProducts];
            if (sol.x[i][j] == 0)
                cout<<"Error: memory could not be allocated\n";
        }
    }

	sol.y = new (nothrow) double***[dados.nbNodes];
	if (sol.y == 0)
		cout<<"Error: memory could not be allocated\n";
	for (i=0; i<dados.nbNodes; ++i){
		sol.y[i] = new (nothrow) double**[dados.nbNodes];
		if (sol.y[i] == 0)
			cout<<"Error: memory could not be allocated\n";
		for (j=0; j<dados.nbNodes; ++j){
			sol.y[i][j] = new (nothrow) double*[dados.nbNodes];
			if (sol.y[i][j] == 0)
				cout<<"Error: memory could not be allocated\n";
			for (k=0; k<dados.nbNodes; ++k){
				sol.y[i][j][k] = new (nothrow) double[dados.nbProducts];
				if (sol.y[i][j][k] == 0)
					cout<<"Error: memory could not be allocated\n";
			}
		}
	}
	    
    /// z is already 0 and 1
    for (i=0; i<dados.nbNodes; i++)
        sol.z[i]=ants.z[i];
    
    /// initialized to zero
    for(p=0;p<dados.nbProducts;p++)
        for (j=0; j<dados.nbNodes; j++)
            for (i=0; i<dados.nbNodes; i++)
                sol.x[i][j][p]=0;

	for (i=0; i<dados.nbNodes; i++) 
		for (j=0; j<dados.nbNodes; j++) 
			for (k=0; k<dados.nbNodes; k++) 
				for(p=0; p<dados.nbProducts; p++) 
					sol.y[i][j][k][p]=0;

    // get sol.x
    for(p=0;p<dados.nbProducts;p++)
        for (j=0; j<dados.nbNodes; j++)
            for (i=0; i<dados.nbNodes; i++)
                if(iter.x_best[i][p]==j)
                    sol.x[i][j][p]=1;
    
    sol.cost=iter.best_cost;
    
    return (sol);
};

solution_x getxSolution(data dados, iteration iter){
    int i,j,k,p;
    solution_x sol_x;

	  sol_x.x = new (nothrow) int**[dados.nbNodes];
    if (sol_x.x == 0)
        cout<<"Error: memory could not be allocated\n";
    for (i=0; i<dados.nbNodes; ++i){
        sol_x.x[i] = new (nothrow) int*[dados.nbNodes];
        if (sol_x.x[i] == 0)
            cout<<"Error: memory could not be allocated\n";
        for (j=0; j<dados.nbNodes; ++j){
            sol_x.x[i][j] = new (nothrow) int[dados.nbProducts];
            if (sol_x.x[i][j] == 0)
                cout<<"Error: memory could not be allocated\n";
        }
    }

	  sol_x.inter_x = new (nothrow) int**[dados.nbNodes];
    if (sol_x.inter_x == 0)
        cout<<"Error: memory could not be allocated\n";
    for (i=0; i<dados.nbNodes; ++i){
        sol_x.inter_x[i] = new (nothrow) int*[dados.nbNodes];
        if (sol_x.inter_x[i] == 0)
            cout<<"Error: memory could not be allocated\n";
        for (j=0; j<dados.nbNodes; ++j){
            sol_x.inter_x[i][j] = new (nothrow) int[dados.nbProducts];
            if (sol_x.inter_x[i][j] == 0)
                cout<<"Error: memory could not be allocated\n";
        }
    }

	sol_x.count_inter=0;
	// initialized to zero
	  for(p=0;p<dados.nbProducts;p++)
        for (j=0; j<dados.nbNodes; j++)
            for (i=0; i<dados.nbNodes; i++)
                  sol_x.x[i][j][p]=0;
	  for(p=0;p<dados.nbProducts;p++)
        for (j=0; j<dados.nbNodes; j++)
			for (i=0; i<dados.nbNodes; i++)
				sol_x.inter_x[i][j][p]=0;
	
 // get sol.x
    for(p=0;p<dados.nbProducts;p++)
        for (j=0; j<dados.nbNodes; j++)
            for (i=0; i<dados.nbNodes; i++)
                if(iter.x_best[i][p]==j)
                    sol_x.x[i][j][p]=1;

	// get inter_hub connections
	 for(p=0;p<dados.nbProducts;p++)
        for (j=0; j<dados.nbNodes; j++)
			if(sol_x.x[j][j][p]==1){
				for (i=0; i<dados.nbNodes; i++)
					if(sol_x.x[i][i][p]==1){
						sol_x.inter_x[i][j][p]=1;
						sol_x.inter_x[j][i][p]=1;
						sol_x.count_inter++;
					}
			}	
	return(sol_x);
}


solution getIterSolution(data dados, best bst){
    int i,j,k,p;
    solution sol;
    
    //memory allocation
    sol.z = new (nothrow) int [dados.nbNodes];
    if (sol.z == 0)
        cout<<"Error: memory could not be allocated for sol.z\n\n";
    
    sol.x = new (nothrow) int**[dados.nbNodes];
    if (sol.x == 0)
        cout<<"Error: memory could not be allocated\n";
    for (i=0; i<dados.nbNodes; ++i){
        sol.x[i] = new (nothrow) int*[dados.nbNodes];
        if (sol.x[i] == 0)
            cout<<"Error: memory could not be allocated\n";
        for (j=0; j<dados.nbNodes; ++j){
            sol.x[i][j] = new (nothrow) int[dados.nbProducts];
            if (sol.x[i][j] == 0)
                cout<<"Error: memory could not be allocated\n";
        }
    }

	sol.y = new (nothrow) double***[dados.nbNodes];
	if (sol.y == 0)
		cout<<"Error: memory could not be allocated\n";
	for (i=0; i<dados.nbNodes; ++i){
		sol.y[i] = new (nothrow) double**[dados.nbNodes];
		if (sol.y[i] == 0)
			cout<<"Error: memory could not be allocated\n";
		for (j=0; j<dados.nbNodes; ++j){
			sol.y[i][j] = new (nothrow) double*[dados.nbNodes];
			if (sol.y[i][j] == 0)
				cout<<"Error: memory could not be allocated\n";
			for (k=0; k<dados.nbNodes; ++k){
				sol.y[i][j][k] = new (nothrow) double[dados.nbProducts];
				if (sol.y[i][j][k] == 0)
					cout<<"Error: memory could not be allocated\n";
			}
		}
	}

    /// z is already 0 and 1
    for (i=0; i<dados.nbNodes; i++)
        sol.z[i]=bst.z[i];
	
    /// initialized to zero
    for(p=0;p<dados.nbProducts;p++)
        for (j=0; j<dados.nbNodes; j++)
            for (i=0; i<dados.nbNodes; i++)
                sol.x[i][j][p]=0;

	for (i=0; i<dados.nbNodes; i++) 
		for (j=0; j<dados.nbNodes; j++) 
			for (k=0; k<dados.nbNodes; k++) 
				for(p=0; p<dados.nbProducts; p++) 
					sol.y[i][j][k][p]=0;

    // get sol.x
    for(p=0;p<dados.nbProducts;p++)
        for (j=0; j<dados.nbNodes; j++)
            for (i=0; i<dados.nbNodes; i++)
                if(bst.x[i][p]==j)
                    sol.x[i][j][p]=1;
    
    sol.cost=bst.cost;
    
    return (sol);
};

solution getBestSolution(data dados, ant ants, iteration iter){
    
    int i,j,k,p;
    solution sol;
    
    //memory allocation
    sol.z = new (nothrow) int [dados.nbNodes];
    if (sol.z == 0)
        cout<<"Error: memory could not be allocated for sol.z\n\n";
    
    sol.x = new (nothrow) int**[dados.nbNodes];
    if (sol.x == 0)
        cout<<"Error: memory could not be allocated\n";
    for (i=0; i<dados.nbNodes; ++i){
        sol.x[i] = new (nothrow) int*[dados.nbNodes];
        if (sol.x[i] == 0)
            cout<<"Error: memory could not be allocated\n";
        for (j=0; j<dados.nbNodes; ++j){
            sol.x[i][j] = new (nothrow) int[dados.nbProducts];
            if (sol.x[i][j] == 0)
                cout<<"Error: memory could not be allocated\n";
        }
    }

	sol.y = new (nothrow) double***[dados.nbNodes];
	if (sol.y == 0)
		cout<<"Error: memory could not be allocated\n";
	for (i=0; i<dados.nbNodes; ++i){
		sol.y[i] = new (nothrow) double**[dados.nbNodes];
		if (sol.y[i] == 0)
			cout<<"Error: memory could not be allocated\n";
		for (j=0; j<dados.nbNodes; ++j){
			sol.y[i][j] = new (nothrow) double*[dados.nbNodes];
			if (sol.y[i][j] == 0)
				cout<<"Error: memory could not be allocated\n";
			for (k=0; k<dados.nbNodes; ++k){
				sol.y[i][j][k] = new (nothrow) double[dados.nbProducts];
				if (sol.y[i][j][k] == 0)
					cout<<"Error: memory could not be allocated\n";
			}
		}
	}

    
    /// z is already 0 and 1
    for (i=0; i<dados.nbNodes; i++)
        sol.z[i]=ants.z[i];
    
    /// initialized to zero
    for(p=0;p<dados.nbProducts;p++)
        for (j=0; j<dados.nbNodes; j++)
            for (i=0; i<dados.nbNodes; i++)
                sol.x[i][j][p]=0;
    
    for(p=0;p<dados.nbProducts;p++)
        for (j=0; j<dados.nbNodes; j++)
            for (i=0; i<dados.nbNodes; i++)
                if(iter.x_best[i][p]==j)
                    sol.x[i][j][p]=1;

    sol.cost=iter.best_cost;
    
    return (sol);
}