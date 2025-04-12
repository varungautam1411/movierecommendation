package com.movierecommender.model.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import javax.validation.constraints.*;

@Data
public class RatingRequest {
    @NotBlank(message = "Customer ID is required")
    @JsonProperty("customer-id")
    private String customerId;
    
    @NotBlank(message = "Movie ID is required")
    @JsonProperty("movie-id")
    private String movieId;
    
    @Min(1) @Max(5)
    private int rating;
    
    @Pattern(regexp = "\\d{4}-\\d{2}-\\d{2}", message = "Date must be in format YYYY-MM-DD")
    private String date;
}

    
