package com.heroku.java.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.ui.Model;

import com.heroku.java.bean.Cat;

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
public class CatController {
    private static final Logger LOGGER = Logger.getLogger(CatController.class.getName());
    private static final String SESSION_USER_ID = loginController.SESSION_USER_ID;
    private static final String SESSION_USER_EMAIL = loginController.SESSION_USER_EMAIL;

    private final DataSource dataSource;

    @Autowired
    public CatController(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    private int generateCatID(Connection connection) throws SQLException {
        String query = "SELECT MAX(catid) FROM cat";
        try (PreparedStatement statement = connection.prepareStatement(query);
                ResultSet resultSet = statement.executeQuery()) {
            if (resultSet.next()) {
                int lastID = resultSet.getInt(1);
                return lastID + 1;
            } else {
                return 1; // Start with 1 if no records found
            }
        }
    }

    @PostMapping("/createCat")
    public String createCat(HttpSession session, @ModelAttribute("cat") Cat cat, Model model) {
        Integer custid = (Integer) session.getAttribute(SESSION_USER_ID);
        if (custid == null) {
            return "redirect:/login";
        }

        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(false);

            int catid = generateCatID(connection);
            String sql = "INSERT INTO cat (catid, custid, catname, catbreed, catage) VALUES (?, ?, ?, ?, ?)";
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setInt(1, catid);
                statement.setInt(2, custid);
                statement.setString(3, cat.getCatname());
                statement.setString(4, cat.getCatbreed());
                statement.setInt(5, cat.getCatage());
                int rowsAffected = statement.executeUpdate();
                if (rowsAffected > 0) {
                    connection.commit();
                    return "redirect:/catList";
                } else {
                    connection.rollback();
                    model.addAttribute("error", "Failed to create cat. Please try again.");
                    return "createCat";
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error creating cat", e);
            model.addAttribute("error", "An error occurred while creating the cat. Please try again.");
            return "createCat";
        }
    }

    @GetMapping("/createCat")
    public String showCreateCatForm(Model model) {
        model.addAttribute("cat", new Cat());
        return "createCat";
    }

    @GetMapping("/catList")
    public String viewCatList(HttpSession session, Model model) {
        Integer custid = (Integer) session.getAttribute(SESSION_USER_ID);
        String userEmail = (String) session.getAttribute(SESSION_USER_EMAIL);
        LOGGER.info("Logged in customer email: " + userEmail);
        LOGGER.info("Customer ID retrieved from session in viewCatList: " + custid);

        if (custid == null || custid == 0) {
            LOGGER.warning("Customer ID not found in session.");
            return "redirect:/login";
        }

        try (Connection connection = dataSource.getConnection()) {
            String sql = "SELECT * FROM cat WHERE custid = ?";
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setInt(1, custid);
                try (ResultSet resultSet = statement.executeQuery()) {
                    List<Cat> cats = new ArrayList<>();
                    while (resultSet.next()) {
                        Cat cat = new Cat();
                        cat.setCatid(resultSet.getInt("catid"));
                        cat.setCustid(resultSet.getInt("custid"));
                        cat.setCatname(resultSet.getString("catname"));
                        cat.setCatbreed(resultSet.getString("catbreed"));
                        cat.setCatage(resultSet.getInt("catage"));
                        cats.add(cat);
                    }
                    model.addAttribute("cats", cats);
                    if (cats.isEmpty()) {
                        model.addAttribute("message", "You have no cats registered. Add a new cat to get started!");
                    }
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error retrieving cat data", e);
            model.addAttribute("error", "An error occurred while retrieving your cat profiles. Please try again.");
        }

        return "catList";
    }

    @GetMapping("/updateCat/{catid}")
    public String showUpdateCatForm(@PathVariable("catid") int catid, HttpSession session, Model model) {
        Integer custid = (Integer) session.getAttribute(SESSION_USER_ID);
        if (custid == null) {
            return "redirect:/login";
        }

        try (Connection connection = dataSource.getConnection()) {
            String sql = "SELECT * FROM cat WHERE catid = ? AND custid = ?";
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setInt(1, catid);
                statement.setInt(2, custid);
                try (ResultSet resultSet = statement.executeQuery()) {
                    if (resultSet.next()) {
                        Cat cat = new Cat();
                        cat.setCatid(resultSet.getInt("catid"));
                        cat.setCustid(resultSet.getInt("custid"));
                        cat.setCatname(resultSet.getString("catname"));
                        cat.setCatbreed(resultSet.getString("catbreed"));
                        cat.setCatage(resultSet.getInt("catage"));
                        model.addAttribute("cat", cat);
                        return "updateCat";
                    } else {
                        model.addAttribute("error",
                                "Cat details not found or you don't have permission to update this cat.");
                        return "redirect:/catList";
                    }
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error retrieving cat for update", e);
            model.addAttribute("error", "An error occurred while retrieving the cat profile. Please try again.");
            return "updateCat";
        }
    }

    @PostMapping("/updateCat")
    public String updateCatProfile(@ModelAttribute("cat") Cat cat, HttpSession session, Model model) {
        Integer custid = (Integer) session.getAttribute(SESSION_USER_ID);
        if (custid == null) {
            return "redirect:/login";
        }

        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(false);
            String sql = "UPDATE cat SET catname = ?, catbreed = ?, catage = ? WHERE catid = ? AND custid = ?";
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, cat.getCatname());
                statement.setString(2, cat.getCatbreed());
                statement.setInt(3, cat.getCatage());
                statement.setInt(4, cat.getCatid());
                statement.setInt(5, custid);
                int rowsAffected = statement.executeUpdate();
                if (rowsAffected > 0) {
                    connection.commit();
                    model.addAttribute("success", "Cat Profile updated successfully.");
                    return "redirect:/catList";
                } else {
                    connection.rollback();
                    model.addAttribute("error", "Failed to update cat. Please try again.");
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error updating cat", e);
            model.addAttribute("error", "An error occurred while updating the cat profile. Please try again.");
        }
        return "updateCat";
    }

    @PostMapping("/deleteCat/{catid}")
    public String deleteCat(@PathVariable("catid") int catid, HttpSession session, Model model) {
        Integer custid = (Integer) session.getAttribute(SESSION_USER_ID);
        if (custid == null) {
            return "redirect:/login";
        }

        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(false);
            try {
                // First, delete related records in booking_cat table
                String deleteBookingCatSql = "DELETE FROM booking_cat WHERE cat_id = ?";
                try (PreparedStatement bookingCatStatement = connection.prepareStatement(deleteBookingCatSql)) {
                    bookingCatStatement.setInt(1, catid);
                    bookingCatStatement.executeUpdate();
                }

                // Then, delete the cat record
                String deleteCatSql = "DELETE FROM cat WHERE catid = ? AND custid = ?";
                try (PreparedStatement catStatement = connection.prepareStatement(deleteCatSql)) {
                    catStatement.setInt(1, catid);
                    catStatement.setInt(2, custid);
                    int rowsAffected = catStatement.executeUpdate();

                    if (rowsAffected > 0) {
                        connection.commit();
                        model.addAttribute("success", "Cat deleted successfully.");
                    } else {
                        connection.rollback();
                        model.addAttribute("error",
                                "Failed to delete cat. Please ensure you have permission to delete this cat.");
                    }
                }
            } catch (SQLException e) {
                connection.rollback();
                LOGGER.log(Level.SEVERE, "Error deleting cat", e);
                model.addAttribute("error", "An error occurred while deleting the cat. Please try again.");
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error establishing database connection", e);
            model.addAttribute("error", "An error occurred while connecting to the database. Please try again.");
        }

        return "redirect:/catList";
    }
}