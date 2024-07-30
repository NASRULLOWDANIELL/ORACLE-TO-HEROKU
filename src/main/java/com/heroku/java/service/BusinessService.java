package com.heroku.java.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

@Service
public class BusinessService {
    private final String API_URL = "https://coop-management-2024-f4cb6cd5cd97.herokuapp.com/api/businesses";

    public List<JsonNode> fetchBusinesses() {
        RestTemplate restTemplate = new RestTemplate();
        String jsonResponse = restTemplate.getForObject(API_URL, String.class);
        
        ObjectMapper objectMapper = new ObjectMapper();
        List<JsonNode> businesses = new ArrayList<>();
        
        try {
            JsonNode root = objectMapper.readTree(jsonResponse);
            if (root.isArray()) {
                for (JsonNode businessNode : root) {
                    businesses.add(businessNode);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return businesses;
    }
}