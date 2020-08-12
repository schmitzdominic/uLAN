package registry;

import entities.Release;
import helpers.Info;
import helpers.Tool;
import network.Client;

import java.io.File;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

public class Registry {

    Preferences settings; //Preferences
    final String path = "/ulan/preferences"; // settings.Settings path

    // Default settings
    final private String DEFAULTTRANSFERPATH = System.getProperty("user.home") + "\\downloads";
    final private String DEFAULTICON = "/icons/server.png";
    final private String WINDOWICON = "/icons/server-256.png";
    final private String ID = "";
    final private int DEFAULTMESSAGE = 1; // NOT USED
    final private int PORT = 33123;
    final private int FILEPORT = 50000;
    final private int CLIENTSCOUNT = 253;
    final private int AUTOSTART = 1; // NOT USED
    final private int NOTIFICATIONS = 1; // NOT USED

    // Strings
    final private String clients = "clients";
    final private String releases = "releases";

    /**
     * Default Contructor
     */
    public Registry() {
        settings = Preferences.userRoot().node(path);
    }

    /**
     * Get the Properties
     *
     * @return list of settings HashMap<String, String>
     */
    public HashMap<String, String> getProperties() {

        if (!checkIfPropertiesExist("defaultfiletransferpath")) {
            final File path = new File(DEFAULTTRANSFERPATH);
            if (path.isDirectory()) {
                setSetting("properties", "defaultfiletransferpath", DEFAULTTRANSFERPATH);
            } else {
                setSetting("properties", "defaultfiletransferpath", System.getProperty("user.home"));
            }
        }
        if (!checkIfPropertiesExist("defaulticon")) {
            setSetting("properties", "defaulticon", DEFAULTICON);
        }
        if (!checkIfPropertiesExist("windowicon")) {
            setSetting("properties", "windowicon", WINDOWICON);
        }
        if (!checkIfPropertiesExist("defaultmessage")) {
            setSetting("properties", "defaultmessage", "" + DEFAULTMESSAGE);
        }
        if (!checkIfPropertiesExist("id")) {
            setSetting("properties", "id", getId());
        }
        if (!checkIfPropertiesExist("port")) {
            setSetting("properties", "port", "" + PORT);
        }
        if (!checkIfPropertiesExist("fileport")) {
            setSetting("properties", "fileport", "" + FILEPORT);
        }
        if (!checkIfPropertiesExist("clientscount")) {
            setSetting("properties", "clientscount", "" + CLIENTSCOUNT);
        }
        if (!checkIfPropertiesExist("autostart")) {
            setSetting("properties", "autostart", "" + AUTOSTART);
        }
        if (!checkIfPropertiesExist("notifications")) {
            setSetting("properties", "notifications", "" + NOTIFICATIONS);
        }

        try {
            settings = Preferences.userRoot().node(path + "/properties");

            final HashMap<String, String> x = new HashMap<>();

            for (final String key : settings.keys()) {
                x.put(key, settings.get(key, ""));
            }
            return x;

        } catch (final BackingStoreException e) {
            e.printStackTrace();
            return null;
        }
    }

    private String getId() {

        String tString = "";
        String host = "";
        String ip = "";

        final Date date = new Date();
        final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        tString = sdf.format(date);

        host = Info.getHostname();
        ip = Info.getIp();

        if (host == null) {
            host = "NA";
        }

        if (ip == null) {
            ip = "NA";
        }

        return encryptString(String.format("%s;%s;%s", tString, host, ip));
    }

    public String encryptString(final String input) {
        try {
            // getInstance() method is called with algorithm SHA-512
            final MessageDigest md = MessageDigest.getInstance("SHA-256");

            // digest() method is called
            // to calculate message digest of the input string
            // returned as array of byte
            final byte[] messageDigest = md.digest(input.getBytes());

            // Convert byte array into signum representation
            final BigInteger no = new BigInteger(1, messageDigest);

            // Convert message digest into hex value
            String hashtext = no.toString(16);

            // Add preceding 0s to make it 32 bit
            while (hashtext.length() < 32) {
                hashtext = "0" + hashtext;
            }

            // return the HashText
            return hashtext;
        }

        // For specifying wrong message digest algorithms
        catch (final NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Check if the Software starts the first time
     *
     * @return true - not the first time / false - yes the first time
     */
    public boolean checkIfPropertiesExist(final String setting) {
        try {
            if (setting.equals("root")) {
                return settings.nodeExists(path + "/properties");
            } else {
                settings = Preferences.userRoot().node(path + "/properties");
                for (final String x : settings.keys()) {
                    if (x.equals(setting)) {
                        return true;
                    }
                }
                settings = Preferences.userRoot().node(path);
                return false;
            }

        } catch (final BackingStoreException e) {
            return false;
        }
    }

    /**
     * Get one Setting
     *
     * @param jobName String
     * @param key     String
     * @return one Setting as String
     */
    public String getSetting(final String jobName, String key) {
        try {
            createJob(jobName.toLowerCase());
            key = key.toLowerCase();

            for (final String value : settings.keys()) {
                if (value.equals(key))
                    return settings.get(value, "");
            }
            return null;
        } catch (final BackingStoreException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Set one setting
     *
     * @param jobName String
     * @param set     String
     * @param content String NOTE: If it is a path please use / instead of \
     */
    public void setSetting(String jobName, String set, String content) {
        jobName = jobName.toLowerCase();
        set = set.toLowerCase();
        content = content.toLowerCase();
        createJob(jobName);
        settings.put(set, content);
    }

    /**
     * Create a Job and set this.settings to the job
     *
     * @param name String
     */
    public void createJob(String name) {
        name = name.toLowerCase();
        settings = Preferences.userRoot().node(path + "/" + name);
    }

    public boolean checkIfClientExists(final Client client) {
        try {
            final String clientName = path + "/" + clients + "/" + client.getId();
            return Preferences.userRoot().nodeExists(clientName.toLowerCase());
        } catch (final BackingStoreException e) {
            e.printStackTrace();
        }
        return false;
    }

    public void addClient(final Client client) {
        final String jobName = String.format("%s/%s", clients, client.getId()).toLowerCase();
        if (client.getId() != null) {
            setSetting(jobName, "id", client.getId());
        }

        if (client.getHostname() != null) {
            setSetting(jobName, "hostname", client.getHostname());
        }

        if (client.getListName() != null) {
            setSetting(jobName, "listname", client.getListName());
        }

        if (client.getIp() != null) {
            setSetting(jobName, "ip", client.getIp());
        }
    }

    public boolean addRelease(String path) {
        path = path.toLowerCase();
        final String jobName = String.format("%s", releases).toLowerCase();
        String releases = getSetting(jobName, "releases");
        if (releases == null) {
            setSetting(jobName, "releases", path);
            return true;
        } else {
            if (releases.contains(path)) {
                return false;
            } else {
                releases += String.format(";%s", path);
                setSetting(jobName, "releases", releases);
                return true;
            }
        }
    }

    public boolean removeRelease(String path) {
        path = path.toLowerCase();
        final String jobName = String.format("%s", releases).toLowerCase();
        final String releases = getSetting(jobName, "releases");
        if (releases != null) {
            if (releases.contains(path)) {
                String[] relArray = releases.split(";");
                final List<String> list = new ArrayList<String>(Arrays.asList(relArray));
                list.remove(path);
                relArray = list.toArray(new String[0]);
                setSetting(jobName, "releases", String.join(";", relArray));
                return true;
            }
        }
        return false;
    }

    public String[] getReleases() {
        final String jobName = String.format("%s", releases).toLowerCase();
        String releases = getSetting(jobName, "releases");
        if (releases == null) {
            return null;
        } else {
            if (releases.startsWith(";")) {
                removeRelease("");
                releases = getSetting(jobName, "releases");
            }
            return releases.split(";");
        }
    }

    public List<Release> getReleasesList() {
        return Tool.convertReleasesStringToReleaseList(getReleasesAsString());
    }

    public String getReleaseNormal(final String releaseWithoutSpaces) {
        for (final String p : getReleases()) {
            if (p.replace(" ", "").equals(releaseWithoutSpaces)) {
                return p;
            }
        }
        return null;
    }

    public boolean releaseExists(final String path) {
        for (final String p : getReleases()) {
            if (p.replace(" ", "").equals(path.replace(" ", ""))) {
                return true;
            }
        }
        return false;
    }

    public String getReleasesAsString() {
        final String jobName = String.format("%s", releases).toLowerCase();
        String releases = getSetting(jobName, "releases");
        if (releases == null) {
            return null;
        } else {
            if (releases.startsWith(";")) {
                removeRelease("");
                releases = getSetting(jobName, "releases");
            }
            return releases;
        }
    }

    public void removeClient(final Client client) {
        final String name = client.getId();
        settings = Preferences.userRoot().node(path + "/clients/" + name);
        try {
            settings.removeNode();
        } catch (final BackingStoreException e) {
            e.printStackTrace();
        }

    }

    public void updateClientSettings(final Client client) {
        final String jobName = String.format("%s/%s", clients, client.getId()).toLowerCase();
        client.setHostname(getSetting(jobName, "hostname"));
        client.setListName(getSetting(jobName, "listname"));
        client.setIp(getSetting(jobName, "ip"));
    }
}
