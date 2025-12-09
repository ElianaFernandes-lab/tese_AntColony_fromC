package antcolony;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;

import antcolony.ReadData.Data;
import antcolony.constants.AcoVar;

public class LS {

    // Helper method to write to file (replaces C++ ofstream logic)
    private static void logToFile(String content) {
        if (!AcoVar.LSSHIST) return;
        try (FileWriter myfile = new FileWriter("AcoVar.LSSHISTory.txt", true)) {
            myfile.write(content + "\n");
        } catch (IOException e) {
            System.err.println("Error writing to AcoVar.LSSHISTory.txt: " + e.getMessage());
        }
    }

    /**
     * Implements a local search heuristic (Relocate Node) for the ACO solution.
     * * @param dados The problem data.
     * @param ants The current ant state (used for available capacity checks).
     * @param iter The iteration object storing the current best solution and cost.
     * @param iterat The current iteration number (for logging).
     * @return 0, indicating successful execution or termination.
     */
    public static int LocalSearch1(Data dados, Ant ants, Iteration iter, int iterat) {
        
        if (AcoVar.LSSHIST) {
            logToFile("LSS");
            logToFile("iteration: " + iterat);
        }
        
        int i, j, p, l;
        double compare_cost;
        
        if (AcoVar.LSSHIST) {
            // WRITE CURRENT SOLUTION
            StringBuilder sb = new StringBuilder();
            sb.append("CURRENT SOLUTION\n");
            sb.append("Variables z equal to 1: \n");
            for (j = 0; j < dados.nbNodes; j++)
                if (iter.z_best[j] == 1)
                    sb.append("z[" + j + "]\n");
            sb.append("\n");
            
            sb.append("Variables x equal to 1: \n");
            for (p = 0; p < dados.nbProducts; p++)
                for (i = 0; i < dados.nbNodes; i++)
                    for (j = 0; j < dados.nbNodes; j++) // Note: The C++ condition is tricky, assuming x_best[i][p] holds the hub index j
                        if (iter.x_best[p][i] == j) {
                            sb.append("x[" + i + "][" + j + "][" + p + "]\n");
                        }
            logToFile(sb.toString());
        }
        
        // Declarations
        int ls_iter;
        int first_admissible = 0;
        int nr_avail_hubs;
        // int nr_avail_nodes; // Declared but unused in C++ logic block
        
        double cost_plus;
        double cost_subtract;
        
        int flag = 0;
        int flag_avail_hub = 0;
        int flag_avail_node = 0;
        int hub = 0;
        int new_hub_index;
        int new_hub = 0;
        int node = 0;
        int node_index = 0;
        int prod = 0;
        int nr_avail_prod;
        int temp_prod;
        int temp_list;
        
        // Only run if the ant is alive
        if (ants.life > 0) {
            
            // Note: In Java, memory for nr will be managed by GC.
            // Assuming 'counting' is a function that returns a new Counters object.
            Counters nr = Counters.counting(dados.nbProducts, dados.nbNodes, iter);
            
            int[] avail_prod = new int[dados.nbProducts];
            int[] temp_list_phubs = new int[dados.nbNodes];
            
            // Initializations
            ls_iter = 0;
            first_admissible = 0;
            
            // Initialize temp_list_phubs to -1
            for (l = 0; l < dados.nbNodes; l++)
                temp_list_phubs[l] = -1;

            // Initialize avail_prod list
            for (p = 0; p < dados.nbProducts; p++) {
                avail_prod[p] = p;
            }
            
            // Re-call counting to re-initialize 'nr' list fields (as per C++ logic)
            nr = Counters.counting(dados.nbProducts, dados.nbNodes, iter);
            
            // 1. do while(first_admissible < 1)
            do {
                do { // 2. do while(flag < 1)
                    
                    // Count available products for relocation
                    nr_avail_prod = 0;
                    for (p = 0; p < dados.nbProducts; p++)
                        if (avail_prod[p] >= 0) nr_avail_prod++;
                    
                    if (nr_avail_prod > 0) {
                        // Rearrange the available products to the beginning of the list
                        l = 0;
                        int current_idx = 0;
                        while(l < nr_avail_prod) {
                            if(avail_prod[current_idx] >= 0) {
                                temp_prod = avail_prod[current_idx];
                                avail_prod[l] = temp_prod;
                                if (AcoVar.LSSHIST) logToFile("temp_prod[" + l + "]=" + avail_prod[l]);
                                l++;
                            }
                            current_idx++;
                        }
                        
                        // Put the rest of the list to -1
                        for (l = nr_avail_prod; l < dados.nbProducts; l++)
                            avail_prod[l] = -1;
                        
                        // Choose a random product
                        int prod_index = (int)(Helper.unifRand() * nr_avail_prod);
                        prod = avail_prod[prod_index];
                        
                        // Count the number of nodes available for relocation for that product
                        int nr_nodes = 0;
                        for (l = 0; l < dados.nbNodes; l++)
                            if (nr.list_pnodes[prod][l] >= 0) nr_nodes++;
                        
                        if (nr_nodes > 0) {
                            if (AcoVar.LSSHIST) {
                                logToFile("Product Chosen: " + prod);
                                logToFile("There are " + nr_nodes + " available nodes.");
                                for (i = 0; i < dados.nbNodes; i++)
                                    logToFile("nr.list_pnodes[" + prod + "][" + i + "]= " + nr.list_pnodes[prod][i]);
                            }

                            if (nr_nodes < nr.pnodes[prod]) { // Rearrange the node list
                                if (AcoVar.LSSHIST) logToFile("Rearrange the node list");

                                l = 0;
                                current_idx = 0;
                                while(l < nr_nodes) {
                                    if(nr.list_pnodes[prod][current_idx] >= 0) {
                                        temp_list = nr.list_pnodes[prod][current_idx];
                                        nr.list_pnodes[prod][l] = temp_list;
                                        if (AcoVar.LSSHIST) logToFile("ordered node list[" + l + "]=" + nr.list_pnodes[prod][l]);
                                        l++;
                                    }
                                    current_idx++;
                                }
                                
                                // Put the rest of the list to -1
                                for (l = nr_nodes; l < dados.nbNodes; l++)
                                    nr.list_pnodes[prod][l] = -1;
                            } // end rearrange
                            
                            // Choose a node from that list
                            // C++: unifRand()*(nr_nodes-1)
                            node_index = (int)(Helper.unifRand() * nr_nodes);
                            node = nr.list_pnodes[prod][node_index];
                            
                            // Mark the node as unavailable for relocation in the current LS iteration
                            nr.list_pnodes[prod][node_index] = -1;
                            flag_avail_node = 1;

                            // Get the current assigned hub
                            hub = iter.x_best[prod][node];
                            if (AcoVar.LSSHIST) logToFile("x_best[" + node + "][" + prod + "]= " + hub + " will be reassigned.");

                            // Prepare temporary hub list (temp_list_phubs) excluding the current hub
                            for (l = 0; l < dados.nbNodes; l++) {
                                int current_hub = nr.list_phubs[prod][l];
                                if (current_hub != hub) {
                                    temp_list_phubs[l] = current_hub;
                                } else {
                                    temp_list_phubs[l] = -1;
                                }
                                if (AcoVar.LSSHIST) logToFile("temp_list_phubs[" + l + "] = " + temp_list_phubs[l]);
                            }

                            // Refine the hub list: remove hubs without enough available capacity
                            for (l = 0; l < dados.nbNodes; l++) {
                                j = temp_list_phubs[l];
                                if (j >= 0) {
                                    if (ants.avail_cap[prod][j] < dados.originatedFlow[prod][node]) {
                                        temp_list_phubs[l] = -1;
                                        if (AcoVar.LSSHIST) logToFile("cap(" + j + "," + prod + "): " + ants.avail_cap[prod][j] + "<" + dados.originatedFlow[prod][node] + " dados.O[" + node + "][" + prod + "]");
                                    } else if (AcoVar.LSSHIST) {
                                        logToFile("cap(" + j + "," + prod + "): " + ants.avail_cap[prod][j] + ">=" + dados.originatedFlow[prod][node] + " dados.O[" + node + "][" + prod + "]");
                                    }
                                }
                            }

                            // Count the remaining available hubs
                            nr_avail_hubs = 0;
                            for (l = 0; l < dados.nbNodes; l++)
                                if (temp_list_phubs[l] >= 0) nr_avail_hubs++;
                            
                            if (AcoVar.LSSHIST) logToFile("There are " + nr_avail_hubs + " available hubs.");

                            // Rearrange the temporary hub list
                            if (nr_avail_hubs > 0) {
                                l = 0;
                                current_idx = 0;
                                while(l < nr_avail_hubs) {
                                    if(temp_list_phubs[current_idx] >= 0) {
                                        temp_list = temp_list_phubs[current_idx];
                                        temp_list_phubs[l] = temp_list;
                                        if (AcoVar.LSSHIST) logToFile("temp_list_phubs[" + l + "]=" + temp_list_phubs[l]);
                                        l++;
                                    }
                                    current_idx++;
                                }
                                
                                // Put the rest of the list to -1
                                for (l = nr_avail_hubs; l < dados.nbNodes; l++)
                                    temp_list_phubs[l] = -1;
                                
                                if (AcoVar.LSSHIST) {
                                    StringBuilder hubList = new StringBuilder("The available hubs are:\n");
                                    for (l = 0; l < nr_avail_hubs; l++)
                                        hubList.append(temp_list_phubs[l]).append("\n");
                                    logToFile(hubList.toString());
                                }
                                
                                // Choose a random new hub
                                new_hub_index = (int)(Helper.unifRand() * nr_avail_hubs);
                                new_hub = temp_list_phubs[new_hub_index];
                                temp_list_phubs[new_hub_index] = -1; // Mark as selected (C++ removes it)
                                flag_avail_hub = 1;
                            } else {
                                // No available hubs to relocate to, mark node as searched (C++ removes it)
                                nr.list_pnodes[prod][node_index] = -1;
                                if (AcoVar.LSSHIST) logToFile("There are no available hubs to relocate this node to.\nChoose another node.");
                            }
                            
                        } else {
                            // No relocatable nodes for this product, mark product as searched
                            if (AcoVar.LSSHIST) logToFile("No relocatable nodes for product " + prod + ".");
                            flag_avail_node = 1;
                            avail_prod[prod_index] = -1;
                        }
                    } else {
                        // All products/neighbourhood searched without success
                        if (AcoVar.LSSHIST) logToFile("All neighbourhood has been searched with no sucess\n due to unfeasibility issues.");
                        flag = 2;
                        first_admissible = 2;
                    }
                } while (flag < 1 && first_admissible < 1); // 2. do search the whole space
                
                // If a feasible move was found (i.e., we selected a node and a new hub)
                if (flag_avail_node > 0 && flag_avail_hub > 0) {
                    if (AcoVar.LSSHIST) {
                        logToFile("to dedicated hub " + new_hub);
                        logToFile("cap(" + new_hub + "," + prod + "): " + ants.avail_cap[prod][new_hub] + " >= " + dados.originatedFlow[prod][node] + " : flux(" + node + ") ?");
                    }
                    
                    // Calculate cost difference for Relocate Node move
                    
                    // Cost to add (new connection)
                    cost_plus = dados.d[node][new_hub] * (dados.chi[prod] * dados.originatedFlow[prod][node] + dados.delta[prod] * dados.destinedFlow[prod][node]);
                    if (AcoVar.LSSHIST) logToFile(" add cost: " + cost_plus);
                    
                    // Cost to subtract (old connection)
                    cost_subtract = dados.d[node][hub] * (dados.chi[prod] * dados.originatedFlow[prod][node] + dados.delta[prod] * dados.destinedFlow[prod][node]);
                    if (AcoVar.LSSHIST) {
                        logToFile(" remove cost: " + cost_subtract);
                        logToFile("cost diference: " + (cost_plus - cost_subtract));
                    }
                    
                    // Check for improvement and capacity feasibility (C++ has redundant capacity check)
                    if (cost_plus - cost_subtract < 0 /* && ants.avail_cap[prod][hub] > dados.O[node][prod] */) {
                        
                        // Perform Relocation (Move is accepted)
                        iter.x_best[node][prod] = new_hub;
                        compare_cost = iter.best_cost;
                        iter.best_cost = compare_cost + cost_plus - cost_subtract;
                        
                        if (AcoVar.LSSHIST) logToFile("Reassignment done to hub " + new_hub);
                        
                        // Update capacities
                        ants.avail_cap[prod][hub] = ants.avail_cap[prod][hub] + dados.originatedFlow[prod][node];
                        ants.avail_cap[new_hub][prod] = ants.avail_cap[new_hub][prod] - dados.originatedFlow[prod][node];
                        
                        first_admissible = 2; // Stop after the first admissible (improving) move
                    } else {
                        // Move not improving, mark node as searched (C++ removes it)
                        nr.list_pnodes[prod][node_index] = -1;
                        if (AcoVar.LSSHIST) logToFile("nr.list_pnodes[" + prod + "][" + node_index + "] =" + nr.list_pnodes[prod][node_index]);
                    }
                } else {
                    if (AcoVar.LSSHIST) logToFile("Exiting Local Search with no results.");
                    first_admissible = 2; // Exit if no feasible move could be set up
                } 

                ls_iter++;
                if (AcoVar.LSSHIST) logToFile("ls_iter: " + ls_iter);
                
                // C++ had commented out max_ls_iter check: if(ls_iter>max_ls_iter) first_admissible=2;
                
            } while (first_admissible < 1); // 1. do bigger loop
            
            // C++ explicit memory deallocation (Java GC handles this)
            // C++: delete[] nr.phubs; ... delete [] temp_list_phubs;

        } // if(ants.life > 0)
        
        if (AcoVar.LSSHIST && first_admissible == 2) {
            // WRITE FINAL SOLUTION
            StringBuilder sb = new StringBuilder();
            sb.append("FINAL SOLUTION\n");
            sb.append("Variables z equal to 1: \n");
            for (j = 0; j < dados.nbNodes; j++)
                if (iter.z_best[j] == 1)
                    sb.append("z[" + j + "]\n");
            sb.append("\n");
            
            sb.append("Variables x equal to 1: \n");
            for (p = 0; p < dados.nbProducts; p++)
                for (i = 0; i < dados.nbNodes; i++)
                    for (j = 0; j < dados.nbNodes; j++)
                        if (iter.x_best[p][i] == j) {
                            sb.append("x[" + i + "][" + j + "][" + p + "]\n");
                        }
            logToFile(sb.toString());
        }
        
        return 0;
    }

	// Assuming counting(int, int, Iteration) and unifRand() are implemented elsewhere.
	// Example helper function for unifRand:
	class Helper {
	    private static final Random rand = new Random();
	    public static double unifRand() {
	        return rand.nextDouble(); // Returns a double between 0.0 (inclusive) and 1.0 (exclusive)
	    }
	}
}
