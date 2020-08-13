package entities.windows;

import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import pages.*;
import registry.Registry;
import start.Main;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;

public enum Window {

    MAIN("uLAN", Main.class.getResource("main_window.fxml"), 650, 520),
    RELEASES("Freigaben", ReleaseController.class.getResource("/pages/release_window.fxml"), 500, 300),
    SETTINGS("Einstellungen", SettingsController.class.getResource("/pages/settings_window.fxml"), 500, 300),
    HISTORY("Historie", HistoryController.class.getResource("/pages/history_window.fxml"), 500, 300),
    INFORMATIONS("Informationen", InfoController.class.getResource("/pages/info_window.fxml"), 500, 300),
    DATA_DOWNLOAD("Download", FileTransferController.class.getResource("/pages/file_transfer_window.fxml"), 400, 170);

    private final String PROPERTY_DEFAULT_ICON = "defaulticon";

    private final String title;
    private final URL fxmlLocation;
    private final int width;
    private final int height;
    private final Registry registry = new Registry();
    private final HashMap<String, String> properties;

    private Stage stage;
    private Image icon;
    private EventHandler<WindowEvent> onCloseHandler;

    Window(final String title, final URL fxmlLocation, final int width, final int height) {
        this.title = title;
        this.fxmlLocation = fxmlLocation;
        this.width = width;
        this.height = height;
        properties = registry.getProperties();
        icon = new Image(properties.get(PROPERTY_DEFAULT_ICON));
        stage = new Stage();
    }

    public Stage getStage() {
        return stage;
    }

    public Image getIcon() {
        return icon;
    }

    /**
     * Chain this before showAndWait (if you want to use this function).
     *
     * @param handler to define what happen if the window is closed
     * @return a Instance of it self to chain this function
     */
    public Window setOnCloseHandler(final EventHandler<WindowEvent> handler) {
        stage.setOnCloseRequest(handler);
        return this;
    }

    public void show() {
        stage.show();
    }

    public void close() {
        stage.close();
    }

    public void showAndWait() {
        showAndWait(null);
    }

    public void showAndWait(final javafx.stage.Window owner) {
        showAndWait(stage, icon, owner);
    }

    public void showAndWait(final Stage primaryStage, final Image icon, final javafx.stage.Window owner) {
        setDefaultStageSettings(primaryStage, icon, owner).showAndWait();
        cleanStage();
    }

    public Stage fillStage(final Stage primaryStage, final Image icon, final javafx.stage.Window owner) {
        return setDefaultStageSettings(primaryStage, icon, owner);
    }

    private void cleanStage() {
        stage = new Stage();
    }

    private Stage setDefaultStageSettings(final Stage stage, final Image icon, final javafx.stage.Window owner) {
        try {
            this.icon = icon;
            final Parent root = FXMLLoader.load(fxmlLocation);
            if (this != Window.MAIN) {
                stage.initModality(Modality.WINDOW_MODAL);
                if (owner != null) {
                    stage.initOwner(owner);
                }
            }
            stage.setTitle(title);
            stage.getIcons().add(icon);
            stage.setScene(new Scene(root, width, height));
            stage.setResizable(false);
            this.stage = stage;
            return stage;
        } catch (final IOException e) {
            e.printStackTrace();
        }
        return stage;
    }
}
