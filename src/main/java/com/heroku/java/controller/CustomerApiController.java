package com.heroku.java.controller;

import org.springframework.web.bind.annotation.RestController;

import com.heroku.java.bean.Customer;

import jakarta.servlet.http.HttpSession;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.http.ResponseEntity;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

@RestController
public class CustomerApiController {

    @Autowired
    private DataSource dataSource;

    public static final String SESSION_USER_ID = "custid";

    @GetMapping("/api/viewaccount")
    public ResponseEntity<?> viewAccountApi(HttpSession session) {
        Integer custid = (Integer) session.getAttribute(SESSION_USER_ID);
        if (custid == null || custid == 0) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not logged in");
        }
        try (Connection connection = dataSource.getConnection()) {
            Customer customer = getCustomerDetails(connection, custid);
            if (customer == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Customer not found");
            }
            return ResponseEntity.ok(customer);
        } catch (SQLException e) {
            e.printStackTrace(); // Log properly in production
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred");
        }
    }

    private Customer getCustomerDetails(Connection connection, int custid) throws SQLException {
        String customerSql = "SELECT * FROM customer WHERE custid = ?";
        try (PreparedStatement customerStatement = connection.prepareStatement(customerSql)) {
            customerStatement.setInt(1, custid);
            try (ResultSet resultSet = customerStatement.executeQuery()) {
                if (resultSet.next()) {
                    Customer customer = new Customer();
                    customer.setCustid(resultSet.getInt("custid"));
                    customer.setCustname(resultSet.getString("custname"));
                    customer.setCustphoneno(resultSet.getString("custphoneno"));
                    customer.setCustpassword(resultSet.getString("custpassword"));
                    customer.setCustemail(resultSet.getString("custemail"));
                    return customer;
                } else {
                    return null; // Customer not found
                }
            }
        }
    }
}