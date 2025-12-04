package antcolony;

public class Ind {
    public int prod;
    public int node;
    public int hub;

    public Ind() {
        this.prod = 0;
        this.node = 0;
        this.hub  = 0;
    }
    
    public Ind(int prod, int node, int hub) {
        this.prod = prod;
        this.node = node;
        this.hub  = hub;
    }
}