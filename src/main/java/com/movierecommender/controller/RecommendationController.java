// src/main/java/com/movierecommender/controller/RecommendationController.java
package com.movierecommender.controller;

import com.movierecommender.model.RatingRequest;
import com.movierecommender.model.CustomerMovie;
import com.movierecommender.service.MovieService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/recommendations")
public class RecommendationController {

    @Autowired
    private MovieService movieService;

    @PostMapping("/ratings")
    public ResponseEntity<?> updateRating(@Valid @RequestBody RatingRequest ratingRequest) {
        try {
            movieService.updateRating(ratingRequest);
            return ResponseEntity.ok()
                .body(new ApiResponse(true, "Rating updated successfully", null));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(new ApiResponse(false, "Failed to update rating", e.getMessage()));
        }
    }

    @GetMapping("/ratings/{customerId}")
    public ResponseEntity<?> getCustomerRatings(@PathVariable String customerId) {
        try {
            CustomerMovie customerMovie = movieService.getCustomerRatings(customerId);
            if (customerMovie != null) {
                return ResponseEntity.ok(customerMovie);
            }
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(new ApiResponse(false, "Failed to fetch ratings", e.getMessage()));
        }
    }

    @DeleteMapping("/ratings/{customerId}/{movieId}")
    public ResponseEntity<?> deleteRating(
            @PathVariable String customerId, 
            @PathVariable String movieId) {
        try {
            boolean deleted = movieService.deleteRating(customerId, movieId);
            if (deleted) {
                return ResponseEntity.ok()
                    .body(new ApiResponse(true, "Rating deleted successfully", null));
            }
            return ResponseEntity.notFound()
                .body(new ApiResponse(false, "Rating not found", null));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(new ApiResponse(false, "Failed to delete rating", e.getMessage()));
        }
    }
}

