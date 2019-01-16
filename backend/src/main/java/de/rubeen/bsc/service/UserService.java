package de.rubeen.bsc.service;

import com.google.common.base.Preconditions;
import de.rubeen.bsc.entities.web.AppUserEntity;
import de.rubeen.bsc.entities.web.LoginHoursEntity;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.NameTokenizers;
import org.modelmapper.jooq.RecordValueReader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.sql.Time;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.*;
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
        return dslContext.select(WORKINGHOURS.ID, WORKINGHOURS.STARTTIME, WORKINGHOURS.ENDTIME,
                WORKINGHOURS.MONDAY, WORKINGHOURS.TUESDAY, WORKINGHOURS.WEDNESDAY, WORKINGHOURS.THURSDAY,
                WORKINGHOURS.FRIDAY, WORKINGHOURS.SATURDAY, WORKINGHOURS.SUNDAY)
                .from(APPUSER)
                .innerJoin(WORKINGHOURS).onKey()
                .where(APPUSER.ID.eq(loginService.getUserID(userMail)))
                .fetch().parallelStream()
                .map(record -> modelMapper.map(record, LoginHoursEntity.class))
                .collect(Collectors.toCollection(LinkedList::new));
    }

    public void updateAndCreateWorkingHours(Collection<LoginHoursEntity> workingHours, String userMail) {
        workingHours.parallelStream()
                .filter(loginHoursEntity -> Objects.nonNull(loginHoursEntity.getId()))
                .forEach(loginHoursEntity -> updateWorkingHour(loginHoursEntity, userMail));
        workingHours.parallelStream()
                .filter(loginHoursEntity -> Objects.isNull(loginHoursEntity.getId()))
                .forEach(loginHoursEntity -> createWorkingHour(loginHoursEntity, userMail));
    }

    private void createWorkingHour(LoginHoursEntity loginHoursEntity, String userMail) {
        dslContext.insertInto(WORKINGHOURS)
                .columns(WORKINGHOURS.USER_FK, WORKINGHOURS.STARTTIME, WORKINGHOURS.ENDTIME,
                        WORKINGHOURS.MONDAY, WORKINGHOURS.TUESDAY, WORKINGHOURS.WEDNESDAY, WORKINGHOURS.THURSDAY,
                        WORKINGHOURS.FRIDAY, WORKINGHOURS.SATURDAY, WORKINGHOURS.SUNDAY)
                .values(loginService.getUserID(userMail), Time.valueOf(loginHoursEntity.getStartTime()),
                        Time.valueOf(loginHoursEntity.getEndTime()), loginHoursEntity.isMonday(),
                        loginHoursEntity.isTuesday(), loginHoursEntity.isWednesday(),
                        loginHoursEntity.isThursday(), loginHoursEntity.isFriday(),
                        loginHoursEntity.isSaturday(), loginHoursEntity.isSunday()).executeAsync();
    }

    private void updateWorkingHour(LoginHoursEntity loginHoursEntity, String userMail) {
        dslContext.update(WORKINGHOURS)
                .set(WORKINGHOURS.STARTTIME, Time.valueOf(loginHoursEntity.getStartTime()))
                .set(WORKINGHOURS.ENDTIME, Time.valueOf(loginHoursEntity.getEndTime()))
                .set(WORKINGHOURS.MONDAY, loginHoursEntity.isMonday())
                .set(WORKINGHOURS.TUESDAY, loginHoursEntity.isTuesday())
                .set(WORKINGHOURS.WEDNESDAY, loginHoursEntity.isWednesday())
                .set(WORKINGHOURS.THURSDAY, loginHoursEntity.isThursday())
                .set(WORKINGHOURS.FRIDAY, loginHoursEntity.isFriday())
                .set(WORKINGHOURS.SATURDAY, loginHoursEntity.isSaturday())
                .set(WORKINGHOURS.SUNDAY, loginHoursEntity.isSunday())
                .where(WORKINGHOURS.USER_FK.eq(loginService.getUserID(userMail)))
                .and(WORKINGHOURS.ID.eq(loginHoursEntity.getId()))
                .executeAsync();
    }

    public List<AppUserEntity> getAllAppUsers(String userMail) {
        checkNotNull(userMail);
        return dslContext.select()
                .from(APPUSER)
                .where(APPUSER.ID.notEqual(loginService.getUserID(userMail)))
                .fetch().parallelStream()
                .map(record -> modelMapper.map(record, AppUserEntity.class))
                .collect(Collectors.toList());
    }

    public AppUserEntity getAppUser(String userMail) {
        checkNotNull(userMail);
        return dslContext.select()
                .from(APPUSER)
                .where(APPUSER.ID.eq(loginService.getUserID(userMail)))
                .fetchOne()
                .map(record -> modelMapper.map(record, AppUserEntity.class));
    }

    public AppUserEntity getAppUser(Integer userId) {
        checkNotNull(userId);
        return dslContext.select()
                .from(APPUSER)
                .where(APPUSER.ID.eq(userId))
                .fetchOne()
                .map(record -> modelMapper.map(record, AppUserEntity.class));
    }
}
