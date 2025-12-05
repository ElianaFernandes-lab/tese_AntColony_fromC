package antcolony;

import antcolony.GetSolutions.Solution;
import antcolony.GetSolutions.SolutionX;
import antcolony.ReadData.Data;
import antcolony.constants.AcoVar;
import antcolony.ortools.HiGHSLR;
import antcolony.ortools.HiGHSMILP;

public class RunAco {

	protected static final double MAX_COST = 1e10;
	private static final String BEST_LOG = "best.txt";
	private static final String GAP_IN = "out_best.txt";

	public static void runAco(Data dat, String fichIn, String fichOut) {
		System.out.println("START RUN ACO - " + fichIn);

		int nbNodes = dat.nbNodes;
		int nbProducts = dat.nbProducts;

		long startTime = System.currentTimeMillis();

		int i,j,p,k,l,it; // counters
		int best_count=0; //count the number of best solutions for stopping criterion
		int nr_dead=0; //count the number of dead ants
		int it_stop = 0;
		int sum_available; // sum of available solution components to be added (used to check end of solution construction or infeasibility)
		int sum_remaining;

		Ind index = null;
		Aco a;
		PreProc pre;
		Solution sol = null;
		SolutionX sl;
		Best better;

		// ===================================================================
		// 1. Scaling parameter (for pheromone deposit)
		// ===================================================================
		double scalParam = 0.0;
		double tLR = 0.0;

		if (!AcoVar.SCAL_LR) {
			for (p = 0; p < nbProducts; p++) {
				for (i = 0; i < nbNodes; i++) {
					scalParam += dat.f[i][p] + dat.g[i];
				}
			}
			System.out.println("\nscal_param = " + scalParam + "\n");
		}

		// ===================================================================
		// 2. Initialize pheromone matrices
		// ===================================================================
		a = new Aco(nbNodes, nbNodes, nbProducts);
		Ant[] ants = new Ant[AcoVar.NR_ANTS];

		Iteration itrt = new Iteration(nbProducts, nbNodes);

		better = new Best(nbProducts, nbNodes);

		long t1 = System.currentTimeMillis();
		long t2 = System.currentTimeMillis();

		// ===================================================================
		// 3. CPLEX Lagrangian Relaxation (optional)
		// ===================================================================
		if (AcoVar.SCAL_LR) {
			System.out.println("COMPUTING CPLEX LR");
			try {
				a = HiGHSLR.run(tLR, scalParam, dat, a);
			} catch (Exception e) {
				System.err.println("Error while running HiGHS LR:");
				e.printStackTrace();
			}  // You need to implement this
			System.out.println("FINISHED COMPUTING HiGHS LR");
		}

		if (!AcoVar.REP) {
			Out.prepareOutputFile(fichIn, fichOut, scalParam, tLR);
		}

		// Adjust initial pheromone
		for (p = 0; p < nbProducts; p++) {
			for (i = 0; i < nbNodes; i++) {
				for (j = 0; j < nbNodes; j++) {
					double aux = a.tau0[i][j][p] * AcoVar.TAO + AcoVar.TAU0;
					a.tau0[i][j][p] = aux;
					a.tau[i][j][p] = aux;
				}
			}
		}

		// ===================================================================
		// 4. Preprocessing
		// ===================================================================
		pre = new PreProc(dat.nbProducts, dat.nbNodes);
		if(AcoVar.USE_PRE) {
			pre.compute(dat);
		}

		///////// ///////// ///////// //////     HEURISTIC VISIBILITY        ///////// ///////// ///////// /////////
		HeurVis hv = new HeurVis(dat.nbNodes, dat.nbProducts);
		///////// ///////// ///////// //////       END HEURISTIC VISIBILITY         ///////// ///////// ///////// /////////

		///////// ///////// ///////// ///////// ///////// ///////// ///////// ///////// ///////// ///////// /////////
		///////// ///////// ///////// /////////               ACO            ///////// ///////// ///////// /////////
		///////// ///////// ///////// ///////// ///////// ///////// ///////// ///////// ///////// ///////// /////////
		double temp_cost;
		double max_cost=1e10;
		double global_best= max_cost;
		better.cost= max_cost;

		it=0;
		//for(it=0;it<NR_ITER;it++){
		do{
			//// reset iteration values
			itrt.best_cost=max_cost;
			// TIME
			// EACH ITER
			///////// ///////// ///////// //////    iteration  INITIALIZATIONS        ///////// ///////// ///////// /////////
			// reset cost
			temp_cost=0.0;

			// reset ant values
			// ALIVE ANTS ARE THE ONES THAT PRODUCE FEASIBLE SOLUTIONS (ant_k.life=1)
			for(k = 0 ;k<AcoVar.NR_ANTS;k++){
				ants[k] = new Ant(dat.nbProducts, dat.nbNodes);
				ants[k].initialize(pre, dat.nbProducts, dat.nbNodes, dat.gamma);
				ants[k].life=1;
				ants[k].cost=0.0; // the cost of ant solution is intialized to zero
			}

			

			// SOLUTION BEST COMPONENTS (x(i,j,iter) ----->  initialized to -1 because it can take value 0)
			for(p = 0;p < dat.nbProducts;p++)
				for(i = 0;i<dat.nbNodes;i++)
					itrt.x_best[p][i]=-1;
			//z_values
			for(j=0;j<dat.nbNodes;j++){
				itrt.z_best[j]=0;
			}

			// OPENED HUBS (z(j) ----->  initialized to 0)
			for(k = 0 ;k<AcoVar.NR_ANTS;k++)
				for(j=0;j<dat.nbNodes;j++)
					ants[k].z[j]=0;

			// AVAILABLE CAPACITY AT EACH POTENTIAL HUB NODE j FOR PRODUCT p and EACH SOLUTION COMPONENT OF ANT k
			// (avail_cap(j,p,k) ---> initialized to capacity of each node)
			for(k = 0 ;k<AcoVar.NR_ANTS;k++)
				for(p = 0;p < dat.nbProducts;p++)
					for(j=0;j<dat.nbNodes;j++)
						ants[k].avail_cap[j][p]=dat.gamma[j][p];

			// compute pre-processing results
			// AVAILABLE SOLUTIONS (x(i,j,p) ----->  initialized to 1 or 0, depending on pre-processing)
			for(k = 0 ;k<AcoVar.NR_ANTS;k++)
				for(p = 0;p < dat.nbProducts;p++)
					for(i = 0;i<dat.nbNodes;i++)
						for(j=0;j<dat.nbNodes;j++)
							ants[k].avail_tau[i][j][p]=pre.allow[i][j][p];
			//for(k = 0 ;k<AcoVar.NR_ANTS;k++) È igual para todas nesta fase
			// count nr of available solutions ant total number of silutions
			int tot_sol=0;
			int av_sol=0;
			for(p = 0;p < dat.nbProducts;p++)
				for(i = 0;i<dat.nbNodes;i++)
					for(j=0;j<dat.nbNodes;j++){
						if(ants[0].avail_tau[i][j][p]>0){
							av_sol++;
						}
						tot_sol++;
					}

			///////// ///////// ///////// //////    END iteration INITIALIZATIONS     ///////// ///////// ///////// /////////

			///////// ///////// ///////// //////              COLONY LOOP               ///////// ///////// ///////// /////////
			// TIME
			// ALL ANTS
			for(k = 0 ;k<AcoVar.NR_ANTS;k++){
				// TIME
				// EACH ANT
				temp_cost=0.0;

				///////// ///////// ///////// //////          BUILD SOLUTION          ///////// ///////// ///////// /////////
				int ant_life=1;
				//if(k>0 && ants[k-1].life<=0){
				//	ant_life=0;
				//	index.prod=ants[k-1].prod;//!!!
				//	index.node=ants[k-1].node;
				//	index.hub=ants[k-1].hub;
				//}//if(k>0 && ants[k-1].life<=0)
				do{
					///////// ///////// //////   SOLUTION COMPONENT SELECTION RULE   ///// ///////// /////////
					// INIT
					if(ant_life<=0){ 
						// if the previous ant is dead, start construction of solution with the
						// solution component that causes infeasibility
						ant_life=1;
					} else {// if(ant_life<=0)
						// PSEUDO-RANDOM PROPORTIONAL RULE
						double q = AcoVar.myrand();
						if(q <= AcoVar.Q0){
							index=Greedy.run(ants[k].life,dat,a,ants[k]);
						} else {
							index=Roulette.run(ants[k].life,dat,a,ants[k],pre,hv);
						}
						// END OF PSEUDO-RANDOM PROPORTIONAL RULE
					} //else // if(ant_life<=0)

					if(ants[k].life>0){
						// Actions taken if ant not dead
						Actions.addSolutionComponent(index, dat, ants[k],temp_cost);
						Actions.localPheromoneUpdate(index.prod, index.hub, index.node, a);
						// Apply Single Allocation
						if(index.node!=index.hub){
							Actions.applySingleAllocationRules(index.prod, index.hub, index.node, dat.nbNodes, ants[k], 1, 1);
						} else {
							Actions.applySingleAllocationRules(index.prod, index.hub, index.node, dat.nbNodes, ants[k], 1, 0);
						}
						if(index.node==index.hub){
							//Apply Lk restrictions (to only allow available solutions to be added)
							Actions.applyLkRules(index.prod, index.hub, dat, ants[k]);
						}
						// update available capacity of the to be opened hub
						Actions.updateAvailableCapacities(index.prod, index.hub, index.node, dat, ants[k], k);/// only if the hub is not

						/// not necessary but left here just in case
						if(ants[k].avail_cap[index.hub][index.prod]<0){
							System.err.println("ERROR");
						}

						// open hub if already not opened
						temp_cost = Actions.openHub(index, dat, ants[k], temp_cost);

						// if hub not dedicated to product p yet, dedicate it to product p
						temp_cost = Actions.dedicateHub(index, dat, ants[k], a, k, temp_cost);

						///////// ///////// //// END SOLUTION COMPONENT SELECTION RULE   ///// ///////// 

						///////// /////////   COMPUTE NUMBER OF AVAILABLE SOLUTIONS   ///// /////////
						sum_available=0;
						for(p = 0;p < dat.nbProducts;p++)
							for(i = 0;i<dat.nbNodes;i++)
								for(j=0;j<dat.nbNodes;j++)
									sum_available=sum_available+ants[k].avail_tau[i][j][p];

					} else {//if(ants[k].life>0)
						ants[k].cost=max_cost;
						break;
					}
					///////// /////////   COMPUTE NUMBER OF REMAINING SOLUTIONS  !!! (para evitar bugs)   ///// /////////
					sum_remaining=0;
					for(p = 0;p < dat.nbProducts;p++)
						for(i = 0;i<dat.nbNodes;i++)
							if(ants[k].x[i][p]==-1)
								sum_remaining++;

					if(sum_available==0 && sum_remaining>0){
						ants[k].life=0;
						ants[k].cost=max_cost;

						// start next solution with one of the remaining connections
						//connect hub to itself
						int found=0;
						for(p = 0;p < dat.nbProducts;p++){
							for(i = 0;i<dat.nbNodes;i++){
								if(ants[k].x[i][p]==-1 && dat.O[i][p]<dat.gamma[i][p] && pre.allow[i][j][p]>0){
									ants[k].prod=p;
									ants[k].hub=i;
									ants[k].node=i;
									//update solution component pheromone for the cases
									// where getting a feasible solution is hard
									// !!!
									found=1;
									break;
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
					Actions.getBestAntCost(dat.nbProducts,dat.nbNodes, ants[k], itrt, it,k);
			}//for(k = 0 ;k<AcoVar.NR_ANTS;k++)
			///////// ///////// ///////// //////            END COLONY LOOP               ///////// ///////// ///////// /////////
			// used for writting simplicity
			int kk=itrt.best_ant; // k is being used in colony's loops, can't be used here
			if(itrt.best_cost==max_cost)
				kk = 0 ;

			if(ants[kk].life>0){

				///////// CLOSE HUB WITH ONE CONNECTION
				if (AcoVar.CLOSE) {
					for(i = 0;i<dat.nbNodes;i++) {
						CloseHub.closeHub(dat, ants[kk], itrt, max_cost);
					}
				}

				///////// CLOSE RANDOM HUB
				if (AcoVar.CRH) {
					for(i = 0;i<dat.nbNodes;i++) {
						CloseRandomHub.closeRandomHub(dat, ants[kk], itrt, max_cost);
					}
				}
				///////// LOCAL SEARCH RANDOM RELOCATE RANDOM NODE
				if (AcoVar.LS) {
					LocalSearch.localSearch(dat, ants[kk], itrt, it);
				}
				///////// LOCAL SEARCH LISTS RELOCATE NODE
				if (AcoVar.LSS) {
					LS.LocalSearch1(dat, ants[kk], itrt, it);
				}

				if (AcoVar.CLOSE) {
					CloseHub.closeHub(dat, ants[kk], itrt, max_cost);
				}
				if (AcoVar.CRH) {
					CloseRandomHub.closeRandomHub(dat, ants[kk], itrt, max_cost);
				}

				// Estimate inter-hub costs
				sl = GetSolutions.getxSolution(dat, itrt);
				double inter_cost=0.0;
				double estimate_d=0.0;
				for (p = 0; p < dat.nbProducts; p++)
					for (i = 0; i<dat.nbNodes; i++)
						for (j=0; j<dat.nbNodes; j++)
							if(sl.inter_x[i][j][p]>0){
								estimate_d=estimate_d+sl.inter_x[i][j][p]*dat.d[i][j];
							}

				for(p = 0;p < dat.nbProducts;p++)
					for(i = 0;i<dat.nbNodes;i++)
						for(j=0;j<dat.nbNodes;j++)
							if(sl.inter_x[i][j][p]>0){
								inter_cost+=dat.alpha[p]*dat.O[i][p]*sl.inter_x[i][j][p]*estimate_d/sl.count_inter;
							}

				itrt.best_cost=itrt.best_cost+inter_cost;
				sol = GetSolutions.getSolution(dat,ants[kk], itrt);

				HiGHSMILP.solve(dat, sol, itrt.best_cost, fichIn, fichOut);
				// save y value TIRAR
				for (i = 0; i<dat.nbNodes; i++) 
					for (j=0; j<dat.nbNodes; j++) 
						for (k = 0 ; k < dat.nbNodes; k++) 
							for(p = 0; p < dat.nbProducts; p++){ 
								itrt.y_best[i][j][k][p]=sol.y[i][j][k][p];
								if(itrt.best_cost<=better.cost)
									better.y[i][j][k][p]=sol.y[i][j][k][p];
							}

				////////// GLOBAL PHEROMONE UPDATING
				// global pheromone update
				//NEW
				if (AcoVar.UPDATE_BEST) {
					if(it>1){
						if(itrt.best_cost<global_best+global_best*AcoVar.UPDATE_PARAM)
							//if(itrt.best_cost<iter[it-1].best_cost)
							Actions.globalPheromoneUpdate(dat,a,itrt,scalParam);
					}
					else{
						Actions.globalPheromoneUpdate(dat,a,itrt,scalParam);
					}
				} else {
					Actions.globalPheromoneUpdate(dat,a,itrt,scalParam);
				}
				if(itrt.best_cost>=global_best){
					best_count++;
				} else {
					best_count=0;
				}
				/*	if (it>1)
						if(itrt.best_cost<iter[it-1].best_cost)
					if(best_count>100)
								best_count=best_count-10;*/
				if(best_count> AcoVar.MAX_NO_BEST)
					it_stop=1;


				if(startTime - System.currentTimeMillis() > AcoVar.MAX_TIME * 1000){
					it_stop=1;
				}

				nr_dead=0;
			} else { //if(ants[kk].life>0)

				/// count dead ants in a row
				nr_dead++;
				if(AcoVar.UPDATE_DEAD){
					if(nr_dead > AcoVar.MAX_DEAD){
						Actions.globalDeadPheromoneUpdate(dat,a,ants[kk] ,scalParam, global_best);
					}
				}
			}

			GetBestCost(dat, itrt, better, it, global_best, t1);

			it++;
			if(it >= AcoVar.NR_ITER)
				it_stop=1;

		} while(it_stop<1);
		// ALL ITER TIME
		// get best iteration 
		int it_best=better.nr_iter; 
		if(global_best<max_cost){
			sol = GetSolutions.getIterSolution(dat, better);
			System.out.println("better.cost = " + better.cost);
		} else {
			System.out.println("NO SOLUTION FOUND");
		}
		System.out.println("before cplex");

		if(global_best<max_cost){
			HiGHSMILP.solve(dat, sol, better.cost, fichIn, fichOut);

			// save y value
			for (i = 0; i<dat.nbNodes; i++) 
				for (j=0; j<dat.nbNodes; j++) 
					for (k = 0 ; k < dat.nbNodes; k++) 
						for(p = 0; p < dat.nbProducts; p++)
							better.y[i][j][k][p]=sol.y[i][j][k][p];


		} else {
			System.out.println("NO SOLUTION FOUND");
		}

		System.out.println("after cplex");
		if(global_best<max_cost){
			StringBuilder sb = new StringBuilder("iter[")
					.append(it_best)
					.append("].best_cost = ")
					.append(better.cost)
					.append("\n")
					.append("better.cost = ")
					.append(better.cost)
					.append("\n")
					.append("global_best = ")
					.append(global_best)
					.append("\n");
			System.out.println(sb.toString());
		} else {
			System.out.println("NO SOLUTION FOUND");
		}

	}

	private static void GetBestCost(Data dat, Iteration itrt, Best better, int it, double globalBest, long t1) {
		if (itrt.best_cost < better.cost) {
			better.cost = itrt.best_cost;
			better.nr_iter = it;
			better.time = (System.currentTimeMillis() - t1) / 1000.0;
		}
	}
}
