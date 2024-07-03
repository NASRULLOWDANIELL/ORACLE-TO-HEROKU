package com.heroku.java.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.ui.Model;

import com.heroku.java.bean.Customer;
import com.heroku.java.bean.Room;

import jakarta.servlet.http.HttpSession;
import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@Controller
public class roomController {
    public static final String SESSION_USER_ID = "custid";
    private static final Logger LOGGER = Logger.getLogger(roomController.class.getName());

    private final DataSource dataSource;

    @Autowired
    public roomController(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @GetMapping("/roomList")
    public String showroomListForm(Model model) {
        model.addAttribute("room", new Room());
        return "roomList"; // Ensure this matches your HTML file name
    }

    @GetMapping("/catHotelList")
    public String viewCatHotel(HttpSession session, Model model) {
        Integer custid = (Integer) session.getAttribute(SESSION_USER_ID);
        if (custid == null) {
            return "redirect:/login";
        }

        try (Connection connection = dataSource.getConnection()) {
            Customer customer = getCustomerDetails(connection, custid);
            if (customer == null) {
                return "redirect:/login";
            }
            model.addAttribute("customer", customer);

            List<Room> rooms = getRoomList(connection);
            model.addAttribute("rooms", rooms);

            return "CatHotel";
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error retrieving data for Cat Hotel page", e);
            model.addAttribute("error", "An error occurred while loading the Cat Hotel page. Please try again.");
            return "error";
        }
    }

    @GetMapping("/viewRoom/{roomId}")
    public String viewRoom(@PathVariable int roomId, HttpSession session, Model model) {
        Integer custid = (Integer) session.getAttribute(SESSION_USER_ID);
        if (custid == null) {
            return "redirect:/login";
        }

        try (Connection connection = dataSource.getConnection()) {
            Room room = getRoomDetails(connection, roomId);
            if (room == null) {
                model.addAttribute("error", "Room not found");
                return "error";
            }
            model.addAttribute("room", room);
            return "viewRoom";
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error retrieving room details", e);
            model.addAttribute("error", "An error occurred while loading the room details. Please try again.");
            return "error";
        }
    }

    @GetMapping("/createBooking/{roomId}")
    public String createBooking(@PathVariable int roomId, HttpSession session, Model model) {
        Integer custid = (Integer) session.getAttribute(SESSION_USER_ID);
        if (custid == null) {
            return "redirect:/login";
        }

        try (Connection connection = dataSource.getConnection()) {
            Room room = getRoomDetails(connection, roomId);
            if (room == null) {
                model.addAttribute("error", "Room not found");
                return "error";
            }
            model.addAttribute("room", room);
            return "createBooking"; // Create this view
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error preparing booking page", e);
            model.addAttribute("error", "An error occurred while preparing the booking page. Please try again.");
            return "error";
        }
    }

    private Room getRoomDetails(Connection connection, int roomId) throws SQLException {
        String sql = "SELECT * FROM cathotel WHERE roomid = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, roomId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new Room(
                            rs.getInt("roomid"),
                            rs.getString("roomname"),
                            rs.getString("roomtype"),
                            rs.getString("roomdesc"),
                            rs.getDouble("roomprice"));
                }
            }
        }
        return null;
    }

    private Customer getCustomerDetails(Connection connection, int custid) throws SQLException {
        String sql = "SELECT * FROM customer WHERE custid = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, custid);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Customer customer = new Customer();
                    customer.setCustid(rs.getInt("custid"));
                    customer.setCustname(rs.getString("custname"));
                    customer.setCustemail(rs.getString("custemail"));
                    // Set other customer details as needed
                    return customer;
                }
            }
        }
        return null;
    }

    private List<Room> getRoomList(Connection connection) throws SQLException {
        List<Room> rooms = new ArrayList<>();
        String sql = "SELECT * FROM cathotel";
        try (Statement stmt = connection.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Room room = new Room(
                        rs.getInt("roomid"),
                        rs.getString("roomname"),
                        rs.getString("roomtype"),
                        rs.getString("roomdesc"),
                        rs.getDouble("roomprice"));
                rooms.add(room);
            }
        }
        return rooms;
    }
}