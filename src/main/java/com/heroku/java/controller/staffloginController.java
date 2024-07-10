package com.heroku.java.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import com.heroku.java.DAO.StaffDAO;
import com.heroku.java.bean.Staff;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.ui.Model;
import jakarta.servlet.http.HttpSession;
import javax.sql.DataSource;
import java.sql.SQLException;

@Controller
public class staffloginController {
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
            StaffDAO staffDAO = new StaffDAO(dataSource);
            Staff staff = staffDAO.getStaffByStaffemail(staffemail);
            model.addAttribute("staff", new Staff());

            if (staff == null) {
                model.addAttribute("error", "Email does not exist. Please register.");
                return "loginStaff";
            }

            // Add this block to verify the password
            if (!verifyPassword(staffpassword, staff.getStaffpassword())) {
                model.addAttribute("error", "Incorrect password.");
                return "loginStaff";
            }

            session.setAttribute(SESSION_STAFF_ID, staff.getStaffid());
            session.setAttribute(SESSION_STAFF_EMAIL, staffemail);

            System.out.println("Staff ID set in session during login: " + staff.getStaffid());
            System.out
                    .println("Staff ID retrieved immediately after setting: " + session.getAttribute(SESSION_STAFF_ID));

            return "redirect:/admindashboard";
        } catch (SQLException e) {
            e.printStackTrace();
            model.addAttribute("error", "An error occurred. Please try again.");
            return "loginStaff";
        }
    }

    // Add this method to the staffloginController
    private boolean verifyPassword(String inputPassword, String storedPassword) {
        // Use secure password verification logic here
        return inputPassword.equals(storedPassword); // Replace this with a secure hash comparison in a real application
    }

    @GetMapping("/logoutStaff")
    public String logoutStaff(HttpSession session) {
        // Remove specific attributes
        session.removeAttribute(SESSION_STAFF_ID);
        session.removeAttribute(SESSION_STAFF_EMAIL);

        // Invalidate the entire session
        session.invalidate();

        return "redirect:/";
    }

}