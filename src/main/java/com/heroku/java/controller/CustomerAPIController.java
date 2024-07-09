package com.heroku.java.controller;

import org.springframework.web.bind.annotation.RestController;

import com.heroku.java.bean.Customer;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.http.ResponseEntity;
import org.springframework.beans.factory.annotation.Autowired;

import javax.sql.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

@RestController
public class CustomerAPIController {
    private static final Logger LOGGER = Logger.getLogger(customerController.class.getName());

    private final DataSource dataSource;

    @Autowired
    public CustomerAPIController(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    // ... existing code ...

    @GetMapping("/api/customers")
    public ResponseEntity<List<Customer>> listCustomers() {
        List<Customer> customers = new ArrayList<>();

        try (Connection connection = this.dataSource.getConnection()) {
            String sql = "SELECT * FROM customer";
            try (PreparedStatement statement = connection.prepareStatement(sql);
                    ResultSet resultSet = statement.executeQuery()) {

                while (resultSet.next()) {
                    Customer customer = new Customer();
                    customer.setCustid(resultSet.getInt("custid"));
                    customer.setCustname(resultSet.getString("custname"));
                    customer.setCustphoneno(resultSet.getString("custphoneno"));
                    customer.setCustemail(resultSet.getString("custemail"));
                    // Don't include the password in the response
                    customers.add(customer);
                }
            }
            return ResponseEntity.ok(customers);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error retrieving customer list: " + e.getMessage(), e);
            return ResponseEntity.status(500).body(null);
        }
    }

    // ... existing code ...
}