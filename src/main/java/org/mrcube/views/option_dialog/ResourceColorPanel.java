package org.mrcube.views.option_dialog;

import org.mrcube.utils.Translator;
import org.mrcube.views.OptionDialog;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class ResourceColorPanel extends JPanel {

    private final JLabel fgColorLabel;
    private final JLabel bgColorLabel;
    private final JLabel borderColorLabel;
    private final JLabel selectedBgColorLabel;
    private final JLabel selectedBorderColorLabel;

    private final JButton fgColorButton;
    private final JButton bgColorButton;
    private final JButton borderColorButton;
    private final JButton selectedBgColorButton;
    private final JButton selectedBorderColorButton;

    private Color fgColor;
    private Color bgColor;
    private Color borderColor;
    private Color selectedBgColor;
    private Color selectedBorderColor;

    private final ChangeColorAction changeColorAction;

    private static final int LABEL_WIDTH = 120;
    private static final int LABEL_HEIGHT = 25;
    private static final int BUTTON_WIDTH = 40;
    private static final int BUTTON_HEIGHT = 25;

    public ResourceColorPanel(String resourceType, OptionDialog.RenderingResourceType renderingResourceType,
                              Color fgColor, Color bgColor, Color borderColor, Color selectedBgColor, Color selectedBorderColor) {
        this.fgColor = fgColor;
        this.bgColor = bgColor;
        this.borderColor = borderColor;
        this.selectedBgColor = selectedBgColor;
        this.selectedBorderColor = selectedBorderColor;

        changeColorAction = new ChangeColorAction();
        fgColorLabel = new JLabel(Translator.getString("PreferenceDialog.RenderingTab.ForegroundColor"));
        fgColorLabel.setPreferredSize(new Dimension(LABEL_WIDTH, LABEL_HEIGHT));
        bgColorLabel = new JLabel(Translator.getString("PreferenceDialog.RenderingTab.BackgroundColor"));
        bgColorLabel.setPreferredSize(new Dimension(LABEL_WIDTH, LABEL_HEIGHT));
        borderColorLabel = new JLabel(Translator.getString("PreferenceDialog.RenderingTab.BorderColor"));
        borderColorLabel.setPreferredSize(new Dimension(LABEL_WIDTH, LABEL_HEIGHT));
        selectedBgColorLabel = new JLabel(Translator.getString("PreferenceDialog.RenderingTab.SelectedBackgroundColor"));
        selectedBgColorLabel.setPreferredSize(new Dimension(LABEL_WIDTH, LABEL_HEIGHT));
        selectedBorderColorLabel = new JLabel(Translator.getString("PreferenceDialog.RenderingTab.SelectedBorderColor"));
        selectedBorderColorLabel.setPreferredSize(new Dimension(LABEL_WIDTH, LABEL_HEIGHT));
        fgColorButton = initColorButton(OptionDialog.RenderingType.Foreground);
        bgColorButton = initColorButton(OptionDialog.RenderingType.Background);
        borderColorButton = initColorButton(OptionDialog.RenderingType.Border);
        selectedBgColorButton = initColorButton(OptionDialog.RenderingType.SelectedBackground);
        selectedBorderColorButton = initColorButton(OptionDialog.RenderingType.SelectedBorder);

        setBorder(BorderFactory.createTitledBorder(resourceType));
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        if (renderingResourceType == OptionDialog.RenderingResourceType.Editor) {
            JPanel bgColorPanel = new JPanel();
            bgColorPanel.add(bgColorLabel);
            bgColorPanel.add(bgColorButton);
            add(bgColorPanel);
            return;
        }
        JPanel fgColorPanel = new JPanel();
        fgColorPanel.add(fgColorLabel);
        fgColorPanel.add(fgColorButton);
        add(fgColorPanel);
        if (renderingResourceType != OptionDialog.RenderingResourceType.RDFProperty) {
            JPanel bgColorPanel = new JPanel();
            bgColorPanel.add(bgColorLabel);
            bgColorPanel.add(bgColorButton);
            add(bgColorPanel);
            JPanel selectedBgColorPanel = new JPanel();
            selectedBgColorPanel.add(selectedBgColorLabel);
            selectedBgColorPanel.add(selectedBgColorButton);
            add(selectedBgColorPanel);
        }
        JPanel borderColorPanel = new JPanel();
        borderColorButton.setAlignmentY(0f);
        borderColorPanel.add(borderColorLabel);
        borderColorPanel.add(borderColorButton);
        add(borderColorPanel);
        JPanel selectedBorderColorPanel = new JPanel();
        selectedBorderColorPanel.add(selectedBorderColorLabel);
        selectedBorderColorPanel.add(selectedBorderColorButton);
        add(selectedBorderColorPanel);
    }

    public Color getFgColor() {
        return fgColor;
    }

    public void setFgColor(Color fgColor) {
        this.fgColor = fgColor;
    }

    public Color getBgColor() {
        return bgColor;
    }

    public void setBgColor(Color bgColor) {
        this.bgColor = bgColor;
    }

    public Color getBorderColor() {
        return borderColor;
    }

    public void setBorderColor(Color borderColor) {
        this.borderColor = borderColor;
    }

    public Color getSelectedBgColor() {
        return selectedBgColor;
    }

    public void setSelectedBgColor(Color selectedBgColor) {
        this.selectedBgColor = selectedBgColor;
    }

    public Color getSelectedBorderColor() {
        return selectedBorderColor;
    }

    public void setSelectedBorderColor(Color selectedBorderColor) {
        this.selectedBorderColor = selectedBorderColor;
    }

    private JButton initColorButton(OptionDialog.RenderingType renderingType) {
        JButton button = new JButton();
        button.setHorizontalAlignment(JButton.LEFT);
        button.setIcon(new ResourceColorIcon(renderingType));
        button.setMaximumSize(new Dimension(BUTTON_WIDTH, BUTTON_HEIGHT));
        button.setPreferredSize(new Dimension(BUTTON_WIDTH, BUTTON_HEIGHT));
        button.addActionListener(changeColorAction);
        return button;
    }

    class ResourceColorIcon implements Icon {
        private static final int ICON_WIDTH = 10;
        private static final int ICON_HEIGHT = 10;
        private final OptionDialog.RenderingType renderingType;

        ResourceColorIcon(OptionDialog.RenderingType resourceType) {
            this.renderingType = resourceType;
        }

        public int getIconWidth() {
            return ICON_WIDTH;
        }

        public int getIconHeight() {
            return ICON_HEIGHT;
        }

        public void paintIcon(Component c, Graphics g, int x, int y) {
            g.setColor(Color.black);
            g.fillRect(x, y, getIconWidth(), getIconHeight());

            switch (renderingType) {
                case Foreground:
                    g.setColor(fgColor);
                    break;
                case Background:
                    g.setColor(bgColor);
                    break;
                case Border:
                    g.setColor(borderColor);
                    break;
                case SelectedBackground:
                    g.setColor(selectedBgColor);
                    break;
                case SelectedBorder:
                    g.setColor(selectedBorderColor);
                    break;
            }
            g.fillRect(x + 2, y + 2, getIconWidth() - 4, getIconHeight() - 4);
        }
    }

    class ChangeColorAction extends AbstractAction {
        public void actionPerformed(ActionEvent e) {
            Color prevColor = Color.black;
            if (e.getSource() == fgColorButton) {
                prevColor = fgColor;
            } else if (e.getSource() == bgColorButton) {
                prevColor = bgColor;
            } else if (e.getSource() == borderColorButton) {
                prevColor = borderColor;
            } else if (e.getSource() == selectedBgColorButton) {
                prevColor = selectedBgColor;
            } else if (e.getSource() == selectedBorderColorButton) {
                prevColor = selectedBorderColor;
            }
            Color userSelectedColor = JColorChooser.showDialog(null, Translator.getString("PreferenceDialog.RenderingTab.SelectColor"), prevColor);
            if (userSelectedColor == null) {
                userSelectedColor = prevColor;
            }
            if (e.getSource() == fgColorButton) {
                fgColor = userSelectedColor;
            } else if (e.getSource() == bgColorButton) {
                bgColor = userSelectedColor;
            } else if (e.getSource() == borderColorButton) {
                borderColor = userSelectedColor;
            } else if (e.getSource() == selectedBgColorButton) {
                selectedBgColor = userSelectedColor;
            } else if (e.getSource() == selectedBorderColorButton) {
                selectedBorderColor = userSelectedColor;
            }
        }
    }

}
