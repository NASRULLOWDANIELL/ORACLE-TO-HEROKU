package com.heroku.java.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.heroku.java.bean.Business;


import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;



@Service
public class BusinessService {
    private final String API_URL = "https://coop-management-2024-f4cb6cd5cd97.herokuapp.com/api/businesses";

    public List<Business> fetchBusinesses() {
        RestTemplate restTemplate = new RestTemplate();
        String jsonResponse = restTemplate.getForObject(API_URL, String.class);
        
        ObjectMapper objectMapper = new ObjectMapper();
        List<Business> businesses = new ArrayList<>();
        
        try {
            JsonNode root = objectMapper.readTree(jsonResponse);
            JsonNode businessesNode = root.get("businesses");
            
            if (businessesNode != null && businessesNode.isArray()) {
                for (JsonNode businessNode : businessesNode) {
                    String ownerName = businessNode.path("ownerName").asText();
                    String businessType = businessNode.path("businessType").asText();
                    String businessID = businessNode.path("businessID").asText();  // Note the capital "ID"
                    businesses.add(new Business(ownerName, businessType, businessID));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return businesses;
    }
}