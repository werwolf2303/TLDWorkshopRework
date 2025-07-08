package de.werwolf2303.tldwr.swingextensions;

import de.werwolf2303.tldwr.Events;
import de.werwolf2303.tldwr.TLDWREvents;
import de.werwolf2303.tldwr.frames.WorkshopFrame;
import de.werwolf2303.tldwr.workshop.WorkshopAPI;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.io.IOException;
import java.util.ArrayList;

public class ModDisplay extends JLayeredPane {
    ArrayList<ModDisplayModule> modules;
    ArrayList<WorkshopAPI.Mod> mods;
    private boolean ignoreInstalledMods;
    private JPanel modsPanel;
    private JPanel loadingPanel;
    private JProgressBar loadingBar;
    private JScrollPane modsScrollPanel;
    private Color backgroundColor;
    private boolean singleClick = false;

    public boolean isModPacks = false;

    public ModDisplay(JPanel panel, boolean ignoreInstalledMods) {
        this.ignoreInstalledMods = ignoreInstalledMods;
        modules = new ArrayList<>();
        mods = new ArrayList<>();

        backgroundColor = panel.getBackground();

        Timer resizeTimer = new Timer(250, event -> {
            modsScrollPanel.setSize(getSize());
            modsPanel.setSize(getWidth(), modsPanel.getHeight());
            loadingPanel.setSize(getSize());

            recalculatePositions();
        });
        resizeTimer.setRepeats(false);

        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                resizeTimer.restart();
            }
        });

        modsPanel = new JPanel();
        modsPanel.setLayout(null);

        modsScrollPanel = new JScrollPane();
        modsScrollPanel.setViewportView(modsPanel);
        modsScrollPanel.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        modsScrollPanel.getVerticalScrollBar().setUnitIncrement(30);
        modsScrollPanel.getVerticalScrollBar().setValue(0);

        loadingBar = new JProgressBar();
        loadingBar.setIndeterminate(true);
        loadingBar.setStringPainted(false);
        loadingBar.setOrientation(SwingConstants.VERTICAL);

        loadingPanel = new JPanel();
        loadingPanel.setLayout(new BorderLayout());
        loadingPanel.add(loadingBar, BorderLayout.CENTER);

        add(modsScrollPanel, JLayeredPane.DEFAULT_LAYER);
        add(loadingPanel, JLayeredPane.PALETTE_LAYER);

        Events.subscribe(TLDWREvents.MODLOAD_FINISHED.getName(), new Runnable() {
            @Override
            public void run() {
                if(!isVisible()) return;

                endLoading();

                SwingUtilities.invokeLater(() -> {
                    modsScrollPanel.getVerticalScrollBar().setValue(0);
                });
            }
        });
    }

    public void setSingleClick() {
        singleClick = true;
    }

    public void addMod(WorkshopAPI.Mod mod, boolean disableExtendedFrame) {
        if (ignoreInstalledMods) {
            try {
                for (WorkshopAPI.Mod entry : WorkshopAPI.getInstalledMods()) {
                    if (entry.Name.equals(mod.Name)) {
                        return;
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(null, "Failed to get installed mods");
            }
        }
        mods.add(mod);
        modules.add(ModDisplayModule.init(mod, backgroundColor, disableExtendedFrame, singleClick, isModPacks));
    }

    public void beginLoading() {
        loadingPanel.setVisible(true);
        loadingPanel.revalidate();
        loadingPanel.repaint();
    }

    public void endLoading() {
        loadingPanel.setVisible(false);
        loadingPanel.revalidate();
        loadingPanel.repaint();
    }

    public void removeMods() {
        for (JPanel module : modules) {
            modsPanel.remove(module);
        }
        mods.clear();
        modules.clear();
    }

    private void recalculatePositions() {
        int yCache = 3;
        int xCache = 5;

        int columns = (int) Math.floor((double) (modsPanel.getWidth() - 5) / 299);

        int column = 0;
        for (Component component : modsPanel.getComponents()) {
            component.setBounds(xCache, yCache, 299, 121);

            if (column == columns - 1) {
                column = 0;
                xCache = 5;
                yCache += 124;
            } else {
                xCache += 302;
                column++;
            }
        }

        Component lastComponent;
        try {
            lastComponent = modsPanel.getComponents()[modsPanel.getComponentCount() - 1];
        }catch (ArrayIndexOutOfBoundsException e) {
            if (modsPanel.getComponentCount() == 0) return;
            lastComponent = modsPanel.getComponents()[0];
        }
        int calcY = lastComponent.getY() + lastComponent.getHeight() + 3;

        modsPanel.setSize(new Dimension(modsPanel.getWidth(), calcY));
        modsPanel.setPreferredSize(new Dimension(modsPanel.getWidth(), calcY));

        modsPanel.revalidate();
        modsPanel.repaint();
    }

    public void modRefreshRespectSize() {
        int yCache = 3;
        int xCache = 5;

        modsPanel.removeAll();

        int columns = (int) Math.floor((double) (modsPanel.getWidth() - 5) / 299);

        int column = 0;
        for (ModDisplayModule module : modules) {
            module.setBounds(xCache, yCache, 299, 121);
            modsPanel.add(module);

            if (column == columns - 1) {
                column = 0;
                xCache = 5;
                yCache += 124;
            } else {
                xCache += 302;
                column++;
            }
        }

        Component lastComponent;
        try {
            lastComponent = modsPanel.getComponents()[modsPanel.getComponentCount() - 1];
        }catch (ArrayIndexOutOfBoundsException e) {
            if (modsPanel.getComponentCount() == 0) return;
            lastComponent = modsPanel.getComponents()[0];
        }
        int calcY = lastComponent.getY() + lastComponent.getHeight() + 3;

        modsPanel.setSize(new Dimension(modsPanel.getWidth(), calcY));
        modsPanel.setPreferredSize(new Dimension(modsPanel.getWidth(), calcY));

        modsPanel.revalidate();
        modsPanel.repaint();

        Events.triggerEvent(TLDWREvents.MODLOAD_FINISHED.getName());
    }
}
