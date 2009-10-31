package mr3.data;

public class GraphType {

    private String type;
    
    private GraphType(String t) {
        type = t;
    }

	public static final GraphType RDF = new GraphType("RDF");
	public static final GraphType REAL_RDF = new GraphType("REAL_RDF");
    public static final GraphType CLASS = new GraphType("Class");
    public static final GraphType PROPERTY = new GraphType("Property");

    public String toString() {
        return type;
    }
}
