package de.werwolf2303.tldwr.frames;

import com.intellij.uiDesigner.core.GridConstraints;
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
    public JScrollPane modDisplayScrollPanel;
    public JPanel modDisplayScrollPanelPanel;
    public JPanel bottomBar;
    public JPanel topBar;
    public int currentPageNumber = 1;
    private ArrayList<String> categoriesList;
    private SearchEngine searchEngine;

    public WorkshopFrame() {
        searchEngine = new SearchEngine(categories);
        categoriesList = new ArrayList<>();

        modDisplayScrollPanel.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                modDisplay.setPreferredSize(new Dimension(modDisplayScrollPanel.getSize().width, modDisplay.getSize().height));
                modDisplay.modRefreshRespectSize();
            }
        });

        modDisplayScrollPanel.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        itemsPerPage.setValue(PublicValues.config.getInt(ConfigValues.numberOfModsPerPage.name));

        itemsPerPage.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                if(!categories.getSelectedItem().toString().equals("All Categories")) {
                    currentPageNumber = 1;
                    try {
                        currentPage.setText("Page: " + currentPageNumber + "/" + (Math.round(Float.parseFloat(String.valueOf((WorkshopAPI.getAmountOfModsInCategory(categories.getSelectedItem().toString())/ (int)itemsPerPage.getValue()) + 1)))));
                    } catch (IOException ex) {
                        ex.printStackTrace();
                        JOptionPane.showMessageDialog(null, "Failed to update page info");
                    }
                    modDisplay.removeMods();
                    try {
                        for(WorkshopAPI.Mod mod : WorkshopAPI.getModsInCategory(currentPageNumber - 1, (int) itemsPerPage.getValue(), categories.getSelectedItem().toString())) {
                            modDisplay.addMod(mod, false);
                        }
                    } catch (IOException ex) {
                        ex.printStackTrace();
                        JOptionPane.showMessageDialog(null, "Failed to display mods");
                    }
                    return;
                }
                currentPageNumber = 1;
                try {
                    currentPage.setText("Page: " + currentPageNumber + "/" + (Math.round(Float.parseFloat(String.valueOf((WorkshopAPI.getAllMods().size()/ (int)itemsPerPage.getValue()) + 1)))));
                } catch (IOException ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(null, "Failed to update page info");
                }
                modDisplay.removeMods();
                try {
                    for(WorkshopAPI.Mod mod : WorkshopAPI.getMods(currentPageNumber - 1, (int) itemsPerPage.getValue())) {
                        modDisplay.addMod(mod, false);
                    }
                } catch (IOException ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(null, "Failed to display mods");
                }
            }
        });

        try {
            for(WorkshopAPI.Mod mod : WorkshopAPI.getAllMods()) {
                if(!categoriesList.contains(mod.Category)) categoriesList.add(mod.Category);
            }
            refreshCategories();
            for(WorkshopAPI.Mod mod : WorkshopAPI.getMods(currentPageNumber-1, (int) itemsPerPage.getValue())) {
                modDisplay.initMod(mod, false);
            }
        } catch (IOException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(null, "Failed to display mods");
        }

        try {
            currentPage.setText("Page: " + currentPageNumber + "/" + (Math.round(Float.parseFloat(String.valueOf((WorkshopAPI.getAllMods().size()/ (int)itemsPerPage.getValue()) + 1)))));
        } catch (IOException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(null, "Failed to update page info");
        }
        
        PublicValues.modExpandedFrame = new ModExpandedFrame(this);
        PublicValues.modExpandedFrame.getContentPanel().setVisible(false);
        GridConstraints constraints = new GridConstraints();
        constraints.setFill(GridConstraints.FILL_BOTH);
        modDisplayScrollPanelPanel.add(PublicValues.modExpandedFrame.getContentPanel(), constraints);

        MyModsFrame myModsFrame = new MyModsFrame(this);
        myModsFrame.getContentPanel().setVisible(false);
        GridConstraints myModsFrameConstraints = new GridConstraints();
        myModsFrameConstraints.setFill(GridConstraints.FILL_BOTH);
        modDisplayScrollPanelPanel.add(myModsFrame.getContentPanel(), myModsFrameConstraints);

        Events.subscribe(TLDWREvents.MODLOAD_FINISHED.getName(), new Runnable() {
            @Override
            public void run() {
                SwingUtilities.invokeLater(() -> modDisplayScrollPanel.getVerticalScrollBar().setValue(0));
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
                WorkshopAPI.download(PublicValues.lastHighlightedMod.mod, new WorkshopAPI.DownloadProgressRunnable() {
                    @Override
                    public void run(double percentage) {
                        if(!frame.getTitle().contains("-")) {
                            frame.setTitle(frame.getTitle() + " - Downloading (" + Math.round(percentage) + "%)");
                            return;
                        }
                        frame.setTitle(frame.getTitle().split(" - ")[0] + " - Downloading (" + Math.round(percentage) + "%)");
                    }
                });
            }
        });

        refreshButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(!categories.getSelectedItem().toString().equals("All Categories")) {
                    modDisplay.removeMods();
                    try {
                        for(WorkshopAPI.Mod mod : WorkshopAPI.getModsInCategory(currentPageNumber - 1, (int) itemsPerPage.getValue(), categories.getSelectedItem().toString())) {
                            modDisplay.addMod(mod, false);
                        }
                    } catch (IOException ex) {
                        ex.printStackTrace();
                        JOptionPane.showMessageDialog(null, "Failed to display mods");
                    }
                    return;
                }

                modDisplay.removeMods();
                WorkshopAPI.reloadMods();
                categoriesList.clear();

                try {
                    for(WorkshopAPI.Mod mod : WorkshopAPI.getAllMods()) {
                        if(!categoriesList.contains(mod.Category)) categoriesList.add(mod.Category);
                    }
                    refreshCategories();
                    for(WorkshopAPI.Mod mod : WorkshopAPI.getMods((currentPageNumber - 1) * (int) itemsPerPage.getValue(), (int) itemsPerPage.getValue())) {
                        modDisplay.addMod(mod, false);
                    }
                } catch (IOException ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(null, "Failed to display mods");
                }
            }
        });

        pageNextButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    if (currentPageNumber + 1 > (Math.round(Float.parseFloat(String.valueOf((WorkshopAPI.getAllMods().size() / (int) itemsPerPage.getValue()) + 1))))) {
                        return;
                    }
                }catch (IOException ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(null, "Failed to calculate max forward number");
                }
                currentPageNumber++;

                if(!categories.getSelectedItem().toString().equals("All Categories")) {
                    try {
                        currentPage.setText("Page: " + currentPageNumber + "/" + (Math.round(Float.parseFloat(String.valueOf((WorkshopAPI.getAmountOfModsInCategory(categories.getSelectedItem().toString())/ (int)itemsPerPage.getValue()) + 1)))));
                    } catch (IOException ex) {
                        ex.printStackTrace();
                        JOptionPane.showMessageDialog(null, "Failed to update page info");
                    }
                    modDisplay.removeMods();
                    try {
                        for(WorkshopAPI.Mod mod : WorkshopAPI.getModsInCategory(currentPageNumber - 1, (int) itemsPerPage.getValue(), categories.getSelectedItem().toString())) {
                            modDisplay.addMod(mod, false);
                        }
                    } catch (IOException ex) {
                        ex.printStackTrace();
                        JOptionPane.showMessageDialog(null, "Failed to display mods");
                    }
                    return;
                }

                try {
                    currentPage.setText("Page: " + currentPageNumber + "/" + (Math.round(Float.parseFloat(String.valueOf((WorkshopAPI.getAllMods().size()/ (int)itemsPerPage.getValue()) + 1)))));
                } catch (IOException ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(null, "Failed to update page info");
                }
                modDisplay.removeMods();
                try {
                    for(WorkshopAPI.Mod mod : WorkshopAPI.getMods((currentPageNumber - 1) * (int) itemsPerPage.getValue(), (int) itemsPerPage.getValue())) {
                        modDisplay.addMod(mod, false);
                    }
                } catch (IOException ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(null, "Failed to display mods");
                }
            }
        });

        pagePreviousButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(currentPageNumber - 1 < 1) {
                    return;
                }
                currentPageNumber--;

                if(!categories.getSelectedItem().toString().equals("All Categories")) {
                    try {
                        currentPage.setText("Page: " + currentPageNumber + "/" + (Math.round(Float.parseFloat(String.valueOf((WorkshopAPI.getAmountOfModsInCategory(categories.getSelectedItem().toString())/ (int)itemsPerPage.getValue()) + 1)))));
                    } catch (IOException ex) {
                        ex.printStackTrace();
                        JOptionPane.showMessageDialog(null, "Failed to update page info");
                    }
                    modDisplay.removeMods();
                    try {
                        for(WorkshopAPI.Mod mod : WorkshopAPI.getModsInCategory(currentPageNumber - 1, (int) itemsPerPage.getValue(), categories.getSelectedItem().toString())) {
                            modDisplay.addMod(mod, false);
                        }
                    } catch (IOException ex) {
                        ex.printStackTrace();
                        JOptionPane.showMessageDialog(null, "Failed to display mods");
                    }
                    return;
                }

                try {
                    currentPage.setText("Page: " + currentPageNumber + "/" + (Math.round(Float.parseFloat(String.valueOf((WorkshopAPI.getAllMods().size()/ (int)itemsPerPage.getValue()) + 1)))));
                } catch (IOException ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(null, "Failed to update page info");
                }
                modDisplay.removeMods();
                try {
                    for(WorkshopAPI.Mod mod : WorkshopAPI.getMods(currentPageNumber - 1, (int) itemsPerPage.getValue())) {
                        modDisplay.addMod(mod, false);
                    }
                } catch (IOException ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(null, "Failed to display mods");
                }
            }
        });

        Events.subscribe(TLDWREvents.DOWNLOAD_FINISHED.getName(), new Runnable() {
            @Override
            public void run() {
                frame.setTitle(frame.getTitle().split(" - ")[0]);
            }
        });

        Events.subscribe(TLDWREvents.MOD_SELECTED.getName(), new Runnable() {
            @Override
            public void run() {
                downloadButton.setEnabled(true);
            }
        });

        Events.subscribe(TLDWREvents.MOD_UNSELECTED.getName(), new Runnable() {
            @Override
            public void run() {
                downloadButton.setEnabled(false);
            }
        });

        homeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(PublicValues.extendedFrameVisible) {
                    PublicValues.modExpandedFrame.hide();
                }
                PublicValues.mainFrame.switchView(MainFrame.Views.Home);
            }
        });

        categories.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                if(e.getItem().toString().equals("All Categories")) {
                    modDisplay.removeMods();
                    WorkshopAPI.reloadMods();

                    currentPageNumber = 1;

                    try {
                        currentPage.setText("Page: " + currentPageNumber + "/" + (Math.round(Float.parseFloat(String.valueOf((WorkshopAPI.getAllMods().size()/ (int)itemsPerPage.getValue()) + 1)))));
                    } catch (IOException ex) {
                        ex.printStackTrace();
                        JOptionPane.showMessageDialog(null, "Failed to update page info");
                    }

                    try {
                        for(WorkshopAPI.Mod mod : WorkshopAPI.getMods((currentPageNumber - 1) * (int) itemsPerPage.getValue(), (int) itemsPerPage.getValue())) {
                            modDisplay.addMod(mod, false);
                        }
                    } catch (IOException ex) {
                        ex.printStackTrace();
                        JOptionPane.showMessageDialog(null, "Failed to display mods");
                    }
                    return;
                }

                currentPageNumber = 1;

                try {
                    currentPage.setText("Page: " + currentPageNumber + "/" + (Math.round(Float.parseFloat(String.valueOf((WorkshopAPI.getAmountOfModsInCategory(categories.getSelectedItem().toString())/ (int)itemsPerPage.getValue()) + 1)))));
                } catch (IOException ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(null, "Failed to update page info");
                }
                modDisplay.removeMods();
                try {
                    for(WorkshopAPI.Mod mod : WorkshopAPI.getModsInCategory(currentPageNumber - 1, (int) itemsPerPage.getValue(), categories.getSelectedItem().toString())) {
                        modDisplay.addMod(mod, false);
                    }
                } catch (IOException ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(null, "Failed to display mods");
                }
            }
        });

        searchField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if(e.getKeyCode() == KeyEvent.VK_ENTER) {
                    if(searchField.getText().isEmpty()) {
                        currentPage.setEnabled(true);
                        itemsPerPage.setEnabled(true);
                        pagePreviousButton.setEnabled(true);
                        pageNextButton.setEnabled(true);
                        refreshButton.setEnabled(true);
                        itemsPerPageLabel.setEnabled(true);
                        modDisplay.removeMods();
                        WorkshopAPI.reloadMods();

                        currentPageNumber = 1;

                        try {
                            currentPage.setText("Page: " + currentPageNumber + "/" + (Math.round(Float.parseFloat(String.valueOf((WorkshopAPI.getAllMods().size()/ (int)itemsPerPage.getValue()) + 1)))));
                        } catch (IOException ex) {
                            ex.printStackTrace();
                            JOptionPane.showMessageDialog(null, "Failed to update page info");
                        }

                        try {
                            for(WorkshopAPI.Mod mod : WorkshopAPI.getMods((currentPageNumber - 1) * (int) itemsPerPage.getValue(), (int) itemsPerPage.getValue())) {
                                modDisplay.addMod(mod, false);
                            }
                        } catch (IOException ex) {
                            ex.printStackTrace();
                            JOptionPane.showMessageDialog(null, "Failed to display mods");
                        }
                        return;
                    }

                    currentPage.setEnabled(false);
                    itemsPerPage.setEnabled(false);
                    pagePreviousButton.setEnabled(false);
                    pageNextButton.setEnabled(false);
                    refreshButton.setEnabled(false);
                    itemsPerPageLabel.setEnabled(false);

                    ArrayList<WorkshopAPI.Mod> mods;
                    try {
                        mods = searchEngine.search(WorkshopAPI.getAllMods(), searchField.getText());
                    }catch (IOException ex) {
                        ex.printStackTrace();
                        JOptionPane.showMessageDialog(null, "Failed to search for mods");
                        return;
                    }
                    currentPageNumber = 1;

                    currentPage.setText("Page: " + currentPageNumber + "/" + (Math.round(Float.parseFloat(String.valueOf((mods.size()/ (int)itemsPerPage.getValue()) + 1)))));
                    modDisplay.removeMods();

                    for(WorkshopAPI.Mod mod : mods) {
                        modDisplay.addMod(mod, false);
                    }
                }
            }
        });
    }

    private void refreshCategories() {
        ((DefaultComboBoxModel) categories.getModel()).removeAllElements();
        ((DefaultComboBoxModel) categories.getModel()).addElement("All Categories");
        categories.getModel().setSelectedItem("All Categories");
        for(String category : categoriesList) {
            ((DefaultComboBoxModel) categories.getModel()).addElement(category);
        }
    }

    private void createUIComponents() {
        modDisplay = new ModDisplay(this, false);
    }

    public void setFrame(JFrame frame) {
        this.frame = frame;
    }
}
