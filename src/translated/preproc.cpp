//
//  preproc.cpp
//
//  Created by Eliana Fernandes
//  Copyright (c) 2015 Eliana Fernandes. All rights reserved.
//

#include "preproc.h"
#include "readdata.h"

pre_prc preProc(data dados){
    
    int i,j,p;
    
    pre_prc pre_p;
    
    pre_p.allow = new(nothrow) int ** [dados.nbNodes];
    if (pre_p.allow == 0)
        cout<<"Error: memory could not be allocated\n";
    for (i=0; i<dados.nbNodes; ++i) {
        pre_p.allow [i]= new (nothrow) int * [dados.nbNodes];
        if (pre_p.allow[i] == 0)
            cout<<"Error: memory could not be allocated\n";
        for (j=0; j<dados.nbNodes; ++j){
            pre_p.allow [i][j]= new (nothrow) int [dados.nbProducts];
            if (pre_p.allow[i][j] == 0)
                cout<<"Error: memory could not be allocated\n";
        }
    }
    
    /// pre processing values initialized to 1
    for(i=0;i<dados.nbNodes;i++)
        for(j=0;j<dados.nbNodes;j++)
            for(p=0;p<dados.nbProducts;p++)
                pre_p.allow[i][j][p]=1;
    
    
    /// EK 1999
    /// Compute pre for 1.Capacity (13)
    for(p=0;p<dados.nbProducts;p++)
        for(j=0;j<dados.nbNodes;j++)
            for(i=0;i<dados.nbNodes;i++)
                if(dados.O[i][p]>dados.Gamma[j][p]-dados.O[j][p])
					if(i!=j) //!!!FIM!!!
						pre_p.allow[i][j][p]=0;

    
#ifdef	USE_FIXED_COST_PRE
	/// Compute pre for 2.Fixed Cost (13)
	for(p=0;p<dados.nbProducts;p++)
		for(i=0;i<dados.nbNodes;i++)
			for(j=0;j<dados.nbNodes;j++)
				if(dados.O[i][p]<dados.Gamma[i][p] &&
					dados.d[i][j]*(dados.chi[p]*dados.O[i][p]+dados.delta[p]*dados.D[i][p])>
					(dados.f[i][p]+dados.g[i])+dados.alpha[p]*dados.d[i][j]*(dados.O[i][p]+dados.D[i][p]-2*dados.w[i][i][p]))
					pre_p.allow[i][j][p]=0;

#ifdef HISTORY
	ofstream myfile;
	myfile.open ("history1.txt",ios::app);
	myfile<<"FIXED COST PRE USED"<<endl;
	myfile.close();
#endif
#endif  
    /// Compute pre for 3.Inference (13)
    // if hub j is closed, no node can be allocated to it
    for(p=0;p<dados.nbProducts;p++)
        for(j=0;j<dados.nbNodes;j++)
            if(pre_p.allow[j][j][p]<1)
                for(i=0;i<dados.nbNodes;i++)
                    if(j!=i)
                        pre_p.allow[i][j][p]=0;

    return (pre_p);
    
}

