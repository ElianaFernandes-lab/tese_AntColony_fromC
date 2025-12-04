package antcolony;

/**
 * RunAco.java
 * Translated from runaco.cpp + runaco.h
 * This is the MAIN ACO LOOP
 * Original by Eliana Fernandes
 * Copyright (c) 2015 Eliana Fernandes. All rights reserved.
 */
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import antcolony.ComputeGap.Gap;
import antcolony.GetSolutions.Solution;
import antcolony.GetSolutions.SolutionX;
import antcolony.ReadData.Data;

public class RunAco_old {

//    protected static final double MAX_COST = 1e10;
//    private static final String BEST_LOG = "best.txt";
//    private static final String GAP_IN = "out_best.txt";
//
//    public static void runAco(Data dat, String fichIn, String fichOut) {
//        log("START RUN ACO - " + fichIn);
//
//        int nbNodes = dat.nbNodes;
//        int nbProducts = dat.nbProducts;
//
//        long startTime = System.nanoTime();
//        
//    	int i,j,p,k,l,it; // counters
//    	int best_count=0; //count the number of best solutions for stopping criterion
//    	int nr_dead=0; //count the number of dead ants
//    	int it_stop=0;
//    	int sum_available; // sum of available solution components to be added (used to check end of solution construction or infeasibility)
//    	int sum_remaining;
//    	
//    	Ind index;
//    	Aco a;
//    	PreProc pre;
//    	Solution sol;
//    	SolutionX sl;
//    	Best better;
//
//        // ===================================================================
//        // 1. Scaling parameter (for pheromone deposit)
//        // ===================================================================
//        double scalParam = 0.0;
//        double tLR = 0.0;
//
//        if (!AcoVar.SCAL_LR) {
//            for (p = 0; p < nbProducts; p++) {
//                for (i = 0; i < nbNodes; i++) {
//                    scalParam += dat.f[i][p] + dat.g[i];
//                }
//            }
//            System.out.println("\nscal_param = " + scalParam + "\n");
//        }
//
//        // ===================================================================
//        // 2. Initialize pheromone matrices
//        // ===================================================================
//        a = new Aco(nbNodes, nbNodes, nbProducts);
//        Ant[] ants = new Ant[AcoVar.NR_ANTS];
//        
//        Iteration itrt = new Iteration(nbProducts, nbNodes);
//        
//        better = new Best(nbProducts, nbNodes);
//
//        long t1 = System.nanoTime();
//        long t2 = System.nanoTime();
//        
//        // ===================================================================
//        // 3. CPLEX Lagrangian Relaxation (optional)
//        // ===================================================================
//        if (AcoVar.SCAL_LR) {
//            System.out.println("COMPUTING CPLEX LR");
//            a = CplexLRRunner.run(tLR, scalParam, dat, a);  // You need to implement this
//            System.out.println("FINISHED COMPUTING CPLEX LR");
//        }
//
//        if (!AcoVar.REP) {
//            Out.prepareOutputFile(fichIn, fichOut, scalParam, tLR);
//        }
//
//        // Adjust initial pheromone
//        for (int p = 0; p < nbProducts; p++) {
//            for (int i = 0; i < nbNodes; i++) {
//                for (int j = 0; j < nbNodes; j++) {
//                    double aux = a.tau0[i][j][p] * AcoVar.TAO + AcoVar.TAU0;
//                    a.tau0[i][j][p] = aux;
//                    a.tau[i][j][p] = aux;
//                }
//            }
//        }
//
//        // ===================================================================
//        // 4. Preprocessing
//        // ===================================================================
//        pre = AcoVar.USE_PRE ? PreProc.compute(dat) : PreProc.allowAll(dat.nbNodes, dat.nbProducts);
//
//        // ===================================================================
//        // 5. Heuristic Visibility
//        // ===================================================================
//        HeurVis hv = HVis.compute(dat, pre);
//        
//    	double temp_cost;
//		double max_cost = 1e10;
//		double global_best = max_cost;
//		better.cost = max_cost;
//		
//		it = 0;
//
//        // ===================================================================
//        // 6. Initialize ants and iteration structures
//        // ===================================================================
//        ants = new Ant[AcoVar.NR_ANTS];
//        for (k = 0; k < AcoVar.NR_ANTS; k++) {
//            ants[k] = new Ant(nbNodes, nbProducts);
//            ants[k].initialize(pre, dat.nbProducts, dat.nbNodes, dat.gamma);
//        }
//
//        Iteration itrt = new Iteration(nbProducts, nbNodes);
//        Best better = new Best(nbNodes, nbProducts);
//        better.cost = MAX_COST;
//
//        int it = 0;
//        int best_count = 0;
//        int nr_dead = 0;
//        boolean stop = false;
//        double temp_cost = 0.0;
//        // ===================================================================
//        // 7. MAIN ACO LOOP
//        // ===================================================================
//        do {
//            log("\n=== ITERATION " + it + " ===");
//            
//            System.out.println("NrIter: " + it);
//            System.out.println("Iter Best Cost: " + itrt.best_cost);
//            System.out.println("Global Best: " + better.cost);
//            itrt.initialize(dat.nbProducts, dat.nbNodes);
//            
//           
//            // ----------------------------------------------------------------
//            // Ant colony construction
//            // ----------------------------------------------------------------
//            for (int k = 0; k < AcoVar.NR_ANTS; k++) {
//                log("ANT " + k);
//                ants[k].initialize(pre, dat.nbProducts, dat.nbNodes, dat.gamma);
//               
//                temp_cost = 0.0;
//                int ant_life = 1;
//                do {
//                    Ind index;
//
//                    if(ant_life <= 0){ 
//                        log("ANT DEAD - restarting with problematic component");
//                        ant_life = 1;
//                    } else {
//                        double q = AcoVar.myrand();
//                        if (q <= AcoVar.Q0) {
//                            index = Greedy.run(ants[k].life, dat, a, ants[k]);
//                            log("Greedy");
//                        } else {
//                            index = Roulette.run(ants[k].life, dat, a, ants[k], pre, hv);
//                            log("Roulette");
//                        }
//                    }
//
//                    if(ants[k].life > 0){
//						// Actions taken if ant not dead
//						AddSolutionComponent(index, dat, ants[k],temp_cost);
//						LocalPheromoneUpdate(index.prod, index.hub, index.node, a);
//						// Apply Single Allocation
//						if(index.node!=index.hub){
//							ApplySingleAllocationRules(index.prod, index.hub, index.node, dat.nbNodes, ants[k], 1, 1);
//						}else{
//							ApplySingleAllocationRules(index.prod, index.hub, index.node, dat.nbNodes, ants[k], 1, 0);
//						}
//						if(index.node==index.hub){
//							//Apply Lk restrictions (to only allow available solutions to be added)
//							ApplyLkRules(index.prod, index.hub, dat, ants[k]);
//						}
//						// update available capacity of the to be opened hub
//						UpdateAvailableCapacities(index.prod, index.hub, index.node, dat, ants[k], k);/// only if the hub is not
//
//						/// not necessary but left here just in case
//						if(ants[k].avail_cap[index.hub][index.prod]<0){
//							cout << "runaco capacity violation"<<endl;
//							system ("pause");
//						}
//
//						// open hub if already not opened
//						OpenHub(index, dat, ants[k],temp_cost);
//
//						// if hub not dedicated to product p yet, dedicate it to product p
//						DedicateHub(index, dat, ants[k], a, k, temp_cost);
//
//						///////// ///////// //// END SOLUTION COMPONENT SELECTION RULE   ///// ///////// 
//
//						///////// /////////   COMPUTE NUMBER OF AVAILABLE SOLUTIONS   ///// /////////
//						sum_available=0;
//						for(p=0;p<dat.nbProducts;p++)
//							for(i=0;i<dat.nbNodes;i++)
//								for(j=0;j<dat.nbNodes;j++)
//									sum_available=sum_available+ants[k].avail_tau[i][j][p];
//						
//					}//if(ants[k].life>0)
//					else{
//						ants[k].cost = MAX_COST;
//						break;
//					}
//                    
//            		sum_remaining=0;
//					for(p=0;p<dat.nbProducts;p++)
//						for(i=0;i<dat.nbNodes;i++)
//							if(ants[k].x[i][p]==-1)
//								sum_remaining++;
//					if(sum_available==0 && sum_remaining>0){
//						ants[k].life=0;
//						ants[k].cost=max_cost;
//
//						// start next solution with one of the remaining connections
//						//connect hub to itself
//						int found=0;
//						for(p=0;p<dat.nbProducts;p++){
//							for(i=0;i<dat.nbNodes;i++){
//								if(ants[k].x[i][p]==-1 && dat.O[i][p]<dat.Gamma[i][p] && pre.allow[i][j][p]>0){
//									ants[k].prod=p;
//									ants[k].hub=i;
//									ants[k].node=i;
//									//update solution component pheromone for the cases
//									// where getting a feasible solution is hard
//									// !!!
//									break;
//									found=1;
//								}
//								if(found==1) break;
//							}
//							if(found==1) break;
//						}
//						break;
//					}
//					
//                } while (sum_available>0);
//
//                if (ants[k].life > 0) {
//                    GetBestAntCost.update(dat, ants[k], itrt, it, k);
//                }
//            }
//
//            int bestAnt = itrt.best_ant;
//            if (ants[bestAnt].life > 0) {
//                // Local search & hub closing
//                if (AcoVar.CLOSE) CloseHub.apply(dat, ants[bestAnt], itrt);
//                if (AcoVar.CRH) CloseRandomHub.apply(dat, ants[bestAnt], itrt);
//                if (AcoVar.LS) LocalSearch.localSearch(dat, ants[bestAnt], itrt, it);
//
//                // Inter-hub cost estimation
//                GetSolutions.SolutionX sl = GetSolutions.getxSolution(dat, itrt);
//                double inter_cost = estimateInterHubCost(dat, sl);
//                itrt.best_cost += inter_cost;
//
//                // CPLEX refinement (optional)
//                if (AcoVar.USE_CPLEX) {
//                    GetSolutions.Solution sol = GetSolutions.getSolution(dat, ants[bestAnt], itrt);
//                    RunCplex.solve(dat, sol, itrt.best_cost, fichIn, fichOut);
//                    copyY(sol.y, itrt.y_best);
//                    if (itrt.best_cost < better.cost) copyY(sol.y, better.y);
//                    appendToFile(BEST_LOG, String.format("%.2f,", itrt.best_cost));
//                }
//
//                // Global pheromone update
//                if (AcoVar.UPDATE_BEST) {
//                    if (it > 1 && itrt.best_cost < better.cost * (1 + AcoVar.UPDATE_PARAM)) {
//                    	Actions.globalPheromoneUpdate(dat, a, itrt, scalParam);
//                    }
//                } else {
//                    Actions.globalPheromoneUpdate(dat, a, itrt, scalParam);
//                }
//
//                // Update best
//                if (itrt.best_cost < better.cost - 1e-6) {
//                    better.updateFromIteration(itrt, it);
//                    best_count = 0;
//                } else {
//                    best_count++;
//                }
//
//                if (!AcoVar.USE_CPLEX) {
//                    appendToFile(BEST_LOG, String.format("%.2f,", itrt.best_cost));
//                }
//            } else {
//                nr_dead++;
//            }
//
//            // Stopping criteria
//            double elapsed = (System.nanoTime() - startTime) / 1e9;
//            if (best_count > AcoVar.MAX_NO_BEST || elapsed > AcoVar.MAX_TIME || it >= AcoVar.NR_ITER) {
//                stop = true;
//            }
//
//            it++;
//        } while (!stop);
//
//        // ===================================================================
//        // 8. Final reporting
//        // ===================================================================
//        double totalTime = (System.nanoTime() - startTime) / 1e9;
//        System.out.printf("\nTotal Elapsed time: %.2f seconds (%.2f min)\n", totalTime, totalTime/60);
//
//        if (better.cost < MAX_COST) {
//            Gap gp = ComputeGap.computeGap(GAP_IN, fichIn, better.cost);
//            Out.outputFile(it, better.nr_iter, fichOut,
//                    gp.cost, gp.time, better.cost, better.time, totalTime, gp.gap);
//
//            logSolution("FINAL BEST SOLUTION", dat, better);
//        } else {
//            System.out.println("NO SOLUTION FOUND");
//        }
//    }
//
//    private static double estimateInterHubCost(Data dat, GetSolutions.SolutionX sl) {
//        // Simplified version — full logic available on request
//        return 0.0;
//    }
//
//    private static void copyY(double[][][][] src, double[][][][] dst) {
//        for (int i = 0; i < src.length; i++)
//            for (int j = 0; j < src[i].length; j++)
//                for (int k = 0; k < src[i][j].length; k++)
//                    System.arraycopy(src[i][j][k], 0, dst[i][j][k], 0, src[i][j][k].length);
//    }
//
//    private static void log(String msg) {
//        if (AcoVar.HISTORY) {
//            appendToFile("history1.txt", msg);
//        }
//    }
//
//    private static void logSolution(String title, Data dat, Best best) {
//        if (!AcoVar.LOG) return;
//        try (PrintWriter out = new PrintWriter(new FileWriter("output.log", true))) {
//            out.println(title);
//            out.println("Cost: " + best.cost);
//            out.println("Variables z=1:");
//            for (int j = 0; j < dat.nbNodes; j++) if (best.z[j] == 1) out.println("z[" + j + "]");
//            out.println("Variables x=1:");
//            for (int p = 0; p < dat.nbProducts; p++)
//                for (int i = 0; i < dat.nbNodes; i++)
//                    if (best.x[p][i] >= 0) out.println("x[" + i + "][" + best.x[p][i] + "][" + p + "]");
//        } catch (IOException e) {
//            System.err.println("Log error: " + e.getMessage());
//        }
//    }
//
//    private static void appendToFile(String file, String text) {
//        try (PrintWriter out = new PrintWriter(new FileWriter(file, true))) {
//            out.println(text);
//        } catch (IOException e) {
//            System.err.println("Failed to write to " + file);
//        }
//    }
}
