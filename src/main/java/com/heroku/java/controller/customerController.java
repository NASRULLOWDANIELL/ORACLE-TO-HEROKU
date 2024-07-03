package com.heroku.java.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;

import com.heroku.java.bean.Customer;

import jakarta.servlet.http.HttpSession;
import javax.sql.DataSource;
import java.sql.*;
import java.util.logging.Level;
import java.util.logging.Logger;

@Controller
public class customerController {

    public static final String SESSION_USER_ID = "custid";
    private static final Logger LOGGER = Logger.getLogger(customerController.class.getName());

    private final DataSource dataSource;

    @Autowired
    public customerController(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @GetMapping("/register")
    public String showRegisterForm(Model model) {
        model.addAttribute("customer", new Customer());
        return "register"; // Ensure this matches your HTML file name
    }

    @PostMapping("/register")
    public String addAccount(@ModelAttribute("customer") Customer customer, Model model, RedirectAttributes redirectAttributes) {
        LOGGER.log(Level.INFO, "Starting customer registration for email: {0}", customer.getCustemail());
        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(false); // Start transaction

            // Generate unique custid
            int custid = generateCustomerID(connection);
            if (custid == 0) {
                model.addAttribute("error", "Unable to generate customer ID. Please try again.");
                return "register";
            }

            // Insert into customer table
            String customerSql = "INSERT INTO customer (custid, custname, custphoneno, custpassword, custemail) VALUES (?, ?, ?, ?, ?)";
            try (PreparedStatement customerStatement = connection.prepareStatement(customerSql)) {
                customerStatement.setInt(1, custid);
                customerStatement.setString(2, customer.getCustname());
                customerStatement.setString(3, customer.getCustphoneno());
                customerStatement.setString(4, customer.getCustpassword());
                customerStatement.setString(5, customer.getCustemail());

                int customerRowsAffected = customerStatement.executeUpdate();
                if (customerRowsAffected == 0) {
                    LOGGER.log(Level.SEVERE, "Customer registration failed: {0}", customer.getCustemail());
                    model.addAttribute("error", "Registration failed. Please try again.");
                    connection.rollback(); // Rollback transaction
                    return "register";
                } else {
                    redirectAttributes.addFlashAttribute("message", "Account created successfully!");
                }   
            }

            connection.commit(); // Commit transaction
            LOGGER.log(Level.INFO, "Customer registered successfully: {0}", customer.getCustemail());
            return "redirect:/login";
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "SQL Error during customer registration: " + e.getMessage(), e);
            model.addAttribute("error", "An error occurred during registration: " + e.getMessage());
            return "register";
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Unexpected Error during customer registration: " + e.getMessage(), e);
            model.addAttribute("error", "An unexpected error occurred: " + e.getMessage());
            return "register";
        }
    }

    private int generateCustomerID(Connection connection) throws SQLException {
        String query = "SELECT MAX(custid) FROM customer";
        try (PreparedStatement statement = connection.prepareStatement(query);
                ResultSet resultSet = statement.executeQuery()) {
            if (resultSet.next()) {
                int lastID = resultSet.getInt(1);
                return lastID + 1;
            } else {
                return 100; // Start with 100 if no records found
            }
        }
    }

    @GetMapping("/viewaccount")
    public String viewProfile(HttpSession session, Model model) {
        int custid = (int) session.getAttribute(SESSION_USER_ID);
        if (custid == 0) {
            return "redirect:/login";
        }

        try (Connection connection = dataSource.getConnection()) {
            Customer customer = getCustomerDetails(connection, custid);
            if (customer == null) {
                return "redirect:/login";
            }
            model.addAttribute("customer", customer);
            return "viewaccount";
        } catch (SQLException e) {
            e.printStackTrace(); // Log or handle exception properly
            return "error";
        }
    }

    @GetMapping("/updateAccount")
    public String updateaccount(HttpSession session, Model model) {
        Integer custid = (Integer) session.getAttribute(SESSION_USER_ID);
        if (custid == null) {
            LOGGER.info("Session custid is null when accessing updateAccount. Redirecting to login.");
            return "redirect:/login";
        }

        try (Connection connection = dataSource.getConnection()) {
            Customer customer = getCustomerDetails(connection, custid);
            if (customer == null) {
                return "redirect:/login";
            }
            model.addAttribute("customer", customer);
            return "updateAccount";
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error retrieving customer details: " + e.getMessage(), e);
            model.addAttribute("error", "An error occurred while retrieving your profile. Please try again.");
            return "error";
        }
    }

    @PostMapping("/updateAccount")
    public String updateviewaccount(@ModelAttribute("customer") Customer customer, Model model, HttpSession session,
            RedirectAttributes redirectAttributes) {
        Integer custid = (Integer) session.getAttribute(SESSION_USER_ID);
        LOGGER.info("Session custid: " + custid);
        LOGGER.info("Submitted customer custid: " + customer.getCustid());
        if (custid == null) {
            LOGGER.info("Session custid is null. Redirecting to login.");
            redirectAttributes.addFlashAttribute("error", "Your session has expired. Please login again.");
            return "redirect:/login";
        }
        if (!custid.equals(customer.getCustid())) {
            LOGGER.info("Mismatch between session custid and submitted custid. Session: " + custid + ", Submitted: "
                    + customer.getCustid());
            redirectAttributes.addFlashAttribute("error", "Invalid account update attempt. Please try again.");
            return "redirect:/login";
        }

        try (Connection connection = dataSource.getConnection()) {
            String updateCustomerSql = "UPDATE customer SET custname = ?, custphoneno = ?, custpassword = ? WHERE custid = ?";
            try (PreparedStatement updateCustomerStatement = connection.prepareStatement(updateCustomerSql)) {
                updateCustomerStatement.setString(1, customer.getCustname());
                updateCustomerStatement.setString(2, customer.getCustphoneno());
                updateCustomerStatement.setString(3, customer.getCustpassword());
                updateCustomerStatement.setInt(4, custid);

                int rowsAffected = updateCustomerStatement.executeUpdate();
                if (rowsAffected == 0) {
                    redirectAttributes.addFlashAttribute("error", "Failed to update profile. Please try again.");
                    return "redirect:/updateAccount";
                }
            }
            redirectAttributes.addFlashAttribute("success", "Profile updated successfully.");
            return "redirect:/viewaccount";
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error updating customer profile: " + e.getMessage(), e);
            redirectAttributes.addFlashAttribute("error", "An error occurred during profile update. Please try again.");
            return "redirect:/updateAccount";
        }
    }

    private Customer getCustomerDetails(Connection connection, int custid) throws SQLException {
        String customerSql = "SELECT * FROM customer WHERE custid = ?";
        try (PreparedStatement customerStatement = connection.prepareStatement(customerSql)) {
            customerStatement.setInt(1, custid);
            try (ResultSet resultSet = customerStatement.executeQuery()) {
                if (resultSet.next()) {
                    Customer customer = new Customer();
                    customer.setCustid(resultSet.getInt("custid"));
                    customer.setCustname(resultSet.getString("custname"));
                    customer.setCustphoneno(resultSet.getString("custphoneno"));
                    customer.setCustpassword(resultSet.getString("custpassword"));
                    customer.setCustemail(resultSet.getString("custemail"));
                    return customer;
                } else {
                    return null; // Customer not found
                }
            }
        }
    }
}