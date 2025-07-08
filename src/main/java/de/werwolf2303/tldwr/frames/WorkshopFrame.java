package de.werwolf2303.tldwr.frames;

import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;
import de.werwolf2303.tldwr.Events;
import de.werwolf2303.tldwr.PublicValues;
import de.werwolf2303.tldwr.TLDWREvents;
import de.werwolf2303.tldwr.config.ConfigValues;
import de.werwolf2303.tldwr.search.SearchEngine;
import de.werwolf2303.tldwr.swingextensions.ModDisplay;
import de.werwolf2303.tldwr.workshop.WorkshopAPI;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.util.ArrayList;

public class WorkshopFrame {
    public JFrame frame;
    public JPanel contentPanel;
    public JButton homeButton;
    public JButton refreshButton;
    public JComboBox categories;
    public JTextField searchField;
    public JLabel searchLabel;
    public JButton pagePreviousButton;
    public JButton pageNextButton;
    public JSpinner itemsPerPage;
    public JButton modPackManagerButton;
    public JButton myModsButton;
    public JButton downloadButton;
    public JLabel currentPage;
    public JLabel itemsPerPageLabel;
    public ModDisplay modDisplay;
    public JPanel modDisplayScrollPanelPanel;
    public JPanel bottomBar;
    public JPanel topBar;
    public int currentPageNumber = 1;
    private ArrayList<String> categoriesList;
    private SearchEngine searchEngine;
    private boolean bypassCategoryUpdate = false;

    public WorkshopFrame() {
        $$$setupUI$$$();
        searchEngine = new SearchEngine(categories);
        categoriesList = new ArrayList<>();

        itemsPerPage.setValue(PublicValues.config.getInt(ConfigValues.numberOfModsPerPage.name));

        itemsPerPage.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                if (((int) itemsPerPage.getValue()) == 0) itemsPerPage.setValue(1); // Min
                if (((int) itemsPerPage.getValue()) > 998) itemsPerPage.setValue(998); // Max
                new Thread(() -> {
                    modDisplay.beginLoading();

                    currentPageNumber = 1;

                    if (!categories.getSelectedItem().toString().equals("All Categories")) {

                        ArrayList<WorkshopAPI.Mod> mods = WorkshopAPI.getModsInCategory(0, (int) itemsPerPage.getValue(), categories.getSelectedItem().toString());
                        int modsInCategory = WorkshopAPI.getAmountOfModsInCategory(categories.getSelectedItem().toString());

                        SwingUtilities.invokeLater(() -> {
                            currentPage.setText("Page: " + currentPageNumber + "/" + (Math.round(Float.parseFloat(String.valueOf((modsInCategory / (int) itemsPerPage.getValue()) + 1)))));

                            modDisplay.removeMods();

                            for (WorkshopAPI.Mod mod : mods) {
                                modDisplay.addMod(mod, false);
                            }

                            modDisplay.modRefreshRespectSize();
                        });
                        return;
                    }

                    ArrayList<WorkshopAPI.Mod> mods = WorkshopAPI.getMods(currentPageNumber - 1, (int) itemsPerPage.getValue());
                    int modsInCategory = WorkshopAPI.getAllMods().size();

                    SwingUtilities.invokeLater(() -> {
                        currentPage.setText("Page: " + currentPageNumber + "/" + (Math.round(Float.parseFloat(String.valueOf((modsInCategory / (int) itemsPerPage.getValue()) + 1)))));

                        modDisplay.removeMods();

                        for (WorkshopAPI.Mod mod : mods) {
                            modDisplay.addMod(mod, false);
                        }

                        modDisplay.modRefreshRespectSize();
                    });
                }, "Mod per page").start();
            }
        });

        new Thread(() -> {
            modDisplay.beginLoading();

            ArrayList<WorkshopAPI.Mod> mods = WorkshopAPI.getMods(0, (int) itemsPerPage.getValue());
            int modsInCategory = WorkshopAPI.getAllMods().size();

            SwingUtilities.invokeLater(() -> {
                for (WorkshopAPI.Mod mod : mods) {
                    if (!categoriesList.contains(mod.Category)) categoriesList.add(mod.Category);
                    modDisplay.addMod(mod, false);
                }

                refreshCategories();

                currentPage.setText("Page: " + currentPageNumber + "/" + (Math.round(Float.parseFloat(String.valueOf((modsInCategory / (int) itemsPerPage.getValue()) + 1)))));

                modDisplay.modRefreshRespectSize();
            });
        }, "Mods loader").start();

        MyModsFrame myModsFrame = new MyModsFrame(this);
        myModsFrame.getContentPanel().setVisible(false);
        GridConstraints myModsFrameConstraints = new GridConstraints();
        myModsFrameConstraints.setFill(GridConstraints.FILL_BOTH);
        modDisplayScrollPanelPanel.add(myModsFrame.getContentPanel(), myModsFrameConstraints);

        modPackManagerButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                PublicValues.mainFrame.modpacks.loadMods();
                PublicValues.mainFrame.switchView(MainFrame.Views.Modpacks);
            }
        });

        myModsButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                myModsFrame.showIt();
            }
        });

        downloadButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                downloadButton.setEnabled(false);
                PublicValues.lastHighlightedMod.download();
            }
        });

        refreshButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new Thread(() -> {
                    modDisplay.beginLoading();
                    if (!categories.getSelectedItem().toString().equals("All Categories")) {

                        ArrayList<WorkshopAPI.Mod> mods = WorkshopAPI.getModsInCategory((currentPageNumber - 1) * (int) itemsPerPage.getValue(), (int) itemsPerPage.getValue(), categories.getSelectedItem().toString());

                        SwingUtilities.invokeLater(() -> {
                            modDisplay.removeMods();

                            for (WorkshopAPI.Mod mod : mods) {
                                modDisplay.addMod(mod, false);
                            }

                            modDisplay.modRefreshRespectSize();
                        });
                        return;
                    }

                    WorkshopAPI.reloadMods();
                    categoriesList.clear();

                    ArrayList<WorkshopAPI.Mod> mods;
                    ArrayList<WorkshopAPI.Mod> paginatedMods;
                    mods = WorkshopAPI.getAllMods();
                    paginatedMods = WorkshopAPI.getMods((currentPageNumber - 1) * (int) itemsPerPage.getValue(), (int) itemsPerPage.getValue());

                    SwingUtilities.invokeLater(() -> {
                        modDisplay.removeMods();

                        for (WorkshopAPI.Mod mod : mods) {
                            if (!categoriesList.contains(mod.Category)) categoriesList.add(mod.Category);
                        }

                        refreshCategories();

                        for (WorkshopAPI.Mod mod : paginatedMods) {
                            modDisplay.addMod(mod, false);
                        }

                        modDisplay.modRefreshRespectSize();
                    });
                }, "Mods refresh").start();
            }
        });

        pageNextButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new Thread(() -> {
                    if (currentPageNumber + 1 > (Math.round(Float.parseFloat(String.valueOf((WorkshopAPI.getAllMods().size() / (int) itemsPerPage.getValue()) + 1))))) {
                        return;
                    }

                    modDisplay.beginLoading();
                    currentPageNumber++;

                    if (!categories.getSelectedItem().toString().equals("All Categories")) {
                        ArrayList<WorkshopAPI.Mod> mods = WorkshopAPI.getModsInCategory((currentPageNumber - 1) * (int) itemsPerPage.getValue(), (int) itemsPerPage.getValue(), categories.getSelectedItem().toString());
                        int modsInCategory = WorkshopAPI.getAmountOfModsInCategory(categories.getSelectedItem().toString());

                        SwingUtilities.invokeLater(() -> {
                            currentPage.setText("Page: " + currentPageNumber + "/" + (Math.round(Float.parseFloat(String.valueOf((modsInCategory / (int) itemsPerPage.getValue()) + 1)))));

                            modDisplay.removeMods();

                            for (WorkshopAPI.Mod mod : mods) {
                                modDisplay.addMod(mod, false);
                            }

                            modDisplay.modRefreshRespectSize();
                        });
                        return;
                    }

                    ArrayList<WorkshopAPI.Mod> mods = WorkshopAPI.getMods((currentPageNumber - 1) * (int) itemsPerPage.getValue(), (int) itemsPerPage.getValue());
                    int modsInCategory = WorkshopAPI.getAllMods().size();

                    SwingUtilities.invokeLater(() -> {
                        currentPage.setText("Page: " + currentPageNumber + "/" + (Math.round(Float.parseFloat(String.valueOf((modsInCategory / (int) itemsPerPage.getValue()) + 1)))));

                        modDisplay.removeMods();

                        for (WorkshopAPI.Mod mod : mods) {
                            modDisplay.addMod(mod, false);
                        }

                        modDisplay.modRefreshRespectSize();
                    });
                }, "Next page").start();
            }
        });

        pagePreviousButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new Thread(() -> {
                    if (currentPageNumber - 1 < 1) {
                        return;
                    }

                    modDisplay.beginLoading();
                    currentPageNumber--;

                    if (!categories.getSelectedItem().toString().equals("All Categories")) {
                        ArrayList<WorkshopAPI.Mod> mods = WorkshopAPI.getModsInCategory((currentPageNumber - 1) * (int) itemsPerPage.getValue(), (int) itemsPerPage.getValue(), categories.getSelectedItem().toString());
                        int modsInCategory = WorkshopAPI.getAmountOfModsInCategory(categories.getSelectedItem().toString());

                        SwingUtilities.invokeLater(() -> {
                            currentPage.setText("Page: " + currentPageNumber + "/" + (Math.round(Float.parseFloat(String.valueOf((modsInCategory / (int) itemsPerPage.getValue()) + 1)))));

                            modDisplay.removeMods();

                            for (WorkshopAPI.Mod mod : mods) {
                                modDisplay.addMod(mod, false);
                            }

                            modDisplay.modRefreshRespectSize();
                        });

                        return;
                    }

                    ArrayList<WorkshopAPI.Mod> mods = WorkshopAPI.getMods((currentPageNumber - 1) * (int) itemsPerPage.getValue(), (int) itemsPerPage.getValue());
                    int modsInCategory = WorkshopAPI.getAllMods().size();

                    SwingUtilities.invokeLater(() -> {
                        currentPage.setText("Page: " + currentPageNumber + "/" + (Math.round(Float.parseFloat(String.valueOf((modsInCategory / (int) itemsPerPage.getValue()) + 1)))));

                        modDisplay.removeMods();

                        for (WorkshopAPI.Mod mod : mods) {
                            modDisplay.addMod(mod, false);
                        }

                        modDisplay.modRefreshRespectSize();
                    });
                }, "Previous page").start();
            }
        });

        Events.subscribe(TLDWREvents.DOWNLOAD_FINISHED.getName(), new Runnable() {
            @Override
            public void run() {
                if (!contentPanel.isVisible()) return;
                downloadButton.setEnabled(true);
                JOptionPane.showMessageDialog(frame, "Download finished");
            }
        });

        Events.subscribe(TLDWREvents.MOD_SELECTED.getName(), new Runnable() {
            @Override
            public void run() {
                if (!contentPanel.isVisible()) return;
                downloadButton.setEnabled(true);
            }
        });

        Events.subscribe(TLDWREvents.MOD_UNSELECTED.getName(), new Runnable() {
            @Override
            public void run() {
                if (!contentPanel.isVisible()) return;
                downloadButton.setEnabled(false);
            }
        });

        homeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                PublicValues.mainFrame.switchView(MainFrame.Views.Home);
            }
        });

        categories.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                if (bypassCategoryUpdate) return;

                currentPageNumber = 1;

                modDisplay.beginLoading();

                if (e.getItem().toString().equals("All Categories")) {
                    modDisplay.removeMods();
                    WorkshopAPI.reloadMods();

                    currentPage.setText("Page: " + currentPageNumber + "/" + (Math.round(Float.parseFloat(String.valueOf((WorkshopAPI.getAllMods().size() / (int) itemsPerPage.getValue()) + 1)))));

                    for (WorkshopAPI.Mod mod : WorkshopAPI.getMods((currentPageNumber - 1) * (int) itemsPerPage.getValue(), (int) itemsPerPage.getValue())) {
                        modDisplay.addMod(mod, false);
                    }
                    return;
                }

                currentPage.setText("Page: " + currentPageNumber + "/" + (Math.round(Float.parseFloat(String.valueOf((WorkshopAPI.getAmountOfModsInCategory(categories.getSelectedItem().toString()) / (int) itemsPerPage.getValue()) + 1)))));
                modDisplay.removeMods();
                for (WorkshopAPI.Mod mod : WorkshopAPI.getModsInCategory(currentPageNumber - 1, (int) itemsPerPage.getValue(), categories.getSelectedItem().toString())) {
                    modDisplay.addMod(mod, false);
                }
                modDisplay.endLoading();
            }
        });

        searchField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    modDisplay.beginLoading();
                    if (searchField.getText().isEmpty()) {
                        currentPage.setEnabled(true);
                        itemsPerPage.setEnabled(true);
                        pagePreviousButton.setEnabled(true);
                        pageNextButton.setEnabled(true);
                        refreshButton.setEnabled(true);
                        itemsPerPageLabel.setEnabled(true);
                        modDisplay.removeMods();
                        WorkshopAPI.reloadMods();

                        currentPageNumber = 1;

                        currentPage.setText("Page: " + currentPageNumber + "/" + (Math.round(Float.parseFloat(String.valueOf((WorkshopAPI.getAllMods().size() / (int) itemsPerPage.getValue()) + 1)))));

                        for (WorkshopAPI.Mod mod : WorkshopAPI.getMods((currentPageNumber - 1) * (int) itemsPerPage.getValue(), (int) itemsPerPage.getValue())) {
                            modDisplay.addMod(mod, false);
                        }
                        return;
                    }

                    currentPage.setEnabled(false);
                    itemsPerPage.setEnabled(false);
                    pagePreviousButton.setEnabled(false);
                    pageNextButton.setEnabled(false);
                    refreshButton.setEnabled(false);
                    itemsPerPageLabel.setEnabled(false);

                    ArrayList<WorkshopAPI.Mod> mods = searchEngine.search(WorkshopAPI.getAllMods(), searchField.getText());
                    currentPageNumber = 1;

                    currentPage.setText("Page: " + currentPageNumber + "/" + (Math.round(Float.parseFloat(String.valueOf((mods.size() / (int) itemsPerPage.getValue()) + 1)))));
                    modDisplay.removeMods();

                    for (WorkshopAPI.Mod mod : mods) {
                        modDisplay.addMod(mod, false);
                    }
                    modDisplay.endLoading();
                }
            }
        });
    }

    private void refreshCategories() {
        bypassCategoryUpdate = true;
        ((DefaultComboBoxModel) categories.getModel()).removeAllElements();
        ((DefaultComboBoxModel) categories.getModel()).addElement("All Categories");
        categories.getModel().setSelectedItem("All Categories");
        for (String category : categoriesList) {
            ((DefaultComboBoxModel) categories.getModel()).addElement(category);
        }
        bypassCategoryUpdate = false;
    }

    private void createUIComponents() {
        modDisplay = new ModDisplay(new JPanel(), true);
    }

    public void setFrame(JFrame frame) {
        this.frame = frame;
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
        contentPanel.setLayout(new GridLayoutManager(3, 1, new Insets(5, 5, 5, 5), -1, -1));
        contentPanel.setMinimumSize(new Dimension(658, 555));
        contentPanel.setPreferredSize(new Dimension(658, 555));
        bottomBar = new JPanel();
        bottomBar.setLayout(new GridLayoutManager(2, 5, new Insets(0, 0, 0, 0), -1, -1));
        contentPanel.add(bottomBar, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
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
        modPackManagerButton = new JButton();
        modPackManagerButton.setEnabled(true);
        modPackManagerButton.setText("ModPack Manager");
        modPackManagerButton.setToolTipText("Comming Soon");
        bottomBar.add(modPackManagerButton, new GridConstraints(0, 2, 2, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, new Dimension(-1, 35), new Dimension(-1, 35), new Dimension(-1, 35), 0, false));
        myModsButton = new JButton();
        myModsButton.setDoubleBuffered(true);
        myModsButton.setEnabled(true);
        myModsButton.setFocusCycleRoot(false);
        myModsButton.setText("My Mods");
        bottomBar.add(myModsButton, new GridConstraints(0, 3, 2, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, new Dimension(-1, 35), new Dimension(-1, 35), new Dimension(-1, 35), 0, false));
        downloadButton = new JButton();
        downloadButton.setEnabled(false);
        downloadButton.setText("Download");
        bottomBar.add(downloadButton, new GridConstraints(0, 4, 2, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, new Dimension(-1, 35), new Dimension(-1, 35), new Dimension(-1, 35), 0, false));
        topBar = new JPanel();
        topBar.setLayout(new GridLayoutManager(1, 7, new Insets(0, 0, 0, 0), -1, -1));
        contentPanel.add(topBar, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        categories = new JComboBox();
        topBar.add(categories, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, new Dimension(150, -1), new Dimension(150, -1), null, 0, false));
        searchField = new JTextField();
        searchField.setAlignmentX(0.5f);
        searchField.setColumns(0);
        topBar.add(searchField, new GridConstraints(0, 5, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        searchLabel = new JLabel();
        searchLabel.setText("Search:");
        topBar.add(searchLabel, new GridConstraints(0, 4, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer3 = new Spacer();
        topBar.add(spacer3, new GridConstraints(0, 3, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        final Spacer spacer4 = new Spacer();
        topBar.add(spacer4, new GridConstraints(0, 6, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        refreshButton = new JButton();
        refreshButton.setText("Refresh");
        topBar.add(refreshButton, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        homeButton = new JButton();
        homeButton.setText("Home");
        topBar.add(homeButton, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        modDisplayScrollPanelPanel = new JPanel();
        modDisplayScrollPanelPanel.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        contentPanel.add(modDisplayScrollPanelPanel, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        modDisplayScrollPanelPanel.add(modDisplay, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return contentPanel;
    }
}
