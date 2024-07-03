package com.heroku.java.DAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.sql.DataSource;

public class StaffLoginDAO {
    private DataSource dataSource;

    public StaffLoginDAO(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public boolean checkStaff(String staffemail, String staffpassword) throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            String sql = "SELECT COUNT(*) FROM staff WHERE staffemail = ? AND staffpassword = ?";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, staffemail);
            statement.setString(2, staffpassword);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                int rowCount = resultSet.getInt(1);
                return rowCount > 0;
            }
            connection.close();
        } catch (SQLException e) {
            throw e;
        }
        return false;
    }
}
