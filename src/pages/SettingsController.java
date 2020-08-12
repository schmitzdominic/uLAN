package pages;


import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.stage.DirectoryChooser;
import registry.Registry;
import start.MainController;

import java.io.File;
import java.net.URL;
import java.util.HashMap;
import java.util.ResourceBundle;

import static info.Info.getSettings;

public class SettingsController implements Initializable {

    @FXML
    Label labelTextDownloadPath;

    @FXML
    Label labelDownloadPath;

    HashMap<String, String> settings = getSettings();
    Registry registry = new Registry();

    final String descriptionDownloadPath = "Hier werden alle deine Downloads gespeichert";

    @Override
    public void initialize(final URL location, final ResourceBundle resources) {
        this.initData();
    }

    public void initData() {
        this.labelTextDownloadPath.setText(this.descriptionDownloadPath);
        this.labelDownloadPath.setText(this.settings.get("defaultfiletransferpath"));
    }

    public void buttonChooseDownloadPath(final ActionEvent event) {
        final DirectoryChooser directoryChooser = new DirectoryChooser();
        final File selectedDirectory = directoryChooser.showDialog(MainController.settingsStage);
        if (selectedDirectory != null) {
            final String path = selectedDirectory.getAbsolutePath();
            this.registry.setSetting("properties", "defaultfiletransferpath", path);
            this.labelDownloadPath.setText(path);
        }
    }

    public void buttonOk(final ActionEvent event) {
        MainController.settingsStage.close();
    }
}
