/*
 * Project Name: MR^3 (Meta-Model Management based on RDFs Revision Reflection)
 * Project Website: http://mrcube.org/
 *
 * Copyright (C) 2003-2018 Yamaguchi Laboratory, Keio University. All rights reserved.
 *
 * This file is part of MR^3.
 *
 * MR^3 is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * MR^3 is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with MR^3.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package org.mrcube.jgraph;

import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.vocabulary.OWL;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;
import org.jgraph.JGraph;
import org.jgraph.graph.*;
import org.mrcube.MR3;
import org.mrcube.MR3Project;
import org.mrcube.editors.ClassEditor;
import org.mrcube.editors.PropertyEditor;
import org.mrcube.editors.RDFEditor;
import org.mrcube.io.MR3Writer;
import org.mrcube.io.RDFSModelExtraction;
import org.mrcube.layout.GraphLayoutData;
import org.mrcube.layout.GraphLayoutUtilities;
import org.mrcube.layout.JGraphTreeLayout;
import org.mrcube.layout.VGJTreeLayout;
import org.mrcube.models.MR3Constants.CellViewType;
import org.mrcube.models.MR3Constants.GraphType;
import org.mrcube.models.MR3Constants.HistoryType;
import org.mrcube.models.*;
import org.mrcube.utils.GraphUtilities;
import org.mrcube.utils.MR3CellMaker;
import org.mrcube.utils.Translator;
import org.mrcube.utils.Utilities;
import org.mrcube.views.*;
import org.mrcube.views.FindResourceDialog.FindActionType;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.lang.ref.WeakReference;
import java.util.List;
import java.util.*;
import java.util.Map.Entry;
import java.util.prefs.Preferences;

/**
 * @author Takeshi Morita
 */
public class GraphManager {

    private Frame rootFrame;
    private JTabbedPane desktopTabbedPane;

    private MR3Writer mr3Writer;

    private RDFGraph realRDFGraph;

    private boolean isImporting;
    private boolean isShowTypeCell;

    private MR3CellMaker cellMaker;
    private JGraphTreeLayout treeLayout;

    private WeakReference<AttributeDialog> attrDialogRef;
    private WeakReference<FindResourceDialog> findResDialogRef;
    private WeakReference<NameSpaceTableDialog> nsTableDialogRef;
    private WeakReference<RemoveDialog> removeDialogRef;

    public static CellViewType cellViewType;

    private String baseURI;
    private static Preferences userPrefs;

    private static final String WARNING = Translator.getString("Warning");

    public GraphManager(JTabbedPane desktop, Preferences prefs, Frame root) {
        desktopTabbedPane = desktop;
        rootFrame = root;
        userPrefs = prefs;
        attrDialogRef = new WeakReference<AttributeDialog>(null);
        findResDialogRef = new WeakReference<FindResourceDialog>(null);
        nsTableDialogRef = new WeakReference<NameSpaceTableDialog>(null);
        removeDialogRef = new WeakReference<RemoveDialog>(null);
        cellMaker = new MR3CellMaker(this);
        treeLayout = new JGraphTreeLayout(this);
        baseURI = userPrefs.get(PrefConstants.BaseURI, MR3Resource.getURI());
        GraphLayoutUtilities.setGraphManager(this);
        mr3Writer = new MR3Writer(this);
    }

    public MR3CellMaker getCellMaker() {
        return cellMaker;
    }

    public RDFSInfoMap getCurrentRDFSInfoMap() {
        MR3Project project = (MR3Project) desktopTabbedPane.getSelectedComponent();
        return project.getRDFSInfoMap();
    }

    public RDFEditor getCurrentRDFEditor() {
        MR3Project project = (MR3Project) desktopTabbedPane.getSelectedComponent();
        return project.getRDFEditor();
    }

    public ClassEditor getCurrentClassEditor() {
        MR3Project project = (MR3Project) desktopTabbedPane.getSelectedComponent();
        return project.getClassEditor();
    }

    public PropertyEditor getCurrentPropertyEditor() {
        MR3Project project = (MR3Project) desktopTabbedPane.getSelectedComponent();
        return project.getPropertyEditor();
    }

    public RDFGraph getCurrentRDFGraph() {
        MR3Project project = (MR3Project) desktopTabbedPane.getSelectedComponent();
        if (project != null) {
            return (RDFGraph) project.getRDFEditor().getGraph();
        } else {
            return null;
        }
    }

    public RDFGraph getCurrentPropertyGraph() {
        MR3Project project = (MR3Project) desktopTabbedPane.getSelectedComponent();
        if (project != null) {
            return (RDFGraph) project.getPropertyEditor().getGraph();
        } else {
            return null;
        }
    }

    public RDFGraph getCurrentClassGraph() {
        MR3Project project = (MR3Project) desktopTabbedPane.getSelectedComponent();
        if (project != null) {
            return (RDFGraph) project.getClassEditor().getGraph();
        } else {
            return null;
        }
    }

    public JTabbedPane getDesktopTabbedPane() {
        return desktopTabbedPane;
    }

    public Frame getRootFrame() {
        return rootFrame;
    }

    public Preferences getUserPrefs() {
        return userPrefs;
    }

    public boolean isImporting() {
        return isImporting;
    }

    // private OntTreeEditor rdfsTreeEditor;

    // public void setRDFSTreeEditor(OntTreeEditor editor) {
    // rdfsTreeEditor = editor;
    // }
    //
    // public OntTreeEditor getRDFSTreeEditor() {
    // return rdfsTreeEditor;
    // }

    private void setVisibleEditors(boolean t) {
        getCurrentRDFEditor().setVisible(!t);
        getCurrentClassEditor().setVisible(!t);
        getCurrentPropertyEditor().setVisible(!t);
        // rdfsTreeEditor.setVisible(!t);
    }

    public void importing(boolean t) {
        isImporting = t;
        if (t) {
            setVisibleEditors(t);
            removeTypeCells();
            MR3.STATUS_BAR.startTime();
        } else {
            if (isShowTypeCell()) {
                addTypeCells();
            }
            Object rootCell = getPropertyCell(MR3Resource.Property);
            if (rootCell != null) {
                RDFGraph propGraph = getCurrentPropertyGraph();
                propGraph.removeCellsWithEdges(propGraph.getDescendants(new Object[]{rootCell}));
            }
            setVisibleEditors(t);
            MR3.STATUS_BAR.setCurrentTime();
        }
    }

    public void showRDFPropertyLabel(boolean t) {
        RDFGraph rdfGraph = getCurrentRDFGraph();
        Object[] cells = rdfGraph.getAllCells();
        for (int i = 0; i < cells.length; i++) {
            if (RDFGraph.isRDFPropertyCell(cells[i])) {
                GraphCell cell = (GraphCell) cells[i];
                if (t) {
                    GraphConstants.setFont(cell.getAttributes(), GraphUtilities.defaultFont);
                } else {
                    GraphConstants.setFont(cell.getAttributes(), new Font("", 0, 0));
                }
                rdfGraph.getGraphLayoutCache().editCell(cell, cell.getAttributes());
            }
        }
    }

    public boolean isAutoNodeSize() {
        String nodeSize = userPrefs.get(PrefConstants.NODE_SIZE, PrefConstants.NODE_SIZE_AUTO);
        return nodeSize.equals(PrefConstants.NODE_SIZE_AUTO);
    }

    public boolean isShowTypeCell() {
        return isShowTypeCell;
    }

    public static boolean isLogAvailable() {
        return userPrefs.get(PrefConstants.isLogAvailable, "false").equals("true");
    }

    public void setIsShowTypeCell(boolean t) {
        isShowTypeCell = t;
    }

    public RDFGraph getRealRDFGraph() {
        return realRDFGraph;
    }

    public boolean isRDFGraph(Object graph) {
        return graph == getCurrentRDFGraph();
    }

    public boolean isClassGraph(Object graph) {
        return graph == getCurrentClassGraph();
    }

    public boolean isPropertyGraph(Object graph) {
        return graph == getCurrentPropertyGraph();
    }

    public RDFGraph getGraph(GraphType type) {
        if (type == GraphType.RDF) {
            return getCurrentRDFGraph();
        } else if (type == GraphType.CLASS) {
            return getCurrentClassGraph();
        } else if (type == GraphType.PROPERTY) {
            return getCurrentPropertyGraph();
        }
        return null;
    }

    public void clearSelection() {
        getCurrentRDFGraph().clearSelection();
        getCurrentClassGraph().clearSelection();
        getCurrentPropertyGraph().clearSelection();
    }

    public void setGraphBackground(Color color) {
        RDFGraph currentRDFGraph = getCurrentRDFGraph();
        if (currentRDFGraph != null) {
            currentRDFGraph.setBackground(color);
        }
        RDFGraph currentClassGraph = getCurrentClassGraph();
        if (currentClassGraph != null) {
            currentClassGraph.setBackground(color);
        }
        RDFGraph currentPropertyGraph = getCurrentPropertyGraph();
        if (currentPropertyGraph != null) {
            currentPropertyGraph.setBackground(color);
        }
    }

    private Set<Resource> getMetaClassList(String[] list) {
        Set<Resource> metaClassList = new HashSet<Resource>();
        for (int i = 0; i < list.length; i++) {
            metaClassList.add(ResourceFactory.createResource(list[i]));
        }
        return metaClassList;
    }

    public static String getDefaultLang() {
        return userPrefs.get(PrefConstants.DefaultLang, "ja");
    }

    public void setDefaultLang(String lang) {
        userPrefs.put(PrefConstants.DefaultLang, lang);
    }

    public String getWorkDirectory() {
        return userPrefs.get(PrefConstants.WorkDirectory, "");
    }

    public void setResourceContainer(DefaultListModel historyBox) {
        StringBuilder history = new StringBuilder();
        for (int i = 0; i < historyBox.getSize(); i++) {
            history.append(historyBox.getElementAt(i).toString() + ",");
        }
        userPrefs.put(PrefConstants.ResourceContainer, history.toString());
    }

    public String[] getResourceContainer() {
        return userPrefs.get(PrefConstants.ResourceContainer, "").split(",");
    }

    public static final String CLASS_CLASS_LIST = RDFS.Class.toString() + " " + OWL.Class.toString();

    public String getDefaultClassClass() {
        return userPrefs.get(PrefConstants.DefaultClassClass, RDFS.Class.getURI());
    }

    public Set<Resource> getClassClassList() {
        return getMetaClassList(userPrefs.get(PrefConstants.ClassClassList, CLASS_CLASS_LIST).split(" "));
    }

    public static final String PROPERTY_CLASS_LIST = RDF.Property.toString() + " " + OWL.ObjectProperty.toString()
            + " " + OWL.DatatypeProperty.toString();

    public String getDefaultPropertyClass() {
        return userPrefs.get(PrefConstants.DefaultPropertyClass, RDF.Property.getURI());
    }

    public Set<Resource> getPropertyClassList() {
        return getMetaClassList(userPrefs.get(PrefConstants.PropClassList, PROPERTY_CLASS_LIST).split(" "));
    }

    public Set<Resource> getMetaClassList() {
        Set<Resource> metaClassList = getClassClassList();
        metaClassList.addAll(getPropertyClassList());
        return metaClassList;
    }

    public void findMetaClass(Model orgModel, Resource supClass, Set<Resource> set) {
        for (ResIterator i = orgModel.listSubjectsWithProperty(RDFS.subClassOf, supClass); i.hasNext(); ) {
            Resource subject = i.nextResource();
            set.add(subject);
            findMetaClass(orgModel, subject, set);
        }
    }

    public void setClassClassList(Set<Resource> classClassList) {
        String classClassListStr = "";
        for (Resource classClass : classClassList) {
            classClassListStr += classClass.toString() + " ";
        }
        userPrefs.put(PrefConstants.ClassClassList, classClassListStr);
    }

    public void setPropertyClassList(Set<Resource> propClassList) {
        String propClassListStr = "";
        for (Resource propClass : propClassList) {
            propClassListStr += propClass.toString() + " ";
        }
        userPrefs.put(PrefConstants.PropClassList, propClassListStr);
    }

    public void setAntialias() {
        boolean isAntialias = userPrefs.getBoolean(PrefConstants.Antialias, true);
        if (getCurrentRDFGraph() != null) {
            getCurrentRDFGraph().setAntiAliased(isAntialias);
            getCurrentClassGraph().setAntiAliased(isAntialias);
            getCurrentPropertyGraph().setAntiAliased(isAntialias);
        }
    }

    public String getBaseURI() {
        return baseURI;
    }

    public void setBaseURI(String base) {
        baseURI = base;
    }

    public Set<String> getClassSet() {
        Set<String> classSet = new HashSet<String>();
        for (Object cell : getCurrentClassGraph().getAllCells()) {
            if (RDFGraph.isRDFSClassCell(cell)) {
                RDFSInfo info = (RDFSInfo) GraphConstants.getValue(((GraphCell) cell).getAttributes());
                classSet.add(info.getURIStr());
            }
        }
        return classSet;
    }

    public Set<String> getPropertySet() {
        Set<String> propertySet = new HashSet<String>();
        for (Object cell : getCurrentPropertyGraph().getAllCells()) {
            if (RDFGraph.isRDFSPropertyCell(cell)) {
                RDFSInfo info = (RDFSInfo) GraphConstants.getValue(((GraphCell) cell).getAttributes());
                propertySet.add(info.getURIStr());
            }
        }
        return propertySet;
    }

    public void resetTypeCells() {
        if (isShowTypeCell) {
            removeTypeCells();
            addTypeCells();
        }
    }

    public void addTypeCells() {
        if (!isShowTypeCell) {
            clearSelection();
            return;
        }
        for (Object rdfCell : getCurrentRDFGraph().getAllCells()) {
            if (RDFGraph.isRDFResourceCell(rdfCell)) {
                cellMaker.addTypeCell((GraphCell) rdfCell, new AttributeMap());
            }
        }
        clearSelection();
    }

    public void setAutoSize(boolean t) {
        for (Object p : desktopTabbedPane.getComponents()) {
            if (p instanceof MR3Project) {
                MR3Project mr3Project = (MR3Project) p;
                RDFGraph rdfGraph = (RDFGraph) mr3Project.getRDFEditor().getGraph();
                rdfGraph.setAutoSize(t);
                RDFGraph classGraph = (RDFGraph) mr3Project.getClassEditor().getGraph();
                classGraph.setAutoSize(t);
                RDFGraph propertyGraph = (RDFGraph) mr3Project.getPropertyEditor().getGraph();
                propertyGraph.setAutoSize(t);
            }
        }
    }

    public void removeTypeCells() {
        Object[] rdfCells = getCurrentRDFGraph().getAllCells();
        List<GraphCell> typeCellList = new ArrayList<>();
        for (int i = 0; i < rdfCells.length; i++) {
            GraphCell cell = (GraphCell) rdfCells[i];
            if (RDFGraph.isTypeCell(cell)) {
                typeCellList.add(cell);
            }
            //	else if (cell.getClass().equals(DefaultGraphCell.class)) {
            //		typeCellList.add(cell);
            //	}
        }
        getCurrentRDFGraph().removeCellsWithEdges(typeCellList.toArray());
    }

    private boolean isRDFResourceDuplicated(String uri, Object cell, GraphType type) {
        Object[] rdfCells = getCurrentRDFGraph().getAllCells();
        for (int i = 0; i < rdfCells.length; i++) {
            GraphCell rdfCell = (GraphCell) rdfCells[i];
            if (rdfCell instanceof RDFResourceCell) {
                RDFResourceInfo resInfo = (RDFResourceInfo) GraphConstants.getValue(rdfCell.getAttributes());
                if (resInfo == null) {
                    resInfo = (RDFResourceInfo) GraphConstants.getValue(rdfCell.getAttributes());
                }
                String tmpURI = resInfo.getURIStr();
                if (tmpURI.equals(uri) && rdfCell != cell) {
                    if (resInfo.getTypeCell() == null) {
                        return true;
                    }
                    RDFSInfo rdfsInfo = resInfo.getTypeInfo();
                    // RDFエディタ内のクラス定義は，重複とみなさないようにする．
                    return (!(type == GraphType.CLASS && rdfsInfo.getURI().equals(RDFS.Class) || type == GraphType.PROPERTY
                            && rdfsInfo.getURI().equals(RDF.Property)));
                }
            }
        }

        // Collection entrySet = resInfoMap.entrySet();
        // for (Iterator i = entrySet.iterator(); i.hasNext();) {
        // Map.Entry entry = (Map.Entry) i.next();
        // Object infoCell = entry.getKey();
        // RDFResourceInfo resInfo = (RDFResourceInfo) entry.getValue();
        // String tmpURI = resInfo.getURIStr();
        //
        // if (tmpURI.equals(uri) && infoCell != cell) {
        // // RDFSInfo rdfsInfo =
        // // rdfsInfoMap.getCellInfo(resInfo.getTypeCell());
        // RDFSInfo rdfsInfo = (RDFSInfo) GraphConstants.getValue(((GraphCell)
        // resInfo.getTypeCell())
        // .getAttributes());
        // /*
        // * RDFエディタ内のクラス定義は，重複とみなさないようにする．
        // */
        // if (!(type == GraphType.CLASS && rdfsInfo != null &&
        // rdfsInfo.getURI().equals(RDFS.Class) || type == GraphType.PROPERTY
        // && rdfsInfo != null && rdfsInfo.getURI().equals(RDF.Property))) {
        // return true; }
        // }
        // }
        return false;
    }

    public boolean isEmptyURI(String uri) {
        if (uri.equals("")) {
            JOptionPane.showMessageDialog(desktopTabbedPane, Translator.getString("Warning.Message4"), WARNING,
                    JOptionPane.ERROR_MESSAGE);
            return true;
        }
        return false;
    }

    public boolean isDuplicatedWithDialog(String uri, Object cell, GraphType type) {
        if (isDuplicated(uri, cell, type)) {
            JOptionPane.showMessageDialog(getGraph(type), Translator.getString("Warning.Message1"), WARNING,
                    JOptionPane.ERROR_MESSAGE);
            return true;
        }
        return false;
    }

    public boolean isDuplicated(String uri, Object cell, GraphType type) {
        return (getCurrentRDFSInfoMap().isDuplicated(uri, cell, type) || isRDFResourceDuplicated(uri, cell, type));
    }

    /**
     * 名前空間のリストを返す
     */
    public Set<String> getNameSpaceSet(GraphType type) {
        if (type == GraphType.RDF) {
            return getRDFNameSpaceSet();
        } else if (type == GraphType.CLASS) {
            return getClassNameSpaceSet();
        } else if (type == GraphType.PROPERTY) {
            return getPropertyNameSpaceSet();
        }
        return new HashSet<String>();
    }

    public Set<String> getAllNameSpaceSet() {
        Set<String> allNSSet = new HashSet<String>();
        allNSSet.addAll(getRDFNameSpaceSet());
        allNSSet.addAll(getClassNameSpaceSet());
        allNSSet.addAll(getPropertyNameSpaceSet());

        return allNSSet;
    }

    public Set<String> getRDFNameSpaceSet() {
        Set<String> nameSpaces = new HashSet<String>();

        Object[] rdfCells = getCurrentRDFGraph().getAllCells();
        for (int i = 0; i < rdfCells.length; i++) {
            GraphCell cell = (GraphCell) rdfCells[i];
            if (RDFGraph.isRDFResourceCell(cell)) {
                RDFResourceInfo info = (RDFResourceInfo) GraphConstants.getValue(cell.getAttributes());
                Resource uri = info.getURI();
                nameSpaces.add(uri.getNameSpace());
            }
        }
        return nameSpaces;
    }

    public Set<String> getClassNameSpaceSet() {
        Set<String> nameSpaces = new HashSet<String>();

        Object[] classCells = getCurrentClassGraph().getAllCells();
        for (int i = 0; i < classCells.length; i++) {
            GraphCell cell = (GraphCell) classCells[i];
            if (RDFGraph.isRDFSClassCell(cell)) {
                RDFSInfo info = (RDFSInfo) GraphConstants.getValue(cell.getAttributes());
                Resource uri = info.getURI();
                nameSpaces.add(uri.getNameSpace());
            }
        }
        return nameSpaces;
    }

    public Set<String> getPropertyNameSpaceSet() {
        Set<String> nameSpaces = new HashSet<String>();
        for (Object cell : getCurrentPropertyGraph().getAllCells()) {
            if (RDFGraph.isRDFSPropertyCell(cell)) {
                RDFSInfo info = (RDFSInfo) GraphConstants.getValue(((GraphCell) cell).getAttributes());
                Resource uri = info.getURI();
                nameSpaces.add(uri.getNameSpace());
            }
        }
        return nameSpaces;
    }

    public void setNodeBounds(Map<Resource, MR3Literal> uriNodeInfoMap) {
        MR3.STATUS_BAR.initNormal(uriNodeInfoMap.keySet().size());
        RDFSInfoMap rdfsInfoMap = getCurrentRDFSInfoMap();

        for (Entry<Resource, MR3Literal> entry : uriNodeInfoMap.entrySet()) {
            Resource uri = entry.getKey();
            MR3Literal rec = entry.getValue();

            GraphCell cell = (GraphCell) getRDFResourceCell(uri);
            if (cell != null) {
                continue;
            } else if (rdfsInfoMap.isClassCell(uri)) {
                continue;
            } else if (rdfsInfoMap.isPropertyCell(uri)) {
                continue;
            } else if (uri.getURI().matches(MR3Resource.Literal.getURI() + ".*")) {
                MR3Literal literal = new MR3Literal(rec.getString(), rec.getLanguage(), rec.getDatatype());
                DefaultGraphCell litCell = (DefaultGraphCell) cellMaker.insertRDFLiteral(rec.getRectangle(), literal);
                DefaultGraphCell source = (DefaultGraphCell) getRDFResourceCell(rec.getResource());
                if (source == null) {
                    // System.out.println("s: " + source);
                    continue;
                }
                GraphCell propCell = (GraphCell) rdfsInfoMap.getPropertyCell(rec.getProperty());
                if (propCell != null) {
                    RDFSInfo info = (RDFSInfo) GraphConstants.getValue(propCell.getAttributes());
                    cellMaker.connect((Port) source.getChildAt(0), (Port) litCell.getChildAt(0), info,
                            getCurrentRDFGraph());
                }
            }
            MR3.STATUS_BAR.addValue();
        }
        MR3.STATUS_BAR.hideProgressBar();
    }

    private void setCellBounds(AttributeMap map, Dimension dim) {
        Rectangle2D rec = GraphConstants.getBounds(map);
        map.createRect(rec.getX(), rec.getY(), dim.getWidth(), dim.getHeight());
    }

    public void setCellValue(GraphCell cell, String value) {
        AttributeMap map = cell.getAttributes();
        GraphConstants.setValue(map, value);
        if (isAutoNodeSize()) {
            Dimension dim = null;
            if (RDFGraph.isRDFLiteralCell(cell)) {
                dim = GraphUtilities.getAutoLiteralNodeDimention(this, value);
            } else if (RDFGraph.isRDFResourceCell(cell)) {
                dim = GraphUtilities.getAutoNodeDimension(this, value);
                if (isShowTypeCell) {
                    RDFResourceInfo info = (RDFResourceInfo) GraphConstants.getValue(cell.getAttributes());
                    RDFSInfo typeInfo = (RDFSInfo) GraphConstants.getValue(info.getTypeCell().getAttributes());
                    Dimension typeDim = GraphUtilities.getAutoNodeDimension(this,
                            getRDFSNodeValue(info.getType(), typeInfo));

                    if (dim.getWidth() < typeDim.getWidth()) {
                        dim = typeDim;
                    }

                    GraphCell typeCell = (GraphCell) info.getTypeViewCell();
                    if (typeCell != null) {
                        setCellBounds(typeCell.getAttributes(), dim);
                    }
                }
            } else {
                dim = GraphUtilities.getAutoNodeDimension(this, value);
            }
            if (!GraphUtilities.isColor && RDFGraph.isRDFSCell(cell)) {
                GraphConstants.setBorderColor(map, GraphUtilities.graphBackgroundColor);
            }
            setCellBounds(map, dim);
        }
        cell.getAttributes().applyMap(map);
    }

    public String getRDFSNodeValue(Resource uri, RDFSInfo info) {
        switch (cellViewType) {
            case URI:
                return GraphUtilities.getNSPrefix(uri);
            case ID:
                if (uri.getLocalName().length() != 0) {
                    return uri.getLocalName();
                }
                break;
            case LABEL:
                if (info != null) {
                    if (info.getDefaultLabel(getDefaultLang()) != null) {
                        return info.getDefaultLabel(getDefaultLang()).getString();
                    } else if (info.getFirstLabel() != null) {
                        return info.getFirstLabel().getString();
                    }
                }
                break;
        }
        return GraphUtilities.getNSPrefix(uri);
    }

    public String getRDFNodeValue(Resource uri, ResourceInfo info) {
        if (uri.isAnon()) {
            return "";
        }
        switch (cellViewType) {
            case URI:
                return GraphUtilities.getNSPrefix(uri);
            case LABEL:
                if (info != null) {
                    if (info.getDefaultLabel(getDefaultLang()) != null) {
                        return info.getDefaultLabel(getDefaultLang()).getString();
                    } else if (info.getFirstLabel() != null) {
                        return info.getFirstLabel().getString();
                    }
                }
                break;
            case ID:
                if (uri.getLocalName().length() != 0) {
                    return Utilities.getLocalName(uri);
                }
                break;
        }
        return GraphUtilities.getNSPrefix(uri);
    }

    private Set<Resource> getSolitudeCells(RDFGraph graph) {
        Object[] cells = graph.getAllCells();
        Set<Resource> region = new HashSet<Resource>();
        for (int i = 0; i < cells.length; i++) {
            DefaultGraphCell cell = (DefaultGraphCell) cells[i];
            if (RDFGraph.isRDFSClassCell(cells[i])) {
                DefaultPort port = (DefaultPort) cell.getChildAt(0);
                if (port.getEdges().isEmpty()) {
                    RDFSInfo info = (RDFSInfo) GraphConstants.getValue(cell.getAttributes());
                    region.add(info.getURI());
                }
            }
        }
        return region;
    }

    public List<GraphCell> getPropertyList() {
        List<GraphCell> list = new ArrayList<GraphCell>();
        Object[] cells = getCurrentPropertyGraph().getAllCells();
        for (int i = 0; i < cells.length; i++) {
            GraphCell cell = (GraphCell) cells[i];
            if (RDFGraph.isRDFSPropertyCell(cell)) {
                list.add(cell);
            }
        }
        return list;
    }

    public Object getRDFResourceCell(Resource uri) {
        Object[] cells = getCurrentRDFGraph().getAllCells();
        for (int i = 0; i < cells.length; i++) {
            if (RDFGraph.isRDFResourceCell(cells[i])) {
                GraphCell cell = (GraphCell) cells[i];
                RDFResourceInfo info = (RDFResourceInfo) GraphConstants.getValue(cell.getAttributes());
                /*
                 * Resourceクラスのインスタンスをequalsで比べるとisAnon()
                 * によって，true,falseを決められる．文字列を保存しているuri
                 * は，isAnonは必ずfalseとなるため，文字列比較をするために toStringを用いている．
                 */
                if (info.getURI().toString().equals(uri.toString())) {
                    return cells[i];
                }
            }
        }
        return null;
    }

    public Object getRDFPropertyCell(Resource uri) {
        Object[] cells = getCurrentRDFGraph().getAllCells();
        for (int i = 0; i < cells.length; i++) {
            if (RDFGraph.isRDFPropertyCell(cells[i])) {
                GraphCell cell = (GraphCell) cells[i];
                RDFSInfo info = (RDFSInfo) GraphConstants.getValue(cell.getAttributes());
                if (info == null) {
                    return null;
                }
                if (info.getURI().equals(uri)) {
                    return cell;
                }
            }
        }
        return null;
    }

    private static Point INSERT_POINT = new Point(50, 50);

    public Object getClassCell(Resource uri, Map<RDFNode, GraphLayoutData> cellLayoutMap) {
        RDFSInfoMap rdfsInfoMap = getCurrentRDFSInfoMap();
        GraphCell cell = rdfsInfoMap.getClassCell(uri);
        GraphLayoutData data = null;
        if (cellLayoutMap != null) {
            data = cellLayoutMap.get(uri);
        }
        if (cell != null) {
            moveCell(cell, data, getCurrentClassGraph());
            return cell;
        }
        if (data != null) {
            return cellMaker.insertClass(data.getRectangle(), uri.getURI());
        }
        return cellMaker.insertClass(INSERT_POINT, uri.getURI());
    }

    public Object getClassCell(Resource uri) {
        return getCurrentRDFSInfoMap().getClassCell(uri);
    }

    public GraphCell getClassCell(Resource uri, boolean isCheck) {
        GraphCell cell = getCurrentRDFSInfoMap().getClassCell(uri);
        if (cell != null) {
            return cell;
        } else if (isCheck && isDuplicated(uri.getURI(), null, getCurrentClassGraph().getType())) {
            return null;
        } else {
            return cellMaker.insertClass(INSERT_POINT, uri.getURI());
        }
    }

    public Set getSupRDFS(RDFGraph graph, String title) {
        if (graph.getAllCells().length == 0) {
            return new HashSet();
        }
        SelectRDFSDialog selectSupRDFSDialog = new SelectRDFSDialog(title, this);
        selectSupRDFSDialog.replaceGraph(graph);
        selectSupRDFSDialog.setRegionSet(new HashSet());
        selectSupRDFSDialog.setVisible(true);
        return (Set) selectSupRDFSDialog.getValue();
    }

    public Object insertSubRDFS(Resource uri, Set supRDFS, RDFGraph graph) {
        Point2D point = cellMaker.calcInsertPoint(supRDFS);
        DefaultGraphCell cell = null;
        if (isClassGraph(graph)) {
            cell = cellMaker.insertClass(point, uri.getURI());
        } else if (isPropertyGraph(graph)) {
            cell = cellMaker.insertProperty(point, uri.getURI());
        }
        Port sourcePort = (Port) cell.getChildAt(0);
        if (supRDFS != null) {
            Object[] supCells = graph.getDescendants(supRDFS.toArray());
            cellMaker.connectSubToSups(sourcePort, supCells, graph);
        }
        return cell;
    }

    public Object getPropertyCell(Resource uri, Map<RDFNode, GraphLayoutData> cellLayoutMap) {
        GraphCell cell = (GraphCell) getCurrentRDFSInfoMap().getPropertyCell(uri);
        GraphLayoutData data = null;
        if (cellLayoutMap != null) {
            data = cellLayoutMap.get(uri);
        }
        if (cell != null) {
            moveCell(cell, data, getCurrentPropertyGraph());
            return cell;
        }
        if (data != null) {
            return cellMaker.insertProperty(data.getRectangle(), uri.getURI());
        }
        return cellMaker.insertProperty(INSERT_POINT, uri.getURI());
    }

    public Object getPropertyCell(Resource uri) {
        return getCurrentRDFSInfoMap().getPropertyCell(uri);
    }

    public GraphCell getPropertyCell(Resource uri, boolean isCheck) {
        GraphCell cell = (GraphCell) getCurrentRDFSInfoMap().getPropertyCell(uri);
        if (cell != null) {
            return cell;
        } else if (isCheck && isDuplicated(uri.getURI(), null, getCurrentPropertyGraph().getType())) {
            return null;
        } else {
            if (MR3.OFF_META_MODEL_MANAGEMENT) {
                return null;
            }
            return cellMaker.insertProperty(INSERT_POINT, uri.getURI());
        }
    }

    private boolean isMatches(String key, Resource uri) {
        return uri.getURI() != null && uri.getURI().indexOf(key) != -1;
    }

    public Set getClassInstanceSet(Object type) {
        Set<Object> instanceSet = new HashSet<Object>();
        Object[] cells = getCurrentRDFGraph().getAllCells();
        for (int i = 0; i < cells.length; i++) {
            if (RDFGraph.isRDFResourceCell(cells[i])) {
                GraphCell cell = (GraphCell) cells[i];
                RDFResourceInfo info = (RDFResourceInfo) GraphConstants.getValue(cell.getAttributes());
                if (info.getTypeCell() == type) {
                    instanceSet.add(cell);
                }
            }
        }
        return instanceSet;
    }

    public Set<RDFResourceInfo> getClassInstanceInfoSet(RDFSInfo clsInfo) {
        Set<RDFResourceInfo> instanceInfoSet = new HashSet<RDFResourceInfo>();
        Object[] cells = getCurrentRDFGraph().getAllCells();
        for (int i = 0; i < cells.length; i++) {
            if (RDFGraph.isRDFResourceCell(cells[i])) {
                GraphCell cell = (GraphCell) cells[i];
                RDFResourceInfo info = (RDFResourceInfo) GraphConstants.getValue(cell.getAttributes());
                if (info.getType().getURI().equals(clsInfo.getURIStr())) {
                    instanceInfoSet.add(info);
                }
            }
        }
        return instanceInfoSet;
    }

    public Set getPropertyInstanceSet(Object rdfsPropCell) {
        // TreeSet を使うためにはcompratorを適切に実装しないといけない．
        Set instanceSet = new HashSet();
        Object[] cells = getCurrentRDFGraph().getAllCells();
        RDFGraph rdfGraph = getCurrentRDFGraph();
        for (int i = 0; i < cells.length; i++) {
            GraphCell cell = (GraphCell) cells[i];
            if (RDFGraph.isRDFPropertyCell(cell)) {
                RDFSInfo info = (RDFSInfo) GraphConstants.getValue(cell.getAttributes());
                Object propCell = getPropertyCell(info.getURI());
                if (propCell == rdfsPropCell) {
                    Object sourceCell = rdfGraph.getSourceVertex(cell);
                    instanceSet.add(sourceCell);
                    // instanceSet.add(cell);
                }
            }
        }
        return instanceSet;
    }

    public Set getPropertyInstanceInfoSet(RDFSInfo propInfo) {
        // TreeSet を使うためにはcompratorを適切に実装しないといけない．
        Set instanceSet = new HashSet();
        RDFGraph rdfGraph = getCurrentRDFGraph();
        Object[] cells = rdfGraph.getAllCells();
        for (int i = 0; i < cells.length; i++) {
            GraphCell cell = (GraphCell) cells[i];
            if (RDFGraph.isRDFPropertyCell(cell)) {
                RDFSInfo info = (RDFSInfo) GraphConstants.getValue(cell.getAttributes());
                Object propCell = getPropertyCell(info.getURI());
                if (info.getURIStr().equals(propInfo.getURIStr())) {
                    Object sourceCell = rdfGraph.getSourceVertex(cell);
                    instanceSet.add(sourceCell);
                    // instanceSet.add(cell);
                }
            }
        }
        return instanceSet;
    }

    public Set<GraphCell> getFindRDFResult(String key) {
        Set<GraphCell> result = new HashSet<GraphCell>();
        RDFGraph rdfGraph = getCurrentRDFGraph();
        Object[] cells = rdfGraph.getAllCells();
        for (int i = 0; i < cells.length; i++) {
            GraphCell cell = (GraphCell) cells[i];
            if (RDFGraph.isRDFResourceCell(cell)) {
                RDFResourceInfo info = (RDFResourceInfo) GraphConstants.getValue(cell.getAttributes());
                if (isMatches(key, info.getURI())) {
                    result.add(cell);
                }
            } else if (RDFGraph.isRDFPropertyCell(cell)) {
                RDFSInfo info = (RDFSInfo) GraphConstants.getValue(cell.getAttributes());
                if (isMatches(key, info.getURI())) {
                    result.add(cell);
                }
            }
        }
        return result;
    }

    public Set<GraphCell> getFindRDFResult(String key, FindResourceDialog.FindActionType findActionType) {
        Set<GraphCell> result = new HashSet<GraphCell>();
        RDFGraph rdfGraph = getCurrentRDFGraph();
        Object[] cells = rdfGraph.getAllCells();
        for (int i = 0; i < cells.length; i++) {
            GraphCell cell = (GraphCell) cells[i];
            List<MR3Literal> list = null;
            if (RDFGraph.isRDFResourceCell(cell)) {
                RDFResourceInfo info = (RDFResourceInfo) GraphConstants.getValue(cell.getAttributes());
                if (findActionType == FindResourceDialog.FindActionType.LABEL) {
                    list = info.getLabelList();
                } else if (findActionType == FindResourceDialog.FindActionType.COMMENT) {
                    list = info.getCommentList();
                }
                for (MR3Literal lit : list) {
                    if (lit.getString().indexOf(key) != -1) {
                        result.add(cell);
                    }
                }
            } else if (RDFGraph.isRDFPropertyCell(cell)) {
                RDFSInfo info = (RDFSInfo) GraphConstants.getValue(cell.getAttributes());
                if (findActionType == FindResourceDialog.FindActionType.LABEL) {
                    list = info.getLabelList();
                } else if (findActionType == FindResourceDialog.FindActionType.COMMENT) {
                    list = info.getCommentList();
                }
                for (MR3Literal lit : list) {
                    if (lit.getString().indexOf(key) != -1) {
                        result.add(cell);
                    }
                }
            }
        }
        return result;
    }

    public Set<GraphCell> getFindRDFSResult(String key, RDFGraph graph) {
        Set<GraphCell> result = new HashSet<GraphCell>();
        Object[] cells = graph.getAllCells();

        for (int i = 0; i < cells.length; i++) {
            if (RDFGraph.isRDFSCell(cells[i])) {
                GraphCell cell = (GraphCell) cells[i];
                RDFSInfo info = (RDFSInfo) GraphConstants.getValue(cell.getAttributes());
                if (isMatches(key, info.getURI())) {
                    result.add(cell);
                }
            }
        }
        return result;
    }

    public Set<GraphCell> getFindRDFSResult(String key, RDFGraph graph, FindActionType findActionType) {
        Set<GraphCell> result = new HashSet<GraphCell>();
        Object[] cells = graph.getAllCells();

        for (int i = 0; i < cells.length; i++) {
            if (RDFGraph.isRDFSCell(cells[i])) {
                GraphCell cell = (GraphCell) cells[i];
                RDFSInfo info = (RDFSInfo) GraphConstants.getValue(cell.getAttributes());
                List<MR3Literal> list = null;
                if (findActionType == FindResourceDialog.FindActionType.LABEL) {
                    list = info.getLabelList();
                } else if (findActionType == FindResourceDialog.FindActionType.COMMENT) {
                    list = info.getCommentList();
                }

                for (MR3Literal lit : list) {
                    if (lit.getString().indexOf(key) != -1) {
                        result.add(cell);
                    }
                }
            }
        }
        return result;
    }

    public Set<GraphCell> getFindClassResult(String key, FindActionType findActionType) {
        if (findActionType == FindActionType.URI) {
            return getFindRDFSResult(key, getCurrentClassGraph());
        }
        return getFindRDFSResult(key, getCurrentClassGraph(), findActionType);
    }

    public Set<GraphCell> getFindPropertyResult(String key, FindActionType findActionType) {
        if (findActionType == FindActionType.URI) {
            return getFindRDFSResult(key, getCurrentPropertyGraph());
        }
        return getFindRDFSResult(key, getCurrentPropertyGraph(), findActionType);
    }

    public void selectCell(Object cell, JGraph graph) {
        if (!graph.getModel().contains(cell)) {
            return;
        }
        graph.scrollCellToVisible(cell);
        if (graph == getCurrentRDFGraph()) {
            Object parent = graph.getModel().getParent(cell);
            if (parent == null) {
                graph.setSelectionCell(cell);
            } else {
                graph.setSelectionCell(parent);
            }
        } else {
            graph.setSelectionCell(cell);
        }
    }

    public void selectRDFCell(Object cell) {
        selectCell(cell, getCurrentRDFGraph());
    }

    public void selectClassCell(Object cell) {
        selectCell(cell, getCurrentClassGraph());
    }

    public void selectPropertyCell(Object cell) {
        selectCell(cell, getCurrentPropertyGraph());
    }

    private Map checkUnRemovablePropertyCells(Object[] cells, List notRmCells, Set notRmList, Object graph) {
        Map classPropMap = new HashMap();
        if (isPropertyGraph(graph)) {
            return classPropMap;
        }

        for (Object cell : cells) {
            Object[] propCells = getCurrentPropertyGraph().getAllCells();
            Set<GraphCell> propSet = new HashSet<GraphCell>();

            if (RDFGraph.isRDFSClassCell(cell)) { // 削除したクラスのセル
                // cellを参照していないかどうかすべてのプロパティに対して調べる
                // domainかrangeに含まれている可能性がある．
                for (int j = 0; j < propCells.length; j++) {
                    GraphCell propCell = (GraphCell) propCells[j];
                    if (RDFGraph.isRDFSPropertyCell(propCell)) {
                        PropertyInfo info = (PropertyInfo) GraphConstants.getValue(propCell.getAttributes());
                        Set domain = info.getDomain();
                        Set range = info.getRange();
                        if (domain.contains(cell) || range.contains(cell)) {
                            propSet.add(propCell);
                        }
                    }
                }
                classPropMap.put(cell, propSet);
                if (!propSet.isEmpty()) {
                    notRmCells.add(cell); // 削除することができないリストにcellを追加
                    notRmList.add(cell); // JListでnullを表示しないようにするため
                    List children = ((DefaultGraphCell) cell).getChildren();
                    notRmCells.addAll(children);
                }
            }
        }
        return classPropMap;
    }

    private Map checkUnRemovableRDFCells(Object[] cells, List notRmCells, Set notRmList) {
        Map classRDFMap = new HashMap();

        for (Object cell : cells) {
            Object[] rdfCells = getCurrentRDFGraph().getAllCells();
            Set<GraphCell> rdfSet = new HashSet<GraphCell>();
            for (int j = 0; j < rdfCells.length; j++) {
                GraphCell rdfCell = (GraphCell) rdfCells[j];
                if (RDFGraph.isRDFResourceCell(rdfCell)) {
                    RDFResourceInfo rdfInfo = (RDFResourceInfo) GraphConstants.getValue(rdfCell.getAttributes());
                    if (rdfInfo.getTypeCell() == cell) {
                        rdfSet.add(rdfCell);
                    }
                } else if (RDFGraph.isEdge(rdfCell)) {
                    RDFSInfo info = (RDFSInfo) GraphConstants.getValue(rdfCell.getAttributes());
                    Object propCell = getCurrentRDFSInfoMap().getPropertyCell(info.getURI());
                    if (propCell == cell) {
                        rdfSet.add(rdfCell);
                    }
                }
            }

            classRDFMap.put(cell, rdfSet);
            if (!rdfSet.isEmpty()) {
                notRmCells.add(cell); // 削除することができないリストにcellを追加
                notRmList.add(cell); // JListでnullを表示しないようにするため
                List children = ((DefaultGraphCell) cell).getChildren();
                notRmCells.addAll(children);
            }
        }

        return classRDFMap;
    }

    public void setEnabled(boolean t) {
        getCurrentRDFGraph().setEnabled(t);
        getCurrentClassGraph().setEnabled(t);
        getCurrentPropertyGraph().setEnabled(t);
    }

    private Object[] removeCells;
    private RDFGraph removeGraph;

    public void retryRemoveCells() {
        removeCells();
    }

    public Object[] getRemoveCells() {
        return removeCells;
    }

    public boolean removeAction(RDFGraph graph) {
        removeGraph = graph;
        removeCells = graph.getAllSelectedCells();
        return removeCells();
    }

    public void repaintRDFGraph() {
        RDFGraph rdfGraph = getCurrentRDFGraph();
        rdfGraph.getGraphLayoutCache().reload();
        rdfGraph.repaint();
    }

    public void repaintClassGrpah() {
        RDFGraph classGraph = getCurrentClassGraph();
        classGraph.getGraphLayoutCache().reload();
        classGraph.repaint();
    }

    public void repaintPropertyGraph() {
        RDFGraph propGraph = getCurrentPropertyGraph();
        propGraph.getGraphLayoutCache().reload();
        propGraph.repaint();
    }

    private void repaintAllGraphs() {
        repaintRDFGraph();
        repaintClassGrpah();
        repaintPropertyGraph();
    }

    private void saveRemoveLog() {
        if (isClassGraph(removeGraph)) {
            HistoryManager.saveHistory(HistoryType.DELETE_CLASS, removeCells);
        } else if (isPropertyGraph(removeGraph)) {
            HistoryManager.saveHistory(HistoryType.DELETE_ONT_PROPERTY, removeCells);
        }
    }

    private boolean removeCells() {
        if (removeGraph.isSelectionEmpty()) {
            return true;
        }

        if (isRDFGraph(removeGraph)) {
            removeGraph.removeCellsWithEdges(removeCells);
            HistoryManager.saveHistory(HistoryType.DELETE_RDF, removeCells);
            return true;
        }

        if (MR3.OFF_META_MODEL_MANAGEMENT) {
            removeGraph.removeCellsWithEdges(removeCells);
            saveRemoveLog();
            return true;
        }

        Set<Object> removableCells = new HashSet<Object>();
        List unremovableCells = new ArrayList();
        Set unremovableResourceCells = new HashSet();

        Map rdfMap = checkUnRemovableRDFCells(removeCells, unremovableCells, unremovableResourceCells);
        Map propMap = checkUnRemovablePropertyCells(removeCells, unremovableCells, unremovableResourceCells,
                removeGraph);

        if (unremovableCells.isEmpty()) {
            removeGraph.removeCellsWithEdges(removeCells);
            saveRemoveLog();
            repaintAllGraphs();
        } else {
            for (Object removeCell : removeCells) {
                if (!unremovableCells.contains(removeCell)) {
                    removableCells.add(removeCell);
                }
            }
            removeGraph.removeCellsWithEdges(removableCells.toArray());
            saveRemoveLog();
            getRemoveDialog().setRefListInfo(unremovableResourceCells, rdfMap, propMap);
            getRemoveDialog().setVisible(true);
        }
        return false;
    }

    public AttributeDialog getAttrDialog() {
        AttributeDialog result = attrDialogRef.get();
        if (result == null) {
            result = new AttributeDialog(getRootFrame());
            attrDialogRef = new WeakReference<AttributeDialog>(result);
        }
        return result;
    }

    public NameSpaceTableDialog getNSTableDialog() {
        NameSpaceTableDialog result = nsTableDialogRef.get();
        if (result == null) {
            result = new NameSpaceTableDialog(this);
            nsTableDialogRef = new WeakReference<NameSpaceTableDialog>(result);
        }
        return result;
    }

    public FindResourceDialog getFindResourceDialog() {
        FindResourceDialog result = findResDialogRef.get();
        if (result == null) {
            result = new FindResourceDialog(this);
            findResDialogRef = new WeakReference<FindResourceDialog>(result);
        }
        return result;
    }

    public RemoveDialog getRemoveDialog() {
        RemoveDialog result = removeDialogRef.get();
        if (result == null) {
            result = new RemoveDialog(this);
            removeDialogRef = new WeakReference<RemoveDialog>(result);
        }
        return result;
    }

    public void setVisibleAttrDialog(boolean t) {
        getAttrDialog().setVisible(t);
    }

    private boolean isGraphEmpty() {
        return getCurrentRDFGraph().getAllCells().length == 0 && getCurrentClassGraph().getAllCells().length == 0
                && getCurrentPropertyGraph().getAllCells().length == 0;
    }

    public void applyLayout(GraphType graphType) {
        RDFSModelExtraction extractRDFS = new RDFSModelExtraction(this);
        if (GraphType.RDF == graphType) {
            if (GraphLayoutUtilities.LAYOUT_TYPE.equals(GraphLayoutUtilities.VGJ_TREE_LAYOUT)) {
                applyVGJRDFLayout(graphType);
            } else if (GraphLayoutUtilities.LAYOUT_TYPE.equals(GraphLayoutUtilities.JGRAPH_TREE_LAYOUT)) {
                applyJGraphRDFLayout();
            }
        } else if (GraphType.CLASS == graphType) {
            if (GraphLayoutUtilities.LAYOUT_TYPE.equals(GraphLayoutUtilities.VGJ_TREE_LAYOUT)) {
                applyVGJClassLayout(extractRDFS);
            } else if (GraphLayoutUtilities.LAYOUT_TYPE.equals(GraphLayoutUtilities.JGRAPH_TREE_LAYOUT)) {
                applyJGraphClassLayout();
            }
        } else if (GraphType.PROPERTY == graphType) {
            if (GraphLayoutUtilities.LAYOUT_TYPE.equals(GraphLayoutUtilities.VGJ_TREE_LAYOUT)) {
                applyVGJPropertyLayout(extractRDFS);
            } else if (GraphLayoutUtilities.LAYOUT_TYPE.equals(GraphLayoutUtilities.JGRAPH_TREE_LAYOUT)) {
                applyJGraphPropertyLayout();
            }
        }
    }

    /**
     * @param extractRDFS
     */
    private void applyVGJPropertyLayout(RDFSModelExtraction extractRDFS) {
        extractRDFS.extractPropertyModel(mr3Writer.getPropertyModel());
        Map<RDFNode, GraphLayoutData> cellLayoutMap = VGJTreeLayout.getVGJPropertyCellLayoutMap();
        RDFGraph propGraph = getCurrentPropertyGraph();
        Object[] cells = propGraph.getAllCells();
        for (int i = 0; i < cells.length; i++) {
            GraphCell cell = (GraphCell) cells[i];
            GraphLayoutData data = null;
            if (RDFGraph.isRDFSPropertyCell(cell)) {
                RDFSInfo info = (RDFSInfo) GraphConstants.getValue(cell.getAttributes());
                data = cellLayoutMap.get(info.getURI());
            }
            moveCell(cell, data, propGraph);
        }
    }

    private void applyJGraphPropertyLayout() {
        RDFGraph propGraph = getCurrentPropertyGraph();
        GraphLayoutUtilities.reverseArc(cellMaker, propGraph);
        treeLayout.performJGraphTreeLayout(propGraph, GraphLayoutUtilities.getJGraphPropertyLayoutDirection(),
                GraphLayoutUtilities.PROPERTY_VERTICAL_SPACE, GraphLayoutUtilities.PROPERTY_HORIZONTAL_SPACE);
        GraphLayoutUtilities.reverseArc(cellMaker, propGraph);
        clearSelection();
    }

    /**
     * @param extractRDFS
     */
    private void applyVGJClassLayout(RDFSModelExtraction extractRDFS) {
        extractRDFS.extractClassModel(mr3Writer.getClassModel());
        Map<RDFNode, GraphLayoutData> cellLayoutMap = VGJTreeLayout.getVGJClassCellLayoutMap();
        RDFGraph classGraph = getCurrentClassGraph();
        Object[] cells = classGraph.getAllCells();
        for (int i = 0; i < cells.length; i++) {
            GraphCell cell = (GraphCell) cells[i];
            GraphLayoutData data = null;
            if (RDFGraph.isRDFSClassCell(cell)) {
                RDFSInfo info = (RDFSInfo) GraphConstants.getValue(cell.getAttributes());
                data = cellLayoutMap.get(info.getURI());
            }
            moveCell(cell, data, classGraph);
        }
    }

    private void applyJGraphClassLayout() {
        RDFGraph classGraph = getCurrentClassGraph();
        GraphLayoutUtilities.reverseArc(cellMaker, classGraph);
        treeLayout.performJGraphTreeLayout(classGraph, GraphLayoutUtilities.getJGraphClassLayoutDirection(),
                GraphLayoutUtilities.CLASS_VERTICAL_SPACE, GraphLayoutUtilities.CLASS_HORIZONTAL_SPACE);
        GraphLayoutUtilities.reverseArc(cellMaker, classGraph);
        clearSelection();
    }

    /**
     * @param graphType
     */
    private void applyVGJRDFLayout(GraphType graphType) {
        removeTypeCells();
        Map<RDFNode, GraphLayoutData> cellLayoutMap = VGJTreeLayout.getVGJRDFCellLayoutMap(mr3Writer.getRDFModel());
        RDFGraph rdfGraph = getCurrentRDFGraph();
        Object[] cells = rdfGraph.getAllCells();
        for (int i = 0; i < cells.length; i++) {
            GraphCell cell = (GraphCell) cells[i];
            GraphLayoutData data = null;
            // if (rdfGraph.isRDFResourceCell(cell)) {
            // RDFResourceInfo info = resInfoMap.getCellInfo(cell);
            // data = (GraphLayoutData) cellLayoutMap.get(info.getURI());
            // } else if (rdfGraph.isRDFLiteralCell(cell)) {
            // Literal literal = litInfoMap.getCellInfo(cell);
            // data = (GraphLayoutData) cellLayoutMap.get(literal);
            // }
            if (RDFGraph.isEdge(cell)) {
                GraphCell sourceCell = (GraphCell) rdfGraph.getSourceVertex(cell);
                RDFResourceInfo info = (RDFResourceInfo) GraphConstants.getValue(sourceCell.getAttributes());
                data = cellLayoutMap.get(info.getURI());
                moveCell(sourceCell, data, rdfGraph);
                GraphCell targetCell = (GraphCell) rdfGraph.getTargetVertex(cell);
                if (RDFGraph.isRDFLiteralCell(targetCell)) {
                    MR3Literal literal = (MR3Literal) GraphConstants.getValue(targetCell.getAttributes());
                    Resource tmp = ResourceFactory.createResource(info.getURIStr() + literal.getLiteral().hashCode());
                    data = cellLayoutMap.get(tmp);
                } else {
                    info = (RDFResourceInfo) GraphConstants.getValue(targetCell.getAttributes());
                    data = cellLayoutMap.get(info.getURI());
                }
                moveCell(targetCell, data, rdfGraph);
            }
        }
        if (graphType == GraphType.RDF && isShowTypeCell()) {
            addTypeCells();
        }
    }

    private void applyJGraphRDFLayout() {
        removeTypeCells();
        treeLayout.performJGraphTreeLayout(getCurrentRDFGraph(), GraphLayoutUtilities.getJGraphRDFLayoutDirection(),
                GraphLayoutUtilities.RDF_VERTICAL_SPACE, GraphLayoutUtilities.RDF_HORIZONTAL_SPACE);
        addTypeCells();
    }

    private void moveCell(GraphCell cell, GraphLayoutData data, RDFGraph graph) {
        if (data != null) {
            AttributeMap map = cell.getAttributes();
            GraphConstants.setBounds(map, data.getRectangle());
            GraphUtilities.editCell(cell, map, graph);
        }
    }

    public void selectChangedRDFCells(RDFSInfo rdfsInfo) {
        Set selectedCellSet = new HashSet();
        RDFGraph rdfGraph = getCurrentRDFGraph();
        for (Object cell : rdfGraph.getAllCells()) {
            if (RDFGraph.isRDFResourceCell(cell)) {
                RDFResourceCell rdfCell = (RDFResourceCell) cell;
                RDFResourceInfo rdfInfo = (RDFResourceInfo) GraphConstants.getValue(rdfCell.getAttributes());
                if (rdfInfo.getType().equals(rdfsInfo.getURI())) {
                    selectedCellSet.add(rdfCell);
                }
            } else if (RDFGraph.isRDFPropertyCell(cell)) {
                RDFPropertyCell rdfCell = (RDFPropertyCell) cell;
                RDFSInfo info = (RDFSInfo) GraphConstants.getValue(rdfCell.getAttributes());
                if (info.getURI().equals(rdfsInfo.getURI())) {
                    rdfGraph.getGraphLayoutCache().editCell(rdfCell, rdfCell.getAttributes());
                    selectedCellSet.add(rdfCell);
                }
            }
        }
        rdfGraph.setSelectionCells(selectedCellSet.toArray());
    }

    private void selectCells(RDFGraph graph) {
        if (graph != null) {
            for (Object cell : graph.getAllCells()) {
                GraphCell gCell = (GraphCell) cell;
                graph.getGraphLayoutCache().editCell(gCell, gCell.getAttributes());
            }
        }
    }

    /**
     * グラフが再描画されない場合があるため，一度セルを選択して，強制的に再描画する
     */
    public void refreshGraphs() {
        try {
            selectCells(getCurrentRDFGraph());
            selectCells(getCurrentClassGraph());
            selectCells(getCurrentPropertyGraph());
        } catch (NullPointerException npe) {
            npe.printStackTrace();
        }
    }
}