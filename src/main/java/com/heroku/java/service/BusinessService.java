package com.heroku.java.service;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.heroku.java.bean.Business;

import java.util.Arrays;
import java.util.List;

@Service
public class BusinessService {
    private final String API_URL = "https://coop-management-2024-f4cb6cd5cd97.herokuapp.com/api/businesses";

    public List<Business> fetchBusinesses() {
        RestTemplate restTemplate = new RestTemplate();
        Business[] businessesArray = restTemplate.getForObject(API_URL, Business[].class);
        return Arrays.asList(businessesArray);
    }
}