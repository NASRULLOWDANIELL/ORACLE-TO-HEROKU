package com.heroku.java.controller;

import org.springframework.web.bind.annotation.RestController;

import com.heroku.java.DAO.CustomerDAO;
import com.heroku.java.bean.Customer;
import com.heroku.java.bean.LoginRequest;
import com.heroku.java.bean.LoginResponse;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.http.ResponseEntity;

import java.sql.SQLException;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

@RestController
public class LoginApiController {

    private final DataSource dataSource;

    @Autowired
    public LoginApiController(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @PostMapping("/api/login")
    public ResponseEntity<?> apiLogin(@RequestBody LoginRequest loginRequest) {
        try {
            CustomerDAO customerDAO = new CustomerDAO(dataSource);
            Customer customer = customerDAO.getCustomerByCustemail(loginRequest.getEmail());

            if (customer == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new LoginResponse(false, "Email does not exist"));
            }

            if (!verifyPassword(loginRequest.getPassword(), customer.getCustpassword())) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new LoginResponse(false, "Incorrect password"));
            }

            return ResponseEntity.ok(new LoginResponse(true, "Login successful"));
        } catch (SQLException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new LoginResponse(false, "An error occurred"));
        }
    }

    private boolean verifyPassword(String inputPassword, String storedPassword) {
        // Implement secure password verification here
        return inputPassword.equals(storedPassword); // This is not secure, replace with proper hashing
    }
}