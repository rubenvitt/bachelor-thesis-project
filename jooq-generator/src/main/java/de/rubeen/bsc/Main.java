package de.rubeen.bsc;

import de.rubeen.bsc.entities.db.Tables;
import de.rubeen.bsc.entities.db.tables.Appuser;
import de.rubeen.bsc.entities.db.tables.Credential;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.Table;
import org.jooq.impl.DSL;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Main {
    public static void main(String[] args) {
        String username = "testuser";
        String password = "testpass";
        String url = "jdbc:postgresql://localhost:5432/testdatabase";

        try(Connection connection = DriverManager.getConnection(url, username, password)) {
            DSLContext create = DSL.using(connection, SQLDialect.POSTGRES);
            create.select().from(Tables.APPUSER).fetch().forEach(record -> {
                System.out.println("ID: " +record.getValue(Appuser.APPUSER.ID) + "; MAIL: " + record.getValue(Appuser.APPUSER.NAME));
            });
            System.out.println("now a join...");
            create.select().from(Tables.APPUSER).innerJoin(Tables.CREDENTIAL).onKey().fetch().forEach(record -> {
                System.out.println(record.getValue(Appuser.APPUSER.NAME) + " has credential: " + record.getValue(Credential.CREDENTIAL.CREDENTIAL_));
            });
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
