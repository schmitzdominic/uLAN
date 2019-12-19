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

    public Client getClientByHostname(String hostname){
        for(Client c : this.clientMap.values()){
            if(c.getHostname().equals(hostname)){
                return c;
            }
        }
        return null;
    }

    public void addClient(Client client) {
        if (!this.clientList.getItems().contains(client.getHostname()) & this.clientMap.get(client.getId()) == null){
            this.clientList.getItems().add(client.getHostname());
            this.clientMap.put(client.getId(), client);
        }
    }

    public void removeClient(Client client) {
        int index = 0;
        for(String c : this.clientList.getItems()) {
            if (c.equals(client.getHostname())) {
                this.clientList.getItems().remove(index);
                this.clientMap.remove(client.getId());
            }
            index++;
        }
    }
}
