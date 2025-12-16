package main.java.com.ef.antcolony;

public class PNH {
    public final int prod;
    public final int node;
    public final int hub;

    public PNH() {
        this.prod = 0;
        this.node = 0;
        this.hub  = 0;
    }
    
    public PNH(int prod, int node, int hub) {
        this.prod = prod;
        this.node = node;
        this.hub  = hub;
    }

	@Override
	public String toString() {
		return "PNH [prod=" + prod + ", node=" + node + ", hub=" + hub + "]";
	}
}