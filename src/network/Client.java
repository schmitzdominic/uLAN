package network;

import entities.Income;
import entities.Release;
import entities.payload.DownloadData;
import entities.payload.ProvideData;
import entities.payload.ReleaseChange;
import helpers.Tool;
import interfaces.ClientsCallback;
import registry.Registry;
import start.MainController;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;

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
    private List<Release> releases;

    public Client(final String id, final InetAddress ip, final String hostname) {
        this(id, ip.getHostAddress(), hostname);
        ipAddress = ip;
        listener = false;
    }

    public Client(final String id, final String ip, final String hostname) {

        reg = new Registry();
        releases = new ArrayList<>();
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

    public List<Release> getReleases() {
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

    public void setReleases(final List<Release> releases) {
        this.releases = releases;
    }

    public void setOut(final PrintWriter out) {
        this.out = out;
    }

    public boolean isListener() {
        return listener;
    }

    public void addRelease(final String folder, final String path) {
        releases.add(new Release(folder, path));
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
                                final Income income = new Income(line);
                                switch (income.getMode()) {
                                    case RELEASE_CHANGE:
                                        releaseChange(income.getObject());
                                        break;
                                    case PROVIDE:
                                        provideData(income.getObject());
                                        break;
                                    case DOWNLOAD_FOLDER:
                                        downloadFolder(income.getObject());
                                        break;
                                    default:
                                        // TODO: Throw an exception that the income is not valid!
                                }
                            }
                        } catch (final SocketException e) {
                            System.out.printf("TCP LISTENER %s STOPPED! %n", getListName());
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

    private void releaseChange(final ReleaseChange releaseChange) {
        if (releaseChange.hasReleases()) {
            setReleases(releaseChange.getReleases());
        } else {
            setReleases(new ArrayList<>());
        }
        clientsCallback.notifyClientHasChanged();
    }

    private void provideData(final ProvideData provideData) {
        if (reg.releaseExists(provideData.getPath())) {
            final String release = reg.getReleaseNormal(provideData.getPath());
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
    }

    private void downloadFolder(final DownloadData downloadData) {
        final String clientId = downloadData.getId();
        if (Tool.downloads != null) {
            for (final Download x : Tool.downloads) {
                System.out.println(x.getId() + " " + x.getPath());
            }
        }
        if (!Tool.downloadExist(downloadData.getId(), downloadData.getFolderName())) {
            Tool.addDownload(downloadData.getId(), downloadData.getFolderName());
            final String dPath = reg.getProperties().get("defaultfiletransferpath");
            if (dPath != null) {
                final File path = new File(dPath);
                if (path.isDirectory()) {
                    Tool.openFileTransferWindow(MainController.init,
                            path, getIp(), downloadData);
                } else {
                    // TODO: ERROR, NO PATH FOUND!
                }
            } else {
                // TODO: ERROR, NO DOWNLOAD PATH FOUND!
            }
        }
    }

    public void removeRelease(final String path) {
        releases.removeIf(r -> r.getPath().equals(path));
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
