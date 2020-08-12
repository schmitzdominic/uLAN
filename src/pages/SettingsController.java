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

import static helpers.Info.getSettings;

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
        initData();
    }

    public void initData() {
        labelTextDownloadPath.setText(descriptionDownloadPath);
        labelDownloadPath.setText(settings.get("defaultfiletransferpath"));
    }

    public void buttonChooseDownloadPath(final ActionEvent event) {
        final DirectoryChooser directoryChooser = new DirectoryChooser();
        final File selectedDirectory = directoryChooser.showDialog(MainController.settingsStage);
        if (selectedDirectory != null) {
            final String path = selectedDirectory.getAbsolutePath();
            registry.setSetting("properties", "defaultfiletransferpath", path);
            labelDownloadPath.setText(path);
        }
    }

    public void buttonOk(final ActionEvent event) {
        MainController.settingsStage.close();
    }
}
