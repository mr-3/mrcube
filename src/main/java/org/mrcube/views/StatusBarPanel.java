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

package org.mrcube.views;

import javax.swing.*;
import java.awt.*;
import java.util.Calendar;

/**
 * @author Takeshi Morita
 */
public class StatusBarPanel extends Panel {

    private long startTime;
    private double progressTime;

    private int maxValue;
    private int currentValue;

    private final JTextField statusField;
    private final JProgressBar progressBar;
    private static final Color STATUS_BAR_COLOR = new Color(240, 240, 240);

    public StatusBarPanel() {
        progressBar = new JProgressBar(JProgressBar.HORIZONTAL);
        progressBar.setStringPainted(false);
        progressBar.setVisible(false);
        statusField = new JTextField();
        statusField.setBackground(STATUS_BAR_COLOR);
        statusField.setEditable(false);
        setLayout(new BorderLayout());
        add(statusField, BorderLayout.CENTER);
        add(progressBar, BorderLayout.EAST);
    }

    public void setCurrentTime() {
        progressTime = (double) (Calendar.getInstance().getTimeInMillis() - startTime) / 1000;
        statusField.setText("Time: " + progressTime);
    }

    public void startTime() {
        startTime = Calendar.getInstance().getTimeInMillis();
        setCurrentTime();
    }

    public double getProgressTime() {
        return progressTime;
    }

    public void initNormal(int max) {
        maxValue = max;
        progressBar.setIndeterminate(false);
        progressBar.setMinimum(0);
        progressBar.setMaximum(maxValue);
        progressBar.setValue(0);
        currentValue = 0;
        progressBar.setVisible(true);
    }

    public void initIndeterminate() {
        progressBar.setIndeterminate(true);
        progressBar.setVisible(true);
    }

    public void addValue() {
        currentValue++;
        if (maxValue < 10) {
            setValue();
        } else if (currentValue % (maxValue / 10) == 0) {
            setValue();
        }
    }

    private void setValue() {
        setCurrentTime();
        progressBar.setValue(currentValue);
        progressBar.paintImmediately(progressBar.getVisibleRect());
    }

    public int getValue() {
        return progressBar.getValue();
    }

    public void setMaximum(int max) {
        progressBar.setMaximum(max);
    }

    public void setMinimum(int min) {
        progressBar.setMinimum(min);
    }

    public void hideProgressBar() {
        progressBar.setVisible(false);
    }

    public void setText(String text) {
        statusField.setText(text);
    }

    public String getText() {
        return statusField.getText();
    }
}
