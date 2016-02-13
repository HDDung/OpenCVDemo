package org.opencv.demo.gui.swing;

import org.opencv.demo.core.RecognizerManager;
import org.opencv.demo.gui.Utils;
import org.opencv.demo.misc.Constants;
import org.opencv.demo.misc.SwingLogger;
import org.opencv.demo.core.DetectorsManager;
import org.opencv.core.Core;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.Map;


public class OpenCvDemo extends JFrame {

    static final long serialVersionUID = 0;
    private WebcamPanel webcamPanel;

    private JTextArea consoleOutputTextArea;
    private JLabel statusBar;
    private SwingLogger logger = new SwingLogger(this);
    private DetectorsManager detectorsManager = new DetectorsManager(logger);

    public OpenCvDemo() throws Exception {
        super("OpenCV Demo");
        Utils.loadClassifiers("/home/andrea/Downloads/opencv-3.1.0/data");
        UIManager.setLookAndFeel("com.sun.java.swing.plaf.gtk.GTKLookAndFeel");

        setSize(800, 800);
        Utils.centerFrame(this);

        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        // menus
        setJMenuBar(createMenuBar());

        // main panels
        consoleOutputTextArea = new JTextArea(Constants.MINERVAI_COMPLETE);
        consoleOutputTextArea.setEditable(false);
        webcamPanel = new WebcamPanel(logger, detectorsManager);
        webcamPanel.startCamera();

        JScrollPane consoleScrollPane = new JScrollPane(consoleOutputTextArea, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        consoleScrollPane.setWheelScrollingEnabled(true);

        // creates the divider for the two panes
        JSplitPane spDivider = new JSplitPane(JSplitPane.VERTICAL_SPLIT, webcamPanel, consoleScrollPane);
        spDivider.setDividerLocation(600);
        spDivider.setOneTouchExpandable(true);
        getContentPane().add(spDivider, BorderLayout.CENTER);

        // sets the status bar
        statusBar = new JLabel(" Ready");
        getContentPane().add("South", statusBar);

        // repaints everything and starts
        setVisible(true);
    }

    private JMenuBar createMenuBar() {

        JMenuBar menuBar = new JMenuBar();

        // MENU FILE
        JMenu menu = new JMenu("File");
        menuBar.add(menu);
        menu.setMnemonic(KeyEvent.VK_F);

        JMenuItem item = new JMenuItem("Exit");
        item.setMnemonic(KeyEvent.VK_X);
        item.addActionListener(e -> System.exit(0));
        menu.add(item);

        menu = new JMenu("Detection");
        menuBar.add(menu);
        menu.setMnemonic(KeyEvent.VK_D);

        // MENU CLASSIFIERS
        Map<String, String[]> dataFiles = Utils.loadClassifiers("/home/andrea/dev/code/java/OpenCVDemo/src/main/resources/data/");

        for (String classifierName : dataFiles.keySet()) {
            JMenu classifierSubMenu = new JMenu(classifierName);
            for (String name : dataFiles.get(classifierName)) {
                JCheckBoxMenuItem menuItem = new JCheckBoxMenuItem(name);
                menuItem.addActionListener(e -> changeClassifier(
                        ((JCheckBoxMenuItem) e.getSource()).isSelected(),
                        File.separator + "data" + File.separator + classifierName + File.separator + name));
                classifierSubMenu.add(menuItem);
            }
            menu.add(classifierSubMenu);
        }

        menu = new JMenu("Face Recognition");
        menuBar.add(menu);
        menu.setMnemonic(KeyEvent.VK_R);

        item = new JMenuItem("Capture new user");
        item.setMnemonic(KeyEvent.VK_C);
        item.addActionListener(e -> {

            if (checkForClassifier()) {
                UserCaptureDialog userCaptureDialog = new UserCaptureDialog(detectorsManager);
                userCaptureDialog.setVisible(true);
            }
        });
        menu.add(item);

        item = new JMenuItem("Start recognition");
        item.setMnemonic(KeyEvent.VK_S);
        item.addActionListener(e -> {

            // for recognizing a face, we need only the face classifier
            detectorsManager.clear();
            detectorsManager.addDetector(Constants.DEFAULT_FACE_CLASSIFIER);
            detectorsManager.changeRecognizerStatus();
        });

        menu.add(item);
        menuBar.add(menu);

        return menuBar;
    }

    private boolean checkForClassifier() {

        if (!detectorsManager.hasDetector(Constants.DEFAULT_FACE_CLASSIFIER)
                || detectorsManager.getDetectors().size() != 1) {
            int resp = JOptionPane.showConfirmDialog(this, "For starting user capture only the default face classifier has to be set.\nDo you want to set it?", "Set face classifier", JOptionPane.YES_NO_OPTION);
            if (resp == JOptionPane.YES_OPTION) {
                detectorsManager.clear();
                detectorsManager.addDetector(Constants.DEFAULT_FACE_CLASSIFIER);
            }
            else {
                return false;
            }
        }
        return true;
    }

    private void changeClassifier(boolean isSelected, String resourceName) {

        if (! isSelected) {
            webcamPanel.removeClassifier(resourceName);
        }
        else {
            webcamPanel.addClassifier(resourceName);
        }
    }

    public void consoleLog(String message) {
        consoleOutputTextArea.append("\n" + message);
        consoleOutputTextArea.repaint();
    }

    public static void main(String[] args) throws Exception {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        new OpenCvDemo();
    }
}