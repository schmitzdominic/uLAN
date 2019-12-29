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
        HashMap<String, String> map = new HashMap<>();
        map.put("MODE","INITIALIZE");
        map.put("ID",settings.get("ID"));
        map.put("IP",getIp());
        map.put("HOSTNAME",getHostname());
        // TODO: HERE RELEASES!
        return map;
    }
}
