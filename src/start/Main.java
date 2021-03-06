package start;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import registry.Registry;
import tray.Tray;

import java.util.HashMap;

public class Main extends Application {

    private Stage primaryStage;
    private Registry registry;
    private HashMap<String, String> properties;
    public static Stage main;

    @Override
    public void start(final Stage primaryStage) throws Exception {
        this.registry = new Registry();
        this.properties = this.registry.getProperties();

        Platform.setImplicitExit(false);
        final Parent root = FXMLLoader.load(this.getClass().getResource("main_window.fxml"));

        this.primaryStage = primaryStage;
        this.primaryStage.setTitle("uLAN");
        this.primaryStage.getIcons().add(new Image(this.properties.get("windowicon")));
        this.primaryStage.setScene(new Scene(root, 650, 520));
        this.primaryStage.setResizable(false);
        Main.main = this.primaryStage;

        new Tray(this);
    }

    public static void main(final String[] args) {
        Application.launch(args);
    }

    public Stage getPrimaryStage() {
        return this.primaryStage;
    }

    public HashMap<String, String> getProperties() {
        return this.properties;
    }

    public void exitApplication() {
        Platform.exit();
        System.exit(0);
    }
}
