package de.werwolf2303.tldwr.frames;

import de.werwolf2303.tldwr.PublicValues;
import de.werwolf2303.tldwr.TLDPatcher.TLDPatcher;
import de.werwolf2303.tldwr.config.ConfigValues;
import de.werwolf2303.tldwr.workshop.WorkshopAPI;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.event.*;
import java.io.IOException;
import java.util.ArrayList;

public class SettingsFrame {
    public JPanel contentPanel;
    private JTable information;
    private JSpinner numberOfPPPL;
    private JRadioButton checkModUpdates;
    private JButton checkModUpdatesButton;
    private JButton backButton;
    private JLabel informationLabel;
    private JLabel miscLabel;
    private JPanel miscPanel;
    private JLabel numberOfPPPLabel;
    private JPanel informationPanel;
    private JLabel advancedLabel;
    private JPanel advancedPanel;
    private JPanel controlPanel;
    private JButton uninstallButton;

    public SettingsFrame() {
        contentPanel.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentShown(ComponentEvent e) {
                super.componentShown(e);
                load();
            }

            @Override
            public void componentHidden(ComponentEvent e) {
                super.componentHidden(e);
                save();
            }
        });

        backButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                PublicValues.mainFrame.switchView(MainFrame.Views.Home);
            }
        });

        checkModUpdatesButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    ArrayList<WorkshopAPI.Mod> modsWithUpdates = WorkshopAPI.checkForModUpdates();
                    if(modsWithUpdates.isEmpty()) {
                        JOptionPane.showMessageDialog(null, "No updates available");
                        return;
                    }
                    new UpdateModsFrame().showIt(modsWithUpdates);
                } catch (IOException ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(null, "Failed to check for updates");
                }
            }
        });

        uninstallButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new TLDPatcher().uninstall();
                JOptionPane.showMessageDialog(null, "Uninstall complete");
                System.exit(0);
            }
        });
    }

    private void load() {
        information.setModel(new DefaultTableModel(new Object[][]{}, new String[]{""}));
        ((DefaultTableModel) information.getModel()).addRow(new Object[]{"Mods Path:"});
        ((DefaultTableModel) information.getModel()).addRow(new Object[]{PublicValues.tldUserPath});
        ((DefaultTableModel) information.getModel()).addRow(new Object[]{""});
        ((DefaultTableModel) information.getModel()).addRow(new Object[]{""});
        ((DefaultTableModel) information.getModel()).addRow(new Object[]{"Game Path"});
        ((DefaultTableModel) information.getModel()).addRow(new Object[]{PublicValues.tldPath});
        ((DefaultTableModel) information.getModel()).addRow(new Object[]{""});
        ((DefaultTableModel) information.getModel()).addRow(new Object[]{""});
        try {
            ((DefaultTableModel) information.getModel()).addRow(new Object[]{"Total mods on the Workshop: " + (WorkshopAPI.getAllMods().size())});
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Failed to get total mods");
        }
        ((DefaultTableModel) information.getModel()).addRow(new Object[]{""});
        try {
            ((DefaultTableModel) information.getModel()).addRow(new Object[]{"Installed mods: " + (WorkshopAPI.getInstalledMods().size())});
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Failed to load installed mods");
        }
        information.setShowGrid(false);
        information.setShowHorizontalLines(false);
        information.setShowVerticalLines(false);

        numberOfPPPL.setValue(PublicValues.config.getInt(ConfigValues.numberOfModsPerPage.name));
        checkModUpdates.setSelected(PublicValues.config.getBoolean(ConfigValues.checkModUpdates.name));
    }

    private void createUIComponents() {
        information = new JTable() {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }

            @Override
            public String getToolTipText(MouseEvent e) {
                String tip = null;
                java.awt.Point p = e.getPoint();
                int rowIndex = rowAtPoint(p);
                int colIndex = columnAtPoint(p);
                try {
                    if(rowIndex == 1 || rowIndex == 5){
                        tip = getValueAt(rowIndex, colIndex).toString();
                    }
                } catch (RuntimeException ignored) {
                }
                return tip;
            }
        };
    }

    private void save() {
        if(!contentPanel.isVisible()) return;
        PublicValues.config.write(ConfigValues.checkModUpdates.name, checkModUpdates.isSelected());
        PublicValues.config.write(ConfigValues.numberOfModsPerPage.name, numberOfPPPL.getValue());
    }
}

