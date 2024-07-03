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

import com.heroku.java.bean.Staff;

public class StaffDAO {
    private final DataSource dataSource;

    public StaffDAO(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public class StaffIdGenerator {
        private static AtomicInteger counter = new AtomicInteger(0);

        public static int getNextId() {
            return counter.incrementAndGet();
        }

        public static void setCounter(int value) {
            counter.set(value);
        }
    }

    // public Staff addStaff(Staff staff) throws SQLException {
    // try (Connection connection = dataSource.getConnection()) {
    // String sql = "INSERT INTO staff (staffname, staffemail, staffpassword,
    // staffphoneno, staffrole) "
    // + "VALUES (?, ?, ?, ?, ?)";
    // try (PreparedStatement pstmt = connection.prepareStatement(sql,
    // Statement.RETURN_GENERATED_KEYS)) {
    // pstmt.setString(1, staff.getStaffname());
    // pstmt.setString(2, staff.getStaffemail());
    // pstmt.setString(3, staff.getStaffpassword());
    // pstmt.setString(4, staff.getStaffphoneno());
    // pstmt.setString(5, staff.getStaffrole());

    // pstmt.executeUpdate();
    // try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
    // if (generatedKeys.next()) {
    // int generatedId = generatedKeys.getInt(1);
    // staff.setStaffid(generatedId);
    // }
    // }
    // }
    // return staff;
    // } catch (SQLException e) {
    // e.printStackTrace();
    // throw e;
    // }
    // }

    public Staff addStaff(Staff staff) throws SQLException {
        String sql = "INSERT INTO staff (staffid, staffname, staffemail, staffpassword, staffphoneno, staffrole) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection connection = dataSource.getConnection();
                PreparedStatement pstmt = connection.prepareStatement(sql)) {

            int newId = StaffIdGenerator.getNextId();
            staff.setStaffid(newId);

            pstmt.setInt(1, newId);
            pstmt.setString(2, staff.getStaffname());
            pstmt.setString(3, staff.getStaffemail());
            pstmt.setString(4, staff.getStaffpassword());
            pstmt.setString(5, staff.getStaffphoneno());
            pstmt.setString(6, staff.getStaffrole());

            pstmt.executeUpdate();

            return staff;
        } catch (SQLException e) {
            e.printStackTrace();
            throw e;
        }
    }

    public void initializeIdGenerator() throws SQLException {
        String sql = "SELECT MAX(staffid) FROM staff";
        try (Connection connection = dataSource.getConnection();
                Statement stmt = connection.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                int maxId = rs.getInt(1);
                StaffIdGenerator.setCounter(maxId);
            }
        }
    }

    public Staff getStaffByStaffemail(String staffemail) throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            String sql = "SELECT * FROM staff WHERE staffemail = ?";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, staffemail);

            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                Staff staff = new Staff();
                staff.setStaffid(resultSet.getInt("staffid"));
                staff.setStaffname(resultSet.getString("staffname"));
                staff.setStaffemail(resultSet.getString("staffemail"));
                staff.setStaffpassword(resultSet.getString("staffpassword"));
                staff.setStaffphoneno(resultSet.getString("staffphoneno"));
                staff.setStaffrole(resultSet.getString("staffrole"));
                return staff;
            }
            return null;
        } catch (SQLException e) {
            e.printStackTrace();
            throw e;
        }
    }

    public void updateStaff(Staff staff) throws SQLException {
        String sql = "UPDATE staff SET staffname = ?, staffpassword = ?, staffphoneno = ?, managerid = ? WHERE staffemail = ?";
        try (Connection connection = dataSource.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, staff.getStaffname());
            statement.setString(2, staff.getStaffpassword());
            statement.setString(3, staff.getStaffphoneno());
            if (staff.getManagerid() != null) {
                statement.setInt(4, staff.getManagerid());
            } else {
                statement.setNull(4, java.sql.Types.INTEGER);
            }
            statement.setString(5, staff.getStaffemail());
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            throw e;
        }
    }

    public List<Staff> getAllManagerStaffs() throws SQLException {
        List<Staff> staffs = new ArrayList<>();

        try (Connection connection = dataSource.getConnection()) {
            String sql = "SELECT * FROM staff WHERE staffrole = 'Manager' ORDER BY staffid";
            try (PreparedStatement statement = connection.prepareStatement(sql);
                    ResultSet resultSet = statement.executeQuery()) {

                while (resultSet.next()) {
                    Staff staff = new Staff();
                    staff.setStaffid(resultSet.getInt("staffid"));
                    staff.setStaffname(resultSet.getString("staffname"));
                    staff.setStaffemail(resultSet.getString("staffemail"));
                    staff.setStaffpassword(resultSet.getString("staffpassword"));
                    staff.setStaffphoneno(resultSet.getString("staffphoneno"));
                    staff.setStaffrole(resultSet.getString("staffrole"));

                    staffs.add(staff);
                }
            }
        } catch (SQLException e) {
            throw new SQLException("Error retrieving staff: " + e.getMessage());
        }

        return staffs;
    }

}