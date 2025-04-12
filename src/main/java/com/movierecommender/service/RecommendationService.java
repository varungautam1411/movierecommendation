package com.movierecommender.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.movierecommender.config.RecommendationConfig;
import com.movierecommender.model.domain.CustomerMovie;
import com.movierecommender.model.domain.CustomerVector;
import com.movierecommender.model.domain.WatchedMovie;
import com.movierecommender.model.response.RecommendationResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class RecommendationService {
    private static final Logger logger = LoggerFactory.getLogger(RecommendationService.class);
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

   

    private CustomerMovie getCustomerMovie(String customerId) {
        try {
            String value = redisTemplate.opsForValue().get("customer:" + customerId);
            if (value != null) {
                return objectMapper.readValue(value, CustomerMovie.class);
            }
            return null;
        } catch (Exception e) {
            logger.error("Error fetching customer {}: ", customerId, e);
            return null;
        }
    }

   

    private CustomerVector createCustomerVector(CustomerMovie customerMovie) {
        CustomerVector vector = new CustomerVector(customerMovie.getCustomerId());
        for (WatchedMovie movie : customerMovie.getWatchedMovies()) {
            vector.addRating(movie.getMovieId(), movie.getRating());
        }
        return vector;
    }

 

    private double calculateCosineSimilarity(CustomerVector vector1, CustomerVector vector2) {
        double dotProduct = 0.0;
        double norm1 = 0.0;
        double norm2 = 0.0;

        // Calculate dot product and norms
        for (Map.Entry<String, Double> entry : vector1.getMovieRatings().entrySet()) {
            String movieId = entry.getKey();
            double rating1 = entry.getValue();
            Double rating2 = vector2.getMovieRatings().get(movieId);

            if (rating2 != null) {
                dotProduct += rating1 * rating2;
            }
            norm1 += rating1 * rating1;
        }

        for (double rating : vector2.getMovieRatings().values()) {
            norm2 += rating * rating;
        }

        // Calculate similarity
        if (norm1 == 0.0 || norm2 == 0.0) {
            return 0.0;
        }

        return dotProduct / (Math.sqrt(norm1) * Math.sqrt(norm2));
    }

   



//<!---------new code to limit vector search on the data set ----->

   @Autowired
    private RecommendationConfig config;

    public RecommendationResponse getRecommendations(String customerId) {
        try {
            // 1. Get customer's watched movies
            CustomerMovie customerMovie = getCustomerMovie(customerId);
            if (customerMovie == null || customerMovie.getWatchedMovies().isEmpty()) {
                logger.info("No watch history found for customer: {}", customerId);
                return new RecommendationResponse(customerId, new ArrayList<>(), null);
            }

            // 2. Create vector for customer
            CustomerVector customerVector = createCustomerVector(customerMovie);

            // 3. Get sample of customers from MemoryDB
            List<CustomerMovie> sampleCustomers = getSampleCustomers(customerId);
            logger.info("Found {} sample customers for comparison", sampleCustomers.size());

            // 4. Find similar customers
            List<Map.Entry<String, Double>> similarCustomers = findSimilarCustomers(customerVector, sampleCustomers);
            
            if (similarCustomers.isEmpty()) {
                logger.info("No similar customers found for customer: {}", customerId);
                return new RecommendationResponse(customerId, new ArrayList<>(), null);
            }

            // 5. Get recommendations based on similar customers
            List<WatchedMovie> recommendations = getRecommendedMovies(
                customerMovie.getWatchedMovies(),
                similarCustomers.stream()
                    .map(entry -> getCustomerMovie(entry.getKey()))
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList())
            );

            String mostSimilarCustomerId = similarCustomers.get(0).getKey();
            return new RecommendationResponse(customerId, recommendations, mostSimilarCustomerId);

        } catch (Exception e) {
            logger.error("Error generating recommendations for customer {}: ", customerId, e);
            throw new RuntimeException("Failed to generate recommendations", e);
        }
    }

    private List<CustomerMovie> getSampleCustomers(String excludeCustomerId) {
        List<CustomerMovie> customers = new ArrayList<>();
        Set<String> keys = redisTemplate.keys("customer:*");
        
        if (keys != null) {
            // Randomly sample keys
            List<String> shuffledKeys = new ArrayList<>(keys);
            Collections.shuffle(shuffledKeys);
            
            int count = 0;
            for (String key : shuffledKeys) {
                if (count >= config.getMaxCustomersToScan()) break;
                
                // Skip the current customer
                if (key.equals("customer:" + excludeCustomerId)) continue;
                
                String value = redisTemplate.opsForValue().get(key);
                try {
                    CustomerMovie customer = objectMapper.readValue(value, CustomerMovie.class);
                    if (!customer.getWatchedMovies().isEmpty()) {
                        customers.add(customer);
                        count++;
                    }
                } catch (Exception e) {
                    logger.error("Error parsing customer data for key {}: ", key, e);
                }
            }
        }
        
        return customers;
    }

    private List<Map.Entry<String, Double>> findSimilarCustomers(
            CustomerVector customerVector,
            List<CustomerMovie> sampleCustomers) {
        
        Map<String, Double> similarities = new HashMap<>();

        for (CustomerMovie other : sampleCustomers) {
            CustomerVector otherVector = createCustomerVector(other);
            double similarity = calculateCosineSimilarity(customerVector, otherVector);
            
            // Only include customers with similarity above threshold
            if (similarity >= config.getMinimumSimilarity()) {
                similarities.put(other.getCustomerId(), similarity);
            }
        }

        return similarities.entrySet().stream()
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .limit(5) // Consider top 5 similar customers
                .collect(Collectors.toList());
    }

    private List<WatchedMovie> getRecommendedMovies(
            List<WatchedMovie> customerMovies,
            List<CustomerMovie> similarCustomers) {
        
        // Get set of movies already watched by customer
        Set<String> watchedMovieIds = customerMovies.stream()
                .map(WatchedMovie::getMovieId)
                .collect(Collectors.toSet());

        // Create a map to track movie ratings across similar customers
        Map<String, Double> avgRatings = new HashMap<>();
        Map<String, Integer> countRatings = new HashMap<>();
        Map<String, WatchedMovie> movieDetails = new HashMap<>();

        // Aggregate ratings from similar customers
        for (CustomerMovie similarCustomer : similarCustomers) {
            for (WatchedMovie movie : similarCustomer.getWatchedMovies()) {
                if (!watchedMovieIds.contains(movie.getMovieId())) {
                    String movieId = movie.getMovieId();
                    avgRatings.merge(movieId, (double) movie.getRating(), Double::sum);
                    countRatings.merge(movieId, 1, Integer::sum);
                    movieDetails.putIfAbsent(movieId, movie);
                }
            }
        }

        // Calculate average ratings and create recommendations
        return avgRatings.entrySet().stream()
                .map(entry -> {
                    WatchedMovie movie = movieDetails.get(entry.getKey());
                    movie.setRating((int) Math.round(entry.getValue() / countRatings.get(entry.getKey())));
                    return movie;
                })
                .sorted((m1, m2) -> Double.compare(m2.getRating(), m1.getRating()))
                .limit(config.getMaxRecommendations())
                .collect(Collectors.toList());
    }


}
