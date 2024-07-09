package com.heroku.java.controller;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.http.ResponseEntity;
import org.springframework.beans.factory.annotation.Autowired;

import com.heroku.java.bean.Cat;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

@RestController
public class CatApiController {
    private static final Logger LOGGER = Logger.getLogger(CatApiController.class.getName());

    private final DataSource dataSource;

    @Autowired
    public CatApiController(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @GetMapping("/api/cats")
    public ResponseEntity<List<Cat>> listCats() {
        List<Cat> cats = new ArrayList<>();

        try (Connection connection = this.dataSource.getConnection()) {
            String sql = "SELECT * FROM cat";
            try (PreparedStatement statement = connection.prepareStatement(sql);
                 ResultSet resultSet = statement.executeQuery()) {

                while (resultSet.next()) {
                    Cat cat = new Cat();
                    cat.setCatid(resultSet.getInt("catid"));
                    cat.setCustid(resultSet.getInt("custid"));
                    cat.setCatname(resultSet.getString("catname"));
                    cat.setCatbreed(resultSet.getString("catbreed"));
                    cat.setCatage(resultSet.getInt("catage"));
                    cats.add(cat);
                }
            }
            return ResponseEntity.ok(cats);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error retrieving cat list: " + e.getMessage(), e);
            return ResponseEntity.status(500).body(null);
        }
    }
}