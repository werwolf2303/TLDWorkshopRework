package de.werwolf2303.tldwr.frames;

import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;
import de.werwolf2303.tldwr.Events;
import de.werwolf2303.tldwr.PublicValues;
import de.werwolf2303.tldwr.TLDWREvents;
import de.werwolf2303.tldwr.swingextensions.JCheckBoxList;
import de.werwolf2303.tldwr.workshop.WorkshopAPI;
import org.apache.commons.io.IOUtils;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Modpacks {
    public JPanel contentPanel;
    private JPanel modpackEditorView;
    private JButton mdvHomeButton;
    private JTextField nameTextField;
    private JButton createModpackTxtButton;
    private JButton importModpackTxtButton;
    private JButton browseAvailableModpacksButton;
    private JButton backButton;
    private JScrollPane modListScrollPane;
    private JProgressBar progressBar;
    private JCheckBoxList modListTree;
    private Map<String, Boolean> mods = new HashMap<>();

    public Modpacks() {
        mdvHomeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                PublicValues.mainFrame.switchView(MainFrame.Views.Home);
            }
        });

        modListTree = new JCheckBoxList();
        modListTree.addCheckBoxListener(new JCheckBoxList.CheckBoxListener() {
            @Override
            public void selectionChange(JCheckBox checkBox) {
                mods.put(checkBox.getText(), checkBox.isSelected());
            }
        });
        modListTree.setSelectionBackground(Color.decode("#0178d7"));
        modListTree.setSelectionForeground(Color.white);

        modListScrollPane.setViewportView(modListTree);

        progressBar.setVisible(false);
        progressBar.setMinimum(0);
        progressBar.setMaximum(100);
        progressBar.setValue(0);

        browseAvailableModpacksButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                PublicValues.mainFrame.modPackList.load();
                PublicValues.mainFrame.switchView(MainFrame.Views.ModpacksList);
            }
        });

        contentPanel.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                modListTree.setSize(modListScrollPane.getWidth(), modListScrollPane.getHeight());
            }
        });

        backButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                PublicValues.mainFrame.navigateBack();
            }
        });

        createModpackTxtButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                StringBuilder fileBuilder = new StringBuilder();
                for (Object object : modListTree.getModel().toArray()) {
                    JCheckBox checkbox = (JCheckBox) object;
                    if (checkbox.isSelected()) {
                        fileBuilder.append(checkbox.getText()).append("\n");
                    }
                }
                try {
                    FileOutputStream stream = new FileOutputStream(new File(Paths.get(PublicValues.tldUserPath, "Modpacks", nameTextField.getText() + ".txt").toUri()));
                    stream.write(fileBuilder.toString().getBytes());
                    stream.close();

                    JOptionPane.showMessageDialog(null, "Modpack created in: " + Paths.get(PublicValues.tldUserPath, "Modpacks", nameTextField.getText() + ".txt"));
                } catch (IOException ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(null, "Failed saving Modpack file: " + ex.getMessage());
                }
            }
        });

        browseAvailableModpacksButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                PublicValues.mainFrame.modPackList.load();
                PublicValues.mainFrame.switchView(MainFrame.Views.ModpacksList);
            }
        });

        importModpackTxtButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser chooser = new JFileChooser();
                chooser.setDialogTitle("Open Modpack File");
                chooser.setFileHidingEnabled(false);
                chooser.setCurrentDirectory(new File(PublicValues.tldUserPath));
                chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
                chooser.setFileFilter(new FileFilter() {
                    @Override
                    public boolean accept(File f) {
                        return f.getName().toLowerCase().endsWith(".txt") || f.isDirectory();
                    }

                    @Override
                    public String getDescription() {
                        return "TLD Modpack";
                    }
                });
                if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                    File selectedFile = chooser.getSelectedFile();
                    new Thread(() -> {
                        progressBar.setVisible(true);

                        WorkshopAPI.runWithModPack(selectedFile.getAbsolutePath(), new WorkshopAPI.DownloadProgressRunnable() {
                            @Override
                            public void run(double percentage) {
                                progressBar.setValue((int) percentage);
                            }
                        });
                    }, "Modpack Import").start();
                }
            }
        });

        Events.subscribe(TLDWREvents.DOWNLOAD_FINISHED.getName(), new Runnable() {
            @Override
            public void run() {
                if (!contentPanel.isVisible()) return;
                progressBar.setVisible(false);
            }
        });
    }

    public void loadMods() {
        try {
            for (WorkshopAPI.Mod mod : WorkshopAPI.getInstalledMods()) {
                JCheckBox checkBox = new JCheckBox(mod.FileName);
                checkBox.setSelected(true);
                modListTree.getModel().addElement(checkBox);
                mods.put(mod.FileName, true);
            }
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(contentPanel, "Failed to load mods");
        }
    }

    {
// GUI initializer generated by IntelliJ IDEA GUI Designer
// >>> IMPORTANT!! <<<
// DO NOT EDIT OR ADD ANY CODE HERE!
        $$$setupUI$$$();
    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$() {
        contentPanel = new JPanel();
        contentPanel.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        modpackEditorView = new JPanel();
        modpackEditorView.setLayout(new GridLayoutManager(2, 2, new Insets(5, 5, 5, 5), -1, -1));
        contentPanel.add(modpackEditorView, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        mdvHomeButton = new JButton();
        mdvHomeButton.setText("Home");
        modpackEditorView.add(mdvHomeButton, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer1 = new Spacer();
        modpackEditorView.add(spacer1, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        final JPanel panel1 = new JPanel();
        panel1.setLayout(new GridLayoutManager(8, 1, new Insets(30, 30, 30, 30), -1, -1));
        modpackEditorView.add(panel1, new GridConstraints(1, 0, 1, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final JLabel label1 = new JLabel();
        label1.setText("ModPack Name:");
        panel1.add(label1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        nameTextField = new JTextField();
        nameTextField.setText("SampleModpack");
        panel1.add(nameTextField, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        final JLabel label2 = new JLabel();
        label2.setText("Mods to include (From your Downloaded Mods, Only Workshop Mods!):");
        panel1.add(label2, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JPanel panel2 = new JPanel();
        panel2.setLayout(new GridLayoutManager(2, 3, new Insets(0, 0, 0, 0), -1, -1));
        panel1.add(panel2, new GridConstraints(4, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        createModpackTxtButton = new JButton();
        createModpackTxtButton.setText("Create Modpack (.txt)");
        panel2.add(createModpackTxtButton, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, new Dimension(-1, 35), new Dimension(-1, 35), new Dimension(-1, 35), 0, false));
        importModpackTxtButton = new JButton();
        importModpackTxtButton.setText("Import Modpack (.txt)");
        panel2.add(importModpackTxtButton, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, new Dimension(-1, 35), new Dimension(-1, 35), new Dimension(-1, 35), 0, false));
        browseAvailableModpacksButton = new JButton();
        browseAvailableModpacksButton.setText("Browse available Modpacks");
        panel2.add(browseAvailableModpacksButton, new GridConstraints(1, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, new Dimension(-1, 35), new Dimension(-1, 35), new Dimension(-1, 35), 0, false));
        final Spacer spacer2 = new Spacer();
        panel2.add(spacer2, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, new Dimension(-1, 15), new Dimension(-1, 15), new Dimension(-1, 15), 0, false));
        backButton = new JButton();
        backButton.setText("Back");
        panel1.add(backButton, new GridConstraints(6, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer3 = new Spacer();
        panel1.add(spacer3, new GridConstraints(5, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        modListScrollPane = new JScrollPane();
        panel1.add(modListScrollPane, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        progressBar = new JProgressBar();
        panel1.add(progressBar, new GridConstraints(7, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return contentPanel;
    }
}
