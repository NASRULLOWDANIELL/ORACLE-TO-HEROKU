package com.heroku.java.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

@RestController
@RequestMapping("/api/admin")
public class AdminDashboardRestController {

    private static final Logger LOGGER = Logger.getLogger(AdminDashboardRestController.class.getName());

    @Autowired
    private DataSource dataSource;

    @GetMapping("/dashboard")
    public ResponseEntity<?> getAdminDashboardData() {
        try (Connection connection = dataSource.getConnection()) {
            Map<String, Object> dashboardData = new HashMap<>();
            
            dashboardData.put("staffList", getAllStaff(connection));
            dashboardData.put("bookingList", getAllBookings(connection));
            dashboardData.put("feedbackList", getAllFeedback(connection));
            dashboardData.put("catList", getAllCats(connection));

            return ResponseEntity.ok(dashboardData);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error fetching data for admin dashboard", e);
            return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("An error occurred while loading the dashboard data.");
        }
    }

    private List<Map<String, Object>> getAllStaff(Connection connection) throws SQLException {
        List<Map<String, Object>> staffList = new ArrayList<>();
        String query = "SELECT * FROM staff";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                Map<String, Object> staff = new HashMap<>();
                staff.put("id", rs.getLong("id"));
                staff.put("name", rs.getString("name"));
                staff.put("role", rs.getString("role"));
                staffList.add(staff);
            }
        }
        return staffList;
    }

    private List<Map<String, Object>> getAllBookings(Connection connection) throws SQLException {
        List<Map<String, Object>> bookingList = new ArrayList<>();
        String query = "SELECT * FROM booking";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                Map<String, Object> booking = new HashMap<>();
                booking.put("id", rs.getLong("id"));
                booking.put("bookingTime", rs.getTimestamp("booking_time"));
                booking.put("customerName", rs.getString("customer_name"));
                bookingList.add(booking);
            }
        }
        return bookingList;
    }

    private List<Map<String, Object>> getAllFeedback(Connection connection) throws SQLException {
        List<Map<String, Object>> feedbackList = new ArrayList<>();
        String query = "SELECT * FROM feedback";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                Map<String, Object> feedback = new HashMap<>();
                feedback.put("id", rs.getLong("id"));
                feedback.put("comment", rs.getString("comment"));
                feedback.put("rating", rs.getInt("rating"));
                feedbackList.add(feedback);
            }
        }
        return feedbackList;
    }

    private List<Map<String, Object>> getAllCats(Connection connection) throws SQLException {
        List<Map<String, Object>> catList = new ArrayList<>();
        String query = "SELECT * FROM cat";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                Map<String, Object> cat = new HashMap<>();
                cat.put("id", rs.getLong("id"));
                cat.put("name", rs.getString("name"));
                cat.put("breed", rs.getString("breed"));
                catList.add(cat);
            }
        }
        return catList;
    }
}