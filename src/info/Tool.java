package info;

import javafx.collections.ObservableMap;
import javafx.scene.image.ImageView;
import network.Client;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.zip.CRC32;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;


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
                    // TELL THE CLIENT THAT THE DOWNLOAD CAN BEGIN
                    Tool.sendMessage(socket, Info.getDownloadFolderPackage());

                    if (path.exists()) {
                        ZipOutputStream zipOpStream = new ZipOutputStream(socket.getOutputStream());
                        sendFileOutput(zipOpStream, path);
                        zipOpStream.flush();

                        /*File[] files = path.listFiles();

                        BufferedOutputStream bos = new BufferedOutputStream(socket.getOutputStream());
                        DataOutputStream dos = new DataOutputStream(bos);

                        assert files != null;
                        dos.writeInt(files.length);

                        for(File file : files) {
                            System.out.println("PROVIDING " + file.getName());
                        }*/
                    } else {
                        System.out.println("Folder to read does not exist ["+path.getAbsolutePath()+"]");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public static void sendFileOutput(ZipOutputStream zipOpStream, File outFile)
            throws Exception {
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

    public static void downloadFile(Socket socket, File path) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    System.out.println("DOWNLOAD FILE TO " + path.getName());
                    BufferedInputStream bis = new BufferedInputStream(socket.getInputStream());
                    ZipInputStream zips = new ZipInputStream(bis);
                    ZipEntry zipEntry = null;


                    while(null != (zipEntry = zips.getNextEntry())){
                        String fileName = zipEntry.getName();
                        File outFile = new File(path.getAbsolutePath() + "/" + fileName);
                        System.out.println("----["+outFile.getName()+"], filesize["+zipEntry.getCompressedSize()+"]");


                        if(zipEntry.isDirectory()){
                            File zipEntryFolder = new File(zipEntry.getName());
                            if(!zipEntryFolder.exists()){
                                outFile.mkdirs();
                            }

                            continue;
                        }else{
                            File parentFolder = outFile.getParentFile();
                            if(!parentFolder.exists()){
                                parentFolder.mkdirs();
                            }
                        }

                        System.out.println("ZipEntry::"+zipEntry.getCompressedSize());



                        FileOutputStream fos = new FileOutputStream(outFile);
                        int fileLength = (int)zipEntry.getSize();

                        byte[] fileByte = new byte[fileLength];
                        zips.read(fileByte);
                        fos.write(fileByte);
                        fos.close();
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
}
