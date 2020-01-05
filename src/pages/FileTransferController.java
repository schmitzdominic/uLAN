package pages;

import info.Tool;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicLong;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class FileTransferController implements Initializable {

    @FXML
    public ProgressBar progressBar;

    @FXML
    public ProgressBar progressBarFile;

    @FXML
    public Label labelFolderName;

    @FXML
    public Label labelTimeLeft;

    @FXML
    public Label labelActualFolderSize;

    @FXML
    public Label labelFolderSize;

    @FXML
    public Label labelPercent;

    @FXML
    public Label labelPercentFile;

    @FXML
    public Label labelFileName;

    @FXML
    public Label labelDataPerSecond;

    @FXML
    public Label labelTime;

    private File path;
    private Stage stage;
    private InetAddress ip;
    private Socket socket;
    private Thread transferThread;
    private String folderName;
    private String id;
    private String serverPath;
    private int port;
    private long size;
    private double aSize = 0;
    private boolean initError = false;

    @Override
    public void initialize(URL location, ResourceBundle resources) {}

    public void initData(File path, Stage stage, String ip, Map<String, String> info) {
        try {
            this.path = path;
            this.stage = stage;
            this.ip = InetAddress.getByName(ip);
            this.id = info.get("ID");
            this.serverPath = info.get("FOLDERNAME");
            this.port = Integer.parseInt(info.get("PORT"));
            this.folderName = info.get("FOLDERNAME");
            this.size = Long.parseLong(info.get("SIZE"));

            this.initListeners();

            this.labelFolderName.setText(this.folderName);
            this.labelFolderSize.setText(Tool.humanReadableByteCountSI(size));
            this.progressBar.setProgress(0);

        } catch (UnknownHostException e) {
            // TODO: InetAddress, Error Message that the other side did not exists!
            e.printStackTrace();
            this.initError = true;
        } catch (NumberFormatException e) {
            System.out.println("PORT OR SIZE IS NOT A INTEGER OR LONG NUMBER! PORT:" + info.get("PORT") + " SIZE:" + info.get("SIZE"));
            // TODO: ERROR, PORT IS NOT A INTEGER NUMBER!
            this.initError = true;
        }

        if (!this.initError) {
            this.transferData().start();
        }
    }

    public void initListeners() {
        this.stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent event) {
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private Thread transferData() {
        this.transferThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    socket = new Socket(ip, port);
                    BufferedInputStream bis = new BufferedInputStream(socket.getInputStream());
                    ZipInputStream zips = new ZipInputStream(bis);
                    ZipEntry zipEntry = null;

                    Timer time = new Timer();

                    DownloadProgressTask dt = new DownloadProgressTask(labelDataPerSecond, labelTime, size);
                    DownloadProgressTask tl = new DownloadProgressTask(labelTimeLeft, size);

                    time.schedule(dt, 0, 1000);
                    time.schedule(tl, 0, 3000);

                    try {
                        while(null != (zipEntry = zips.getNextEntry())){
                            String fileName = zipEntry.getName();
                            File outFile = new File(path.getAbsolutePath() + "/" + fileName);

                            Platform.runLater(new Runnable() {
                                @Override
                                public void run() {
                                    progressBarFile.setProgress(0);
                                    labelFileName.setText(fileName);
                                }
                            });

                            if (createDirectoryIfNotExist(zipEntry, outFile)) {
                                continue;
                            }

                            FileOutputStream fos = new FileOutputStream(outFile);
                            int fileLength = (int)zipEntry.getSize();

                            byte[] fileByte = new byte[fileLength];

                            double fSize = 0;
                            int readSize;
                            while ((readSize = zips.read(fileByte)) > 0) {
                                fos.write(fileByte, 0, readSize);

                                aSize += readSize;
                                fSize += readSize;
                                final double progress = (double)(aSize/size)*100;
                                final double fileProgress = (double)(fSize/zipEntry.getSize())*100;
                                long finalASize = (long) aSize;
                                Platform.runLater(new Runnable() {
                                    @Override
                                    public void run() {
                                        labelActualFolderSize.setText(Tool.humanReadableByteCountSI(finalASize) + " /");
                                        progressBar.setProgress(progress/100);
                                        progressBarFile.setProgress(fileProgress/100);
                                        labelPercent.setText((int)progress + " %");
                                        labelPercentFile.setText((int)fileProgress + " %");
                                        dt.updateDownloaded(finalASize);
                                        tl.updateDownloaded(finalASize);
                                    }
                                });
                            }
                            fos.close();
                        }
                    } catch (IOException ignored) {

                    } finally {
                        dt.cancel();
                        tl.cancel();
                        socket.close();
                        Tool.removeDownload(id, serverPath);
                        closeWindow();
                    }
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

    private void closeWindow() {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                stage.getOnCloseRequest().handle(new WindowEvent(stage, WindowEvent.WINDOW_CLOSE_REQUEST));
                stage.close();
            }
        });
    }

    static class DownloadProgressTask extends TimerTask
    {
        private Label dataSec;
        private Label time;
        private Label timeLeft;

        private AtomicLong downloaded = new AtomicLong();
        private AtomicLong sinceLastTime = new AtomicLong();
        private AtomicLong dataLeft = new AtomicLong();
        private AtomicLong leftTime = new AtomicLong();

        private long folderSize;

        public DownloadProgressTask (Label dataSec, Label time, long folderSize) {
            this.dataSec = dataSec;
            this.time = time;
            this.folderSize = folderSize;
        }

        public DownloadProgressTask (Label timeLeft, long folderSize) {
            this.timeLeft = timeLeft;
            this.folderSize = folderSize;
        }

        public void run() {
            Platform.runLater(new Runnable() {
                @Override
                public void run() {

                    if (time != null) {
                        time.setText(Tool.humanReadableTime(leftTime.addAndGet(1)));
                    }
                    if (timeLeft != null) {
                        try {
                            long lTime = dataLeft.get() / (sinceLastTime.get() / 3);
                            timeLeft.setText("~ " + Tool.humanReadableTime(lTime));
                        } catch (ArithmeticException e) {
                            // Ignoring
                        }
                    }
                    if (dataSec != null) {
                        dataSec.setText(Tool.humanReadableByteCountSI(sinceLastTime.get()) + "/s");
                    }
                    sinceLastTime.set(0);
                }
            });
        }

        public void updateDownloaded (long newVal) {
            this.sinceLastTime.addAndGet(newVal - this.downloaded.get());
            this.downloaded.set(newVal);
            this.dataLeft.set(this.folderSize - this.downloaded.get());
        }
    }
}
