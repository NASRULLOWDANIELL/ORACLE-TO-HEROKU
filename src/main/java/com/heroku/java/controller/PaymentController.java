package com.heroku.java.controller;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import com.heroku.java.bean.Card;
import com.heroku.java.bean.Payment;

import jakarta.servlet.http.HttpSession;

@Controller
public class PaymentController {
    public static final String SESSION_STAFF_ID = staffloginController.SESSION_STAFF_ID;
    private static final String SESSION_USER_ID = loginController.SESSION_USER_ID;
    private static final Logger LOGGER = Logger.getLogger(PaymentController.class.getName());

    private final DataSource dataSource;

    @Autowired
    public PaymentController(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @GetMapping("/viewPayment/{bookingId}")
    public String viewPayment(@PathVariable("bookingId") int bookingId, Model model, HttpSession session) {
        Integer custID = (Integer) session.getAttribute(SESSION_USER_ID);
        Integer staffID = (Integer) session.getAttribute(staffloginController.SESSION_STAFF_ID);

        if (custID == null && staffID == null) {
            return "redirect:/login";
        }

        boolean isStaff = (staffID != null);
        model.addAttribute("isStaff", isStaff);

        try (Connection connection = dataSource.getConnection()) {
            Payment payment = getPaymentDetails(connection, bookingId);
            String paymentStatus = getBookingPaymentStatus(connection, bookingId);
            if (payment != null) {
                model.addAttribute("payment", payment);
                model.addAttribute("paymentStatus", paymentStatus);
                return "viewPayment";
            } else {
                model.addAttribute("error", "Payment not found");
                return "error";
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error fetching payment details", e);
            model.addAttribute("error", "Unable to fetch payment details. Please try again.");
            return "error";
        }
    }

    @GetMapping("/makePayment/{bookingId}")
    public String showPaymentForm(@PathVariable("bookingId") int bookingId, Model model, HttpSession session) {
        Integer custID = (Integer) session.getAttribute(SESSION_USER_ID);
        if (custID == null) {
            return "redirect:/login";
        }

        try (Connection connection = dataSource.getConnection()) {
            String sql = "SELECT * FROM BOOKING WHERE BOOKINGID = ?";
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setInt(1, bookingId);
                try (ResultSet rs = statement.executeQuery()) {
                    if (rs.next()) {
                        Payment payment = new Payment();
                        payment.setBookingId(bookingId);
                        payment.setPaymentTotal(rs.getDouble("bookingprice")); // Assuming the column name is PRICE
                        payment.setCard(new Card());  // Add this line to initialize the Card object
                        model.addAttribute("payment", payment);
                        return "paymentform";
                    } else {
                        model.addAttribute("error", "Booking not found");
                        return "error";
                    }
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error fetching booking details", e);
            model.addAttribute("error", "Unable to fetch booking details. Please try again.");
            return "error";
        }
    }

    @PostMapping("/processPayment")
    public String processPayment(@ModelAttribute("payment") Payment payment, HttpSession session, Model model) {
        Integer custID = (Integer) session.getAttribute(SESSION_USER_ID);
        if (custID == null) {
            return "redirect:/login";
        }

        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(false);

            // Insert into BOOKINGPAYMENT table
            String paymentSql = "INSERT INTO BOOKINGPAYMENT (PAYMENTID, PAYMENTTYPE, PAYMENTTOTAL, BOOKINGID) VALUES (?, ?, ?, ?)";
            try (PreparedStatement paymentStatement = connection.prepareStatement(paymentSql)) {
                int paymentId = generatePaymentId(connection);
                paymentStatement.setInt(1, paymentId);
                paymentStatement.setString(2, payment.getPaymentType());
                paymentStatement.setDouble(3, payment.getPaymentTotal());
                paymentStatement.setInt(4, payment.getBookingId());
                int rowsAffected = paymentStatement.executeUpdate();
                if (rowsAffected == 0) {
                    throw new SQLException("Creating payment failed, no rows affected.");
                }

                // Insert into CARD or CASH table based on payment type
                if ("CARD".equals(payment.getPaymentType())) {
                    insertCardPayment(connection, paymentId, payment.getCard());
                    // Update booking payment status and payment id
                    updateBookingPaymentStatus(connection, payment.getBookingId(), "PAID");
                    updateBookingPaymentId(connection, payment.getBookingId(), paymentId);
                } else if ("CASH".equals(payment.getPaymentType())) {
                    insertCashPayment(connection, paymentId);
                    // Update booking payment status and payment id
                    updateBookingPaymentStatus(connection, payment.getBookingId(), "PENDING");
                    updateBookingPaymentId(connection, payment.getBookingId(), paymentId);
                }
            }

            connection.commit();
            return "redirect:/paymentConfirmation/" + payment.getBookingId();
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error processing payment", e);
            model.addAttribute("error", "Payment processing failed. Please try again.");
            return "paymentform";
        }
    }

    private int generatePaymentId(Connection connection) throws SQLException {
        String query = "SELECT MAX(PAYMENTID) FROM BOOKINGPAYMENT";
        try (PreparedStatement statement = connection.prepareStatement(query);
                ResultSet resultSet = statement.executeQuery()) {
            if (resultSet.next()) {
                return resultSet.getInt(1) + 1;
            } else {
                return 1;
            }
        }
    }

    private void insertCardPayment(Connection connection, int paymentId, Card card) throws SQLException {
        String cardSql = "INSERT INTO CARD (PAYMENTID, CARDTYPE, CARDNUMBER) VALUES (?, ?, ?)";
        try (PreparedStatement cardStatement = connection.prepareStatement(cardSql)) {
            cardStatement.setInt(1, paymentId);
            cardStatement.setString(2, card.getCardType());
            cardStatement.setString(3, card.getCardNumber());
            cardStatement.executeUpdate();
        }
    }

    private void insertCashPayment(Connection connection, int paymentId) throws SQLException {
        String cashSql = "INSERT INTO CASH (PAYMENTID) VALUES (?)";
        try (PreparedStatement cashStatement = connection.prepareStatement(cashSql)) {
            cashStatement.setInt(1, paymentId);
            cashStatement.executeUpdate();
        }
    }

    @GetMapping("/paymentConfirmation/{bookingId}")
    public String showPaymentConfirmation(@PathVariable int bookingId, Model model, HttpSession session) {
        Integer custID = (Integer) session.getAttribute(SESSION_USER_ID);
        if (custID == null) {
            return "redirect:/login";
        }

        // Fetch payment details and add to model
        try (Connection connection = dataSource.getConnection()) {
            Payment payment = getPaymentDetails(connection, bookingId);
            model.addAttribute("payment", payment);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error fetching payment details", e);
            model.addAttribute("error", "Unable to fetch payment details. Please contact support.");
            return "error";
        }

        return "paymentConfirmation";
    }

    private Payment getPaymentDetails(Connection connection, int bookingId) throws SQLException {
        String sql = "SELECT * FROM BOOKINGPAYMENT WHERE BOOKINGID = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, bookingId);
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    Payment payment = new Payment();
                    payment.setPaymentId(rs.getInt("PAYMENTID"));
                    payment.setPaymentType(rs.getString("PAYMENTTYPE"));
                    payment.setPaymentTotal(rs.getDouble("PAYMENTTOTAL"));
                    payment.setBookingId(rs.getInt("BOOKINGID"));
                    return payment;
                }
            }
        }
        return null;
    }

    private String getBookingPaymentStatus(Connection connection, int bookingId) throws SQLException {
        String sql = "SELECT paymentstatus FROM BOOKING WHERE BOOKINGID = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, bookingId);
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("paymentstatus");
                }
            }
        }
        return null;
    }

    @PostMapping("/confirmCashPayment/{bookingId}")
    public String confirmCashPayment(@PathVariable int bookingId, HttpSession session) {
        Integer staffID = (Integer) session.getAttribute(SESSION_STAFF_ID);

        if (staffID == null) {
            return "redirect:/stafflogin"; // or wherever your staff login page is
        }

        try (Connection connection = dataSource.getConnection()) {
            updateBookingPaymentStatus(connection, bookingId, "PAID");
            return "redirect:/viewPayment/" + bookingId;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error confirming cash payment", e);
            return "redirect:/error";
        }
    }

    private void updateBookingPaymentStatus(Connection connection, int bookingId, String status) throws SQLException {
        String sql = "UPDATE BOOKING SET paymentstatus = ? WHERE bookingid = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, status);
            statement.setInt(2, bookingId);
            int rowsAffected = statement.executeUpdate();
            if (rowsAffected == 0) {
                throw new SQLException("Updating booking payment status failed, no rows affected.");
            }
        }
    }

    private void updateBookingPaymentId(Connection connection, int bookingId, int paymentId) throws SQLException {
        String sql = "UPDATE BOOKING SET paymentid = ? WHERE bookingid = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, paymentId);
            statement.setInt(2, bookingId);
            int rowsAffected = statement.executeUpdate();
            if (rowsAffected == 0) {
                throw new SQLException("Updating booking payment ID failed, no rows affected.");
            }
        }
    }
}
