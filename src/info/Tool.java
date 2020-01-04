package info;

import javafx.collections.ObservableMap;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import network.Client;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;


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
            PrintWriter out = new PrintWriter(socket.getOutputStream());
            out.print(message + "\n");
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void sendMessage(Socket socket, HashMap<String, String> message) {
        try {
            PrintWriter out = new PrintWriter(socket.getOutputStream());
            out.println(message);
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void sendMessage(Client client, HashMap<String, String> message) {
        client.getOut().println(message);
        client.getOut().flush();
    }

    public static Map<String, String> convertMessage(String message) {
        message = message.replaceAll("[{} ]","");
        return Tool.convertWithStream(message);
    }

    public static HashMap<String, String> convertReleasesString(String releases) {
        HashMap<String, String> map = new HashMap<>();
        String[] reArray = releases.split(";");
        for(int i = 0; i < reArray.length; i++) {
            String[] folderNames = reArray[i].split("\\\\");
            String folder = folderNames[folderNames.length-1];
            map.put(reArray[i], folder);
        }
        return map;
    }

    public static void sendReleasesChange(ObservableMap<String, Client> clientList) {
        for (Client client : clientList.values()) {
            if (client.getSocket() != null) {
                sendMessage(client, Info.getReleasesChangedPackage());
            }
        }
    }

    public static void provideFileToClient(Socket socket, File path) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    File[] files = path.listFiles();

                    BufferedOutputStream bos = new BufferedOutputStream(socket.getOutputStream());
                    DataOutputStream dos = new DataOutputStream(bos);

                    assert files != null;
                    dos.writeInt(files.length);

                    for(File file : files) {
                        System.out.println("PROVIDING " + file.getName());
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public static void downloadFile(Socket socket, File path) {

    }
}
