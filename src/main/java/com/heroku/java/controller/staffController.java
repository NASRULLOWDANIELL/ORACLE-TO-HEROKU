package com.heroku.java.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import com.heroku.java.bean.Staff;
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
public class staffController {

    public static final String SESSION_STAFF_ID = "staffid";
    private static final Logger LOGGER = Logger.getLogger(customerController.class.getName());
    private final DataSource dataSource;

    @Autowired
    public staffController(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @GetMapping("/registerStaff")
    public String showRegisterForm(Model model) {
        model.addAttribute("staff", new Staff());
        return "registerStaff"; // Ensure this matches your HTML file name
    }

    @PostMapping("/registerStaff")
    public String registerStaff(@ModelAttribute("registerStaff") Staff staff, Model model) {
        LOGGER.log(Level.INFO, "Starting staff registration for email: {0}", staff.getStaffemail());
        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(false); // Start transaction

            // Generate unique staffid
            int staffid = generateStaffID(connection);
            if (staffid == 0) {
                model.addAttribute("error", "Unable to generate staff ID. Please try again.");
                return "registerStaff";
            }

            // Insert into staff table
            String staffSql = "INSERT INTO staff (staffid, staffname, staffphoneno, staffemail, staffpassword, staffrole) VALUES (?, ?, ?, ?, ?, ?)";
            try (PreparedStatement staffStatement = connection.prepareStatement(staffSql)) {
                staffStatement.setInt(1, staffid);
                staffStatement.setString(2, staff.getStaffname());
                staffStatement.setString(3, staff.getStaffphoneno());
                staffStatement.setString(4, staff.getStaffemail());
                staffStatement.setString(5, staff.getStaffpassword());
                staffStatement.setString(6, staff.getStaffrole());

                int staffRowsAffected = staffStatement.executeUpdate();
                if (staffRowsAffected == 0) {
                    LOGGER.log(Level.SEVERE, "Staff registration failed: {0}", staff.getStaffemail());
                    model.addAttribute("error", "Registration failed. Please try again.");
                    connection.rollback(); // Rollback transaction
                    return "registerStaff";
                }
            }

            connection.commit(); // Commit transaction
            LOGGER.log(Level.INFO, "Staff registered successfully: {0}", staff.getStaffemail());
            return "redirect:/loginStaff";
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "SQL Error during staff registration: " + e.getMessage(), e);
            model.addAttribute("error", "An error occurred during registration: " + e.getMessage());
            return "registerStaff";
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Unexpected Error during staff registration: " + e.getMessage(), e);
            model.addAttribute("error", "An unexpected error occurred: " + e.getMessage());
            return "registerStaff";
        }
    }

    private int generateStaffID(Connection connection) throws SQLException {
        String query = "SELECT MAX(staffid) FROM staff";
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

    @GetMapping("/viewstaffaccount")
    public String viewStaffProfile(HttpSession session, Model model) {
        LOGGER.info("Entering viewStaffProfile method");

        Integer staffid = (Integer) session.getAttribute(SESSION_STAFF_ID);
        LOGGER.info("Retrieved staffid from session: " + staffid);

        if (staffid == null) {
            LOGGER.warning("No staff ID in session, redirecting to login");
            return "redirect:/loginStaff";
        }

        try (Connection connection = dataSource.getConnection()) {
            Staff staff = getStaffDetails(connection, staffid);
            if (staff == null) {
                LOGGER.warning("No staff found for ID: " + staffid + ", redirecting to login");
                return "redirect:/loginStaff";
            }
            model.addAttribute("staff", staff);
            LOGGER.info("Returning viewstaffaccount view");
            return "viewstaffaccount";
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "SQL exception in viewStaffProfile", e);
            model.addAttribute("error", "An error occurred while retrieving your account details.");
            return "error";
        }
    }

    @GetMapping("/updateStaffAccount")
    public String updateStaffAccount(HttpSession session, Model model) {
        Integer staffid = (Integer) session.getAttribute(SESSION_STAFF_ID);
        if (staffid == null) {
            LOGGER.warning("No staff ID in session, redirecting to login");
            return "redirect:/loginStaff";
        }

        try (Connection connection = dataSource.getConnection()) {
            Staff staff = getStaffDetails(connection, staffid);
            if (staff == null) {
                return "redirect:/loginStaff";
            }
            model.addAttribute("staff", staff);

            List<Staff> allManagers = getAllManagers(connection);
            LOGGER.info("Retrieved " + allManagers.size() + " managers for dropdown");
            model.addAttribute("managers", allManagers);

            return "updateStaffAccount";
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error updating staff account", e);
            model.addAttribute("error", "An error occurred while retrieving your account details.");
            return "error";
        }
    }

    private List<Staff> getAllManagers(Connection connection) throws SQLException {
        List<Staff> managers = new ArrayList<>();
        String sql = "SELECT staffid, staffname FROM staff WHERE staffrole = 'Manager'";
        try (PreparedStatement stmt = connection.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                Staff manager = new Staff();
                manager.setStaffid(rs.getInt("staffid"));
                manager.setStaffname(rs.getString("staffname"));
                managers.add(manager);
            }
        }
        return managers;
    }

    @PostMapping("/updateStaffAccount")
    public String updateStaffAccount(@ModelAttribute("staff") Staff staff, Model model, HttpSession session) {
        Integer staffid = (Integer) session.getAttribute(SESSION_STAFF_ID);
        LOGGER.info("Updating profile. Session staff ID: " + staffid + ", Form staff ID: " + staff.getStaffid());

        if (staffid == null || !staffid.equals(staff.getStaffid())) {
            LOGGER.warning("Staff ID mismatch. Session: " + staffid + ", Form: " + staff.getStaffid());
            model.addAttribute("error", "Session expired or invalid data. Please log in again.");
            return "redirect:/loginStaff";
        }

        try (Connection connection = dataSource.getConnection()) {
            String updateStaffSql = "UPDATE staff SET staffname = ?, staffpassword = ?, managerid = ?, staffphoneno = ? WHERE staffid = ?";
            try (PreparedStatement updateStaffStatement = connection.prepareStatement(updateStaffSql)) {
                updateStaffStatement.setString(1, staff.getStaffname());
                updateStaffStatement.setString(2, staff.getStaffpassword());
                if (staff.getManagerid() != null) {
                    updateStaffStatement.setInt(3, staff.getManagerid());
                } else {
                    updateStaffStatement.setNull(3, java.sql.Types.INTEGER);
                }
                updateStaffStatement.setString(4, staff.getStaffphoneno());
                updateStaffStatement.setInt(5, staffid);

                int rowsAffected = updateStaffStatement.executeUpdate();
                if (rowsAffected == 0) {
                    LOGGER.warning("No rows affected when updating staff profile");
                    model.addAttribute("error", "Failed to update profile. Please try again.");
                    List<Staff> allManagers = getAllManagers(connection);
                    model.addAttribute("managers", allManagers);
                    return "updateStaffAccount";
                }
            }
            LOGGER.info("Staff profile updated successfully");
            return "redirect:/viewstaffaccount";
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error updating staff profile", e);
            model.addAttribute("error", "An error occurred during profile update: " + e.getMessage());
            try (Connection connection = dataSource.getConnection()) {
                List<Staff> allManagers = getAllManagers(connection);
                model.addAttribute("managers", allManagers);
            } catch (SQLException ex) {
                LOGGER.log(Level.SEVERE, "Error retrieving managers", ex);
            }
            return "updateStaffAccount";
        }
    }

    private Staff getStaffDetails(Connection connection, Integer staffid) throws SQLException {
        String staffSql = "SELECT * FROM staff WHERE staffid = ?";
        try (PreparedStatement staffStatement = connection.prepareStatement(staffSql)) {
            staffStatement.setInt(1, staffid);
            try (ResultSet resultSet = staffStatement.executeQuery()) {
                if (resultSet.next()) {
                    Staff staff = new Staff();
                    staff.setStaffid(resultSet.getInt("staffid"));
                    staff.setStaffname(resultSet.getString("staffname"));
                    staff.setStaffemail(resultSet.getString("staffemail"));
                    staff.setStaffpassword(resultSet.getString("staffpassword").toString());
                    staff.setStaffrole(resultSet.getString("staffrole"));
                    staff.setStaffphoneno(resultSet.getString("staffphoneno"));
                    int managerid = resultSet.getInt("managerid");
                    if (!resultSet.wasNull()) {
                        staff.setManagerid(managerid);
                    }
                    return staff;
                } else {
                    return null; // Staff not found
                }
            }
        }
    }
}