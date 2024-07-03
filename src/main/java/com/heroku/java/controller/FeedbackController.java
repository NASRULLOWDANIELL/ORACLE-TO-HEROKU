package com.heroku.java.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import com.heroku.java.bean.Feedback;
import jakarta.servlet.http.HttpSession;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@Controller
public class FeedbackController {

    private static final Logger LOGGER = Logger.getLogger(FeedbackController.class.getName());
    private static final String SESSION_USER_ID = loginController.SESSION_USER_ID;

    private final DataSource dataSource;

    @Autowired
    public FeedbackController(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public Feedback findFeedbackByBookingId(int bookingId) {
        String sql = "SELECT * FROM bookingfeedback WHERE bookingid = ?";
        try (Connection connection = dataSource.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, bookingId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    Feedback feedback = new Feedback();
                    feedback.setFeedbackId(resultSet.getInt("feedbackId"));
                    feedback.setFeedbackRating(resultSet.getString("feedbackRating"));
                    feedback.setFeedbackComment(resultSet.getString("feedbackComment"));
                    feedback.setBookingId(resultSet.getInt("bookingId"));
                    return feedback;
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "SQL Error during finding feedback by booking ID: " + e.getMessage(), e);
        }
        return null; // Return null if feedback not found
    }

    private int generateFeedbackID(Connection connection) throws SQLException {
        String query = "SELECT MAX(feedbackid) FROM bookingfeedback";
        try (PreparedStatement ps = connection.prepareStatement(query);
                ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                int lastID = rs.getInt(1);
                return lastID + 1;
            } else {
                return 1; // Start with 1 if no records found
            }
        }
    }

    @GetMapping("/submitFeedback")
    public String showFeedbackForm(@RequestParam("bookingid") int bookingid, Model model) {
        Feedback feedback = new Feedback();
        feedback.setBookingId(bookingid);
        model.addAttribute("feedback", feedback);
        return "submitFeedback";
    }

    @PostMapping("/submitFeedback")
    public String submitFeedback(@ModelAttribute("feedback") Feedback feedback, Model model, HttpSession session) {
        Integer custid = (Integer) session.getAttribute(SESSION_USER_ID);
        if (custid == null) {
            return "redirect:/login";
        }

        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(false);

            // First, verify if the bookingid exists
            String checkBookingSql = "SELECT COUNT(*) FROM booking WHERE bookingid = ?";
            try (PreparedStatement checkStmt = connection.prepareStatement(checkBookingSql)) {
                checkStmt.setInt(1, feedback.getBookingId());
                try (ResultSet rs = checkStmt.executeQuery()) {
                    if (rs.next() && rs.getInt(1) == 0) {
                        model.addAttribute("error", "Invalid booking ID. Please try again.");
                        return "submitFeedback";
                    }
                }
            }

            int feedbackId = generateFeedbackID(connection);

            // Insert feedback into BookingFeedback table first
            String insertFeedbackSql = "INSERT INTO bookingfeedback (feedbackid, feedbackrating, feedbackcomment, bookingid) VALUES (?, ?, ?, ?)";
            try (PreparedStatement insertStmt = connection.prepareStatement(insertFeedbackSql)) {
                insertStmt.setInt(1, feedbackId);
                insertStmt.setString(2, feedback.getFeedbackRating());
                insertStmt.setString(3, feedback.getFeedbackComment());
                insertStmt.setInt(4, feedback.getBookingId());
                insertStmt.executeUpdate();
            }

            // Then update the Booking table with the new feedbackId
            String updateBookingSql = "UPDATE booking SET feedbackid = ? WHERE bookingid = ?";
            try (PreparedStatement updateStmt = connection.prepareStatement(updateBookingSql)) {
                updateStmt.setInt(1, feedbackId);
                updateStmt.setInt(2, feedback.getBookingId());
                int rowsAffected = updateStmt.executeUpdate();
                if (rowsAffected == 0) {
                    connection.rollback();
                    model.addAttribute("error", "Failed to update booking with feedback. Please try again.");
                    return "submitFeedback";
                }
            }

            connection.commit();
            return "redirect:/feedbackSuccess";
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "SQL Error during feedback submission: " + e.getMessage(), e);
            model.addAttribute("error", "An error occurred during feedback submission: " + e.getMessage());
            return "submitFeedback";
        }
    }

    @GetMapping("/feedbackSuccess")
    public String feedbackSuccess() {
        return "feedbackSuccess";
    }

    @GetMapping("/viewFeedback/{feedbackId}")
    public String viewFeedback(@PathVariable("feedbackId") int feedbackId, Model model, HttpSession session) {
        Integer custid = (Integer) session.getAttribute(SESSION_USER_ID);
        if (custid == null) {
            return "redirect:/login";
        }

        // Fetch bookingId based on feedbackId
        String getBookingIdSql = "SELECT bookingid FROM bookingfeedback WHERE feedbackid = ?";
        Integer bookingId = null;

        try (Connection connection = dataSource.getConnection();
                PreparedStatement statement = connection.prepareStatement(getBookingIdSql)) {
            statement.setInt(1, feedbackId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    bookingId = resultSet.getInt("bookingid");
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "SQL Error while retrieving bookingId for feedbackId: " + feedbackId, e);
            model.addAttribute("error", "An error occurred while retrieving the feedback. Please try again later.");
            return "error";
        }

        if (bookingId == null) {
            model.addAttribute("error", "Feedback not found or you don't have permission to view it.");
            return "error";
        }

        Feedback feedback = findFeedbackByBookingId(bookingId);
        if (feedback == null) {
            model.addAttribute("error", "Feedback not found or you don't have permission to view it.");
            return "error";
        }

        model.addAttribute("feedback", feedback);
        return "viewFeedback";
    }

    @GetMapping("/viewFeedbackList/{bookingid}")
    public String viewFeedbackList(@PathVariable("bookingid") String bookingid, Model model, HttpSession session) {
        Integer custid = (Integer) session.getAttribute(SESSION_USER_ID);
        if (custid == null) {
            return "redirect:/login";
        }

        String sql = "SELECT f.* FROM bookingfeedback f " +
                "JOIN booking b ON f.bookingid = b.bookingid " +
                "JOIN booking_cat bc ON b.bookingid = bc.booking_id " +
                "JOIN cat c ON bc.cat_id = c.catid " +
                "JOIN customer cu ON c.custid = cu.custid " +
                "WHERE f.bookingid = ? AND cu.custid = ?";

        List<Feedback> feedbacks = new ArrayList<>();
        try (Connection connection = dataSource.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, bookingid);
            statement.setInt(2, custid);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    Feedback feedback = new Feedback();
                    feedback.setFeedbackId(resultSet.getInt("feedbackId"));
                    feedback.setFeedbackRating(resultSet.getString("feedbackRating"));
                    feedback.setFeedbackComment(resultSet.getString("feedbackComment"));
                    feedback.setBookingId(resultSet.getInt("bookingId"));
                    feedbacks.add(feedback);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE,
                    "SQL Error while retrieving feedback for bookingid: " + bookingid + ", custid: " + custid, e);
            model.addAttribute("error", "An error occurred while retrieving the feedback. Please try again later.");
            return "error";
        }

        if (feedbacks.isEmpty()) {
            model.addAttribute("error", "Feedback not found or you don't have permission to view it.");
        } else {
            model.addAttribute("feedbacks", feedbacks);
            model.addAttribute("feedback", feedbacks.get(0)); // Add the first feedback as a single object
        }

        return "viewFeedbackList";
    }
}