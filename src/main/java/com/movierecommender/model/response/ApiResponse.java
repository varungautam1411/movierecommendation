package com.movierecommender.model.response;

public class ApiResponse {
    private boolean success;
    private String message;
    private String error;

    // Default constructor
    public ApiResponse() {
    }

    // Constructor for success response
    public ApiResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
        this.error = null;
    }

    // Constructor for error response
    public ApiResponse(boolean success, String message, String error) {
        this.success = success;
        this.message = message;
        this.error = error;
    }

    // Getters and Setters
    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }
}

    
