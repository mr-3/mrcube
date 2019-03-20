/*
 * Project Name: MR^3 (Meta-Model Management based on RDFs Revision Reflection)
 * Project Website: http://mrcube.org/
 *
 * Copyright (C) 2003-2019 Yamaguchi Laboratory, Keio University. All rights reserved.
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

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.jgraph.JGraph;
import org.jgraph.graph.*;
import org.mrcube.MR3;
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

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.List;
import java.util.*;
import java.util.Map.Entry;
import java.util.prefs.Preferences;

/**
 * @author Takeshi Morita
 */
public class GraphManager {

    private final Frame rootFrame;
    private MR3ProjectPanel mr3ProjectPanel;

    private final MR3Writer mr3Writer;

    private boolean isImporting;
    private boolean isShowTypeCell;

    private final MR3CellMaker cellMaker;
    private final JGraphTreeLayout treeLayout;

    private AttributeDialog attributeDialog;
    private FindResourceDialog findResourceDialog;
    private NameSpaceTableDialog nameSpaceTableDialog;
    private RemoveDialog removeDialog;

    public static CellViewType cellViewType;

    private String baseURI;
    private static Preferences userPrefs;

    public static String CLASS_CLASS_LIST;
    public static String PROPERTY_CLASS_LIST;

    public GraphManager(Preferences prefs, Frame root) {
        rootFrame = root;
        userPrefs = prefs;
        attributeDialog = new AttributeDialog(getRootFrame());
        findResourceDialog = new FindResourceDialog(this);
        nameSpaceTableDialog = new NameSpaceTableDialog(this);
        removeDialog = new RemoveDialog(this);
        cellMaker = new MR3CellMaker(this);
        treeLayout = new JGraphTreeLayout(this);
        baseURI = userPrefs.get(PrefConstants.BaseURI, MR3Resource.getURI());
        GraphLayoutUtilities.setGraphManager(this);
        mr3Writer = new MR3Writer(this);
        CLASS_CLASS_LIST = RDFS.Class.toString() + " " + OWL.Class.toString();
        PROPERTY_CLASS_LIST = RDF.Property.toString() + " " + OWL.ObjectProperty.toString()
                + " " + OWL.DatatypeProperty.toString();
    }

    public void closeAllDialogs() {
        attributeDialog.setVisible(false);
        findResourceDialog.setVisible(false);
        nameSpaceTableDialog.setVisible(false);
        removeDialog.setVisible(false);
    }

    public void setMR3ProjectPanel(MR3ProjectPanel panel) {
        this.mr3ProjectPanel = panel;
    }

    public MR3CellMaker getCellMaker() {
        return cellMaker;
    }

    public RDFSModelMap getCurrentRDFSInfoMap() {
        return mr3ProjectPanel.getRDFSInfoMap();
    }

    public RDFEditor getCurrentRDFEditor() {
        return mr3ProjectPanel.getRDFEditor();
    }

    private ClassEditor getCurrentClassEditor() {
        return mr3ProjectPanel.getClassEditor();
    }

    private PropertyEditor getCurrentPropertyEditor() {
        return mr3ProjectPanel.getPropertyEditor();
    }

    public RDFGraph getCurrentRDFGraph() {
        if (mr3ProjectPanel != null) {
            return (RDFGraph) mr3ProjectPanel.getRDFEditor().getGraph();
        } else {
            return null;
        }
    }

    public RDFGraph getCurrentPropertyGraph() {
        if (mr3ProjectPanel != null) {
            return (RDFGraph) mr3ProjectPanel.getPropertyEditor().getGraph();
        } else {
            return null;
        }
    }

    public RDFGraph getCurrentClassGraph() {
        if (mr3ProjectPanel != null) {
            return (RDFGraph) mr3ProjectPanel.getClassEditor().getGraph();
        } else {
            return null;
        }
    }

    private boolean containKeyword(String keyword, ResourceModel model) {
        List<MR3Literal> literalList = new ArrayList<>();
        literalList.addAll(model.getLabelList());
        literalList.addAll(model.getCommentList());
        for (MR3Literal lit : literalList) {
            if (lit.getString().contains(keyword)) {
                return true;
            }
        }
        return false;
    }

    public Set<GraphCell> findRDFResourceSet(String keyword) {
        Set<GraphCell> rdfResourceSet = new HashSet<>();
        RDFGraph rdfGraph = getCurrentRDFGraph();
        for (Object cell1 : rdfGraph.getAllCells()) {
            GraphCell cell = (GraphCell) cell1;
            if (RDFGraph.isRDFResourceCell(cell) || RDFGraph.isRDFPropertyCell(cell)) {
                Resource uri = ResourceFactory.createResource();
                ResourceModel resourceModel = (ResourceModel) GraphConstants.getValue(cell.getAttributes());
                if (RDFGraph.isRDFResourceCell(cell)) {
                    uri = ((RDFResourceModel) resourceModel).getURI();
                } else if (RDFGraph.isRDFPropertyCell(cell)) {
                    uri = ((RDFSModel) resourceModel).getURI();
                }
                if (isMatches(keyword, uri)) {
                    rdfResourceSet.add(cell);
                }
                if (containKeyword(keyword, resourceModel)) {
                    rdfResourceSet.add(cell);
                }
            }
        }
        return rdfResourceSet;
    }

    public Set<GraphCell> findRDFSResourceSet(String keyword, RDFGraph graph) {
        Set<GraphCell> rdfsResourceSet = new HashSet<>();
        for (Object cell1 : graph.getAllCells()) {
            if (RDFGraph.isRDFSCell(cell1)) {
                GraphCell cell = (GraphCell) cell1;
                RDFSModel rdfsModel = (RDFSModel) GraphConstants.getValue(cell.getAttributes());
                if (isMatches(keyword, rdfsModel.getURI())) {
                    rdfsResourceSet.add(cell);
                }
                if (containKeyword(keyword, rdfsModel)) {
                    rdfsResourceSet.add(cell);
                }
            }
        }
        return rdfsResourceSet;
    }

    private boolean isMatches(String key, Resource uri) {
        return uri.getURI() != null && uri.getURI().contains(key);
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
        for (Object cell1 : cells) {
            if (RDFGraph.isRDFPropertyCell(cell1)) {
                GraphCell cell = (GraphCell) cell1;
                if (t) {
                    GraphConstants.setFont(cell.getAttributes(), GraphUtilities.defaultFont);
                } else {
                    GraphConstants.setFont(cell.getAttributes(), new Font("", Font.PLAIN, 0));
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

    public void setIsShowTypeCell(boolean t) {
        isShowTypeCell = t;
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

    public void clearSelection() {
        getCurrentRDFGraph().clearSelection();
        getCurrentClassGraph().clearSelection();
        getCurrentPropertyGraph().clearSelection();
    }

    private Set<Resource> getMetaClassList(String[] list) {
        Set<Resource> metaClassList = new HashSet<>();
        for (String s : list) {
            metaClassList.add(ResourceFactory.createResource(s));
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
            history.append(historyBox.getElementAt(i).toString()).append(",");
        }
        userPrefs.put(PrefConstants.ResourceContainer, history.toString());
    }

    public String[] getResourceContainer() {
        return userPrefs.get(PrefConstants.ResourceContainer, "").split(",");
    }


    public String getDefaultClassClass() {
        return userPrefs.get(PrefConstants.DefaultClassClass, RDFS.Class.getURI());
    }

    public Set<Resource> getClassClassList() {
        return getMetaClassList(userPrefs.get(PrefConstants.ClassClassList, CLASS_CLASS_LIST).split(" "));
    }


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
        for (Resource subject : orgModel.listSubjectsWithProperty(RDFS.subClassOf, supClass).toList()) {
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
        Set<String> classSet = new HashSet<>();
        for (Object cell : getCurrentClassGraph().getAllCells()) {
            if (RDFGraph.isRDFSClassCell(cell)) {
                RDFSModel info = (RDFSModel) GraphConstants.getValue(((GraphCell) cell).getAttributes());
                classSet.add(info.getURIStr());
            }
        }
        return classSet;
    }

    public Set<String> getPropertySet() {
        Set<String> propertySet = new HashSet<>();
        for (Object cell : getCurrentPropertyGraph().getAllCells()) {
            if (RDFGraph.isRDFSPropertyCell(cell)) {
                RDFSModel info = (RDFSModel) GraphConstants.getValue(((GraphCell) cell).getAttributes());
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

    public void removeTypeCells() {
        Object[] rdfCells = getCurrentRDFGraph().getAllCells();
        List<GraphCell> typeCellList = new ArrayList<>();
        for (Object rdfCell : rdfCells) {
            GraphCell cell = (GraphCell) rdfCell;
            if (RDFGraph.isTypeCell(cell)) {
                typeCellList.add(cell);
            }
        }
        getCurrentRDFGraph().removeCellsWithEdges(typeCellList.toArray());
    }

    private boolean isRDFResourceDuplicated(String uri, Object cell, GraphType type) {
        Object[] rdfCells = getCurrentRDFGraph().getAllCells();
        for (Object rdfCell1 : rdfCells) {
            GraphCell rdfCell = (GraphCell) rdfCell1;
            if (rdfCell instanceof RDFResourceCell) {
                RDFResourceModel resInfo = (RDFResourceModel) GraphConstants.getValue(rdfCell.getAttributes());
                if (resInfo == null) {
                    resInfo = (RDFResourceModel) GraphConstants.getValue(rdfCell.getAttributes());
                }
                String tmpURI = resInfo.getURIStr();
                if (tmpURI.equals(uri) && rdfCell != cell) {
                    if (resInfo.getTypeCell() == null) {
                        return true;
                    }
                    RDFSModel rdfsModel = resInfo.getTypeInfo();
                    // RDFエディタ内のクラス定義は，重複とみなさないようにする．
                    return (!(type == GraphType.CLASS && rdfsModel.getURI().equals(RDFS.Class) || type == GraphType.PROPERTY
                            && rdfsModel.getURI().equals(RDF.Property)));
                }
            }
        }

        // Collection entrySet = resInfoMap.entrySet();
        // for (Iterator i = entrySet.iterator(); i.hasNext();) {
        // Map.Entry entry = (Map.Entry) i.next();
        // Object infoCell = entry.getKey();
        // RDFResourceModel resInfo = (RDFResourceModel) entry.getValue();
        // String tmpURI = resInfo.getURIStr();
        //
        // if (tmpURI.equals(uri) && infoCell != cell) {
        // // RDFSModel rdfsModel =
        // // rdfsInfoMap.getCellInfo(resInfo.getTypeCell());
        // RDFSModel rdfsModel = (RDFSModel) GraphConstants.getValue(((GraphCell)
        // resInfo.getTypeCell())
        // .getAttributes());
        // /*
        // * RDFエディタ内のクラス定義は，重複とみなさないようにする．
        // */
        // if (!(type == GraphType.CLASS && rdfsModel != null &&
        // rdfsModel.getURI().equals(RDFS.Class) || type == GraphType.PROPERTY
        // && rdfsModel != null && rdfsModel.getURI().equals(RDF.Property))) {
        // return true; }
        // }
        // }
        return false;
    }

    public boolean isEmptyURI(String uri) {
        if (uri.equals("")) {
            Utilities.showErrorMessageDialog(Translator.getString("Warning.Message4"));
            return true;
        }
        return false;
    }

    public boolean isDuplicatedWithDialog(String uri, Object cell, GraphType type) {
        if (isDuplicated(uri, cell, type)) {
            Utilities.showErrorMessageDialog(Translator.getString("Warning.Message1"));
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
        return new HashSet<>();
    }

    public Set<String> getAllNameSpaceSet() {
        Set<String> allNSSet = new HashSet<>();
        allNSSet.addAll(getRDFNameSpaceSet());
        allNSSet.addAll(getClassNameSpaceSet());
        allNSSet.addAll(getPropertyNameSpaceSet());

        return allNSSet;
    }

    private Set<String> getRDFNameSpaceSet() {
        Set<String> nameSpaces = new HashSet<>();

        Object[] rdfCells = getCurrentRDFGraph().getAllCells();
        for (Object rdfCell : rdfCells) {
            GraphCell cell = (GraphCell) rdfCell;
            if (RDFGraph.isRDFResourceCell(cell)) {
                RDFResourceModel info = (RDFResourceModel) GraphConstants.getValue(cell.getAttributes());
                Resource uri = info.getURI();
                nameSpaces.add(uri.getNameSpace());
            }
        }
        return nameSpaces;
    }

    private Set<String> getClassNameSpaceSet() {
        Set<String> nameSpaces = new HashSet<>();

        Object[] classCells = getCurrentClassGraph().getAllCells();
        for (Object classCell : classCells) {
            GraphCell cell = (GraphCell) classCell;
            if (RDFGraph.isRDFSClassCell(cell)) {
                RDFSModel info = (RDFSModel) GraphConstants.getValue(cell.getAttributes());
                Resource uri = info.getURI();
                nameSpaces.add(uri.getNameSpace());
            }
        }
        return nameSpaces;
    }

    private Set<String> getPropertyNameSpaceSet() {
        Set<String> nameSpaces = new HashSet<>();
        for (Object cell : getCurrentPropertyGraph().getAllCells()) {
            if (RDFGraph.isRDFSPropertyCell(cell)) {
                RDFSModel info = (RDFSModel) GraphConstants.getValue(((GraphCell) cell).getAttributes());
                Resource uri = info.getURI();
                nameSpaces.add(uri.getNameSpace());
            }
        }
        return nameSpaces;
    }

    public void setNodeBounds(Map<Resource, MR3Literal> uriNodeInfoMap) {
        MR3.STATUS_BAR.initNormal(uriNodeInfoMap.keySet().size());
        RDFSModelMap rdfsModelMap = getCurrentRDFSInfoMap();

        for (Entry<Resource, MR3Literal> entry : uriNodeInfoMap.entrySet()) {
            Resource uri = entry.getKey();
            MR3Literal rec = entry.getValue();

            GraphCell cell = (GraphCell) getRDFResourceCell(uri);
            if (cell != null) {
                continue;
            } else if (rdfsModelMap.isClassCell(uri)) {
                continue;
            } else if (rdfsModelMap.isPropertyCell(uri)) {
                continue;
            } else if (uri.getURI().matches(MR3Resource.Literal.getURI() + ".*")) {
                MR3Literal literal = new MR3Literal(rec.getString(), rec.getLanguage(), rec.getDatatype());
                DefaultGraphCell litCell = (DefaultGraphCell) cellMaker.insertRDFLiteral(rec.getRectangle(), literal);
                DefaultGraphCell source = (DefaultGraphCell) getRDFResourceCell(rec.getResource());
                if (source == null) {
                    // System.out.println("s: " + source);
                    continue;
                }
                GraphCell propCell = (GraphCell) rdfsModelMap.getPropertyCell(rec.getProperty());
                if (propCell != null) {
                    RDFSModel info = (RDFSModel) GraphConstants.getValue(propCell.getAttributes());
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
            Dimension dim;
            if (RDFGraph.isRDFLiteralCell(cell)) {
                dim = GraphUtilities.getAutoLiteralNodeDimention(this, value);
            } else if (RDFGraph.isRDFResourceCell(cell)) {
                dim = GraphUtilities.getAutoNodeDimension(this, value);
                if (isShowTypeCell) {
                    RDFResourceModel info = (RDFResourceModel) GraphConstants.getValue(cell.getAttributes());
                    RDFSModel typeInfo = (RDFSModel) GraphConstants.getValue(info.getTypeCell().getAttributes());
                    Dimension typeDim = GraphUtilities.getAutoNodeDimension(this, getRDFSNodeValue(info.getType(), typeInfo));

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
            setCellBounds(map, dim);
        }
        cell.getAttributes().applyMap(map);
    }

    public String getRDFSNodeValue(Resource uri, RDFSModel info) {
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

    public String getRDFNodeValue(Resource uri, ResourceModel info) {
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
        Set<Resource> region = new HashSet<>();
        for (Object cell1 : cells) {
            DefaultGraphCell cell = (DefaultGraphCell) cell1;
            if (RDFGraph.isRDFSClassCell(cell1)) {
                DefaultPort port = (DefaultPort) cell.getChildAt(0);
                if (port.getEdges().isEmpty()) {
                    RDFSModel info = (RDFSModel) GraphConstants.getValue(cell.getAttributes());
                    region.add(info.getURI());
                }
            }
        }
        return region;
    }

    public List<GraphCell> getPropertyList() {
        List<GraphCell> list = new ArrayList<>();
        Object[] cells = getCurrentPropertyGraph().getAllCells();
        for (Object cell1 : cells) {
            GraphCell cell = (GraphCell) cell1;
            if (RDFGraph.isRDFSPropertyCell(cell)) {
                list.add(cell);
            }
        }
        return list;
    }

    public Object getRDFResourceCell(Resource uri) {
        Object[] cells = getCurrentRDFGraph().getAllCells();
        for (Object cell1 : cells) {
            if (RDFGraph.isRDFResourceCell(cell1)) {
                GraphCell cell = (GraphCell) cell1;
                RDFResourceModel info = (RDFResourceModel) GraphConstants.getValue(cell.getAttributes());
                /*
                 * Resourceクラスのインスタンスをequalsで比べるとisAnon()
                 * によって，true,falseを決められる．文字列を保存しているuri
                 * は，isAnonは必ずfalseとなるため，文字列比較をするために toStringを用いている．
                 */
                if (info.getURI().toString().equals(uri.toString())) {
                    return cell1;
                }
            }
        }
        return null;
    }

    public Object getRDFPropertyCell(Resource uri) {
        Object[] cells = getCurrentRDFGraph().getAllCells();
        for (Object cell1 : cells) {
            if (RDFGraph.isRDFPropertyCell(cell1)) {
                GraphCell cell = (GraphCell) cell1;
                RDFSModel info = (RDFSModel) GraphConstants.getValue(cell.getAttributes());
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

    private static final Point INSERT_POINT = new Point(50, 50);

    public Object getClassCell(Resource uri, Map<RDFNode, GraphLayoutData> cellLayoutMap) {
        RDFSModelMap rdfsModelMap = getCurrentRDFSInfoMap();
        GraphCell cell = rdfsModelMap.getClassCell(uri);
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


    public Set getClassInstanceSet(Object type) {
        Set<Object> instanceSet = new HashSet<>();
        Object[] cells = getCurrentRDFGraph().getAllCells();
        for (Object cell1 : cells) {
            if (RDFGraph.isRDFResourceCell(cell1)) {
                GraphCell cell = (GraphCell) cell1;
                RDFResourceModel info = (RDFResourceModel) GraphConstants.getValue(cell.getAttributes());
                if (info.getTypeCell() == type) {
                    instanceSet.add(cell);
                }
            }
        }
        return instanceSet;
    }

    public Set<RDFResourceModel> getClassInstanceInfoSet(RDFSModel clsInfo) {
        Set<RDFResourceModel> instanceInfoSet = new HashSet<>();
        Object[] cells = getCurrentRDFGraph().getAllCells();
        for (Object cell1 : cells) {
            if (RDFGraph.isRDFResourceCell(cell1)) {
                GraphCell cell = (GraphCell) cell1;
                RDFResourceModel info = (RDFResourceModel) GraphConstants.getValue(cell.getAttributes());
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
        for (Object cell1 : cells) {
            GraphCell cell = (GraphCell) cell1;
            if (RDFGraph.isRDFPropertyCell(cell)) {
                RDFSModel info = (RDFSModel) GraphConstants.getValue(cell.getAttributes());
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

    public Set getPropertyInstanceInfoSet(RDFSModel propInfo) {
        // TreeSet を使うためにはcompratorを適切に実装しないといけない．
        Set instanceSet = new HashSet();
        RDFGraph rdfGraph = getCurrentRDFGraph();
        Object[] cells = rdfGraph.getAllCells();
        for (Object cell1 : cells) {
            GraphCell cell = (GraphCell) cell1;
            if (RDFGraph.isRDFPropertyCell(cell)) {
                RDFSModel info = (RDFSModel) GraphConstants.getValue(cell.getAttributes());
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
            Set<GraphCell> propSet = new HashSet<>();

            if (RDFGraph.isRDFSClassCell(cell)) { // 削除したクラスのセル
                // cellを参照していないかどうかすべてのプロパティに対して調べる
                // domainかrangeに含まれている可能性がある．
                for (Object propCell1 : propCells) {
                    GraphCell propCell = (GraphCell) propCell1;
                    if (RDFGraph.isRDFSPropertyCell(propCell)) {
                        PropertyModel info = (PropertyModel) GraphConstants.getValue(propCell.getAttributes());
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
            Set<GraphCell> rdfSet = new HashSet<>();
            for (Object rdfCell1 : rdfCells) {
                GraphCell rdfCell = (GraphCell) rdfCell1;
                if (RDFGraph.isRDFResourceCell(rdfCell)) {
                    RDFResourceModel rdfInfo = (RDFResourceModel) GraphConstants.getValue(rdfCell.getAttributes());
                    if (rdfInfo.getTypeCell() == cell) {
                        rdfSet.add(rdfCell);
                    }
                } else if (RDFGraph.isEdge(rdfCell)) {
                    RDFSModel info = (RDFSModel) GraphConstants.getValue(rdfCell.getAttributes());
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

    private void repaintClassGrpah() {
        RDFGraph classGraph = getCurrentClassGraph();
        classGraph.getGraphLayoutCache().reload();
        classGraph.repaint();
    }

    private void repaintPropertyGraph() {
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

        Set<Object> removableCells = new HashSet<>();
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
        return attributeDialog;
    }

    public NameSpaceTableDialog getNSTableDialog() {
        return nameSpaceTableDialog;
    }

    public FindResourceDialog getFindResourceDialog() {
        return findResourceDialog;
    }

    public RemoveDialog getRemoveDialog() {
        return removeDialog;
    }

    public void setVisibleAttrDialog(boolean t) {
        getAttrDialog().setVisible(t);
    }

    public void applyLayout(GraphType graphType) {
        RDFSModelExtraction extractRDFS = new RDFSModelExtraction(this);
        switch (graphType) {
            case RDF:
                switch (GraphLayoutUtilities.LAYOUT_TYPE) {
                    case GraphLayoutUtilities.VGJ_TREE_LAYOUT:
                        applyVGJRDFLayout(graphType);
                        break;
                    case GraphLayoutUtilities.JGRAPH_TREE_LAYOUT:
                        applyJGraphRDFLayout();
                        break;
                }
                break;
            case CLASS:
                switch (GraphLayoutUtilities.LAYOUT_TYPE) {
                    case GraphLayoutUtilities.VGJ_TREE_LAYOUT:
                        applyVGJClassLayout(extractRDFS);
                        break;
                    case GraphLayoutUtilities.JGRAPH_TREE_LAYOUT:
                        applyJGraphClassLayout();
                        break;
                }
                break;
            case PROPERTY:
                switch (GraphLayoutUtilities.LAYOUT_TYPE) {
                    case GraphLayoutUtilities.VGJ_TREE_LAYOUT:
                        applyVGJPropertyLayout(extractRDFS);
                        break;
                    case GraphLayoutUtilities.JGRAPH_TREE_LAYOUT:
                        applyJGraphPropertyLayout();
                        break;
                }
                break;
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
        for (Object cell1 : cells) {
            GraphCell cell = (GraphCell) cell1;
            GraphLayoutData data = null;
            if (RDFGraph.isRDFSPropertyCell(cell)) {
                RDFSModel info = (RDFSModel) GraphConstants.getValue(cell.getAttributes());
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
        for (Object cell1 : cells) {
            GraphCell cell = (GraphCell) cell1;
            GraphLayoutData data = null;
            if (RDFGraph.isRDFSClassCell(cell)) {
                RDFSModel info = (RDFSModel) GraphConstants.getValue(cell.getAttributes());
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
        for (Object cell1 : cells) {
            GraphCell cell = (GraphCell) cell1;
            GraphLayoutData data = null;
            // if (rdfGraph.isRDFResourceCell(cell)) {
            // RDFResourceModel info = resInfoMap.getCellInfo(cell);
            // data = (GraphLayoutData) cellLayoutMap.get(info.getURI());
            // } else if (rdfGraph.isRDFLiteralCell(cell)) {
            // Literal literal = litInfoMap.getCellInfo(cell);
            // data = (GraphLayoutData) cellLayoutMap.get(literal);
            // }
            if (RDFGraph.isEdge(cell)) {
                GraphCell sourceCell = (GraphCell) rdfGraph.getSourceVertex(cell);
                RDFResourceModel info = (RDFResourceModel) GraphConstants.getValue(sourceCell.getAttributes());
                data = cellLayoutMap.get(info.getURI());
                moveCell(sourceCell, data, rdfGraph);
                GraphCell targetCell = (GraphCell) rdfGraph.getTargetVertex(cell);
                if (RDFGraph.isRDFLiteralCell(targetCell)) {
                    MR3Literal literal = (MR3Literal) GraphConstants.getValue(targetCell.getAttributes());
                    Resource tmp = ResourceFactory.createResource(info.getURIStr() + literal.getLiteral().hashCode());
                    data = cellLayoutMap.get(tmp);
                } else {
                    info = (RDFResourceModel) GraphConstants.getValue(targetCell.getAttributes());
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

    public void selectChangedRDFCells(RDFSModel rdfsModel) {
        Set selectedCellSet = new HashSet();
        RDFGraph rdfGraph = getCurrentRDFGraph();
        for (Object cell : rdfGraph.getAllCells()) {
            if (RDFGraph.isRDFResourceCell(cell)) {
                RDFResourceCell rdfCell = (RDFResourceCell) cell;
                RDFResourceModel rdfInfo = (RDFResourceModel) GraphConstants.getValue(rdfCell.getAttributes());
                if (rdfInfo.getType().equals(rdfsModel.getURI())) {
                    selectedCellSet.add(rdfCell);
                }
            } else if (RDFGraph.isRDFPropertyCell(cell)) {
                RDFPropertyCell rdfCell = (RDFPropertyCell) cell;
                RDFSModel info = (RDFSModel) GraphConstants.getValue(rdfCell.getAttributes());
                if (info.getURI().equals(rdfsModel.getURI())) {
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