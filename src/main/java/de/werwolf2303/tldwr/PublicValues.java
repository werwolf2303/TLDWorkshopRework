package de.werwolf2303.tldwr;

import de.werwolf2303.tldwr.config.Config;
import de.werwolf2303.tldwr.frames.Frame;
import de.werwolf2303.tldwr.frames.MainFrame;
import de.werwolf2303.tldwr.frames.ModExpandedFrame;
import de.werwolf2303.tldwr.swingextensions.ModDisplayModule;
import de.werwolf2303.tldwr.utils.AutoThrow;
import de.werwolf2303.tldwr.utils.OSDetect;

import java.net.URL;

public class PublicValues {
    public static URL tldPatcherDownloadURL = AutoThrow.create(URL.class,"https://gitlab.com/KolbenLP/WorkshopTLDMods/-/raw/WorkshopDatabase8.5/Workshop/TLDPatcher.zip");
    public static URL tldLoaderDownloadURL = AutoThrow.create(URL.class, "https://gitlab.com/KolbenLP/WorkshopTLDMods/-/raw/WorkshopDatabase8.5/Workshop/TLDLoader.dll");
    public static String tldPath;
    public static String steamPath;
    public static OSDetect.OSType osType;
    public static String tldUserPath;
    public static Frame currentFrame;
    public static ModExpandedFrame modExpandedFrame;
    public static ModDisplayModule lastHighlightedMod;
    public static MainFrame mainFrame;
    public static boolean extendedFrameVisible = false;
    public static Config config;
}
