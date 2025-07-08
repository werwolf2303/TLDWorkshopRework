package de.werwolf2303.tldwr;

import com.jtattoo.plaf.luna.LunaLookAndFeel;
import com.squareup.okhttp.OkHttpClient;
import de.werwolf2303.tldwr.TLDPatcher.TLDPatcher;
import de.werwolf2303.tldwr.config.Config;
import de.werwolf2303.tldwr.frames.MainFrame;
import de.werwolf2303.tldwr.frames.PatchFrame;
import de.werwolf2303.tldwr.utils.OSDetect;
import de.werwolf2303.tldwr.workshop.WorkshopAPI;

import javax.swing.*;
import java.io.File;
import java.util.logging.Logger;

public class Initiator {
    private static final Logger logger = PublicValues.newLogger(Initiator.class.getSimpleName());

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(new LunaLookAndFeel());
            logger.info("Set look and feel to luna");
        } catch (UnsupportedLookAndFeelException e) {
            logger.warning("Failed setting look and feel");
            throw new RuntimeException(e);
        }

        PublicValues.osType = OSDetect.getDetectedOS();
        logger.info("Detected OS: " + OSDetect.getDetectedOS());

        for(TLDWREvents event : TLDWREvents.values()) {
            Events.register(event.getName(), true);
            logger.info("Registered event " + event.getName());
        }

        PublicValues.client = new OkHttpClient();
        logger.info("Initialized http client");

        if(new TLDPatcher().needPatching()) {
            logger.info("Asking to patch TLD");
            new PatchFrame().open();
        } else logger.info("TLD is patched");

        new WorkshopAPI();
        logger.info("Initialized Workshop API");

        PublicValues.config = new Config(new File(PublicValues.tldUserPath + File.separator + "Mods", "config.tldwr").getAbsolutePath());
        logger.info("Config path: " + PublicValues.config.getPath());

        logger.info("Opening window");
        new MainFrame().open();
    }
}
