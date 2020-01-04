package pages;

import javafx.fxml.Initializable;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ResourceBundle;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class FileTransferController implements Initializable {

    private File path;
    private InetAddress ip;
    private int port;
    private long size;
    private Thread transferThread;

    @Override
    public void initialize(URL location, ResourceBundle resources) {}

    public void initData(File path, String ip, int port, long size) {
        try {
            this.path = path;
            this.ip = InetAddress.getByName(ip);
            this.port = port;
            this.size = size;
            this.transferData().start();
        } catch (UnknownHostException e) {
            // TODO: InetAddress, Error Message that the other side did not exists!
            e.printStackTrace();
        }
    }

    private Thread transferData() {
        this.transferThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Socket socket = new Socket(ip, port);
                    BufferedInputStream bis = new BufferedInputStream(socket.getInputStream());
                    ZipInputStream zips = new ZipInputStream(bis);
                    ZipEntry zipEntry = null;

                    long aSize = 0;

                    while(null != (zipEntry = zips.getNextEntry())){
                        String fileName = zipEntry.getName();
                        File outFile = new File(path.getAbsolutePath() + "/" + fileName);
                        if (!outFile.exists()) {

                            if (createDirectoryIfNotExist(zipEntry, outFile)) {
                                continue;
                            }

                            // System.out.println("----["+outFile.getName()+"], filesize["+zipEntry.getCompressedSize()+"]");
                            // System.out.println("ZipEntry::"+zipEntry.getCompressedSize());
                            // System.out.println("SAVE FILE TO: " + outFile.getAbsolutePath());
                            // System.out.println("FILE EXISTS: " + outFile.exists());

                            aSize += zipEntry.getSize();
                            System.out.println(String.format("PROGRESS: %s/%s", aSize, size));

                            FileOutputStream fos = new FileOutputStream(outFile);
                            int fileLength = (int)zipEntry.getSize();

                            byte[] fileByte = new byte[fileLength];
                            zips.read(fileByte);
                            fos.write(fileByte);
                            fos.close();
                        }
                    }

                    System.out.println("FINISH");
                    socket.close();

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        return this.transferThread;
    }

    private boolean createDirectoryIfNotExist(ZipEntry zipEntry, File outFile) {
        if(zipEntry.isDirectory()){
            File zipEntryFolder = new File(zipEntry.getName());
            if(!zipEntryFolder.exists()){
                outFile.mkdirs();
            }
            return true;

        }else{
            File parentFolder = outFile.getParentFile();
            if(!parentFolder.exists()){
                parentFolder.mkdirs();
            }
            return false;

        }
    }
}
