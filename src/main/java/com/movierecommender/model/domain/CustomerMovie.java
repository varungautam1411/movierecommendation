package com.movierecommender.model.domain;

import java.util.ArrayList;
import java.util.List;

public class CustomerMovie {
    private String customerId;
    private List<WatchedMovie> watchedMovies = new ArrayList<>();

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public List<WatchedMovie> getWatchedMovies() {
        return watchedMovies;
    }

    public void setWatchedMovies(List<WatchedMovie> watchedMovies) {
        this.watchedMovies = watchedMovies;
    }
}

    
