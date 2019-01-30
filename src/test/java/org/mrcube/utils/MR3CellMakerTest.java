package org.mrcube.utils;

import org.jgraph.graph.GraphCell;
import org.jgraph.graph.GraphConstants;
import org.junit.jupiter.api.*;
import org.mrcube.MR3;
import org.mrcube.jgraph.RDFGraph;
import org.mrcube.models.MR3Constants;
import org.mrcube.models.MR3Literal;
import org.mrcube.models.RDFResourceModel;
import org.mrcube.models.RDFSModel;

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

        GraphCell cell;
        String testURI = DEFAULT_URI + "test_resource";

        @BeforeEach
        void setUp() {
            var project = MR3.getCurrentProject();
            var rdfGraphHandler = project.getRDFEditor().getRdfGraphMarqueeHandler();
            var cellMaker = rdfGraphHandler.getCellMaker();
            cell = cellMaker.insertRDFResource(new Point(30, 30), testURI, null, MR3Constants.URIType.URI);
        }

        @Test
        void isRDFResourceCell() {
            var expected = true;
            var actual = RDFGraph.isRDFResourceCell(cell);
            assertEquals(expected, actual);
        }

        @Test
        void testInsertedRDFResourceURI() {
            var expected = testURI;
            var model = (RDFResourceModel) GraphConstants.getValue(cell.getAttributes());
            var actual = model.getURIStr();
            assertEquals(expected, actual);
        }

    }

    @Nested
    class InsertRDFPropertyTest {

        GraphCell cell;
        String testURI = DEFAULT_URI + "test_property";

        @BeforeEach
        void setUp() {
            var project = MR3.getCurrentProject();
            var rdfGraphHandler = project.getRDFEditor().getRdfGraphMarqueeHandler();
            var cellMaker = rdfGraphHandler.getCellMaker();
            String testURI1 = DEFAULT_URI + "test_resource1";
            String testURI2 = DEFAULT_URI + "test_resource2";
            var cell1 = cellMaker.insertRDFResource(new Point(30, 30), testURI1, null, MR3Constants.URIType.URI);
            var cell2 = cellMaker.insertRDFResource(new Point(80, 30), testURI2, null, MR3Constants.URIType.URI);
            // TODO refactoring and implement MR3CellMaker#connect(cell1, cell2)
        }

        @Test
        void isRDFPropertyCell() {
            // TODO implement
            var expected = true;
//            var actual = RDFGraph.isRDFPropertyCell(cell);
            var actual = false;
            assertEquals(expected, actual);
        }

        @Test
        void testInsertedRDFResourceURI() {
            // TODO implement
            var expected = testURI;
//            var model = (RDFResourceModel) GraphConstants.getValue(cell.getAttributes());
//            var actual = model.getURIStr();
            var actual = "";
            assertEquals(expected, actual);
        }

    }


    @Nested
    class InsertRDFLiteralTest {
        GraphCell cell;
        String testLiteralString = "Test";

        @BeforeEach
        void setUp() {
            var project = MR3.getCurrentProject();
            var rdfGraphHandler = project.getRDFEditor().getRdfGraphMarqueeHandler();
            var cellMaker = rdfGraphHandler.getCellMaker();
            var literal = new MR3Literal();
            literal.setString(testLiteralString);
            cell = cellMaker.insertRDFLiteral(new Point(100, 100), literal);
        }

        @Test
        void isRDFLiteralCell() {
            var expected = true;
            var actual = RDFGraph.isRDFLiteralCell(cell);
            assertEquals(expected, actual);
        }

        @Test
        void testInsertedRDFLiteral() {
            var expected = testLiteralString;
            var literal = (MR3Literal) GraphConstants.getValue(cell.getAttributes());
            var actual = literal.getString();
            assertEquals(expected, actual);
        }

    }

    @Nested
    class InsertRDFSClassTest {
        GraphCell cell;
        String testURI = DEFAULT_URI + "test_class";

        @BeforeEach
        void setUp() {
            var project = MR3.getCurrentProject();
            var classGraphHandler = project.getClassEditor().getClassGraphMarqueeHandler();
            var cellMaker = classGraphHandler.getCellMaker();
            cell = cellMaker.insertClass(new Point(50, 50), testURI);
        }

        @Test
        void isRDFSClassCell() {
            var expected = true;
            var actual = RDFGraph.isRDFSClassCell(cell);
            assertEquals(expected, actual);
        }

        @Test
        void testInsertedRDFSClassURI() {
            var expected = testURI;
            var model = (RDFSModel) GraphConstants.getValue(cell.getAttributes());
            var actual = model.getURIStr();
            assertEquals(expected, actual);
        }
    }

    @Nested
    class InsertRDFSPropertyTest {
        GraphCell cell;
        String testURI = DEFAULT_URI + "test_property";

        @BeforeEach
        void setUp() {
            var project = MR3.getCurrentProject();
            var propertyGraphHandler = project.getPropertyEditor().getPropertyGraphMarqueeHandler();
            var cellMaker = propertyGraphHandler.getCellMaker();
            cell = cellMaker.insertProperty(new Point(50, 50), testURI);
        }

        @Test
        void isRDFSPropertyCell() {
            var expected = true;
            var actual = RDFGraph.isRDFSPropertyCell(cell);
            assertEquals(expected, actual);
        }

        @Test
        void testInsertedRDFSPropertyURI() {
            var expected = testURI;
            var model = (RDFSModel) GraphConstants.getValue(cell.getAttributes());
            var actual = model.getURIStr();
            assertEquals(expected, actual);
        }
    }

}