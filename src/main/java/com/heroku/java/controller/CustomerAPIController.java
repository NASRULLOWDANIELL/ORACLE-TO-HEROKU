package com.heroku.java.controller;

import com.heroku.java.bean.Customer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@RestController
@RequestMapping("/api")
public class CustomerAPIController {

    @Autowired
    private DataSource dataSource;

    @GetMapping("/viewaccount")
    public ResponseEntity<?> viewProfile(@RequestHeader("Authorization") String token) {
        int custId = validateTokenAndGetCustomerId(token);
        if (custId == 0) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid or expired token");
        }

        try (Connection connection = dataSource.getConnection()) {
            Customer customer = getCustomerDetails(connection, custId);
            if (customer == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Customer not found");
            }
            return ResponseEntity.ok(customer);
        } catch (SQLException e) {
            e.printStackTrace(); // Log or handle exception properly
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred");
        }
    }

    private int validateTokenAndGetCustomerId(String token) {
        if (token != null && token.startsWith("Bearer ")) {
            String actualToken = token.substring(7);
            // Simple example: token is "user_<id>"
            if (actualToken.startsWith("user_")) {
                try {
                    return Integer.parseInt(actualToken.substring(5));
                } catch (NumberFormatException e) {
                    return 0;
                }
            }
        }
        return 0;
    }

    private Customer getCustomerDetails(Connection connection, int custId) throws SQLException {
        String query = "SELECT custid, custname, custemail, custpassword, custphoneno FROM customers WHERE custid = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, custId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new Customer(
                        rs.getInt("custid"),
                        rs.getString("custname"),
                        rs.getString("custemail"),
                        rs.getString("custpassword"),
                        rs.getString("custphoneno")
                    );
                }
            }
        }
        return null;
    }
}