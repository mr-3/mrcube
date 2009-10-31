package jp.ac.shizuoka.cs.panda.mmm.mr3.jgraph;
import com.jgraph.graph.*;

// A Custom Model that does not allow Self-References
public class RDFGraphModel extends DefaultGraphModel {

    public boolean acceptsSource(Object edge, Object port) {
        return (((Edge) edge).getTarget() != port); // Source only Valid if not Equal Target
    }

    public boolean acceptsTarget(Object edge, Object port) {
        return (((Edge) edge).getSource() != port); // Target only Valid if not Equal Source
    }
}
