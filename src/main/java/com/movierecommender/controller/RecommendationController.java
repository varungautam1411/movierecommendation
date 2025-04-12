package com.movierecommender.controller;

import com.movierecommender.model.request.RatingRequest;
import com.movierecommender.model.response.ApiResponse;
import com.movierecommender.model.response.RecommendationResponse;
import com.movierecommender.model.domain.CustomerMovie;
import com.movierecommender.service.MovieService;
import com.movierecommender.service.RecommendationService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/recommendations")
@Tag(name = "Movie Recommendations")
public class RecommendationController {

    @Autowired
    private MovieService movieService;

    @Operation(summary = "Update movie rating")
    @PostMapping("/ratings")
    public ResponseEntity<ApiResponse> updateRating(@Valid @RequestBody RatingRequest ratingRequest) {
        try {
            movieService.updateRating(ratingRequest);
            return ResponseEntity.ok(new ApiResponse(true, "Rating updated successfully"));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(new ApiResponse(false, "Failed to update rating", e.getMessage()));
        }
    }

    @Operation(summary = "Get customer ratings")
    @GetMapping("/ratings/{customerId}")
    public ResponseEntity<Object> getCustomerRatings(@PathVariable String customerId) {
        try {
            CustomerMovie customerMovie = movieService.getCustomerRatings(customerId);
            if (customerMovie != null) {
                return ResponseEntity.ok(customerMovie);
            }
            return ResponseEntity.notFound()
                .build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(new ApiResponse(false, "Failed to fetch ratings", e.getMessage()));
        }
    }

    @Operation(summary = "Delete rating")
    @DeleteMapping("/ratings/{customerId}/{movieId}")
    public ResponseEntity<ApiResponse> deleteRating(
            @PathVariable String customerId, 
            @PathVariable String movieId) {
        try {
            boolean deleted = movieService.deleteRating(customerId, movieId);
            if (deleted) {
                return ResponseEntity.ok(new ApiResponse(true, "Rating deleted successfully"));
            }
            return ResponseEntity.status(404)
                .body(new ApiResponse(false, "Rating not found"));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(new ApiResponse(false, "Failed to delete rating", e.getMessage()));
        }
    }
    @Autowired
private RecommendationService recommendationService;

@Operation(summary = "Get movie recommendations for customer")
@GetMapping("/movies/{customerId}")
public ResponseEntity<?> getRecommendations(@PathVariable String customerId) {
    try {
        RecommendationResponse recommendations = recommendationService.getRecommendations(customerId);
        if (recommendations.getRecommendedMovies().isEmpty()) {
            return ResponseEntity.ok()
                .body(new ApiResponse(true, "No recommendations found for customer", null));
        }
        return ResponseEntity.ok(recommendations);
    } catch (Exception e) {
        return ResponseEntity.internalServerError()
            .body(new ApiResponse(false, "Failed to get recommendations", e.getMessage()));
    }
}

}
