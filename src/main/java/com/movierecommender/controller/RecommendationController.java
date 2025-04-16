package com.movierecommender.controller;

import com.movierecommender.model.request.RatingRequest;
import com.movierecommender.model.response.ApiResponse;
import com.movierecommender.model.response.RecommendationResponse;
import com.movierecommender.model.domain.CustomerMovie;
import com.movierecommender.service.MovieService;
import com.movierecommender.service.RecommendationService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.validation.Valid;

@Component  // Changed from @RestController to @Component
public class RecommendationController {
    private static final Logger logger = LoggerFactory.getLogger(RecommendationController.class);
    
    @Autowired
    private MovieService movieService;

    @Autowired
    private RecommendationService recommendationService;

    public ApiResponse updateRating(@Valid RatingRequest ratingRequest) {
        try {
            movieService.updateRating(ratingRequest);
            return new ApiResponse(true, "Rating updated successfully");
        } catch (Exception e) {
            logger.error("Failed to update rating", e);
            return new ApiResponse(false, "Failed to update rating", e.getMessage());
        }
    }

    public Object getCustomerRatings(String customerId) {
        try {
            CustomerMovie customerMovie = movieService.getCustomerRatings(customerId);
            if (customerMovie != null) {
                return customerMovie;
            }
            return new ApiResponse(false, "No ratings found for customer");
        } catch (Exception e) {
            logger.error("Failed to fetch ratings", e);
            return new ApiResponse(false, "Failed to fetch ratings", e.getMessage());
        }
    }

    public ApiResponse deleteRating(String customerId, String movieId) {
        try {
            boolean deleted = movieService.deleteRating(customerId, movieId);
            if (deleted) {
                return new ApiResponse(true, "Rating deleted successfully");
            }
            return new ApiResponse(false, "Rating not found");
        } catch (Exception e) {
            logger.error("Failed to delete rating", e);
            return new ApiResponse(false, "Failed to delete rating", e.getMessage());
        }
    }

    public Object getRecommendations(String customerId) {
        logger.info("Getting recommendations for customer: {}", customerId);
        try {
            RecommendationResponse recommendations = recommendationService.getRecommendations(customerId);
            if (recommendations.getRecommendedMovies().isEmpty()) {
                return new ApiResponse(true, "No recommendations found for customer", null);
            }
            return recommendations;
        } catch (Exception e) {
            logger.error("Failed to get recommendations", e);
            return new ApiResponse(false, "Failed to get recommendations", e.getMessage());
        }
    }
}
