package com.movierecommender.model.domain;

import java.util.Map;
import java.util.HashMap;

public class CustomerVector {
    private String customerId;
    private Map<String, Double> movieRatings;

    public CustomerVector(String customerId) {
        this.customerId = customerId;
        this.movieRatings = new HashMap<>();
    }

    public void addRating(String movieId, int rating) {
        movieRatings.put(movieId, (double) rating);
    }

    public String getCustomerId() {
        return customerId;
    }

    public Map<String, Double> getMovieRatings() {
        return movieRatings;
    }
}
