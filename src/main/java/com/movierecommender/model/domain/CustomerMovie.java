package com.movierecommender.model.domain;

import lombok.Data;
import java.util.ArrayList;
import java.util.List;

@Data
public class CustomerMovie {
    private String customerId;
    private List<WatchedMovie> watchedMovies = new ArrayList<>();
}

