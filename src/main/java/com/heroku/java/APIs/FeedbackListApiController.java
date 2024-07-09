package com.heroku.java.APIs;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.http.ResponseEntity;
import org.springframework.beans.factory.annotation.Autowired;

import com.heroku.java.bean.Feedback;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

@RestController
public class FeedbackListApiController {
    private static final Logger LOGGER = Logger.getLogger(FeedbackListApiController.class.getName());

    private final DataSource dataSource;

    @Autowired
    public FeedbackListApiController(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @GetMapping("/api/feedbacks")
    public ResponseEntity<List<Feedback>> listFeedbacks() {
        List<Feedback> feedbacks = new ArrayList<>();

        try (Connection connection = this.dataSource.getConnection()) {
            String sql = "SELECT * FROM bookingfeedback";
            try (PreparedStatement statement = connection.prepareStatement(sql);
                 ResultSet resultSet = statement.executeQuery()) {

                while (resultSet.next()) {
                    Feedback feedback = new Feedback();
                    feedback.setFeedbackId(resultSet.getInt("feedbackid"));
                    feedback.setFeedbackRating(resultSet.getString("feedbackrating"));
                    feedback.setFeedbackComment(resultSet.getString("feedbackcomment"));
                    feedback.setBookingId(resultSet.getInt("bookingid"));
                    feedbacks.add(feedback);
                }
            }
            return ResponseEntity.ok(feedbacks);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error retrieving feedback list: " + e.getMessage(), e);
            return ResponseEntity.status(500).body(null);
        }
    }
}