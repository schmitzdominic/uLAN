package network;

import registry.Registry;

import java.net.InetAddress;
import java.util.HashMap;

public class Client {
    private String id;
    private String ip;
    private String hostname;
    private String listName;
    private Registry reg;
    private InetAddress ipAddress;

    private HashMap<String, String> releases;

    public Client(String id, InetAddress ip, String hostname) {
        this(id, ip.getHostAddress(), hostname);
        this.ipAddress = ip;
    }

    public Client(String id, String ip, String hostname) {

        this.reg = new Registry();
        this.releases = new HashMap<>();
        this.id = id;

        if(reg.checkIfClientExists(this)){
            reg.updateClientSettings(this);
        } else {
            this.id = id;
            this.ip = ip;
            this.listName = hostname;
            this.hostname = hostname;
            reg.addClient(this);
        }
    }

    public String getId() {
        return this.id;
    }

    public String getHostname() {
        return this.hostname;
    }

    public String getListName() {
        return this.listName;
    }

    public String getIp() {
        return this.ip;
    }

    public InetAddress getIpAddress() {
        return this.ipAddress;
    }

    public HashMap<String, String> getReleases() {
        return this.releases;
    }

    public void setId(String id) {
        this.id = id;
        this.reg.addClient(this);
    }

    public void setIp(String ip) {
        this.ip = ip;
        this.reg.addClient(this);
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
        this.reg.addClient(this);
    }

    public void setListName(String listName) {
        this.listName = listName;
        this.reg.addClient(this);
    }

    public void setIpAddress(InetAddress ipAddress) {
        this.ipAddress = ipAddress;
    }

    public void addRelease(String folder, String path) {
        this.releases.put(path, folder);
    }

    public void removeRelease(String path) {
        this.releases.remove(path);
    }
}
