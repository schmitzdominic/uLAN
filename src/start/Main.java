package start;

import entities.windows.Window;
import helpers.Info;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import tray.Tray;

public class Main extends Application {

    public static Stage main;

    @Override
    public void start(final Stage primaryStage) {
        Platform.setImplicitExit(false);
        // TODO remove Main.main
        Main.main = Window.MAIN.fillStage(primaryStage, new Image(Info.getProperties().get("windowicon")), null);
        new Tray(this);
    }

    public static void main(final String[] args) {
        Application.launch(args);
    }

    public void exitApplication() {
        Platform.exit();
        System.exit(0);
    }
}
