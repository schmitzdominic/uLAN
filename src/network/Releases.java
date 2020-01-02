package network;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableMap;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.Tooltip;
import javafx.util.Callback;

public class Releases {

    private ObservableMap<String, String> releaseMap;
    private ListView<String> releaseList;

    public Releases(ListView<String> releaseList) {
        this.releaseMap = FXCollections.observableHashMap();
        this.releaseList = releaseList;
    }

    public void addReleases(Client client) {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                removeAllReleases(client);
                if (client != null) {
                    for(String key : client.getReleases().keySet()) {
                        releaseList.getItems().add(getListItem(client.getReleases().get(key), key));
                        releaseMap.put(key, client.getReleases().get(key));
                    }
                }
                setTooltip();
            }
        });
    }

    public void removeAllReleases(Client client) {
        this.releaseList.getItems().clear();
        this.releaseMap.clear();
    }

    public void removeRelease(String folder, String path) {
        String listItem = this.getListItem(folder, path);
        int index = 0;
        for(String item : this.releaseList.getItems()) {
            if (item.equals(listItem)) {
                this.releaseList.getItems().remove(index);
                this.releaseMap.remove(folder);
                return;
            }
            index++;
        }
    }

    private String getListItem(String folder, String path) {
        return folder + " | " + path;
    }

    private void setTooltip() {
        this.releaseList.setCellFactory(new Callback<ListView<String>, ListCell<String>>() {
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

    public String getPathFromListItem(String item) {
        if (item != null) {
            if(item.contains(" | ")) {
                return item.split(" | ")[2];
            } else {
                return null;
            }
        }
        return null;
    }
}
