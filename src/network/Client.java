package network;

import registry.Registry;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.util.HashMap;

public class Client {
    private String id;
    private String ip;
    private String hostname;
    private String listName;
    private Registry reg;
    private InetAddress ipAddress;
    private Socket socket;

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

    public Socket getSocket() {
        return socket;
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

    public void setSocket(Socket socket) {
        this.socket = socket;
    }

    public void addRelease(String folder, String path) {
        this.releases.put(path, folder);
    }

    public void setReleases(HashMap<String, String> releases) {
        this.releases = releases;
    }

    public void addTCPListener() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (socket != null) {
                    try {
                        BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                        String line;
                        // Wait for Package
                        try {
                            while((line = reader.readLine()) != null) {
                                System.out.println("REPEATE FROM " + getListName() + ": " + line);
                                // TODO: DESIDE HERE WHAT TO DO!
                            }
                        } catch (SocketException e) {
                            System.out.println(String.format("TCP LISTENER %s STOPED!", getListName()));
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    public void removeRelease(String path) {
        this.releases.remove(path);
    }
}
