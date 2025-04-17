package com.movierecommender.model.request;

import com.fasterxml.jackson.annotation.JsonProperty;

public class RatingRequest {
    @JsonProperty("movieId")  // Changed from "movie-id" to "movieId"
    private String movieId;

    @JsonProperty("customerId")  // Changed from "customer-id" to "customerId"
    private String customerId;

    @JsonProperty("rating")
    private int rating;

    @JsonProperty("date")
    private String date;

    @JsonProperty("title")
    private String title;

    @JsonProperty("yearOfRelease")  // Changed from "yearOfRealease" to "yearOfRelease"
    private Integer yearOfRelease;


    public String getTitle() {
        return title;
    }

    public int getYearOfRelease() {
        return yearOfRelease;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setYearOfRelease(int yearOfRelease) {
        this.yearOfRelease = yearOfRelease;
    }

    // Getters and Setters
    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public String getMovieId() {
        return movieId;
    }

    public void setMovieId(String movieId) {
        this.movieId = movieId;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}

    
