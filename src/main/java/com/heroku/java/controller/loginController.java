
package com.heroku.java.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import com.heroku.java.DAO.CustomerDAO;
import com.heroku.java.bean.Customer;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.ui.Model;
import jakarta.servlet.http.HttpSession;
import javax.sql.DataSource;
import java.sql.SQLException;

@Controller
public class loginController {
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
        model.addAttribute("customer", new Customer());
        return "login";
    }

    @PostMapping("/login")
    public String login(HttpSession session, @RequestParam("custemail") String custemail,
            @RequestParam("custpassword") String custpassword, Model model) {
        try {
            CustomerDAO customerDAO = new CustomerDAO(dataSource);
            Customer customer = customerDAO.getCustomerByCustemail(custemail);

            if (customer == null) {
                model.addAttribute("error", "Email does not exist. Please register.");
                return "login";
            }

            if (!verifyPassword(custpassword, customer.getCustpassword())) {
                model.addAttribute("error", "Incorrect password.");
                return "login";
            }

            session.setAttribute(SESSION_USER_ID, customer.getCustid());
            session.setAttribute(SESSION_USER_EMAIL, custemail);

            System.out.println("Customer ID set in session during login: " + customer.getCustid());
            System.out.println(
                    "Customer ID retrieved immediately after setting: " + session.getAttribute(SESSION_USER_ID));

            return "redirect:/customerindex";
        } catch (SQLException e) {
            e.printStackTrace();
            model.addAttribute("error", "An error occurred. Please try again.");
            return "login";
        }
    }

    private boolean verifyPassword(String inputPassword, String storedPassword) {
        // Use secure password verification logic here
        return inputPassword.equals(storedPassword); // Replace this with a secure hash comparison in a real application
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        // Remove specific attributes
        session.removeAttribute(SESSION_USER_ID);
        session.removeAttribute(SESSION_USER_EMAIL);

        // Invalidate the entire session
        session.invalidate();

        return "redirect:/index";
    }
}