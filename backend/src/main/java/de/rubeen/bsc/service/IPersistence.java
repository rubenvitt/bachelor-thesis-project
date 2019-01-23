package de.rubeen.bsc.service;

import java.sql.Connection;
import java.sql.SQLException;

public interface IPersistence {
    Connection getDatabaseConnection() throws SQLException;
}
