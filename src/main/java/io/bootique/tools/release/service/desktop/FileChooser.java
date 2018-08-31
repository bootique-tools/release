
package io.bootique.tools.release.service.desktop;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JPanel;

class FileChooser extends JPanel implements ActionListener {

    private JFileChooser fileChooser;
    private volatile File selectedFile;

    FileChooser() {
        super(new BorderLayout());

        //Create a file chooser
        fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

        JButton openButton = new JButton("Select folder");
        openButton.addActionListener(this);

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(openButton);

        add(buttonPanel, BorderLayout.PAGE_START);
    }

    void select() {
        int returnVal = fileChooser.showOpenDialog(FileChooser.this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            selectedFile = fileChooser.getSelectedFile();
        }
    }

    public void actionPerformed(ActionEvent e) {
        select();
    }

    File getSelectedFile() {
        return selectedFile;
    }
}