package de.werwolf2303.tldwr.swingextensions;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

public class JCheckBoxList extends JList<JCheckBox> {
    private ArrayList<CheckBoxListener> listeners = new ArrayList<>();
    private DefaultListModel<JCheckBox> modListModel;

    public JCheckBoxList() {
        super();

        setCellRenderer(new CellRenderer());
        setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                int index = locationToIndex(e.getPoint());
                if (index != -1) {
                    Rectangle cellBounds = getCellBounds(index, index);
                    JCheckBox checkbox = getModel().getElementAt(index);

                    JCheckBox renderer = new JCheckBox(checkbox.getText(), checkbox.isSelected());
                    renderer.setFont(getFont());
                    Dimension checkBoxSize = renderer.getPreferredSize();

                    int checkBoxX = cellBounds.x + 2;
                    int checkBoxY = cellBounds.y + (cellBounds.height - checkBoxSize.height) / 2;
                    Rectangle checkBoxBounds = new Rectangle(checkBoxX, checkBoxY, checkBoxSize.width, checkBoxSize.height);

                    if (checkBoxBounds.contains(e.getPoint())) {
                        checkbox.setSelected(!checkbox.isSelected());
                        listeners.forEach(listener -> listener.selectionChange(checkbox));
                        repaint();
                    }
                }
            }
        });

        modListModel = new DefaultListModel<>();
        setModel(modListModel);
    }

    @Override
    public DefaultListModel<JCheckBox> getModel() {
        return modListModel;
    }

    public void addCheckBoxListener(CheckBoxListener listener) {
        listeners.add(listener);
    }

    public void removeCheckBoxListener(CheckBoxListener listener) {
        listeners.remove(listener);
    }

    public interface CheckBoxListener {
        void selectionChange(JCheckBox checkBox);
    }

    protected class CellRenderer implements ListCellRenderer<JCheckBox> {
        public Component getListCellRendererComponent(
                JList<? extends JCheckBox> list, JCheckBox value, int index,
                boolean isSelected, boolean cellHasFocus) {
            JCheckBox checkbox = value;

            checkbox.setBackground(isSelected ? getSelectionBackground()
                    : getBackground());
            checkbox.setForeground(isSelected ? getSelectionForeground()
                    : getForeground());
            checkbox.setFont(getFont());
            return checkbox;
        }
    }
}
