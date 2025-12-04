package antcolony;

//CheckNrProds.java
//Java translation of checknrprods.cpp / checknrprods.h
//Returns the number of products dedicated to each hub (array of length nrNodes).

public class CheckNrProds {

 /**
  * Count how many products are handled (dedicated) by each hub for a given ant.
  *
  * @param nrProducts number of products (P)
  * @param nrNodes    number of nodes/hubs (N)
  * @param ant        the ant instance; expects ant.x[node][product] == hubIndex (or -1 if unassigned)
  * @return an int[] of length nrNodes where result[j] is the number of products handled by hub j
  */
 public static int[] checkProdConnects(int nrProducts, int nrNodes, Ant ant) {
     if (ant == null) throw new IllegalArgumentException("ant cannot be null");
     if (ant.x == null) throw new IllegalArgumentException("ant.x cannot be null");

     int[] prods = new int[nrNodes];

     // initialize to zero (automatic in Java, but explicit for clarity)
     for (int j = 0; j < nrNodes; j++) prods[j] = 0;

     // count the number of products handled by each hub
     // C++: for (j=0;j<nr_nodes;j++) for (p=0;p<nr_products;p++) if(ants.x[j][p]==j) n.prods[j]++;
     // This assumes ant.x[j][p] == j means node j is assigned to hub j (i.e. dedicated).
     for (int j = 0; j < nrNodes; j++) {
         for (int p = 0; p < nrProducts; p++) {
             // guard in case shapes differ
             if (j < ant.x.length && p < ant.x[j].length) {
                 if (ant.x[j][p] == j) {
                     prods[j]++;
                 }
             }
         }
     }
     return prods;
 }

 /**
  * Alternative: if you have ant.x as x[node][product] but ant type is not RunAco.Ant,
  * you can call this helper overload passing the raw array.
  */
 public static int[] checkProdConnects(int nrProducts, int nrNodes, int[][] x) {
     if (x == null) throw new IllegalArgumentException("x cannot be null");
     int[] prods = new int[nrNodes];
     for (int j = 0; j < nrNodes; j++) {
         for (int p = 0; p < nrProducts; p++) {
             if (j < x.length && p < x[j].length && x[j][p] == j) prods[j]++;
         }
     }
     return prods;
 }
}
