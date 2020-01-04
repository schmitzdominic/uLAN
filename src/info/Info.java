package info;

import registry.Registry;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;

public class Info {

    public static HashMap<String, String> getSettings() {
        Registry reg = new Registry();
        return reg.getProperties();
    }

    public static String getIp() {
        InetAddress ip = getIpAddress();
        if(ip != null) {
            return ip.getHostAddress();
        }
        return null;
    }

    public static InetAddress getIpAddress() {
        try {
            return InetAddress.getLocalHost();
        } catch (UnknownHostException e) {
            return null;
        }
    }

    public static String getHostname() {
        InetAddress ip = getIpAddress();
        if(ip != null) {
            return ip.getHostName();
        }
        return null;
    }

    public static HashMap<String, String> getInitializePackage() {
        HashMap<String, String> settings = getSettings();
        Registry registry = new Registry();
        HashMap<String, String> map = new HashMap<>();
        map.put("MODE","INITIALIZE");
        map.put("ID",settings.get("id"));
        map.put("IP",getIp());
        map.put("HOSTNAME",getHostname());

        String releases = registry.getReleasesAsString();
        if (releases != null) {
            if (!releases.isEmpty()) {
                map.put("RELEASES", releases);
            }
        }
        return map;
    }

    public static HashMap<String, String> getRepeatPackage() {
        HashMap<String, String> settings = getSettings();
        Registry registry = new Registry();
        HashMap<String, String> map = new HashMap<>();
        map.put("MODE","REPEAT");
        map.put("ID",settings.get("id"));
        map.put("IP",getIp());
        map.put("HOSTNAME",getHostname());

        String releases = registry.getReleasesAsString();
        if (releases != null) {
            if (!releases.isEmpty()) {
                map.put("RELEASES", releases);
            }
        }
        return map;
    }

    public static HashMap<String, String> getReleasesChangedPackage() {
        HashMap<String, String> settings = getSettings();
        Registry registry = new Registry();
        HashMap<String, String> map = new HashMap<>();
        map.put("MODE","RELEASECHANGE");
        map.put("ID",settings.get("id"));

        String releases = registry.getReleasesAsString();
        if (releases != null) {
            if (!releases.isEmpty()) {
                map.put("RELEASES", releases);
            }
        }
        return map;
    }

    public static HashMap<String ,String> getProvideFolderPackage(String path) {
        HashMap<String, String> settings = getSettings();
        HashMap<String, String> map = new HashMap<>();
        map.put("MODE","PROVIDE");
        map.put("ID",settings.get("id"));
        map.put("PATH", path);
        return map;
    }

    public static HashMap<String ,String> getDownloadFolderPackage(int port, String folderName, long size) {
        HashMap<String, String> settings = getSettings();
        HashMap<String, String> map = new HashMap<>();
        map.put("MODE","DOWNLOAD");
        map.put("ID",settings.get("id"));
        map.put("PORT", port+"");
        map.put("FOLDERNAME", folderName);
        map.put("SIZE", size+"");
        return map;
    }

    public static HashMap<String, String> getDisconnectPackage() {
        HashMap<String, String> settings = getSettings();
        HashMap<String, String> map = new HashMap<>();
        map.put("MODE","DISCONNECT");
        map.put("ID",settings.get("id"));
        return map;
    }
}
