package com.hotel.util;

import com.hotel.model.Reservation;
import com.hotel.model.Room;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Utility class for exporting hotel data to CSV files.
 *
 * <p>Files are written to the {@code exports/} directory, created automatically
 * if it does not exist. Each export is timestamped to avoid overwriting previous reports.</p>
 */
public class CsvExporter {

    private static final String EXPORTS_DIR = "exports";
    private static final DateTimeFormatter TIMESTAMP_FMT =
            DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
    private static final String SEPARATOR = ",";

    private CsvExporter() {
        // Utility class — prevent instantiation
    }

    // =========================================================================
    // PUBLIC EXPORT METHODS
    // =========================================================================

    /**
     * Exports the full list of rooms to the specified file.
     *
     * @param rooms List of Room objects to export
     * @param file Target file
     * @throws IOException if the file cannot be written
     */
    public static void exportRoomsToFile(List<Room> rooms, java.io.File file) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            // Header
            writer.write("id,number,type,capacity,price_per_night,status,is_active,created_at");
            writer.newLine();

            // Data rows
            for (Room room : rooms) {
                writer.write(buildRoomRow(room));
                writer.newLine();
            }
        }
    }

    /**
     * Exports the full list of rooms to {@code exports/habitaciones_export_<timestamp>.csv}.
     */
    public static String exportRooms(List<Room> rooms) throws IOException {
        ensureExportsDirExists();
        String fileName = "habitaciones_export_" + timestamp() + ".csv";
        Path filePath = Paths.get(EXPORTS_DIR, fileName);
        exportRoomsToFile(rooms, filePath.toFile());
        return filePath.toAbsolutePath().toString();
    }

    /**
     * Exports active reservations to the specified file.
     *
     * @param reservations List of active Reservation objects to export
     * @param file Target file
     * @throws IOException if the file cannot be written
     */
    public static void exportActiveReservationsToFile(List<Reservation> reservations, java.io.File file) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            // Header
            writer.write("id,room_id,guest_id,check_in,check_out,status,total_cost");
            writer.newLine();

            // Data rows
            for (Reservation res : reservations) {
                writer.write(buildReservationRow(res));
                writer.newLine();
            }
        }
    }

    /**
     * Exports active reservations to {@code exports/reservas_activas_<timestamp>.csv}.
     */
    public static String exportActiveReservations(List<Reservation> reservations) throws IOException {
        ensureExportsDirExists();
        String fileName = "reservas_activas_" + timestamp() + ".csv";
        Path filePath = Paths.get(EXPORTS_DIR, fileName);
        exportActiveReservationsToFile(reservations, filePath.toFile());
        return filePath.toAbsolutePath().toString();
    }

    // =========================================================================
    // PRIVATE HELPERS
    // =========================================================================

    /**
     * Builds a CSV row for a Room entity.
     */
    private static String buildRoomRow(Room room) {
        return join(
            safe(room.getId()),
            safe(room.getNumber()),
            safe(room.getType()),
            safe(room.getCapacity()),
            safe(room.getPricePerNight()),
            safe(room.getStatus()),
            room.isActive() ? "ACTIVE" : "INACTIVE",
            safe(room.getCreatedAt())
        );
    }

    /**
     * Builds a CSV row for a Reservation entity.
     */
    private static String buildReservationRow(Reservation res) {
        return join(
            safe(res.getId()),
            safe(res.getRoomId()),
            safe(res.getGuestId()),
            safe(res.getCheckIn()),
            safe(res.getCheckOut()),
            safe(res.getStatus()),
            safe(res.getTotalCost())
        );
    }

    /**
     * Ensures the {@code exports/} directory exists, creating it if necessary.
     */
    private static void ensureExportsDirExists() throws IOException {
        Path dir = Paths.get(EXPORTS_DIR);
        if (!Files.exists(dir)) {
            Files.createDirectories(dir);
        }
    }

    /**
     * Joins values with the CSV separator, wrapping each in quotes to handle commas.
     */
    private static String join(Object... values) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < values.length; i++) {
            if (i > 0) sb.append(SEPARATOR);
            sb.append("\"").append(values[i]).append("\"");
        }
        return sb.toString();
    }

    /**
     * Safely converts a potentially null value to String.
     */
    private static String safe(Object value) {
        return value != null ? value.toString() : "";
    }

    /**
     * Returns the current timestamp formatted for filenames.
     */
    private static String timestamp() {
        return LocalDateTime.now().format(TIMESTAMP_FMT);
    }
}
