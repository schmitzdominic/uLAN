package network;

import helpers.Tool;
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
        ipAddress = ip;
        listener = false;
    }

    public Client(final String id, final String ip, final String hostname) {

        reg = new Registry();
        releases = new HashMap<>();
        this.id = id;
        listener = false;

        if (reg.checkIfClientExists(this)) {
            reg.updateClientSettings(this);
        } else {
            this.id = id;
            this.ip = ip;
            listName = hostname;
            this.hostname = hostname;
            reg.addClient(this);
        }
    }

    public String getId() {
        return id;
    }

    public String getHostname() {
        return hostname;
    }

    public String getListName() {
        return listName;
    }

    public String getIp() {
        return ip;
    }

    public InetAddress getIpAddress() {
        return ipAddress;
    }

    public HashMap<String, String> getReleases() {
        return releases;
    }

    public Socket getSocket() {
        return socket;
    }

    public Thread getTcpListener() {
        return tcpListener;
    }

    public PrintWriter getOut() {
        return out;
    }

    public void setListener(final boolean listener) {
        this.listener = listener;
    }

    public void setId(final String id) {
        this.id = id;
        reg.addClient(this);
    }

    public void setIp(final String ip) {
        this.ip = ip;
        reg.addClient(this);
    }

    public void setHostname(final String hostname) {
        this.hostname = hostname;
        reg.addClient(this);
    }

    public void setListName(final String listName) {
        this.listName = listName;
        reg.addClient(this);
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
        return listener;
    }

    public void addRelease(final String folder, final String path) {
        releases.put(path, folder);
    }

    public void addTCPListener() {
        tcpListener = new Thread(new Runnable() {
            @Override
            public void run() {
                if (socket != null) {
                    try {
                        final BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                        if (out == null) {
                            out = new PrintWriter(socket.getOutputStream());
                        }
                        String line;
                        // Wait for Package
                        try {
                            listener = true;
                            while ((line = reader.readLine()) != null) {
                                final Map<String, String> info = Tool.convertMessage(line);
                                final String mode = info.get("MODE");
                                if (mode != null) {
                                    if (mode.equals("RELEASECHANGE")) {
                                        final String releases = info.get("RELEASES");
                                        if (releases == null) {
                                            setReleases(new HashMap<>());
                                        } else {
                                            setReleases(Tool.convertReleasesString(info.get("RELEASES")));
                                        }
                                        clientsCallback.notifyClientHasChanged();
                                    } else if (mode.equals("PROVIDE")) {
                                        if (reg.releaseExists(info.get("PATH"))) {
                                            final String release = reg.getReleaseNormal(info.get("PATH"));
                                            if (release != null) {
                                                final File path = new File(release);
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
                                        if (Tool.downloads != null) {
                                            for (final Download x : Tool.downloads) {
                                                System.out.println(x.getId() + " " + x.getPath());
                                            }
                                        }
                                        if (!Tool.downloadExist(info.get("ID"), info.get("FOLDERNAME"))) {
                                            Tool.addDownload(info.get("ID"), info.get("FOLDERNAME"));
                                            final String dPath = reg.getProperties().get("defaultfiletransferpath");
                                            if (dPath != null) {
                                                final File path = new File(dPath);
                                                if (path.isDirectory()) {
                                                    Tool.openFileTransferWindow(MainController.init,
                                                            path, getIp(), info);
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
                            System.out.println(String.format("TCP LISTENER %s STOPED!", getListName()));
                            listener = false;
                            if (clientsCallback != null) {
                                clientsCallback.removeClient(getId());
                            }
                        }
                    } catch (final IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        tcpListener.start();
    }

    public void removeRelease(final String path) {
        releases.remove(path);
    }

    public void closeSocket() {
        try {
            socket.close();
        } catch (final IOException e) {
            e.printStackTrace();
        }
    }

    public void refresh(Client client) {
        if (client.getListName() != null) {
            setListName(client.getListName());
        }
        if (client.getId() != null) {
            setId(client.getId());
        }
        if (client.getHostname() != null) {
            setHostname(client.getHostname());
        }
        if (client.getIp() != null) {
            setIp(client.getIp());
        }
        if (client.getReleases() != null) {
            setReleases(client.getReleases());
        }
        if (client.getIpAddress() != null) {
            setIpAddress(client.getIpAddress());
        }
        if (client.getSocket() != null) {
            setSocket(client.getSocket());

            if (client.isListener()) {
                addTCPListener();
            }
        }

        client = null;
    }

    public void registerClientsCallback(final ClientsCallback clientsCallback) {
        this.clientsCallback = clientsCallback;
    }
}
