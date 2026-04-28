package com.hotel.view;

import com.hotel.config.DatabaseInitializer;
import com.hotel.controller.*;
import com.hotel.dao.*;
import com.hotel.service.*;
import com.hotel.util.CsvExporter;
import com.hotel.util.UserSession;
import com.hotel.model.Reservation;
import com.hotel.model.Room;
import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Main application entry point. 
 * Acts as the Central Stage Manager to handle Scene switching.
 */
public class MainView extends Application {

    private static Stage primaryStage;
    private static MainView instance;

    private RoomController roomController;
    private UserController userController;
    private AuthenticationController authController;
    private GuestController guestController;
    private ReservationController reservationController;

    @Override
    public void start(Stage stage) {
        instance = this;
        primaryStage = stage;
        
        // Initialize database schema
        DatabaseInitializer.initialize();

        // Dependency injection chain
        UserDao userDao = new UserDao();
        RoomDao roomDao = new RoomDao();
        GuestDao guestDao = new GuestDao();
        ReservationDao reservationDao = new ReservationDao();

        UserService userService = new UserService(userDao);
        AuthenticationService authService = new AuthenticationService(userDao);
        RoomService roomService = new RoomService(roomDao);
        GuestService guestService = new GuestService(guestDao);
        ReservationService reservationService = new ReservationService(reservationDao, roomDao, guestDao);

        userController = new UserController(userService);
        authController = new AuthenticationController(authService);
        roomController = new RoomController(roomService);
        guestController = new GuestController(guestService);
        reservationController = new ReservationController(reservationService, roomService, guestService);

        // Start with Login
        showLogin();
    }

    /**
     * Switches the current scene of the application.
     */
    public static void setScene(Parent root, String title) {
        Scene scene = new Scene(root, 1000, 800);
        if (MainView.class.getResource("/styles.css") != null) {
            scene.getStylesheets().add(MainView.class.getResource("/styles.css").toExternalForm());
        }
        primaryStage.setTitle("HotelNova — " + title);
        primaryStage.setScene(scene);
        primaryStage.centerOnScreen();
        primaryStage.show();
    }

    public static void showLogin() {
        LoginView loginView = new LoginView(instance.authController, MainView::showDashboard);
        setScene(loginView.getView(), "Login");
    }

    public static void showDashboard() {
        Label title = new Label("hotelNova — Dashboard");
        title.getStyleClass().add("title-label");

        Label welcome = new Label("Welcome, " + UserSession.getCurrentUser().getUsername() + 
                                  " [" + UserSession.getCurrentUser().getRole() + "]");
        welcome.setStyle("-fx-text-fill: #bac2de; -fx-font-size: 16px;");

        Button btnRooms = new Button("Manage Rooms");
        btnRooms.setMaxWidth(300);
        btnRooms.setOnAction(e -> setScene(new RoomView(instance.roomController).getView(), "Room Management"));

        Button btnGuests = new Button("Manage Guests");
        btnGuests.setMaxWidth(300);
        btnGuests.setOnAction(e -> setScene(new GuestView(instance.guestController).getView(), "Guest Management"));

        Button btnReservations = new Button("Manage Reservations");
        btnReservations.setMaxWidth(300);
        btnReservations.setStyle("-fx-background-color: #fab387; -fx-text-fill: #1e1e2e;");
        btnReservations.setOnAction(e -> setScene(new ReservationView(instance.reservationController).getView(), "Reservation Management"));

        VBox root = new VBox(25, title, welcome, btnRooms, btnGuests, btnReservations);

        // Admin-only features
        if (UserSession.isAdmin()) {
            Button btnUsers = new Button("Manage Users");
            btnUsers.setMaxWidth(300);
            btnUsers.setOnAction(e -> setScene(new UserView(instance.userController).getView(), "User Management"));
            root.getChildren().add(4, btnUsers);
        }

        Button btnExport = new Button("Export CSV");
        btnExport.setMaxWidth(300);
        btnExport.setStyle("-fx-background-color: #a6e3a1; -fx-text-fill: #1e1e2e;");
        btnExport.setOnAction(e -> handleExportCsv());
        root.getChildren().add(btnExport);

        Button btnLogout = new Button("Log Out");
        btnLogout.getStyleClass().add("button-secondary");
        btnLogout.setMaxWidth(300);
        btnLogout.setOnAction(e -> {
            UserSession.logout();
            showLogin();
        });
        root.getChildren().add(btnLogout);

        root.setAlignment(Pos.CENTER);
        root.getStyleClass().add("main-container");

        setScene(root, "Premium Dashboard");
    }

    public static void main(String[] args) {
        launch(args);
    }

    /**
     * Handles CSV export for rooms and active reservations.
     * Invoked by the "Export CSV" button on the dashboard.
     */
    private static void handleExportCsv() {
        try {
            // 1. Save Room Report
            FileChooser roomChooser = new FileChooser();
            roomChooser.setTitle("Save Room Report");
            roomChooser.setInitialFileName("hotel_rooms_report.csv");
            roomChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
            File roomFile = roomChooser.showSaveDialog(primaryStage);

            if (roomFile != null) {
                List<Room> rooms = instance.roomController.listByTypeAndStatus(null, null);
                CsvExporter.exportRoomsToFile(rooms, roomFile);
            }

            // 2. Save Active Reservations Report
            FileChooser resChooser = new FileChooser();
            resChooser.setTitle("Save Active Reservations Report");
            resChooser.setInitialFileName("active_reservations_report.csv");
            resChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
            File resFile = resChooser.showSaveDialog(primaryStage);

            if (resFile != null) {
                List<Reservation> reservations = instance.reservationController.listActive();
                CsvExporter.exportActiveReservationsToFile(reservations, resFile);
            }

            if (roomFile != null || resFile != null) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Export Finished");
                alert.setHeaderText("Success");
                alert.setContentText("The requested reports have been saved successfully.");
                alert.showAndWait();
            }

        } catch (IOException e) {
            Alert error = new Alert(Alert.AlertType.ERROR);
            error.setTitle("Export Failed");
            error.setHeaderText("File Error");
            error.setContentText("Could not save the file: " + e.getMessage());
            error.showAndWait();
        } catch (Exception e) {
            Alert error = new Alert(Alert.AlertType.ERROR);
            error.setTitle("Export Failed");
            error.setHeaderText("System Error");
            error.setContentText("An unexpected error occurred: " + e.getMessage());
            error.showAndWait();
        }
    }
}
