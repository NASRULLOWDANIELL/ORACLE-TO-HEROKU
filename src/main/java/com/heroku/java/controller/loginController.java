
package com.heroku.java.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.ui.Model;
import jakarta.servlet.http.HttpSession;
import javax.sql.DataSource;
import java.sql.SQLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.heroku.java.DAO.CustomerDAO;
import com.heroku.java.bean.Customer;

@Controller
public class loginController {
    private static final Logger logger = LoggerFactory.getLogger(loginController.class);
    private final DataSource dataSource;

    // Constants for session attribute names
    public static final String SESSION_USER_ID = "custid";
    public static final String SESSION_USER_EMAIL = "loggedInUser";

    @Autowired
    public loginController(DataSource dataSource) {
        this.dataSource = dataSource;

    }

    @GetMapping("/login")
    public String showLoginForm(Model model) {
        System.out.println("Login form requested");
        if (!model.containsAttribute("customer")) {
            model.addAttribute("customer", new Customer());
        }
        return "login";
    }

    @PostMapping("/login")
    public String login(HttpSession session, @RequestParam("custemail") String custemail,
            @RequestParam("custpassword") String custpassword, Model model) {
        try {
            logger.info("Attempting customer login for email: {}", custemail);
            CustomerDAO customerDAO = new CustomerDAO(dataSource);
            Customer customer = customerDAO.getCustomerByCustemail(custemail);

            if (customer == null) {
                logger.warn("Login attempt failed: Email does not exist: {}", custemail);
                model.addAttribute("error", "Email does not exist. Please register.");
                return "login";
            }

            if (!verifyPassword(custpassword, customer.getCustpassword())) {
                logger.warn("Login attempt failed: Incorrect password for email: {}", custemail);
                model.addAttribute("error", "Incorrect password.");
                return "login";
            }

            session.setAttribute(SESSION_USER_ID, customer.getCustid());
            session.setAttribute(SESSION_USER_EMAIL, custemail);

            logger.info("Customer login successful for email: {}", custemail);
            return "redirect:/customerindex";
        } catch (SQLException e) {
            logger.error("SQL error during customer login", e);
            model.addAttribute("error", "An error occurred. Please try again.");
            return "login";
        } catch (Exception e) {
            logger.error("Unexpected error during customer login", e);
            model.addAttribute("error", "An unexpected error occurred. Please try again.");
            return "login";
        }
    }

    private boolean verifyPassword(String inputPassword, String storedPassword) {
        // Use a secure password hashing library like BCrypt in a real application
        return inputPassword.equals(storedPassword);
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        logger.info("Customer logout requested");
        session.removeAttribute(SESSION_USER_ID);
        session.removeAttribute(SESSION_USER_EMAIL);
        session.invalidate();
        return "redirect:/";
    }
}