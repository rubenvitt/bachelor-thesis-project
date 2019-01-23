package de.rubeen.bsc.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

@Component
public class DefaultPersistence implements IPersistence {

    private final String url, user, password;

    public DefaultPersistence(@Value("${database.url}") final String url,
                              @Value("${database.user}") final String user,
                              @Value("${database.pass}") final String password) {
        this.url = url;
        this.user = user;
        this.password = password;
    }

    @Override
    public Connection getDatabaseConnection() throws SQLException {
        return DriverManager.getConnection(url, user, password);
    }
}
