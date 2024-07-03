package com.heroku.java.DAO;

import java.sql.Connection;
// import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import com.heroku.java.bean.Booking;
import com.heroku.java.bean.Cat;

public class BookingDAO {
    private final DataSource dataSource;

    public BookingDAO(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public Booking addBooking(Booking booking) throws SQLException {
        String sql = "INSERT INTO booking (bookingCheckInDate, bookingCheckOutDate, bookingPrice, catid, roomid, paymentstatus) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection connection = dataSource.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql,
                        PreparedStatement.RETURN_GENERATED_KEYS)) {

            // Set parameters
            int parameterIndex = 1;
            statement.setDate(parameterIndex++, booking.getBookingCheckInDate());
            statement.setDate(parameterIndex++, booking.getBookingCheckOutDate());
            statement.setBigDecimal(parameterIndex++, booking.getBookingPrice());
            statement.setInt(parameterIndex++, booking.getCatid()); // Changed to setInt
            statement.setInt(parameterIndex++, booking.getRoomid()); // Changed to setInt
            statement.setString(parameterIndex, booking.getPaymentstatus());

            // Execute the insert statement
            int affectedRows = statement.executeUpdate();

            if (affectedRows == 0) {
                throw new SQLException("Creating booking failed, no rows affected.");
            }

            // Retrieve auto-generated keys
            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    booking.setBookingid(generatedKeys.getInt(1));
                } else {
                    throw new SQLException("Creating booking failed, no ID obtained.");
                }
            }
        } catch (SQLException e) {
            // Log the error here if you have a logging system
            throw new SQLException("Error creating booking: " + e.getMessage(), e);
        }

        return booking;
    }

    public Cat getCatByCatid(Integer catid) throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            String sql = "SELECT * FROM cat WHERE catid = ?";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setInt(1, catid);

            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                Cat cat = new Cat();
                cat.setCatid(resultSet.getInt("catid"));
                cat.setCatname(resultSet.getString("catname"));
                cat.setCatbreed(resultSet.getString("catbreed"));
                cat.setCatage(resultSet.getInt("catage"));
                return cat;
            }
            connection.close();
            return null;
        } catch (SQLException e) {
            e.printStackTrace();
            throw e;
        }
    }

    public void updateCat(Cat cat) throws SQLException {
        String sql = "UPDATE cat SET catname = ?, catbreed = ?, catage = ? WHERE catid = ?";
        try (Connection connection = dataSource.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, cat.getCatname());
            statement.setString(2, cat.getCatbreed());
            statement.setInt(3, cat.getCatage());
            statement.setInt(4, cat.getCatid());

            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            throw e;
        }
    }

    // Fetch cats based on custid
    public List<Booking> getBookingsByCustid(int custid) throws SQLException {
        List<Booking> bookings = new ArrayList<>();
        String sql = "SELECT b.bookingid, b.bookingCheckInDate, b.bookingCheckOutDate, b.bookingPrice, b.catid, b.roomid, b.paymentstatus "
                +
                "FROM booking b JOIN cat c ON b.catid = c.catid " +
                "WHERE c.custid = ?";

        try (Connection connection = dataSource.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, custid);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    Booking booking = new Booking();
                    booking.setBookingid(resultSet.getInt("bookingid"));
                    booking.setBookingCheckInDate(resultSet.getDate("bookingCheckInDate"));
                    booking.setBookingCheckOutDate(resultSet.getDate("bookingCheckOutDate"));
                    booking.setBookingPrice(resultSet.getBigDecimal("bookingPrice"));
                    booking.setCatid(resultSet.getInt("catid"));
                    booking.setRoomid(resultSet.getInt("roomid"));
                    booking.setPaymentstatus(resultSet.getString("paymentstatus"));
                    bookings.add(booking);
                }
            }
        }
        return bookings;
    }

    public void deleteCat(int catid) throws SQLException {
        String sql = "DELETE FROM cat WHERE catid = ?";
        try (Connection connection = dataSource.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, catid);
            statement.executeUpdate();
        }
    }
}

// public Cat getCustomerByEmail(String custemail) throws SQLException {
// try (Connection connection = dataSource.getConnection()) {
// String sql = "SELECT * FROM customer WHERE custemail = ?";
// PreparedStatement statement = connection.prepareStatement(sql);
// statement.setString(1, custemail);

// ResultSet resultSet = statement.executeQuery();

// if (resultSet.next()) {
// Cat customer = new Cat();
// customer.setCustid(resultSet.getInt("custid"));
// customer.setCustname(resultSet.getString("custname"));
// customer.setCustemail(resultSet.getString("custemail"));
// customer.setCustpassword(resultSet.getString("custpassword"));
// customer.setCustphoneno(resultSet.getString("custphoneno"));
// // Set any other properties of the Customer object based on the ResultSet

// return customer;
// }
// connection.close();
// return null; // Return null if the customer is not found
// } catch (SQLException e) {
// // Handle any exceptions or errors that occurred during the database
// operation
// e.printStackTrace();
// throw e;
// }
// }
