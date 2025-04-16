package com.movierecommender.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "recommendation")
public class RecommendationConfig {
    private int maxCustomersToScan = 100;  // Default value
    private int maxRecommendations = 10;   // Default value
    private double minimumSimilarity = 0.1; // Default value

    // Getters and setters
    public int getMaxCustomersToScan() {
        return maxCustomersToScan;
    }

    public void setMaxCustomersToScan(int maxCustomersToScan) {
        this.maxCustomersToScan = maxCustomersToScan;
    }

    public int getMaxRecommendations() {
        return maxRecommendations;
    }

    public void setMaxRecommendations(int maxRecommendations) {
        this.maxRecommendations = maxRecommendations;
    }

    public double getMinimumSimilarity() {
        return minimumSimilarity;
    }

    public void setMinimumSimilarity(double minimumSimilarity) {
        this.minimumSimilarity = minimumSimilarity;
    }
}
