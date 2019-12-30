package info;

import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import static info.Info.getSettings;

public class Tool {

    public static Stage releaseStage;

    public static ImageView resizeImage(ImageView image){
        return resizeImage(image, 22, 22);
    }

    public static ImageView resizeImage(ImageView image, int width, int height){
        image.setFitHeight(height);
        image.setFitWidth(width);
        return image;
    }

    public static Map<String, String> convertWithStream(String mapAsString) {
        Map<String, String> map = Arrays.stream(mapAsString.split(","))
                .map(entry -> entry.split("="))
                .collect(Collectors.toMap(entry -> entry[0], entry -> entry[1]));
        return map;
    }

    public static Socket isOnline(InetAddress ip, int port){
        try{
            return new Socket(ip, port);
        } catch (IOException x){
            return null;
        }
    }

    public static void sendMessage(Socket socket, String message) {
        try {
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            out.println(message);
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void sendMessage(Socket socket, HashMap<String, String> message) {
        try {
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            out.println(message);
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Map<String, String> convertMessage(String message) {
        message = message.replaceAll("[{} ]","");
        return Tool.convertWithStream(message);
    }

    public static void openReleases(Initializable parentController, Stage parentStage) {
        try {
            HashMap<String, String> settings = getSettings();

            Stage rStage = new Stage();
            Tool.releaseStage = rStage;
            Parent fxStage = FXMLLoader.load(parentController.getClass().getResource("../pages/release_window.fxml"));
            rStage.setTitle("Freigaben");
            rStage.setScene(new Scene(fxStage, 500, 500));
            rStage.getIcons().add(new Image(settings.get("defaulticon")));
            rStage.setResizable(false);
            rStage.initModality(Modality.WINDOW_MODAL);
            rStage.initOwner(parentStage);
            rStage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
