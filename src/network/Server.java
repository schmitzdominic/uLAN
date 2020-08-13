package network;

import entities.Income;
import entities.payload.Initialize;
import entities.payload.Payload;
import helpers.Info;
import interfaces.ClientFoundListener;
import start.MainController;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;


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
                    final Income income = new Income(in.readLine());
                    if (income.getMode() != null) {
                        switch (income.getMode()) {
                            case INITIALIZE:
                                initialize(income, socket);
                                break;
                            case DISCONNECT:
                                disconnectClient(income);
                                break;
                        }
                    }
                } catch (final IOException e) {
                    System.out.printf("FULL HANDSHAKE WITH %s IS NOT POSSIBLE!%n", socket.getInetAddress().getHostAddress());
                }
            }
        }).start();
    }

    private void initialize(final Income income, final Socket socket) {
        if (clientListener != null) {

            final Initialize payload = income.getObject();

            if (payload.isValid()) {
                final Client client = new Client(payload.getId(), payload.getIp(), payload.getHostName());

                if (payload.hasReleases()) {
                    client.setReleases(payload.getReleases());
                }

                client.setSocket(socket);
                client.addTCPListener();
                Payload.INIT_REPEAT.sendTo(client);
                clientListener.onClientFound(client);
            }
        }
    }

    private void disconnectClient(final Income income) {
        clientListener.onClientRemove(income.getObject().getId());
    }

    public void registerClientFoundListener(final ClientFoundListener clientListener) {
        this.clientListener = clientListener;
    }
}
