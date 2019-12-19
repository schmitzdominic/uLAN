package start;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.event.ActionEvent;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.*;
import network.Client;
import network.Clients;

public class MainController {

    @FXML
    AnchorPane mainWindow;

    @FXML
    ListView<String> clientList;

    @FXML
    Label clientTitle;

    private Clients clients;

    @FXML
    private void initialize() {
        this.createClientList();
    }

    private void createClientList(){
        this.clients = new Clients(this.clientList);
        this.clientList.setOpacity(0.5);

        this.clientList.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String value) {
                selectClient(clients.getClientByHostname(value));
            }
        });
    }

    private void selectClient(Client client){
        if(!this.clientTitle.isVisible()){
            this.makeClientInfoVisible();
        }
        clientTitle.setText(client.getHostname()); // Client Title
    }

    private void makeClientInfoVisible(){
        this.clientTitle.setVisible(true);
    }

    public void buttonAction(ActionEvent event) {
        this.clients.addClient(new Client("ABC", "10.20.30.40", "COOLER_PC"));
    }



}
