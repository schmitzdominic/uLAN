package network;

import helpers.Info;
import helpers.Tool;
import interfaces.ClientFoundListener;
import start.MainController;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;


public class Server extends Thread {

    private ClientFoundListener clientListener;
    private final MainController mainController;
    private final int port;
    private final String id;
    private final String ip;

    public Server(final MainController mainController, final int port) {
        this.mainController = mainController;
        this.port = port;
        id = Info.getSettings().get("id");
        ip = Info.getIp();
    }

    @Override
    public void run() {
        try {
            final ServerSocket listener = new ServerSocket(port);

            while (true) {
                final Socket socket = listener.accept();
                if (!socket.getInetAddress().getHostAddress().equals(ip)) {
                    checkMessage(socket);
                }
            }
        } catch (final IOException e) {
            System.out.println("Es l\u00e4uft bereits eine Instanz des Programms. Programm wird geschlossen!");
            System.exit(0);
        }
    }

    private void checkMessage(final Socket socket) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    final BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    final Map<String, String> info = Tool.convertMessage(in.readLine());
                    final String mode = info.get("MODE");
                    if (mode != null) {
                        if (mode.equals("INITIALIZE")) {
                            if (!info.get("ID").equals(id)) {
                                initialize(info, socket);
                            }
                        } else if (mode.equals("DISCONNECT")) {
                            disconnectClient(info);
                        }
                    }
                } catch (final IOException e) {
                    System.out.println(String.format("FULL HANDSHAKE WITH %s IS NOT POSSIBLE!", socket.getInetAddress().getHostAddress()));
                }
            }
        }).start();
    }

    private void initialize(final Map<String, String> info, final Socket socket) {
        if (clientListener != null) {

            final String id = info.get("ID");
            final String ip = info.get("IP");
            final String hostname = info.get("HOSTNAME");

            if (id != null & ip != null & hostname != null) {
                final Client client = new Client(id, ip, hostname);

                if (info.get("RELEASES") != null) {
                    client.setReleases(Tool.convertReleasesString(info.get("RELEASES")));
                }

                client.setSocket(socket);
                client.addTCPListener();
                Tool.sendMessage(client, Info.getRepeatPackage());
                clientListener.onClientFound(client);
            }
        }
    }

    private void disconnectClient(final Map<String, String> info) {
        clientListener.onClientRemove(info.get("ID"));
    }

    public void registerClientFoundListener(final ClientFoundListener clientListener) {
        this.clientListener = clientListener;
    }
}
