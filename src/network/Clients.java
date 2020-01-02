package network;

import info.Info;
import info.Tool;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableMap;
import javafx.scene.control.ListView;

import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

public class Clients {

    private ObservableMap<String, Client> clientMap;
    private ListView<String> clientList;
    private String id;
    private int port;

    /**
     * Constructor
     * @param clientList ListView<String> to view clients
     */
    public Clients(ListView<String> clientList){
        this.clientMap = FXCollections.observableHashMap();
        this.clientList = clientList;
        this.id = Info.getSettings().get("id");
        this.port = Integer.parseInt(Info.getSettings().get("port"));
        // this.clientList.getItems().sort(Comparator.naturalOrder()); TODO SORT!
    }

    public Client getClientById(String id){
        return this.clientMap.get(id);
    }

    public Client getClientByListName(String listName){
        for(Client c : this.clientMap.values()){
            if(c.getListName().equals(listName)){
                return c;
            }
        }
        return null;
    }

    public boolean clientExists(Client client) {
        if (clientMap.get(client.getId()) == null) {
            return false;
        } else {
            return true;
        }
    }

    public void addClient(Client client) {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                if (!client.getId().equals(id)) {
                    if (!clientList.getItems().contains(client.getListName()) & clientMap.get(client.getId()) == null){
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

    public void removeClientById(String id) {
        for(Client client : clientMap.values()) {
            if (client.getId().equals(id)) {
                this.removeClient(client);
            }
        }
    }

    public void removeClientByIp(String ip) {
        for(Client client : clientMap.values()) {
            if (client.getIp().equals(ip)) {
                this.removeClient(client);
            }
        }
    }

    public void removeClient(Client client) {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                int index = 0;
                for(String c : clientList.getItems()) {
                    if (c.equals(client.getListName())) {
                        clientMap.remove(client.getId());
                        break;
                    }
                    index++;
                }
                clientList.getItems().remove(index);
            }
        });
    }

    public boolean existName(String hostname){
        for(Client client : this.clientMap.values()){
            if(client.getListName().equals(hostname) | client.getHostname().equals(hostname)){
                return true;
            }
        }
        return false;
    }

    public void changeClient(String oldName, Client client){
        if(this.clientMap.get(client.getId()) != null){
            this.clientMap.put(client.getId(), client);
            int index = 0;
            for(String name : this.clientList.getItems()){
                if(name.equals(oldName)){
                    this.clientList.getItems().set(index, client.getListName());
                }
                index++;
            }
        }
    }

    public void disconnect() {
        for(Client client : this.clientMap.values()) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Socket socket = Tool.isOnline(InetAddress.getByName(client.getIp()), port);
                        if (socket != null) {
                            Tool.sendMessage(socket, Info.getDisconnectPackage());
                        }
                    } catch (UnknownHostException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }
    }
}
