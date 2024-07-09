package com.heroku.java.controller;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.http.ResponseEntity;
import org.springframework.beans.factory.annotation.Autowired;

import com.heroku.java.bean.Booking;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

@RestController
public class bookingAPIController {
    private static final Logger LOGGER = Logger.getLogger(bookingAPIController.class.getName());

    private final DataSource dataSource;

    @Autowired
    public bookingAPIController(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @GetMapping("/api/bookings")
    public ResponseEntity<List<Booking>> listBookings() {
        List<Booking> bookings = new ArrayList<>();

        try (Connection connection = this.dataSource.getConnection()) {
            String sql = "SELECT b.bookingid, b.bookingcheckindate, b.bookingcheckoutdate, " +
                                "b.bookingprice, b.roomid, b.paymentstatus, b.feedbackId, " +
                                "STRING_AGG(bc.cat_id::text, ',') as cat_ids, " +
                                "bp.paymenttype " +
                        "FROM booking b " +
                        "JOIN booking_cat bc ON b.bookingid = bc.booking_id " +
                        "LEFT JOIN bookingpayment bp ON b.bookingid = bp.bookingid " +
                        "GROUP BY b.bookingid, b.bookingcheckindate, b.bookingcheckoutdate, " +
                                "b.bookingprice, b.roomid, b.paymentstatus, b.feedbackId, bp.paymenttype";
            try (PreparedStatement ps = connection.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {
                
                while (rs.next()) {
                    Booking booking = new Booking();
                    booking.setBookingid(rs.getInt("bookingid"));
                    booking.setBookingCheckInDate(rs.getDate("bookingcheckindate"));
                    booking.setBookingCheckOutDate(rs.getDate("bookingcheckoutdate"));
                    booking.setBookingPrice(rs.getBigDecimal("bookingprice"));
                    booking.setRoomid(rs.getInt("roomid"));
                    String paymentStatus = rs.getString("paymentstatus");
                    booking.setPaymentstatus(paymentStatus != null ? paymentStatus : "NOT PAID");
                    booking.setCatIdsString(rs.getString("cat_ids"));
                    int feedbackId = rs.getInt("feedbackId");
                    if (!rs.wasNull()) {
                        booking.setFeedbackId(feedbackId);
                    }
                    String paymentType = rs.getString("paymenttype");
                    if (paymentType != null) {
                        booking.setPaymentType(paymentType);
                    }
                    bookings.add(booking);
                }
            }

            bookings.sort(Comparator.comparingInt(Booking::getBookingid));
            return ResponseEntity.ok(bookings);

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error fetching booking list: " + e.getMessage(), e);
            return ResponseEntity.status(500).body(null);
        }
    }
}