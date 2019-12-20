package start;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
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

    @FXML
    TextField textFieldChangeName;

    @FXML
    Button buttonChangeName;

    @FXML
    Button buttonSaveName;

    @FXML
    ImageView imageButtonRefresh;

    @FXML
    ImageView imageButtonChangeName;

    @FXML
    ImageView imageButtonSaveName;

    @FXML
    ImageView imageButtonRelease;

    @FXML
    ImageView imageButtonInfo;

    @FXML
    ImageView imageButtonSettings;

    @FXML
    ImageView imageButtonHistory;

    private Clients clients;
    private Client client;

    @FXML
    private void initialize() {
        this.createClientList();
        this.setButtonIcons();
    }

    private void createClientList(){
        this.clients = new Clients(this.clientList);
        this.clientList.setOpacity(0.5);

        this.clientList.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String value) {
                selectClient(clients.getClientByListName(value));
            }
        });
    }

    private void setButtonIcons(){
        this.imageButtonRefresh.setImage(new Image("/icons/baseline_autorenew_white_18dp.png"));
        this.imageButtonChangeName.setImage(new Image("/icons/baseline_edit_white_18dp.png"));
        this.imageButtonSaveName.setImage(new Image("/icons/baseline_save_white_18dp.png"));
        this.imageButtonRelease.setImage(new Image("/icons/baseline_toc_white_18dp.png"));
        this.imageButtonInfo.setImage(new Image("/icons/baseline_info_white_18dp.png"));
        this.imageButtonSettings.setImage(new Image("/icons/baseline_settings_applications_white_18dp.png"));
        this.imageButtonHistory.setImage(new Image("/icons/baseline_history_white_18dp.png"));

        this.resizeImage(this.imageButtonRefresh);
        this.resizeImage(this.imageButtonChangeName, 30, 30);
        this.resizeImage(this.imageButtonSaveName, 30, 30);
        this.resizeImage(this.imageButtonRelease);
        this.resizeImage(this.imageButtonInfo);
        this.resizeImage(this.imageButtonSettings);
        this.resizeImage(this.imageButtonHistory);
    }

    private ImageView resizeImage(ImageView image){
        return this.resizeImage(image, 22, 22);
    }

    private ImageView resizeImage(ImageView image, int width, int height){
        image.setFitHeight(height);
        image.setFitWidth(width);
        return image;
    }

    private void selectClient(Client client){
        if(!this.clientTitle.isVisible()){
            this.makeClientInfoVisible();
        }
        this.resetChangeName();
        this.client = client;
        this.clientTitle.setText(client.getListName()); // Client Title
    }

    private void makeClientInfoVisible(){
        this.clientTitle.setVisible(true);
        this.buttonChangeName.setVisible(true);

    }

    private void saveClientListName(){
        this.client.setListName(this.textFieldChangeName.getText());
        this.clients.changeClient(this.clientTitle.getText(), this.client);
        this.refreshClientInfo();
        this.resetChangeName();
    }

    public void refreshClientInfo(){
        this.clientTitle.setText(client.getListName());
        // TODO: Add other client Infos!
    }

    public void resetChangeName(){
        this.buttonSaveName.setVisible(false);
        this.textFieldChangeName.setVisible(false);
        this.buttonChangeName.setVisible(true);
        this.clientTitle.setVisible(true);
    }

    public void addClient(Client client){
        this.clients.addClient(client);
    }

    public void removeClient(Client client){
        this.clients.removeClient(client);
    }

    public boolean checkIfCientNameExist(String name){
        return this.clients.existName(name);
    }

    public void buttonAction(ActionEvent event) {
        for(int i = 0; i < 20; i++){
            this.clients.addClient(new Client(String.format("%s", i), String.format("10.20.30.%s", i), String.format("COOLER_PC_%s", i)));
        }
    }

    public void buttonChangeName(ActionEvent event) {
        this.buttonChangeName.setVisible(false);
        this.buttonSaveName.setVisible(true);
        this.clientTitle.setVisible(false);
        this.textFieldChangeName.setText(this.client.getListName());
        this.textFieldChangeName.setVisible(true);
        this.buttonSaveName.setDefaultButton(true);
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                textFieldChangeName.requestFocus();
            }
        });
    }

    public void buttonSaveName(ActionEvent event) {
        String newName = this.textFieldChangeName.getText();
        if(!this.checkIfCientNameExist(newName)){
            this.saveClientListName();
        } else if (this.client.getHostname().equals(newName)){
            this.saveClientListName();
        } else {
            System.out.println("EXISTS!");  // TODO: Popup Message!
        }

    }



}
