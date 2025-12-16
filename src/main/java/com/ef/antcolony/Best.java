package main.java.com.ef.antcolony;

/** Global best solution found so far */
public  class Best {
    public int nr_iter;
    public double cost;
    public double time;

    // Global best solution
    public double[][][][] y;
    public int[][] x;
    public int[] z;

    public Best(int nProducts, int nNodes) {
        this.y = new double[nProducts][nNodes][nNodes][nNodes];
        this.x = new int[nProducts][nNodes];
        this.z = new int[nNodes];
    }
}