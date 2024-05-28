package de.werwolf2303.tldwr.frames;

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

        modDisplay.setWorkshopFrame(workshopFrame);

        backButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                hide();
            }
        });

        modsFolderButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(Desktop.isDesktopSupported()) {
                    try {
                        Desktop.getDesktop().open(new File(PublicValues.tldUserPath, "Mods"));
                    } catch (IOException ex) {
                        ex.printStackTrace();
                        JOptionPane.showMessageDialog(null, "Failed to open directory");
                    }
                }else{
                    JOptionPane.showMessageDialog(null, "Not supported on your Operating System");
                }
            }
        });

        modDisplayScrollPanel.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        itemsPerPage.setValue(PublicValues.config.getInt(ConfigValues.numberOfModsPerPage.name));

        itemsPerPage.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                currentPageNumber = 1;
                try {
                    currentPage.setText("Page: " + currentPageNumber + "/" + (Math.round(Float.parseFloat(String.valueOf((WorkshopAPI.getInstalledMods().size()/ (int)itemsPerPage.getValue()) + 1)))));
                } catch (IOException ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(null, "Failed to update page info");
                }
                modDisplay.removeMods();
                try {
                    for(WorkshopAPI.Mod mod : WorkshopAPI.getInstalledMods(currentPageNumber - 1, (int) itemsPerPage.getValue())) {
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
                    for(WorkshopAPI.Mod mod : WorkshopAPI.getInstalledMods((currentPageNumber-1) * (int) itemsPerPage.getValue(), (int) itemsPerPage.getValue())) {
                        modDisplay.addMod(mod, true);
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
                    if (currentPageNumber + 1 > (Math.round(Float.parseFloat(String.valueOf((WorkshopAPI.getInstalledMods().size() / (int) itemsPerPage.getValue()) + 1))))) {
                        return;
                    }
                }catch (IOException ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(null, "Failed to calculate max forward number");
                }
                currentPageNumber++;
                try {
                    currentPage.setText("Page: " + currentPageNumber + "/" + (Math.round(Float.parseFloat(String.valueOf((WorkshopAPI.getInstalledMods().size()/ (int)itemsPerPage.getValue()) + 1)))));
                } catch (IOException ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(null, "Failed to update page info");
                }
                modDisplay.removeMods();
                try {
                    for(WorkshopAPI.Mod mod : WorkshopAPI.getInstalledMods((currentPageNumber-1) * (int) itemsPerPage.getValue(), (int) itemsPerPage.getValue())) {
                        modDisplay.addMod(mod, true);
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
                try {
                    currentPage.setText("Page: " + currentPageNumber + "/" + (Math.round(Float.parseFloat(String.valueOf((WorkshopAPI.getInstalledMods().size()/ (int)itemsPerPage.getValue()) + 1)))));
                } catch (IOException ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(null, "Failed to update page info");
                }
                modDisplay.removeMods();
                try {
                    for(WorkshopAPI.Mod mod : WorkshopAPI.getInstalledMods(currentPageNumber-1, (int) itemsPerPage.getValue())) {
                        modDisplay.addMod(mod, true);
                    }
                } catch (IOException ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(null, "Failed to display mods");
                }
            }
        });

        deleteButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    WorkshopAPI.removeModFromMyMods(PublicValues.lastHighlightedMod.mod);
                    refreshButton.getActionListeners()[refreshButton.getActionListeners().length-1].actionPerformed(null); //Refresh mods
                    workshopFrame.refreshButton.getActionListeners()[workshopFrame.refreshButton.getActionListeners().length-1].actionPerformed(null); //Refresh mods in workshop
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
        modDisplay = new ModDisplay(workshopFrame, true);
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
        if(PublicValues.lastHighlightedMod != null) lastHighlightedModBackup = PublicValues.lastHighlightedMod;

        //List mods
        modDisplay.removeMods();
        currentPageNumber = 1;
        try {
            currentPage.setText("Page: " + currentPageNumber + "/" + (Math.round(Float.parseFloat(String.valueOf((WorkshopAPI.getInstalledMods().size()/ (int)itemsPerPage.getValue()) + 1)))));
        } catch (IOException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(null, "Failed to update page info");
        }
        modDisplay.removeMods();
        try {
            for(WorkshopAPI.Mod mod : WorkshopAPI.getInstalledMods(currentPageNumber-1, (int) itemsPerPage.getValue())) {
                modDisplay.addMod(mod, true);
            }
        } catch (IOException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(null, "Failed to display mods");
        }

        Events.subscribe(TLDWREvents.MOD_SELECTED.getName(), onModSelected);
        Events.subscribe(TLDWREvents.MOD_UNSELECTED.getName(), onModUnselected);
        Events.subscribe(TLDWREvents.MODLOAD_FINISHED.getName(), onModLoadFinished);


        workshopFrame.topBar.setVisible(false);
        workshopFrame.bottomBar.setVisible(false);
        workshopFrame.modDisplay.setVisible(false);
        contentPanel.setVisible(true);
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                workshopFrame.modDisplayScrollPanel.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
            }
        });
    }

    public void hide() {
        if(lastHighlightedModBackup != null) PublicValues.lastHighlightedMod = lastHighlightedModBackup;

        Events.unsubscribe(TLDWREvents.MOD_SELECTED.getName(), onModSelected);
        Events.unsubscribe(TLDWREvents.MOD_UNSELECTED.getName(), onModUnselected);
        Events.unsubscribe(TLDWREvents.MODLOAD_FINISHED.getName(), onModLoadFinished);

        contentPanel.setVisible(false);
        workshopFrame.topBar.setVisible(true);
        workshopFrame.bottomBar.setVisible(true);
        workshopFrame.modDisplay.setVisible(true);
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                workshopFrame.modDisplayScrollPanel.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
            }
        });
    }
}
