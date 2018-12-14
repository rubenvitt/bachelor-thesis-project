package de.rubeen.bsc.service;

import de.rubeen.bsc.entities.db.Tables;
import de.rubeen.bsc.entities.db.tables.Appuser;
import de.rubeen.bsc.entities.web.LoginUser;
import org.jooq.Record;
import org.jooq.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.sql.SQLException;

@Service
public class LoginService extends AbstractDatabaseService {
    Logger LOG = LoggerFactory.getLogger(this.getClass());

    public LoginService(@Value("${database.url}") final String url,
                        @Value("${database.user}") final String user,
                        @Value("${database.pass}") final String password) throws SQLException {
        super(url, user, password);
    }

    public Boolean login(final String user, final String password) {
        Result<Record> result = dslContext
                .select()
                .from(Tables.APPUSER)
                .where(Appuser.APPUSER.MAIL.eq(user)
                        .and(Appuser.APPUSER.PASSWORD.eq(password)))
                .limit(1)
                .fetch();
        return result.size() == 1;
    }

    public Boolean login(LoginUser loginUser) {
        Result<Record> result = dslContext
                .select()
                .from(Tables.APPUSER)
                .where(Appuser.APPUSER.MAIL.eq(loginUser.getEmail())
                        .and(Appuser.APPUSER.PASSWORD.eq(loginUser.getPassword())))
                .limit(1)
                .fetch();
        return result.size() == 1;
    }

}
