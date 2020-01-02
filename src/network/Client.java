package network;

import Interfaces.ClientsCallback;
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
    private boolean listener;
    private Registry reg;
    private InetAddress ipAddress;
    private Socket socket;
    private Thread tcpListener;
    private ClientsCallback clientsCallback;

    private HashMap<String, String> releases;

    public Client(String id, InetAddress ip, String hostname) {
        this(id, ip.getHostAddress(), hostname);
        this.ipAddress = ip;
        this.listener = false;
    }

    public Client(String id, String ip, String hostname) {

        this.reg = new Registry();
        this.releases = new HashMap<>();
        this.id = id;
        this.listener = false;

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

    public void setListener(boolean listener) {
        this.listener = listener;
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

    public void setReleases(HashMap<String, String> releases) {
        this.releases = releases;
    }

    public boolean isListener() {
        return listener;
    }

    public void addRelease(String folder, String path) {
        this.releases.put(path, folder);
    }

    public void addTCPListener() {
        this.tcpListener = new Thread(new Runnable() {
            @Override
            public void run() {
                if (socket != null) {
                    try {
                        BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                        String line;
                        // Wait for Package
                        try {
                            listener = true;
                            while((line = reader.readLine()) != null) {
                                System.out.println("REPEATE FROM " + getListName() + ": " + line);
                                // TODO: DECIDE HERE WHAT TO DO!
                            }
                        } catch (SocketException e) {
                            System.out.println(String.format("TCP LISTENER %s STOPED!", getListName()));
                            listener = false;
                            if (clientsCallback != null) {
                                clientsCallback.removeClient(getId());
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        this.tcpListener.start();
    }

    public void removeRelease(String path) {
        this.releases.remove(path);
    }

    public void refresh(Client client) {
        if (client.getListName() != null) {
            this.setListName(client.getListName());
        }
        if (client.getId() != null) {
            this.setId(client.getId());
        }
        if (client.getHostname() != null) {
            this.setHostname(client.getHostname());
        }
        if (client.getIp() != null) {
            this.setIp(client.getIp());
        }
        if (client.getReleases() != null) {
            this.setReleases(client.getReleases());
        }
        if (client.getIpAddress() != null) {
            this.setIpAddress(client.getIpAddress());
        }
        if (client.getSocket() != null) {
            this.setSocket(client.getSocket());

            if (client.isListener()) {
                this.addTCPListener();
            }
        }

        client = null;
    }

    public void registerClientsCallback(ClientsCallback clientsCallback) {
        this.clientsCallback = clientsCallback;
    }
}
