package start;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.event.ActionEvent;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import network.Client;
import network.Clients;
import network.Releases;
import network.Server;

public class MainController {

    @FXML
    AnchorPane mainWindow;

    @FXML
    AnchorPane clientInformation;

    @FXML
    AnchorPane clientReleases;

    @FXML
    ListView<String> clientList;

    @FXML
    ListView<String> releaseList;

    @FXML
    Label clientTitle;

    @FXML
    Label labelHostnameText;

    @FXML
    Label labelIPText;

    @FXML
    TextField textFieldChangeName;

    @FXML
    Button buttonChangeName;

    @FXML
    Button buttonSaveName;

    @FXML
    Button buttonDownload;

    @FXML
    Button buttonRefresh;

    @FXML
    ImageView imageButtonRefresh;

    @FXML
    ImageView imageButtonChangeName;

    @FXML
    ImageView imageButtonSaveName;

    @FXML
    ImageView imageButtonDownload;

    @FXML
    ImageView imageButtonRelease;

    @FXML
    ImageView imageButtonInfo;

    @FXML
    ImageView imageButtonSettings;

    @FXML
    ImageView imageButtonHistory;

    private Server server;
    private Clients clients;
    private Client client;
    private Releases releases;
    private String actualRelease;

    @FXML
    private void initialize() {
        this.createServer();
        this.createClientList();
        this.createReleasesList();
        this.setButtonIcons();
    }

    private void createServer(){
        this.server = new Server(this);
        this.server.start();
    }

    private void createClientList() {
        this.clients = new Clients(this.clientList);
        this.clientList.setOpacity(0.5);

        this.clientList.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String value) {
                selectClient(clients.getClientByListName(value));
            }
        });
    }

    private void createReleasesList() {
        this.releases = new Releases(this.releaseList);

        this.releaseList.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String value) {
                buttonDownload.setVisible(true);
                actualRelease = releases.getPathFromListItem(value);
            }
        });
    }

    private void setButtonIcons(){
        this.imageButtonRefresh.setImage(new Image("/icons/baseline_autorenew_white_18dp.png"));
        this.imageButtonChangeName.setImage(new Image("/icons/baseline_edit_white_18dp.png"));
        this.imageButtonSaveName.setImage(new Image("/icons/baseline_save_white_18dp.png"));
        this.imageButtonDownload.setImage(new Image("/icons/baseline_save_alt_white_18dp.png"));
        this.imageButtonRelease.setImage(new Image("/icons/baseline_toc_white_18dp.png"));
        this.imageButtonInfo.setImage(new Image("/icons/baseline_info_white_18dp.png"));
        this.imageButtonSettings.setImage(new Image("/icons/baseline_settings_applications_white_18dp.png"));
        this.imageButtonHistory.setImage(new Image("/icons/baseline_history_white_18dp.png"));

        this.resizeImage(this.imageButtonRefresh);
        this.resizeImage(this.imageButtonChangeName, 30, 30);
        this.resizeImage(this.imageButtonSaveName, 30, 30);
        this.resizeImage(this.imageButtonDownload);
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
        this.labelHostnameText.setText(client.getHostname());
        this.labelIPText.setText(client.getIp());
        if (client.getReleases().size() != 0) {
            this.clientReleases.setVisible(true);
            this.buttonDownload.setVisible(false);
            this.addReleases(client);
        } else {
            this.clientReleases.setVisible(false);
            this.removeAllReleases(client);
        }
    }

    private void makeClientInfoVisible(){
        this.clientTitle.setVisible(true);
        this.buttonChangeName.setVisible(true);
        this.clientInformation.setVisible(true);
    }

    private void saveClientListName(){
        this.client.setListName(this.textFieldChangeName.getText().toLowerCase());
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

    public void addReleases(Client client) {
        this.releases.addReleases(client);
    }

    public void removeAllReleases(Client client) {
        this.releases.removeAllReleases(client);
    }

    public boolean checkIfCientNameExist(String name){
        return this.clients.existName(name);
    }

    public void buttonRefresh(ActionEvent event) {
        for(int i = 0; i < 20; i++){
            Client client = new Client(String.format("%s", i), String.format("10.20.30.%s", i), String.format("COOLER_PC_%s", i));
            if(i%5==0){
                client.addRelease("TESTFOLDERABCSWE","PATHasdvönljosadvüojnwervarüojnaesvünojseravjnoövsar1");
                client.addRelease("TESTFOLDE","PATHasdvönljosadvüojnwervarüojnaesvvjnoövsar1");
                client.addRelease("TESTFOLDERABCSWE","PATHasdvönljosüojnwervarüojnaesvünojseravjnoövsar1");
                client.addRelease("TESTFOLDE","PATHasdvönljosadvüojnwervarüojnaesvvjnar1");
                client.addRelease("TESTFOLDERABCSWE","PATHasdvönljosadvüojnwervarüojnaesvünojseravvsar1");
                client.addRelease("TESTFOLDE","PATHasdvönljosadvüojvarüojnaesvvjnoövsar1");
                client.addRelease("TESTFOLDE","PATHasdvönljosadvüojnwervasvsvdvserüojnaesvvjnar1");
                client.addRelease("TESTFOLDERABCSWE","PATHasdvönljosadvüojnwervarüojnaesvüuio.,hjmgfhnojseravvsar1");
                client.addRelease("TESTFOLDE","PATHasdvönljosadvüosevesgfdsrbvjvarüojnaesvvjnoövsar1");
            }
            this.clients.addClient(client);
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
        String newName = this.textFieldChangeName.getText().toLowerCase();
        if(!this.checkIfCientNameExist(newName)){
            this.saveClientListName();
        } else if (this.client.getHostname().equals(newName)){
            this.saveClientListName();
        } else {
            System.out.println("EXISTS!");  // TODO: Popup Message!
        }
    }

    public void buttonDownload(ActionEvent event) {
        if (this.actualRelease != null) {
            System.out.println(String.format("From Client %s DOWNLOADING: %s", this.client.getHostname(), this.actualRelease));
            // TODO: Start Download here!
        }
    }
}
