module org.mrcube.mrcube {
    requires java.desktop;
    requires java.prefs;
    requires java.logging;
    requires jgraph;
    requires org.apache.jena.core;
    requires jfontchooser;
    requires idw;
    exports org.mrcube;
    exports org.mrcube.jgraph;
    exports org.mrcube.models;
    exports org.mrcube.utils;
}