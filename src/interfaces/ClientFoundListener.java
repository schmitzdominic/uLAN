package interfaces;

import network.Client;

public interface ClientFoundListener {
    void onClientFound(Client client);

    void onClientRemove(String id);

    void onClientRemoveIp(String ip);
}
