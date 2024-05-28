package de.werwolf2303.tldwr.swingextensions;

import de.werwolf2303.tldwr.Events;
import de.werwolf2303.tldwr.TLDWREvents;
import de.werwolf2303.tldwr.frames.WorkshopFrame;
import de.werwolf2303.tldwr.workshop.WorkshopAPI;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;

public class ModDisplay extends JPanel {
    ArrayList<ModDisplayModule> modules;
    ArrayList<WorkshopAPI.Mod> mods;
    private WorkshopFrame mainFrame;
    private boolean ignoreInstalledMods;

    public ModDisplay(WorkshopFrame frame, boolean ignoreInstalledMods) {
        this.ignoreInstalledMods = ignoreInstalledMods;
        modules = new ArrayList<>();
        mods = new ArrayList<>();

        this.mainFrame = frame;

        setLayout(null);
    }

    public void setWorkshopFrame(WorkshopFrame frame) {
        this.mainFrame = frame;
    }

    public void initMod(WorkshopAPI.Mod mod, boolean disableExtendedFrame) {
        if (!ignoreInstalledMods) {
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
        modules.add(ModDisplayModule.init(mod, mainFrame, disableExtendedFrame));
        modRefresh();
    }

    public void addMod(WorkshopAPI.Mod mod, boolean disableExtendedFrame) {
        if (!ignoreInstalledMods) {
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
        modules.add(ModDisplayModule.init(mod, mainFrame, disableExtendedFrame));

        modRefreshRespectSize();
    }

    public void removeMods() {
        for (JPanel module : modules) {
            remove(module);
        }

        mods.clear();
        modules.clear();

        modRefreshRespectSize();
    }

    public void modRefreshRespectSize() {
        int yCache = 3;
        int xCache = 3;
        boolean firstRow = true;

        for (ModDisplayModule module : modules) {
            if (299 * 2 < getSize().width) {
                module.setBounds(xCache, yCache, 299, 121);
                add(module);
                xCache += 305;
                if (xCache + 305 > getPreferredSize().width) {
                    yCache += 124;
                    xCache = 3;
                }
            } else {
                if (firstRow) {
                    module.setBounds(3, yCache, 299, 121);
                    add(module);
                    firstRow = false;
                } else {
                    module.setBounds(305, yCache, 299, 121);
                    add(module);
                    firstRow = true;
                    yCache += 124;
                }
            }
        }

        setSize(new Dimension(xCache + 2, yCache + 3));
        setPreferredSize(new Dimension(xCache + 3, yCache + 3));

        revalidate();
        repaint();

        Events.triggerEvent(TLDWREvents.MODLOAD_FINISHED.getName());
    }

    private void modRefresh() {
        int yCache = 3;
        boolean firstRow = true;

        for (ModDisplayModule module : modules) {
            if (firstRow) {
                module.setBounds(3, yCache, 299, 121);
                add(module);
                firstRow = false;
            } else {
                module.setBounds(305, yCache, 299, 121);
                add(module);
                firstRow = true;
                yCache += 124;
            }
        }

        setPreferredSize(new Dimension(305, yCache + 3));
    }
}
