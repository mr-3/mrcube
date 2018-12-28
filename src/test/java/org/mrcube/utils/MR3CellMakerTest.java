package org.mrcube.utils;

import org.jgraph.graph.GraphCell;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mrcube.MR3;
import org.mrcube.views.MR3ProjectPanel;
import org.mrcube.jgraph.ClassGraphMarqueeHandler;
import org.mrcube.jgraph.PropertyGraphMarqueeHandler;
import org.mrcube.jgraph.RDFGraphMarqueeHandler;
import org.mrcube.models.MR3Constants;
import org.mrcube.models.MR3Literal;

import java.awt.*;

import static org.junit.jupiter.api.Assertions.assertTrue;

class MR3CellMakerTest {

    private static final String DEFAULT_URI = "http://mrcube.org#";

    @BeforeAll
    public static void setUp() {
        MR3.initialize(MR3.class);
        new MR3();
    }

    @AfterAll
    public static void tearDown() {
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.exit(0);
    }

    @Test
    void insertRDFResource() {
        MR3ProjectPanel project = MR3.getCurrentProject();
        RDFGraphMarqueeHandler rdfGraphHandler = project.getRDFEditor().getRdfGraphMarqueeHandler();
        MR3CellMaker cellMaker = rdfGraphHandler.getCellMaker();
        String uri  = DEFAULT_URI + "resource_test";
        GraphCell cell = cellMaker.insertRDFResource(new Point(30, 30), uri, null, MR3Constants.URIType.URI);
        assertTrue(cell != null);
    }

    @Test
    void insertRDFLiteral() {
        MR3ProjectPanel project = MR3.getCurrentProject();
        RDFGraphMarqueeHandler rdfGraphHandler = project.getRDFEditor().getRdfGraphMarqueeHandler();
        MR3CellMaker cellMaker = rdfGraphHandler.getCellMaker();
        MR3Literal literal = new MR3Literal();
        literal.setString("Test");
        GraphCell cell = cellMaker.insertRDFLiteral(new Point(100, 100), literal);
        assertTrue(cell != null);
    }

    @Test
    void insertClass() {
        MR3ProjectPanel project = MR3.getCurrentProject();
        ClassGraphMarqueeHandler classGraphHandler = project.getClassEditor().getClassGraphMarqueeHandler();
        MR3CellMaker cellMaker = classGraphHandler.getCellMaker();
        GraphCell cell = cellMaker.insertClass(new Point(50, 50), DEFAULT_URI + "TestClass");
        assertTrue(cell != null);
    }

    @Test
    void insertProperty() {
        MR3ProjectPanel project = MR3.getCurrentProject();
        PropertyGraphMarqueeHandler propertyGraphHandler = project.getPropertyEditor().getPropertyGraphMarqueeHandler();
        MR3CellMaker cellMaker = propertyGraphHandler.getCellMaker();
        GraphCell cell = cellMaker.insertProperty(new Point(50, 50), DEFAULT_URI + "testProperty");
        assertTrue(cell != null);
    }
}