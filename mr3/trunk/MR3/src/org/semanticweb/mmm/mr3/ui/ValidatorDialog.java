/*
 * Project Name: MR^3 (Meta-Model Management based on RDFs Revision Reflection)
 * Project Website: http://mr3.sourceforge.net/
 * 
 * Copyright (C) 2003-2008 Yamaguchi Laboratory, Keio University. All rights reserved. 
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

package org.semanticweb.mmm.mr3.ui;

import java.awt.*;
import java.awt.event.*;
import java.util.*;

import javax.swing.*;
import javax.swing.event.*;

import org.semanticweb.mmm.mr3.data.*;
import org.semanticweb.mmm.mr3.io.*;
import org.semanticweb.mmm.mr3.jgraph.*;
import org.semanticweb.mmm.mr3.util.*;

import com.bbn.semweb.owl.vowlidator.*;
import com.bbn.semweb.owl.vowlidator.indications.*;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.vocabulary.*;

/**
 * @author takeshi morita
 */
public class ValidatorDialog extends JDialog {

    private MR3Writer mr3Writer;
    private GraphManager gmanager;
    private ValidatorAPI validator;

    private JEditorPane indicationPane;
    private JButton validateButton;
    private JButton cancelButton;

    private static final int WINDOW_HEIGHT = 400;
    private static final int WINDOW_WIDTH = 550;

    private static final String TITLE = Translator.getString("Component.Tools.Validator.Text");

    public ValidatorDialog(Frame frame, GraphManager gm) {
        super(frame, TITLE);
        mr3Writer = new MR3Writer(gm);
        gmanager = gm;

        try {
            // System.out.println(Utilities.getURL("preferences.xml").getPath());
            // validator = new
            // ValidatorAPI(Utilities.getURL("preferences.xml").getPath());
            // validator = new ValidatorAPI(getVOWLidatorPrefs());
            validator = new ValidatorAPI(new Preferences());
        } catch (Exception e) {
            e.printStackTrace();
        }

        indicationPane = new JEditorPane();
        indicationPane.addHyperlinkListener(new HyperlinkAction());
        indicationPane.setEditable(false);
        indicationPane.setContentType("text/html");
        JScrollPane showWarningAreaScroll = new JScrollPane(indicationPane);

        getContentPane().add(showWarningAreaScroll, BorderLayout.CENTER);
        getContentPane().add(getButtonPanel(), BorderLayout.SOUTH);

        setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
        setLocationRelativeTo(this);
        setVisible(false);
    }

    private Preferences getVOWLidatorPrefs() {
        Preferences prefs = new Preferences();
        prefs.addSubstitution("http://www.daml.org/2000/12/daml+oil#", "http://www.w3.org/2002/07/owl#", false, true);
        prefs.addSubstitution("http://www.daml.org/2000/10/daml-ont#", "http://www.w3.org/2002/07/owl#", false, true);
        prefs.addSubstitution("http://www.daml.org/2001/03/daml+oil#", "http://www.w3.org/2002/07/owl#", false, true);
        prefs.addSubstitution("http://www.w3.org/TR/rdf-schema#", "http://www.w3.org/2000/01/rdf-schema#", false, true);
        prefs.addSubstitution("http://www.w3.org/TR/1999/PR-rdf-schema-19990303#",
                "http://www.w3.org/2000/01/rdf-schema#", false, true);
        prefs.addSubstitution("http://www.w3.org/2001/10/daml+oil#", "http://www.daml.org/2001/03/daml+oil#", false,
                true);
        prefs.addSubstitution("http://www.w3.org/2000/10/XMLSchema#", "http://www.w3.org/2001/XMLSchema#", false, true);

        prefs.addToCache("http://www.w3.org/2002/07/owl#", Utilities.getURL("cache/www.w3.org_2002_07_owl").toString());
        prefs.addToCache("http://www.w3.org/2001/03/XMLSchema.xsd", Utilities.getURL(
                "cache/www_w3_org_2001_03_XMLSchema.xsd").toString());
        prefs.addToCache("http://www.w3.org/2001/XMLSchema.xsd", Utilities
                .getURL("cache/www_w3_org_2001_XMLSchema.xsd").toString());
        prefs.addToCache("http://www.w3.org/2000/01/rdf-schema#", Utilities.getURL(
                "cache/www.w3.org_2000_01_rdf-schema").toString());
        prefs.addToCache("http://www.w3.org/1999/02/22-rdf-syntax-ns#", Utilities.getURL(
                "cache/www.w3.org_1999_02_22-rdf-syntax-ns").toString());
        /*
         * DAML, OIL, DAML+OILはデフォルトでは支援しない
         * 
         * prefs.addToCache("http://www.daml.org/2000/10/daml-ont#", Utilities
         * .getURL("cache/www.daml.org_2000_10_daml-ont").toString());
         * prefs.addToCache("http://www.daml.org/2001/03/daml+oil-ex-dt#",
         * Utilities.getURL(
         * "cache/www.daml.org_2001_03_daml+oil-ex-dt").toString());
         * prefs.addToCache("http://www.daml.org/2001/03/daml+oil#",
         * Utilities.getURL( "cache/www.daml.org_2001_03_daml+oil").toString());
         * prefs.addToCache("http://www.daml.org/2001/03/daml+oil-ex#",
         * Utilities.getURL(
         * "cache/www.daml.org_2001_03_daml+oil-ex").toString());
         * prefs.addToCache("http://www.daml.org/2000/12/daml+oil#",
         * Utilities.getURL( "cache/www.daml.org_2000_12_daml+oil").toString());
         */
        return prefs;
    }

    class HyperlinkAction implements HyperlinkListener {
        public void hyperlinkUpdate(HyperlinkEvent e) {
            if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
                Resource res = ResourceFactory.createResource(e.getURL().toString());
                Object cell = gmanager.getRDFResourceCell(res);
                if (cell != null) {
                    gmanager.selectRDFCell(cell);
                    return;
                }
                cell = gmanager.getRDFPropertyCell(res);
                if (cell != null) {
                    gmanager.selectRDFCell(cell);
                    return;
                }
                cell = gmanager.getClassCell(res);
                if (cell != null) {
                    gmanager.selectClassCell(cell);
                    return;
                }
                cell = gmanager.getPropertyCell(res);
                if (cell != null) {
                    gmanager.selectPropertyCell(cell);
                    return;
                }
            }
        }
    }

    private JComponent getButtonPanel() {
        validateButton = new JButton(new ValidateAction());
        validateButton.setMnemonic('v');
        cancelButton = new JButton(new CancelAction());
        cancelButton.setMnemonic('c');
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridLayout(1, 2, 5, 5));
        buttonPanel.add(validateButton);
        buttonPanel.add(cancelButton);
        return Utilities.createEastPanel(buttonPanel);
    }

    private String getIndicationMessage(Indication indication) {
        String message = "";
        if (indication.toString().indexOf("ERROR") != -1) {
            message = indication.toString().split("ERROR")[1];
        } else if (indication.toString().indexOf("WARNING") != -1) {
            message = indication.toString().split("WARNING")[1];
        }
        message = message.split("\n")[0];
        String[] ms = message.split(":");
        String type = ms[0].split("-")[1];
        message = "";
        for (int i = 1; i < ms.length; i++) {
            message += ms[i];
        }
        message = " - (<font color=navy><b>" + type + "</b></font> )<br>" + message + "<br>";

        return message;
    }

    private void validateModel() {
        Model model = mr3Writer.getRDFModel();
        model.add(mr3Writer.getRDFSModel());
        Vector indications = validator.validateModel(model, gmanager.getBaseURI());
        StringBuilder messages = new StringBuilder("");
        messages.append("<html><body>");
        int id = 0;
        for (Iterator i = indications.iterator(); i.hasNext();) {
            Indication indication = (Indication) i.next();
            // if (!(indication instanceof UndefinedResourceIndication)
            // && !(indication instanceof UndefinedPropertyIndication)
            // && indication.getLevel() == Indication.WARNING) {
            if (indication instanceof UndefinedPropertyIndication || indication instanceof UndefinedResourceIndication) {
                String msg = indication.getMessage();
                if (msg.indexOf(OWL.getURI()) != -1 || msg.indexOf(RDFS.getURI()) != -1
                        || msg.indexOf(RDF.getURI()) != -1) {
                    continue;
                }
            }
            String message = getIndicationMessage(indication);
            Statement stmt = indication.getStatement();
            String location = "";
            if (stmt != null) {
                location = "At: [" + "<a href=" + stmt.getSubject() + ">" + stmt.getSubject() + "</a>" + ", <a href="
                        + stmt.getPredicate() + ">" + stmt.getPredicate() + "</a>";
                if (stmt.getObject() instanceof Resource) {
                    location += ", <a href=" + stmt.getObject() + ">" + stmt.getObject() + "</a>]<br>";
                } else {
                    location += ", " + stmt.getObject() + "]<br>";
                }
            }
            id++;
            messages.append("<b>[" + id + "]</b> <font color=red>WARNING</font>" + message + location + "<br>");
            // }
        }
        if (id == 0) {
            messages.append("OK");
        }
        messages.append("</body></html>");
        indicationPane.setText(messages.toString());
    }

    class ValidateAction extends AbstractAction {
        ValidateAction() {
            super(Translator.getString("Component.Tools.Validator.Validate"));
        }

        public void actionPerformed(ActionEvent e) {
            validateModel();
        }
    }

    class CancelAction extends AbstractAction {
        CancelAction() {
            super(MR3Constants.CANCEL);
        }
        public void actionPerformed(ActionEvent e) {
            setVisible(false);
        }
    }

    public void setVisible(boolean t) {
        validateModel();
        super.setVisible(t);
    }
}
