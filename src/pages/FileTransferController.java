package pages;

import javafx.fxml.Initializable;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

public class FileTransferController implements Initializable {

    private File path;
    private String ip;
    private int port;
    private long size;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        System.out.println("INITIALIZE");
    }

    public void initData(File path, String ip, int port, long size) {
        System.out.println("INITDATA");
        this.path = path;
        this.ip = ip;
        this.port = port;
        this.size = size;
    }
}
