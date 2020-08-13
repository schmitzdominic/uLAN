package network;

import entities.Payload;
import helpers.Info;
import helpers.Tool;
import interfaces.ClientList;
import interfaces.ClientsCallback;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableMap;
import javafx.scene.control.ListView;

import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

public class Clients implements ClientsCallback {

    private final ObservableMap<String, Client> clientMap;
    private final ListView<String> clientList;
    private final ClientList controller;
    private final String id;
    private final int port;

    /**
     * Constructor
     *
     * @param clientList ListView<String> to view clients
     */
    public Clients(final ListView<String> clientList, final ClientList controller) {
        clientMap = FXCollections.observableHashMap();
        this.clientList = clientList;
        this.controller = controller;
        id = Info.getSettings().get("id");
        port = Integer.parseInt(Info.getSettings().get("port"));
        // this.clientList.getItems().sort(Comparator.naturalOrder()); TODO SORT!
    }

    public ObservableMap<String, Client> getClientMap() {
        return clientMap;
    }

    public Client getClientById(final String id) {
        return clientMap.get(id);
    }

    public Client getClientByListName(final String listName) {
        for (final Client c : clientMap.values()) {
            if (c.getListName().equals(listName)) {
                return c;
            }
        }
        return null;
    }

    public boolean clientExists(final Client client) {
        if (clientMap.get(client.getId()) == null) {
            return false;
        } else {
            return true;
        }
    }

    public void addClient(final Client client) {
        if (client != null) {
            client.registerClientsCallback(this);
        }
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                if (!client.getId().equals(id)) {
                    if (!clientList.getItems().contains(client.getListName()) & clientMap.get(client.getId()) == null) {
                        clientList.getItems().add(client.getListName());
                        clientMap.put(client.getId(), client);
                    }
                } else {
                    clientMap.get(client.getId()).refresh(client);
                    clientList.refresh();
                }
            }
        });
    }

    public void removeClientById(final String id) {
        for (final Client client : clientMap.values()) {
            if (client.getId().equals(id)) {
                removeClient(client);
            }
        }
    }

    public void removeClientByIp(final String ip) {
        for (final Client client : clientMap.values()) {
            if (client.getIp().equals(ip)) {
                removeClient(client);
            }
        }
    }

    public void removeClient(final Client client) {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                int index = 0;
                for (final String c : clientList.getItems()) {
                    if (c.equals(client.getListName())) {
                        final Client cTemp = clientMap.get(client.getId());
                        if (cTemp.getSocket() != null) {
                            clientMap.get(client.getId()).closeSocket();
                            clientMap.get(client.getId()).getTcpListener().interrupt();
                        }
                        clientMap.remove(client.getId());
                        break;
                    }
                    index++;
                }
                if (clientList.getSelectionModel().isSelected(index)) {
                    if (clientList.getItems().size() > 1) {
                        clientList.getSelectionModel().select(0);
                    } else {
                        clientList.getSelectionModel().clearSelection();
                        controller.makeClientInfoInvisible();
                    }
                }
                if (clientList.getItems().size() > 0) {
                    clientList.getItems().remove(index);
                }
            }
        });
    }

    public boolean existName(final String hostname) {
        for (final Client client : clientMap.values()) {
            if (client.getListName().equals(hostname) | client.getHostname().equals(hostname)) {
                return true;
            }
        }
        return false;
    }

    public void changeClient(final String oldName, final Client client) {
        if (clientMap.get(client.getId()) != null) {
            clientMap.put(client.getId(), client);
            int index = 0;
            for (final String name : clientList.getItems()) {
                if (name.equals(oldName)) {
                    clientList.getItems().set(index, client.getListName());
                }
                index++;
            }
        }
    }

    public void disconnect() {
        for (final Client client : clientMap.values()) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        final Socket socket = Tool.isOnline(InetAddress.getByName(client.getIp()), port);
                        if (socket != null) {
                            Payload.DISCONNECT.sendTo(client);
                        }
                    } catch (final UnknownHostException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }
    }

    @Override
    public void notifyClientHasChanged() {
        clientList.refresh();
        final int selected = clientList.getSelectionModel().getSelectedIndex();
        clientList.getSelectionModel().clearSelection();
        clientList.getSelectionModel().select(selected);
    }

    @Override
    public void removeClient(final String id) {
        removeClientById(id);
    }
}
