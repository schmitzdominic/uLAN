package info;

import javafx.application.Platform;
import javafx.collections.ObservableMap;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import network.Client;
import pages.FileTransferController;
import registry.Registry;
import start.MainController;

import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.zip.CRC32;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import static info.Info.getSettings;


public class Tool {

    private static int MAX_READ_SIZE = 1024;

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

    public static String humanReadableByteCountSI(long bytes) {
        String s = bytes < 0 ? "-" : "";
        long b = bytes == Long.MIN_VALUE ? Long.MAX_VALUE : Math.abs(bytes);
        return b < 1000L ? bytes + " B"
                : b < 999_950L ? String.format("%s%.1f kB", s, b / 1e3)
                : (b /= 1000) < 999_950L ? String.format("%s%.1f MB", s, b / 1e3)
                : (b /= 1000) < 999_950L ? String.format("%s%.1f GB", s, b / 1e3)
                : (b /= 1000) < 999_950L ? String.format("%s%.1f TB", s, b / 1e3)
                : (b /= 1000) < 999_950L ? String.format("%s%.1f PB", s, b / 1e3)
                : String.format("%s%.1f EB", s, b / 1e6);
    }

    public static String humanReadableTime(long sec) {
        long hours = sec / 3600;
        long minutes = (sec % 3600) / 60;
        long seconds = sec % 60;

        if (hours == 0 & minutes == 0) {
            return String.format("%02d Sek.", seconds);
        } else if (hours == 0) {
            return String.format("%02d:%02d", minutes, seconds);
        } else {
            return String.format("%02d:%02d:%02d", hours, minutes, seconds);
        }
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

    public static PrintWriter sendMessage(Socket socket, HashMap<String, String> message) {
        try {
            PrintWriter out = new PrintWriter(socket.getOutputStream());
            out.println(message);
            out.flush();
            return out;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void sendMessage(Client client, HashMap<String, String> message) {
        if (client != null) {
            client.getOut().println(message);
            client.getOut().flush();
        }
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

    public static void provideFolderToClient(Socket communicationSocket, File path) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {

                    ServerSocket listener = Tool.getFreeServerSocket();

                    if (listener != null) {
                        long folderSize = calculateFolderSize(path);

                        // TELL THE CLIENT THAT THE DOWNLOAD CAN BEGIN
                        System.out.println("USE PORT: " + listener.getLocalPort());
                        Tool.sendMessage(communicationSocket, Info.getDownloadFolderPackage(listener.getLocalPort(), path.getName(), folderSize));

                        Socket socket = listener.accept();

                        if (path.exists()) {

                            try {
                                ZipOutputStream zipOpStream = new ZipOutputStream(socket.getOutputStream());
                                sendFileOutput(zipOpStream, path);
                                zipOpStream.flush();
                            } catch (Exception e) {
                                System.out.println("OTHER SIDE STOPED DOWNLOAD!");
                            } finally {
                                socket.close();
                                listener.close();
                            }
                        } else {
                            // TODO: Exception handling! REMOVE SOP!
                            System.out.println("Folder to read does not exist ["+path.getAbsolutePath()+"]");
                        }
                    }
                } catch (Exception e) {
                    System.out.println("UPLOAD STOPED");
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public static void sendFileOutput(ZipOutputStream zipOpStream, File outFile) throws Exception {
        String relativePath = outFile.getAbsoluteFile().getParentFile().getAbsolutePath();
        System.out.println("relativePath[" + relativePath + "]");
        outFile = outFile.getAbsoluteFile();
        sendFolder(zipOpStream, outFile, relativePath);
    }

    public static void sendFolder(ZipOutputStream zipOpStream, File folder, String relativePath) throws Exception {
        File[] filesList = folder.listFiles();
        assert filesList != null;
        for (File file : filesList) {
            if (file.isDirectory()) {
                sendFolder(zipOpStream, file, relativePath);
            } else {
                sendFile(zipOpStream, file, relativePath);
            }
        }
    }

    public static void sendFile(ZipOutputStream zipOpStream, File file, String relativePath) throws Exception {
        String absolutePath = file.getAbsolutePath();
        String zipEntryFileName = absolutePath;
        int index = absolutePath.indexOf(relativePath);
        if(absolutePath.startsWith(relativePath)){
            zipEntryFileName = absolutePath.substring(relativePath.length());
            if(zipEntryFileName.startsWith(File.separator)){
                zipEntryFileName = zipEntryFileName.substring(1);
            }
            System.out.println("zipEntryFileName:::"+relativePath.length()+"::"+zipEntryFileName);
        }else{
            throw new Exception("Invalid Absolute Path");
        }

        BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file));
        byte[] fileByte = new byte[MAX_READ_SIZE];
        int readBytes = 0;
        CRC32 crc = new CRC32();
        while (0 != (readBytes = bis.read(fileByte))) {
            if(-1 == readBytes){
                break;
            }
            crc.update(fileByte, 0, readBytes);
        }
        bis.close();

        ZipEntry zipEntry = new ZipEntry(zipEntryFileName);
        zipEntry.setMethod(ZipEntry.STORED);
        zipEntry.setCompressedSize(file.length());
        zipEntry.setSize(file.length());
        zipEntry.setCrc(crc.getValue());
        zipOpStream.putNextEntry(zipEntry);
        bis = new BufferedInputStream(new FileInputStream(file));
        while (0 != (readBytes = bis.read(fileByte))) {
            if(-1 == readBytes){
                break;
            }
            zipOpStream.write(fileByte, 0, readBytes);
        }
        bis.close();

    }

    public static long calculateFolderSize(File directory) {
        long length = 0;
        if (directory != null) {
            for (File file : Objects.requireNonNull(directory.listFiles())) {
                if (file.isFile())
                    length += file.length();
                else
                    length += calculateFolderSize(file);
            }
        }
        return length;
    }

    public static void openFileTransferWindow(Initializable sStage, File path, String ip, int port, String folderName, long size) {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                try {
                    HashMap<String, String> settings = getSettings();

                    FXMLLoader fxmlLoader = new FXMLLoader(sStage.getClass().getResource("/pages/file_transfer_window.fxml"));
                    Parent root1 = fxmlLoader.load();

                    Stage fStage = new Stage();
                    fStage.setTitle("Datenübertragung");
                    fStage.getIcons().add(new Image(settings.get("defaulticon")));
                    fStage.setResizable(false);
                    fStage.setScene(new Scene(root1, 500, 170));
                    FileTransferController controller = fxmlLoader.<FileTransferController>getController();
                    controller.initData(path, fStage, ip, port, folderName, size);
                    fStage.show();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public static ServerSocket getFreeServerSocket() {
        Registry registry = new Registry();
        int startPort = Integer.parseInt(registry.getProperties().get("fileport"));
        int endPort = startPort + 1000;

        for (int i=startPort; i < endPort; i++) {
            try {
                return new ServerSocket(i);
            } catch (IOException ignored) {}
        }

        return null;
    }
}
