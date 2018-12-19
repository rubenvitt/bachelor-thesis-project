package de.rubeen.bsc.service;

import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public abstract class AbstractDatabaseService {
    final DSLContext dslContext;
    Logger LOG = LoggerFactory.getLogger(this.getClass());

    public AbstractDatabaseService(final String url, final String user, final String password) throws SQLException {
        Connection databaseConnection = DriverManager.getConnection(url, user, password);
        dslContext = DSL.using(databaseConnection, SQLDialect.POSTGRES);
    }
}
