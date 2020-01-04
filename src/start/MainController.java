package start;

import Interfaces.ClientFoundListener;
import Interfaces.ClientList;
import info.Info;
import info.Tool;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import network.*;
import registry.Registry;

import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.ResourceBundle;

import static info.Info.getSettings;

public class MainController implements ClientFoundListener, Initializable, ClientList {

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

    public static Stage releaseStage;

    private Clients clients;
    private Client client;
    private Releases releases;
    private Finder finder = new Finder();
    private Registry registry = new Registry();
    private String actualRelease;
    private int port;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        this.createServer();
        this.createClientList();
        this.createReleasesList();
        this.setButtonIcons();
        this.setOwnInformation();
        this.searchClients();

        Runtime.getRuntime().addShutdownHook(new Thread()
        {
            public void run()
            {
                clients.disconnect();
            }
        });
    }

    private void createServer() {
        HashMap<String, String> settings = Info.getSettings();
        this.port = Integer.parseInt(settings.get("port"));
        Server server = new Server(this, this.port);
        server.registerClientFoundListener(this);
        server.start();
    }

    private void createClientList() {
        this.clients = new Clients(this.clientList, this);
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

        Tool.resizeImage(this.imageButtonRefresh);
        Tool.resizeImage(this.imageButtonChangeName, 30, 30);
        Tool.resizeImage(this.imageButtonSaveName, 30, 30);
        Tool.resizeImage(this.imageButtonDownload);
        Tool.resizeImage(this.imageButtonRelease);
        Tool.resizeImage(this.imageButtonInfo);
        Tool.resizeImage(this.imageButtonSettings);
        Tool.resizeImage(this.imageButtonHistory);
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
            this.finder.registerClientFoundListener(this);
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
    public void onClientFound(Client client) {
        if (!this.clients.clientExists(client)) {
            this.addClient(client);
        }
    }

    @Override
    public void onClientRemove(String id) {
        this.removeClient(id);
    }

    @Override
    public void onClientRemoveIp(String ip) {
        this.removeClientByIp(ip);
    }

    @Override
    public void makeClientInfoInvisible() {
        this.clientTitle.setVisible(false);
        this.buttonChangeName.setVisible(false);
        this.clientInformation.setVisible(false);
        this.clientReleases.setVisible(false);
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
        this.selectClient(this.client);
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

    public void removeClient(Client client) {
        this.clients.removeClient(client);
    }

    public void removeClient(String id) {
        this.clients.removeClientById(id);
    }

    public void removeClientByIp(String ip) {
        this.clients.removeClientByIp(ip);
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
        this.searchClients();
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
            // TODO: Check if its already exists!
            Tool.sendMessage(this.client.getSocket(), Info.getProvideFolderPackage(this.actualRelease));
        }
    }

    public void buttonReleases(ActionEvent event) {
        try {
            HashMap<String, String> settings = getSettings();
            String[] initReleases = registry.getReleases();

            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/release_window.fxml"));
            Parent root1 = fxmlLoader.load();
            releaseStage = new Stage();
            releaseStage.initModality(Modality.WINDOW_MODAL);
            releaseStage.setTitle("Freigaben");
            releaseStage.getIcons().add(new Image(settings.get("defaulticon")));
            releaseStage.setResizable(false);
            releaseStage.initOwner(Main.main);
            releaseStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
                @Override
                public void handle(WindowEvent event) {
                    boolean changed = false;
                    String[] releases = registry.getReleases();
                    if (releases != null) {
                        changed = !Arrays.equals(releases, initReleases);
                    }
                    if (changed) {
                        Tool.sendReleasesChange(clients.getClientMap());
                    }
                }
            });
            releaseStage.setScene(new Scene(root1, 500, 300));
            releaseStage.showAndWait();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
