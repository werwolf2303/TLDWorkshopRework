package de.werwolf2303.tldwr.frames;

import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import de.werwolf2303.tldwr.PublicValues;
import de.werwolf2303.tldwr.TLDPatcher.TLDPatcher;
import de.werwolf2303.tldwr.config.ConfigValues;
import de.werwolf2303.tldwr.workshop.WorkshopAPI;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
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
    private JLabel numberOfPPPLabel;
    private JPanel advancedPanel;
    private JButton uninstallButton;

    public SettingsFrame() {
        $$$setupUI$$$();
        backButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                save();
                PublicValues.mainFrame.switchView(MainFrame.Views.Home);
            }
        });

        checkModUpdatesButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    ArrayList<WorkshopAPI.Mod> modsWithUpdates = WorkshopAPI.checkForModUpdates();
                    if (modsWithUpdates.isEmpty()) {
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

        information.setBackground(new Color(0, 0, 0, 0));
    }

    public void load() {
        information.setModel(new DefaultTableModel(new Object[][]{}, new String[]{""}));
        ((DefaultTableModel) information.getModel()).addRow(new Object[]{"Mods Path:"});
        ((DefaultTableModel) information.getModel()).addRow(new Object[]{PublicValues.tldUserPath});
        ((DefaultTableModel) information.getModel()).addRow(new Object[]{""});
        ((DefaultTableModel) information.getModel()).addRow(new Object[]{"Game Path"});
        ((DefaultTableModel) information.getModel()).addRow(new Object[]{PublicValues.tldPath});
        ((DefaultTableModel) information.getModel()).addRow(new Object[]{""});
        ((DefaultTableModel) information.getModel()).addRow(new Object[]{"Total mods on the Workshop: " + (WorkshopAPI.getAllMods().size())});
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
                Point p = e.getPoint();
                int rowIndex = rowAtPoint(p);
                int colIndex = columnAtPoint(p);
                try {
                    if (rowIndex == 1 || rowIndex == 5) {
                        tip = getValueAt(rowIndex, colIndex).toString();
                    }
                } catch (RuntimeException ignored) {
                }
                return tip;
            }
        };
    }

    private void save() {
        if (!contentPanel.isVisible()) return;

        if (checkModUpdates.isSelected() == PublicValues.config.getBoolean(ConfigValues.checkModUpdates.name)
                && ((int) numberOfPPPL.getValue()) == PublicValues.config.getInt(ConfigValues.numberOfModsPerPage.name)) {
            return;
        }

        PublicValues.config.write(ConfigValues.checkModUpdates.name, checkModUpdates.isSelected());
        PublicValues.config.write(ConfigValues.numberOfModsPerPage.name, numberOfPPPL.getValue());

        JOptionPane.showMessageDialog(contentPanel, "Changes will take effect on the next restart");
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
        contentPanel.setLayout(new GridLayoutManager(5, 1, new Insets(5, 5, 5, 5), -1, -1));
        contentPanel.setMinimumSize(new Dimension(658, 555));
        contentPanel.setPreferredSize(new Dimension(658, 555));
        informationLabel = new JLabel();
        informationLabel.setText("Information:");
        contentPanel.add(informationLabel, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        advancedPanel = new JPanel();
        advancedPanel.setLayout(new GridLayoutManager(2, 1, new Insets(0, 0, 0, 0), -1, -1));
        contentPanel.add(advancedPanel, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        advancedPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.black), "Advanced", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        checkModUpdates = new JRadioButton();
        checkModUpdates.setText("Check for mod updates on startup");
        advancedPanel.add(checkModUpdates, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        checkModUpdatesButton = new JButton();
        checkModUpdatesButton.setText("Check for mod updates now");
        advancedPanel.add(checkModUpdatesButton, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, new Dimension(-1, 35), new Dimension(-1, 35), new Dimension(-1, 35), 0, false));
        backButton = new JButton();
        backButton.setText("Back");
        contentPanel.add(backButton, new GridConstraints(4, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, new Dimension(-1, 50), new Dimension(-1, 50), new Dimension(-1, 50), 0, false));
        contentPanel.add(information, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_WANT_GROW, null, new Dimension(150, 50), null, 0, false));
        final JPanel panel1 = new JPanel();
        panel1.setLayout(new GridLayoutManager(2, 2, new Insets(0, 0, 0, 0), -1, -1));
        contentPanel.add(panel1, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        panel1.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.black), "Misc", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        numberOfPPPLabel = new JLabel();
        numberOfPPPLabel.setText("Default number of mods per page");
        panel1.add(numberOfPPPLabel, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        uninstallButton = new JButton();
        uninstallButton.setText("Uninstall");
        panel1.add(uninstallButton, new GridConstraints(1, 0, 1, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, new Dimension(-1, 35), new Dimension(-1, 35), new Dimension(-1, 35), 0, false));
        numberOfPPPL = new JSpinner();
        panel1.add(numberOfPPPL, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return contentPanel;
    }
}

