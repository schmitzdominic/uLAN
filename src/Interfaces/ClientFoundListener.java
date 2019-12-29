package Interfaces;

import network.Client;

import java.net.Socket;

public interface ClientFoundListener {
    void onClientFound(Client client, Socket socket);
    void onClientRemove(Client client, Socket socket);
}
