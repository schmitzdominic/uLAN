package network;

import entities.Payload;
import helpers.Info;
import helpers.Tool;
import interfaces.ClientFoundListener;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class Finder {

    private int count;
    private int port;
    public AtomicInteger counter;
    public boolean active = false;
    private String ownIp;
    private ClientFoundListener clientListener;

    public Finder() {
        counter = new AtomicInteger();
    }

    public void setCount(final int count) {
        this.count = count;
    }

    public void setPort(final int port) {
        this.port = port;
    }

    public void searchClients() {
        ownIp = Info.getIp();
        active = true;
        counter.set(0);
        final String ipAddress = getNetworkIP(Info.getIp());
        if (ipAddress == null) {
            counter.set(count);
            active = false;
            return;
        }
        for (int i = 2; i < count + 2; i++) {
            final int client = i;
            final Thread search = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        // Build a InetAddress and check if the ip is not ours
                        final InetAddress ip = InetAddress.getByName(ipAddress + client);
                        if (!(ownIp).equals(ip.getHostAddress())) {

                            // Check if the client is online
                            final Socket socket = isOnline(ip, port);

                            if (socket != null) {
                                // If the Client is available
                                try {
                                    // Send a initialize Package
                                    final PrintWriter out = Payload.INITIALIZE.send(socket);
                                    final BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                                    final String line;
                                    // Wait for the REPEAT Package
                                    try {
                                        if ((line = reader.readLine()) != null) {
                                            repeat(line, socket, out);
                                        }
                                    } catch (final SocketException e) {
                                        final String removeClient = socket.getInetAddress().getHostAddress();
                                        System.out.println(String.format("Client %s reset", removeClient));
                                        clientListener.onClientRemoveIp(removeClient);
                                    }
                                } catch (final IOException e) {
                                    e.printStackTrace();
                                }
                            }

                            if (count == counter.addAndGet(1)) {
                                active = false;
                            }
                        }
                    } catch (final UnknownHostException e) {
                        e.printStackTrace();
                    }
                }
            });

            // Interrupts the search after 12 seconds
            final Thread interrupter = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Thread.sleep(12000);
                        if (count == counter.addAndGet(1)) {
                            active = false;
                        }
                        search.interrupt();
                    } catch (final InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            });

            search.start();
            interrupter.start();
        }
    }

    /**
     * Check if the Client is online or even not!
     *
     * @param ip   InetAddress
     * @param port int
     * @return true - Online / false - Offline
     */
    private Socket isOnline(final InetAddress ip, final int port) {
        try {
            return new Socket(ip, port);
        } catch (final IOException x) {
            return null;
        }
    }

    private String getNetworkIP(final String ip) {
        if (ip == null) {
            return null;
        }
        final String[] ipArray = ip.split("\\.");
        if (ipArray.length == 4) {
            return String.format("%s.%s.%s.", ipArray[0], ipArray[1], ipArray[2]);
        } else {
            return null;
        }
    }

    private void repeat(final String message, final Socket socket, final PrintWriter out) {

        if (clientListener != null) {

            final Client client = buildClient(message);

            if (client != null) {
                client.setSocket(socket);
                client.setOut(out);
                client.addTCPListener();
                clientListener.onClientFound(client);
            }
        }
    }

    private Client buildClient(final String message) {

        final Map<String, String> info = Tool.convertMessage(message);

        final String id = info.get("ID");
        final String ip = info.get("IP");
        final String hostname = info.get("HOSTNAME");

        if (id != null & ip != null & hostname != null) {
            final Client client = new Client(id, ip, hostname);

            if (info.get("RELEASES") != null) {
                client.setReleases(Tool.convertReleasesString(info.get("RELEASES")));
            }

            return client;
        } else {
            return null;
        }
    }

    public void registerClientFoundListener(final ClientFoundListener clientListener) {
        this.clientListener = clientListener;
    }
}
