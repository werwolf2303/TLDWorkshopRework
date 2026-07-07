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
import java.util.List;

public class WorkshopFrame {
    private static final String ALL_CATEGORIES = "All Categories";
    private static final int MIN_ITEMS_PER_PAGE = 1;
    private static final int MAX_ITEMS_PER_PAGE = 998;

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
                runInBackground("Mod per page", () -> {
                    int perPage = clampItemsPerPage();
                    modDisplay.beginLoading();
                    currentPageNumber = 1;
                    ArrayList<WorkshopAPI.Mod> mods = getCurrentPageMods(perPage);
                    int totalMods = getCurrentTotalMods();
                    SwingUtilities.invokeLater(() -> {
                        setPageText(totalMods, perPage);
                        renderMods(mods);
                    });
                });
            }
        });

        runInBackground("Mods loader", () -> {
            int perPage = clampItemsPerPage();
            modDisplay.beginLoading();
            ArrayList<WorkshopAPI.Mod> mods = WorkshopAPI.getMods(0, perPage);
            int modsInCategory = WorkshopAPI.getAllMods().size();

            SwingUtilities.invokeLater(() -> {
                refreshCategoriesFromAllMods();
                setPageText(modsInCategory, perPage);
                renderMods(mods);
            });
        });

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
                runInBackground("Mods refresh", () -> {
                    int perPage = clampItemsPerPage();
                    modDisplay.beginLoading();
                    WorkshopAPI.reloadMods();
                    ArrayList<WorkshopAPI.Mod> mods = getCurrentPageMods(perPage);
                    int totalMods = getCurrentTotalMods();
                    SwingUtilities.invokeLater(() -> {
                        refreshCategoriesFromAllMods();
                        setPageText(totalMods, perPage);
                        renderMods(mods);
                    });
                });
            }
        });

        pageNextButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                runInBackground("Next page", () -> {
                    int perPage = clampItemsPerPage();
                    int totalMods = getCurrentTotalMods();
                    if (currentPageNumber + 1 > calculateTotalPages(totalMods, perPage)) {
                        return;
                    }

                    modDisplay.beginLoading();
                    currentPageNumber++;
                    ArrayList<WorkshopAPI.Mod> mods = getCurrentPageMods(perPage);
                    SwingUtilities.invokeLater(() -> {
                        setPageText(totalMods, perPage);
                        renderMods(mods);
                    });
                });
            }
        });

        pagePreviousButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                runInBackground("Previous page", () -> {
                    int perPage = clampItemsPerPage();
                    if (currentPageNumber - 1 < 1) {
                        return;
                    }

                    modDisplay.beginLoading();
                    currentPageNumber--;
                    int totalMods = getCurrentTotalMods();
                    ArrayList<WorkshopAPI.Mod> mods = getCurrentPageMods(perPage);
                    SwingUtilities.invokeLater(() -> {
                        setPageText(totalMods, perPage);
                        renderMods(mods);
                    });
                });
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
                if (bypassCategoryUpdate || e.getStateChange() != ItemEvent.SELECTED) return;

                currentPageNumber = 1;

                modDisplay.beginLoading();
                int perPage = clampItemsPerPage();
                if (allCategoriesSelected()) {
                    WorkshopAPI.reloadMods();
                    refreshCategoriesFromAllMods();
                }
                int totalMods = getCurrentTotalMods();
                ArrayList<WorkshopAPI.Mod> mods = getCurrentPageMods(perPage);
                setPageText(totalMods, perPage);
                renderMods(mods);
            }
        });

        searchField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    int perPage = clampItemsPerPage();
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

                        ArrayList<WorkshopAPI.Mod> mods = WorkshopAPI.getMods(0, perPage);
                        setPageText(WorkshopAPI.getAllMods().size(), perPage);
                        renderMods(mods);
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

                    setPageText(mods.size(), perPage);
                    renderMods(mods);
                }
            }
        });
    }

    private int getItemsPerPageValue() {
        return (int) itemsPerPage.getValue();
    }

    private int clampItemsPerPage() {
        int value = getItemsPerPageValue();
        if (value < MIN_ITEMS_PER_PAGE) {
            itemsPerPage.setValue(MIN_ITEMS_PER_PAGE);
            return MIN_ITEMS_PER_PAGE;
        }
        if (value > MAX_ITEMS_PER_PAGE) {
            itemsPerPage.setValue(MAX_ITEMS_PER_PAGE);
            return MAX_ITEMS_PER_PAGE;
        }
        return value;
    }

    private int calculateTotalPages(int totalItems, int perPage) {
        if (perPage < 1) {
            return 1;
        }
        return Math.max(1, (totalItems + perPage - 1) / perPage);
    }

    private String selectedCategory() {
        Object selected = categories.getSelectedItem();
        return selected == null ? ALL_CATEGORIES : selected.toString();
    }

    private boolean allCategoriesSelected() {
        return ALL_CATEGORIES.equals(selectedCategory());
    }

    private int currentOffset(int perPage) {
        return (currentPageNumber - 1) * perPage;
    }

    private void setPageText(int totalItems, int perPage) {
        currentPage.setText("Page: " + currentPageNumber + "/" + calculateTotalPages(totalItems, perPage));
    }

    private ArrayList<WorkshopAPI.Mod> getCurrentPageMods(int perPage) {
        int offset = currentOffset(perPage);
        if (allCategoriesSelected()) {
            return WorkshopAPI.getMods(offset, perPage);
        }
        return WorkshopAPI.getModsInCategory(offset, perPage, selectedCategory());
    }

    private int getCurrentTotalMods() {
        if (allCategoriesSelected()) {
            return WorkshopAPI.getAllMods().size();
        }
        return WorkshopAPI.getAmountOfModsInCategory(selectedCategory());
    }

    private void renderMods(List<WorkshopAPI.Mod> mods) {
        modDisplay.removeMods();
        for (WorkshopAPI.Mod mod : mods) {
            modDisplay.addMod(mod, false);
        }
        modDisplay.modRefreshRespectSize();
    }

    private void refreshCategoriesFromAllMods() {
        categoriesList.clear();
        for (WorkshopAPI.Mod mod : WorkshopAPI.getAllMods()) {
            if (!categoriesList.contains(mod.Category)) {
                categoriesList.add(mod.Category);
            }
        }
        refreshCategories();
    }

    private void runInBackground(String name, Runnable runnable) {
        new Thread(runnable, name).start();
    }

    private void refreshCategories() {
        bypassCategoryUpdate = true;
        ((DefaultComboBoxModel) categories.getModel()).removeAllElements();
        ((DefaultComboBoxModel) categories.getModel()).addElement(ALL_CATEGORIES);
        categories.getModel().setSelectedItem(ALL_CATEGORIES);
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
