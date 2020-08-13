package pages;

import entities.windows.Window;
import helpers.Tool;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.DirectoryChooser;
import javafx.stage.WindowEvent;
import javafx.util.Callback;
import registry.Registry;
import start.MainController;

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
    public void initialize(final URL location, final ResourceBundle resources) {
        initListeners();
        setButtonIcons();
        setReleases();
    }

    private void initListeners() {
        listReleases.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(final ObservableValue<? extends String> observable, final String oldValue, final String value) {
                release = value;
                buttonRemove.setVisible(true);
            }
        });
    }

    private void setButtonIcons() {
        imageButtonAdd.setImage(new Image("/icons/baseline_add_white_18dp.png"));
        imageButtonRemove.setImage(new Image("/icons/baseline_delete_outline_white_18dp.png"));

        Tool.resizeImage(imageButtonAdd);
        Tool.resizeImage(imageButtonRemove);
    }

    private void setReleases() {
        listReleases.getItems().clear();
        if (ReleasesNotEmpty(registry.getReleases())) {
            listReleases.getItems().addAll(registry.getReleases());
            setTooltip();
        }
    }

    private void setTooltip() {
        listReleases.setCellFactory(new Callback<ListView<String>, ListCell<String>>() {
            @Override
            public ListCell<String> call(final ListView<String> param) {
                final Label leadLbl = new Label();
                final Tooltip tooltip = new Tooltip();
                return new ListCell<String>() {
                    @Override
                    public void updateItem(final String item, final boolean empty) {
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

    private boolean ReleasesNotEmpty(final String[] releases) {
        if (releases == null) {
            return false;
        }
        if (releases.length == 0) {
            return false;
        } else if (releases.length == 1 & releases[0].isEmpty()) {
            return false;
        } else {
            return true;
        }
    }

    public void buttonAdd(final ActionEvent event) {
        final DirectoryChooser directoryChooser = new DirectoryChooser();
        final File selectedDirectory = directoryChooser.showDialog(MainController.releaseStage);
        if (selectedDirectory != null) {
            final String path = selectedDirectory.getAbsolutePath();
            final boolean added = registry.addRelease(path);
            if (added) {
                setReleases();
            }
        }
    }

    public void buttonRemove(final ActionEvent event) {
        final boolean removed = registry.removeRelease(release);
        if (removed) {
            setReleases();
            if (!ReleasesNotEmpty(registry.getReleases())) {
                buttonRemove.setVisible(false);
                listReleases.getItems().clear();
            } else {
                listReleases.getSelectionModel().select(0);
            }
        }
    }

    public void buttonOk(final ActionEvent event) {
        Window.RELEASES.getStage().getOnCloseRequest().handle(new WindowEvent(
                Window.RELEASES.getStage(), WindowEvent.WINDOW_CLOSE_REQUEST
        ));
        Window.RELEASES.close();
    }
}
