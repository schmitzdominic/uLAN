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
import javafx.stage.WindowEvent;
import javafx.util.Callback;
import registry.Registry;

import java.net.URL;
import java.util.Arrays;
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
    String[] initReleases;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        this.initListeners();
        this.setButtonIcons();
        this.setReleases();
        this.initReleases = registry.getReleases();
    }

    private void initListeners() {
        if (Tool.releaseStage != null) {
            Tool.releaseStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
                @Override
                public void handle(WindowEvent event) {
                    boolean changed = false;
                    String[] releases = registry.getReleases();
                    if (releases != null) {
                        changed = !Arrays.equals(releases, initReleases);
                    }
                    if (changed) {
                        System.out.println("Something has changed..");
                        // TODO: Send all that something has changed!
                    }
                }
            });
        }

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
        this.listReleases.getItems().addAll(registry.getReleases());
        this.setTooltip();
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

    public void buttonAdd(ActionEvent event) {
        registry.addRelease("C:/PATH/TO/LUCKAX/PATH/TO/LUCKAX/PATH/TO/LUCKAX/PATH/TO/LUCKAX/PATH/TO/LUCKAX/PATH/TO/LUCKAX/PATH");
        registry.addRelease("C:/PATH/TO/LUCKAX/PATH/TO/LUCKAX/PATH/TO/LUCKAX/PATH/TO/LUCKAX/LUCKAX/PATH/TO/LUCKAX");
        registry.addRelease("C:/PATH/TO/LUCKAX/PATH/TO/LUCKAX/PATH/TO/LUCKAX/PATH/TO/LUCKAX/PATH/TO/LUCKAX/PATH/TO/LUCKAX");
        registry.addRelease("C:/PATH/TO/LUCKAX/PATH/TO/LUCKAX/PATH/TO/LUCKAX/PATH/TO/LUCKAX/PATH/TO/LUCKAX/PATH//LUCKAX");
        registry.addRelease("C:/PATH/TO/LUCKAX/PATH/TO/LUCKAX/PATH/TO/LUCKAXAX/PATH/TO/LUCKAX");
        registry.addRelease("C:/PATH/TO/LUCKAX/PATH/TO/LUCKAX/PATH/TO/LUCKAX/PATH/TO/LUCKAX/PATH/TO/LUCKAATH/TO/LUCKAX/PATH/TO/LUCKAX");
        registry.addRelease("C:/PATH/TO/LUCKAX/PATH/TO/LUCKAX/PATH/TO/LUCKAX/PATH/TO/LUCKAX/PATH/TO/LUCKAX/PAPATH/TO/LUCKAX");
        registry.addRelease("C:/PATH/TO/LUCKAX/PATH/TO/LUCKAX/PATH/TO/LUCATH/TO/LUCKAX/PATH/TO/LUCKAX/PATH/TO/LUCKAX");
        registry.addRelease("C:/PATH/TO/LUCKAX/PATH/TO/LUCKAX/PATH/TO/LUCKAO/LUCKAX/PATH/TO/LUCKAX/PATH/TO/LUCKAX");
        registry.addRelease("C:/PATH/TO/LUCKAX/PATH/TO/LUCKAX/PATH/TO/LUCKAX/PATH/TO/LUCATH/TO/LUCKAX/PATH/TO/LUCKAX");
        registry.addRelease("C:/PATH/TO/LUCKAX/PATH/TO/LUCKAX/PATH/TO/LUCKAX/PATH/TO/LUCKAX/PA/LUCKAX");
        registry.addRelease("C:/PATH/TO/LUCKAX//TO/LUCKAX");
        registry.addRelease("C:/PATH/TO/LUCKAX/PATH/TO/LUCPATH/TO/LUCKAX");
        registry.addRelease("C:/PATH/TO/LUCKAX/PATH/TO/LUCKAX/PATH/TO/LUCKAX/PATH/TO/L/PATH/TO/LUCKAX");
        registry.addRelease("C:/PATH/TAX/PATH/TO/LUCKAX");
        registry.addRelease("C:/PATH/TO/LUCKAX/PATH/TO/LUCKAX/PATH/TO/LUCKAX/PATH/TO/LUCKAX/AX/PATH/TO/LUCKAX");
        registry.addRelease("C:/PATH/TO/LUCKAX/PATH/TO/LUCKAX/PATH/TOTO/LUCKAX");
        boolean added = registry.addRelease("C:/PATH/TO/LUCKAX/PATH/TO/LTO/LUCKAX");
        if (added) {
            this.setReleases();
        }
    }

    public void buttonRemove(ActionEvent event) {
        boolean removed = registry.removeRelease(this.release);
        if (removed) {
            this.setReleases();
            String[] relArray = registry.getReleases();
            if (relArray.length == 1 & relArray[0].isEmpty()) {
                this.buttonRemove.setVisible(false);
                this.listReleases.getItems().clear();
            } else {
                this.listReleases.getSelectionModel().select(0);
            }
        }
    }
}
