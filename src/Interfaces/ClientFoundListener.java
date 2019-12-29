package Interfaces;

import network.Client;

import java.net.Socket;

public interface ClientFoundListener {
    void onClientFound(Client client);
    void onClientRemove(String id);
    void onClientRemoveIp(String ip);
}
