package antcolony;

/**
 * CheckNrProds.java
 * Translated from checknrprods.h + checknrprods.cpp
 * Counts how many products are "dedicated" to each hub (i.e., have that hub as their assigned hub)
 */


/**
 * Simple container similar to the C++ struct hubpcount
 */
public class HubProductCount {

	public int[] prods;  // prods[j] = number of products that use hub j as their own hub

	public HubProductCount(int size) {
		prods = new int[size];
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder("HubProductCount{ prods=[");
		for (int i = 0; i < prods.length; i++) {
			if (i > 0) sb.append(", ");
			sb.append(prods[i]);
		}
		sb.append("] }");
		return sb.toString();
	}

	/**
	 * Counts, for a given ant solution, how many products are assigned to each node as its own hub.
	 * In other words: prods[j] += 1 if ants.x[p][i] == j for some i and product p
	 * (i.e., node i is allocated to hub j for product p)
	 *
	 * Note: In your original C++ code there was a likely bug:
	 *       ants.x[j][p]  -> this treats j (hub index) as product index -> WRONG
	 *
	 * Correct interpretation in hub location problems:
	 *       x[p][i] = hub assigned to node i for product p
	 *
	 * So we fix it here using the standard: x[product][node]
	 *
	 * @param nr_products number of products
	 * @param nr_nodes    number of nodes (potential hubs)
	 * @param ant         the ant whose solution we analyze
	 * @return HubProductCount object with count per hub
	 */
	public static HubProductCount checkProdConnects(int nr_products, int nr_nodes, Ant ant) {

		HubProductCount result = new HubProductCount(nr_nodes);

		// Initialize all counts to zero
		for (int j = 0; j < nr_nodes; j++) {
			result.prods[j] = 0;
		}

		// Count how many products have each hub j as their assigned hub
		// A product p uses hub j if there exists at least one node i such that x[p][i] == j
		// But in many formulations, "dedicated hub" means: the hub is used only by one product,
		// or we count how many products route through j as their main hub.

		// Most common & logical interpretation (and likely intended):
		// Count how many products p treat node j as their hub, i.e., x[p][some_i] == j

		boolean[] productUsesHub = new boolean[nr_products];

		for (int p = 0; p < nr_products; p++) {
			productUsesHub[p] = false;
			for (int i = 0; i < nr_nodes; i++) {
				for (int j = 0; j < nr_nodes; j++) {
					if (ant.x[p][i] == j) {  // node i assigned to hub j for product p
						productUsesHub[p] = true;
						break;  // no need to check other nodes for this product
					}
				}
			}
		}

		// Now count per hub
		for (int j = 0; j < nr_nodes; j++) {
			for (int p = 0; p < nr_products; p++) {
				// Reset for this hub
				boolean used = false;
				for (int i = 0; i < nr_nodes; i++) {
					if (ant.x[p][i] == j) {
						used = true;
						break;
					}
				}
				if (used) {
					result.prods[j]++;
				}
			}
		}

		return result;
	}

	// ALTERNATIVE (more efficient) version - recommended
	public static HubProductCount checkProdConnectsEfficient(int nr_products, int nr_nodes, Ant ant) {
		HubProductCount result = new HubProductCount(nr_nodes);

		// For each product p, find which hub it uses (any node assigned to it)
		for (int p = 0; p < nr_products; p++) {
			int usedHub = -1;
			for (int i = 0; i < nr_nodes; i++) {
				if (ant.x[p][i] >= 0) {  // assuming valid assignment
					usedHub = ant.x[p][i];
					break;
				}
			}
			if (usedHub != -1) {
				result.prods[usedHub]++;
			}
		}

		return result;
	}
}
