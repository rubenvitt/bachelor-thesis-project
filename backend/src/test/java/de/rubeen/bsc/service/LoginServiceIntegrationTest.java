package de.rubeen.bsc.service;

import de.rubeen.bsc.entities.db.Tables;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static de.rubeen.bsc.entities.db.tables.Appuser.APPUSER;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@RunWith(SpringRunner.class)
public class LoginServiceIntegrationTest {
    private final int testID = 999999999;
    private final String testMail = "test@test-mail.test.de",
            testName = "Test-User",
            testPassword = "abcdef";
    Logger LOG = LoggerFactory.getLogger(this.getClass());
    @Autowired
    LoginService loginService;

    @Before
    public void before() {
        loginService.dslContext.insertInto(Tables.APPUSER)
                .columns(APPUSER.ID, APPUSER.MAIL, APPUSER.NAME, APPUSER.PASSWORD)
                .values(testID, testMail, testName, testPassword)
                .execute();
    }

    @After
    public void after() {
        loginService.dslContext.deleteFrom(Tables.APPUSER)
                .where(APPUSER.ID.eq(testID))
                .execute();
    }

    @Test
    public void login() {
        assertThat(loginService.login(testMail, testPassword))
                .isTrue();
        assertThat(loginService.login(testMail + "xx", testPassword))
                .isFalse();
        assertThat(loginService.login(testMail, testPassword + "xx"))
                .isFalse();
    }
}