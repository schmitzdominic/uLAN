package start;

import Interfaces.ClientFoundListener;
import info.Info;
import info.Tool;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.event.ActionEvent;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import network.*;
import registry.Registry;

import java.net.Socket;
import java.util.HashMap;
import java.util.Random;

public class MainController implements ClientFoundListener {

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
    Label labelOwnHostname;

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

    @FXML
    ImageView loadingGIF;

    private Server server;
    private Clients clients;
    private Client client;
    private Releases releases;
    private Finder finder = new Finder();
    private String actualRelease;
    private int port;

    @FXML
    private void initialize() {
        this.createServer();
        this.createClientList();
        this.createReleasesList();
        this.setButtonIcons();
        this.setOwnInformation();
        this.searchClients();
    }

    private void createServer() {
        HashMap<String, String> settings = Info.getSettings();
        this.port = Integer.parseInt(settings.get("port"));
        this.server = new Server(this, this.port);
        this.server.registerClientFoundListener(this);
        this.server.start();
    }

    private void createClientList() {
        this.clients = new Clients(this.clientList);
        this.clientList.setOpacity(0.5);

        this.clientList.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String value) {
                buttonDownload.setVisible(false);
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

    private void setButtonIcons() {
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

    private void setOwnInformation() {
        Tooltip tooltip = new Tooltip(Info.getIp());
        this.labelOwnHostname.setText(Info.getHostname());
        this.labelOwnHostname.setTooltip(tooltip);
    }

    public void searchClients() {
        this.searchClients(Integer.parseInt(Info.getSettings().get("clientscount")));
    }

    public void searchClients(int count) {
        if (!finder.active) {
            this.finder.setCount(count);
            this.finder.setPort(this.port);
            this.finder.searchClients();

            new Thread(new Runnable() {
                @Override
                public void run() {
                    loadingGIF.setVisible(true);
                    buttonRefresh.setVisible(false);
                    while (finder.active) {
                        try {
                            Thread.sleep(50);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    loadingGIF.setVisible(false);
                    buttonRefresh.setVisible(true);
                }
            }).start();
        }
    }

    @Override
    public void onClientFound(Client client, Socket socket) {
        if (!this.clients.clientExists(client)) {
            this.addClient(client);
            Tool.sendMessage(socket, Info.getInitializePackage());
        }
    }

    @Override
    public void onClientRemove(Client client, Socket socket) {
        if (this.clients.clientExists(client)) {
            this.removeClient(client);
        }
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
        if (client != null) {
            this.client = client;
        }
        this.resetChangeName();
        this.refreshClientInfo();
        if (this.client.getReleases().size() != 0) {
            this.clientReleases.setVisible(true);
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
        this.labelHostnameText.setText(client.getHostname());
        this.labelIPText.setText(client.getIp());
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
        // TODO: Implement refresh
        this.searchClients();
        /*
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
        */
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
