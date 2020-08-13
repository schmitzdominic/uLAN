package network;

import entities.Release;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableMap;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.Tooltip;
import javafx.util.Callback;

public class Releases {

    private final ObservableMap<String, String> releaseMap;
    private final ListView<String> releaseList;

    public Releases(final ListView<String> releaseList) {
        releaseMap = FXCollections.observableHashMap();
        this.releaseList = releaseList;
    }

    public void addReleases(final Client client) {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                removeAllReleases(client);
                if (client != null) {
                    for (final Release release : client.getReleases()) {
                        releaseList.getItems().add(getListItem(release.getName(), release.getPath()));
                        releaseMap.put(release.getPath(), release.getName());
                    }
                }
                setTooltip();
            }
        });
    }

    public void removeAllReleases(final Client client) {
        releaseList.getItems().clear();
        releaseMap.clear();
    }

    public void removeRelease(final String folder, final String path) {
        final String listItem = getListItem(folder, path);
        int index = 0;
        for (final String item : releaseList.getItems()) {
            if (item.equals(listItem)) {
                releaseList.getItems().remove(index);
                releaseMap.remove(folder);
                return;
            }
            index++;
        }
    }

    private String getListItem(final String folder, final String path) {
        return folder + " | " + path;
    }

    private void setTooltip() {
        releaseList.setCellFactory(new Callback<ListView<String>, ListCell<String>>() {
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

    public String getPathFromListItem(final String item) {
        if (item != null) {
            if (item.contains(" | ")) {
                return item.split(" \\| ")[1];
            } else {
                return null;
            }
        }
        return null;
    }
}
