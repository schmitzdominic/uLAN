package start;

import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.event.ActionEvent;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.*;

import java.util.ArrayList;
import java.util.Arrays;

public class MainController {

    @FXML
    AnchorPane mainWindow;

    @FXML
    TableView<String> clientTable;

    @FXML
    TableColumn<String, String> clientList;

    @FXML
    private void initialize() {
        this.clientTable = new TableView<>(FXCollections.observableArrayList(
           new ArrayList<String>(Arrays.asList(new String[]{
                   "A","B","C"
           }))
        ));
        this.clientList = new TableColumn<>("string");
        this.clientList.setCellValueFactory((p) -> {
            return new ReadOnlyStringWrapper(p.getValue());
        });
        this.clientTable.getColumns().add(this.clientList);
    }

    public void pressReload(ActionEvent event) {
        System.out.println("Hello World");
    }
}
