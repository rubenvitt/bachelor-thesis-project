package de.rubeen.bsc.service;

import de.rubeen.bsc.entities.db.tables.records.AppuserRecord;
import de.rubeen.bsc.entities.web.AppUserEntity;
import de.rubeen.bsc.entities.web.LoginHoursEntity;
import de.rubeen.bsc.entities.web.NewAppUserEntity;
import org.jooq.Record;
import org.jooq.SelectConditionStep;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.NameTokenizers;
import org.modelmapper.jooq.RecordValueReader;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.sql.Time;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkNotNull;
import static de.rubeen.bsc.entities.db.tables.Appuser.APPUSER;
import static de.rubeen.bsc.entities.db.tables.Workinghours.WORKINGHOURS;

@Service
public class UserService extends LoggableService {
    private final ModelMapper modelMapper = new ModelMapper();
    private final LoginService loginService;
    private final DatabaseService databaseService;

    public UserService(LoginService loginService, DatabaseService databaseService) throws SQLException {
        this.loginService = loginService;
        this.databaseService = databaseService;
        modelMapper.getConfiguration().addValueReader(new RecordValueReader());
        modelMapper.getConfiguration().setSourceNameTokenizer(NameTokenizers.UNDERSCORE);
    }

    public List<LoginHoursEntity> getWorkingHours(String userMail) {
        LOG.info("Looking for working hours for user: {}", userMail);
        return databaseService.getContext()
                .select(WORKINGHOURS.ID, WORKINGHOURS.STARTTIME, WORKINGHOURS.ENDTIME,
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
        databaseService.getContext()
                .insertInto(WORKINGHOURS)
                .columns(WORKINGHOURS.USER_FK, WORKINGHOURS.STARTTIME, WORKINGHOURS.ENDTIME,
                        WORKINGHOURS.MONDAY, WORKINGHOURS.TUESDAY, WORKINGHOURS.WEDNESDAY, WORKINGHOURS.THURSDAY,
                        WORKINGHOURS.FRIDAY, WORKINGHOURS.SATURDAY, WORKINGHOURS.SUNDAY)
                .values(loginService.getUserID(userMail), Time.valueOf(loginHoursEntity.getStartTime()),
                        Time.valueOf(loginHoursEntity.getEndTime()), loginHoursEntity.getMonday(),
                        loginHoursEntity.getTuesday(), loginHoursEntity.getWednesday(),
                        loginHoursEntity.getThursday(), loginHoursEntity.getFriday(),
                        loginHoursEntity.getSaturday(), loginHoursEntity.getSunday()).executeAsync();
    }

    private void updateWorkingHour(LoginHoursEntity loginHoursEntity, String userMail) {
        databaseService.getContext()
                .update(WORKINGHOURS)
                .set(WORKINGHOURS.STARTTIME, Time.valueOf(loginHoursEntity.getStartTime()))
                .set(WORKINGHOURS.ENDTIME, Time.valueOf(loginHoursEntity.getEndTime()))
                .set(WORKINGHOURS.MONDAY, loginHoursEntity.getMonday())
                .set(WORKINGHOURS.TUESDAY, loginHoursEntity.getTuesday())
                .set(WORKINGHOURS.WEDNESDAY, loginHoursEntity.getWednesday())
                .set(WORKINGHOURS.THURSDAY, loginHoursEntity.getThursday())
                .set(WORKINGHOURS.FRIDAY, loginHoursEntity.getFriday())
                .set(WORKINGHOURS.SATURDAY, loginHoursEntity.getSaturday())
                .set(WORKINGHOURS.SUNDAY, loginHoursEntity.getSunday())
                .where(WORKINGHOURS.USER_FK.eq(loginService.getUserID(userMail)))
                .and(WORKINGHOURS.ID.eq(loginHoursEntity.getId()))
                .executeAsync();
    }

    public List<AppUserEntity> getAllAppUsers(String userMail, String filter) {
        checkNotNull(userMail);
        SelectConditionStep<Record> select = databaseService.getContext()
                .select()
                .from(APPUSER)
                .where(APPUSER.ID.notEqual(loginService.getUserID(userMail)));
        if (filter != null && !filter.isBlank()) {
            filter = filter.toUpperCase();
            select = select
                    .and(APPUSER.NAME.upper().contains(filter)
                            .or(APPUSER.MAIL.upper().contains(filter))
                            .or(APPUSER.POSITION.upper().contains(filter))
                    );
        }
        return select
                .orderBy(APPUSER.NAME, APPUSER.POSITION, APPUSER.MAIL, APPUSER.ID)
                .fetch().parallelStream()
                .map(record -> modelMapper.map(record, AppUserEntity.class))
                .collect(Collectors.toList());
    }

    public AppUserEntity getAppUser(String userMail) {
        checkNotNull(userMail);
        return databaseService.getContext()
                .select()
                .from(APPUSER)
                .where(APPUSER.ID.eq(loginService.getUserID(userMail)))
                .fetchOne()
                .map(record -> modelMapper.map(record, AppUserEntity.class));
    }

    public AppUserEntity getAppUser(Integer userId) {
        checkNotNull(userId);
        return databaseService.getContext()
                .select()
                .from(APPUSER)
                .where(APPUSER.ID.eq(userId))
                .fetchOne()
                .map(record -> modelMapper.map(record, AppUserEntity.class));
    }
}
