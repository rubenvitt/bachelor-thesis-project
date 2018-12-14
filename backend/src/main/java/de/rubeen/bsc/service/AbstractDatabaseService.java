package de.rubeen.bsc.service;

import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public abstract class AbstractDatabaseService {
    final DSLContext dslContext;

    public AbstractDatabaseService(final String url, final String user, final String password) throws SQLException {
        Connection databaseConnection = DriverManager.getConnection(url, user, password);
        dslContext = DSL.using(databaseConnection, SQLDialect.POSTGRES);
    }
}
