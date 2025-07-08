package de.werwolf2303.tldwr.TLDPatcher;

import com.dd.plist.NSDictionary;
import com.dd.plist.PropertyListFormatException;
import com.dd.plist.PropertyListParser;
import de.werwolf2303.tldwr.PublicValues;
import de.werwolf2303.tldwr.utils.OSDetect;
import de.werwolf2303.tldwr.vdfParser.VDF;
import net.lingala.zip4j.ZipFile;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.json.JSONObject;
import org.xml.sax.SAXException;

import javax.swing.*;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.net.SocketException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Objects;
import java.util.logging.Logger;

public class TLDPatcher {
    private final File tldPatcherConfigPath;
    private final File tldPatcherPathConfigPath;
    private final Logger logger = PublicValues.newLogger(getClass().getSimpleName());

    public TLDPatcher() {
        tldPatcherPathConfigPath = new File(System.getProperty("user.home"), "TLDWR" + File.separator + "path.json");
        logger.info("TLD patcher path config path: " + tldPatcherPathConfigPath.getAbsolutePath());

        if(!tldPatcherPathConfigPath.exists()) {
            switch (PublicValues.osType) {
                case MacOS:
                case Windows:
                case Linux:
                    findSteamPath(PublicValues.osType);
                    findTLDPath();
                    findTLDUserPath();
                    break;
                default:
                    //User must manually select: TLD install path and mod folder path
                    PublicValues.tldPath = promptUserForManualPathSelection("Unknown Operating System! Please select the The Long Drive install directory", null, "TheLongDrive.exe");
                    PublicValues.tldUserPath = promptUserForManualPathSelection("Unknown Operating System! Please select the The Long Drive inside the Documents folder", null, "settings.tldc");
                    PublicValues.steamPath = promptUserForManualPathSelection("Unknown Operating System! Please select the Steam install directory that manages The Long Drive", null, "steamapps");
            }
            try {
                tldPatcherPathConfigPath.getParentFile().mkdir();
                tldPatcherPathConfigPath.createNewFile();
                FileWriter writer = new FileWriter(tldPatcherPathConfigPath);
                JSONObject root = new JSONObject();
                root.put("TLDPath", PublicValues.tldPath);
                root.put("TLDUserPath", PublicValues.tldUserPath);
                root.put("SteamPath", PublicValues.steamPath);
                writer.write(root.toString());
                writer.close();
                logger.info("Created patcher path config and wrote defaults");
            } catch (IOException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(null, "Failed to create path config");
            }
        }else{
            try {
                JSONObject root = new JSONObject(IOUtils.toString(Files.newInputStream(tldPatcherPathConfigPath.toPath()), Charset.defaultCharset()));
                PublicValues.tldPath = root.getString("TLDPath");
                PublicValues.tldUserPath = root.getString("TLDUserPath");
                PublicValues.steamPath = root.getString("SteamPath");

                logger.info("Loaded patcher path config");
                logger.info("TLDPath: " + PublicValues.tldPath);
                logger.info("TLDUserPath: " + PublicValues.tldUserPath);
                logger.info("SteamPath: " + PublicValues.steamPath);
            } catch (NullPointerException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(null, "Failed to read path config");
                System.exit(-1);
            } catch (IOException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(null, "Failed to read path config");
                System.exit(-1);
            }
        }

        tldPatcherConfigPath = new File(PublicValues.tldUserPath + File.separator + "Mods", "patcher.tldwr");
        logger.info("Patcher config path: " + tldPatcherConfigPath.getAbsolutePath());
        if(!tldPatcherConfigPath.exists()) {
            if(!new File(PublicValues.tldUserPath, "Mods").exists()) {
                new File(PublicValues.tldUserPath, "Mods").mkdir();
            }
            JSONObject root = new JSONObject();
            root.put("Patched", false);
            try {
                root.put("GameVersion", getGameVersion());
            } catch (IOException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(null, "Failed to get game version");
            }
            try {
                configSave(root);
            }catch (IOException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(null, "Failed to save patcher config");
            }
            logger.info("Created patcher config and wrote defaults");
        }

        PublicValues.modPackFolderPath = new File(PublicValues.tldUserPath, "Modpacks").getAbsolutePath();
        logger.info("Modpack folder path: " + PublicValues.modPackFolderPath);

        if (!new File(PublicValues.tldUserPath, "Modpacks").exists()) {
            new File(PublicValues.tldUserPath, "Modpacks").mkdir();
        }

        if (!new File(PublicValues.tldUserPath, "Modpack").exists()) {
            new File(PublicValues.tldUserPath, "Modpack").mkdir();
        }
    }

    private String getGameVersion() throws IOException {
        String output = IOUtils.toString(Files.newInputStream(new File(PublicValues.tldPath + File.separator + "TheLongDrive_Data", "news.txt").toPath()));
        for(String s : output.split("\n")) {
            if(s.startsWith("v") && s.contains(":")) {
                return s.replace(":", "");
            }
        }
        logger.info("Detected game version: " + output);
        return "";
    }

    private void configSave(JSONObject modified) throws IOException {
        FileWriter writer = new FileWriter(tldPatcherConfigPath);
        writer.write(modified.toString());
        writer.close();
    }

    private String configLoad() throws IOException {
        return IOUtils.toString(Files.newInputStream(tldPatcherConfigPath.toPath()), Charset.defaultCharset());
    }

    public void startPatching() {
        installModloader();
        try {
            JSONObject root = new JSONObject(configLoad());
            root.put("GameVersion", getGameVersion());
            root.put("Patched", true);
            configSave(root);
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Failed to load config");
        }
        if(PublicValues.mainFrame == null) PublicValues.currentFrame.close();
        JOptionPane.showMessageDialog(null, "Installation done");
    }

    private void cleanTLDLoaderInstall() {
        new File(PublicValues.tldPath + File.separator + "TheLongDrive_Data" + File.separator + "Managed" + File.separator + "TLDLoader.dll").delete();
    }

    private void removeTLDModifications() {
        cleanTLDLoaderInstall();
        try {
            FileUtils.deleteDirectory(new File(PublicValues.tldUserPath, "Mods"));
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Failed to delete directory");
        }
        try {
            copyFile(new File(PublicValues.tldPath + File.separator + "TheLongDrive_Data" + File.separator + "Managed" + File.separator + "Assembly-CSharp.original.dll"),
                    new File(PublicValues.tldPath + File.separator + "TheLongDrive_Data" + File.separator + "Managed" + File.separator + "Assembly-CSharp.dll"));
        }catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Failed to copy file/s");
        }
    }

    public void uninstall() {
        cleanTLDLoaderInstall();
        removeTLDModifications();
        try {
            FileUtils.deleteDirectory(tldPatcherPathConfigPath.getParentFile());
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Failed to delete directory");
        }
    }

    private void copyFile(File file, File to) throws IOException {
        try (
                InputStream in = new BufferedInputStream(Files.newInputStream(file.toPath()));
                OutputStream out = new BufferedOutputStream(
                Files.newOutputStream(to.toPath()))) {

            byte[] buffer = new byte[1024];
            int lengthRead;
            while ((lengthRead = in.read(buffer)) > 0) {
                out.write(buffer, 0, lengthRead);
                out.flush();
            }
        }
    }

    private void copyStream(InputStream stream, File to) throws IOException {
        try (
                InputStream in = new BufferedInputStream(stream);
                OutputStream out = new BufferedOutputStream(
                        Files.newOutputStream(to.toPath()))) {

            byte[] buffer = new byte[1024];
            int lengthRead;
            while ((lengthRead = in.read(buffer)) > 0) {
                out.write(buffer, 0, lengthRead);
                out.flush();
            }
        }
    }

    public boolean needPatching() {
        try {
            JSONObject root = new JSONObject(configLoad());
            if(!root.getString("GameVersion").equals(getGameVersion())) {
                return true;
            }
            if(!root.getBoolean("Patched")) {
                return true;
            }
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Failed to load config");
        }
        return false;
    }

    private void installModloader() {
        //Overwrite all files found

        logger.info("Start game patching");
        try {
            JSONObject root = new JSONObject(configLoad());
            if(!root.getString("GameVersion").equals(getGameVersion())) {
                JOptionPane.showMessageDialog(null, "!!!The Game version is incompatible!!! Download the right Workshop version for your game version");
                System.exit(-1);
            }
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Failed to load config");
            System.exit(-1);
        }

        try {
            copyFile(new File(PublicValues.tldPath + File.separator + "TheLongDrive_Data" + File.separator + "Managed" + File.separator + "Assembly-CSharp.dll"),
                    new File(PublicValues.tldPath + File.separator + "TheLongDrive_Data" + File.separator + "Managed" + File.separator + "Assembly-CSharp.original.dll"));
        }catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Failed to copy file/s");
            System.exit(-1);
        }
        logger.info("Created backup of original Assembly-CSharp.dll");

        try {
            copyStream(Objects.requireNonNull(getClass().getResourceAsStream("/Assembly-CSharp.dll")),
                    new File(PublicValues.tldPath + File.separator + "TheLongDrive_Data" + File.separator + "Managed" + File.separator + "Assembly-CSharp.dll"));
        }catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Failed to copy file/s");
            System.exit(-1);
        }
        logger.info("Copied Assembly-CSharp.dll into Managed");

        try {
            copyStream(PublicValues.tldLoaderDownloadURL.openStream(), new File(PublicValues.tldPath + File.separator + "TheLongDrive_Data" + File.separator + "Managed" + File.separator + "TLDLoader.dll"));
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Failed to download TLDLoader.dll");
            System.exit(-1);
        }
        logger.info("Copied TLDLoader.dll into Managed");

        String tmpdir = System.getProperty("java.io.tmpdir");
        try (BufferedInputStream in = new BufferedInputStream(PublicValues.tldPatcherDownloadURL.openStream());
             FileOutputStream fileOutputStream = new FileOutputStream(tmpdir + File.separator + "TLDPatcher.zip")) {
            byte dataBuffer[] = new byte[1024];
            int bytesRead;
            while ((bytesRead = in.read(dataBuffer, 0, 1024)) != -1) {
                fileOutputStream.write(dataBuffer, 0, bytesRead);
            }
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Failed to download TLDPatcher.zip");
            System.exit(-1);
        }
        logger.info("Downloaded patcher from: " + PublicValues.tldPatcherDownloadURL.toString());

        if(!new File(PublicValues.tldUserPath + File.separator + "Mods", "Assets").exists()) {
            new File(PublicValues.tldUserPath + File.separator + "Mods", "Assets").mkdir();
            logger.info("Created assets directory in mod folder");
        }

        try {
            new ZipFile(tmpdir + File.separator + "TLDPatcher.zip").extractAll(PublicValues.tldUserPath + File.separator + "Mods" + File.separator + "Assets");
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Failed to extract TLDPatcher");
            System.exit(-1);
        }
        logger.info("Unzipped TLDPatcher.zip");
    }

    private void findSteamPath(OSDetect.OSType osType) {
        String steamPath = "";

        switch (osType) {
            case Windows:
                steamPath = "C:\\\\Program Files (x86)\\\\Steam";
                break;
            case Linux:
                steamPath = System.getProperty("user.home") + "/.local/share/Steam";
                if (!new File(steamPath).exists()) {
                    steamPath = System.getProperty("user.home") + "/.var/app/com.valvesoftware.Steam/.local/share/Steam";
                }
                break;
            case MacOS:
                steamPath = findSteamPathMacOS();
        }

        if(!new File(steamPath).exists()) {
            System.err.println("Steam install path invalid! Prompting user for manual input...");
            PublicValues.steamPath = promptUserForManualPathSelection("Steam not found! Please select path manually", null, "steamapps");
            return;
        }

        PublicValues.steamPath = steamPath;
        logger.info("Found steam: " + PublicValues.steamPath);
    }

    private void findTLDUserPath() {
        switch (PublicValues.osType) {
            case Linux:
                findTLDUserPathLinux();
                break;
            case Windows:
                findTLDUserPathWindows();
                break;
            case MacOS:
                findTLDUserPathMacOS();
        }
    }

    private void findTLDUserPathWindows() {
        File tldUserPath = new File(System.getProperty("user.home"), "Documents\\TheLongDrive");

        if(!tldUserPath.exists()) {
            JOptionPane.showMessageDialog(null, "Please start The Long Drive once");
            System.exit(-1);
            return;
        }

        PublicValues.tldUserPath = tldUserPath.getAbsolutePath();
        logger.info("Found tld user path: " + PublicValues.tldUserPath);
    }

    private void findTLDUserPathLinux() {
        File protonPath = new File(PublicValues.steamPath, "steamapps/compatdata/1017180/pfx/drive_c/users/steamuser/Documents/TheLongDrive");

        if(!protonPath.exists()) {
            JOptionPane.showMessageDialog(null, "Please start The Long Drive once to create the proton prefix");
            System.exit(-1);
        }

        PublicValues.tldUserPath = protonPath.getAbsolutePath();
        logger.info("Found tld user path: " + PublicValues.tldUserPath);
    }

    private void findTLDUserPathMacOS() {
        //Whisky

        File tldUserPath = new File(new File(PublicValues.steamPath).getParentFile().getParentFile(), "users/crossover/Documents/TheLongDrive");

        if(!tldUserPath.exists()) {
            JOptionPane.showMessageDialog(null, "Please start The Long Drive once");
            System.exit(-1);
        }

        PublicValues.tldUserPath = tldUserPath.getAbsolutePath();
        logger.info("Found tld user path: " + PublicValues.tldUserPath);
    }

    private void findTLDPath() {
        switch (PublicValues.osType) {
            case Linux:
            case Windows:
                findTLDPathDefault();
                break;
            case MacOS:
                findTLDPathMacOS();
                break;
        }
    }

    private void findTLDPathDefault() {
        try {
            JSONObject root = VDF.toJSONObject(IOUtils.toString(Files.newInputStream(new File(PublicValues.steamPath, "steamapps" + File.separator + "libraryfolders.vdf").toPath()), Charset.defaultCharset()), true);
            boolean foundTLD = false;
            for(Object libraryFolder : root.getJSONArray("libraryfolders")) {
                JSONObject entry = new JSONObject(libraryFolder.toString());
                File realPath = new File(entry.getString("path"));
                File tldPath = new File(realPath, "steamapps" + File.separator + "common" + File.separator + "The Long Drive");

                if (!tldPath.exists()) {
                    continue;
                }

                foundTLD = true;

                PublicValues.tldPath = tldPath.getAbsolutePath();
            }
            if(!foundTLD) {
                throw new IOException("Game not installed");
            }
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Didn't find The Long Drive! Is it installed?");
            System.exit(-1);
        }
        logger.info("Found tld path: " + PublicValues.tldPath);
    }

    private void findTLDPathWhisky() {
        try {
            JSONObject root = VDF.toJSONObject(IOUtils.toString(Files.newInputStream(new File(PublicValues.steamPath, "steamapps/libraryfolders.vdf").toPath()), Charset.defaultCharset()), true);
            for(Object libraryFolder : root.getJSONArray("libraryfolders")) {
                JSONObject entry = new JSONObject(libraryFolder.toString());
                File realPath = new File(new File(PublicValues.steamPath).getParentFile().getParentFile(), entry.getString("path").replace("C:\\", "").replaceAll("\\\\", "/"));
                File tldPath = new File(realPath, "steamapps/common/The Long Drive");

                if (!tldPath.exists()) {
                    throw new IOException("Game not installed");
                }

                PublicValues.tldPath = tldPath.getAbsolutePath();
            }
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Didn't find The Long Drive! Is it installed?");
            System.exit(-1);
        }
        logger.info("Found tld path: " + PublicValues.tldPath);
    }

    private void findTLDPathMacOS() {
        if(PublicValues.steamPath.contains("Whisky") && PublicValues.steamPath.contains("(x86)")) {
            findTLDPathWhisky();
            return;
        }

        findTLDPathDefault();

        logger.info("Found tld path: " + PublicValues.tldPath);
    }

    private String findSteamPathMacOS() {
        ArrayList<String> steamInstalls = new ArrayList<>();
        ArrayList<String> whiskyBottleNamesWithSteam = new ArrayList<>();

        if(new File(System.getProperty("user.home") + "/Library/Containers/com.isaacmarovitz.Whisky").exists()) {
            for(String bottle : Objects.requireNonNull(new File(System.getProperty("user.home") + "/Library/Containers/com.isaacmarovitz.Whisky/Bottles").list(
                    (dir, name) -> dir.isDirectory() && !name.startsWith(".")))
            ) {
                if(new File(System.getProperty("user.home") + "/Library/Containers/com.isaacmarovitz.Whisky/Bottles/" + bottle + "/drive_c/Program Files (x86)/Steam").exists()) {
                    try {
                        NSDictionary root = (NSDictionary) PropertyListParser.parse(new File(System.getProperty("user.home") + "/Library/Containers/com.isaacmarovitz.Whisky/Bottles/" + bottle + "/Metadata.plist"));
                        whiskyBottleNamesWithSteam.add(((NSDictionary)root.objectForKey("info")).get("name").toString());
                    } catch (IOException | PropertyListFormatException | ParseException | ParserConfigurationException |
                             SAXException e) {
                        e.printStackTrace();
                        whiskyBottleNamesWithSteam.add("unknown");
                    }
                    steamInstalls.add(System.getProperty("user.home") + "/Library/Containers/com.isaacmarovitz.Whisky/Bottles/" + bottle + "/drive_c/Program Files (x86)/Steam");
                }
            }
        }

        ArrayList<String> userFriendlyNames = new ArrayList<>();
        int whiskyNameCounter = 0;

        for(String s : steamInstalls) {
            if(s.contains("(x86)") && s.contains("Whisky")) {
                userFriendlyNames.add("Whisky (" + whiskyBottleNamesWithSteam.get(whiskyNameCounter) + ")");
            }
            whiskyNameCounter++;
        }

        if(steamInstalls.size() == 1) return steamInstalls.get(0);
        if(steamInstalls.isEmpty()) {
            JOptionPane.showMessageDialog(null, "No Steam installation found", "Can't continue", JOptionPane.ERROR_MESSAGE);
            System.exit(-1);
        }

        Object userSelection = JOptionPane.showInputDialog(null,
                "Select Steam install",
                "Multiple Steam installations found",
                JOptionPane.INFORMATION_MESSAGE, null,
                userFriendlyNames.toArray(),
                userFriendlyNames.get(0)
        );

        if(userSelection == null) System.exit(-1); //User aborted installation

        return steamInstalls.get(userFriendlyNames.indexOf((String) userSelection));
    }

    private String promptUserForManualPathSelection(String title, JFrame parent, String searchForInside) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle(title);
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

        if(fileChooser.showOpenDialog(parent) == JFileChooser.CANCEL_OPTION) System.exit(-1); //User aborted

        if(!new File(fileChooser.getSelectedFile(), searchForInside).exists()) {
            if(PublicValues.osType == OSDetect.OSType.MacOS) {
                JOptionPane.showMessageDialog(parent,
                        "Wrong folder! Because of MacOS we have to close the Workshop! Open it again to select a folder",
                        "Can't continue",
                        JOptionPane.ERROR_MESSAGE
                );
                System.exit(-1);
            }
            return promptUserForManualPathSelection(title, parent, searchForInside);
        }

        return fileChooser.getSelectedFile().getAbsolutePath();
    }
}
