package com.hotel.view;

import com.hotel.controller.ReservationController;
import com.hotel.model.Guest;
import com.hotel.model.Reservation;
import com.hotel.model.Room;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.util.StringConverter;

import java.time.LocalDate;
import java.util.List;

/**
 * View layer for Reservation management (Check-in / Check-out).
 */
public class ReservationView {

    private final ReservationController controller;
    private TableView<Reservation> table;
    
    private ComboBox<Room> comboRoom;
    private ComboBox<Guest> comboGuest;
    private DatePicker dpCheckIn;
    private DatePicker dpCheckOut;

    public ReservationView(ReservationController controller) {
        this.controller = controller;
    }

    /**
     * Builds and returns the Reservation Management layout.
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
        
        Label title = new Label("Reservation Management");
        title.getStyleClass().add("title-label");
        
        header.getChildren().addAll(btnBack, title);

        // --- Check-in Form Card ---
        GridPane form = new GridPane();
        form.getStyleClass().add("card");
        form.setHgap(15);
        form.setVgap(15);

        comboRoom = new ComboBox<>();
        comboRoom.setPromptText("Select Room");
        comboRoom.setPrefWidth(200);
        comboRoom.setConverter(new StringConverter<Room>() {
            @Override public String toString(Room room) { return room == null ? "" : "Room " + room.getNumber(); }
            @Override public Room fromString(String string) { return null; }
        });

        comboGuest = new ComboBox<>();
        comboGuest.setPromptText("Select Guest");
        comboGuest.setPrefWidth(200);
        comboGuest.setConverter(new StringConverter<Guest>() {
            @Override public String toString(Guest guest) { return guest == null ? "" : guest.getFirstName() + " " + guest.getLastName(); }
            @Override public Guest fromString(String string) { return null; }
        });

        dpCheckIn = new DatePicker(LocalDate.now());
        dpCheckOut = new DatePicker(LocalDate.now().plusDays(1));

        form.add(new Label("Room:"), 0, 0);
        form.add(comboRoom, 1, 0);
        form.add(new Label("Guest:"), 2, 0);
        form.add(comboGuest, 3, 0);

        form.add(new Label("Check-in:"), 0, 1);
        form.add(dpCheckIn, 1, 1);
        form.add(new Label("Check-out:"), 2, 1);
        form.add(dpCheckOut, 3, 1);

        // --- Buttons ---
        HBox actions = new HBox(15);
        Button btnCheckIn = new Button("Confirm Check-in");
        Button btnRefresh = new Button("Refresh Lists");
        btnRefresh.getStyleClass().add("button-secondary");
        actions.getChildren().addAll(btnCheckIn, btnRefresh);

        // --- Table ---
        table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        
        TableColumn<Reservation, Long> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colId.setMaxWidth(50);

        TableColumn<Reservation, String> colRoom = new TableColumn<>("Room");
        colRoom.setCellValueFactory(new PropertyValueFactory<>("roomNumber"));

        TableColumn<Reservation, String> colGuest = new TableColumn<>("Guest");
        colGuest.setCellValueFactory(new PropertyValueFactory<>("guestName"));

        TableColumn<Reservation, LocalDate> colIn = new TableColumn<>("Check-in");
        colIn.setCellValueFactory(new PropertyValueFactory<>("checkIn"));

        TableColumn<Reservation, LocalDate> colOut = new TableColumn<>("Check-out");
        colOut.setCellValueFactory(new PropertyValueFactory<>("checkOut"));

        TableColumn<Reservation, String> colStatus = new TableColumn<>("Status");
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        table.getColumns().addAll(colId, colRoom, colGuest, colIn, colOut, colStatus);

        // Check-out Button for selected row
        Button btnCheckOut = new Button("Execute Check-out");
        btnCheckOut.setStyle("-fx-background-color: #f38ba8; -fx-text-fill: #1e1e2e;");
        btnCheckOut.setDisable(true);

        // --- Handlers ---
        btnRefresh.setOnAction(e -> refreshData());
        btnCheckIn.setOnAction(e -> handleCheckIn());
        btnCheckOut.setOnAction(e -> handleCheckOut());

        table.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            btnCheckOut.setDisable(newVal == null || !"CHECKIN".equals(newVal.getStatus().name()));
        });

        root.getChildren().addAll(header, form, actions, table, btnCheckOut);
        refreshData();
        return root;
    }

    private void handleCheckIn() {
        try {
            Room room = comboRoom.getValue();
            Guest guest = comboGuest.getValue();
            
            if (room == null || guest == null) {
                showError("Please select both room and guest");
                return;
            }

            controller.handleCheckIn(room.getId(), guest.getId(), dpCheckIn.getValue(), dpCheckOut.getValue());
            showSuccess("Check-in successful!");
            refreshData();
        } catch (Exception e) {
            showError(e.getMessage());
        }
    }

    private void handleCheckOut() {
        Reservation selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) return;

        try {
            controller.handleCheckOut(selected.getId());
            showSuccess("Check-out completed!");
            refreshData();
        } catch (Exception e) {
            showError(e.getMessage());
        }
    }

    private void refreshData() {
        List<Reservation> active = controller.listActive();
        table.setItems(FXCollections.observableArrayList(active));

        List<Room> available = controller.getAvailableRooms();
        comboRoom.setItems(FXCollections.observableArrayList(available));

        List<Guest> activeGuests = controller.getActiveGuests();
        comboGuest.setItems(FXCollections.observableArrayList(activeGuests));
    }

    private void showSuccess(String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, msg);
        alert.setTitle("Success");
        alert.setHeaderText(null);
        alert.showAndWait();
    }

    private void showError(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR, msg);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.showAndWait();
    }
}
