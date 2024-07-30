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
        
        System.out.println("Raw JSON response: " + jsonResponse);
        
        ObjectMapper objectMapper = new ObjectMapper();
        List<Business> businesses = new ArrayList<>();
        
        try {
            JsonNode root = objectMapper.readTree(jsonResponse);
            JsonNode businessesNode = root.get("businesses");
            
            if (businessesNode != null && businessesNode.isArray()) {
                for (JsonNode businessNode : businessesNode) {
                    String ownerName = businessNode.path("ownerName").asText();
                    String businessType = businessNode.path("businessType").asText();
                    String businessId = businessNode.path("businessId").asText();
                    
                    System.out.println("Parsed business: ownerName=" + ownerName + 
                                       ", businessType=" + businessType + 
                                       ", businessId=" + businessId);
                    
                    businesses.add(new Business(ownerName, businessType, businessId));
                }
            }
        } catch (Exception e) {
            System.err.println("Error parsing JSON: " + e.getMessage());
            e.printStackTrace();
        }
        
        return businesses;
    }
}