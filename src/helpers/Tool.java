package helpers;

import entities.Payload;
import entities.Release;
import entities.payload.DownloadData;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import network.Client;
import network.Download;
import pages.FileTransferController;
import registry.Registry;

import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.stream.Collectors;
import java.util.zip.CRC32;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static helpers.Info.getSettings;

public class Tool {

    public static int MAX_READ_SIZE = 1024;

    public static ObservableList<Download> downloads;

    public static void addDownload(final String id, final String path) {
        if (Tool.downloads == null) {
            Tool.downloads = FXCollections.observableArrayList();
        }
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                if (!Tool.downloadExist(id, path)) {
                    Tool.downloads.add(new Download(id, path));
                }
            }
        });
    }

    public static boolean downloadExist(final String id, final String path) {
        if (Tool.downloads != null) {
            for (final Download download : Tool.downloads) {
                if (download.getId().equals(id) & download.getPath().equals(path)) {
                    return true;
                }
            }
        }
        return false;
    }

    public static void removeDownload(final String id, final String path) {
        if (Tool.downloads != null) {
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    int index = 0;
                    for (final Download download : Tool.downloads) {
                        if (download.getId().equals(id) & download.getPath().equals(path)) {
                            break;
                        }
                        index++;
                    }
                    if (index != Tool.downloads.size()) {
                        Tool.downloads.remove(index);
                    }
                }
            });
        }
    }

    public static ImageView resizeImage(final ImageView image) {
        return resizeImage(image, 22, 22);
    }

    public static ImageView resizeImage(final ImageView image, final int width, final int height) {
        image.setFitHeight(height);
        image.setFitWidth(width);
        return image;
    }

    public static Map<String, String> convertWithStream(final String mapAsString) {
        final Map<String, String> map = Arrays.stream(mapAsString.split(","))
                .map(entry -> entry.split("="))
                .collect(Collectors.toMap(entry -> entry[0], entry -> entry[1]));
        return map;
    }

    public static String humanReadableByteCountSI(final long bytes) {
        final String s = bytes < 0 ? "-" : "";
        long b = bytes == Long.MIN_VALUE ? Long.MAX_VALUE : Math.abs(bytes);
        return b < 1000L ? bytes + " B"
                : b < 999_950L ? String.format("%s%.1f kB", s, b / 1e3)
                : (b /= 1000) < 999_950L ? String.format("%s%.1f MB", s, b / 1e3)
                : (b /= 1000) < 999_950L ? String.format("%s%.1f GB", s, b / 1e3)
                : (b /= 1000) < 999_950L ? String.format("%s%.1f TB", s, b / 1e3)
                : (b /= 1000) < 999_950L ? String.format("%s%.1f PB", s, b / 1e3)
                : String.format("%s%.1f EB", s, b / 1e6);
    }

    public static String humanReadableTime(final long sec) {
        final long hours = sec / 3600;
        final long minutes = (sec % 3600) / 60;
        final long seconds = sec % 60;

        if (hours == 0 & minutes == 0) {
            return String.format("%02d Sek.", seconds);
        } else if (hours == 0) {
            return String.format("%02d:%02d", minutes, seconds);
        } else {
            return String.format("%02d:%02d:%02d", hours, minutes, seconds);
        }
    }

    public static Socket isOnline(final InetAddress ip, final int port) {
        try {
            return new Socket(ip, port);
        } catch (final IOException x) {
            return null;
        }
    }

    public static void sendMessage(final Socket socket, final String message) {
        try {
            final PrintWriter out = new PrintWriter(socket.getOutputStream());
            out.print(message + "\n");
            out.flush();
        } catch (final IOException e) {
            e.printStackTrace();
        }
    }

    public static PrintWriter sendMessage(final Socket socket, final HashMap<String, String> message) {
        try {
            final PrintWriter out = new PrintWriter(socket.getOutputStream());
            out.println(message);
            out.flush();
            return out;
        } catch (final IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void sendMessage(final Client client, final HashMap<String, String> message) {
        if (client != null) {
            if (client.getSocket() != null) {
                if (client.getOut() == null) {
                    try {
                        client.setOut(new PrintWriter(client.getSocket().getOutputStream()));
                    } catch (final IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            client.getOut().println(message);
            client.getOut().flush();
        }
    }

    public static Map<String, String> convertMessage(String message) {
        message = message.replaceAll("[{} ]", "");
        return Tool.convertWithStream(message);
    }

    public static HashMap<String, String> convertReleasesString(final String releases) {
        final HashMap<String, String> map = new HashMap<>();
        final String[] reArray = releases.split(";");
        for (int i = 0; i < reArray.length; i++) {
            final String[] folderNames = reArray[i].split("\\\\");
            final String folder = folderNames[folderNames.length - 1];
            map.put(reArray[i], folder);
        }
        return map;
    }

    public static ArrayList<Release> convertReleasesStringToReleaseList(final String releases) {
        final ArrayList<Release> list = new ArrayList<>();
        final String[] reArray = releases.split(";");
        for (int i = 0; i < reArray.length; i++) {
            final String[] folderNames = reArray[i].split("\\\\");
            final String folder = folderNames[folderNames.length - 1];
            list.add(new Release(folder, reArray[i]));
        }
        return list;
    }

    public static void sendReleasesChange(final ObservableMap<String, Client> clientList) {
        for (final Client client : clientList.values()) {
            if (client.getSocket() != null) {
                Payload.RELEASE_CHANGE.sendTo(client);
            }
        }
    }

    public static void provideFolderToClient(final Socket communicationSocket, final File path) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {

                    final ServerSocket listener = Tool.getFreeServerSocket();

                    if (listener != null) {
                        final long folderSize = Tool.calculateFolderSize(path);

                        // TELL THE CLIENT THAT THE DOWNLOAD CAN BEGIN
                        System.out.println("USE PORT: " + listener.getLocalPort());
                        Payload.DOWNLOAD_FOLDER.setParams(listener.getLocalPort() + "", path.getAbsolutePath(), folderSize + "").sendTo(communicationSocket);

                        final Socket socket = listener.accept();

                        if (path.exists()) {

                            try {
                                final ZipOutputStream zipOpStream = new ZipOutputStream(socket.getOutputStream());
                                Tool.sendFileOutput(zipOpStream, path);
                                zipOpStream.flush();
                            } catch (final Exception e) {
                                System.out.println("OTHER SIDE STOPPED DOWNLOAD!");
                            } finally {
                                socket.close();
                                listener.close();
                            }
                        } else {
                            // TODO: Exception handling! REMOVE SOP!
                            System.out.println("Folder to read does not exist [" + path.getAbsolutePath() + "]");
                        }
                    }
                } catch (final Exception e) {
                    System.out.println("UPLOAD STOPED");
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public static void sendFileOutput(final ZipOutputStream zipOpStream, File outFile) throws Exception {
        final String relativePath = outFile.getAbsoluteFile().getParentFile().getAbsolutePath();
        System.out.println("relativePath[" + relativePath + "]");
        outFile = outFile.getAbsoluteFile();
        sendFolder(zipOpStream, outFile, relativePath);
    }

    public static void sendFolder(final ZipOutputStream zipOpStream, final File folder, final String relativePath) throws Exception {
        final File[] filesList = folder.listFiles();
        assert filesList != null;
        for (final File file : filesList) {
            if (file.isDirectory()) {
                sendFolder(zipOpStream, file, relativePath);
            } else {
                sendFile(zipOpStream, file, relativePath);
            }
        }
    }

    public static void sendFile(final ZipOutputStream zipOpStream, final File file, final String relativePath) throws Exception {
        final String absolutePath = file.getAbsolutePath();
        String zipEntryFileName = absolutePath;
        final int index = absolutePath.indexOf(relativePath);
        if (absolutePath.startsWith(relativePath)) {
            zipEntryFileName = absolutePath.substring(relativePath.length());
            if (zipEntryFileName.startsWith(File.separator)) {
                zipEntryFileName = zipEntryFileName.substring(1);
            }
            System.out.println("zipEntryFileName:::" + relativePath.length() + "::" + zipEntryFileName);
        } else {
            throw new Exception("Invalid Absolute Path");
        }

        BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file));
        final byte[] fileByte = new byte[MAX_READ_SIZE];
        int readBytes = 0;
        final CRC32 crc = new CRC32();
        while (0 != (readBytes = bis.read(fileByte))) {
            if (-1 == readBytes) {
                break;
            }
            crc.update(fileByte, 0, readBytes);
        }
        bis.close();

        final ZipEntry zipEntry = new ZipEntry(zipEntryFileName);
        zipEntry.setMethod(ZipEntry.STORED);
        zipEntry.setCompressedSize(file.length());
        zipEntry.setSize(file.length());
        zipEntry.setCrc(crc.getValue());
        zipOpStream.putNextEntry(zipEntry);
        bis = new BufferedInputStream(new FileInputStream(file));
        while (0 != (readBytes = bis.read(fileByte))) {
            if (-1 == readBytes) {
                break;
            }
            zipOpStream.write(fileByte, 0, readBytes);
        }
        bis.close();

    }

    public static long calculateFolderSize(final File directory) {
        long length = 0;
        if (directory != null) {
            for (final File file : Objects.requireNonNull(directory.listFiles())) {
                if (file.isFile())
                    length += file.length();
                else
                    length += calculateFolderSize(file);
            }
        }
        return length;
    }

    public static void openFileTransferWindow(final Initializable sStage, final File path, final String ip, final DownloadData downloadData) {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                try {
                    final HashMap<String, String> settings = getSettings();

                    final FXMLLoader fxmlLoader = new FXMLLoader(sStage.getClass().getResource("/pages/file_transfer_window.fxml"));
                    final Parent root1 = fxmlLoader.load();

                    final Stage fStage = new Stage();
                    fStage.setTitle("Datenübertragung");
                    fStage.getIcons().add(new Image(settings.get("defaulticon")));
                    fStage.setResizable(false);
                    fStage.setScene(new Scene(root1, 400, 170));
                    final FileTransferController controller = fxmlLoader.<FileTransferController>getController();
                    controller.initData(path, fStage, ip, downloadData);
                    fStage.show();
                } catch (final IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public static ServerSocket getFreeServerSocket() {
        final Registry registry = new Registry();
        final int startPort = Integer.parseInt(registry.getProperties().get("fileport"));
        final int endPort = startPort + 1000;

        for (int i = startPort; i < endPort; i++) {
            try {
                return new ServerSocket(i);
            } catch (final IOException ignored) {
            }
        }

        return null;
    }
}
