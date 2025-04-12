package com.movierecommender.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.movierecommender.model.domain.CustomerMovie;
import com.movierecommender.model.domain.WatchedMovie;
import com.movierecommender.model.domain.MovieDetails;
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

    public void updateRating(RatingRequest request) {
        String key = "customer:" + request.getCustomerId();
        
        try {
            String existingValue = redisTemplate.opsForValue().get(key);
            CustomerMovie customerMovie;

            if (existingValue != null) {
                customerMovie = objectMapper.readValue(existingValue, CustomerMovie.class);
                updateOrAddMovie(customerMovie, request);
            } else {
                customerMovie = createNewCustomerRecord(request);
            }

            String updatedValue = objectMapper.writeValueAsString(customerMovie);
            redisTemplate.opsForValue().set(key, updatedValue);
            
            logger.info("Successfully updated rating for customer: {}, movie: {}", 
                request.getCustomerId(), request.getMovieId());

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
                boolean removed = customerMovie.getWatchedMovies().removeIf(
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

    private void updateOrAddMovie(CustomerMovie customerMovie, RatingRequest request) {
        var existingMovie = customerMovie.getWatchedMovies().stream()
                .filter(m -> m.getMovieId().equals(request.getMovieId()))
                .findFirst();

        if (existingMovie.isPresent()) {
            WatchedMovie movie = existingMovie.get();
            if (isMoreRecent(request.getDate(), movie.getDate())) {
                movie.setRating(request.getRating());
                movie.setDate(request.getDate());
            }
        } else {
            WatchedMovie newMovie = createWatchedMovie(request);
            customerMovie.getWatchedMovies().add(newMovie);
        }
    }

    private CustomerMovie createNewCustomerRecord(RatingRequest request) {
        CustomerMovie customerMovie = new CustomerMovie();
        customerMovie.setCustomerId(request.getCustomerId());
        customerMovie.setWatchedMovies(new ArrayList<>());
        WatchedMovie watchedMovie = createWatchedMovie(request);
        customerMovie.getWatchedMovies().add(watchedMovie);
        return customerMovie;
    }

    private WatchedMovie createWatchedMovie(RatingRequest request) {
        MovieDetails movieDetails = movieDetailsService.getMovieDetails(request.getMovieId());
        WatchedMovie watchedMovie = new WatchedMovie();
        watchedMovie.setMovieId(request.getMovieId());
        watchedMovie.setTitle(movieDetails.getTitle());
        watchedMovie.setYearOfRelease(movieDetails.getYearOfRelease());
        watchedMovie.setRating(request.getRating());
        watchedMovie.setDate(request.getDate());
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
