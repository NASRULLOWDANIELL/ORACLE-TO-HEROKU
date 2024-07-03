package com.heroku.java.DAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.sql.DataSource;

public class LoginDAO {
    private DataSource dataSource;

    public LoginDAO(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public boolean checkCustomer(String custemail, String custpassword) throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            String sql = "SELECT COUNT(*) FROM customer WHERE custemail = ? AND custpassword = ?";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, custemail);
            statement.setString(2, custpassword);
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
