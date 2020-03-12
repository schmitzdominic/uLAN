package network;

import info.Tool;
import interfaces.ClientsCallback;
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
    private final Registry reg;
    private InetAddress ipAddress;
    private Socket socket;
    private Thread tcpListener;
    private ClientsCallback clientsCallback;
    private PrintWriter out;
    private HashMap<String, String> releases;

    public Client(final String id, final InetAddress ip, final String hostname) {
        this(id, ip.getHostAddress(), hostname);
        this.ipAddress = ip;
        this.listener = false;
    }

    public Client(final String id, final String ip, final String hostname) {

        this.reg = new Registry();
        this.releases = new HashMap<>();
        this.id = id;
        this.listener = false;

        if (this.reg.checkIfClientExists(this)) {
            this.reg.updateClientSettings(this);
        } else {
            this.id = id;
            this.ip = ip;
            this.listName = hostname;
            this.hostname = hostname;
            this.reg.addClient(this);
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

    public void setListener(final boolean listener) {
        this.listener = listener;
    }

    public void setId(final String id) {
        this.id = id;
        this.reg.addClient(this);
    }

    public void setIp(final String ip) {
        this.ip = ip;
        this.reg.addClient(this);
    }

    public void setHostname(final String hostname) {
        this.hostname = hostname;
        this.reg.addClient(this);
    }

    public void setListName(final String listName) {
        this.listName = listName;
        this.reg.addClient(this);
    }

    public void setIpAddress(final InetAddress ipAddress) {
        this.ipAddress = ipAddress;
    }

    public void setSocket(final Socket socket) {
        this.socket = socket;
    }

    public void setReleases(final HashMap<String, String> releases) {
        this.releases = releases;
    }

    public void setOut(final PrintWriter out) {
        this.out = out;
    }

    public boolean isListener() {
        return this.listener;
    }

    public void addRelease(final String folder, final String path) {
        this.releases.put(path, folder);
    }

    public void addTCPListener() {
        this.tcpListener = new Thread(new Runnable() {
            @Override
            public void run() {
                if (Client.this.socket != null) {
                    try {
                        final BufferedReader reader = new BufferedReader(new InputStreamReader(Client.this.socket.getInputStream()));
                        if (Client.this.out == null) {
                            Client.this.out = new PrintWriter(Client.this.socket.getOutputStream());
                        }
                        String line;
                        // Wait for Package
                        try {
                            Client.this.listener = true;
                            while ((line = reader.readLine()) != null) {
                                final Map<String, String> info = Tool.convertMessage(line);
                                final String mode = info.get("MODE");
                                if (mode != null) {
                                    if (mode.equals("RELEASECHANGE")) {
                                        final String releases = info.get("RELEASES");
                                        if (releases == null) {
                                            Client.this.setReleases(new HashMap<>());
                                        } else {
                                            Client.this.setReleases(Tool.convertReleasesString(info.get("RELEASES")));
                                        }
                                        Client.this.clientsCallback.notifyClientHasChanged();
                                    } else if (mode.equals("PROVIDE")) {
                                        if (Client.this.reg.releaseExists(info.get("PATH"))) {
                                            final String release = Client.this.reg.getReleaseNormal(info.get("PATH"));
                                            if (release != null) {
                                                final File path = new File(release);
                                                if (path.isDirectory()) {
                                                    Tool.provideFolderToClient(Client.this.socket, path);
                                                } else {
                                                    // TODO: ERROR, NO PATH FOUND!
                                                }
                                            }
                                        } else {
                                            // TODO: ERROR, NO RELEASE FOUND!
                                            // Maybe we should make an extra window with security issues
                                        }
                                    } else if (mode.equals("DOWNLOAD")) {
                                        if (Tool.downloads != null) {
                                            for (final Download x : Tool.downloads) {
                                                System.out.println(x.getId() + " " + x.getPath());
                                            }
                                        }
                                        if (!Tool.downloadExist(info.get("ID"), info.get("FOLDERNAME"))) {
                                            Tool.addDownload(info.get("ID"), info.get("FOLDERNAME"));
                                            final String dPath = Client.this.reg.getProperties().get("defaultfiletransferpath");
                                            if (dPath != null) {
                                                final File path = new File(dPath);
                                                if (path.isDirectory()) {
                                                    Tool.openFileTransferWindow(MainController.init,
                                                            path, Client.this.getIp(), info);
                                                } else {
                                                    // TODO: ERROR, NO PATH FOUND!
                                                }
                                            } else {
                                                // TODO: ERROR, NO DOWNLOAD PATH FOUND!
                                            }
                                        }
                                    }
                                }
                            }
                        } catch (final SocketException e) {
                            System.out.println(String.format("TCP LISTENER %s STOPED!", Client.this.getListName()));
                            Client.this.listener = false;
                            if (Client.this.clientsCallback != null) {
                                Client.this.clientsCallback.removeClient(Client.this.getId());
                            }
                        }
                    } catch (final IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        this.tcpListener.start();
    }

    public void removeRelease(final String path) {
        this.releases.remove(path);
    }

    public void closeSocket() {
        try {
            this.socket.close();
        } catch (final IOException e) {
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

    public void registerClientsCallback(final ClientsCallback clientsCallback) {
        this.clientsCallback = clientsCallback;
    }
}
