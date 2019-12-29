package network;

import info.Info;
import info.Tool;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.concurrent.atomic.AtomicInteger;

public class Finder {

    private int count;
    private int port;
    public AtomicInteger counter;
    public boolean active = false;

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
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        InetAddress ip = InetAddress.getByName(ipAddress + client);
                        Socket client = isOnline(ip, port);

                        if (client != null) {
                            Tool.sendMessage(client, Info.getInitializePackage());
                            try {
                                BufferedReader reader = new BufferedReader(
                                        new InputStreamReader(client.getInputStream()));
                                while(true) {
                                    System.out.println(reader.readLine());
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }

                        if (count == counter.addAndGet(1)) {
                            active = false;
                        }
                    } catch (UnknownHostException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
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
}
