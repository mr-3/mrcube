package org.mrcube.utils;

import org.jgraph.graph.GraphCell;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mrcube.MR3;
import org.mrcube.MR3Project;
import org.mrcube.jgraph.ClassGraphMarqueeHandler;
import org.mrcube.jgraph.PropertyGraphMarqueeHandler;
import org.mrcube.jgraph.RDFGraphMarqueeHandler;
import org.mrcube.models.MR3Literal;

import java.awt.*;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

class MR3CellMakerTest {

    @BeforeAll
    public static void setUp() {
        MR3.initialize(MR3.class);
        MR3 mr3 = new MR3();
        mr3.newProject("Test");
    }

    @AfterAll
    public static void tearDown() {
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.exit(0);
    }

    @Test
    void insertRDFResource() {
        fail();
    }

    @Test
    void insertRDFLiteral() {
        MR3Project project = MR3.getCurrentProject();
        RDFGraphMarqueeHandler rdfGraphHandler = project.getRDFEditor().getRdfGraphMarqueeHandler();
        MR3CellMaker cellMaker = rdfGraphHandler.getCellMaker();
        MR3Literal literal = new MR3Literal();
        literal.setString("Test");
        GraphCell cell = cellMaker.insertRDFLiteral(new Point(50, 50), literal);
        assertTrue(cell != null);
    }

    @Test
    void insertClass() {
        MR3Project project = MR3.getCurrentProject();
        ClassGraphMarqueeHandler classGraphHandler = project.getClassEditor().getClassGraphMarqueeHandler();
        MR3CellMaker cellMaker = classGraphHandler.getCellMaker();
        GraphCell cell = cellMaker.insertClass(new Point(50, 50), "http://mrcube.org/TestClass");
        assertTrue(cell != null);
    }

    @Test
    void insertProperty() {
        MR3Project project = MR3.getCurrentProject();
        PropertyGraphMarqueeHandler propertyGraphHandler = project.getPropertyEditor().getPropertyGraphMarqueeHandler();
        MR3CellMaker cellMaker = propertyGraphHandler.getCellMaker();
        GraphCell cell = cellMaker.insertProperty(new Point(50, 50), "http://mrcube.org/testProperty");
        assertTrue(cell != null);
    }
}