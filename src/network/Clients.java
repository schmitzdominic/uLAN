package network;

import javafx.collections.FXCollections;
import javafx.collections.ObservableMap;
import javafx.scene.control.ListView;

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
        if (!this.clientList.getItems().contains(client.getListName()) & this.clientMap.get(client.getId()) == null){
            this.clientList.getItems().add(client.getListName());
            this.clientMap.put(client.getId(), client);
        }
    }

    public void removeClient(Client client) {
        int index = 0;
        for(String c : this.clientList.getItems()) {
            if (c.equals(client.getListName())) {
                this.clientList.getItems().remove(index);
                this.clientMap.remove(client.getId());
            }
            index++;
        }
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
