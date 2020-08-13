package network;

import entities.Income;
import entities.Payload;
import entities.payload.InitRepeat;
import helpers.Info;
import interfaces.ClientFoundListener;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
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
                                    final PrintWriter out = Payload.INITIALIZE.sendTo(socket);
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

        try {
            final Income income = new Income(message);

            if (clientListener != null) {

                final Client client = buildClient(income.getObject());

                if (client != null) {
                    client.setSocket(socket);
                    client.setOut(out);
                    client.addTCPListener();
                    clientListener.onClientFound(client);
                }
            }
        } catch (final NullPointerException npe) {
            System.out.println("INCOMING REPEAT MESSAGE IS NOT VALID! Message: " + message);
            npe.printStackTrace();
        }
    }

    private Client buildClient(final InitRepeat payload) {
        if (payload.isValid()) {
            final Client client = new Client(payload.getId(), payload.getIp(), payload.getHostName());

            if (payload.hasReleases()) {
                client.setReleases(payload.getReleases());
            } else {
                // Do nothing, because the other client does not have any releases
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
