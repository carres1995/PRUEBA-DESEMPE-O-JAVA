package com.hotel.util;

import com.hotel.dao.ActivityLogDao;
import com.hotel.model.ActivityLog;
import com.hotel.model.enums.HttpMethod;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/**
 * Central application logger.
 *
 * <p>Each log entry is written to three destinations simultaneously:</p>
 * <ol>
 *   <li><b>Console</b> — simulates HTTP traces (e.g. {@code [POST] /rooms → 201})</li>
 *   <li><b>app.log</b> — persistent file using {@code java.util.logging}</li>
 *   <li><b>activity_logs table</b> — database persistence via {@link ActivityLogDao}</li>
 * </ol>
 *
 * <p>Usage:</p>
 * <pre>
 *   AppLogger.log(HttpMethod.POST, "/rooms", "Room 102 registered", 201, userId);
 *   AppLogger.logError(HttpMethod.POST, "/rooms", "Room number already exists", 400, userId);
 * </pre>
 */
public class AppLogger {

    private static final Logger JAVA_LOGGER = Logger.getLogger("HotelNova");
    private static final ActivityLogDao logDao = new ActivityLogDao();
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    static {
        // Configure file handler to write to app.log
        try {
            FileHandler fileHandler = new FileHandler("app.log", true); // append mode
            fileHandler.setFormatter(new SimpleFormatter());
            JAVA_LOGGER.addHandler(fileHandler);
            JAVA_LOGGER.setUseParentHandlers(false); // suppress default console handler from java.util.logging
        } catch (IOException e) {
            System.err.println("⚠️  [AppLogger] Could not initialize app.log: " + e.getMessage());
        }
    }

    private AppLogger() {}

    // =========================================================================
    // PUBLIC API
    // =========================================================================

    /**
     * Logs a successful operation (status 2xx).
     *
     * @param method      HTTP method (GET, POST, PUT, PATCH, DELETE)
     * @param resource    Resource path (e.g. "/rooms", "/reservations/1")
     * @param description Human-readable description of what happened
     * @param statusCode  HTTP-style status code (200, 201, etc.)
     * @param userId      ID of the user performing the action (nullable)
     */
    public static void log(HttpMethod method, String resource, String description, int statusCode, Long userId) {
        String timestamp = LocalDateTime.now().format(FMT);
        String trace = buildTrace(method, resource, description, statusCode, userId, timestamp);

        // 1. Console
        System.out.println(trace);

        // 2. File (app.log)
        JAVA_LOGGER.log(Level.INFO, trace);

        // 3. Database
        persistAsync(userId, method, resource, description, statusCode);
    }

    /**
     * Logs a failed/error operation (status 4xx/5xx).
     */
    public static void logError(HttpMethod method, String resource, String description, int statusCode, Long userId) {
        String timestamp = LocalDateTime.now().format(FMT);
        String trace = buildTrace(method, resource, "[ERROR] " + description, statusCode, userId, timestamp);

        System.err.println(trace);
        JAVA_LOGGER.log(Level.WARNING, trace);
        persistAsync(userId, method, resource, description, statusCode);
    }

    // =========================================================================
    // PRIVATE HELPERS
    // =========================================================================

    /**
     * Formats the log line in HTTP-trace style.
     */
    private static String buildTrace(HttpMethod method, String resource, String description,
                                     int statusCode, Long userId, String timestamp) {
        String user = userId != null ? "user:" + userId : "anonymous";
        return String.format("[%s] %-8s %-30s → [%d] %s | %s",
                timestamp, method.name(), resource, statusCode, description, user);
    }

    /**
     * Persists the log to the database in a fire-and-forget manner.
     * Never throws — logging must not interrupt business operations.
     */
    private static void persistAsync(Long userId, HttpMethod method, String resource,
                                     String description, int statusCode) {
        try {
            ActivityLog log = new ActivityLog(userId, method, resource, description, statusCode);
            logDao.insert(log);
        } catch (Exception e) {
            System.err.println("⚠️  [AppLogger] DB persistence failed: " + e.getMessage());
        }
    }
}
