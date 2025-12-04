//
//  runaco.cpp
//
//  Created by Eliana Fernandes
//  Copyright (c) 2015 Eliana Fernandes. All rights reserved.
//

#include "runaco.h"

int runAco(data dat, const char* fichIn, const char* fichOut){

	const char* bestLog;
	bestLog = "best.txt";
	const char* gapIn;
	gapIn = "out_best.txt";

#ifdef HISTORY
    ofstream myfile;
    myfile.open ("history1.txt",ios::app);
    myfile<<"START RUN ACO"<<endl;
	myfile << setw(12) << fichIn << endl;
    myfile.close();
#endif

	int i,j,p,k,l,it; // counters
	int best_count=0; //count the number of best solutions for stopping criterion
	int nr_dead=0; //count the number of dead ants
	int it_stop=0;
	int sum_available; // sum of available solution components to be added (used to check end of solution construction or infeasibility)
	int sum_remaining;
	//int node,hub,prod;

	clock_t t1,t2;  // to get the elapsed time
	t1=clock();

	/* initialize random seed: */
	srand ((double)time(NULL));

	// print time with defined precision
	cout.precision(4);

	ind index;
	aco a;
	pre_prc pre;
	solution sol;
	solution_x sl;
	best better;

	// get scaling parameter
	double scal_param=0.0;

	for (p=0; p<dat.nbProducts; p++)
		for (i=0; i<dat.nbNodes; i++){
			scal_param=scal_param+dat.f[i][p]+dat.g[i];
		}
		cout << "\n\nscal_param = " << scal_param << endl<<endl;

			//TIME
#ifdef TIME
	clock_t t_prepareout1,t_prepareout2;  // to get the elapsed time
	t_prepareout1=clock();
#endif
	prepareOutputFile(fichIn, fichOut, scal_param);
#ifdef TIME
	t_prepareout2=clock();
	double diff_prepareout ((double)t_prepareout2-(double)t_prepareout1);
	double time_prepareout;
	time_prepareout = diff_prepareout / CLOCKS_PER_SEC;
	cout <<endl<< "TIME prepareOutputFile: "<<time_prepareout<<endl;
#endif

#ifdef TIME_MEM
	clock_t t_memalloc1,t_memalloc2;  // to get the elapsed time
	t_memalloc1=clock();
#endif
	//// memory allocation
	cout << "ALLOCATING MEMORY"<<endl;
	/// pheromones
	a.tau0 = new (nothrow) double** [dat.nbNodes];
	if (a.tau0 == 0)
		cout<<"Error: memory could not be allocated\n";
	for (i=0; i<dat.nbNodes; ++i) {
		a.tau0[i]= new (nothrow) double* [dat.nbNodes];
		if (a.tau0 [i] == 0)
			cout<<"Error: memory could not be allocated\n";
		for (j=0; j<dat.nbNodes; ++j){
			a.tau0[i][j]= new (nothrow) double [dat.nbProducts];
			if (a.tau0 [i][j] == 0)
				cout<<"Error: memory could not be allocated\n";
		}
	}

	a.tau = new (nothrow) double ** [dat.nbNodes];
	if (a.tau == 0)
		cout<<"Error: memory could not be allocated\n";
	for (i=0; i<dat.nbNodes; ++i) {
		a.tau[i]= new (nothrow) double * [dat.nbNodes];
		if (a.tau [i] == 0)
			cout<<"Error: memory could not be allocated\n";
		for (int j=0; j<dat.nbNodes; ++j){
			a.tau[i][j]= new (nothrow) double [dat.nbProducts];
			if (a.tau [i][j] == 0)
				cout<<"Error: memory could not be allocated\n";
		}
	}

	/// ant values
	ant *ants = new (nothrow) ant [NR_ANTS];
	if (ants == 0)
		cout<<"Error: memory could not be allocated\n";

	for(k=0;k<NR_ANTS;k++){
		ants[k].avail_tau = new(nothrow) int ** [dat.nbNodes];
		if (ants[k].avail_tau == 0)
			cout<<"Error: memory could not be allocated\n";
		for (i=0; i<dat.nbNodes; ++i) {
			ants[k].avail_tau[i]= new(nothrow) int * [dat.nbNodes];
			if (ants[k].avail_tau[i] == 0)
				cout<<"Error: memory could not be allocated\n";
			for (int j=0; j<dat.nbNodes; ++j){
				ants[k].avail_tau[i][j]= new (nothrow) int [dat.nbProducts];
				if (ants[k].avail_tau[i][j] == 0)
					cout<<"Error: memory could not be allocated\n";
			}
		}


		////// avail_cap temp
		ants[k].avail_cap = new (nothrow) double * [dat.nbNodes];
		if (ants[k].avail_cap == 0)
			cout<<"Error: memory could not be allocated\n";
		for (i=0; i<dat.nbNodes; ++i){
			ants[k].avail_cap[i] = new (nothrow) double [dat.nbProducts];
			if (ants[k].avail_cap[i] == 0)
				cout<<"Error: memory could not be allocated\n";
		}

		ants[k].prob = new (nothrow) double ** [dat.nbNodes];
		if (ants[k].prob == 0)
			cout<<"Error: memory could not be allocated\n";
		for (i=0; i<dat.nbNodes; ++i) {
			ants[k].prob[i]= new (nothrow) double * [dat.nbNodes];
			if (ants[k].prob[i] == 0)
				cout<<"Error: memory could not be allocated\n";
			for (int j=0; j<dat.nbNodes; ++j){
				ants[k].prob[i][j]=new(nothrow) double [dat.nbProducts];
				if (ants[k].prob[i][j] == 0)
					cout<<"Error: memory could not be allocated\n";
			}
		}

		ants[k].x = new (nothrow) int*[dat.nbNodes];
		if (ants[k].x == 0)
			cout<<"Error: memory could not be allocated\n";
		for (i=0; i<dat.nbNodes; ++i){
			ants[k].x[i] = new (nothrow) int[dat.nbProducts];
			if (ants[k].x[i] == 0)
				cout<<"Error: memory could not be allocated\n";
		}

		ants[k].z = new (nothrow) int[dat.nbNodes];
		if (ants[k].z == 0)
			cout<<"Error: memory could not be allocated\n";
	} //for(k=0;k<NR_ANTS;k++)

	/// iteration values
	iteration itrt;

	//y
	itrt.y_best = new (nothrow) double***[dat.nbNodes];
	if (itrt.y_best == 0)
		cout<<"Error: memory could not be allocated\n";
	for (i=0; i<dat.nbNodes; ++i){
		itrt.y_best[i] = new (nothrow) double**[dat.nbNodes];
		if (itrt.y_best[i] == 0)
			cout<<"Error: memory could not be allocated\n";
		for (j=0; j<dat.nbNodes; ++j){
			itrt.y_best[i][j] = new (nothrow) double*[dat.nbNodes];
			if (itrt.y_best[i][j] == 0)
				cout<<"Error: memory could not be allocated\n";
			for (k=0; k<dat.nbNodes; ++k){
				itrt.y_best[i][j][k] = new (nothrow) double[dat.nbProducts];
				if (itrt.y_best[i][j][k] == 0)
					cout<<"Error: memory could not be allocated\n";
			}
		}
	}

	//x
	itrt.x_best = new (nothrow) int*[dat.nbNodes];
	if (itrt.x_best == 0)
		cout<<"Error: memory could not be allocated\n";
	for (i=0; i<dat.nbNodes; ++i){
		itrt.x_best[i] = new (nothrow) int[dat.nbProducts];
		if (itrt.x_best[i] == 0)
			cout<<"Error: memory could not be allocated\n";
	}

	//z
	itrt.z_best = new (nothrow) int[dat.nbNodes];
	if (itrt.z_best == 0)
		cout<<"Error: memory could not be allocated\n";

	//////AAA////////
	// BEST VALUES
	//y
	better.y = new (nothrow) double***[dat.nbNodes];
	if (better.y == 0)
		cout<<"Error: memory could not be allocated\n";
	for (i=0; i<dat.nbNodes; ++i){
		better.y[i] = new (nothrow) double**[dat.nbNodes];
		if (better.y[i] == 0)
			cout<<"Error: memory could not be allocated\n";
		for (j=0; j<dat.nbNodes; ++j){
			better.y[i][j] = new (nothrow) double*[dat.nbNodes];
			if (better.y[i][j] == 0)
				cout<<"Error: memory could not be allocated\n";
			for (k=0; k<dat.nbNodes; ++k){
				better.y[i][j][k] = new (nothrow) double[dat.nbProducts];
				if (better.y[i][j][k] == 0)
					cout<<"Error: memory could not be allocated\n";
			}
		}
	}

	//x
	better.x = new (nothrow) int*[dat.nbNodes];
	if (better.x == 0)
		cout<<"Error: memory could not be allocated\n";
	for (i=0; i<dat.nbNodes; ++i){
		better.x[i] = new (nothrow) int[dat.nbProducts];
		if (better.x[i] == 0)
			cout<<"Error: memory could not be allocated\n";
	}

	//z
	better.z = new (nothrow) int[dat.nbNodes];
	if (better.z == 0)
		cout<<"Error: memory could not be allocated\n";
	//////AAA////////

	cout << "FINISHED ALLOCATING MEMORY"<<endl;

#ifdef TIME_MEM
	t_memalloc2=clock();
	double diff_memalloc ((double)t_memalloc2-(double)t_memalloc1);
	double time_memalloc;
	time_memalloc = diff_memalloc / CLOCKS_PER_SEC;
	cout <<endl<< "TIME memalloc: "<<time_memalloc<<endl;
#endif
	///////// ///////// ///////// ////////  ITERATION INITIALIZATIONS  ///////// ///////// ///////// /////////

	// init global pheromone value (later to be TAU0 or TAU0*x_LR)
	for(p=0;p<dat.nbProducts;p++)
		for(i=0;i<dat.nbNodes;i++)
			for(j=0;j<dat.nbNodes;j++){
				a.tau0[i][j][p]=TAU0;
				a.tau[i][j][p]=TAU0;
			}
//TIME
#ifdef TIME
	clock_t t_runcplex1,t_runcplex2;  // to get the elapsed time
	t_runcplex1=clock();
#endif

#ifdef USE_LR
	cout <<endl<< "COMPUTING CPLEX LR"<<endl;
	runCplexLR(dat, a);
	cout << endl<<"FINISHED COMPUTING CPLEX LR"<<endl;
#endif

#ifdef TIME
	t_runcplex2=clock();
	double diff_runcplex ((double)t_runcplex2-(double)t_runcplex1);
	double time_runcplex;
	time_runcplex = diff_runcplex / CLOCKS_PER_SEC;
	cout <<endl<< "TIME runCplexLR: "<<time_runcplex<<endl;
#endif
	
#ifdef HISTORY
	myfile.open ("history1.txt",ios::app);
	myfile<<"CPLEX LR COMPUTED"<<endl;
	myfile << setw(12) << fichIn << endl;
	myfile.close();
#endif

#ifdef TAU_HIST
	ofstream taufile;
	taufile.open ("tau_hist.txt",ios::app);
	for (i=0; i<dat.nbNodes; i++)
		for (k=0; k<dat.nbNodes; k++) 
			for (p=0; p<dat.nbProducts; ++p) {
				if(a.tau0[i][k][p]>0)
					taufile << "a.tau0"<<"["<<i<<"]["<<k<<"]["<<p<<"] = "<< a.tau0[i][k][p] << endl;
			}
			taufile.close();
#endif

		// init global pheromone value
		for(p=0;p<dat.nbProducts;p++)
			for(i=0;i<dat.nbNodes;i++)
				for(j=0;j<dat.nbNodes;j++){
					double a_aux=a.tau0[i][j][p]*TAO+TAU0; //!!!
					a.tau0[i][j][p]=a_aux;
					a.tau[i][j][p]=a.tau0[i][j][p];
				}

#ifdef TAU_HIST
			taufile.open ("tau_hist.txt",ios::app);
			taufile << "tau init values"<< endl;
			for (i=0; i<dat.nbNodes; i++)
				for (k=0; k<dat.nbNodes; k++) 
					for (p=0; p<dat.nbProducts; ++p) {
						if(a.tau0[i][k][p]>0)
							taufile << "a.tau"<<"["<<i<<"]["<<k<<"]["<<p<<"] = "<< a.tau[i][k][p] << endl;
					}
					taufile.close();
#endif

			///////// ///////// ///////// //////   END ITERATION INITIALIZATIONS  /////// ///////// ///////// /////////
			//TIME
#ifdef TIME
			clock_t t_prprc1,t_prprc2;  // to get the elapsed time
			t_prprc1=clock();
#endif

#ifndef USE_PRE
			// memory allocation for pre-proc
			pre.allow = new(nothrow) int ** [dat.nbNodes];
			if (pre.allow == 0)
				cout<<"Error: memory could not be allocated\n";
			for (i=0; i<dat.nbNodes; ++i) {
				pre.allow [i]= new (nothrow) int * [dat.nbNodes];
				if (pre.allow[i] == 0)
					cout<<"Error: memory could not be allocated\n";
				for (j=0; j<dat.nbNodes; ++j){
					pre.allow [i][j]= new (nothrow) int [dat.nbProducts];
					if (pre.allow[i][j] == 0)
						cout<<"Error: memory could not be allocated\n";
				}
			}
			/// pre processing values initialized to 1
			for(i=0;i<dat.nbNodes;i++)
				for(j=0;j<dat.nbNodes;j++)
					for(p=0;p<dat.nbProducts;p++)
						pre.allow[i][j][p]=1;
#ifdef HISTORY
			myfile.open ("history1.txt",ios::app);
			myfile<<"PRE PROCESSING NOT DONE"<<endl;
			myfile.close();
#endif
#endif
			///////// ///////// ///////// ///////        PRE-PROCESSING        ///////// ///////// ///////// /////////
#ifdef USE_PRE
			pre = preProc(dat);
#ifdef HISTORY
			myfile.open ("history1.txt",ios::app);
			myfile<<"PRE PROCESSING DONE"<<endl;
			myfile.close();
#endif
#endif
			///////// ///////// ///////// /////////  END PRE-PROCESSING     ////// ///////// ///////// ///////// /////////
#ifdef TIME
			t_prprc2=clock();
			double diff_prprc ((double)t_prprc2-(double)t_prprc1);
			double time_prprc;
			time_prprc = diff_prprc / CLOCKS_PER_SEC;
			cout << endl<<"TIME preProc: "<<time_prprc<<endl;
#endif
			///////// ///////// ///////// //////     HEURISTIC VISIBILITY        ///////// ///////// ///////// /////////
			//TIME
#ifdef TIME
			clock_t t_hv1,t_hv2;  // to get the elapsed time
			t_hv1=clock();
#endif
			heur_vis hv;
			hv = HVis(dat, pre);
#ifdef TAU_HIST
			taufile.open ("tau_hist.txt",ios::app);
			taufile<<endl<<"HVis Values"<<endl<<endl;
			for (p=0; p<dat.nbProducts; p++)
				for (i=0; i<dat.nbNodes; i++)
					for (j=0;j<dat.nbNodes; j++)
						if(hv.eta[i][j][p]>100)
							taufile << "hv.eta["<<i<<"]["<<j<<"]["<<p<<"]= "<<hv.eta[i][j][p]<<endl;
			myfile.close();
#endif	
#ifdef TIME
			t_hv2=clock();
			double diff_hv ((double)t_hv2-(double)t_hv1);
			double time_hv;
			time_hv = diff_hv / CLOCKS_PER_SEC;
			cout << "TIME heuristic visibility: "<<time_hv<<endl;
#endif
			///////// ///////// ///////// //////       END HEURISTIC VISIBILITY         ///////// ///////// ///////// /////////

			///////// ///////// ///////// ///////// ///////// ///////// ///////// ///////// ///////// ///////// /////////
			///////// ///////// ///////// /////////               ACO            ///////// ///////// ///////// /////////
			///////// ///////// ///////// ///////// ///////// ///////// ///////// ///////// ///////// ///////// /////////
			double temp_cost;
			double max_cost=1e10;
			double global_best= max_cost;
			better.cost= max_cost;

#ifdef TIME
			clock_t t_alliter1,t_alliter2;  // to get the elapsed time
			t_alliter1=clock();
#endif
			it=0;
			//for(it=0;it<NR_ITER;it++){
			do{
				//// reset iteration values
				itrt.best_cost=max_cost;
#ifdef HISTORY
				myfile.open ("history1.txt",ios::app);
				myfile<< endl<< "ITERATION" <<it <<endl;
				myfile.close();
#endif
				// TIME
				// EACH ITER
#ifdef TIME
				clock_t t_eachiter1,t_eachiter2;  // to get the elapsed time
				t_eachiter1=clock();
#endif
				cout<<endl<<"NrIter: "<<it<<endl<<endl;
				cout << "Iter Best Cost" << itrt.best_cost<<endl;
				cout <<"Global Best: "<< global_best<<endl<<endl;
#ifdef HISTORY
				myfile.open ("history1.txt",ios::app);
				myfile<<endl<<"NrIter: "<<it<<endl<<endl;
				myfile<<"Global Best: "<< global_best<<endl<<endl;
				myfile.close();
#endif
				///////// ///////// ///////// //////    iteration  INITIALIZATIONS        ///////// ///////// ///////// /////////
				// reset cost
				temp_cost=0.0;

				// reset ant values
				// ALIVE ANTS ARE THE ONES THAT PRODUCE FEASIBLE SOLUTIONS (ant_k.life=1)
				for(k=0;k<NR_ANTS;k++){
					ants[k].life=1;
					ants[k].cost=0.0; // the cost of ant solution is intialized to zero
				}

				// SOLUTION COMPONENTS (x(i,j,p) ----->  initialized to -1 because it can take value 0)
				for(k=0;k<NR_ANTS;k++)
					for(p=0;p<dat.nbProducts;p++)
						for(i=0;i<dat.nbNodes;i++)
							ants[k].x[i][p]=-1;

				// SOLUTION BEST COMPONENTS (x(i,j,iter) ----->  initialized to -1 because it can take value 0)
				for(p=0;p<dat.nbProducts;p++)
					for(i=0;i<dat.nbNodes;i++)
						itrt.x_best[i][p]=-1;
				//z_values
				for(j=0;j<dat.nbNodes;j++){
					itrt.z_best[j]=0;
				}

				// OPENED HUBS (z(j) ----->  initialized to 0)
				for(k=0;k<NR_ANTS;k++)
					for(j=0;j<dat.nbNodes;j++)
						ants[k].z[j]=0;

				// AVAILABLE CAPACITY AT EACH POTENTIAL HUB NODE j FOR PRODUCT p and EACH SOLUTION COMPONENT OF ANT k
				// (avail_cap(j,p,k) ---> initialized to capacity of each node)
				for(k=0;k<NR_ANTS;k++)
					for(p=0;p<dat.nbProducts;p++)
						for(j=0;j<dat.nbNodes;j++)
							ants[k].avail_cap[j][p]=dat.Gamma[j][p];

				// compute pre-processing results
				// AVAILABLE SOLUTIONS (x(i,j,p) ----->  initialized to 1 or 0, depending on pre-processing)
				for(k=0;k<NR_ANTS;k++)
					for(p=0;p<dat.nbProducts;p++)
						for(i=0;i<dat.nbNodes;i++)
							for(j=0;j<dat.nbNodes;j++)
								ants[k].avail_tau[i][j][p]=pre.allow[i][j][p];
#ifdef HISTORY
				myfile.open ("history1.txt",ios::app);
				myfile<<"AVAILABLE SOLUTION COMPONENTS"<<endl;
				//for(k=0;k<NR_ANTS;k++) é igual para todas nesta fase
				// count nr of available solutions ant total number of silutions
				int tot_sol=0;
				int av_sol=0;
				for(p=0;p<dat.nbProducts;p++)
					for(i=0;i<dat.nbNodes;i++)
						for(j=0;j<dat.nbNodes;j++){
							if(ants[0].avail_tau[i][j][p]>0){
								myfile << "ants.avail.tau"<<"["<<i<<"]["<<j<<"]["<<p<<"] = "<< ants[0].avail_tau[i][j][p] << endl;
								av_sol++;
							}
							tot_sol++;
						}
						myfile<<"TOTAL SOLUTION COMPONENTS: "<<tot_sol<<endl;
						myfile<<"AVAILABLE SOLUTION COMPONENTS: "<<av_sol<<endl;
						myfile.close();
#endif

				///////// ///////// ///////// //////    END iteration INITIALIZATIONS     ///////// ///////// ///////// /////////

				///////// ///////// ///////// //////              COLONY LOOP               ///////// ///////// ///////// /////////
				// TIME
				// ALL ANTS
#ifdef TIME
				clock_t t_allants1,t_allants2;  // to get the elapsed time
				t_allants1=clock();
#endif
				for(k=0;k<NR_ANTS;k++){
#ifdef HISTORY
					myfile.open ("history1.txt",ios::app);
					myfile<< endl<< "ANT " <<k <<endl;
					myfile.close();
#endif
					// TIME
					// EACH ANT
#ifdef TIME
					clock_t t_eachant1,t_eachant2;  // to get the elapsed time
					t_eachant1=clock();
#endif
					temp_cost=0.0;

					///////// ///////// ///////// //////          BUILD SOLUTION          ///////// ///////// ///////// /////////
					int ant_life=1;
					if(k>0 && ants[k-1].life<=0){
						ant_life=0;
						index.prod=ants[k-1].prod;
						index.node=ants[k-1].node;
						index.hub=ants[k-1].hub;
					}//if(k>0 && ants[k-1].life<=0)
					do{
						///////// ///////// //////   SOLUTION COMPONENT SELECTION RULE   ///// ///////// /////////
						// INIT
						if(ant_life<=0){
							// if the previous ant is dead, start construction of solution with the
							// solution component that causes infeasibility
							ant_life=1;
#ifdef HISTORY
							myfile.open ("history1.txt",ios::app);
							myfile<<"PREVIOUS ANT DEAD"<<endl;
							myfile <<"index.prod = "<<index.prod<<endl;
							myfile <<"index.node = "<<index.node<<endl;
							myfile <<"index.hub = "<<index.hub<<endl;
							myfile.close();
#endif
						}// if(ant_life<=0)
						else{
							// PSEUDO-RANDOM PROPORTIONAL RULE
							double q = unifRand();
							if(q<=Q0){
								index=Greedy(ants[k].life,dat,a,ants[k]);
#ifdef HISTORY
								myfile.open ("history1.txt",ios::app);
								myfile<<"Greedy"<<endl;
							/*	myfile <<"index.prod = "<<index.prod<<endl;
								myfile <<"index.node = "<<index.node<<endl;
								myfile <<"index.hub = "<<index.hub<<endl;*/
								myfile.close();
#endif
							}
							else{
								index=RouletteWheel(ants[k].life,dat,a,ants[k],pre,hv);
#ifdef HISTORY
								myfile.open ("history1.txt",ios::app);
								myfile<<"Roulette"<<endl;
								/*myfile <<"index.prod = "<<index.prod<<endl;
								myfile <<"index.node = "<<index.node<<endl;
								myfile <<"index.hub = "<<index.hub<<endl;*/
								myfile.close();
#endif
							}
							// END OF PSEUDO-RANDOM PROPORTIONAL RULE
						} //else // if(ant_life<=0)

						if(ants[k].life>0){
							// Actions taken if ant not dead
							AddSolutionComponent(index, dat, ants[k],temp_cost);
							LocalPheromoneUpdate(index.prod, index.hub, index.node, a);
							// Apply Single Allocation
							if(index.node!=index.hub){
								ApplySingleAllocationRules(index.prod, index.hub, index.node, dat.nbNodes, ants[k], 1, 1);
							}else{
								ApplySingleAllocationRules(index.prod, index.hub, index.node, dat.nbNodes, ants[k], 1, 0);
							}
							if(index.node==index.hub){
								//Apply Lk restrictions (to only allow available solutions to be added)
								ApplyLkRules(index.prod, index.hub, dat, ants[k]);
							}
							// update available capacity of the to be opened hub
							UpdateAvailableCapacities(index.prod, index.hub, index.node, dat, ants[k], k);

							/// not necessary but left here just in case
							if(ants[k].avail_cap[index.hub][index.prod]<=0){
								cout << "runaco capacity violation"<<endl;
								system ("pause");
							}

							// open hub if already not opened
							OpenHub(index, dat, ants[k],temp_cost);

							// if hub not dedicated to product p yet, dedicate it to product p
							DedicateHub(index, dat, ants[k], a, k, temp_cost);

							///////// ///////// //// END SOLUTION COMPONENT SELECTION RULE   ///// ///////// 

							///////// /////////   COMPUTE NUMBER OF AVAILABLE SOLUTIONS   ///// /////////
							sum_available=0;
							for(p=0;p<dat.nbProducts;p++)
								for(i=0;i<dat.nbNodes;i++)
									for(j=0;j<dat.nbNodes;j++)
										sum_available=sum_available+ants[k].avail_tau[i][j][p];
							
						}//if(ants[k].life>0)
						else{
							ants[k].cost=max_cost;
							break;
						}
						///////// /////////   COMPUTE NUMBER OF REMAINING SOLUTIONS  !!! (para evitar bugs)   ///// /////////
						sum_remaining=0;
						for(p=0;p<dat.nbProducts;p++)
							for(i=0;i<dat.nbNodes;i++)
								if(ants[k].x[i][p]==-1)
									sum_remaining++;
						
#ifdef HISTORY
						myfile.open ("history1.txt",ios::app);
						myfile<<endl<<"Remaining and available solutions:"<<endl;
						myfile <<"sum_remaining = "<<sum_remaining<<endl;
						myfile <<"sum_available = "<<sum_available<<endl;
						myfile.close();
#endif
						if(sum_available==0 && sum_remaining>0){
							ants[k].life=0;
							ants[k].cost=max_cost;

							// start next solution with one of the remaining connections
							//connect hub to itself
							int found=0;
							for(p=0;p<dat.nbProducts;p++){
								for(i=0;i<dat.nbNodes;i++){
									if(ants[k].x[i][p]==-1 || dat.O[i][p]<dat.Gamma[i][p]){
										ants[k].prod=p;
										ants[k].hub=i;
										ants[k].node=i;
										//update solution component pheromone for the cases
										// where getting a feasible solution is hard
										// !!!
										break;
										found=1;
									}
									if(found==1) break;
								}
								if(found==1) break;
							}
							break;
						}// if(sum_available==0 && sum_remaining>0)
						///////// ///////// ////     END COMPUTE SUM AVAILABLE           ///// ///////// /////////
					} while(sum_available>0);
					/// BEST ANT COST
					if(ants[k].life>0)
						GetBestAntCost(dat.nbProducts,dat.nbNodes, ants[k], itrt, it,k);
#ifdef TIME
					t_eachant2=clock();
					double diff_eachant ((double)t_eachant2-(double)t_eachant1);
					double time_eachant;
					time_eachant = diff_eachant / CLOCKS_PER_SEC;
					//cout << "TIME each ant: "<<time_eachant<<endl;
#endif
#ifdef HISTORY
					myfile.open ("history1.txt",ios::app);
					myfile<<endl<<"ANT :"<<k<<" COST = "<<ants[k].cost ;
					myfile.close();
#endif
				}//for(k=0;k<NR_ANTS;k++)
#ifdef TIME
				t_allants2=clock();
				double diff_allants ((double)t_allants2-(double)t_allants1);
				double time_allants;
				time_allants = diff_allants / CLOCKS_PER_SEC;
				cout << endl<<"TIME all ants: "<<time_allants<<endl;
#endif
				///////// ///////// ///////// //////            END COLONY LOOP               ///////// ///////// ///////// /////////
				// used for writting simplicity
				int kk=itrt.best_ant; // k is being used in colony's loops, can't be used here
#ifdef HISTORY
				myfile.open ("history1.txt",ios::app);
				myfile<<endl<<"BEST ANT :"<<kk<<endl;
				myfile <<"ant_cost = "<<itrt.best_cost<<endl;
				myfile.close();
#endif
				if(ants[kk].life>0){
					
						///////// CLOSE HUB WITH ONE CONNECTION
#ifdef CLOSE
					for(i=0;i<dat.nbNodes;i++)
						CloseHub(dat, ants[kk], itrt, max_cost);
#endif
					///////// CLOSE RANDOM HUB
#ifdef CRH
					for(i=0;i<dat.nbNodes;i++)
						CloseRandomHub(dat, ants[kk], itrt, max_cost);
#endif
				///////// LOCAL SEARCH RANDOM RELOCATE RANDOM NODE
#ifdef LS
					LocalSearch(dat, ants[kk], itrt, it);
#endif
						///////// CLOSE HUB
#ifdef CLOSE
					CloseHub(dat, ants[kk], itrt, max_cost);
#endif
					///////// LOCAL SEARCH LISTS RELOCATE NODE
#ifdef LSS
					LocalSearch1(dat, ants[kk], itrt, it);
#endif
#ifdef CLOSE
					CloseHub(dat, ants[kk], itrt, max_cost);
#endif
#ifdef CRH
					CloseRandomHub(dat, ants[kk], itrt, max_cost);
#endif
					// Estimate inter-hub costs
					sl=getxSolution(dat, itrt);
#ifdef INTER_HUB
					const char* fLog = "output_Inter_hub.log";
					ofstream logFile(fLog,ios::app);
					logFile.setf(ios::fixed);
					//"Variables x equal to 1: "
					logFile << "Variables x equal to 1: " << endl;
					for (p=0; p<dat.nbProducts; p++)
						for (i=0; i<dat.nbNodes; i++)
							for (j=0; j<dat.nbNodes; j++)
								if(sl.x[i][j][p]>0)
								logFile << "x[" << i << "][" << j << "][" << p << "] = " << sl.x[i][j][p]<< endl;
					for (p=0; p<dat.nbProducts; p++)
						for (i=0; i<dat.nbNodes; i++)
							for (j=0; j<dat.nbNodes; j++)
								if(sl.inter_x[i][j][p]>0)
								logFile << "inter_x[" << i << "][" << j << "][" << p << "]="<<sl.inter_x[i][j][p] << endl;
					logFile.close();
#endif
					double inter_cost=0.0;
					double estimate_d=0.0;
					for(p=0;p<dat.nbProducts;p++)
						for(i=0;i<dat.nbNodes;i++)
							for(j=0;j<dat.nbNodes;j++)
								if(sl.inter_x[i][j][p]>0){
									inter_cost+=dat.alpha[p]*dat.O[i][p]*sl.x[i][j][p]+estimate_d/(sl.count_inter*2);
#ifdef INTER_HUB
									logFile.open(fLog,ios::app);
									logFile.setf(ios::fixed);
									logFile<<"estimate_d"<<estimate_d<<endl;
									logFile.close();
#endif
									estimate_d=0.0;
									for (l=0;l<dat.nbNodes;l++)
										if(sl.inter_x[i][j][p]>0)
											estimate_d=estimate_d+sl.inter_x[i][j][p]*dat.d[j][l];
#ifdef INTER_HUB
									logFile.open(fLog,ios::app);
									logFile.setf(ios::fixed);
									logFile<<"estimate_d"<<estimate_d<<endl;
									logFile<<"inter_cost "<<inter_cost<<endl;
									logFile.close();
#endif
								}
#ifdef INTER_HUB
								logFile.open(fLog,ios::app);
								logFile.setf(ios::fixed);
								logFile<<"nr_inter "<<sl.count_inter<<endl;
								logFile.close();
#endif
                    				itrt.best_cost=itrt.best_cost+inter_cost;
#ifdef USE_CPLEX
									//TIME
#ifdef TIME
									clock_t t_getsol1,t_getsol2;  // to get the elapsed time
									t_getsol1=clock();
#endif
									sol = getSolution(dat,ants[kk], itrt);

#ifdef TIME
									t_getsol2=clock();
									double diff_getsol ((double)t_getsol2-(double)t_getsol1);
									double time_getsol;
									time_getsol = diff_getsol / CLOCKS_PER_SEC;
									cout << "TIME getsol: "<<time_getsol<<endl;
#endif
#ifdef TIME
									clock_t t_runcplex1,t_runcplex2;  // to get the elapsed time
									t_runcplex1=clock();
#endif
									runCplex(dat, sol, itrt.best_cost, fichIn, fichOut);
#ifdef TIME
									t_runcplex2=clock();
									double diff_runcplex ((double)t_runcplex2-(double)t_runcplex1);
									double time_runcplex;
									time_runcplex = diff_runcplex / CLOCKS_PER_SEC;
									cout << "TIME runcplex: "<<time_runcplex<<endl;
#endif
									// save y value TIRAR
									for (i=0; i<dat.nbNodes; i++) 
										for (j=0; j<dat.nbNodes; j++) 
											for (k=0; k<dat.nbNodes; k++) 
												for(p=0; p<dat.nbProducts; p++){ 
													itrt.y_best[i][j][k][p]=sol.y[i][j][k][p];
													if(itrt.best_cost<=better.cost)
														better.y[i][j][k][p]=sol.y[i][j][k][p];
												}

									ofstream file;
									file.open (bestLog,ios::app);
									file << fixed << showpoint;
									file<<setprecision(2)<<itrt.best_cost<<",";
									file.close();

									// TIRAR
									delete [] sol.z;
									for (i=0; i<dat.nbNodes; ++i)
										for (j=0; j<dat.nbNodes; ++j)
											delete [] sol.x[i][j];
									for (i=0; i<dat.nbNodes; ++i)
										delete [] sol.x[i];
									delete [] sol.x;

									for (i=0; i<dat.nbNodes; ++i)
										for (j=0; j<dat.nbNodes; ++j)
											for (k=0; k<dat.nbNodes; ++k)
												delete [] sol.y[i][j][k];
									for (i=0; i<dat.nbNodes; ++i)
										for (j=0; j<dat.nbNodes; ++j)
											delete [] sol.y[i][j];
									for (i=0; i<dat.nbNodes; ++i)
										delete [] sol.y[i];
									delete [] sol.y;
#endif
                    ////////// GLOBAL PHEROMONE UPDATING
					
					// global pheromone update
//NEW
#ifdef UPDATE_BEST
					if(it>1){
						if(itrt.best_cost<global_best+global_best*UPDATE_PARAM)
						//if(itrt.best_cost<iter[it-1].best_cost)
							GlobalPheromoneUpdate(dat,a,itrt,scal_param);
					}
					else{
						GlobalPheromoneUpdate(dat,a,itrt,scal_param);
					}
#endif
#ifndef UPDATE_BEST
					GlobalPheromoneUpdate(dat,a,itrt,scal_param);
#endif
					if(itrt.best_cost>=global_best){
						best_count++;
					}else{
						best_count=0;
					}
				/*	if (it>1)
						if(itrt.best_cost<iter[it-1].best_cost)
					if(best_count>100)
								best_count=best_count-10;*/
					if(best_count>MAX_NO_BEST)
						it_stop=1;

					cout << "best_count = " << best_count << endl;

					time_t t;
					t=clock();
					double time_diff ((double)t-(double)t1);
					double time;
					time = time_diff / CLOCKS_PER_SEC;
				
					if(time>MAX_TIME){
						it_stop=1;
						cout << "time = " << time << endl;
					}

#ifdef TIME
					t_globalpher2=clock();
					double diff_globalpher ((double)t_globalpher2-(double)t_globalpher1);
					double time_globalpher;
					time_globalpher = diff_globalpher / CLOCKS_PER_SEC;
					cout << "TIME global pheromone update: "<<time_globalpher<<endl;
#endif

#ifndef USE_CPLEX
					ofstream file;
					file.open (bestLog,ios::app);
					file << fixed << showpoint;
					file<<setprecision(2)<<itrt.best_cost<<",";
					file.close();
#endif
					nr_dead=0;
				} //if(ants[kk].life>0)
				else{
				/// count dead ants in a row
				nr_dead++;
#ifdef UPDATE_DEAD
					if(nr_dead>MAX_DEAD){
						GlobalDeadPheromoneUpdate(dat,a,ants[kk] ,scal_param, global_best);
					}
#endif
				}

				GetBestCost(dat, itrt,better, it, global_best, t1);

				// EACH ITER TIME
#ifdef TIME
				t_eachiter2=clock();
				double diff_eachiter ((double)t_eachiter2-(double)t_eachiter1);
				double time_eachiter;
				time_eachiter = diff_eachiter / CLOCKS_PER_SEC;
				cout <<endl<< "TIME EACH ITER: "<<time_eachiter<<endl;
#endif
				//}// for(it=0;it<NR_ITER;it++) 
				it++;
				if(it>=NR_ITER)
					it_stop=1;
			} while(it_stop<1);
			// ALL ITER TIME
#ifdef TIME
			t_alliter2=clock();
			double diff_alliter ((double)t_alliter2-(double)t_alliter1);
			double time_alliter;
			time_alliter = diff_alliter / CLOCKS_PER_SEC;
			cout << endl<< "TIME ALL ITER TIME: "<<time_alliter<<endl;
#endif
			// get best iteration 
			int it_best=better.nr_iter; 
#ifdef HISTORY
			myfile.open ("history1.txt",ios::app);
			myfile<<endl<<"BEST ITERATION :"<<it_best<<" COST = "<<iter[it_best].best_cost ;
			myfile.close();
#endif
#ifndef USE_CPLEX
			//TIME
#ifdef TIME
			clock_t t_getsol1,t_getsol2;  // to get the elapsed time
			t_getsol1=clock();
#endif
			if(global_best<max_cost){
				sol = getIterSolution(dat, better);
				cout << "iter["<<it_best<<"].best_cost = "<<better.cost<<endl<<endl;
				cout << "better.cost = "<<better.cost<<endl<<endl;
			}else{
				cout << "NO SOLUTION FOUND" << endl;
			}
			cout << "before cplex" << endl;
			
#ifdef TIME
			t_getsol2=clock();
			double diff_getsol ((double)t_getsol2-(double)t_getsol1);
			double time_getsol;
			time_getsol = diff_getsol / CLOCKS_PER_SEC;
			cout << "TIME getsol: "<<time_getsol<<endl;
#endif
#ifdef TIME
			clock_t t1_runcplex1,t1_runcplex2;  // to get the elapsed time
			t1_runcplex1=clock();
#endif
			if(global_best<max_cost){
				runCplex(dat, sol, better.cost, fichIn, fichOut);
				
				// save y value
				for (i=0; i<dat.nbNodes; i++) 
					for (j=0; j<dat.nbNodes; j++) 
						for (k=0; k<dat.nbNodes; k++) 
							for(p=0; p<dat.nbProducts; p++)
								better.y[i][j][k][p]=sol.y[i][j][k][p];

				delete [] sol.z;
				for (i=0; i<dat.nbNodes; ++i)
					for (j=0; j<dat.nbNodes; ++j)
						delete [] sol.x[i][j];
				for (i=0; i<dat.nbNodes; ++i)
					delete [] sol.x[i];
				delete [] sol.x;

				for (i=0; i<dat.nbNodes; ++i)
					for (j=0; j<dat.nbNodes; ++j)
						for (k=0; k<dat.nbNodes; ++k)
							delete [] sol.y[i][j][k];
				for (i=0; i<dat.nbNodes; ++i)
					for (j=0; j<dat.nbNodes; ++j)
						delete [] sol.y[i][j];
				for (i=0; i<dat.nbNodes; ++i)
					delete [] sol.y[i];
				delete [] sol.y;

			}else{
				cout << "NO SOLUTION FOUND" << endl;
			}
			
#ifdef TIME
			t1_runcplex2=clock();
			double diff1_runcplex ((double)t1_runcplex2-(double)t1_runcplex1);
			double time1_runcplex;
			time1_runcplex = diff1_runcplex / CLOCKS_PER_SEC;
			cout << "TIME runcplex: "<<time1_runcplex<<endl;
#endif
#endif

			cout << "after cplex" << endl;
			if(global_best<max_cost){
				cout << "iter["<<it_best<<"].best_cost = "<<better.cost<<endl<<endl;
				cout << "better.cost = "<<better.cost<<endl<<endl;
				cout << "global_best = "<<global_best<<endl<<endl;
			}else{
				cout << "NO SOLUTION FOUND" << endl;
			}
			t2=clock();
			double diff ((double)t2-(double)t1);
			double seconds = diff / CLOCKS_PER_SEC;
			cout<< "\nTotal Elapsed time: "<<seconds<<" CPU seconds."<<endl;
			if(seconds>60)
				cout<< "Total Elapsed time: "<<seconds/60<<" CPU minutes."<<endl;
			if(global_best<max_cost){
			cout << "iter["<<it_best<<"].best_cost = "<<better.cost<<endl<<endl;
			cout << "better.cost"<<better.cost<<endl<<endl;
			
			gap gp = computeGap(gapIn,fichIn,better.cost);
			cout << "gp.inst_name" << gp.inst_name << endl;
			cout << " gp.cost" << gp.cost << endl;
			cout << " gp.value" << gp.value << endl;
			cout << " gp.time" << gp.time << endl;

			OutputFile(NR_ITER, it_best, fichOut, gp.cost , gp.time, better.cost, better.time, seconds, gp.value); 
			}else{
				cout << "NO SOLUTION FOUND" << endl;
			}
			// write log file
			// Log file if LOG is defined
#ifdef LOG
			const char* fLog = "output.log";
			ofstream logFile(fLog,ios::app);
			logFile.setf(ios::fixed);
			logFile << setw(12) << fichIn << endl;
			logFile << "MODEL - ACO" << endl;
			logFile << "Optimal value : ";
			if(global_best<max_cost){
				logFile << setw(7) << setprecision(2) << better.cost << endl;
			}else{
				logFile << setw(7) << setprecision(2) << "NO SOLUTION FOUND" << endl;
			}
			logFile << "CPU           : ";
			logFile << setw(7) << setprecision(2) << seconds << " seconds" << endl;
			logFile << endl;
			logFile << "Best Iteration: " << it_best << endl;

			// "Variables z equal to 1: "
			if(global_best<max_cost){
				logFile << "Variables z equal to 1: " << endl;
				for (k=0; k<dat.nbNodes; k++)
					if (better.z[k] == 1)
						logFile << "z[" << k << "]=" << better.z[k] << endl;
				logFile << endl;

				//"Variables x equal to 1: "
				logFile << "Variables x equal to 1: " << endl;
				for (p=0; p<dat.nbProducts; p++)
					for (i=0; i<dat.nbNodes; i++)
						for (j=0; j<dat.nbNodes; j++)
							if(better.x[i][p]==j)
								logFile << "x[" << i << "][" << j << "][" << p << "]" << endl;

				logFile << "Variables y: " << endl;
				for (p=0; p<dat.nbProducts; p++) 
					for (i=0; i<dat.nbNodes; i++)
						for (j=0; j<dat.nbNodes; j++)
							for (k=0; k<dat.nbNodes; k++)
								if (better.y[i][j][k][p]  > 0.000001)
									logFile << "y[" << i << "][" << j << "][" << k << "][" << p << "] = " << better.y[i][j][k][p] << endl;
			}
			else{
				cout << "NO SOLUTION FOUND" << endl;
			}
			logFile << endl;
			logFile.close();

#endif
			cout << "FREEING MEMORY"<<endl;
			// Free memory
		
			for (i=0; i<dat.nbNodes; ++i){
				delete [] hv.tfc[i];
				delete [] hv.hfc[i];
			}
			delete[] hv.tfc;
			delete[] hv.hfc;

			for (i=0; i<dat.nbNodes; ++i)
				for (j=0; j<dat.nbNodes; ++j)
					delete [] hv.eta[i][j];

			for (i=0; i<dat.nbNodes; ++i)
				delete [] hv.eta[i];

			delete [] hv.eta;
			for (i=0; i<dat.nbNodes; ++i)
				for (j=0; j<dat.nbNodes; ++j)
					delete [] a.tau0[i][j];
			for (i=0; i<dat.nbNodes; ++i)
				delete [] a.tau0[i];
			delete [] a.tau0;

			for (i=0; i<dat.nbNodes; ++i)
				for (j=0; j<dat.nbNodes; ++j)
					delete [] a.tau[i][j];
			for (i=0; i<dat.nbNodes; ++i)
				delete [] a.tau[i];
			delete [] a.tau;

			for(k=0;k<NR_ANTS;k++){
				for (i=0; i<dat.nbNodes; ++i)
					for (j=0; j<dat.nbNodes; ++j)
						delete[] ants[k].avail_tau[i][j];
				for (i=0; i<dat.nbNodes; ++i)
					delete [] ants[k].avail_tau[i];
					delete [] ants[k].avail_tau;

				for (i=0; i<dat.nbNodes; ++i)
					delete [] ants[k].avail_cap[i];

				delete [] ants[k].avail_cap;

				for (i=0; i<dat.nbNodes; ++i)
					for (int j=0; j<dat.nbNodes; ++j)
						delete [] ants[k].prob[i][j];
				for (i=0; i<dat.nbNodes; ++i)
					delete [] ants[k].prob[i];
				delete [] ants[k].prob;

				for (i=0; i<dat.nbNodes; ++i)
					delete [] ants[k].x[i];
				delete [] ants[k].x;

				delete [] ants[k].z;
			}

			
			for (i=0; i<dat.nbNodes; ++i){
				delete [] itrt.x_best[i];
			}
			delete[] itrt.x_best;
			
			for (i=0; i<dat.nbNodes; ++i){
				for (j=0; j<dat.nbNodes; ++j){
					for (k=0; k<dat.nbNodes; ++k){
						delete [] itrt.y_best[i][j][k];
					}
				}
			}
			for (i=0; i<dat.nbNodes; ++i){
				for (j=0; j<dat.nbNodes; ++j){
					delete [] itrt.y_best[i][j];
				}
			}
			for (i=0; i<dat.nbNodes; ++i){
				delete [] itrt.y_best[i];
			}
			delete [] itrt.y_best;
			

			//////AAA////////
			delete [] better.z;

			for (i=0; i<dat.nbNodes; ++i){
				delete [] better.x[i];
			}
			delete[] better.x;

			for (i=0; i<dat.nbNodes; ++i){
				for (j=0; j<dat.nbNodes; ++j){
					for (k=0; k<dat.nbNodes; ++k){
						delete [] better.y[i][j][k];
					}
				}
			}
			for (i=0; i<dat.nbNodes; ++i){
				for (j=0; j<dat.nbNodes; ++j){
					delete [] better.y[i][j];
				}
			}
			for (i=0; i<dat.nbNodes; ++i){
				delete [] better.y[i];
			}
			delete [] better.y;
			//////AAA////////
			for (i=0; i<dat.nbNodes; ++i)
				for (j=0; j<dat.nbNodes; ++j)
					delete [] pre.allow [i][j];
			for (i=0; i<dat.nbNodes; ++i)
				delete [] pre.allow [i];
			delete [] pre.allow;

			for (i=0; i<dat.nbNodes; ++i)
				for (j=0; j<dat.nbNodes; ++j)
					delete[] dat.w[i][j];
			for (i=0; i<dat.nbNodes; ++i)
				delete[] dat.w[i];
			delete [] dat.w;

			delete [] dat.chi;

			delete [] dat.alpha;

			delete [] dat.delta;

			for (i=0; i<dat.nbNodes; ++i)
				delete [] dat.d[i];
			delete [] dat.d;

			delete[] dat.g;

			delete[] dat.L;

			for (i=0; i<dat.nbNodes; ++i)
				delete [] dat.f[i];
			delete[] dat.f;

			for (i=0; i<dat.nbNodes; ++i)
				delete [] dat.Gamma[i];
			delete[] dat.Gamma;

			for (i=0; i<dat.nbNodes; ++i)
				delete [] dat.O[i];
			delete[] dat.O;

			for (i=0; i<dat.nbNodes; ++i)
				delete [] dat.D[i];
			delete dat.D;

			delete [] ants;

			cout << "END FREEING MEMORY"<<endl;

			return 0;
}
