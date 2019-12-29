package network;

import Interfaces.ClientFoundListener;
import info.Info;
import info.Tool;
import start.MainController;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
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
                System.out.println("WAITING...");
                Socket socket = listener.accept();
                System.out.println("CLIENT! " + socket.getInetAddress().getHostAddress());
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
                    String map = in.readLine();
                    map = map.replaceAll("[{} ]","");
                    Map<String, String> info = Tool.convertWithStream(map);
                    String mode = info.get("MODE");
                    if (mode != null) {
                        if (mode.equals("INITIALIZE")) {
                            if (!info.get("ID").equals(id)) {
                                initialize(info, socket);
                            }
                        } else if (mode.equals("REPEAT")) {
                            repeat(info);
                        } else if (mode.equals("DISCONNECT")) {

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
                HashMap<String, String> test = Info.getRepeatPackage();
                Tool.sendMessage(socket, test);
                clientListener.onClientFound(client);
            }
        }
    }

    private void repeat(Map<String, String> info) {
        if (this.clientListener != null) {

            String id = info.get("ID");
            String ip = info.get("IP");
            String hostname = info.get("HOSTNAME");

            if (id != null & ip != null & hostname != null) {
                Client client = new Client(id, ip, hostname);
                clientListener.onClientFound(client);
            }
        }
    }

    public void registerClientFoundListener(ClientFoundListener clientListener) {
        this.clientListener = clientListener;
    }
}
