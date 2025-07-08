package de.werwolf2303.tldwr;

import com.squareup.okhttp.OkHttpClient;
import de.werwolf2303.tldwr.config.Config;
import de.werwolf2303.tldwr.frames.Frame;
import de.werwolf2303.tldwr.frames.MainFrame;
import de.werwolf2303.tldwr.frames.ModExpandedFrame;
import de.werwolf2303.tldwr.swingextensions.ModDisplayModule;
import de.werwolf2303.tldwr.utils.AutoThrow;
import de.werwolf2303.tldwr.utils.OSDetect;

import java.net.URL;
import java.util.logging.ConsoleHandler;
import java.util.logging.Logger;

public class PublicValues {
    public static URL tldPatcherDownloadURL = AutoThrow.create(URL.class,"https://gitlab.com/KolbenLP/WorkshopTLDMods/-/raw/WorkshopDatabase8.5/Workshop/TLDPatcher.zip");
    public static URL tldLoaderDownloadURL = AutoThrow.create(URL.class, "https://gitlab.com/KolbenLP/WorkshopTLDMods/-/raw/WorkshopDatabase8.5/Workshop/TLDLoader.dll");
    public static String tldPath;
    public static String steamPath;
    public static OSDetect.OSType osType;
    public static String tldUserPath;
    public static Frame currentFrame;
    public static ModDisplayModule lastHighlightedMod;
    public static MainFrame mainFrame;
    public static Config config;
    public static OkHttpClient client;
    public static String modPackFolderPath;

    public static Logger newLogger(String className) {
        Logger logger = Logger.getLogger(className);
        logger.setUseParentHandlers(false);
        ConsoleHandler consoleHandler = new ConsoleHandler();
        consoleHandler.setFormatter(new LoggerFormatter());
        logger.addHandler(consoleHandler);
        return logger;
    }
}
