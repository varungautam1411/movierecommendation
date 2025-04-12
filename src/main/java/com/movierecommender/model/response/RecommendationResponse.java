package com.movierecommender.model.response;

import com.movierecommender.model.domain.WatchedMovie;
import java.util.List;

public class RecommendationResponse {
    private String customerId;
    private List<WatchedMovie> recommendedMovies;
    private String similarCustomerId;

    public RecommendationResponse(String customerId, List<WatchedMovie> recommendedMovies, String similarCustomerId) {
        this.customerId = customerId;
        this.recommendedMovies = recommendedMovies;
        this.similarCustomerId = similarCustomerId;
    }

    // Getters and setters
    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public List<WatchedMovie> getRecommendedMovies() {
        return recommendedMovies;
    }

    public void setRecommendedMovies(List<WatchedMovie> recommendedMovies) {
        this.recommendedMovies = recommendedMovies;
    }

    public String getSimilarCustomerId() {
        return similarCustomerId;
    }

    public void setSimilarCustomerId(String similarCustomerId) {
        this.similarCustomerId = similarCustomerId;
    }
}

    
