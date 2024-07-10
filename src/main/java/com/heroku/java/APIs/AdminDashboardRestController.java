package com.heroku.java.APIs;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.heroku.java.bean.Staff;
import com.heroku.java.bean.Booking;
import com.heroku.java.bean.Feedback;
import com.heroku.java.bean.Cat;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

@RestController
@RequestMapping("/api/admin")
public class AdminDashboardRestController {

    private static final Logger LOGGER = Logger.getLogger(AdminDashboardRestController.class.getName());
    private final DataSource dataSource;

    @Autowired
    public AdminDashboardRestController(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @GetMapping("/dashboard")
    public ResponseEntity<?> getAdminDashboardData() {
        Map<String, Object> dashboardData = new HashMap<>();
        try (Connection connection = dataSource.getConnection()) {
            dashboardData.put("staffList", getAllStaff(connection));
            dashboardData.put("bookingList", getAllBookings(connection));
            dashboardData.put("feedbackList", getAllFeedback(connection));
            dashboardData.put("catList", getAllCats(connection));
            return ResponseEntity.ok(dashboardData);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error fetching data for admin dashboard", e);
            return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("An error occurred while loading the dashboard data: " + e.getMessage());
        }
    }

    private List<Staff> getAllStaff(Connection connection) throws SQLException {
        List<Staff> staffList = new ArrayList<>();
        String sql = "SELECT * FROM staff";
        try (PreparedStatement stmt = connection.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                Staff staff = new Staff();
                staff.setStaffid(rs.getInt("staffid"));
                staff.setStaffname(rs.getString("staffname"));
                staff.setStaffemail(rs.getString("staffemail"));
                staff.setStaffrole(rs.getString("staffrole"));
                staff.setStaffphoneno(rs.getString("staffphoneno"));
                staff.setManagerid(rs.getInt("managerid"));

                staffList.add(staff);
            }
        }
        return staffList;
    }

    private List<Booking> getAllBookings(Connection connection) throws SQLException {
        Map<Integer, Booking> bookingMap = new LinkedHashMap<>();
        String sql = "SELECT b.*, c.custname, cat.catid, cat.catname FROM booking b " +
                "JOIN booking_cat bc ON b.bookingid = bc.booking_id " +
                "JOIN cat ON bc.cat_id = cat.catid " +
                "JOIN customer c ON cat.custid = c.custid " +
                "ORDER BY b.bookingid";
        try (PreparedStatement stmt = connection.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                int bookingId = rs.getInt("bookingid");
                Booking booking = bookingMap.computeIfAbsent(bookingId, id -> {
                    Booking newBooking = new Booking();
                    try {
                        newBooking.setBookingid(id);
                        newBooking.setCustname(rs.getString("custname"));
                        newBooking.setBookingCheckInDate(rs.getDate("bookingCheckInDate"));
                        newBooking.setBookingCheckOutDate(rs.getDate("bookingCheckOutDate"));
                        newBooking.setPaymentstatus(rs.getString("bookingPrice"));
                        newBooking.setPaymentstatus(rs.getString("catid"));
                        newBooking.setPaymentstatus(rs.getString("catIdsString"));
                        newBooking.setPaymentstatus(rs.getString("roomid"));
                        newBooking.setPaymentstatus(rs.getString("feedbackId"));
                        newBooking.setPaymentstatus(rs.getString("paymentType"));
                        newBooking.setPaymentstatus(rs.getString("catname"));
                        newBooking.setPaymentstatus(rs.getString("bookingstatus"));

                        newBooking.setCats(new ArrayList<>());
                    } catch (SQLException e) {
                        LOGGER.log(Level.SEVERE, "Error setting booking details", e);
                    }
                    return newBooking;
                });

                Cat cat = new Cat();
                cat.setCatid(rs.getInt("catid"));
                cat.setCatname(rs.getString("catname"));
                booking.getCats().add(cat);
            }
        }
        return new ArrayList<>(bookingMap.values());
    }

    private List<Feedback> getAllFeedback(Connection connection) throws SQLException {
        List<Feedback> feedbackList = new ArrayList<>();
        String sql = "SELECT f.*, b.bookingid, c.custname FROM bookingfeedback f " +
                "JOIN booking b ON f.bookingid = b.bookingid " +
                "JOIN booking_cat bc ON b.bookingid = bc.booking_id " +
                "JOIN cat ON bc.cat_id = cat.catid " +
                "JOIN customer c ON cat.custid = c.custid";
        try (PreparedStatement stmt = connection.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                Feedback feedback = new Feedback();
                feedback.setFeedbackId(rs.getInt("feedbackId"));
                feedback.setBookingId(rs.getInt("bookingId"));
                feedback.setCustname(rs.getString("custname"));
                feedback.setFeedbackRating(rs.getString("feedbackRating"));
                feedback.setFeedbackComment(rs.getString("feedbackComment"));
                feedbackList.add(feedback);
            }
        }
        return feedbackList;
    }

    private List<Cat> getAllCats(Connection connection) throws SQLException {
        List<Cat> catList = new ArrayList<>();
        String sql = "SELECT cat.*, c.custname FROM cat " +
                "JOIN customer c ON cat.custid = c.custid";
        try (PreparedStatement stmt = connection.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                Cat cat = new Cat();
                cat.setCatid(rs.getInt("catid"));
                cat.setCatname(rs.getString("catname"));
                cat.setCustname(rs.getString("custname"));
                cat.setCatbreed(rs.getString("catbreed"));
                cat.setCatage(rs.getInt("catage"));
                catList.add(cat);
            }
        }
        return catList;
    }
}