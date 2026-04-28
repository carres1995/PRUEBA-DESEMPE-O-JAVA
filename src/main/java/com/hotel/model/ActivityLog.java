package com.hotel.model;

import com.hotel.model.enums.HttpMethod;

import java.time.LocalDateTime;

/**
 * Represents a single HTTP-style activity log entry.
 * Maps to the {@code activity_logs} table.
 */
public class ActivityLog {

    private Long id;
    private Long userId;
    private HttpMethod httpMethod;
    private String resource;
    private String description;
    private int statusCode;
    private LocalDateTime createdAt;

    public ActivityLog() {}

    public ActivityLog(Long userId, HttpMethod httpMethod, String resource, String description, int statusCode) {
        this.userId = userId;
        this.httpMethod = httpMethod;
        this.resource = resource;
        this.description = description;
        this.statusCode = statusCode;
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public HttpMethod getHttpMethod() { return httpMethod; }
    public void setHttpMethod(HttpMethod httpMethod) { this.httpMethod = httpMethod; }

    public String getResource() { return resource; }
    public void setResource(String resource) { this.resource = resource; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public int getStatusCode() { return statusCode; }
    public void setStatusCode(int statusCode) { this.statusCode = statusCode; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
