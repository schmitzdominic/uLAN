package network;

import Interfaces.ClientsCallback;
import info.Tool;
import registry.Registry;
import start.MainController;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.util.HashMap;
import java.util.Map;

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
    private PrintWriter out;
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
        return this.socket;
    }

    public Thread getTcpListener() {
        return this.tcpListener;
    }

    public PrintWriter getOut() {
        return this.out;
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

    public void setOut(PrintWriter out) {
        this.out = out;
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
                        if (out == null) {
                            out = new PrintWriter(socket.getOutputStream());
                        }
                        String line;
                        // Wait for Package
                        try {
                            listener = true;
                            while((line = reader.readLine()) != null) {
                                Map<String, String> info = Tool.convertMessage(line);
                                String mode = info.get("MODE");
                                if (mode != null) {
                                    if (mode.equals("RELEASECHANGE")) {
                                        String releases = info.get("RELEASES");
                                        if (releases == null) {
                                            setReleases(new HashMap<>());
                                        } else {
                                            setReleases(Tool.convertReleasesString(info.get("RELEASES")));
                                        }
                                        clientsCallback.notifyClientHasChanged();
                                    } else if (mode.equals("PROVIDE")) {
                                        if (reg.releaseExists(info.get("PATH"))) {
                                            String release = reg.getReleaseNormal(info.get("PATH"));
                                            if (release != null) {
                                                File path = new File(release);
                                                if (path.isDirectory()) {
                                                    Tool.provideFolderToClient(socket, path);
                                                } else {
                                                    // TODO: ERROR, NO PATH FOUND!
                                                }
                                            }
                                        } else {
                                            // TODO: ERROR, NO RELEASE FOUND!
                                            // Maybe we should make an extra window with security issues
                                        }
                                    } else if (mode.equals("DOWNLOAD")) {
                                        try {
                                            String dPath = reg.getProperties().get("defaultfiletransferpath");
                                            int port = Integer.parseInt(info.get("PORT"));
                                            long size = Long.parseLong(info.get("SIZE"));
                                            if (dPath != null) {
                                                File path = new File(dPath);
                                                if (path.isDirectory()) {
                                                    Tool.openFileTransferWindow(MainController.init, path, getIp(), port, size);
                                                } else {
                                                    // TODO: ERROR, NO PATH FOUND!
                                                }
                                            } else {
                                                // TODO: ERROR, NO DOWNLOAD PATH FOUND!
                                            }
                                        } catch (NumberFormatException e) {
                                            System.out.println("PORT OR SIZE IS NOT A INTEGER OR LONG NUMBER! PORT:" + info.get("PORT") + " SIZE:" + info.get("SIZE"));
                                            // TODO: ERROR, PORT IS NOT A INTEGER NUMBER!
                                        }
                                    }
                                }
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

    public void closeSocket() {
        try {
            this.socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
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
