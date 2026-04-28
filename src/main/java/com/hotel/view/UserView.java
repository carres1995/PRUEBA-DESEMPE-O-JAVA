package com.hotel.view;

import com.hotel.controller.UserController;
import com.hotel.model.enums.Role;
import com.hotel.model.User;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;

import java.util.List;

/**
 * User management screen for administrators.
 */
public class UserView {

    private final UserController controller;
    private TableView<User> table;
    
    private TextField txtUsername;
    private PasswordField txtPassword;
    private TextField txtEmail;
    private ComboBox<Role> cbRole;

    public UserView(UserController controller) {
        this.controller = controller;
    }

    /**
     * Builds and returns the User Management layout.
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
        
        Label title = new Label("User Management");
        title.getStyleClass().add("title-label");
        
        header.getChildren().addAll(btnBack, title);

        // --- Form Card ---
        GridPane form = new GridPane();
        form.getStyleClass().add("card");
        form.setHgap(15);
        form.setVgap(15);

        txtUsername = new TextField();
        txtPassword = new PasswordField();
        txtEmail = new TextField();
        cbRole = new ComboBox<>(FXCollections.observableArrayList(Role.values()));

        form.add(new Label("Username:"), 0, 0);
        form.add(txtUsername, 1, 0);
        form.add(new Label("Password:"), 2, 0);
        form.add(txtPassword, 3, 0);
        form.add(new Label("Email:"),4, 0 );
        form.add(txtEmail, 5, 0);
        form.add(new Label("Role:"), 0, 1);
        form.add(cbRole, 1, 1);

        Button btnSave = new Button("Register User");
        btnSave.setOnAction(e -> handleRegister());
        form.add(btnSave, 3, 1);

        // --- Table ---
        table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        
        TableColumn<User, Long> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        
        TableColumn<User, String> colUser = new TableColumn<>("Username");
        colUser.setCellValueFactory(new PropertyValueFactory<>("username"));
        
        TableColumn<User, String> colEmail = new TableColumn<>("Email");
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        
        TableColumn<User, Role> colRole = new TableColumn<>("Role");
        colRole.setCellValueFactory(new PropertyValueFactory<>("role"));
        
        TableColumn<User, Boolean> colActive = new TableColumn<>("Active");
        colActive.setCellValueFactory(new PropertyValueFactory<>("active"));

        table.getColumns().addAll(colId, colUser, colEmail, colRole, colActive);

        // Context Menu for Deactivation
        ContextMenu menu = new ContextMenu();
        MenuItem toggleItem = new MenuItem("Toggle Active/Inactive");
        toggleItem.setOnAction(e -> {
            User selected = table.getSelectionModel().getSelectedItem();
            if (selected != null) {
                controller.handleToggleActive(selected.getId());
                refreshTable();
            }
        });
        menu.getItems().add(toggleItem);
        table.setContextMenu(menu);

        root.getChildren().addAll(header, form, table);
        refreshTable();
        return root;
    }

    private void handleRegister() {
        try {
            controller.handleRegister(
                txtUsername.getText(),
                txtEmail.getText(),
                txtPassword.getText(),
                cbRole.getValue()
            );
            txtUsername.clear();
            txtEmail.clear();
            txtPassword.clear();
            cbRole.setValue(null);
            refreshTable();
            showSuccess("User registered successfully");
        } catch (Exception e) {
            showError(e.getMessage());
        }
    }

    private void refreshTable() {
        List<User> users = controller.listAll();
        table.setItems(FXCollections.observableArrayList(users));
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
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
