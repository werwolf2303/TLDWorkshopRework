package de.werwolf2303.tldwr.frames;

import de.werwolf2303.tldwr.workshop.WorkshopAPI;

import javax.swing.*;
import java.awt.event.*;
import java.io.IOException;
import java.util.ArrayList;

public class UpdateModsFrame {
    private JButton updateAllButton;
    private JList<String> modList;
    private JProgressBar updateProgress;
    private JButton updateSelectedButton;
    private JLabel updateInfo;
    private JPanel contentPanel;
    private JFrame frame;
    private boolean allowClose = true;
    private ArrayList<WorkshopAPI.Mod> modsWithUpdates = new ArrayList<>();

    public UpdateModsFrame() {
        updateAllButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                allowClose = false;
                updateAllButton.setEnabled(false);
                updateSelectedButton.setEnabled(false);
                Thread updateThread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        for(WorkshopAPI.Mod mod : modsWithUpdates) {
                            updateInfo.setText(mod.Name);
                            WorkshopAPI.executeUpdate(mod, updateProgress);
                        }
                        allowClose = true;
                    }
                });
                updateThread.start();
            }
        });

        updateSelectedButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                allowClose = false;
                updateAllButton.setEnabled(false);
                updateSelectedButton.setEnabled(false);
                Thread updateThread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        String selectedName = modList.getSelectedValue();
                        for(WorkshopAPI.Mod mod : modsWithUpdates) {
                            if(mod.Name.equals(selectedName)) {
                                updateInfo.setText(mod.Name);
                                WorkshopAPI.executeUpdate(mod, updateProgress);
                                break;
                            }
                        }
                        allowClose = true;
                    }
                });
                updateThread.start();
            }
        });
    }

    public void showIt(ArrayList<WorkshopAPI.Mod> modsWithUpdates) {
        this.modsWithUpdates = modsWithUpdates;
        frame = new JFrame("The Long Drive Workshop");
        frame.setContentPane(contentPanel);
        frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if(allowClose) hide();
            }
        });
        frame.setVisible(true);
        frame.pack();

        fetchUpdates(modsWithUpdates);
    }

    private void fetchUpdates(ArrayList<WorkshopAPI.Mod> modsWithUpdates) {
        for(WorkshopAPI.Mod mod : modsWithUpdates) {
            ((DefaultListModel<String>) modList.getModel()).addElement(mod.Name);
        }
    }

    public void hide() {
        frame.dispose();
    }
}
