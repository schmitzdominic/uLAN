package network;

import Interfaces.ClientFoundListener;
import info.Info;
import info.Tool;
import start.MainController;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;


public class Server extends Thread {

    private ClientFoundListener clientListener;
    private MainController mainController;
    private int port;
    private String id;
    private String ip;

    public Server(MainController mainController, int port){
        this.mainController = mainController;
        this.port = port;
        this.id = Info.getSettings().get("id");
        this.ip = Info.getIp();
    }

    public void run(){
        try {
            ServerSocket listener = new ServerSocket(this.port);

            while (true) {
                Socket socket = listener.accept();
                if (!socket.getInetAddress().getHostAddress().equals(this.ip)) {
                    this.checkMessage(socket);
                }
            }
        } catch (IOException e) {
            System.out.println("Es l\u00e4uft bereits eine Instanz des Programms. Programm wird geschlossen!");
            System.exit(0);
        }
    }

    private void checkMessage(Socket socket) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    Map<String, String> info = Tool.convertMessage(in.readLine());
                    String mode = info.get("MODE");
                    if (mode != null) {
                        if (mode.equals("INITIALIZE")) {
                            if (!info.get("ID").equals(id)) {
                                initialize(info, socket);
                            }
                        } else if (mode.equals("DISCONNECT")) {
                            disconnectClient(info);
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void initialize(Map<String, String> info, Socket socket) {
        if (this.clientListener != null) {

            String id = info.get("ID");
            String ip = info.get("IP");
            String hostname = info.get("HOSTNAME");

            if (id != null & ip != null & hostname != null) {
                Client client = new Client(id, ip, hostname);

                if (info.get("RELEASES") != null) {
                    client.setReleases(Tool.convertReleasesString(info.get("RELEASES")));
                }

                client.setSocket(socket);
                client.addTCPListener();
                Tool.sendMessage(socket, Info.getRepeatPackage());
                clientListener.onClientFound(client);
            }
        }
    }

    private void disconnectClient(Map<String, String> info) {
        clientListener.onClientRemove(info.get("ID"));
    }

    public void registerClientFoundListener(ClientFoundListener clientListener) {
        this.clientListener = clientListener;
    }
}
