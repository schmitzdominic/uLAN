package start;

import info.Info;
import info.Tool;
import interfaces.ClientFoundListener;
import interfaces.ClientList;
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

    public static Initializable init;
    public static Stage releaseStage;
    public static Stage infoStage;
    public static Stage settingsStage;
    public static Stage historyStage;

    private Clients clients;
    private Client client;
    private Releases releases;
    private final Finder finder = new Finder();
    private final Registry registry = new Registry();
    private String actualRelease;
    private int port;

    @Override
    public void initialize(final URL location, final ResourceBundle resources) {
        MainController.init = this;
        this.createServer();
        this.createClientList();
        this.createReleasesList();
        this.setButtonIcons();
        this.setOwnInformation();
        this.searchClients();

        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                MainController.this.clients.disconnect();
            }
        });
    }

    private void createServer() {
        final HashMap<String, String> settings = Info.getSettings();
        this.port = Integer.parseInt(settings.get("port"));
        final Server server = new Server(this, this.port);
        server.registerClientFoundListener(this);
        server.start();
    }

    private void createClientList() {
        this.clients = new Clients(this.clientList, this);
        this.clientList.setOpacity(0.5);

        this.clientList.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(final ObservableValue<? extends String> observable, final String oldValue, final String value) {
                MainController.this.buttonDownload.setVisible(false);
                MainController.this.selectClient(MainController.this.clients.getClientByListName(value));
            }
        });
    }

    private void createReleasesList() {
        this.releases = new Releases(this.releaseList);

        this.releaseList.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(final ObservableValue<? extends String> observable, final String oldValue, final String value) {
                MainController.this.buttonDownload.setVisible(true);
                MainController.this.actualRelease = MainController.this.releases.getPathFromListItem(value);
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
        final Tooltip tooltip = new Tooltip(Info.getIp());
        this.labelOwnHostname.setText(Info.getHostname());
        this.labelOwnHostname.setTooltip(tooltip);
    }

    public void searchClients() {
        this.searchClients(Integer.parseInt(Info.getSettings().get("clientscount")));
    }

    public void searchClients(final int count) {
        if (!this.finder.active) {
            this.finder.registerClientFoundListener(this);
            this.finder.setCount(count);
            this.finder.setPort(this.port);
            this.finder.searchClients();

            new Thread(new Runnable() {
                @Override
                public void run() {
                    MainController.this.loadingGIF.setVisible(true);
                    MainController.this.buttonRefresh.setVisible(false);
                    while (MainController.this.finder.active) {
                        try {
                            Thread.sleep(50);
                        } catch (final InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    MainController.this.loadingGIF.setVisible(false);
                    MainController.this.buttonRefresh.setVisible(true);
                }
            }).start();
        }
    }

    @Override
    public void onClientFound(final Client client) {
        if (!this.clients.clientExists(client)) {
            this.addClient(client);
        }
    }

    @Override
    public void onClientRemove(final String id) {
        this.removeClient(id);
    }

    @Override
    public void onClientRemoveIp(final String ip) {
        this.removeClientByIp(ip);
    }

    @Override
    public void makeClientInfoInvisible() {
        this.clientTitle.setVisible(false);
        this.buttonChangeName.setVisible(false);
        this.clientInformation.setVisible(false);
        this.clientReleases.setVisible(false);
    }

    private void selectClient(final Client client) {
        if (!this.clientTitle.isVisible()) {
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

    private void makeClientInfoVisible() {
        this.clientTitle.setVisible(true);
        this.buttonChangeName.setVisible(true);
        this.clientInformation.setVisible(true);
    }

    private void saveClientListName() {
        this.client.setListName(this.textFieldChangeName.getText().toLowerCase());
        this.clients.changeClient(this.clientTitle.getText(), this.client);
        this.refreshClientInfo();
        this.resetChangeName();
        this.selectClient(this.client);
    }

    public void refreshClientInfo() {
        this.clientTitle.setText(this.client.getListName());
        this.labelHostnameText.setText(this.client.getHostname());
        this.labelIPText.setText(this.client.getIp());
    }

    public void resetChangeName() {
        this.buttonSaveName.setVisible(false);
        this.textFieldChangeName.setVisible(false);
        this.buttonChangeName.setVisible(true);
        this.clientTitle.setVisible(true);
    }

    public void addClient(final Client client) {
        this.clients.addClient(client);
    }

    public void removeClient(final Client client) {
        this.clients.removeClient(client);
    }

    public void removeClient(final String id) {
        this.clients.removeClientById(id);
    }

    public void removeClientByIp(final String ip) {
        this.clients.removeClientByIp(ip);
    }

    public void addReleases(final Client client) {
        this.releases.addReleases(client);
    }

    public void removeAllReleases(final Client client) {
        this.releases.removeAllReleases(client);
    }

    public boolean checkIfCientNameExist(final String name) {
        return this.clients.existName(name);
    }

    public void buttonRefresh(final ActionEvent event) {
        this.searchClients();
    }

    public void buttonChangeName(final ActionEvent event) {
        this.buttonChangeName.setVisible(false);
        this.buttonSaveName.setVisible(true);
        this.clientTitle.setVisible(false);
        this.textFieldChangeName.setText(this.client.getListName());
        this.textFieldChangeName.setVisible(true);
        this.buttonSaveName.setDefaultButton(true);
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                MainController.this.textFieldChangeName.requestFocus();
            }
        });
    }

    public void buttonSaveName(final ActionEvent event) {
        final String newName = this.textFieldChangeName.getText().toLowerCase();
        if (!this.checkIfCientNameExist(newName)) {
            this.saveClientListName();
        } else if (this.client.getHostname().equals(newName)) {
            this.saveClientListName();
        } else {
            System.out.println("EXISTS!");  // TODO: Popup Message!
        }
    }

    public void buttonDownload(final ActionEvent event) {
        if (this.actualRelease != null) {
            Tool.sendMessage(this.client, Info.getProvideFolderPackage(this.actualRelease));
        }
    }

    public void buttonReleases(final ActionEvent event) {
        try {
            final HashMap<String, String> settings = getSettings();
            final String[] initReleases = this.registry.getReleases();

            final FXMLLoader fxmlLoader = new FXMLLoader(this.getClass().getResource("/pages/release_window.fxml"));
            final Parent root = fxmlLoader.load();
            releaseStage = new Stage();
            releaseStage.initModality(Modality.WINDOW_MODAL);
            releaseStage.setTitle("Freigaben");
            releaseStage.getIcons().add(new Image(settings.get("defaulticon")));
            releaseStage.setResizable(false);
            releaseStage.initOwner(Main.main);
            releaseStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
                @Override
                public void handle(final WindowEvent event) {
                    boolean changed = false;
                    final String[] releases = MainController.this.registry.getReleases();
                    if (releases != null) {
                        changed = !Arrays.equals(releases, initReleases);
                    }
                    if (changed) {
                        Tool.sendReleasesChange(MainController.this.clients.getClientMap());
                    }
                }
            });
            releaseStage.setScene(new Scene(root, 500, 300));
            releaseStage.showAndWait();

        } catch (final IOException e) {
            e.printStackTrace();
        }
    }

    public void buttonInfo(final ActionEvent event) {

        try {
            final HashMap<String, String> settings = getSettings();
            final FXMLLoader fxmlLoader = new FXMLLoader(this.getClass().getResource("/pages/info_window.fxml"));
            final Parent root = fxmlLoader.load();
            infoStage = new Stage();
            infoStage.initModality(Modality.WINDOW_MODAL);
            infoStage.setTitle("Info");
            infoStage.getIcons().add(new Image(settings.get("defaulticon")));
            infoStage.setResizable(false);
            infoStage.initOwner(Main.main);
            infoStage.setScene(new Scene(root, 500, 300));
            infoStage.showAndWait();
        } catch (final IOException e) {
            e.printStackTrace();
        }
    }

    public void buttonSettings(final ActionEvent event) {
        try {
            final HashMap<String, String> settings = getSettings();
            final FXMLLoader fxmlLoader = new FXMLLoader(this.getClass().getResource("/pages/settings_window.fxml"));
            final Parent root = fxmlLoader.load();
            settingsStage = new Stage();
            settingsStage.initModality(Modality.WINDOW_MODAL);
            settingsStage.setTitle("Einstellungen");
            settingsStage.getIcons().add(new Image(settings.get("defaulticon")));
            settingsStage.setResizable(false);
            settingsStage.initOwner(Main.main);
            settingsStage.setScene(new Scene(root, 500, 300));
            settingsStage.showAndWait();
        } catch (final IOException e) {
            e.printStackTrace();
        }
    }

    public void buttonHistory(final ActionEvent event) {
        try {
            final HashMap<String, String> settings = getSettings();
            final FXMLLoader fxmlLoader = new FXMLLoader(this.getClass().getResource("/pages/history_window.fxml"));
            final Parent root = fxmlLoader.load();
            historyStage = new Stage();
            historyStage.initModality(Modality.WINDOW_MODAL);
            historyStage.setTitle("Historie");
            historyStage.getIcons().add(new Image(settings.get("defaulticon")));
            historyStage.setResizable(false);
            historyStage.initOwner(Main.main);
            historyStage.setScene(new Scene(root, 500, 300));
            historyStage.showAndWait();
        } catch (final IOException e) {
            e.printStackTrace();
        }
    }


}
