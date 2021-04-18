/*::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
 Copyright (c) 2021.
 Project: Projet 2A
 Author: Edwin HELET & Julien GUY
 Class: ResultsDialog.java
 :::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::*/

package fr.hedwin.swing.window;

import fr.hedwin.swing.panel.result.ResultPanel;

import javax.swing.*;

import java.awt.event.*;

public class ResultsDialog extends JDialog {

    public <T> ResultsDialog(JFrame parent, String title, boolean modal, ResultPanel<T> futureResult) {
        super(parent, title, modal);
        initComponents(futureResult);
    }

    public <T> ResultsDialog(JDialog parent, boolean modal, ResultPanel<T> futureResult) {
        super(parent, modal);
        initComponents(futureResult);
    }

    private <T> void initComponents(ResultPanel<T> result) {
        //jPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                result.onClose();
            }
        });
        result.getLoadDataBar().close();
        add(result);
        pack();
        setLocationRelativeTo(null);
    }

}