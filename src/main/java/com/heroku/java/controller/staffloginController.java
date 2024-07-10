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
import com.heroku.java.DAO.StaffDAO;
import com.heroku.java.bean.Staff;

@Controller
public class staffloginController {
    private static final Logger logger = LoggerFactory.getLogger(staffloginController.class);
    private final DataSource dataSource;

    // Constants for session attribute names
    public static final String SESSION_STAFF_ID = "staffid";
    public static final String SESSION_STAFF_EMAIL = "loggedInStaff";

    @Autowired
    public staffloginController(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @GetMapping("/loginStaff")
    public String loginForm(Model model) {
        System.out.println("Login form requested");
        if (!model.containsAttribute("staff")) {
            model.addAttribute("staff", new Staff());
        }
        return "loginStaff";
    }

    @PostMapping("/loginStaff")
    public String login(HttpSession session, @RequestParam("staffemail") String staffemail,
            @RequestParam("staffpassword") String staffpassword, Model model) {
        try {
            logger.info("Attempting staff login for email: {}", staffemail);
            StaffDAO staffDAO = new StaffDAO(dataSource);
            Staff staff = staffDAO.getStaffByStaffemail(staffemail);
            model.addAttribute("staff", new Staff());

            if (staff == null) {
                logger.warn("Login attempt failed: Email does not exist: {}", staffemail);
                model.addAttribute("error", "Email does not exist. Please register.");
                return "loginStaff";
            }

            if (!verifyPassword(staffpassword, staff.getStaffpassword())) {
                logger.warn("Login attempt failed: Incorrect password for email: {}", staffemail);
                model.addAttribute("error", "Incorrect password.");
                return "loginStaff";
            }

            session.setAttribute(SESSION_STAFF_ID, staff.getStaffid());
            session.setAttribute(SESSION_STAFF_EMAIL, staffemail);

            logger.info("Staff login successful for email: {}", staffemail);
            return "redirect:/admindashboard";
        } catch (SQLException e) {
            logger.error("SQL error during staff login", e);
            model.addAttribute("error", "An error occurred. Please try again.");
            return "loginStaff";
        } catch (Exception e) {
            logger.error("Unexpected error during staff login", e);
            model.addAttribute("error", "An unexpected error occurred. Please try again.");
            return "loginStaff";
        }
    }

    private boolean verifyPassword(String inputPassword, String storedPassword) {
        // Use a secure password hashing library like BCrypt in a real application
        return inputPassword.equals(storedPassword);
    }

    @GetMapping("/logoutStaff")
    public String logoutStaff(HttpSession session) {
        logger.info("Staff logout requested");
        session.removeAttribute(SESSION_STAFF_ID);
        session.removeAttribute(SESSION_STAFF_EMAIL);
        session.invalidate();
        return "redirect:/";
    }

}