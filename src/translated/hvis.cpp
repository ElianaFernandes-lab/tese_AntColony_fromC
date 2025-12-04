//
//  hvis.cpp
//
//  Created by Eliana Fernandes
//  Copyright (c) 2015 Eliana Fernandes. All rights reserved.
//

#include "hvis.h"

heur_vis HVis(data dados,pre_prc pre_p){
    
    int i,j,p,l;
    double aux,aux1_eta,aux2_eta;
    heur_vis hv;
    
    // memory allocation
    hv.tfc = new (nothrow) double*[dados.nbNodes];
    if (hv.tfc == 0)
        cout<<"Error: memory could not be allocated\n\n";
    for (i=0; i<dados.nbNodes; ++i){
        hv.tfc[i] = new (nothrow) double[dados.nbProducts];
        if (hv.tfc[i] == 0)
            cout<<"Error: memory could not be allocated\n";
    }
    
    hv.eta = new (nothrow) double**[dados.nbNodes];
    if (hv.eta == 0)
        cout<<"Error: memory could not be allocated\n";
    for (i=0; i<dados.nbNodes; ++i){
        hv.eta[i] = new (nothrow) double*[dados.nbNodes];
        if (hv.eta[i] == 0)
            cout<<"Error: memory could not be allocated\n";
        for (j=0; j<dados.nbNodes; ++j){
            hv.eta[i][j] = new (nothrow) double[dados.nbProducts];
            if (hv.eta[i][j] == 0)
                cout<<"Error: memory could not be allocated\n";
        }
    }
    
    hv.hfc = new (nothrow) double*[dados.nbNodes];
    if (hv.hfc == 0)
        cout<<"Error: memory could not be allocated\n";
    for (i=0; i<dados.nbNodes; ++i){
        hv.hfc[i] = new (nothrow) double[dados.nbProducts];
        if (hv.hfc[i] == 0)
            cout<<"Error: memory could not be allocated\n";
    }
    
    // Total Flow Cost tfc -> star archictecture where there is a single hub that handles all the flow
    // initialization
    for (p=0; p<dados.nbProducts; p++)
        for (i=0; i<dados.nbNodes; i++)
            hv.tfc[i][p]=0;

    for (p=0; p<dados.nbProducts; p++)
        for (i=0; i<dados.nbNodes; i++){
            aux=0.0;
            for (j=0; j<dados.nbNodes; j++){
                for (l=0; l<dados.nbNodes; l++)
                    aux=aux+dados.w[j][l][p]*(dados.chi[p]*dados.d[j][i]+dados.delta[p]*dados.d[i][l]);
            }
            hv.tfc[i][p]=aux;
        }
    
	// Hub Fixed Cost hfc -> the hub fixed cost
	// initialization
    for (p=0; p<dados.nbProducts; p++)
        for (i=0; i<dados.nbNodes; i++)
            hv.hfc[i][p]=0;
    
    for (p=0; p<dados.nbProducts; p++)
        for (i=0; i<dados.nbNodes; i++){
            hv.hfc[i][p]=dados.f[i][p]+dados.g[i];
        }

	// eta is only used for openinbg hubs (for now), 
	//will be initialized to 1 so it doesn't have influence in the other solution comnponents
	// in other solution components, sao inicializados a 100 assim como os valores iniciais de pheromona
    for (p=0; p<dados.nbProducts; p++)
        for (i=0; i<dados.nbNodes; i++)
            for (j=0; j<dados.nbNodes; j++)
                hv.eta[i][j][p]=100;
	//para que os valores da heuristic visibility sejam mais ou menos da mesma ordem de grandeza
	aux1_eta=0.0;
	 for (p=0; p<dados.nbProducts; p++)
        for (i=0; i<dados.nbNodes; i++)
			aux1_eta=aux1_eta+hv.tfc[i][p]*hv.hfc[i][p];
	 
	 aux1_eta=aux1_eta*10;
	 /* aux2_eta=0.0;
	 for (p=0; p<dados.nbProducts; p++)
	 for (i=0; i<dados.nbNodes; i++)
	 for (j=0;j<dados.nbNodes; j++)
	 if(i!=j)
	 aux2_eta=aux2_eta+pow(dados.d[i][j]*(dados.chi[p]*dados.O[i][p]+dados.delta[p]*dados.D[i][p])+hv.hfc[i][p],1);*/
	 //aux2_eta=0;

#ifdef USE_HVIS
    for (p=0; p<dados.nbProducts; p++)
        for (i=0; i<dados.nbNodes; i++)
            hv.eta[i][i][p]=pre_p.allow[i][i][p]*aux1_eta/(hv.tfc[i][p]*hv.hfc[i][p]);

	//for (p=0; p<dados.nbProducts; p++)
	//      for (i=0; i<dados.nbNodes; i++)
	//	for (j=0;j<dados.nbNodes; j++)
	//		if(i!=j)
	//			// transfer and collection costs
	//			hv.eta[i][j][p]=pre_p.allow[i][j][p]*aux2_eta/(dados.d[i][j]*(dados.chi[p]*dados.O[i][p]+dados.delta[p]*dados.D[i][p])+hv.hfc[i][p]);
#ifdef HISTORY
	ofstream myfile;
	myfile.open ("history1.txt",ios::app);
	myfile<<"HEURISTIC VISIBILITY USED"<<endl;
	myfile.close();
#endif
#endif

    return (hv);
}//heur_vis HVis(data dados,pre_prc pre_p)