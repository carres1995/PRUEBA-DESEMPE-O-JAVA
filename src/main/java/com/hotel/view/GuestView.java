package com.hotel.view;

import com.hotel.controller.GuestController;
import com.hotel.model.Guest;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;

import java.util.List;

/**
 * View layer for Guest management.
 */
public class GuestView {

    private final GuestController controller;
    private TableView<Guest> table;
    
    private TextField txtFirstName;
    private TextField txtLastName;
    private TextField txtDocument;
    private TextField txtEmail;
    private TextField txtPhone;
    private CheckBox chkActive;
    
    private Guest selectedGuest;

    public GuestView(GuestController controller) {
        this.controller = controller;
    }

    /**
     * Builds and returns the Guest Management layout.
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
        
        Label title = new Label("Guest Management");
        title.getStyleClass().add("title-label");
        
        header.getChildren().addAll(btnBack, title);

        // --- Form Card ---
        GridPane form = new GridPane();
        form.getStyleClass().add("card");
        form.setHgap(15);
        form.setVgap(15);

        txtFirstName = new TextField();
        txtLastName = new TextField();
        txtDocument = new TextField();
        txtEmail = new TextField();
        txtPhone = new TextField();
        chkActive = new CheckBox("Is Active");
        chkActive.setSelected(true);

        form.add(new Label("First Name:"), 0, 0);
        form.add(txtFirstName, 1, 0);
        form.add(new Label("Last Name:"), 2, 0);
        form.add(txtLastName, 3, 0);

        form.add(new Label("Document:"), 0, 1);
        form.add(txtDocument, 1, 1);
        form.add(new Label("Email:"), 2, 1);
        form.add(txtEmail, 3, 1);

        form.add(new Label("Phone:"), 0, 2);
        form.add(txtPhone, 1, 2);
        form.add(chkActive, 3, 2);

        // --- Buttons ---
        HBox actions = new HBox(15);
        Button btnSave = new Button("Save Guest");
        Button btnClear = new Button("Clear");
        btnClear.getStyleClass().add("button-secondary");
        actions.getChildren().addAll(btnSave, btnClear);

        // --- Table ---
        table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        
        TableColumn<Guest, String> colFirst = new TableColumn<>("First Name");
        colFirst.setCellValueFactory(new PropertyValueFactory<>("firstName"));
        
        TableColumn<Guest, String> colLast = new TableColumn<>("Last Name");
        colLast.setCellValueFactory(new PropertyValueFactory<>("lastName"));
        
        TableColumn<Guest, String> colDoc = new TableColumn<>("Document");
        colDoc.setCellValueFactory(new PropertyValueFactory<>("documentNumber"));
        
        TableColumn<Guest, String> colEmail = new TableColumn<>("Email");
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        
        TableColumn<Guest, Boolean> colActive = new TableColumn<>("Active");
        colActive.setCellValueFactory(new PropertyValueFactory<>("active"));

        table.getColumns().addAll(colFirst, colLast, colDoc, colEmail, colActive);

        // --- Context Menu: Deactivate Guest (BR-006) ---
        ContextMenu contextMenu = new ContextMenu();
        MenuItem miDeactivate = new MenuItem("🚫 Deactivate Guest");
        miDeactivate.setOnAction(e -> {
            Guest selected = table.getSelectionModel().getSelectedItem();
            if (selected != null) {
                try {
                    controller.handleDeactivate(selected);
                    showSuccess("Guest deactivated successfully");
                    refreshTable();
                } catch (Exception ex) {
                    showError(ex.getMessage());
                }
            }
        });
        contextMenu.getItems().add(miDeactivate);
        table.setContextMenu(contextMenu);

        // --- Handlers ---
        btnSave.setOnAction(e -> handleSave());
        btnClear.setOnAction(e -> clearForm());
        
        table.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) fillForm(newVal);
        });

        root.getChildren().addAll(header, form, actions, table);
        refreshTable();
        return root;
    }

    private void handleSave() {
        try {
            if (selectedGuest == null) {
                controller.handleSave(
                    txtFirstName.getText(), txtLastName.getText(),
                    txtDocument.getText(), txtEmail.getText(), txtPhone.getText()
                );
                showSuccess("Guest registered successfully");
            } else {
                controller.handleUpdate(
                    selectedGuest.getId(),
                    txtFirstName.getText(), txtLastName.getText(),
                    txtDocument.getText(), txtEmail.getText(), txtPhone.getText(),
                    chkActive.isSelected()
                );
                showSuccess("Guest updated successfully");
            }
            clearForm();
            refreshTable();
        } catch (Exception e) {
            showError(e.getMessage());
        }
    }

    private void refreshTable() {
        List<Guest> guests = controller.listAll();
        table.setItems(FXCollections.observableArrayList(guests));
    }

    private void fillForm(Guest guest) {
        selectedGuest = guest;
        txtFirstName.setText(guest.getFirstName());
        txtLastName.setText(guest.getLastName());
        txtDocument.setText(guest.getDocumentNumber());
        txtEmail.setText(guest.getEmail());
        txtPhone.setText(guest.getPhone());
        chkActive.setSelected(guest.isActive());
    }

    private void clearForm() {
        selectedGuest = null;
        txtFirstName.clear();
        txtLastName.clear();
        txtDocument.clear();
        txtEmail.clear();
        txtPhone.clear();
        chkActive.setSelected(true);
    }

    private void showSuccess(String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, msg);
        alert.setTitle("Success");
        alert.setHeaderText(null);
        alert.showAndWait();
    }

    private void showError(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR, msg);
        alert.setTitle("Validation Error");
        alert.setHeaderText(null);
        alert.showAndWait();
    }
}
