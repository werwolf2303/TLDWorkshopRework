package de.werwolf2303.tldwr.workshop;

import com.google.gson.Gson;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import de.werwolf2303.tldwr.Events;
import de.werwolf2303.tldwr.PublicValues;
import de.werwolf2303.tldwr.TLDWREvents;
import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.model.FileHeader;
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
import java.nio.file.Files;
import java.util.ArrayList;

public class WorkshopAPI {
    private static String respositoryURL = "https://kolbenlp.gitlab.io/WorkshopTLDMods";
    private static String modlist = "modlist_3.json";
    private static ArrayList<Mod> modsCache;
    private static ArrayList<Object[]> imageCache;
    private static File configPath;
    private static int getStreamRetries = 0;
    private static int getStringRetries = 0;
    private static int customRetries = 0;

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
        modsCache = new ArrayList<>();
        imageCache = new ArrayList<>();

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
        }
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
        return modsWithUpdates;
    }

    public static void executeUpdate(Mod mod, JProgressBar progress) {
        File tmpdir = new File(System.getProperty("java.io.tmpdir"));
        try (BufferedInputStream in = new BufferedInputStream(new URL(mod.Link).openStream());
             FileOutputStream fileOutputStream = new FileOutputStream(new File(tmpdir, getFileName(mod.Link)))) {
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
                new ZipFile(new File(tmpdir, getFileName(mod.Link))).extractAll(new File(PublicValues.tldUserPath, "Mods").getAbsolutePath());
            }
            copyFile(new File(tmpdir, getFileName(mod.Link)), new File(PublicValues.tldUserPath + File.separator + "Mods", getFileName(mod.Link)));
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

    private static String getFileName(String url) {
         return url.split("/")[url.split("/").length-1];
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

    private static String makeGetString(String url) throws IOException, ConnectionPendingException {
        if(getStringRetries > 4) {
            throw new ConnectionPendingException();
        }
        OkHttpClient client = new OkHttpClient();
        client.setRetryOnConnectionFailure(true);
        Request request = new Request.Builder()
                .url(url)
                .build();
        try {
            String ret = client.newCall(request).execute().body().string();
            getStringRetries = 0;
            return ret;
        }catch (ConnectException e) {
            getStringRetries++;
            return makeGetString(url);
        }
    }

    private static InputStream makeGetStream(String url) throws IOException, ConnectionPendingException {
        if(getStreamRetries > 4) {
            throw new ConnectionPendingException();
        }
        OkHttpClient client = new OkHttpClient();
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

    public static ArrayList<Mod> getAllMods() throws IOException {
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
        Thread downloadThread = new Thread(() -> {
            File tmpdir = new File(System.getProperty("java.io.tmpdir"));
            try (BufferedInputStream in = new BufferedInputStream(new URL(mod.Link).openStream());
                 FileOutputStream fileOutputStream = new FileOutputStream(new File(tmpdir, getFileName(mod.Link)))) {
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
                addModToMyMods(mod);
                if(mod.Link.toLowerCase().endsWith(".zip")) {
                    //Unpack zip
                    new ZipFile(new File(tmpdir, getFileName(mod.Link))).extractAll(new File(PublicValues.tldUserPath, "Mods").getAbsolutePath());
                    if(new File(PublicValues.tldUserPath + File.separator + "Mods", getFileName(mod.Link)).exists()) {
                        new File(PublicValues.tldUserPath + File.separator + "Mods", getFileName(mod.Link)).delete();
                    }
                }
                copyFile(new File(tmpdir, getFileName(mod.Link)), new File(PublicValues.tldUserPath + File.separator + "Mods", getFileName(mod.Link)));
                Events.triggerEvent(TLDWREvents.DOWNLOAD_FINISHED.getName());
            } catch (IOException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(null, "Failed to download mod");
            }
        });
        downloadThread.start();
    }

    public static ArrayList<Mod> getInstalledMods() throws IOException {
        ArrayList<Mod> mods = new ArrayList<>();
        JSONObject root = new JSONObject(configLoad());

        for(Object entry : root.getJSONArray("Mods")) {
            Mod mod = new Gson().fromJson(entry.toString(), Mod.class);
            mods.add(mod);
        }
        return mods;
    }

    public static void reloadMods() throws ConnectionPendingException {
        if(customRetries > 4) {
            throw new ConnectionPendingException();
        }
        modsCache.clear();
        OkHttpClient client = new OkHttpClient();
        client.setRetryOnConnectionFailure(true);
        Request request = new Request.Builder()
                .url(respositoryURL + "/" + modlist)
                .build();
        try {
            for (Object entry : new JSONObject(client.newCall(request).execute().body().string()).getJSONArray("Mods")) {
                modsCache.add(new Gson().fromJson(entry.toString(), Mod.class));
            }
            customRetries = 0;
        } catch (ConnectException e) {
            customRetries++;
            reloadMods();
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Failed to fetch mods");
        }
    }

    public static int getAmountOfModsInCategory(String categoryName) throws IOException {
        int count = 0;

        for(Mod mod : getAllMods()) {
            if(mod.Category.equals(categoryName)) {
                count++;
            }
        }

        return count;
    }

    public static ArrayList<Mod> getModsInCategory(int offset, int limit, String categoryName) throws IOException {
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

    public static ArrayList<Mod> getMods(int offset, int limit) throws IOException {
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
}
