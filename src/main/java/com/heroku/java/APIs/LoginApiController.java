package com.heroku.java.APIs;

import org.springframework.web.bind.annotation.RestController;
import com.heroku.java.DAO.CustomerDAO;
import com.heroku.java.DAO.StaffDAO;
import com.heroku.java.bean.Customer;
import com.heroku.java.bean.Staff;
import com.heroku.java.bean.LoginRequest;
import com.heroku.java.bean.LoginResponse;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.http.ResponseEntity;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

@RestController
public class LoginApiController {
    private final DataSource dataSource;

    @Autowired
    public LoginApiController(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @PostMapping("/api/login")
    public ResponseEntity<?> apiLogin(@RequestBody LoginRequest loginRequest) {
        try {
            CustomerDAO customerDAO = new CustomerDAO(dataSource);
            Customer customer = customerDAO.getCustomerByCustemail(loginRequest.getEmail());
            if (customer == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new LoginResponse(false, "Email does not exist", null, null, null));
            }
            if (!verifyPassword(loginRequest.getPassword(), customer.getCustpassword())) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new LoginResponse(false, "Incorrect password", null, null, null));
            }
            return ResponseEntity.ok(new LoginResponse(true, "Login successful", 
                customer.getCustemail(), customer.getCustid(), customer.getCustpassword()));
        } catch (SQLException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new LoginResponse(false, "An error occurred", null, null, null));
        }
    }

    @PostMapping("/api/staff-login")
    public ResponseEntity<?> apiStaffLogin(@RequestBody LoginRequest loginRequest) {
        try {
            StaffDAO staffDAO = new StaffDAO(dataSource);
            Staff staff = staffDAO.getStaffByStaffemail(loginRequest.getEmail());
            if (staff == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new LoginResponse(false, "Email does not exist", null, null, null));
            }
            if (!verifyPassword(loginRequest.getPassword(), staff.getStaffpassword())) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new LoginResponse(false, "Incorrect password", null, null, null));
            }
            return ResponseEntity.ok(new LoginResponse(true, "Staff login successful", 
                staff.getStaffemail(), staff.getStaffid(), staff.getStaffpassword()));
        } catch (SQLException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new LoginResponse(false, "An error occurred", null, null, null));
        }
    }

    private boolean verifyPassword(String inputPassword, String storedPassword) {
        // Implement secure password verification here
        return inputPassword.equals(storedPassword); // This is not secure, replace with proper hashing
    }
}