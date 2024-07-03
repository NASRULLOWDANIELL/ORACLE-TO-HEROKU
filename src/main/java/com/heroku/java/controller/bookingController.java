// package com.heroku.java.controller;

// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.stereotype.Controller;
// import org.springframework.web.bind.annotation.*;
// import org.springframework.web.servlet.mvc.support.RedirectAttributes;
// import org.springframework.ui.Model;

// import com.heroku.java.bean.Booking;
// import com.heroku.java.bean.Cat;

// import jakarta.servlet.http.HttpSession;
// import javax.sql.DataSource;
// import java.sql.Connection;
// import java.sql.PreparedStatement;
// import java.sql.ResultSet;
// import java.sql.SQLException;
// import java.util.ArrayList;
// import java.util.HashMap;
// import java.util.List;
// import java.util.Map;
// import java.util.logging.Level;
// import java.util.logging.Logger;

// @Controller
// public class bookingController {
//     private static final Logger LOGGER = Logger.getLogger(bookingController.class.getName());
//     private static final String SESSION_USER_ID = loginController.SESSION_USER_ID;
//     // private static final String SESSION_USER_EMAIL =
//     // loginController.SESSION_USER_EMAIL;

//     private final DataSource dataSource;

//     @Autowired
//     public bookingController(DataSource dataSource) {
//         this.dataSource = dataSource;
//     }

//     private int generateBookingID(Connection connection) throws SQLException {
//         String query = "SELECT MAX(bookingid) FROM booking";
//         try (PreparedStatement ps = connection.prepareStatement(query);
//                 ResultSet rs = ps.executeQuery()) {
//             if (rs.next()) {
//                 int lastID = rs.getInt(1);
//                 return lastID + 1;
//             } else {
//                 return 1; // Start with 1 if no records found
//             }
//         }
//     }

//     private List<Cat> getCustomerCats(Connection connection, int custid) throws SQLException {
//         List<Cat> cats = new ArrayList<>();
//         String sql = "SELECT DISTINCT catid, catname FROM cat WHERE custid = ?";
//         try (PreparedStatement stmt = connection.prepareStatement(sql)) {
//             stmt.setInt(1, custid);
//             try (ResultSet rs = stmt.executeQuery()) {
//                 while (rs.next()) {
//                     Cat cat = new Cat();
//                     cat.setCatid(rs.getInt("catid"));
//                     cat.setCatname(rs.getString("catname"));
//                     cats.add(cat);
//                 }
//             }
//         }
//         return cats;
//     }

//     @GetMapping("/createBooking")
//     public String showCreateBookingForm(Model model, HttpSession session) {

//         Integer custid = (Integer) session.getAttribute(SESSION_USER_ID);
//         if (custid == null) {
//             return "redirect:/login";
//         }

//         try (Connection connection = dataSource.getConnection()) {
//             List<Cat> cats = getCustomerCats(connection, custid);
//             LOGGER.info("Retrieved " + cats.size() + " cats for customer ID: " + custid);

//             Booking booking = new Booking();
//             model.addAttribute("booking", booking);
//             model.addAttribute("cats", cats);
//         } catch (SQLException e) {
//             LOGGER.log(Level.SEVERE, "Error fetching cats", e);
//             model.addAttribute("error", "An error occurred while preparing the booking form. Please try again.");
//         }

//         return "createBooking";
//     }

//     @PostMapping("/createBooking")
//     public String createBooking(@ModelAttribute Booking booking,
//             @RequestParam(required = false) List<Integer> selectedCats,
//             HttpSession session,
//             Model model) {
//         Integer custid = (Integer) session.getAttribute(SESSION_USER_ID);
//         if (custid == null) {
//             return "redirect:/login";
//         }
//         if (booking.getRoomid() == null || booking.getRoomid() == 0) {
//             model.addAttribute("error", "Please select a room.");
//             return "createBooking";
//         }
//         if (selectedCats == null || selectedCats.isEmpty()) {
//             LOGGER.warning("No cats selected for booking.");
//             model.addAttribute("error", "Please select at least one cat for the booking.");
//             return "createBooking";
//         }

//         try (Connection connection = dataSource.getConnection()) {
//             connection.setAutoCommit(false);
//             int bookingId = generateBookingID(connection);

//             // Insert into booking table
//             String bookingSql = "INSERT INTO booking (bookingid, roomid, bookingCheckInDate, bookingCheckOutDate, bookingPrice) VALUES (?, ?, ?, ?, ?)";
//             try (PreparedStatement ps = connection.prepareStatement(bookingSql)) {
//                 ps.setInt(1, bookingId);
//                 ps.setInt(2, booking.getRoomid());
//                 ps.setDate(3, new java.sql.Date(booking.getBookingCheckInDate().getTime()));
//                 ps.setDate(4, new java.sql.Date(booking.getBookingCheckOutDate().getTime()));
//                 ps.setBigDecimal(5, booking.getBookingPrice());
//                 ps.executeUpdate();
//             }

//             // Insert into booking_cat table
//             String bookingCatSql = "INSERT INTO booking_cat (booking_id, cat_id) VALUES (?, ?)";
//             try (PreparedStatement ps = connection.prepareStatement(bookingCatSql)) {
//                 for (Integer catid : selectedCats) {
//                     ps.setInt(1, bookingId);
//                     ps.setInt(2, catid);
//                     ps.addBatch();
//                 }
//                 ps.executeBatch();
//             }

//             connection.commit();
//             return "redirect:/bookingList";
//         } catch (SQLException e) {
//             LOGGER.log(Level.SEVERE, "Error creating booking", e);
//             model.addAttribute("error", "An error occurred while creating the booking. Please try again.");
//             return "createBooking";
//         }
//     }

//     @GetMapping("/bookingList")
//     public String viewbookingList(HttpSession session, Model model) {
//         Integer custid = (Integer) session.getAttribute(SESSION_USER_ID);
//         if (custid == null || custid == 0) {
//             LOGGER.warning("Customer ID not found in session.");
//             return "redirect:/login";
//         }

//         Map<Integer, Booking> bookingsMap = new HashMap<>();

//         try (Connection connection = dataSource.getConnection()) {
//             String sql = "SELECT b.bookingid, b.bookingcheckindate, b.bookingcheckoutdate, " +
//                     "b.bookingprice, b.roomid, b.paymentstatus, b.feedbackId, " +
//                     "LISTAGG(bc.cat_id, ',') WITHIN GROUP (ORDER BY bc.cat_id) as cat_ids, " +
//                     "bp.paymenttype " +
//                     "FROM booking b " +
//                     "JOIN booking_cat bc ON b.bookingid = bc.booking_id " +
//                     "JOIN cat ct ON bc.cat_id = ct.catid " +
//                     "LEFT JOIN bookingpayment bp ON b.bookingid = bp.bookingid " +
//                     "WHERE ct.custid = ? " +
//                     "GROUP BY b.bookingid, b.bookingcheckindate, b.bookingcheckoutdate, " +
//                     "b.bookingprice, b.roomid, b.paymentstatus, b.feedbackId, bp.paymenttype";
//             try (PreparedStatement ps = connection.prepareStatement(sql)) {
//                 ps.setInt(1, custid);
//                 try (ResultSet rs = ps.executeQuery()) {
//                     while (rs.next()) {
//                         Booking booking = new Booking();
//                         booking.setBookingid(rs.getInt("bookingid"));
//                         booking.setBookingCheckInDate(rs.getDate("bookingcheckindate"));
//                         booking.setBookingCheckOutDate(rs.getDate("bookingcheckoutdate"));
//                         booking.setBookingPrice(rs.getBigDecimal("bookingprice"));
//                         booking.setRoomid(rs.getInt("roomid"));
//                         String paymentStatus = rs.getString("paymentstatus");
//                         booking.setPaymentstatus(paymentStatus != null ? paymentStatus : "Not Paid");
//                         booking.setCatIdsString(rs.getString("cat_ids"));
//                         int feedbackId = rs.getInt("feedbackId");
//                         if (rs.wasNull()) {
//                             booking.setFeedbackId(null);
//                             LOGGER.info("Booking " + booking.getBookingid() + " has no feedback");
//                         } else {
//                             booking.setFeedbackId(feedbackId);
//                             LOGGER.info("Booking " + booking.getBookingid() + " has feedback ID: " + feedbackId);
//                         }
//                         booking.setPaymentType(rs.getString("paymenttype"));
//                         bookingsMap.put(booking.getBookingid(), booking);

//                     }
//                 }
//             }
//         } catch (SQLException e) {
//             LOGGER.log(Level.SEVERE, "Error retrieving booking data", e);
//             model.addAttribute("error", "An error occurred while retrieving your bookings. Please try again.");
//         }

//         List<Booking> bookings = new ArrayList<>(bookingsMap.values());
//         model.addAttribute("bookings", bookings);
//         if (bookings.isEmpty()) {
//             model.addAttribute("message", "You have no bookings. Make a new booking to get started!");
//         }

//         return "bookingList";
//     }

//     @GetMapping("/updateBooking/{bookingid}")
//     public String showUpdateBookingForm(@PathVariable("bookingid") int bookingid, HttpSession session, Model model) {
//         Integer sessionCustId = (Integer) session.getAttribute(SESSION_USER_ID);
//         if (sessionCustId == null) {
//             return "redirect:/login";
//         }

//         try (Connection connection = dataSource.getConnection()) {
//             String sql = "SELECT * FROM booking WHERE bookingid = ?";
//             try (PreparedStatement ps = connection.prepareStatement(sql)) {
//                 ps.setInt(1, bookingid);
//                 try (ResultSet rs = ps.executeQuery()) {
//                     if (rs.next()) {
//                         Booking booking = new Booking();
//                         booking.setBookingid(rs.getInt("bookingid"));
//                         booking.setBookingCheckInDate(rs.getDate("bookingCheckInDate"));
//                         booking.setBookingCheckOutDate(rs.getDate("bookingCheckOutDate"));
//                         booking.setBookingPrice(rs.getBigDecimal("bookingPrice"));
//                         booking.setRoomid(rs.getInt("roomid"));
//                         booking.setPaymentstatus(rs.getString("paymentstatus"));

//                         model.addAttribute("booking", booking);

//                         LOGGER.info("Booking object created: " + booking);
//                         return "updateBooking";
//                     } else {
//                         model.addAttribute("error", "Booking details not found.");
//                         return "redirect:/bookingList";
//                     }
//                 }
//             }
//         } catch (SQLException e) {
//             LOGGER.log(Level.SEVERE, "Error retrieving booking for update", e);
//             model.addAttribute("error", "An error occurred while retrieving the booking. Please try again.");
//             return "updateBooking";
//         }
//     }

//     @PostMapping("/updateBooking")
//     public String updateBooking(@ModelAttribute("booking") Booking booking, HttpSession session, Model model) {
//         Integer custid = (Integer) session.getAttribute(SESSION_USER_ID);
//         if (custid == null) {
//             return "redirect:/login";
//         }

//         LOGGER.info("Attempting to update booking with ID: " + booking.getBookingid());

//         try (Connection connection = dataSource.getConnection()) {
//             connection.setAutoCommit(false);
//             String sql = "UPDATE booking SET bookingCheckInDate = ?, bookingCheckOutDate = ? WHERE bookingid = ?";
//             try (PreparedStatement ps = connection.prepareStatement(sql)) {
//                 ps.setDate(1, new java.sql.Date(booking.getBookingCheckInDate().getTime()));
//                 ps.setDate(2, new java.sql.Date(booking.getBookingCheckOutDate().getTime()));
//                 ps.setInt(3, booking.getBookingid());
//                 LOGGER.info("Executing update query: " + sql);
//                 int rowsAffected = ps.executeUpdate();
//                 LOGGER.info("Rows affected: " + rowsAffected);
//                 if (rowsAffected > 0) {
//                     connection.commit();
//                     LOGGER.info("Booking updated successfully");
//                     model.addAttribute("success", "Booking updated successfully.");
//                     return "redirect:/bookingList";
//                 } else {
//                     connection.rollback();
//                     LOGGER.warning("No rows updated for booking ID: " + booking.getBookingid());
//                     model.addAttribute("error", "Failed to update booking. Please try again.");
//                 }
//             }
//         } catch (SQLException e) {
//             LOGGER.log(Level.SEVERE, "Error updating booking", e);
//             model.addAttribute("error", "An error occurred while updating the booking. Please try again.");
//         }
//         return "updateBooking";
//     }

//     @PostMapping("/deleteBooking/{bookingid}")
//     public String deleteBooking(@PathVariable("bookingid") int bookingid, HttpSession session,
//             RedirectAttributes redirectAttributes) {
//         LOGGER.info("Entering deleteBooking method for bookingid: " + bookingid);
//         Integer custid = (Integer) session.getAttribute(SESSION_USER_ID);
//         if (custid == null) {
//             LOGGER.warning("Customer ID not found in session");
//             return "redirect:/login";
//         }

//         try (Connection connection = dataSource.getConnection()) {
//             connection.setAutoCommit(false);
//             try {
//                 // First, check if the booking belongs to the logged-in customer
//                 String checkBookingSql = "SELECT COUNT(*) FROM booking b JOIN booking_cat bc ON b.bookingid = bc.booking_id JOIN cat c ON bc.cat_id = c.catid WHERE b.bookingid = ? AND c.custid = ?";
//                 try (PreparedStatement ps = connection.prepareStatement(checkBookingSql)) {
//                     ps.setInt(1, bookingid);
//                     ps.setInt(2, custid);
//                     try (ResultSet rs = ps.executeQuery()) {
//                         if (rs.next() && rs.getInt(1) == 0) {
//                             LOGGER.warning("Booking does not belong to the logged-in customer");
//                             redirectAttributes.addFlashAttribute("error",
//                                     "You don't have permission to delete this booking.");
//                             return "redirect:/bookingList";
//                         }
//                     }
//                 }

//                 // Delete from booking_cat table
//                 LOGGER.info("Deleting from booking_cat table");
//                 String deleteBookingCatSql = "DELETE FROM booking_cat WHERE booking_id = ?";
//                 try (PreparedStatement ps = connection.prepareStatement(deleteBookingCatSql)) {
//                     ps.setInt(1, bookingid);
//                     int bookingCatRowsAffected = ps.executeUpdate();
//                     LOGGER.info("Rows affected in booking_cat: " + bookingCatRowsAffected);
//                 }

//                 // Delete from booking table
//                 LOGGER.info("Deleting from booking table");
//                 String deleteBookingSql = "DELETE FROM booking WHERE bookingid = ?";
//                 try (PreparedStatement ps = connection.prepareStatement(deleteBookingSql)) {
//                     ps.setInt(1, bookingid);
//                     int bookingRowsAffected = ps.executeUpdate();
//                     LOGGER.info("Rows affected in booking: " + bookingRowsAffected);

//                     if (bookingRowsAffected > 0) {
//                         connection.commit();
//                         LOGGER.info("Booking deleted successfully");
//                         redirectAttributes.addFlashAttribute("success", "Booking deleted successfully.");
//                     } else {
//                         connection.rollback();
//                         LOGGER.warning("No rows deleted from booking table");
//                         redirectAttributes.addFlashAttribute("error", "Failed to delete booking. Please try again.");
//                     }
//                 }
//             } catch (SQLException e) {
//                 connection.rollback();
//                 LOGGER.log(Level.SEVERE, "Error during deletion process", e);
//                 redirectAttributes.addFlashAttribute("error",
//                         "An error occurred while deleting the booking: " + e.getMessage());
//             }
//         } catch (SQLException e) {
//             LOGGER.log(Level.SEVERE, "Error establishing database connection", e);
//             redirectAttributes.addFlashAttribute("error", "Could not connect to the database. Please try again later.");
//         }

//         LOGGER.info("Exiting deleteBooking method, redirecting to bookingList");
//         return "redirect:/bookingList";
//     }
// }

package com.heroku.java.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.ui.Model;

import com.heroku.java.bean.Booking;
import com.heroku.java.bean.Cat;

import jakarta.servlet.http.HttpSession;
import javax.sql.DataSource;
import java.sql.*;
import java.sql.Date;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

@Controller
public class bookingController {
    private static final Logger LOGGER = Logger.getLogger(bookingController.class.getName());
    private static final String SESSION_USER_ID = loginController.SESSION_USER_ID;

    private final DataSource dataSource;

    @Autowired
    public bookingController(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    private int generateBookingID(Connection connection) throws SQLException {
        String query = "SELECT MAX(bookingid) FROM booking";
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

    private List<Cat> getCustomerCats(Connection connection, int custid) throws SQLException {
        List<Cat> cats = new ArrayList<>();
        String sql = "SELECT DISTINCT catid, catname FROM cat WHERE custid = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, custid);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Cat cat = new Cat();
                    cat.setCatid(rs.getInt("catid"));
                    cat.setCatname(rs.getString("catname"));
                    cats.add(cat);
                }
            }
        }
        return cats;
    }

    @GetMapping("/createBooking")
    public String showCreateBookingForm(Model model, HttpSession session) {
        Integer custid = (Integer) session.getAttribute(SESSION_USER_ID);
        if (custid == null) {
            return "redirect:/login";
        }

        try (Connection connection = dataSource.getConnection()) {
            List<Cat> cats = getCustomerCats(connection, custid);
            LOGGER.info("Retrieved " + cats.size() + " cats for customer ID: " + custid);

            Booking booking = new Booking();
            model.addAttribute("booking", booking);
            model.addAttribute("cats", cats);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error fetching cats", e);
            model.addAttribute("error", "An error occurred while preparing the booking form. Please try again.");
        }

        return "createBooking";
    }

    @PostMapping("/createBooking")
    public String createBooking(@ModelAttribute Booking booking,
            @RequestParam(required = false) List<Integer> selectedCats,
            HttpSession session,
            Model model,
            RedirectAttributes redirectAttributes) {
        Integer custid = (Integer) session.getAttribute(SESSION_USER_ID);
        if (custid == null) {
            return "redirect:/login";
        }

        if (booking.getRoomid() == null || booking.getRoomid() == 0) {
            model.addAttribute("error", "Please select a room.");
            return "createBooking";
        }

        if (selectedCats == null || selectedCats.isEmpty()) {
            LOGGER.warning("No cats selected for booking.");
            model.addAttribute("error", "Please select at least one cat for the booking.");
            return "createBooking";
        }

        Date currentDate = new Date(System.currentTimeMillis());
        if (booking.getBookingCheckInDate() == null || booking.getBookingCheckOutDate() == null) {
            model.addAttribute("error", "Please select both check-in and check-out dates.");
            return "createBooking";
        }
        if (booking.getBookingCheckInDate().before(currentDate)) {
            model.addAttribute("error", "Check-in date cannot be in the past.");
            return "createBooking";
        }
        if (booking.getBookingCheckOutDate().before(booking.getBookingCheckInDate())) {
            model.addAttribute("error", "Check-out date must be after check-in date.");
            return "createBooking";
        }

        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(false);

            // Check room availability
            if (!isRoomAvailable(connection, booking)) {
                model.addAttribute("error", "The selected room is not available for the chosen dates.");
                // Ensure cats are always in the model
                model.addAttribute("cats", getCustomerCats(connection, custid));
                return "createBooking";
            }

            int bookingId = generateBookingID(connection);

            // Insert into booking table
            insertBooking(connection, bookingId, booking);

            // Insert into booking_cat table
            insertBookingCats(connection, bookingId, selectedCats);

            connection.commit();
            redirectAttributes.addFlashAttribute("success", "Booking created successfully.");
            return "redirect:/bookingList";
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error creating booking", e);
            model.addAttribute("error", "An error occurred while creating the booking. Please try again.");
            try {
                // Ensure cats are always in the model
                model.addAttribute("cats", getCustomerCats(dataSource.getConnection(), custid));
            } catch (SQLException ex) {
                LOGGER.log(Level.SEVERE, "Error fetching cats after booking creation failure", ex);
            }
            return "createBooking";
        }
    }

    private boolean isRoomAvailable(Connection connection, Booking booking) throws SQLException {
        String availabilityCheckSql = "SELECT COUNT(*) FROM booking WHERE roomid = ? AND bookingStatus != 'CANCELLED' AND ((bookingCheckInDate <= ? AND bookingCheckOutDate >= ?) OR (bookingCheckInDate <= ? AND bookingCheckOutDate >= ?))";
        try (PreparedStatement ps = connection.prepareStatement(availabilityCheckSql)) {
            ps.setInt(1, booking.getRoomid());
            ps.setDate(2, new java.sql.Date(booking.getBookingCheckOutDate().getTime()));
            ps.setDate(3, new java.sql.Date(booking.getBookingCheckInDate().getTime()));
            ps.setDate(4, new java.sql.Date(booking.getBookingCheckInDate().getTime()));
            ps.setDate(5, new java.sql.Date(booking.getBookingCheckOutDate().getTime()));
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) == 0;
            }
        }
    }

    private void insertBooking(Connection connection, int bookingId, Booking booking) throws SQLException {
        String bookingSql = "INSERT INTO booking (bookingid, roomid, bookingCheckInDate, bookingCheckOutDate, bookingPrice, bookingStatus) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(bookingSql)) {
            ps.setInt(1, bookingId);
            ps.setInt(2, booking.getRoomid());
            ps.setDate(3, new java.sql.Date(booking.getBookingCheckInDate().getTime()));
            ps.setDate(4, new java.sql.Date(booking.getBookingCheckOutDate().getTime()));
            ps.setBigDecimal(5, booking.getBookingPrice());
            ps.setString(6, "CONFIRMED");
            ps.executeUpdate();
        }
    }

    private void insertBookingCats(Connection connection, int bookingId, List<Integer> selectedCats)
            throws SQLException {
        String bookingCatSql = "INSERT INTO booking_cat (booking_id, cat_id) VALUES (?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(bookingCatSql)) {
            for (Integer catid : selectedCats) {
                ps.setInt(1, bookingId);
                ps.setInt(2, catid);
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    @GetMapping("/bookingList")
    public String viewBookingList(HttpSession session, Model model) {
        Integer custid = (Integer) session.getAttribute(SESSION_USER_ID);
        if (custid == null || custid == 0) {
            LOGGER.warning("Customer ID not found in session.");
            return "redirect:/login";
        }

        Map<Integer, Booking> bookingsMap = new HashMap<>();

        try (Connection connection = dataSource.getConnection()) {
            String sql = "SELECT b.bookingid, b.bookingcheckindate, b.bookingcheckoutdate, " +
                    "b.bookingprice, b.roomid, b.paymentstatus, b.feedbackId, " +
                    "LISTAGG(bc.cat_id, ',') WITHIN GROUP (ORDER BY bc.cat_id) as cat_ids, " +
                    "bp.paymenttype " +
                    "FROM booking b " +
                    "JOIN booking_cat bc ON b.bookingid = bc.booking_id " +
                    "JOIN cat ct ON bc.cat_id = ct.catid " +
                    "LEFT JOIN bookingpayment bp ON b.bookingid = bp.bookingid " +
                    "WHERE ct.custid = ? " +
                    "GROUP BY b.bookingid, b.bookingcheckindate, b.bookingcheckoutdate, " +
                    "b.bookingprice, b.roomid, b.paymentstatus, b.feedbackId, bp.paymenttype";
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                ps.setInt(1, custid);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        Booking booking = new Booking();
                        booking.setBookingid(rs.getInt("bookingid"));
                        booking.setBookingCheckInDate(rs.getDate("bookingcheckindate"));
                        booking.setBookingCheckOutDate(rs.getDate("bookingcheckoutdate"));
                        booking.setBookingPrice(rs.getBigDecimal("bookingprice"));
                        booking.setRoomid(rs.getInt("roomid"));
                        String paymentStatus = rs.getString("paymentstatus");
                        booking.setPaymentstatus(paymentStatus != null ? paymentStatus : "Not Paid");
                        booking.setCatIdsString(rs.getString("cat_ids"));
                        int feedbackId = rs.getInt("feedbackId");
                        if (rs.wasNull()) {
                            booking.setFeedbackId(null);
                            LOGGER.info("Booking " + booking.getBookingid() + " has no feedback");
                        } else {
                            booking.setFeedbackId(feedbackId);
                            LOGGER.info("Booking " + booking.getBookingid() + " has feedback ID: " + feedbackId);
                        }
                        String paymentType = rs.getString("paymenttype");
                        if (paymentType != null) {
                            booking.setPaymentType(paymentType);
                        }
                        bookingsMap.put(booking.getBookingid(), booking);
                    }
                }
            }

            List<Booking> bookings = new ArrayList<>(bookingsMap.values());
            bookings.sort(Comparator.comparingInt(Booking::getBookingid));

            model.addAttribute("bookings", bookings);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error fetching booking list", e);
            model.addAttribute("error", "An error occurred while retrieving bookings. Please try again.");
        }

        return "bookingList";
    }

    @GetMapping("/updateBooking/{bookingid}")
    public String showUpdateBookingForm(@PathVariable("bookingid") int bookingid, HttpSession session, Model model) {
        Integer sessionCustId = (Integer) session.getAttribute(SESSION_USER_ID);
        if (sessionCustId == null) {
            return "redirect:/login";
        }
        try (Connection connection = dataSource.getConnection()) {
            String sql = "SELECT b.bookingid, b.bookingcheckindate, b.bookingcheckoutdate, " +
                    "LISTAGG(bc.cat_id, ',') WITHIN GROUP (ORDER BY bc.cat_id) as cat_ids " +
                    "FROM booking b " +
                    "JOIN booking_cat bc ON b.bookingid = bc.booking_id " +
                    "WHERE b.bookingid = ? " +
                    "GROUP BY b.bookingid, b.bookingcheckindate, b.bookingcheckoutdate";
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                ps.setInt(1, bookingid);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        Booking booking = new Booking();
                        booking.setBookingid(rs.getInt("bookingid"));
                        booking.setBookingCheckInDate(rs.getDate("bookingcheckindate"));
                        booking.setBookingCheckOutDate(rs.getDate("bookingcheckoutdate"));
                        booking.setCatIdsString(rs.getString("cat_ids"));
                        model.addAttribute("booking", booking);
                        model.addAttribute("cats", getCustomerCats(connection, sessionCustId));
                        return "updateBooking";
                    } else {
                        model.addAttribute("error", "Booking details not found.");
                        return "redirect:/bookingList";
                    }
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error retrieving booking for update", e);
            model.addAttribute("error", "An error occurred while retrieving the booking. Please try again.");
            return "redirect:/bookingList";
        }
    }

    @PostMapping("/updateBooking")
    public String updateBooking(@ModelAttribute("booking") Booking booking, HttpSession session, Model model) {
        Integer sessionCustId = (Integer) session.getAttribute(SESSION_USER_ID);
        if (sessionCustId == null) {
            return "redirect:/login";
        }
        try (Connection connection = dataSource.getConnection()) {
            // Check if the new dates are already booked
            String checkSql = "SELECT COUNT(*) FROM booking " +
                    "WHERE bookingid <> ? " +
                    "AND bookingcheckindate <= ? " +
                    "AND bookingcheckoutdate >= ?";
            try (PreparedStatement checkPs = connection.prepareStatement(checkSql)) {
                checkPs.setInt(1, booking.getBookingid());
                checkPs.setDate(2, new java.sql.Date(booking.getBookingCheckOutDate().getTime()));
                checkPs.setDate(3, new java.sql.Date(booking.getBookingCheckInDate().getTime()));
                try (ResultSet rs = checkPs.executeQuery()) {
                    if (rs.next() && rs.getInt(1) > 0) {
                        model.addAttribute("error",
                                "The selected dates are already booked. Please choose different dates.");
                        model.addAttribute("booking", booking);
                        model.addAttribute("cats", getCustomerCats(connection, sessionCustId));
                        return "updateBooking";
                    }
                }
            }

            // Update the booking if dates are available
            String updateSql = "UPDATE booking SET bookingcheckindate = ?, bookingcheckoutdate = ? WHERE bookingid = ?";
            try (PreparedStatement ps = connection.prepareStatement(updateSql)) {
                ps.setDate(1, new java.sql.Date(booking.getBookingCheckInDate().getTime()));
                ps.setDate(2, new java.sql.Date(booking.getBookingCheckOutDate().getTime()));
                ps.setInt(3, booking.getBookingid());
                ps.executeUpdate();
                return "redirect:/bookingList";
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error updating booking", e);
            model.addAttribute("error", "An error occurred while updating the booking. Please try again.");
            return "updateBooking";
        }
    }

    @PostMapping("/deleteBooking/{bookingid}")
    public String deleteBooking(@PathVariable("bookingid") int bookingid, HttpSession session,
            RedirectAttributes redirectAttributes) {
        Integer custid = (Integer) session.getAttribute(SESSION_USER_ID);
        if (custid == null) {
            return "redirect:/login";
        }

        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(false);

            // Check if the booking belongs to the logged-in customer
            String checkBookingSql = "SELECT COUNT(*) FROM booking b JOIN booking_cat bc ON b.bookingid = bc.booking_id JOIN cat c ON bc.cat_id = c.catid WHERE b.bookingid = ? AND c.custid = ?";
            try (PreparedStatement ps = connection.prepareStatement(checkBookingSql)) {
                ps.setInt(1, bookingid);
                ps.setInt(2, custid);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next() && rs.getInt(1) == 0) {
                        redirectAttributes.addFlashAttribute("error",
                                "You don't have permission to delete this booking.");
                        return "redirect:/bookingList";
                    }
                }
            }

            // Delete from booking_cat table
            String deleteBookingCatSql = "DELETE FROM booking_cat WHERE booking_id = ?";
            try (PreparedStatement ps = connection.prepareStatement(deleteBookingCatSql)) {
                ps.setInt(1, bookingid);
                ps.executeUpdate();
            }

            // Delete from booking table
            String deleteBookingSql = "DELETE FROM booking WHERE bookingid = ?";
            try (PreparedStatement ps = connection.prepareStatement(deleteBookingSql)) {
                ps.setInt(1, bookingid);
                int rowsAffected = ps.executeUpdate();

                if (rowsAffected > 0) {
                    connection.commit();
                    redirectAttributes.addFlashAttribute("success", "Booking deleted successfully.");
                } else {
                    connection.rollback();
                    redirectAttributes.addFlashAttribute("error", "Failed to delete booking. Please try again.");
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error deleting booking", e);
            redirectAttributes.addFlashAttribute("error",
                    "An error occurred while deleting the booking. Please try again.");
        }
        return "redirect:/bookingList";
    }
}
