package com.heroku.java.DAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import com.heroku.java.bean.Customer;
import com.heroku.java.bean.IdGenerator;

public class CustomerDAO {
    private final DataSource dataSource;

    public CustomerDAO(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    // public Customer addCustomer(Customer customer) throws SQLException {
    // try (Connection connection = dataSource.getConnection()) {
    // String sql = "INSERT INTO customer (custname, custemail, custpassword,
    // custphoneno) "
    // + "VALUES (?, ?, ?, ?)";
    // try (PreparedStatement pstmt = connection.prepareStatement(sql,
    // Statement.RETURN_GENERATED_KEYS)) {
    // pstmt.setString(1, customer.getCustname());
    // pstmt.setString(2, customer.getCustemail());
    // pstmt.setString(3, customer.getCustpassword());
    // pstmt.setString(4, customer.getCustphoneno());

    // pstmt.executeUpdate();
    // try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
    // if (generatedKeys.next()) {
    // int generatedId = generatedKeys.getInt(1);
    // customer.setCustid(generatedId);
    // }
    // }
    // }
    // return customer;
    // } catch (SQLException e) {
    // e.printStackTrace();
    // throw e;
    // }
    // }
    public Customer addCustomer(Customer customer) throws SQLException {
        String sql = "INSERT INTO customer (custid, custname, custemail, custpassword, custphoneno) VALUES (?, ?, ?, ?, ?)";

        try (Connection connection = dataSource.getConnection();
                PreparedStatement pstmt = connection.prepareStatement(sql)) {

            int newId = IdGenerator.getNextId();
            customer.setCustid(newId);

            pstmt.setInt(1, newId);
            pstmt.setString(2, customer.getCustname());
            pstmt.setString(3, customer.getCustemail());
            pstmt.setString(4, customer.getCustpassword());
            pstmt.setString(5, customer.getCustphoneno());

            pstmt.executeUpdate();

            return customer;
        } catch (SQLException e) {
            e.printStackTrace();
            throw e;
        }
    }

    // Method to initialize the ID generator based on the max ID in the database
    public void initializeIdGenerator() throws SQLException {
        String sql = "SELECT MAX(custid) FROM customer";
        try (Connection connection = dataSource.getConnection();
                Statement stmt = connection.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                int maxId = rs.getInt(1);
                IdGenerator.setCounter(maxId);
            }
        }
    }

    public Customer getCustomerByCustemail(String custemail) throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            String sql = "SELECT * FROM customer WHERE custemail = ?";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, custemail);

            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                Customer customer = new Customer();
                // customer.setCustid(resultSet.getInt("custid"));
                customer.setCustid(resultSet.getInt("custid"));
                customer.setCustname(resultSet.getString("custname"));
                customer.setCustemail(resultSet.getString("custemail"));
                customer.setCustpassword(resultSet.getString("custpassword"));
                customer.setCustphoneno(resultSet.getString("custphoneno"));
                return customer;
            }
            connection.close();
            return null;
        } catch (SQLException e) {
            e.printStackTrace();
            throw e;
        }
    }

    public void updateCustomer(Customer customer) throws SQLException {
        String sql = "UPDATE customer SET custname = ?, custpassword = ?, custphoneno = ? WHERE custemail = ?";
        try (Connection connection = dataSource.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, customer.getCustname());
            statement.setString(2, customer.getCustpassword());
            statement.setString(3, customer.getCustphoneno());
            statement.setString(4, customer.getCustemail());
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            throw e;
        }
    }

    public List<Customer> getAllCustomers() throws SQLException {
        List<Customer> customers = new ArrayList<>();

        try (Connection connection = dataSource.getConnection()) {
            String sql = "SELECT * FROM customer order by custid";
            PreparedStatement statement = connection.prepareStatement(sql);
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                Customer customer = new Customer();
                customer.setCustname(resultSet.getString("custname"));
                customer.setCustid(resultSet.getInt("custid"));
                customer.setCustemail(resultSet.getString("custemail"));
                customer.setCustpassword(resultSet.getString("custpassword"));
                customer.setCustphoneno(resultSet.getString("custphoneno"));

                customers.add(customer);
            }
            connection.close();
        } catch (SQLException e) {
            throw new SQLException("Error retrieving rentals: " + e.getMessage());
        }

        return customers;
    }

    public Customer getCustomerByEmail(String custemail) throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            String sql = "SELECT * FROM customer WHERE custemail = ?";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, custemail);

            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                Customer customer = new Customer();
                customer.setCustid(resultSet.getInt("custid"));
                customer.setCustname(resultSet.getString("custname"));
                customer.setCustemail(resultSet.getString("custemail"));
                customer.setCustpassword(resultSet.getString("custpassword"));
                customer.setCustphoneno(resultSet.getString("custphoneno"));
                // Set any other properties of the Customer object based on the ResultSet

                return customer;
            }
            connection.close();
            return null; // Return null if the customer is not found
        } catch (SQLException e) {
            // Handle any exceptions or errors that occurred during the database operation
            e.printStackTrace();
            throw e;
        }
    }

    public int getCatIdByCustomerId(int customerId) throws SQLException {
        String query = "SELECT catid FROM Cats WHERE custid = ?";
        try (Connection conn = dataSource.getConnection();
                PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, customerId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("catid");
                } else {
                    throw new SQLException("No cat found for customer ID: " + customerId);
                }
            }
        }
    }
}
