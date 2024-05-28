package de.werwolf2303.tldwr;

import com.sun.java.swing.plaf.motif.MotifLookAndFeel;
import de.werwolf2303.tldwr.TLDPatcher.TLDPatcher;
import de.werwolf2303.tldwr.config.Config;
import de.werwolf2303.tldwr.frames.MainFrame;
import de.werwolf2303.tldwr.frames.PatchFrame;
import de.werwolf2303.tldwr.frames.WorkshopFrame;
import de.werwolf2303.tldwr.utils.OSDetect;
import de.werwolf2303.tldwr.workshop.WorkshopAPI;

import javax.swing.*;
import java.io.File;

public class Initiator {
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(new MotifLookAndFeel());
        } catch (UnsupportedLookAndFeelException e) {
            throw new RuntimeException(e);
        }
        PublicValues.osType = OSDetect.getDetectedOS();
        for(TLDWREvents event : TLDWREvents.values()) {
            Events.register(event.getName(), true);
        }
        if(new TLDPatcher().needPatching()) {
            new PatchFrame().open();
        }
        new WorkshopAPI();
        PublicValues.config = new Config(new File(PublicValues.tldUserPath + File.separator + "Mods", "config.tldwr").getAbsolutePath());
        new MainFrame().open();
    }
}
