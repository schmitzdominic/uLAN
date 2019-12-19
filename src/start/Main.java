package start;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import tray.Tray;

public class Main extends Application {

    private Stage primaryStage;

    @Override
    public void start(Stage primaryStage) throws Exception{
        Platform.setImplicitExit(false);
        this.primaryStage = primaryStage;
        Parent root = FXMLLoader.load(getClass().getResource("main_window.fxml"));
        this.primaryStage.setTitle("uLAN");
        this.primaryStage.getIcons().add(new Image("/icons/baseline_account_tree_white_18dp.png"));;
        this.primaryStage.setScene(new Scene(root, 650, 450));
        this.primaryStage.setResizable(false);
        // this.primaryStage.show();
        new Tray(this);
    }

    public static void main(String[] args) {
        launch(args);
    }

    public Stage getPrimaryStage(){
        return this.primaryStage;
    }

    public void exitApplication(){
        Platform.exit();
        System.exit(0);
    }
}
