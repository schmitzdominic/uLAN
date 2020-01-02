package pages;

import info.Tool;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.DirectoryChooser;
import javafx.stage.WindowEvent;
import javafx.util.Callback;
import registry.Registry;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

public class ReleaseController implements Initializable {

    @FXML
    public Button buttonAdd;

    @FXML
    public Button buttonRemove;

    @FXML
    public ImageView imageButtonAdd;

    @FXML
    public ImageView imageButtonRemove;

    @FXML
    public ListView<String> listReleases;

    Registry registry = new Registry();
    String release;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        this.initListeners();
        this.setButtonIcons();
        this.setReleases();
    }

    private void initListeners() {
        this.listReleases.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String value) {
                release = value;
                buttonRemove.setVisible(true);
            }
        });
    }

    private void setButtonIcons() {
        this.imageButtonAdd.setImage(new Image("/icons/baseline_add_white_18dp.png"));
        this.imageButtonRemove.setImage(new Image("/icons/baseline_delete_outline_white_18dp.png"));

        Tool.resizeImage(this.imageButtonAdd);
        Tool.resizeImage(this.imageButtonRemove);
    }

    private void setReleases() {
        this.listReleases.getItems().clear();
        if (isReleasesNotEmpty(registry.getReleases())) {
            this.listReleases.getItems().addAll(registry.getReleases());
            this.setTooltip();
        }
    }

    private void setTooltip() {
        this.listReleases.setCellFactory(new Callback<ListView<String>, ListCell<String>>() {
            @Override
            public ListCell<String> call(ListView<String> param) {
                final Label leadLbl = new Label();
                final Tooltip tooltip = new Tooltip();
                return new ListCell<String>() {
                    @Override
                    public void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);
                        if (item != null) {
                            leadLbl.setText(item);
                            setText(item);
                            tooltip.setText(item);
                            setTooltip(tooltip);
                        }
                    }
                };
            }
        });
    }

    private boolean isReleasesNotEmpty(String[] releases) {
        if (releases.length == 0) {
            return false;
        } else if (releases.length == 1 & releases[0].isEmpty()) {
            return false;
        } else {
            return true;
        }
    }

    public void buttonAdd(ActionEvent event) {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        File selectedDirectory = directoryChooser.showDialog(Tool.releaseStage);
        String path = selectedDirectory.getAbsolutePath();
        if (path.contains(" ")) {
            // TODO: WARNING MESSAGE, NO SPACES INSIDE THE PATH!
        } else {
            boolean added = registry.addRelease(path);
            if (added) {
                this.setReleases();
            }
        }
    }

    public void buttonRemove(ActionEvent event) {
        boolean removed = registry.removeRelease(this.release);
        if (removed) {
            this.setReleases();
            if (!isReleasesNotEmpty(registry.getReleases())) {
                this.buttonRemove.setVisible(false);
                this.listReleases.getItems().clear();
            } else {
                this.listReleases.getSelectionModel().select(0);
            }
        }
    }

    public void buttonOk(ActionEvent event) {
        Tool.releaseStage.getOnCloseRequest().handle(new WindowEvent(
                Tool.releaseStage, WindowEvent.WINDOW_CLOSE_REQUEST
        ));
        Tool.releaseStage.close();
    }
}
