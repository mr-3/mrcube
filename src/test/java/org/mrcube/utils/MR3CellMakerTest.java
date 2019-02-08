package org.mrcube.utils;

import org.apache.jena.rdf.model.ResourceFactory;
import org.jgraph.graph.DefaultGraphCell;
import org.jgraph.graph.GraphCell;
import org.jgraph.graph.GraphConstants;
import org.jgraph.graph.Port;
import org.junit.jupiter.api.*;
import org.mrcube.MR3;
import org.mrcube.jgraph.RDFGraph;
import org.mrcube.models.*;

import java.awt.*;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MR3CellMakerTest {

    private static final String DEFAULT_URI = "http://mrcube.org#";

    @BeforeAll
    static void setUp() {
        MR3.initialize(MR3.class);
        new MR3();
    }

    @AfterAll
    static void tearDown() {
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.exit(0);
    }

    @Nested
    class InsertRDFResourceTest {

        GraphCell rdfResourceCell;
        String testResourceURI = DEFAULT_URI + "test_resource";

        @BeforeEach
        void setUp() {
            var project = MR3.getCurrentProject();
            var rdfGraphHandler = project.getRDFEditor().getRdfGraphMarqueeHandler();
            var cellMaker = rdfGraphHandler.getCellMaker();
            rdfResourceCell = cellMaker.insertRDFResource(new Point(30, 30), testResourceURI, null, MR3Constants.URIType.URI);
        }

        @Test
        void isRDFResourceCell() {
            var expected = true;
            var actual = RDFGraph.isRDFResourceCell(rdfResourceCell);
            assertEquals(expected, actual);
        }

        @Test
        void testInsertedRDFResourceURI() {
            var expected = testResourceURI;
            var model = (RDFResourceModel) GraphConstants.getValue(rdfResourceCell.getAttributes());
            var actual = model.getURIStr();
            assertEquals(expected, actual);
        }

    }

    @Nested
    class InsertRDFPropertyTest {

        GraphCell rdfPropertyCell;
        String testPropertyURI = DEFAULT_URI + "test_property";

        @BeforeEach
        void setUp() {
            var project = MR3.getCurrentProject();
            var rdfGraphHandler = project.getRDFEditor().getRdfGraphMarqueeHandler();
            var cellMaker = rdfGraphHandler.getCellMaker();
            String testURI1 = DEFAULT_URI + "test_resource1";
            String testURI2 = DEFAULT_URI + "test_resource2";
            var cell1 = cellMaker.insertRDFResource(new Point(100, 200), testURI1, null, MR3Constants.URIType.URI);
            var cell2 = cellMaker.insertRDFResource(new Point(200, 300), testURI2, null, MR3Constants.URIType.URI);
            var rdfsPropertyCell = cellMaker.insertProperty(new Point(50, 50), testPropertyURI);
            project.getPropertyEditor().getGraph().setSelectionCell(rdfsPropertyCell);
            project.getRDFEditor().getGraph().setSelectionCell(cell1);
            var selectedPortSet = rdfGraphHandler.getSelectedResourcePorts();
            var cell2Port = ((DefaultGraphCell) cell2).getChildAt(0);
            rdfGraphHandler.connectCells(selectedPortSet, (Port) cell2Port);
            rdfPropertyCell = (GraphCell) project.getGraphManager().getRDFPropertyCell(ResourceFactory.createResource(testPropertyURI));
        }

        @Test
        void isRDFPropertyCell() {
            var expected = true;
            var actual = RDFGraph.isRDFPropertyCell(rdfPropertyCell);
            assertEquals(expected, actual);
        }

        @Test
        void testInsertedRDFPropertyURI() {
            var expected = testPropertyURI;
            var model = (PropertyModel) GraphConstants.getValue(rdfPropertyCell.getAttributes());
            var actual = model.getURIStr();
            assertEquals(expected, actual);
        }

    }


    @Nested
    class InsertRDFLiteralTest {
        GraphCell literalCell;
        String testLiteralString = "Test";

        @BeforeEach
        void setUp() {
            var project = MR3.getCurrentProject();
            var rdfGraphHandler = project.getRDFEditor().getRdfGraphMarqueeHandler();
            var cellMaker = rdfGraphHandler.getCellMaker();
            var literal = new MR3Literal();
            literal.setString(testLiteralString);
            literalCell = cellMaker.insertRDFLiteral(new Point(100, 100), literal);
        }

        @Test
        void isRDFLiteralCell() {
            var expected = true;
            var actual = RDFGraph.isRDFLiteralCell(literalCell);
            assertEquals(expected, actual);
        }

        @Test
        void testInsertedRDFLiteral() {
            var expected = testLiteralString;
            var literal = (MR3Literal) GraphConstants.getValue(literalCell.getAttributes());
            var actual = literal.getString();
            assertEquals(expected, actual);
        }

    }

    @Nested
    class InsertRDFSClassTest {
        GraphCell rdfsClassCell;
        String testClassURI = DEFAULT_URI + "test_class";

        @BeforeEach
        void setUp() {
            var project = MR3.getCurrentProject();
            var classGraphHandler = project.getClassEditor().getClassGraphMarqueeHandler();
            var cellMaker = classGraphHandler.getCellMaker();
            rdfsClassCell = cellMaker.insertClass(new Point(50, 50), testClassURI);
        }

        @Test
        void isRDFSClassCell() {
            var expected = true;
            var actual = RDFGraph.isRDFSClassCell(rdfsClassCell);
            assertEquals(expected, actual);
        }

        @Test
        void testInsertedRDFSClassURI() {
            var expected = testClassURI;
            var model = (RDFSModel) GraphConstants.getValue(rdfsClassCell.getAttributes());
            var actual = model.getURIStr();
            assertEquals(expected, actual);
        }
    }

    @Nested
    class InsertRDFSPropertyTest {
        GraphCell rdfsPropertyCell;
        String testRDFSPropertyURI = DEFAULT_URI + "test_property";

        @BeforeEach
        void setUp() {
            var project = MR3.getCurrentProject();
            var propertyGraphHandler = project.getPropertyEditor().getPropertyGraphMarqueeHandler();
            var cellMaker = propertyGraphHandler.getCellMaker();
            rdfsPropertyCell = cellMaker.insertProperty(new Point(50, 50), testRDFSPropertyURI);
        }

        @Test
        void isRDFSPropertyCell() {
            var expected = true;
            var actual = RDFGraph.isRDFSPropertyCell(rdfsPropertyCell);
            assertEquals(expected, actual);
        }

        @Test
        void testInsertedRDFSPropertyURI() {
            var expected = testRDFSPropertyURI;
            var model = (RDFSModel) GraphConstants.getValue(rdfsPropertyCell.getAttributes());
            var actual = model.getURIStr();
            assertEquals(expected, actual);
        }
    }

}