package com.heroku.java.controller;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.http.ResponseEntity;
import org.springframework.beans.factory.annotation.Autowired;

import com.heroku.java.bean.Payment;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

@RestController
public class PaymentListApiController {
    private static final Logger LOGGER = Logger.getLogger(PaymentListApiController.class.getName());

    private final DataSource dataSource;

    @Autowired
    public PaymentListApiController(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @GetMapping("/api/payments")
    public ResponseEntity<List<Payment>> listPayments() {
        List<Payment> payments = new ArrayList<>();

        try (Connection connection = this.dataSource.getConnection()) {
            String sql = "SELECT * FROM bookingpayment";
            try (PreparedStatement statement = connection.prepareStatement(sql);
                 ResultSet resultSet = statement.executeQuery()) {

                while (resultSet.next()) {
                    Payment payment = new Payment();
                    payment.setPaymentId(resultSet.getInt("paymentid"));
                    payment.setPaymentType(resultSet.getString("paymenttype"));
                    payment.setPaymentTotal(resultSet.getDouble("paymenttotal"));
                    payment.setBookingId(resultSet.getInt("bookingid"));
                    payments.add(payment);
                }
            }
            return ResponseEntity.ok(payments);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error retrieving payment list: " + e.getMessage(), e);
            return ResponseEntity.status(500).body(null);
        }
    }
}