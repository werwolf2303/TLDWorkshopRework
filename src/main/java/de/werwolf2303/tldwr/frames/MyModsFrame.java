package de.werwolf2303.tldwr.frames;

import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;
import de.werwolf2303.tldwr.Events;
import de.werwolf2303.tldwr.PublicValues;
import de.werwolf2303.tldwr.TLDWREvents;
import de.werwolf2303.tldwr.config.ConfigValues;
import de.werwolf2303.tldwr.swingextensions.ModDisplay;
import de.werwolf2303.tldwr.swingextensions.ModDisplayModule;
import de.werwolf2303.tldwr.workshop.WorkshopAPI;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;

public class MyModsFrame {
    private JPanel bottomBar;
    private JLabel currentPage;
    private JButton pagePreviousButton;
    private JButton pageNextButton;
    private JLabel itemsPerPageLabel;
    private JSpinner itemsPerPage;
    private JButton backButton;
    private JButton deleteButton;
    private JPanel topBar;
    private JButton homeButton;
    private JButton refreshButton;
    private JScrollPane modDisplayScrollPanel;
    private ModDisplay modDisplay;
    private JButton modsFolderButton;
    private JPanel contentPanel;
    private WorkshopFrame workshopFrame;
    private int currentPageNumber = 0;
    private ModDisplayModule lastHighlightedModBackup;

    public MyModsFrame(WorkshopFrame frame) {
        this.workshopFrame = frame;

        $$$setupUI$$$();
        backButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                hide();
            }
        });

        modsFolderButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (Desktop.isDesktopSupported()) {
                    try {
                        Desktop.getDesktop().open(new File(PublicValues.tldUserPath, "Mods"));
                    } catch (IOException ex) {
                        ex.printStackTrace();
                        JOptionPane.showMessageDialog(null, "Failed to open directory");
                    }
                } else {
                    JOptionPane.showMessageDialog(null, "Not supported on your Operating System");
                }
            }
        });

        modDisplayScrollPanel.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        itemsPerPage.setValue(PublicValues.config.getInt(ConfigValues.numberOfModsPerPage.name));

        itemsPerPage.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                if (((int) itemsPerPage.getValue()) == 0) itemsPerPage.setValue(1);
                if (((int) itemsPerPage.getValue()) >= 998) itemsPerPage.setValue(998);

                currentPageNumber = 1;
                try {
                    currentPage.setText("Page: " + currentPageNumber + "/" + (Math.round(Float.parseFloat(String.valueOf((WorkshopAPI.getInstalledMods().size() / (int) itemsPerPage.getValue()) + 1)))));
                } catch (IOException ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(null, "Failed to update page info");
                }
                modDisplay.removeMods();
                try {
                    for (WorkshopAPI.Mod mod : WorkshopAPI.getInstalledMods(currentPageNumber - 1, (int) itemsPerPage.getValue())) {
                        modDisplay.addMod(mod, true);
                    }
                } catch (IOException ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(null, "Failed to display mods");
                }
            }
        });

        refreshButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                modDisplay.removeMods();

                WorkshopAPI.reloadMods();

                try {
                    for (WorkshopAPI.Mod mod : WorkshopAPI.getInstalledMods((currentPageNumber - 1) * (int) itemsPerPage.getValue(), (int) itemsPerPage.getValue())) {
                        modDisplay.addMod(mod, true);
                    }
                } catch (IOException ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(null, "Failed to display mods");
                }

                modDisplay.modRefreshRespectSize();

                contentPanel.revalidate();
                contentPanel.repaint();
            }
        });

        pageNextButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    if (currentPageNumber + 1 > (Math.round(Float.parseFloat(String.valueOf((WorkshopAPI.getInstalledMods().size() / (int) itemsPerPage.getValue()) + 1))))) {
                        return;
                    }
                } catch (IOException ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(null, "Failed to calculate max forward number");
                }
                currentPageNumber++;
                try {
                    currentPage.setText("Page: " + currentPageNumber + "/" + (Math.round(Float.parseFloat(String.valueOf((WorkshopAPI.getInstalledMods().size() / (int) itemsPerPage.getValue()) + 1)))));
                } catch (IOException ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(null, "Failed to update page info");
                }
                modDisplay.removeMods();
                try {
                    for (WorkshopAPI.Mod mod : WorkshopAPI.getInstalledMods((currentPageNumber - 1) * (int) itemsPerPage.getValue(), (int) itemsPerPage.getValue())) {
                        modDisplay.addMod(mod, true);
                    }
                } catch (IOException ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(null, "Failed to display mods");
                }

                modDisplay.modRefreshRespectSize();
            }
        });

        pagePreviousButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (currentPageNumber - 1 < 1) {
                    return;
                }
                currentPageNumber--;
                try {
                    currentPage.setText("Page: " + currentPageNumber + "/" + (Math.round(Float.parseFloat(String.valueOf((WorkshopAPI.getInstalledMods().size() / (int) itemsPerPage.getValue()) + 1)))));
                } catch (IOException ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(null, "Failed to update page info");
                }
                modDisplay.removeMods();
                try {
                    for (WorkshopAPI.Mod mod : WorkshopAPI.getInstalledMods(currentPageNumber - 1, (int) itemsPerPage.getValue())) {
                        modDisplay.addMod(mod, true);
                    }
                } catch (IOException ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(null, "Failed to display mods");
                }

                modDisplay.modRefreshRespectSize();
            }
        });

        deleteButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    WorkshopAPI.removeModFromMyMods(PublicValues.lastHighlightedMod.mod);
                    refreshButton.getActionListeners()[refreshButton.getActionListeners().length - 1].actionPerformed(null); //Refresh mods
                    workshopFrame.refreshButton.getActionListeners()[workshopFrame.refreshButton.getActionListeners().length - 1].actionPerformed(null); //Refresh mods in workshop
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(null, "Failed to remove mod");
                }
            }
        });

        homeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                hide();
                PublicValues.mainFrame.switchView(MainFrame.Views.Home);
            }
        });
    }

    private void createUIComponents() {
        modDisplay = new ModDisplay(new JPanel(), false);
    }

    private Runnable onModSelected = new Runnable() {
        @Override
        public void run() {
            deleteButton.setEnabled(true);
        }
    };

    private Runnable onModUnselected = new Runnable() {
        @Override
        public void run() {
            deleteButton.setEnabled(false);
        }
    };

    private Runnable onModLoadFinished = new Runnable() {
        @Override
        public void run() {
            SwingUtilities.invokeLater(() -> modDisplayScrollPanel.getVerticalScrollBar().setValue(0));
        }
    };


    public JPanel getContentPanel() {
        return contentPanel;
    }


    public void showIt() {
        if (PublicValues.lastHighlightedMod != null) lastHighlightedModBackup = PublicValues.lastHighlightedMod;

        //List mods
        modDisplay.removeMods();

        currentPageNumber = 1;

        try {
            currentPage.setText("Page: " + currentPageNumber + "/" + (Math.round(Float.parseFloat(String.valueOf((WorkshopAPI.getInstalledMods().size() / (int) itemsPerPage.getValue()) + 1)))));
        } catch (IOException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(null, "Failed to update page info");
            return;
        }

        try {
            for (WorkshopAPI.Mod mod : WorkshopAPI.getInstalledMods((currentPageNumber - 1) * (int) itemsPerPage.getValue(), (int) itemsPerPage.getValue())) {
                modDisplay.addMod(mod, true);
            }
        } catch (IOException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(null, "Failed to display mods");
            return;
        }

        Events.subscribe(TLDWREvents.MOD_SELECTED.getName(), onModSelected);
        Events.subscribe(TLDWREvents.MOD_UNSELECTED.getName(), onModUnselected);
        Events.subscribe(TLDWREvents.MODLOAD_FINISHED.getName(), onModLoadFinished);


        workshopFrame.topBar.setVisible(false);
        workshopFrame.bottomBar.setVisible(false);
        workshopFrame.modDisplay.setVisible(false);
        contentPanel.setVisible(true);

        modDisplay.modRefreshRespectSize();
    }

    public void hide() {
        if (lastHighlightedModBackup != null) PublicValues.lastHighlightedMod = lastHighlightedModBackup;

        Events.unsubscribe(TLDWREvents.MOD_SELECTED.getName(), onModSelected);
        Events.unsubscribe(TLDWREvents.MOD_UNSELECTED.getName(), onModUnselected);
        Events.unsubscribe(TLDWREvents.MODLOAD_FINISHED.getName(), onModLoadFinished);

        contentPanel.setVisible(false);
        workshopFrame.topBar.setVisible(true);
        workshopFrame.bottomBar.setVisible(true);
        workshopFrame.modDisplay.setVisible(true);

        workshopFrame.refreshButton.doClick();
    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$() {
        createUIComponents();
        contentPanel = new JPanel();
        contentPanel.setLayout(new GridLayoutManager(3, 1, new Insets(0, 0, 0, 0), -1, -1));
        contentPanel.setMinimumSize(new Dimension(-1, -1));
        contentPanel.setPreferredSize(new Dimension(-1, -1));
        bottomBar = new JPanel();
        bottomBar.setLayout(new GridLayoutManager(2, 5, new Insets(0, 0, 0, 0), -1, -1));
        contentPanel.add(bottomBar, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final JPanel panel1 = new JPanel();
        panel1.setLayout(new GridLayoutManager(1, 3, new Insets(0, 0, 0, 0), -1, -1));
        bottomBar.add(panel1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        currentPage = new JLabel();
        currentPage.setText("Page: 0/0");
        panel1.add(currentPage, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        pagePreviousButton = new JButton();
        pagePreviousButton.setText("Previous");
        panel1.add(pagePreviousButton, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        pageNextButton = new JButton();
        pageNextButton.setText("Next");
        panel1.add(pageNextButton, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JPanel panel2 = new JPanel();
        panel2.setLayout(new GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1));
        bottomBar.add(panel2, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        itemsPerPageLabel = new JLabel();
        itemsPerPageLabel.setText("Items per page:");
        panel2.add(itemsPerPageLabel, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        itemsPerPage = new JSpinner();
        panel2.add(itemsPerPage, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer1 = new Spacer();
        bottomBar.add(spacer1, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        final Spacer spacer2 = new Spacer();
        bottomBar.add(spacer2, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        backButton = new JButton();
        backButton.setDoubleBuffered(true);
        backButton.setEnabled(true);
        backButton.setFocusCycleRoot(false);
        backButton.setText("Back");
        bottomBar.add(backButton, new GridConstraints(0, 3, 2, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, new Dimension(-1, 50), new Dimension(-1, 50), new Dimension(-1, 50), 0, false));
        deleteButton = new JButton();
        deleteButton.setEnabled(false);
        deleteButton.setText("Delete");
        bottomBar.add(deleteButton, new GridConstraints(0, 4, 2, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, new Dimension(-1, 50), new Dimension(-1, 50), new Dimension(-1, 50), 0, false));
        modsFolderButton = new JButton();
        modsFolderButton.setText("Mods Folder");
        bottomBar.add(modsFolderButton, new GridConstraints(0, 2, 2, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, new Dimension(-1, 50), new Dimension(-1, 50), new Dimension(-1, 50), 0, false));
        topBar = new JPanel();
        topBar.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        contentPanel.add(topBar, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final JPanel panel3 = new JPanel();
        panel3.setLayout(new GridLayoutManager(1, 3, new Insets(0, 0, 0, 0), -1, -1));
        topBar.add(panel3, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        homeButton = new JButton();
        homeButton.setText("Home");
        panel3.add(homeButton, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        refreshButton = new JButton();
        refreshButton.setText("Refresh");
        panel3.add(refreshButton, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer3 = new Spacer();
        panel3.add(spacer3, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        modDisplayScrollPanel = new JScrollPane();
        contentPanel.add(modDisplayScrollPanel, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        modDisplayScrollPanel.setViewportView(modDisplay);
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return contentPanel;
    }
}
