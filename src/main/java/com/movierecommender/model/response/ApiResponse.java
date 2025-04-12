package com.movierecommender.model.response;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
@NoArgsConstructor  // Add this annotation
public class ApiResponse {
    private boolean success;
    private String message;
    private String error;
}
