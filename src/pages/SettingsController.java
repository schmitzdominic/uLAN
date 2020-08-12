package pages;


import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import start.MainController;

import java.net.URL;
import java.util.HashMap;
import java.util.ResourceBundle;

import static info.Info.getSettings;

public class SettingsController implements Initializable {

    HashMap<String, String> settings;

    @Override
    public void initialize(final URL location, final ResourceBundle resources) {
        this.settings = getSettings();
    }

    public void initData() {

    }

    public void buttonOk(final ActionEvent event) {
        MainController.settingsStage.close();
    }
}
