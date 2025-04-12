// src/main/java/com/movierecommender/model/ApiResponse.java
package com.movierecommender.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ApiResponse {
    private boolean success;
    private String message;
    private String error;
}

