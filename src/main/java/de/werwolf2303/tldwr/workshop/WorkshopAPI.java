package de.werwolf2303.tldwr.workshop;

import com.google.gson.Gson;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import de.werwolf2303.tldwr.Events;
import de.werwolf2303.tldwr.PublicValues;
import de.werwolf2303.tldwr.TLDWREvents;
import net.lingala.zip4j.ZipFile;
import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.swing.*;
import java.io.*;
import java.net.ConnectException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.channels.ConnectionPendingException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.logging.Logger;

public class WorkshopAPI {
    private static String respositoryURL = "https://kolbenlp.gitlab.io/WorkshopTLDMods";
    private static String modlist = "modlist_3.json";
    private static ArrayList<Mod> modsCache;
    private static ArrayList<Object[]> imageCache;
    private static File configPath;
    private static int getStreamRetries = 0;
    private static int customRetries = 0;
    private static final Logger logger = PublicValues.newLogger(WorkshopAPI.class.getSimpleName());
    private static String modPackRepositoryURL = "https://gitlab.com/KolbenLP/WorkshopTLDMods";
    private static String modPackList = "/-/raw/WorkshopDatabase8.5/Modpacks/modlist_3.json";
    private static ArrayList<Mod> modPacksCache;

    public static class Mod {
        public String Name;
        public String Version;
        public String Description;
        public String Date;
        public String Link;
        public String PictureLink;
        public String FileName;
        public String Category;
        public String Changelog;
        public String Author;
    }

    @FunctionalInterface
    public interface DownloadProgressRunnable {
        void run(double percentage);
    }

    public WorkshopAPI() {
        WorkshopAPI.logger.info("Initializing Workshop API");

        modsCache = new ArrayList<>();
        imageCache = new ArrayList<>();
        modPacksCache = new ArrayList<>();

        checkIfModConfigIsPresent();

        reloadMods();
    }

    public static InputStream getImageStream(String url) throws IOException, MalformedURLException {
        if(url.isEmpty()) throw new MalformedURLException("No image available");
        for(Object[] object : imageCache) {
            if (object[0].toString().equals(url)) return new ByteArrayInputStream((byte[]) object[1]);
        }
        byte[] imageBytes = IOUtils.toByteArray(makeGetStream(url));
        imageCache.add(new Object[]{
                url,
                imageBytes
        });
        return new ByteArrayInputStream(imageBytes);
    }

    private void checkIfModConfigIsPresent() {
        configPath = new File(PublicValues.tldUserPath, "Mods" + File.separator + "mods.tldwr");

        if(!configPath.exists()) {
            JSONObject root = new JSONObject();
            JSONArray mods = new JSONArray();

            root.put("Mods", mods);

            try {
                configSave(root);
            } catch (IOException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(null, "Failed to write to mod store");
            }

            logger.info("Wrote new config: " + configPath);
        }else logger.info("Mod config exists");
    }

    public static ArrayList<Mod> checkForModUpdates() throws IOException {
        ArrayList<Mod> modsWithUpdates = new ArrayList<>();
        for(Mod mod : getInstalledMods()) {
            int index = searchForInstalledMod(mod.Name);
            if(index != -1) {
                if(!mod.Version.equals(getAllMods().get(index).Version)) {
                    modsWithUpdates.add(mod);
                }
            }
        }
        logger.info("Mods with updates: " + modsWithUpdates.size());
        return modsWithUpdates;
    }

    public static void executeUpdate(Mod mod, JProgressBar progress) {
        File tmpdir = new File(System.getProperty("java.io.tmpdir"));
        try (BufferedInputStream in = new BufferedInputStream(new URL(mod.Link).openStream());
             FileOutputStream fileOutputStream = new FileOutputStream(new File(tmpdir, mod.FileName))) {
            URL url = new URL(mod.Link);
            URLConnection con = url.openConnection();
            int size = con.getContentLength();
            progress.setMaximum(size);
            byte[] dataBuffer = new byte[1024];
            int bytesRead;
            double sumCount = 0.0;
            while ((bytesRead = in.read(dataBuffer, 0, 1024)) != -1) {
                fileOutputStream.write(dataBuffer, 0, bytesRead);
                sumCount += bytesRead;
                progress.setValue((int) sumCount);
            }
            addModToMyMods(mod);
            if(mod.Link.toLowerCase().endsWith(".zip")) {
                //Unpack zip
                new ZipFile(new File(tmpdir, mod.FileName)).extractAll(new File(PublicValues.tldUserPath, "Mods").getAbsolutePath());
            }
            copyFile(new File(tmpdir, mod.FileName), new File(PublicValues.tldUserPath + File.separator + "Mods", mod.FileName));
            Events.triggerEvent(TLDWREvents.DOWNLOAD_FINISHED.getName());
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Failed to download mod");
        }
    }

    private static int searchForInstalledMod(String name) throws IOException {
        for(Mod mod : getAllMods()) {
            if(mod.Name.equals(name)) {
                return getAllMods().indexOf(mod);
            }
        }
        return -1;
    }

    private static void configSave(JSONObject modified) throws IOException {
        FileWriter writer = new FileWriter(configPath);
        writer.write(modified.toString());
        writer.close();
    }

    private static String configLoad() throws IOException {
        return IOUtils.toString(Files.newInputStream(configPath.toPath()), Charset.defaultCharset());
    }

    public static void removeModFromMyMods(Mod mod) throws IOException {
        JSONObject root = new JSONObject(configLoad());

        int counter = 0;

        for(Object entry : root.getJSONArray("Mods")) {
            JSONObject modEntry = new JSONObject(entry.toString());

            if(mod.Name.equals(modEntry.getString("Name"))) {
                break;
            }
            counter++;
        }

        if(!new File(PublicValues.tldUserPath + File.separator + "Mods", root.getJSONArray("Mods").getJSONObject(counter).getString("FileName")).delete()) {
            JOptionPane.showMessageDialog(null, "Failed to delete mod");
        }

        root.getJSONArray("Mods").remove(counter);
        configSave(root);
    }

    private static void addModToMyMods(Mod mod) throws IOException {
        JSONObject root = new JSONObject(configLoad());
        boolean alreadyExists = false;
        int existsAt = 0;
        JSONObject newMod = new JSONObject();

        newMod.put("Name", mod.Name);
        newMod.put("Version", mod.Version);
        newMod.put("Description", mod.Description);
        newMod.put("Date", mod.Date);
        newMod.put("Link", mod.Link);
        newMod.put("PictureLink", mod.PictureLink);
        newMod.put("FileName", mod.FileName);
        newMod.put("Category", mod.Category);
        newMod.put("Changelog", mod.Changelog);
        newMod.put("Author", mod.Author);

        for(int counter = 0; counter < root.getJSONArray("Mods").length(); counter ++) {
            Object modEntry = root.getJSONArray("Mods").get(counter);
            JSONObject modJSON = new JSONObject(modEntry.toString());
            if(modJSON.getString("Name").equals(mod.Name)) {
                alreadyExists = true;
                existsAt = counter;
                break;
            }
        }

        if(!alreadyExists) {
            root.getJSONArray("Mods").put(newMod);
        }else{
            root.getJSONArray("Mods").remove(existsAt);
            root.getJSONArray("Mods").put(existsAt, newMod);
        }
        configSave(root);
    }

    private static InputStream makeGetStream(String url) throws IOException, ConnectionPendingException {
        if(getStreamRetries > 4) {
            throw new ConnectionPendingException();
        }

        OkHttpClient client = PublicValues.client;
        client.setRetryOnConnectionFailure(true);
        Request request = new Request.Builder()
                .url(url)
                .build();

        try {
            InputStream stream = client.newCall(request).execute().body().byteStream();
            getStreamRetries = 0;
            return stream;
        }catch (ConnectException e) {
            getStreamRetries++;
            return makeGetStream(url);
        }
    }

    public static ArrayList<Mod> getAllMods() {
        return modsCache;
    }

    private static void copyFile(File file, File to) throws IOException {
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

    public static void download(Mod mod, DownloadProgressRunnable runnable) {
        download(mod, runnable, PublicValues.tldUserPath + File.separator + "Mods", true);
    }

    public static void download(Mod mod, DownloadProgressRunnable runnable, String downloadPath, boolean addToMyMods) {
        Thread downloadThread = new Thread(() -> {
            try {
                downloadSingleThreaded(mod, runnable, downloadPath, addToMyMods);
            } catch (IOException ignored) {
            }
        });
        downloadThread.start();
    }

    public static void downloadSingleThreaded(Mod mod, DownloadProgressRunnable runnable, String downloadPath, boolean addToMyMods) throws IOException {
        File tmpdir = new File(System.getProperty("java.io.tmpdir"));
        try (BufferedInputStream in = new BufferedInputStream(new URL(mod.Link).openStream());
             FileOutputStream fileOutputStream = new FileOutputStream(new File(tmpdir, mod.FileName))) {
            URL url = new URL(mod.Link);
            URLConnection con = url.openConnection();
            int size = con.getContentLength();
            byte[] dataBuffer = new byte[1024];
            int bytesRead;
            double sumCount = 0.0;
            while ((bytesRead = in.read(dataBuffer, 0, 1024)) != -1) {
                fileOutputStream.write(dataBuffer, 0, bytesRead);
                sumCount += bytesRead;
                runnable.run(sumCount / size * 100.0);
            }
            if (addToMyMods) addModToMyMods(mod);
            if(mod.Link.toLowerCase().endsWith(".zip")) {
                //Unpack zip
                new ZipFile(new File(tmpdir, mod.FileName)).extractAll(downloadPath);
                if(new File(downloadPath, mod.FileName).exists()) {
                    new File(downloadPath, mod.FileName).delete();
                }
                logger.info("Unzipped " + mod.FileName + " to " + downloadPath);
            }else {
                logger.info("Copying " + mod.FileName + " to " + new File(downloadPath, mod.FileName).getAbsolutePath());
                copyFile(new File(tmpdir, mod.FileName), new File(downloadPath, mod.FileName));
            }
            if(addToMyMods) Events.triggerEvent(TLDWREvents.DOWNLOAD_FINISHED.getName());
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Failed to download mod: " + mod.Name);
            throw e;
        }
    }

    public static ArrayList<Mod> getInstalledMods() throws IOException {
        ArrayList<Mod> mods = new ArrayList<>();
        JSONObject root = new JSONObject(configLoad());

        for(Object entry : root.getJSONArray("Mods")) {
            Mod mod = new Gson().fromJson(entry.toString(), Mod.class);
            mods.add(mod);
        }

        logger.info("Getting installed mods");
        return mods;
    }

    public static void reloadMods() throws ConnectionPendingException {
        if(customRetries > 4) {
            throw new ConnectionPendingException();
        }
        logger.info("Reloading mods");
        modsCache.clear();
        modPacksCache.clear();
        OkHttpClient client = PublicValues.client;
        client.setRetryOnConnectionFailure(true);
        Request request = new Request.Builder()
                .url(respositoryURL + "/" + modlist)
                .build();
        try {
            for (Object entry : new JSONObject(client.newCall(request).execute().body().string()).getJSONArray("Mods")) {
                modsCache.add(new Gson().fromJson(entry.toString(), Mod.class));
            }

            request = request.newBuilder().url(modPackRepositoryURL + modPackList).build();

            for (Object entry : new JSONObject(client.newCall(request).execute().body().string()).getJSONArray("Mods")) {
                modPacksCache.add(new Gson().fromJson(entry.toString(), Mod.class));
            }
            customRetries = 0;
        } catch (ConnectException e) {
            logger.warning("Connect exception while reloading mods. Retrying...");
            logger.warning(e.getMessage());
            customRetries++;
            reloadMods();
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Failed to fetch mods");
        }
    }

    public static int getAmountOfModsInCategory(String categoryName) {
        int count = 0;

        for(Mod mod : getAllMods()) {
            if(mod.Category.equals(categoryName)) {
                count++;
            }
        }

        return count;
    }

    public static ArrayList<Mod> getModsInCategory(int offset, int limit, String categoryName) {
        ArrayList<Mod> modsCache = new ArrayList<>();
        ArrayList<Mod> mods = new ArrayList<>();

        for(Mod mod : getAllMods()) {
            if(mod.Category.equals(categoryName)) {
                modsCache.add(mod);
            }
        }

        int endIndex = Math.min(offset + limit, modsCache.size());
        for(Mod mod : new ArrayList<>(modsCache.subList(offset, endIndex))) {
            mods.add(mod);
        }

        return mods;
    }

    public static ArrayList<Mod> getInstalledMods(int offset, int limit) throws IOException {
        ArrayList<Mod> mods = new ArrayList<>();

        if (getInstalledMods().isEmpty() || offset < 0 || limit < 1 || offset >= getInstalledMods().size()) {
            return new ArrayList<>();
        }

        int endIndex = Math.min(offset + limit, getInstalledMods().size());
        for(Mod mod : new ArrayList<>(getInstalledMods().subList(offset, endIndex))) {
            mods.add(mod);
        }

        return mods;
    }

    public static ArrayList<Mod> getMods(int offset, int limit) {
        ArrayList<Mod> mods = new ArrayList<>();

        if (modsCache.isEmpty() || offset < 0 || limit < 1 || offset >= modsCache.size()) {
            return new ArrayList<>();
        }

        int endIndex = Math.min(offset + limit, modsCache.size());
        for(Mod mod : new ArrayList<>(modsCache.subList(offset, endIndex))) {
            mods.add(mod);
        }

        return mods;
    }

    public static ArrayList<Mod> getAllModPacks() {
        return modPacksCache;
    }

    public static void deleteModPackFolder(File path) {
        for (File file : path.listFiles()) {
            if (file.isDirectory()) {
                deleteModPackFolder(file);
                continue;
            }
            file.delete();
        }
    }

    public static void runWithModPack(String txtPath, DownloadProgressRunnable callback) {
        new Thread(() -> {
            try {
                if (new File(PublicValues.tldUserPath, "Modpack").exists()) {
                    deleteModPackFolder(new File(PublicValues.tldUserPath, "Modpack"));
                }

                new File(PublicValues.tldUserPath, "Modpack").mkdir();

                String[] mods = IOUtils.toString(Files.newInputStream(Paths.get(txtPath)), StandardCharsets.UTF_8).split("\n");
                int unavailableMods = 0;
                for (String mod : mods) {
                    if (mod.equals("")) continue;
                    boolean found = false;

                    for (Mod listedMod : getAllMods()) {
                        if(listedMod.FileName.equals(mod)) {
                            downloadSingleThreaded(listedMod, callback, new File(PublicValues.tldUserPath, "Modpack").getAbsolutePath(), false);
                            found = true;
                            break;
                        }
                    }

                    if (!found) unavailableMods++;
                }

                Events.triggerEvent(TLDWREvents.DOWNLOAD_FINISHED.getName());

                JOptionPane.showMessageDialog(null, "Modpack download finished. Not available mods: " + unavailableMods);

                new File(PublicValues.tldUserPath + File.separator + "Mods" + File.separator + "temp").mkdir();
                new File(PublicValues.tldUserPath + File.separator + "Mods" + File.separator + "temp" + File.separator + "mp.dat").createNewFile();

                switch (PublicValues.osType) {
                    case Linux:
                        Runtime.getRuntime().exec(new String[] {new File(PublicValues.steamPath, "steam.sh").getAbsolutePath(), "steam://rungameid/1017180"});
                        break;
                    case Windows:
                        Runtime.getRuntime().exec(new String[] {new File(PublicValues.steamPath, "steam.exe").getAbsolutePath(), "steam://rungameid/1017180"});
                        break;
                    default:
                        JOptionPane.showMessageDialog(null, "Unsupported OS! Please start the game manually");
                }
            } catch (IOException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(null, "Failed to start tld with modpack: " + e.getMessage());
            }
        }, "Modpack download").start();
    }
}
