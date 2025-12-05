package antcolony.tempfiles;

//AcoRunner.java
//Translated (structural) version of runaco.cpp into Java (Option A: single file).
//NOTE: many helper functions from the original C++ are not available in the paste.
//    They are implemented here as stubs that must be replaced with real implementations.
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Random;

import antcolony.Aco;
import antcolony.Ant;
import antcolony.Best;
import antcolony.ComputeGap.Gap;
import antcolony.GetSolutions.Solution;
import antcolony.GetSolutions.SolutionX;
import antcolony.Greedy;
import antcolony.HeurVis;
import antcolony.Ind;
import antcolony.Iteration;
import antcolony.PreProc;
import antcolony.ReadData.Data;
import antcolony.constants.AcoVar;
import antcolony.Roulette;
import antcolony.UnifRand;

public class RunAco {

 public static int runAco(Data dat, String fichIn, String fichOut) {

     final String bestLog = "best.txt";
     final String gapIn = "out_best.txt";

     // timers
     long t1 = System.currentTimeMillis();

     // seed RNG
     UnifRand.setSeed(System.currentTimeMillis());

     Ind index = new Ind();
     Aco a = new Aco(dat.nbNodes, dat.nbNodes, dat.nbProducts);
     PreProc pre = new PreProc();
     Solution sol = new Solution(dat);
     SolutionX sl = new SolutionX(dat);
     Best better = new Best(dat.nbProducts, dat.nbNodes);

     // get scaling parameter
     double scal_param = 0.0;
     for (int p = 0; p < dat.nbProducts; p++)
         for (int i = 0; i < dat.nbNodes; i++) {
             scal_param += dat.f[i][p] + dat.g[i];
         }
     System.out.println("\n\nscal_param = " + scal_param + "\n");

     // prepare output file (stub)
     prepareOutputFile(fichIn, fichOut, scal_param);

     // ---------------------- MEMORY ALLOCATION ----------------------
     System.out.println("ALLOCATING MEMORY");

     // allocate tau0 and tau as 3D arrays [nbNodes][nbNodes][nbProducts]
     a.tau0 = new double[dat.nbNodes][dat.nbNodes][dat.nbProducts];
     a.tau = new double[dat.nbNodes][dat.nbNodes][dat.nbProducts];

     // allocate ants
     Ant[] ants = new Ant[AcoVar.NR_ANTS];
     for (int k = 0; k < AcoVar.NR_ANTS; k++) {
         ants[k] = new Ant(dat.nbNodes, dat.nbNodes);
     }

     // iteration values
     Iteration itrt = new Iteration(dat.nbProducts, dat.nbNodes);
     itrt.x_best = new int[dat.nbNodes][dat.nbProducts];
     for (int i = 0; i < dat.nbNodes; i++)
         Arrays.fill(itrt.x_best[i], -1);
     // y_best allocate as 5D: [i][j][k][p] -> flatten as [i][j][k][p]
     itrt.y_best = new double[dat.nbNodes][dat.nbNodes][dat.nbNodes][dat.nbProducts];
     // better values
     better.x = new int[dat.nbNodes][dat.nbProducts];
     for (int i = 0; i < dat.nbNodes; i++)
         Arrays.fill(better.x[i], -1);
     better.z = new int[dat.nbNodes];
     better.cost = Integer.MAX_VALUE;

     // Pre processing allow
     pre.allow = new int[dat.nbNodes][dat.nbNodes][dat.nbProducts];
     // default initialize to 1 (available)
     for (int i = 0; i < dat.nbNodes; i++)
         for (int j = 0; j < dat.nbNodes; j++)
             for (int p = 0; p < dat.nbProducts; p++)
                 pre.allow[i][j][p] = 1;

     System.out.println("FINISHED ALLOCATING MEMORY");

     // initialize global pheromone
     for (int p = 0; p < dat.nbProducts; p++)
         for (int i = 0; i < dat.nbNodes; i++)
             for (int j = 0; j < dat.nbNodes; j++) {
                 a.tau0[i][j][p] = AcoVar.TAU0;
                 a.tau[i][j][p] = AcoVar.TAU0;
             }

     // optionally run LR (stub)
     // runCplexLR(dat, a);

     // modify tau0 according to TAO
     for (int p = 0; p < dat.nbProducts; p++)
         for (int i = 0; i < dat.nbNodes; i++)
             for (int j = 0; j < dat.nbNodes; j++) {
                 double a_aux = a.tau0[i][j][p] * AcoVar.TAO + AcoVar.TAU0;
                 a.tau0[i][j][p] = a_aux;
                 a.tau[i][j][p] = a.tau0[i][j][p];
             }

     // heuristic visibility
     HeurVis hv = HVis(dat, pre);

     // ---------------------- MAIN ACO LOOP ----------------------
     double temp_cost;
     double max_cost = Integer.MAX_VALUE;
     double global_best = max_cost;
     better.cost = max_cost;

     long t_alliter1 = System.currentTimeMillis();

     int it = 0;
     int best_count = 0;
     int nr_dead = 0;
     int it_stop = 0;

     do {
         // reset iteration
         itrt.best_cost = max_cost;

         System.out.println("\nNrIter: " + it + "\n");
         System.out.println("Iter Best Cost" + itrt.best_cost);
         System.out.println("Global Best: " + global_best + "\n");

         // reset ant values
         temp_cost = 0.0;
         for (int k = 0; k < AcoVar.NR_ANTS; k++) {
             ants[k].life = 1;
             ants[k].cost = 0.0;
             for (int p = 0; p < dat.nbProducts; p++)
                 for (int i = 0; i < dat.nbNodes; i++)
                     ants[k].x[i][p] = -1;
             for (int j = 0; j < dat.nbNodes; j++)
                 ants[k].z[j] = 0;
             for (int j = 0; j < dat.nbNodes; j++)
                 for (int p = 0; p < dat.nbProducts; p++)
                     ants[k].avail_cap[j][p] = dat.gamma[j][p];
             // copy pre.allow into avail_tau
             for (int i = 0; i < dat.nbNodes; i++)
                 for (int j = 0; j < dat.nbNodes; j++)
                     for (int p = 0; p < dat.nbProducts; p++)
                         ants[k].avail_tau[i][j][p] = pre.allow[i][j][p];
         }

         // colony loop
         long t_allants1 = System.currentTimeMillis();
         for (int k = 0; k < AcoVar.NR_ANTS; k++) {
             temp_cost = 0.0;
             int ant_life = 1;
             if (k > 0 && ants[k - 1].life <= 0) {
                 ant_life = 0;
                 index.prod = ants[k - 1].prod;
                 index.node = ants[k - 1].node;
                 index.hub = ants[k - 1].hub;
             }

             int sum_available = 0;
             int sum_remaining = 0;

             do {
                 if (ant_life <= 0) {
                     // previous ant dead -> start with that infeasible component
                     ant_life = 1;
                     // index set already from aboveø
                 } else {
                     double q = unifRand();
                     if (q <= AcoVar.Q0) {
                         index = Greedy.run(ants[k].life, dat, a, ants[k]);
                     } else {
                         index = Roulette.run(ants[k].life, dat, a, ants[k], pre, hv);
                     }
                 }

                 if (ants[k].life > 0) {
                     AddSolutionComponent(index, dat, ants[k]);
                     LocalPheromoneUpdate(index.prod, index.hub, index.node, a);
                     if (index.node != index.hub) {
                         ApplySingleAllocationRules(index.prod, index.hub, index.node, dat.nbNodes, ants[k], 1, 1);
                     } else {
                         ApplySingleAllocationRules(index.prod, index.hub, index.node, dat.nbNodes, ants[k], 1, 0);
                     }
                     if (index.node == index.hub) {
                         ApplyLkRules(index.prod, index.hub, dat, ants[k]);
                     }
                     UpdateAvailableCapacities(index.prod, index.hub, index.node, dat, ants[k], k);
                     OpenHub(index, dat, ants[k], temp_cost);
                     DedicateHub(index, dat, ants[k], a, k, temp_cost);

                     sum_available = 0;
                     for (int p = 0; p < dat.nbProducts; p++)
                         for (int i = 0; i < dat.nbNodes; i++)
                             for (int j = 0; j < dat.nbNodes; j++)
                                 sum_available += ants[k].avail_tau[i][j][p];
                 } else {
                     ants[k].cost = max_cost;
                     break;
                 }

                 // compute sum_remaining
                 sum_remaining = 0;
                 for (int p = 0; p < dat.nbProducts; p++)
                     for (int i = 0; i < dat.nbNodes; i++)
                         if (ants[k].x[i][p] == -1) sum_remaining++;

                 if (sum_available == 0 && sum_remaining > 0) {
                     ants[k].life = 0;
                     ants[k].cost = max_cost;
                     // choose a remaining connection and mark it (similar to C++)
                     boolean found = false;
                     for (int p = 0; p < dat.nbProducts && !found; p++) {
                         for (int i = 0; i < dat.nbNodes && !found; i++) {
                             if (ants[k].x[i][p] == -1 || dat.O[i][p] < dat.gamma[i][p]) {
                                 ants[k].prod = p;
                                 ants[k].hub = i;
                                 ants[k].node = i;
                                 found = true;
                             }
                         }
                     }
                     break;
                 }
             } while (sum_available > 0);

             // best ant cost (stub)
             if (ants[k].life > 0) {
                 GetBestAntCost(dat.nbProducts, dat.nbNodes, ants[k], itrt, it, k);
             } else {
                 // dead ant
             }
         } // end for ants

         long t_allants2 = System.currentTimeMillis();

         int kk = itrt.best_ant;
         if (kk < 0 || kk >= AcoVar.NR_ANTS) kk = 0; // safe guard

         if (ants[kk].life > 0) {

             // optional local search & close hub stubs
             // CloseHub / CloseRandomHub / LocalSearch / LocalSearch1 are stubs
             // Estimate inter-hub costs (simplified)
             sl = getxSolution(dat, itrt);
             double inter_cost = 0.0;
             double estimate_d = 0.0;
             for (int p = 0; p < dat.nbProducts; p++) {
                 for (int i = 0; i < dat.nbNodes; i++) {
                     for (int j = 0; j < dat.nbNodes; j++) {
                         if (sl != null && sl.inter_x != null && sl.inter_x[i] != null
                                 && sl.inter_x[i][j] != null && sl.inter_x[i][j][p] > 0) {
                             inter_cost += dat.alpha[p] * dat.O[i][p] * ( (sl.x!=null && sl.x[i][j][p]>0) ? sl.x[i][j][p] : 0)
                                     + estimate_d / (sl.count_inter == 0 ? 1 : sl.count_inter * 2);
                             estimate_d = 0.0;
                             for (int l = 0; l < dat.nbNodes; l++)
                                 if (sl.inter_x[i][j][p] > 0)
                                     estimate_d += sl.inter_x[i][j][p] * dat.d[i][j];
                         }
                     }
                 }
             }
             itrt.best_cost += inter_cost;

             // optional: invoke CPLEX (stub)
             // sol = getSolution(dat, ants[kk], itrt);
             // runCplex(dat, sol, itrt.best_cost, fichIn, fichOut);

             // global pheromone update (apply policy)
             if (it > 1) {
                 if (itrt.best_cost < global_best + global_best * AcoVar.UPDATE_PARAM) {
                     GlobalPheromoneUpdate(dat, a, itrt, scal_param);
                 }
             } else {
                 GlobalPheromoneUpdate(dat, a, itrt, scal_param);
             }

             if (itrt.best_cost >= global_best) {
                 best_count++;
             } else {
                 best_count = 0;
             }

             if (best_count > AcoVar.MAX_NO_BEST) it_stop = 1;

             double timeSec = (System.currentTimeMillis() - t1) / 1000.0;
             if (timeSec > AcoVar.MAX_TIME) {
                 it_stop = 1;
                 System.out.println("time = " + timeSec);
             }

             nr_dead = 0;

             // write best value to file (if no CPLEX)
             appendToFile(bestLog, formatDouble(itrt.best_cost) + ",");
         } else {
             nr_dead++;
             if (nr_dead > AcoVar.MAX_DEAD) {
                 GlobalDeadPheromoneUpdate(dat, a, ants[0], scal_param, global_best);
             }
         }

         // Update best global
         GetBestCost(dat, itrt, better, it, global_best, t1);

         it++;
         if (it >= AcoVar.NR_ITER) it_stop = 1;

     } while (it_stop < 1);

     long t_alliter2 = System.currentTimeMillis();
     double totalTimeSec = (t_alliter2 - t1) / 1000.0;
     System.out.println("\nTotal Elapsed time: " + totalTimeSec + " CPU seconds.");
     if (totalTimeSec > 60) System.out.println("Total Elapsed time: " + (totalTimeSec / 60) + " CPU minutes.");

     if (global_best < max_cost) {
         // produce outputs, compute gap (stub)
         Gap gp = computeGap(gapIn, fichIn, better.cost);
         System.out.println("gp.inst_name " + gp.instanceName);
         System.out.println("gp.cost " + gp.cost);
         System.out.println("gp.gap " + gp.gap);
         System.out.println("gp.time " + gp.time);
         OutputFile(AcoVar.NR_ITER, better.nr_iter, fichOut, gp.cost, gp.time, better.cost, better.time, totalTimeSec, gp.gap);
     } else {
         System.out.println("NO SOLUTION FOUND");
     }

     System.out.println("END RUN ACO");
     return 0;
 }

 // --------------------------
 // Helper / stub methods
 // --------------------------

 private static void prepareOutputFile(String fichIn, String fichOut, double scalParam) {
     System.out.println("prepareOutputFile called (stub): " + fichIn + " -> " + fichOut + " scal=" + scalParam);
     // Implement actual file initialization or header writing if needed.
 }

 private static double unifRand() {
	 Random rand = new Random();
	 return rand.nextDouble();
 }

 // Stub: compute heuristic visibility (return a simple placeholder)
 private static HeurVis HVis(Data dat, PreProc pre) {
     HeurVis hv = new HeurVis(dat.nbNodes, dat.nbProducts);
     hv.eta = new double[dat.nbNodes][dat.nbNodes][dat.nbProducts];
     hv.tfc = new double[dat.nbNodes][1];
     hv.hubFixedCost = new double[dat.nbNodes][1];
     // Naive init
     for (int i = 0; i < dat.nbNodes; i++)
         for (int j = 0; j < dat.nbNodes; j++)
             for (int p = 0; p < dat.nbProducts; p++)
                 hv.eta[i][j][p] = 1.0;
     return hv;
 }

 // Stub: AddSolutionComponent - update ant's x to mark assignment and update cost minimally
 private static void AddSolutionComponent(Ind index, Data dat, Ant ant) {
     if (ant != null && ant.x != null) {
         ant.x[index.node][index.prod] = index.hub;
         // incremental cost (dummy)
         ant.cost += 0.0;
     }
 }

 // Stub: Local pheromone update for a single component
 private static void LocalPheromoneUpdate(int prod, int hub, int node, Aco a) {
     // Example: slightly reduce pheromone locally
     if (a != null && a.tau != null && node < a.tau.length && hub < a.tau[node].length && prod < a.tau[node][hub].length) {
         a.tau[node][hub][prod] = (1 - 0.1) * a.tau[node][hub][prod] + 0.1 * AcoVar.TAU0;
     }
 }

 // Stub: ApplySingleAllocationRules - placeholder
 private static void ApplySingleAllocationRules(int prod, int hub, int node, int nbNodes, Ant ant, int a, int b) {
     // No-op placeholder
 }

 // Stub: ApplyLkRules - placeholder
 private static void ApplyLkRules(int prod, int hub, Data dat, Ant ant) {
     // No-op placeholder
 }

 // Stub: Update available capacities
 private static void UpdateAvailableCapacities(int prod, int hub, int node, Data dat, Ant ant, int k) {
     if (ant != null) {
         if (hub < ant.avail_cap.length && prod < ant.avail_cap[hub].length) {
             ant.avail_cap[hub][prod] -= dat.O[node][prod]; // simple assumption
         }
     }
 }

 // Stub: OpenHub
 private static void OpenHub(Ind index, Data dat, Ant ant, double tempCost) {
     if (ant != null) {
         ant.z[index.hub] = 1;
     }
 }

 // Stub: DedicateHub
 private static void DedicateHub(Ind index, Data dat, Ant ant, Aco a, int k, double tempCost) {
     // No-op placeholder
 }

 // Stub: GetBestAntCost - updates iteration.best if this ant better
 private static void GetBestAntCost(int nbProducts, int nbNodes, Ant ant, Iteration itrt, int iterIndex, int k) {
     double cost = ant.cost;
     if (itrt.best_cost > cost) {
         itrt.best_cost = cost;
         itrt.best_ant = k;
         // x_best copy (partial)
         for (int i = 0; i < itrt.x_best.length && i < ant.x.length; i++)
             System.arraycopy(ant.x[i], 0, itrt.x_best[i], 0, Math.min(itrt.x_best[i].length, ant.x[i].length));
     }
 }

 // Stub: CloseHub
 private static void CloseHub(Data dat, Ant ant, Iteration itrt, double maxCost) {
     // placeholder
 }

 // Stub: CloseRandomHub
 private static void CloseRandomHub(Data dat, Ant ant, Iteration itrt, double maxCost) {
     // placeholder
 }

 // Stub: Local search variants
 private static void LocalSearch(Data dat, Ant ant, Iteration itrt, int it) {
     // placeholder
 }

 private static void LocalSearch1(Data dat, Ant ant, Iteration itrt, int it) {
     // placeholder
 }

 // Stub: getxSolution
 private static SolutionX getxSolution(Data dat, Iteration itrt) {
     SolutionX sx = new SolutionX(dat);
     sx.x = new int[dat.nbNodes][dat.nbNodes][dat.nbProducts];
     sx.inter_x = new int[dat.nbNodes][dat.nbNodes][dat.nbProducts];
     sx.count_inter = 0;
     // placeholder: no inter-hub links
     return sx;
 }

 // Stub: getSolution
 private static Solution getSolution(Data dat, Ant ant, Iteration itrt) {
     Solution s = new Solution(dat);
     // construct minimal solution representation
     s.z = new int[dat.nbNodes];
     s.x = new int[dat.nbNodes][dat.nbNodes][dat.nbProducts]; // placeholder
     s.y = new double[dat.nbNodes][dat.nbNodes][dat.nbNodes][dat.nbProducts];
     s.cost = ant.cost;
     return s;
 }

 // Stub: runCplex
 private static void runCplex(Data dat, Solution sol, double bestCost, String fichIn, String fichOut) {
     System.out.println("runCplex called (stub). bestCost=" + bestCost);
 }

 // Stub: GlobalPheromoneUpdate
 private static void GlobalPheromoneUpdate(Data dat, Aco a, Iteration itrt, double scalParam) {
     // Example: reinforce best components (very simplified)
     if (itrt == null) return;
     for (int i = 0; i < dat.nbNodes; i++)
         for (int j = 0; j < dat.nbNodes; j++)
             for (int p = 0; p < dat.nbProducts; p++)
                 a.tau[i][j][p] = (1 - 0.01) * a.tau[i][j][p] + 0.01 * AcoVar.TAU0;
 }

 // Stub: GlobalDeadPheromoneUpdate
 private static void GlobalDeadPheromoneUpdate(Data dat, Aco a, Ant ant, double scalParam, double globalBest) {
     // placeholder - refill pheromone a bit
     for (int i = 0; i < dat.nbNodes; i++)
         for (int j = 0; j < dat.nbNodes; j++)
             for (int p = 0; p < dat.nbProducts; p++)
                 a.tau[i][j][p] += AcoVar.TAU0 * 0.001;
 }

 // Stub: GetBestCost - update global best and better struct
 private static void GetBestCost(Data dat, Iteration itrt, Best better, int it, double globalBest, long t1) {
     if (itrt.best_cost < better.cost) {
         better.cost = itrt.best_cost;
         better.nr_iter = it;
         better.time = (System.currentTimeMillis() - t1) / 1000.0;
     }
 }

 // Stub: computeGap
 private static Gap computeGap(String gapIn, String fichIn, double betterCost) {
     return new Gap(fichIn, betterCost, 0.0, 0.0);
 }

 // Stub: OutputFile
 private static void OutputFile(int nrIter, int itBest, String fichOut, double gpCost, double gpTime, double bestCost, double bestTime, double seconds, double gpValue) {
     System.out.println("OutputFile called (stub). bestCost=" + bestCost);
 }

 // Append to a text file
 private static void appendToFile(String filename, String text) {
     try (FileWriter fw = new FileWriter(filename, true)) {
         fw.write(text);
     } catch (IOException e) {
         System.err.println("Error appending to file " + filename + ": " + e.getMessage());
     }
 }

 private static String formatDouble(double v) {
     DecimalFormat df = new DecimalFormat("#.##");
     return df.format(v);
 }
}

