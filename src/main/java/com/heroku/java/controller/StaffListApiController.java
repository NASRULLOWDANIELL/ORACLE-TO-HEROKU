package com.heroku.java.controller;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.http.ResponseEntity;
import org.springframework.beans.factory.annotation.Autowired;

import com.heroku.java.bean.Staff;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

@RestController
public class StaffListApiController {
    private static final Logger LOGGER = Logger.getLogger(StaffListApiController.class.getName());

    private final DataSource dataSource;

    @Autowired
    public StaffListApiController(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @GetMapping("/api/staff")
    public ResponseEntity<List<Staff>> listStaff() {
        List<Staff> staffList = new ArrayList<>();

        try (Connection connection = this.dataSource.getConnection()) {
            String sql = "SELECT * FROM staff";
            try (PreparedStatement statement = connection.prepareStatement(sql);
                 ResultSet resultSet = statement.executeQuery()) {

                while (resultSet.next()) {
                    Staff staff = new Staff();
                    staff.setStaffid(resultSet.getInt("staffid"));
                    staff.setStaffname(resultSet.getString("staffname"));
                    staff.setStaffemail(resultSet.getString("staffemail"));
                    staff.setStaffphoneno(resultSet.getString("staffphoneno"));
                    staff.setStaffrole(resultSet.getString("staffrole"));
                    staff.setManagerid(resultSet.getInt("managerid"));
                    staffList.add(staff);
                }
            }
            return ResponseEntity.ok(staffList);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error retrieving staff list: " + e.getMessage(), e);
            return ResponseEntity.status(500).body(null);
        }
    }
}