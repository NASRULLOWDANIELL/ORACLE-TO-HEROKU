package com.heroku.java.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.ResponseEntity;

import com.heroku.java.bean.Room;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@RestController
public class RoomRestController {
    private static final Logger LOGGER = Logger.getLogger(RoomRestController.class.getName());

    private final DataSource dataSource;

    @Autowired
    public RoomRestController(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @GetMapping("/api/rooms")
    public ResponseEntity<List<Room>> getRoomList() {
        try (Connection connection = dataSource.getConnection()) {
            List<Room> rooms = getRoomList(connection);
            return ResponseEntity.ok(rooms);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error retrieving room list", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    private List<Room> getRoomList(Connection connection) throws SQLException {
        List<Room> rooms = new ArrayList<>();
        String sql = "SELECT * FROM room";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Room room = new Room(
                    rs.getInt("roomid"),
                    rs.getString("roomname"),
                    rs.getString("roomtype"),
                    rs.getString("roomdesc"),
                    rs.getDouble("roomprice")
                );
                rooms.add(room);
            }
        }
        return rooms;
    }
}