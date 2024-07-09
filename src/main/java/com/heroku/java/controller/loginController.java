package com.heroku.java.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import jakarta.servlet.http.HttpSession;
import javax.sql.DataSource;
import java.sql.SQLException;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jakarta.validation.Valid;
import java.util.regex.Pattern;
import org.springframework.web.bind.annotation.RequestParam;
import com.heroku.java.DAO.CustomerDAO;
import com.heroku.java.bean.Customer;

@Controller
public class loginController {
    private static final Logger logger = LoggerFactory.getLogger(loginController.class);
    private final DataSource dataSource;

    public static final String SESSION_USER_ID = "custid";
    public static final String SESSION_USER_EMAIL = "loggedInUser";

    private static final int MAX_LOGIN_ATTEMPTS = 10;
    private static final String LOGIN_ATTEMPTS = "loginAttempts";

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
    public String login(@Valid @ModelAttribute("customer") Customer customer, 
                        BindingResult bindingResult, 
                        HttpSession session, 
                        Model model) {
        if (bindingResult.hasErrors()) {
            return "login";
        }

        String custemail = customer.getCustemail();
        String custpassword = customer.getCustpassword();

        if (!isValidEmail(custemail)) {
            model.addAttribute("emailError", "Invalid email format.");
            return "login";
        }

        if (!isValidPassword(custpassword)) {
            model.addAttribute("passwordError", "Invalid password format.");
            return "login";
        }

        Integer loginAttempts = (Integer) session.getAttribute(LOGIN_ATTEMPTS);
        if (loginAttempts == null) {
            loginAttempts = 0;
        }

        if (loginAttempts >= MAX_LOGIN_ATTEMPTS) {
            model.addAttribute("error", "Too many failed login attempts. Please try again later.");
            return "login";
        }

        try {
            CustomerDAO customerDAO = new CustomerDAO(dataSource);
            Customer dbCustomer = customerDAO.getCustomerByCustemail(custemail);

            if (dbCustomer == null) {
                model.addAttribute("emailError", "Email does not exist. Please register.");
                return "login";
            }

            if (!verifyPassword(custpassword, dbCustomer.getCustpassword())) {
                loginAttempts++;
                session.setAttribute(LOGIN_ATTEMPTS, loginAttempts);
                model.addAttribute("passwordError", "Incorrect password.");
                return "login";
            }

            session.setAttribute(SESSION_USER_ID, dbCustomer.getCustid());
            session.setAttribute(SESSION_USER_EMAIL, custemail);
            session.removeAttribute(LOGIN_ATTEMPTS);

            logger.info("Customer ID set in session during login: {}", dbCustomer.getCustid());
            logger.info("Customer ID retrieved immediately after setting: {}", session.getAttribute(SESSION_USER_ID));

            return "redirect:/customerindex";
        } catch (SQLException e) {
            logger.error("SQL error during login", e);
            model.addAttribute("error", "An error occurred. Please try again.");
            return "login";
        }
    }

    private boolean isValidEmail(String email) {
        String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
        Pattern pat = Pattern.compile(emailRegex);
        return email != null && pat.matcher(email).matches();
    }

    private boolean isValidPassword(String password) {
        // Implement password validation rules here
        return password != null && password.length() >= 8;
    }

    private boolean verifyPassword(String inputPassword, String storedPassword) {
        return BCrypt.checkpw(inputPassword, storedPassword);
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.removeAttribute(SESSION_USER_ID);
        session.removeAttribute(SESSION_USER_EMAIL);
        session.invalidate();
        return "redirect:/";
    }
}