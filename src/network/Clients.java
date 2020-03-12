package network;

import info.Info;
import info.Tool;
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
        this.clientMap = FXCollections.observableHashMap();
        this.clientList = clientList;
        this.controller = controller;
        this.id = Info.getSettings().get("id");
        this.port = Integer.parseInt(Info.getSettings().get("port"));
        // this.clientList.getItems().sort(Comparator.naturalOrder()); TODO SORT!
    }

    public ObservableMap<String, Client> getClientMap() {
        return this.clientMap;
    }

    public Client getClientById(final String id) {
        return this.clientMap.get(id);
    }

    public Client getClientByListName(final String listName) {
        for (final Client c : this.clientMap.values()) {
            if (c.getListName().equals(listName)) {
                return c;
            }
        }
        return null;
    }

    public boolean clientExists(final Client client) {
        if (this.clientMap.get(client.getId()) == null) {
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
                if (!client.getId().equals(Clients.this.id)) {
                    if (!Clients.this.clientList.getItems().contains(client.getListName()) & Clients.this.clientMap.get(client.getId()) == null) {
                        Clients.this.clientList.getItems().add(client.getListName());
                        Clients.this.clientMap.put(client.getId(), client);
                    }
                } else {
                    Clients.this.clientMap.get(client.getId()).refresh(client);
                    Clients.this.clientList.refresh();
                }
            }
        });
    }

    public void removeClientById(final String id) {
        for (final Client client : this.clientMap.values()) {
            if (client.getId().equals(id)) {
                this.removeClient(client);
            }
        }
    }

    public void removeClientByIp(final String ip) {
        for (final Client client : this.clientMap.values()) {
            if (client.getIp().equals(ip)) {
                this.removeClient(client);
            }
        }
    }

    public void removeClient(final Client client) {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                int index = 0;
                for (final String c : Clients.this.clientList.getItems()) {
                    if (c.equals(client.getListName())) {
                        final Client cTemp = Clients.this.clientMap.get(client.getId());
                        if (cTemp.getSocket() != null) {
                            Clients.this.clientMap.get(client.getId()).closeSocket();
                            Clients.this.clientMap.get(client.getId()).getTcpListener().interrupt();
                        }
                        Clients.this.clientMap.remove(client.getId());
                        break;
                    }
                    index++;
                }
                if (Clients.this.clientList.getSelectionModel().isSelected(index)) {
                    if (Clients.this.clientList.getItems().size() > 1) {
                        Clients.this.clientList.getSelectionModel().select(0);
                    } else {
                        Clients.this.clientList.getSelectionModel().clearSelection();
                        Clients.this.controller.makeClientInfoInvisible();
                    }
                }
                if (Clients.this.clientList.getItems().size() > 0) {
                    Clients.this.clientList.getItems().remove(index);
                }
            }
        });
    }

    public boolean existName(final String hostname) {
        for (final Client client : this.clientMap.values()) {
            if (client.getListName().equals(hostname) | client.getHostname().equals(hostname)) {
                return true;
            }
        }
        return false;
    }

    public void changeClient(final String oldName, final Client client) {
        if (this.clientMap.get(client.getId()) != null) {
            this.clientMap.put(client.getId(), client);
            int index = 0;
            for (final String name : this.clientList.getItems()) {
                if (name.equals(oldName)) {
                    this.clientList.getItems().set(index, client.getListName());
                }
                index++;
            }
        }
    }

    public void disconnect() {
        for (final Client client : this.clientMap.values()) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        final Socket socket = Tool.isOnline(InetAddress.getByName(client.getIp()), Clients.this.port);
                        if (socket != null) {
                            Tool.sendMessage(client, Info.getDisconnectPackage());
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
        this.clientList.refresh();
        final int selected = this.clientList.getSelectionModel().getSelectedIndex();
        this.clientList.getSelectionModel().clearSelection();
        this.clientList.getSelectionModel().select(selected);
    }

    @Override
    public void removeClient(final String id) {
        this.removeClientById(id);
    }
}
