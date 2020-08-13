package start;

import entities.Payload;
import helpers.Info;
import helpers.Tool;
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

import static helpers.Info.getSettings;

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
        createServer();
        createClientList();
        createReleasesList();
        setButtonIcons();
        setOwnInformation();
        searchClients();

        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                clients.disconnect();
            }
        });
    }

    private void createServer() {
        final HashMap<String, String> settings = Info.getSettings();
        port = Integer.parseInt(settings.get("port"));
        final Server server = new Server(this, port);
        server.registerClientFoundListener(this);
        server.start();
    }

    private void createClientList() {
        clients = new Clients(clientList, this);
        clientList.setOpacity(0.5);

        clientList.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(final ObservableValue<? extends String> observable, final String oldValue, final String value) {
                buttonDownload.setVisible(false);
                selectClient(clients.getClientByListName(value));
            }
        });
    }

    private void createReleasesList() {
        releases = new Releases(releaseList);

        releaseList.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(final ObservableValue<? extends String> observable, final String oldValue, final String value) {
                buttonDownload.setVisible(true);
                actualRelease = releases.getPathFromListItem(value);
            }
        });
    }

    private void setButtonIcons() {
        imageButtonRefresh.setImage(new Image("/icons/baseline_autorenew_white_18dp.png"));
        imageButtonChangeName.setImage(new Image("/icons/baseline_edit_white_18dp.png"));
        imageButtonSaveName.setImage(new Image("/icons/baseline_save_white_18dp.png"));
        imageButtonDownload.setImage(new Image("/icons/baseline_save_alt_white_18dp.png"));
        imageButtonRelease.setImage(new Image("/icons/baseline_toc_white_18dp.png"));
        imageButtonInfo.setImage(new Image("/icons/baseline_info_white_18dp.png"));
        imageButtonSettings.setImage(new Image("/icons/baseline_settings_applications_white_18dp.png"));
        imageButtonHistory.setImage(new Image("/icons/baseline_history_white_18dp.png"));

        Tool.resizeImage(imageButtonRefresh);
        Tool.resizeImage(imageButtonChangeName, 30, 30);
        Tool.resizeImage(imageButtonSaveName, 30, 30);
        Tool.resizeImage(imageButtonDownload);
        Tool.resizeImage(imageButtonRelease);
        Tool.resizeImage(imageButtonInfo);
        Tool.resizeImage(imageButtonSettings);
        Tool.resizeImage(imageButtonHistory);

        // TODO: Remove!
        imageButtonInfo.setVisible(false);
        imageButtonHistory.setVisible(false);
    }

    private void setOwnInformation() {
        final Tooltip tooltip = new Tooltip(Info.getIp());
        labelOwnHostname.setText(Info.getHostname());
        labelOwnHostname.setTooltip(tooltip);
    }

    public void searchClients() {
        searchClients(Integer.parseInt(Info.getSettings().get("clientscount")));
    }

    public void searchClients(final int count) {
        if (!finder.active) {
            finder.registerClientFoundListener(this);
            finder.setCount(count);
            finder.setPort(port);
            finder.searchClients();

            new Thread(new Runnable() {
                @Override
                public void run() {
                    loadingGIF.setVisible(true);
                    buttonRefresh.setVisible(false);
                    while (finder.active) {
                        try {
                            Thread.sleep(50);
                        } catch (final InterruptedException e) {
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
    public void onClientFound(final Client client) {
        if (!clients.clientExists(client)) {
            addClient(client);
        }
    }

    @Override
    public void onClientRemove(final String id) {
        removeClient(id);
    }

    @Override
    public void onClientRemoveIp(final String ip) {
        removeClientByIp(ip);
    }

    @Override
    public void makeClientInfoInvisible() {
        clientTitle.setVisible(false);
        buttonChangeName.setVisible(false);
        clientInformation.setVisible(false);
        clientReleases.setVisible(false);
    }

    private void selectClient(final Client client) {
        if (!clientTitle.isVisible()) {
            makeClientInfoVisible();
        }
        if (client != null) {
            this.client = client;
        }
        resetChangeName();
        refreshClientInfo();
        if (this.client.getReleases().size() != 0) {
            clientReleases.setVisible(true);
            addReleases(client);
        } else {
            clientReleases.setVisible(false);
            removeAllReleases(client);
        }
    }

    private void makeClientInfoVisible() {
        clientTitle.setVisible(true);
        buttonChangeName.setVisible(true);
        clientInformation.setVisible(true);
    }

    private void saveClientListName() {
        client.setListName(textFieldChangeName.getText().toLowerCase());
        clients.changeClient(clientTitle.getText(), client);
        refreshClientInfo();
        resetChangeName();
        selectClient(client);
    }

    public void refreshClientInfo() {
        clientTitle.setText(client.getListName());
        labelHostnameText.setText(client.getHostname());
        labelIPText.setText(client.getIp());
    }

    public void resetChangeName() {
        buttonSaveName.setVisible(false);
        textFieldChangeName.setVisible(false);
        buttonChangeName.setVisible(true);
        clientTitle.setVisible(true);
    }

    public void addClient(final Client client) {
        clients.addClient(client);
    }

    public void removeClient(final Client client) {
        clients.removeClient(client);
    }

    public void removeClient(final String id) {
        clients.removeClientById(id);
    }

    public void removeClientByIp(final String ip) {
        clients.removeClientByIp(ip);
    }

    public void addReleases(final Client client) {
        releases.addReleases(client);
    }

    public void removeAllReleases(final Client client) {
        releases.removeAllReleases(client);
    }

    public boolean checkIfClientNameExist(final String name) {
        return clients.existName(name);
    }

    public void buttonRefresh(final ActionEvent event) {
        searchClients();
    }

    public void buttonChangeName(final ActionEvent event) {
        buttonChangeName.setVisible(false);
        buttonSaveName.setVisible(true);
        clientTitle.setVisible(false);
        textFieldChangeName.setText(client.getListName());
        textFieldChangeName.setVisible(true);
        buttonSaveName.setDefaultButton(true);
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                textFieldChangeName.requestFocus();
            }
        });
    }

    public void buttonSaveName(final ActionEvent event) {
        final String newName = textFieldChangeName.getText().toLowerCase();
        if (!checkIfClientNameExist(newName)) {
            saveClientListName();
        } else if (client.getHostname().equals(newName)) {
            saveClientListName();
        } else {
            System.out.println("EXISTS!");  // TODO: Popup Message!
        }
    }

    public void buttonDownload(final ActionEvent event) {
        if (actualRelease != null) {
            Payload.PROVIDE_DATA.setParams(actualRelease).sendTo(client);
        }
    }

    public void buttonReleases(final ActionEvent event) {
        try {
            final HashMap<String, String> settings = getSettings();
            final String[] initReleases = registry.getReleases();

            final FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/pages/release_window.fxml"));
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
                    final String[] releases = registry.getReleases();
                    if (releases != null) {
                        changed = !Arrays.equals(releases, initReleases);
                    }
                    if (changed) {
                        Tool.sendReleasesChange(clients.getClientMap());
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
            final FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/pages/info_window.fxml"));
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
            final FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/pages/settings_window.fxml"));
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
            final FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/pages/history_window.fxml"));
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
