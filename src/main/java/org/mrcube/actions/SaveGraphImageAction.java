package org.mrcube.actions;

import org.mrcube.MR3;
import org.mrcube.jgraph.GraphManager;
import org.mrcube.models.MR3Constants;
import org.mrcube.utils.GPConverter;
import org.mrcube.utils.Translator;
import org.mrcube.utils.Utilities;
import org.mrcube.utils.file_filter.MR3FileFilter;
import org.mrcube.utils.file_filter.PNGFileFilter;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class SaveGraphImageAction extends AbstractAction {

    private GraphManager gmanager;
    private MR3Constants.GraphType graphType;
    protected JFileChooser imageFileChooser;
    private static final PNGFileFilter pngFileFilter = new PNGFileFilter();
    private static final String IMAGE_FILE_TYPE = "png";
    private static final String TITLE = Translator.getString("Action.SaveGraphImage.Text");
    private static final ImageIcon ICON = Utilities.getImageIcon(Translator.getString("Action.SaveGraphImage.Icon"));

    public SaveGraphImageAction(GraphManager gmanager, MR3Constants.GraphType graphType) {
        super(TITLE, ICON);
        this.graphType = graphType;
        this.gmanager = gmanager;
        imageFileChooser = new JFileChooser();
        imageFileChooser.setFileFilter(pngFileFilter);
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
        ) {
            defaultPath += ext;
        }
        return defaultPath;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        File file = selectSaveImageFile();
        if (file == null) {
            return;
        }
        try {
            BufferedImage img = null;
            switch (graphType) {
                case RDF:
                    img = GPConverter.toImage(gmanager.getCurrentRDFGraph());
                    break;
                case CLASS:
                    img = GPConverter.toImage(gmanager.getCurrentClassGraph());
                    break;
                case PROPERTY:
                    img = GPConverter.toImage(gmanager.getCurrentPropertyGraph());
                    break;
            }
            if (img != null) {
                ImageIO.write(img, IMAGE_FILE_TYPE, file);
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }
}
