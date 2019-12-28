package network;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableMap;
import javafx.collections.transformation.SortedList;
import javafx.scene.control.ListView;

import java.util.Comparator;

public class Clients {

    private ObservableMap<String, Client> clientMap;
    private ListView<String> clientList;

    /**
     * Constructor
     * @param clientList ListView<String> to view clients
     */
    public Clients(ListView<String> clientList){
        this.clientMap = FXCollections.observableHashMap();
        this.clientList = clientList;
        this.clientList.getItems().sort(Comparator.naturalOrder());
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

    public void addClient(Client client) {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                if (!clientList.getItems().contains(client.getListName()) & clientMap.get(client.getId()) == null){
                    clientList.getItems().add(client.getListName());
                    clientMap.put(client.getId(), client);
                }
            }
        });
    }

    public void removeClient(Client client) {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                int index = 0;
                for(String c : clientList.getItems()) {
                    if (c.equals(client.getListName())) {
                        clientList.getItems().remove(index);
                        clientMap.remove(client.getId());
                    }
                    index++;
                }
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
}
