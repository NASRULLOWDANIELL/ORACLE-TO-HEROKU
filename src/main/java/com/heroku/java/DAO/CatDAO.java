package com.heroku.java.DAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import javax.sql.DataSource;

import com.heroku.java.bean.Cat;

public class CatDAO {
    private final DataSource dataSource;

    public CatDAO(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public class CatIdGenerator {
        private static AtomicInteger counter = new AtomicInteger(0);

        public static int getNextId() {
            return counter.incrementAndGet();
        }

        public static void setCounter(int value) {
            counter.set(value);
        }
    }

    // public Cat addCat(Cat cat) throws SQLException {
    // String sql = "INSERT INTO cat (catname, catbreed, catage, custid) VALUES (?,
    // ?, ?, ?)";
    // try (Connection connection = dataSource.getConnection();
    // PreparedStatement statement = connection.prepareStatement(sql, new String[] {
    // "catid" })) {
    // statement.setString(1, cat.getCatname());
    // statement.setString(2, cat.getCatbreed());
    // statement.setInt(3, cat.getCatage());
    // statement.setInt(4, cat.getCustid());

    // statement.executeUpdate();
    // try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
    // if (generatedKeys.next()) {
    // cat.setCatid(generatedKeys.getInt(1));
    // }
    // }
    // } catch (SQLException e) {
    // e.printStackTrace();
    // throw e;
    // }
    // return cat;
    // }

    public Cat addCat(Cat cat) throws SQLException {
        String sql = "INSERT INTO cat (catid, catname, catbreed, catage, custid) VALUES (?, ?, ?, ?, ?)";

        try (Connection connection = dataSource.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {

            int newId = CatIdGenerator.getNextId();
            cat.setCatid(newId);

            statement.setInt(1, newId);
            statement.setString(2, cat.getCatname());
            statement.setString(3, cat.getCatbreed());
            statement.setInt(4, cat.getCatage());
            statement.setInt(5, cat.getCustid());

            statement.executeUpdate();

            return cat;
        } catch (SQLException e) {
            e.printStackTrace();
            throw e;
        }
    }

    public void initializeIdGenerator() throws SQLException {
        String sql = "SELECT MAX(catid) FROM cat";
        try (Connection connection = dataSource.getConnection();
                Statement stmt = connection.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                int maxId = rs.getInt(1);
                CatIdGenerator.setCounter(maxId);
            }
        }
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
    public List<Cat> getCatsByCustid(int custid) throws SQLException {
        List<Cat> cats = new ArrayList<>();
        String sql = "SELECT c.catid, c.catname, c.catbreed, c.catage FROM cat c WHERE c.custid = ?";
        try (Connection connection = dataSource.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, custid);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    Cat cat = new Cat();
                    cat.setCatid(resultSet.getInt("catid"));
                    cat.setCatname(resultSet.getString("catname"));
                    cat.setCatbreed(resultSet.getString("catbreed"));
                    cat.setCatage(resultSet.getInt("catage"));
                    cats.add(cat);
                }
            }
        }
        return cats;
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
