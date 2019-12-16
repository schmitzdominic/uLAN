package start;

import javafx.fxml.FXML;
import javafx.event.ActionEvent;
import javafx.scene.control.ListView;
import javafx.scene.layout.*;
import network.Client;
import network.Clients;

public class MainController {

    @FXML
    AnchorPane mainWindow;

    @FXML
    ListView<String> clientList;

    private Clients clients;

    @FXML
    private void initialize() {
        this.createClientList();
    }

    private void createClientList(){
        this.clients = new Clients(this.clientList);
        this.clientList.setOpacity(0.5);
    }

    public void buttonAction(ActionEvent event) {
        this.clients.addClient(new Client("ABC", "10.20.30.40", "COOLER_PC"));
        System.out.println(this.clients.getClientMap().size() + " " + this.clientList.getItems().size());
    }

}
