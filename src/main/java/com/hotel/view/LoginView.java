package com.hotel.view;

import com.hotel.controller.AuthenticationController;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.*;

/**
 * Login screen using JavaFX.
 * Refactored to return its layout for single-stage navigation.
 */
public class LoginView {

    private final AuthenticationController controller;
    private final Runnable onLoginSuccess;

    public LoginView(AuthenticationController controller, Runnable onLoginSuccess) {
        this.controller = controller;
        this.onLoginSuccess = onLoginSuccess;
    }

    /**
     * Builds and returns the login layout.
     */
    public Parent getView() {
        VBox root = new VBox(25);
        root.getStyleClass().add("main-container");
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(50));

        Label title = new Label("HotelNova Login");
        title.getStyleClass().add("title-label");

        VBox form = new VBox(15);
        form.getStyleClass().add("card");
        form.setMaxWidth(400);
        form.setPadding(new Insets(30));

        Label lblUser = new Label("Username");
        TextField txtUser = new TextField();
        txtUser.setPromptText("Enter your username");

        Label lblPass = new Label("Password");
        PasswordField txtPass = new PasswordField();
        txtPass.setPromptText("Enter your password");

        Button btnLogin = new Button("Log In");
        btnLogin.setMaxWidth(Double.MAX_VALUE);
        btnLogin.setOnAction(e -> handleLogin(txtUser.getText(), txtPass.getText()));

        form.getChildren().addAll(lblUser, txtUser, lblPass, txtPass, btnLogin);

        root.getChildren().addAll(title, form);
        return root;
    }

    private void handleLogin(String username, String password) {
        try {
            controller.login(username, password);
            onLoginSuccess.run();
        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Authentication Error");
            alert.setHeaderText(null);
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        }
    }
}
