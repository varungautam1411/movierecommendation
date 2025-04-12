package com.movierecommender.model.domain;

import lombok.Data;

@Data
public class WatchedMovie {
    private String movieId;
    private String title;
    private int yearOfRelease;
    private int rating;
    private String date;
}

    
