package helpers;

import registry.Registry;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;

public class Info {

    public static HashMap<String, String> getProperties() {
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
}
