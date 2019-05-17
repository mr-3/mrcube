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
package org.mrcube.actions;

import org.apache.batik.dom.GenericDOMImplementation;
import org.apache.batik.svggen.SVGGraphics2D;
import org.jgraph.JGraph;
import org.jgraph.plaf.basic.BasicGraphUI;
import org.mrcube.MR3;
import org.mrcube.jgraph.GraphManager;
import org.mrcube.jgraph.RDFGraph;
import org.mrcube.models.MR3Constants;
import org.mrcube.utils.GPConverter;
import org.mrcube.utils.Translator;
import org.mrcube.utils.Utilities;
import org.mrcube.utils.file_filter.JPGFileFilter;
import org.mrcube.utils.file_filter.MR3FileFilter;
import org.mrcube.utils.file_filter.PNGFileFilter;
import org.mrcube.utils.file_filter.SVGFileFilter;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

public class SaveGraphImageAction extends AbstractAction {

    private GraphManager gmanager;
    private MR3Constants.GraphType graphType;
    protected JFileChooser imageFileChooser;
    private static final PNGFileFilter pngFileFilter = new PNGFileFilter();
    private static final JPGFileFilter jpgFileFilter = new JPGFileFilter();
    private static final SVGFileFilter svgFileFilter = new SVGFileFilter();
    private static final String TITLE = Translator.getString("Action.SaveGraphImage.Text");
    private static final ImageIcon ICON = Utilities.getImageIcon(Translator.getString("Action.SaveGraphImage.Icon"));

    public SaveGraphImageAction(GraphManager gmanager, MR3Constants.GraphType graphType) {
        super(TITLE, ICON);
        this.graphType = graphType;
        this.gmanager = gmanager;
        imageFileChooser = new JFileChooser();
        imageFileChooser.setFileFilter(pngFileFilter);
        imageFileChooser.setFileFilter(jpgFileFilter);
        imageFileChooser.setFileFilter(svgFileFilter);
        setValues();
    }

    private void setValues() {
        putValue(SHORT_DESCRIPTION, TITLE);
        putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_C,
                Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx() | InputEvent.SHIFT_DOWN_MASK));
    }

    protected File selectSaveImageFile() {
        File currentProjectFile = MR3.getCurrentProject().getCurrentProjectFile();
        if (currentProjectFile != null) {
            imageFileChooser.setCurrentDirectory(currentProjectFile.getParentFile());
        }
        if (imageFileChooser.showSaveDialog(MR3.getCurrentProject()) == JFileChooser.APPROVE_OPTION) {
            File selectedFile = imageFileChooser.getSelectedFile();
            if (imageFileChooser.getFileFilter() instanceof MR3FileFilter) {
                MR3FileFilter filter = (MR3FileFilter) imageFileChooser.getFileFilter();
                return new File(addFileExtension(selectedFile.getAbsolutePath(), filter.getExtension()));
            } else {
                return selectedFile;
            }
        } else {
            return null;
        }
    }

    private String addFileExtension(String defaultPath, String extension) {
        String ext = (extension != null) ? "." + extension.toLowerCase() : "";
        if (extension != null && !defaultPath.toLowerCase().endsWith(".png")
                && !defaultPath.toLowerCase().endsWith(".jpg") && !defaultPath.toLowerCase().endsWith(".svg")) {
            defaultPath += ext;
        }
        return defaultPath;
    }


    private String getExtension(File f) {
        String ext = null;
        String s = f.getName();
        int i = s.lastIndexOf('.');

        if (i > 0 && i < s.length() - 1) {
            ext = s.substring(i + 1).toLowerCase();
        }
        return ext;
    }

    private RDFGraph getRDFGraph(MR3Constants.GraphType graphType) {
        switch (graphType) {
            case RDF:
                return gmanager.getCurrentRDFGraph();
            case CLASS:
                return gmanager.getCurrentClassGraph();
            case PROPERTY:
                return gmanager.getCurrentPropertyGraph();
            default:
                return gmanager.getCurrentRDFGraph();
        }
    }

    private BufferedImage getGraphImage() {
        return GPConverter.toImage(getRDFGraph(graphType));
    }

    private Dimension getSVGCanvasDimension(Rectangle2D bounds) {
        return new Dimension((int) Math.round(bounds.getX() + bounds.getWidth()),
                (int) Math.round(bounds.getY() + bounds.getHeight()));
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        File imgFile = selectSaveImageFile();
        if (imgFile == null) {
            return;
        }
        String ext = getExtension(imgFile);
        try {
            switch (ext) {
                case "png":
                case "jpg":
                    BufferedImage graphImage = getGraphImage();
                    if (graphImage != null) {
                        ImageIO.write(graphImage, ext, imgFile);
                    }
                    break;
                case "svg":
                    JGraph graph = getRDFGraph(graphType);
                    // Acknowledgement:
                    // http://devdocs.inightmare.org/2012/06/28/exporting-jgraph-to-svg/
                    Object[] cells = graph.getRoots();
                    Rectangle2D bounds = graph.toScreen(graph.getCellBounds(cells));
                    DOMImplementation domImpl = GenericDOMImplementation.getDOMImplementation();
                    Document document = domImpl.createDocument("http://mrcube.org/", "svg", null);
                    SVGGraphics2D svgGraphics = new SVGGraphics2D(document);
                    svgGraphics.setSVGCanvasSize(getSVGCanvasDimension(bounds));
                    RepaintManager repaintManager = RepaintManager.currentManager(graph);
                    repaintManager.setDoubleBufferingEnabled(true);
                    BasicGraphUI gui = (BasicGraphUI) graph.getUI();
                    gui.drawGraph(svgGraphics, bounds);
                    svgGraphics.stream(new OutputStreamWriter(new FileOutputStream(imgFile)), false);
                    break;
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }
}
