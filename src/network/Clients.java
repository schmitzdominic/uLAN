package network;

import javafx.collections.FXCollections;
import javafx.collections.ObservableMap;
import javafx.scene.control.ListView;

public class Clients {

    private ObservableMap<String, Client> clientMap;
    private ListView<String> clientList;

    public Clients(ListView<String> clientList){
        this.clientMap = FXCollections.observableHashMap();
        this.clientList = clientList;
    }

    public ObservableMap<String, Client> getClientMap() {
        return this.clientMap;
    }

    public void setClientMap(ObservableMap<String, Client> clientMap) {
        this.clientMap = clientMap;
    }

    public void addClient(Client client) {
        if (!this.clientList.getItems().contains(client.getHostname())){
            this.clientList.getItems().add(client.getHostname());
        }
        this.clientMap.put(client.getId(), client);
    }

    public void removeClient(Client client) {
        int index = 0;
        for(String c : this.clientList.getItems()) {
            if (c.equals(client.getHostname())) {
                this.clientList.getItems().remove(index);
            }
            index++;
        }
        this.clientMap.remove(client.getId());
    }

    public void removeClient(String id) {
        this.clientMap.remove(id);
    }
}
