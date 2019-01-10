package de.rubeen.bsc.service;

import de.rubeen.bsc.entities.web.LoginHoursEntity;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.NameTokenizers;
import org.modelmapper.jooq.RecordValueReader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import static de.rubeen.bsc.entities.db.tables.Appuser.APPUSER;
import static de.rubeen.bsc.entities.db.tables.Workinghours.WORKINGHOURS;

@Service
public class UserService extends AbstractDatabaseService {

    private final ModelMapper modelMapper = new ModelMapper();
    private final LoginService loginService;

    public UserService(@Value("${database.url}") final String url,
                       @Value("${database.user}") final String user,
                       @Value("${database.pass}") final String password,
                       LoginService loginService) throws SQLException {
        super(url, user, password);
        this.loginService = loginService;
        modelMapper.getConfiguration().addValueReader(new RecordValueReader());
        modelMapper.getConfiguration().setSourceNameTokenizer(NameTokenizers.UNDERSCORE);
    }

    public List<LoginHoursEntity> getWorkingHours(String userMail) {
        LOG.info("Looking for working hours for user: {}", userMail);
        return dslContext.select()
                .from(APPUSER)
                .innerJoin(WORKINGHOURS).onKey()
                .where(APPUSER.ID.eq(loginService.getUserID(userMail)))
                .fetch().parallelStream()
                .map(record -> modelMapper.map(record, LoginHoursEntity.class))
                .collect(Collectors.toCollection(LinkedList::new));
    }
}
