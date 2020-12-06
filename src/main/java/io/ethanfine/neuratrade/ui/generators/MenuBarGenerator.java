package io.ethanfine.neuratrade.ui.generators;

import io.ethanfine.neuratrade.Config;
import io.ethanfine.neuratrade.data.models.BarDataSeries;
import io.ethanfine.neuratrade.ui.State;
import io.ethanfine.neuratrade.ui.UIMain;
import io.ethanfine.neuratrade.util.CSVIO;
import org.apache.commons.io.FilenameUtils;
import org.jfree.data.io.CSV;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

public class MenuBarGenerator implements ActionListener {

    UIMain ui;
    JMenuItem importMenuItem;
    JMenuItem exportMenuItem;
    JMenuItem batchLabelMenuItem;

    // TODO: doc
    public MenuBarGenerator(UIMain ui) {
        this.ui = ui;
    }

    // TODO: doc
    public JMenuBar generateMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu("File");
        fileMenu.getAccessibleContext().setAccessibleDescription("The menu that contains options to import and export data.");
        menuBar.add(fileMenu);

        importMenuItem = new JMenuItem("Import data from CSV");
        importMenuItem.addActionListener(this);
        fileMenu.add(importMenuItem);
        exportMenuItem = new JMenuItem("Export data to CSV");
        exportMenuItem.addActionListener(this);
        fileMenu.add(exportMenuItem);
        batchLabelMenuItem = new JMenuItem("Batch label data");
        batchLabelMenuItem.addActionListener(this);
        fileMenu.add(batchLabelMenuItem);

        return menuBar;
    }

    // TODO: doc
    private void importCSVFile() {
        JFileChooser fileChooser = new JFileChooser();
        FileNameExtensionFilter fileFilter = new FileNameExtensionFilter("CSV files (*csv)","csv");
        fileChooser.setFileFilter(fileFilter);
        if  (fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
            BarDataSeries bdsFromCSV = CSVIO.readFile(fileChooser.getSelectedFile().getAbsolutePath());
            State.setImportedBDS(bdsFromCSV);
            ui.refresh();
        }
    }

    // TODO: doc
    private void exportCSVFile() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        fileChooser.showSaveDialog(null);
        fileChooser.setAcceptAllFileFilterUsed(false);
        if (fileChooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
            BarDataSeries bdsToWrite = State.getDisplayBDS();
            String filePath = fileChooser.getSelectedFile().getAbsolutePath();
            CSVIO.writeBarDataSeriesToFile(bdsToWrite, filePath);
            String successMessage = "Exported current bar data series to " + filePath + ".";
            // TODO: find out why a second file chooser is appearing; also remove file type chooser.
            JOptionPane.showMessageDialog(null, successMessage, "Exported CSV", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void batchLabelData() {
        JFileChooser fileChooser = new JFileChooser();
        FileNameExtensionFilter fileFilter = new FileNameExtensionFilter("CSV files (*csv)","csv");
        fileChooser.setFileFilter(fileFilter);
        fileChooser.setMultiSelectionEnabled(true);
        if (fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
            File[] selectedFiles = fileChooser.getSelectedFiles();
            for (int i = 0; i < selectedFiles.length; i++) {
                String filePath = selectedFiles[i].getAbsolutePath();
                BarDataSeries bdsFromCSV = CSVIO.readFile(selectedFiles[i].getAbsolutePath());
                String filePathNoExt = FilenameUtils.removeExtension(filePath);
                String writeFilePath = filePathNoExt + ",TDATA.csv";
                System.out.println(bdsFromCSV);
                CSVIO.writeBarDataSeriesToFile(bdsFromCSV, writeFilePath);
            }
            String successMessage = "Batch labeled " + selectedFiles.length + " data files. Each labeled file is now stored at <original file name>,TDATA.csv";
            // TODO: find out why a second file chooser is appearing; also remove file type chooser.
            JOptionPane.showMessageDialog(null, successMessage, "Batch Labeled Data ", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    // TODO: doc
    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == importMenuItem) {
            importCSVFile();
        } else if (e.getSource() == exportMenuItem) {
            exportCSVFile();
        } else if (e.getSource() == batchLabelMenuItem) {
            batchLabelData();
        }
    }

}
