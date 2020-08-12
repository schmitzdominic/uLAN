package helpers;

import registry.Registry;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;

public class Info {

    public static HashMap<String, String> getSettings() {
        final Registry reg = new Registry();
        return reg.getProperties();
    }

    public static String getIp() {
        final InetAddress ip = getIpAddress();
        if (ip != null) {
            return ip.getHostAddress();
        }
        return null;
    }

    public static InetAddress getIpAddress() {
        try {
            return InetAddress.getLocalHost();
        } catch (final UnknownHostException e) {
            return null;
        }
    }

    public static String getHostname() {
        final InetAddress ip = getIpAddress();
        if (ip != null) {
            return ip.getHostName();
        }
        return null;
    }

    public static HashMap<String, String> getInitializePackage() {
        final HashMap<String, String> settings = getSettings();
        final Registry registry = new Registry();
        final HashMap<String, String> map = new HashMap<>();
        map.put("MODE", "INITIALIZE");
        map.put("ID", settings.get("id"));
        map.put("IP", getIp());
        map.put("HOSTNAME", getHostname());

        final String releases = registry.getReleasesAsString();
        if (releases != null) {
            if (!releases.isEmpty()) {
                map.put("RELEASES", releases);
            }
        }
        return map;
    }

    public static HashMap<String, String> getRepeatPackage() {
        final HashMap<String, String> settings = getSettings();
        final Registry registry = new Registry();
        final HashMap<String, String> map = new HashMap<>();
        map.put("MODE", "REPEAT");
        map.put("ID", settings.get("id"));
        map.put("IP", getIp());
        map.put("HOSTNAME", getHostname());

        final String releases = registry.getReleasesAsString();
        if (releases != null) {
            if (!releases.isEmpty()) {
                map.put("RELEASES", releases);
            }
        }
        return map;
    }

    public static HashMap<String, String> getReleasesChangedPackage() {
        final HashMap<String, String> settings = getSettings();
        final Registry registry = new Registry();
        final HashMap<String, String> map = new HashMap<>();
        map.put("MODE", "RELEASECHANGE");
        map.put("ID", settings.get("id"));

        final String releases = registry.getReleasesAsString();
        if (releases != null) {
            if (!releases.isEmpty()) {
                map.put("RELEASES", releases);
            }
        }
        return map;
    }

    public static HashMap<String, String> getProvideFolderPackage(final String path) {
        final HashMap<String, String> settings = getSettings();
        final HashMap<String, String> map = new HashMap<>();
        map.put("MODE", "PROVIDE");
        map.put("ID", settings.get("id"));
        map.put("PATH", path);
        return map;
    }

    public static HashMap<String, String> getDownloadFolderPackage(final int port, final String folderName, final long size) {
        final HashMap<String, String> settings = getSettings();
        final HashMap<String, String> map = new HashMap<>();
        map.put("MODE", "DOWNLOAD");
        map.put("ID", settings.get("id"));
        map.put("PORT", port + "");
        map.put("FOLDERNAME", folderName);
        map.put("SIZE", size + "");
        return map;
    }

    public static HashMap<String, String> getDisconnectPackage() {
        final HashMap<String, String> settings = getSettings();
        final HashMap<String, String> map = new HashMap<>();
        map.put("MODE", "DISCONNECT");
        map.put("ID", settings.get("id"));
        return map;
    }
}
