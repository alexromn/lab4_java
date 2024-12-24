package src;

import java.awt.BorderLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.io.*;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;

public class Plot extends JFrame {
    private static final int WIDTH = 800;
    private static final int HEIGHT = 600;
    private JFileChooser fileChooser = null;
    private final JCheckBoxMenuItem showAxisMenuItem;
    private final JCheckBoxMenuItem showMarkersMenuItem;
    private final JCheckBoxMenuItem RotateMenuItem;
    private final GraphicsDisplay display = new GraphicsDisplay();
    private boolean fileLoaded = false;

    private final Action ResetAction;
    private final Action SaveAction;
    public Plot() {
        super("Plotting");
        setSize(WIDTH, HEIGHT);
        Toolkit kit = Toolkit.getDefaultToolkit();
        setLocation((kit.getScreenSize().width - WIDTH)/2,
                (kit.getScreenSize().height - HEIGHT)/2);
        setExtendedState(MAXIMIZED_BOTH);
        JMenuBar menuBar = new JMenuBar();
        setJMenuBar(menuBar);
        JMenu fileMenu = new JMenu("File");
        menuBar.add(fileMenu);
        Action openPlotAction = new AbstractAction("Open file with plot data") {
            public void actionPerformed(ActionEvent event) {
                if (fileChooser==null) {
                    fileChooser = new JFileChooser();
                    fileChooser.setCurrentDirectory(new File("."));
                }
                if (fileChooser.showOpenDialog(Plot.this) ==
                        JFileChooser.APPROVE_OPTION) ;
                openGraphics(fileChooser.getSelectedFile());
            }
        };
        fileMenu.add(openPlotAction);

        SaveAction = new AbstractAction("Save changed data") {
            public void actionPerformed(ActionEvent event) {
                if (fileChooser == null) {
                    fileChooser = new JFileChooser();
                    fileChooser.setCurrentDirectory(new File("."));
                }
                if (fileChooser.showSaveDialog(Plot.this) == JFileChooser.APPROVE_OPTION) {
                    SaveFile(fileChooser.getSelectedFile());
                }
            }
        };
        fileMenu.add(SaveAction);
        SaveAction.setEnabled(false);

        ResetAction = new AbstractAction("Reset") {
            public void actionPerformed(ActionEvent event) {
                display.reset();
            }
        };
        fileMenu.add(ResetAction);
        ResetAction.setEnabled(false);

        JMenu graphicsMenu = new JMenu("Plot");
        menuBar.add(graphicsMenu);
        Action showAxisAction = new AbstractAction("Show axis") {
            public void actionPerformed(ActionEvent event) {
                display.setShowAxis(showAxisMenuItem.isSelected());
            }
        };
        showAxisMenuItem = new JCheckBoxMenuItem(showAxisAction);
        graphicsMenu.add(showAxisMenuItem);
        showAxisMenuItem.setSelected(true);


        Action RotateAction = new AbstractAction("Rotate") {
            public void actionPerformed(ActionEvent event) {
                display.setRotate(RotateMenuItem.isSelected());
            }
        };
        RotateMenuItem = new JCheckBoxMenuItem(RotateAction);
        graphicsMenu.add(RotateMenuItem);
        RotateMenuItem.setSelected(false);
        RotateMenuItem.setEnabled(false);

        Action showMarkersAction = new AbstractAction("Show markers") {
            public void actionPerformed(ActionEvent event) {
                display.setShowMarkers(showMarkersMenuItem.isSelected());
            }
        };
        showMarkersMenuItem = new JCheckBoxMenuItem(showMarkersAction);
        graphicsMenu.add(showMarkersMenuItem);
        showMarkersMenuItem.setSelected(true);
        graphicsMenu.addMenuListener(new GraphicsMenuListener());
        getContentPane().add(display, BorderLayout.CENTER);
    }

    protected void openGraphics(File selectedFile) {
        try {
            DataInputStream in = new DataInputStream(new FileInputStream(selectedFile));


            Double[][] graphicsData = new Double[in.available()/(Double.SIZE/8)/2][];
            Double[][] originalData = new Double[in.available()/(Double.SIZE/8)/2][];
            int i = 0;
            while (in.available()>0) {
                double x = in.readDouble();
                double y = in.readDouble();
                graphicsData[i] = new Double[]{x, y};
                originalData[i++] = new Double[]{x, y};
            }
            if (graphicsData.length>0) {
                fileLoaded = true;
                display.displayGraphics(graphicsData, originalData);
            }
            in.close();
        } catch (FileNotFoundException ex) {
            JOptionPane.showMessageDialog(Plot.this, "File wasn't found",
                    "Can't upload data",
                    JOptionPane.WARNING_MESSAGE);
            ResetAction.setEnabled(false);
            SaveAction.setEnabled(false);
            RotateMenuItem.setEnabled(false);
            return;
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(Plot.this, "Error while reading data from file",
                    "Can't upload data",
                    JOptionPane.WARNING_MESSAGE);
            ResetAction.setEnabled(false);
            RotateMenuItem.setEnabled(false);
            SaveAction.setEnabled(false);
            return;
        }
        RotateMenuItem.setEnabled(true);
        ResetAction.setEnabled(true);
        SaveAction.setEnabled(true);
    }

    private class GraphicsMenuListener implements MenuListener {
        public void menuSelected(MenuEvent e) {
            showAxisMenuItem.setEnabled(fileLoaded);
            showMarkersMenuItem.setEnabled(fileLoaded);
        }
        public void menuDeselected(MenuEvent e) {

        }
        public void menuCanceled(MenuEvent e) {

        }
    }


    private void SaveFile(File selectedFile){
        try {
            DataOutputStream out = new DataOutputStream(new FileOutputStream(selectedFile));
            Double[][] data = display.getGraphicsData();
            for (Double[] dat : data) {
                out.writeDouble((Double) dat[0]);
                out.writeDouble((Double) dat[1]);
            }
            out.close();
        } catch (Exception err) {
            //поф
        }
    }
}
