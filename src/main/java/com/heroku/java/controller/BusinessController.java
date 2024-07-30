package com.heroku.java.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.heroku.java.bean.Business;
import com.heroku.java.service.BusinessService;

import java.util.List;

@Controller
public class BusinessController {

    private final BusinessService businessService;

    @Autowired
    public BusinessController(BusinessService businessService) {
        this.businessService = businessService;
    }

    @GetMapping("/syujaahBusiness")
    public String syujaahBusiness(Model model) {
        try {
            List<Business> businesses = businessService.fetchBusinesses();
            model.addAttribute("businesses", businesses);
        } catch (Exception e) {
            // Log the error
            e.printStackTrace();
            // Add an error message to the model
            model.addAttribute("error", "Unable to fetch business data. Please try again later.");
        }
        return "businessPage";
    }
}