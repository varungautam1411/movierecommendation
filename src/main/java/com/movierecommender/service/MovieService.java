package com.movierecommender.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.movierecommender.model.domain.CustomerMovie;
import com.movierecommender.model.domain.WatchedMovie;
import com.movierecommender.model.request.RatingRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.ArrayList;

@Service
public class MovieService {
    private static final Logger logger = LoggerFactory.getLogger(MovieService.class);
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    private MovieDetailsService movieDetailsService;

    public void updateRating(RatingRequest ratingRequest) {
        String key = "customer:" + ratingRequest.getCustomerId();
        
        try {
            String existingValue = redisTemplate.opsForValue().get(key);
            CustomerMovie customerMovie;

            if (existingValue != null) {
                customerMovie = objectMapper.readValue(existingValue, CustomerMovie.class);
                updateOrAddMovie(customerMovie, ratingRequest);
            } else {
                customerMovie = createNewCustomerRecord(ratingRequest);
            }

            String updatedValue = objectMapper.writeValueAsString(customerMovie);
            redisTemplate.opsForValue().set(key, updatedValue);
            
            logger.info("Successfully updated rating for customer: {}, movie: {}", 
                ratingRequest.getCustomerId(), ratingRequest.getMovieId());

        } catch (Exception e) {
            logger.error("Error updating rating: ", e);
            throw new RuntimeException("Failed to update rating", e);
        }
    }

    public CustomerMovie getCustomerRatings(String customerId) {
        try {
            String key = "customer:" + customerId;
            String value = redisTemplate.opsForValue().get(key);
            if (value != null) {
                return objectMapper.readValue(value, CustomerMovie.class);
            }
            return null;
        } catch (Exception e) {
            logger.error("Error fetching customer ratings: ", e);
            throw new RuntimeException("Failed to fetch customer ratings", e);
        }
    }

    public boolean deleteRating(String customerId, String movieId) {
        try {
            String key = "customer:" + customerId;
            String value = redisTemplate.opsForValue().get(key);
            if (value != null) {
                CustomerMovie customerMovie = objectMapper.readValue(value, CustomerMovie.class);
                boolean removed = customerMovie.getWatch

edMovies().removeIf(
                    movie -> movie.getMovieId().equals(movieId)
                );
                
                if (removed) {
                    String updatedValue = objectMapper.writeValueAsString(customerMovie);
                    redisTemplate.opsForValue().set(key, updatedValue);
                    logger.info("Successfully deleted rating for customer: {}, movie: {}", 
                        customerId, movieId);
                    return true;
                }
            }
            return false;
        } catch (Exception e) {
            logger.error("Error deleting rating: ", e);
            throw new RuntimeException("Failed to delete rating", e);
        }
    }

    private void updateOrAddMovie(CustomerMovie customerMovie, RatingRequest ratingRequest) {
        var existingMovie = customerMovie.getWatchedMovies().stream()
                .filter(m -> m.getMovieId().equals(ratingRequest.getMovieId()))
                .findFirst();

        if (existingMovie.isPresent()) {
            WatchedMovie movie = existingMovie.get();
            if (isMoreRecent(ratingRequest.getDate(), movie.getDate())) {
                movie.setRating(ratingRequest.getRating());
                movie.setDate(ratingRequest.getDate());
            }
        } else {
            WatchedMovie newMovie = createWatchedMovie(ratingRequest);
            customerMovie.getWatchedMovies().add(newMovie);
        }
    }

    private CustomerMovie createNewCustomerRecord(RatingRequest ratingRequest) {
        CustomerMovie customerMovie = new CustomerMovie();
        customerMovie.setCustomerId(ratingRequest.getCustomerId());
        customerMovie.setWatchedMovies(new ArrayList<>());
        WatchedMovie watchedMovie = createWatchedMovie(ratingRequest);
        customerMovie.getWatchedMovies().add(watchedMovie);
        return customerMovie;
    }

    private WatchedMovie createWatchedMovie(RatingRequest ratingRequest) {
        var movieDetails = movieDetailsService.getMovieDetails(ratingRequest.getMovieId());
        WatchedMovie watchedMovie = new WatchedMovie();
        watchedMovie.setMovieId(ratingRequest.getMovieId());
        watchedMovie.setTitle(movieDetails.getTitle());
        watchedMovie.setYearOfRelease(movieDetails.getYearOfRelease());
        watchedMovie.setRating(ratingRequest.getRating());
        watchedMovie.setDate(ratingRequest.getDate());
        return watchedMovie;
    }

    private boolean isMoreRecent(String newDate, String existingDate) {
        try {
            Date date1 = dateFormat.parse(newDate);
            Date date2 = dateFormat.parse(existingDate);
            return date1.after(date2);
        } catch (ParseException e) {
            logger.error("Error parsing dates: {} or {}", newDate, existingDate);
            return false;
        }
    }
}

    
