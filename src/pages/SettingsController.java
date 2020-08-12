package pages;


import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import start.MainController;

import java.net.URL;
import java.util.HashMap;
import java.util.ResourceBundle;

import static info.Info.getSettings;

public class SettingsController implements Initializable {

    @FXML
    Label labelTextDownloadPath;

    @FXML
    Label labelDownloadPath;

    HashMap<String, String> settings;

    final String descriptionDownloadPath = "Hier werden alle deine Downloads gespeichert";

    @Override
    public void initialize(final URL location, final ResourceBundle resources) {
        this.settings = getSettings();

        this.initData();
    }

    public void initData() {
        this.labelTextDownloadPath.setText(this.descriptionDownloadPath);
        this.labelDownloadPath.setText(this.settings.get("defaultfiletransferpath"));
    }

    public void buttonOk(final ActionEvent event) {
        MainController.settingsStage.close();
    }
}
