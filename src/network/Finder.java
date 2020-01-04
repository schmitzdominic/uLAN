package network;

import Interfaces.ClientFoundListener;
import info.Info;
import info.Tool;

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
        this.counter = new AtomicInteger();
    }

    public void setCount(int count) {
        this.count = count;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public void searchClients() {
        this.ownIp = Info.getIp();
        this.active = true;
        this.counter.set(0);
        String ipAddress = this.getNetworkIP(Info.getIp());
        if (ipAddress == null) {
            counter.set(count);
            active = false;
            return;
        }
        for(int i = 2; i < count+2; i++) {
            final int client = i;
            Thread search = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        // Build a InetAddress and check if the ip is not ours
                        InetAddress ip = InetAddress.getByName(ipAddress + client);
                        if (!(ownIp).equals(ip.getHostAddress())) {

                            // Check if the client is online
                            Socket socket = isOnline(ip, port);

                            if (socket != null) {
                                // If the Client is available
                                try {
                                    // Send a initialize Package
                                    PrintWriter out = Tool.sendMessage(socket, Info.getInitializePackage());
                                    BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                                    String line;
                                    // Wait for the REPEAT Package
                                    try {
                                        while((line = reader.readLine()) != null) {
                                            repeat(line, socket, out);
                                            break;
                                        }
                                    } catch (SocketException e) {
                                        String removeClient = socket.getInetAddress().getHostAddress();
                                        System.out.println(String.format("Client %s reset", removeClient));
                                        clientListener.onClientRemoveIp(removeClient);
                                    }
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }

                            if (count == counter.addAndGet(1)) {
                                active = false;
                            }
                        }
                    } catch (UnknownHostException e) {
                        e.printStackTrace();
                    }
                }
            });

            // Interrupts the search after 12 seconds
            Thread interrupter = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Thread.sleep(12000);
                        if (count == counter.addAndGet(1)) {
                            active = false;
                        }
                        search.interrupt();
                    } catch (InterruptedException e) {
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
     * @param ip InetAdress
     * @param port int
     * @return true - Online / false - Offline
     */
    private Socket isOnline(InetAddress ip, int port){
        try{
            return new Socket(ip, port);
        } catch (IOException x){
            return null;
        }
    }

    private String getNetworkIP(String ip) {
        if (ip == null) {
            return null;
        }
        String[] ipArray = ip.split("\\.");
        if (ipArray.length == 4) {
            return String.format("%s.%s.%s.", ipArray[0], ipArray[1], ipArray[2]);
        } else {
            return null;
        }
    }

    private void repeat(String message, Socket socket, PrintWriter out) {

        if (this.clientListener != null) {

            Client client = this.buildClient(message);

            if (client != null) {
                client.setSocket(socket);
                client.setOut(out);
                client.addTCPListener();
                clientListener.onClientFound(client);
            }
        }
    }

    private Client buildClient(String message) {

        Map<String, String> info = Tool.convertMessage(message);

        String id = info.get("ID");
        String ip = info.get("IP");
        String hostname = info.get("HOSTNAME");

        if (id != null & ip != null & hostname != null) {
            Client client = new Client(id, ip, hostname);

            if (info.get("RELEASES") != null) {
                client.setReleases(Tool.convertReleasesString(info.get("RELEASES")));
            }

            return client;
        } else {
            return null;
        }
    }

    public void registerClientFoundListener(ClientFoundListener clientListener) {
        this.clientListener = clientListener;
    }
}
