package pages;

import helpers.Tool;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Tooltip;
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
    private String fName;
    private String id;
    private String serverPath;
    private FileOutputStream fos;
    private int port;
    private long size;
    private double aSize = 0;
    private boolean initError = false;
    private boolean downloadError = false;

    @Override
    public void initialize(final URL location, final ResourceBundle resources) {
    }

    public void initData(final File path, final Stage stage, final String ip, final Map<String, String> info) {
        try {
            this.path = path;
            this.stage = stage;
            this.ip = InetAddress.getByName(ip);
            id = info.get("ID");
            serverPath = info.get("FOLDERNAME");
            port = Integer.parseInt(info.get("PORT"));
            fName = Tool.convertReleasesString(info.get("FOLDERNAME")).get(info.get("FOLDERNAME"));
            size = Long.parseLong(info.get("SIZE"));

            initListeners();

            labelFolderSize.setText(Tool.humanReadableByteCountSI(size));
            progressBar.setProgress(0);

        } catch (final UnknownHostException e) {
            // TODO: InetAddress, Error Message that the other side did not exists!
            e.printStackTrace();
            initError = true;
        } catch (final NumberFormatException e) {
            System.out.println("PORT OR SIZE IS NOT A INTEGER OR LONG NUMBER! PORT:" + info.get("PORT") + " SIZE:" + info.get("SIZE"));
            // TODO: ERROR, PORT IS NOT A INTEGER NUMBER!
            initError = true;
        }

        if (!initError) {
            transferData().start();
        }
    }

    public void initListeners() {
        stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(final WindowEvent event) {
                try {
                    socket.close();
                } catch (final IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private Thread transferData() {
        transferThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    socket = new Socket(ip, port);
                    final BufferedInputStream bis = new BufferedInputStream(socket.getInputStream());
                    final ZipInputStream zips = new ZipInputStream(bis);
                    ZipEntry zipEntry = null;

                    final Timer time = new Timer();

                    final DownloadProgressTask dt = new DownloadProgressTask(labelDataPerSecond, labelTime, size);
                    final DownloadProgressTask tl = new DownloadProgressTask(labelTimeLeft, size);

                    time.schedule(dt, 0, 1000);
                    time.schedule(tl, 0, 3000);

                    int counter = 0;

                    try {
                        while (null != (zipEntry = zips.getNextEntry())) {
                            final String fileName = zipEntry.getName();
                            final File outFile = new File(path.getAbsolutePath() + "/" + fileName);

                            if (counter == 0) {
                                fName = getFirstFolderName(fileName);
                            }

                            Platform.runLater(new Runnable() {
                                @Override
                                public void run() {
                                    labelFolderName.setText(fName + " nach " + path);
                                    labelFolderName.setTooltip(new Tooltip(path.getAbsolutePath()));
                                    stage.setTitle(fName + " wird heruntergeladen...");
                                    progressBarFile.setProgress(0);
                                    labelFileName.setText(fileName);
                                }
                            });

                            if (createDirectoryIfNotExist(zipEntry, outFile)) {
                                continue;
                            }

                            fos = new FileOutputStream(outFile);
                            final int fileLength = (int) zipEntry.getSize();

                            final byte[] fileByte = new byte[fileLength];

                            double fSize = 0;
                            int readSize;

                            while ((readSize = zips.read(fileByte)) > 0) {

                                fos.write(fileByte, 0, readSize);

                                aSize += readSize;
                                fSize += readSize;
                                final double progress = (double) (aSize / size) * 100;
                                final double fileProgress = (double) (fSize / zipEntry.getSize()) * 100;
                                final long finalASize = (long) aSize;
                                Platform.runLater(new Runnable() {
                                    @Override
                                    public void run() {
                                        labelActualFolderSize.setText(Tool.humanReadableByteCountSI(finalASize) + " /");
                                        progressBar.setProgress(progress / 100);
                                        progressBarFile.setProgress(fileProgress / 100);
                                        labelPercent.setText((int) progress + " %");
                                        labelPercentFile.setText((int) fileProgress + " %");
                                        dt.updateDownloaded(finalASize);
                                        tl.updateDownloaded(finalASize);
                                    }
                                });
                            }
                            fos.close();
                            counter++;
                        }
                    } catch (final IOException ignored) {
                        downloadError = true;
                    } finally {
                        dt.cancel();
                        tl.cancel();
                        if (fos != null) {
                            fos.close();
                        }
                        if (!socket.isClosed()) {
                            socket.close();
                        }
                        Tool.removeDownload(id, serverPath);
                        closeWindow();

                        if (downloadError) {
                            final File pathToDelete = new File(path.getAbsolutePath() + "\\" + fName);
                            if (pathToDelete.isDirectory()) {
                                deleteDir(pathToDelete);
                            }
                        }
                    }
                } catch (final IOException e) {
                    e.printStackTrace();
                }
            }
        });
        return transferThread;
    }

    private boolean createDirectoryIfNotExist(final ZipEntry zipEntry, final File outFile) {
        if (zipEntry.isDirectory()) {
            final File zipEntryFolder = new File(zipEntry.getName());
            if (!zipEntryFolder.exists()) {
                outFile.mkdirs();
            }
            return true;

        } else {
            final File parentFolder = outFile.getParentFile();
            if (!parentFolder.exists()) {
                parentFolder.mkdirs();
            }
            return false;

        }
    }

    private boolean deleteDir(final File dir) {
        if (dir.isDirectory()) {
            final String[] children = dir.list();
            for (int i = 0; i < children.length; i++) {
                final boolean success = deleteDir(new File(dir, children[i]));

                if (!success) {
                    return false;
                }
            }
        }
        return dir.delete();
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

    private String getFirstFolderName(final String path) {
        if (path.contains("\\")) {
            return path.split("\\\\")[0];
        }
        return "";
    }

    static class DownloadProgressTask extends TimerTask {
        private Label dataSec;
        private Label time;
        private Label timeLeft;

        private final AtomicLong downloaded = new AtomicLong();
        private final AtomicLong sinceLastTime = new AtomicLong();
        private final AtomicLong dataLeft = new AtomicLong();
        private final AtomicLong leftTime = new AtomicLong();

        private final long folderSize;

        public DownloadProgressTask(final Label dataSec, final Label time, final long folderSize) {
            this.dataSec = dataSec;
            this.time = time;
            this.folderSize = folderSize;
        }

        public DownloadProgressTask(final Label timeLeft, final long folderSize) {
            this.timeLeft = timeLeft;
            this.folderSize = folderSize;
        }

        @Override
        public void run() {
            Platform.runLater(new Runnable() {
                @Override
                public void run() {

                    if (time != null) {
                        time.setText(Tool.humanReadableTime(leftTime.addAndGet(1)));
                    }
                    if (timeLeft != null) {
                        try {
                            final long lTime = dataLeft.get() / (sinceLastTime.get() / 3);
                            timeLeft.setText("~ " + Tool.humanReadableTime(lTime));
                        } catch (final ArithmeticException e) {
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

        public void updateDownloaded(final long newVal) {
            sinceLastTime.addAndGet(newVal - downloaded.get());
            downloaded.set(newVal);
            dataLeft.set(folderSize - downloaded.get());
        }
    }
}
