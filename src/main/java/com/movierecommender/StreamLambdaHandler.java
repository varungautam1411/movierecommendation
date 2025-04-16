package com.movierecommender;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.movierecommender.config.LambdaConfig;
import com.movierecommender.controller.RecommendationController;
import com.movierecommender.model.request.RatingRequest;
import com.movierecommender.model.response.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.http.ResponseEntity;

import java.util.HashMap;
import java.util.Map;

public class StreamLambdaHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {
    
    private static final Logger logger = LoggerFactory.getLogger(StreamLambdaHandler.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final AnnotationConfigApplicationContext springContext;
    private static final RecommendationController controller;
    

    static {
        try {
            springContext = new AnnotationConfigApplicationContext();
            springContext.register(LambdaConfig.class);
            springContext.refresh();
            controller = springContext.getBean(RecommendationController.class);
            logger.info("Spring context initialized successfully");
        } catch (Exception e) {
            logger.error("Failed to initialize Spring context", e);
            throw new RuntimeException("Failed to initialize Spring context", e);
        }
    }

    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent input, Context context) {
        logger.info("Lambda function started");
        logger.info("Raw input: {}", input);
    logger.info("Path: {}", input.getPath());
    logger.info("HTTP Method: {}", input.getHttpMethod());
    logger.info("Headers: {}", input.getHeaders());
    logger.info("Query String Parameters: {}", input.getQueryStringParameters());
    logger.info("Path Parameters: {}", input.getPathParameters());
    logger.info("Body: {}", input.getBody());

        try {
            if (input.getPath() == null || input.getHttpMethod() == null) {
                logger.error("Invalid request - path or method is null");
                logger.error("Input details: path={}, method={}, pathParams={}", 
                             input.getPath(), input.getHttpMethod(), input.getPathParameters());
                return createResponse(400, Map.of("message", "Invalid request - path or method is null"));
            }

            if ("OPTIONS".equals(input.getHttpMethod())) {
                return createResponse(200, null);
            }

            String path = input.getPath();
            String httpMethod = input.getHttpMethod();
            Map<String, String> pathParams = input.getPathParameters();

            if (path == null || httpMethod == null) {
                logger.error("Invalid request - path or method is null");
                return createResponse(400, Map.of("message", "Invalid request"));
            }

            Object result;
            try {
                if (path.matches(".*/api/recommendations/ratings")) {
                    if ("POST".equals(httpMethod)) {
                        RatingRequest ratingRequest = objectMapper.readValue(input.getBody(), RatingRequest.class);
                        result = controller.updateRating(ratingRequest);
                        return createResponseFromObject(result);
                    }
                } else if (path.matches(".*/api/recommendations/ratings/.*")) {
                    if ("GET".equals(httpMethod)) {
                        String customerId = pathParams.get("customerId");
                        result = controller.getCustomerRatings(customerId);
                        return createResponseFromObject(result);
                    } else if ("DELETE".equals(httpMethod)) {
                        String customerId = pathParams.get("customerId");
                        String movieId = pathParams.get("movieId");
                        result = controller.deleteRating(customerId, movieId);
                        return createResponseFromObject(result);
                    }
                } else if (path.matches(".*/api/recommendations/movies/.*")) {
                    if ("GET".equals(httpMethod)) {
                        String customerId = pathParams.get("customerId");
                        result = controller.getRecommendations(customerId);
                        return createResponseFromObject(result);
                    }
                }
            } catch (Exception e) {
                logger.error("Error processing request", e);
                return createResponse(500, new ApiResponse(false, "Error processing request: " + e.getMessage()));
            }

            logger.warn("No matching path found for: {}", path);
            return createResponse(404, new ApiResponse(false, "Path not found"));

        } catch (Exception e) {
            logger.error("Error handling request", e);
            return createResponse(500, new ApiResponse(false, "Internal server error: " + e.getMessage()));
        }
    }

    private APIGatewayProxyResponseEvent createResponseFromObject(Object result) {
        if (result instanceof ResponseEntity) {
            ResponseEntity<?> responseEntity = (ResponseEntity<?>) result;
            return createResponse(responseEntity.getStatusCodeValue(), responseEntity.getBody());
        } else if (result instanceof ApiResponse) {
            return createResponse(200, result);
        } else {
            return createResponse(200, result);
        }
    }

    private APIGatewayProxyResponseEvent createResponse(int statusCode, Object body) {
        APIGatewayProxyResponseEvent response = new APIGatewayProxyResponseEvent();
        response.setStatusCode(statusCode);
        response.setHeaders(getCorsHeaders());

        try {
            if (body != null) {
                response.setBody(objectMapper.writeValueAsString(body));
            }
        } catch (Exception e) {
            logger.error("Error creating response", e);
            response.setStatusCode(500);
            response.setBody("{\"message\":\"Error creating response\"}");
        }

        return response;
    }

    private Map<String, String> getCorsHeaders() {
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        headers.put("Access-Control-Allow-Origin", "*");
        headers.put("Access-Control-Allow-Methods", "GET,POST,DELETE,OPTIONS");
        headers.put("Access-Control-Allow-Headers", "Content-Type,X-Amz-Date,Authorization,X-Api-Key,X-Amz-Security-Token");
        return headers;
    }
}

    
