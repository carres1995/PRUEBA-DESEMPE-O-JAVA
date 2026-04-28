package com.hotel.view;

import com.hotel.controller.RoomController;
import com.hotel.model.Room;
import com.hotel.model.enums.RoomStatus;
import com.hotel.model.enums.RoomType;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;

import java.util.List;

/**
 * View layer for Room management using JavaFX.
 */
public class RoomView {

    private final RoomController controller;
    private TableView<Room> table;
    
    private ComboBox<String> cbNumber;
    private ComboBox<RoomType> cbType;
    private TextField txtCapacity;
    private TextField txtPrice;
    private ComboBox<RoomStatus> cbStatus;
    private CheckBox chkActive;
    
    private Room selectedRoom;

    public RoomView(RoomController controller) {
        this.controller = controller;
    }

    /**
     * Builds and returns the Room Management layout.
     */
    public Parent getView() {
        VBox root = new VBox(20);
        root.getStyleClass().add("main-container");
        root.setPadding(new Insets(20));

        // --- Header with Back Button ---
        HBox header = new HBox(20);
        header.setAlignment(Pos.CENTER_LEFT);
        
        Button btnBack = new Button("← Back");
        btnBack.getStyleClass().add("button-secondary");
        btnBack.setOnAction(e -> MainView.showDashboard());
        
        Label title = new Label("Room Management");
        title.getStyleClass().add("title-label");
        
        header.getChildren().addAll(btnBack, title);

        // --- Form Card ---
        GridPane form = new GridPane();
        form.getStyleClass().add("card");
        form.setHgap(15);
        form.setVgap(15);

        cbNumber = new ComboBox<>();
        cbNumber.setEditable(true);
        cbNumber.setPromptText("Select or type number");
        cbNumber.setPrefWidth(200);

        cbType = new ComboBox<>(FXCollections.observableArrayList(RoomType.values()));
        txtCapacity = new TextField();
        txtPrice = new TextField();
        cbStatus = new ComboBox<>(FXCollections.observableArrayList(RoomStatus.values()));
        chkActive = new CheckBox("Is Active");
        chkActive.setSelected(true);

        form.add(new Label("Room Number:"), 0, 0);
        form.add(cbNumber, 1, 0);
        form.add(new Label("Type:"), 2, 0);
        form.add(cbType, 3, 0);

        form.add(new Label("Capacity:"), 0, 1);
        form.add(txtCapacity, 1, 1);
        form.add(new Label("Price/Night:"), 2, 1);
        form.add(txtPrice, 3, 1);

        form.add(new Label("Status:"), 0, 2);
        form.add(cbStatus, 1, 2);
        form.add(chkActive, 3, 2);

        // --- Buttons ---
        HBox actions = new HBox(15);
        actions.setAlignment(Pos.CENTER_LEFT);
        
        Button btnSave = new Button("Save Room");
        Button btnClear = new Button("Clear");
        btnClear.getStyleClass().add("button-secondary");
        
        actions.getChildren().addAll(btnSave, btnClear);

        // --- Table ---
        table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        
        TableColumn<Room, String> colNum = new TableColumn<>("Number");
        colNum.setCellValueFactory(new PropertyValueFactory<>("number"));
        
        TableColumn<Room, RoomType> colType = new TableColumn<>("Type");
        colType.setCellValueFactory(new PropertyValueFactory<>("type"));
        
        TableColumn<Room, Integer> colCap = new TableColumn<>("Capacity");
        colCap.setCellValueFactory(new PropertyValueFactory<>("capacity"));
        
        TableColumn<Room, Double> colPrice = new TableColumn<>("Price");
        colPrice.setCellValueFactory(new PropertyValueFactory<>("pricePerNight"));
        
        TableColumn<Room, RoomStatus> colStatus = new TableColumn<>("Status");
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        
        TableColumn<Room, Boolean> colActive = new TableColumn<>("Active");
        colActive.setCellValueFactory(new PropertyValueFactory<>("active"));

        table.getColumns().addAll(colNum, colType, colCap, colPrice, colStatus, colActive);

        // --- Event Handlers ---
        btnSave.setOnAction(e -> handleSave());
        btnClear.setOnAction(e -> clearForm());
        
        table.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                fillForm(newVal);
            }
        });

        root.getChildren().addAll(header, form, actions, table);
        refreshTable();
        return root;
    }

    private void handleSave() {
        try {
            if (selectedRoom == null) {
                controller.handleSave(
                    cbNumber.getEditor().getText(),
                    cbType.getValue() != null ? cbType.getValue().name() : null,
                    txtCapacity.getText(),
                    txtPrice.getText()
                );
                showSuccess("Room registered successfully");
            } else {
                controller.handleUpdate(
                    selectedRoom.getId(),
                    cbNumber.getEditor().getText(),
                    cbType.getValue() != null ? cbType.getValue().name() : null,
                    txtCapacity.getText(),
                    txtPrice.getText(),
                    cbStatus.getValue() != null ? cbStatus.getValue().name() : null,
                    chkActive.isSelected()
                );
                showSuccess("Room updated successfully");
            }
            clearForm();
            refreshTable();
        } catch (Exception e) {
            showError(e.getMessage());
        }
    }

    private void refreshTable() {
        List<Room> rooms = controller.listByTypeAndStatus(null, null);
        table.setItems(FXCollections.observableArrayList(rooms));
        
        List<String> available = controller.listAvailableNumbers();
        cbNumber.setItems(FXCollections.observableArrayList(available));
    }

    private void fillForm(Room room) {
        selectedRoom = room;
        cbNumber.getEditor().setText(room.getNumber());
        cbNumber.setValue(room.getNumber());
        cbType.setValue(room.getType());
        txtCapacity.setText(String.valueOf(room.getCapacity()));
        txtPrice.setText(String.valueOf(room.getPricePerNight()));
        cbStatus.setValue(room.getStatus());
        chkActive.setSelected(room.isActive());
    }

    private void clearForm() {
        selectedRoom = null;
        cbNumber.getEditor().clear();
        cbNumber.setValue(null);
        cbType.setValue(null);
        txtCapacity.clear();
        txtPrice.clear();
        cbStatus.setValue(null);
        chkActive.setSelected(true);
    }

    private void showSuccess(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Success");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Business Error");
        alert.setHeaderText("Validation Failed");
        alert.setContentText(message);
        alert.showAndWait();
    }
}
