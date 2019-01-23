package de.rubeen.bsc.service;

import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Service;

import java.sql.SQLException;

@Service
public class DatabaseService extends LoggableService {
    private final DSLContext dslContext;

    public DatabaseService(IPersistence iPersistence) throws SQLException {
        this.dslContext = DSL.using(iPersistence.getDatabaseConnection(), SQLDialect.POSTGRES);
    }


    public DSLContext getContext() {
        return dslContext;
    }
}
