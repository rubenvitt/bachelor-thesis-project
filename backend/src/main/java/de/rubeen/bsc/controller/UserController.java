package de.rubeen.bsc.controller;

import de.rubeen.bsc.entities.db.tables.Appuser;
import de.rubeen.bsc.entities.db.tables.records.AppuserRecord;
import de.rubeen.bsc.entities.web.AppUserEntity;
import de.rubeen.bsc.entities.web.LoginHoursEntity;
import de.rubeen.bsc.entities.web.NewAppUserEntity;
import de.rubeen.bsc.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

@RestController
@RequestMapping(path = "/user")
public class UserController {

    private final UserService userService;
    private Logger LOG = LoggerFactory.getLogger(this.getClass());

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @RequestMapping(path = "/workingHours", method = RequestMethod.GET)
    public List<LoginHoursEntity> getLoginHours(HttpServletResponse response,
                                                @RequestParam(value = "user_id") String userId) {
        return userService.getWorkingHours(userId);
    }

    @RequestMapping(path = "/register", method = RequestMethod.POST)
    public void registerUser(HttpServletResponse response,
                             @RequestBody NewAppUserEntity newAppUserEntity) {
        userService.addUser(newAppUserEntity);
        response.setStatus(HttpServletResponse.SC_CREATED);
    }

    @RequestMapping(path = "/workingHours", method = RequestMethod.POST)
    public void setLoginHours(HttpServletResponse response,
                              @RequestParam(value = "user_id") String userId,
                              @RequestBody List<LoginHoursEntity> workingHours) {
        LOG.info("got hours: {}", workingHours);
        userService.updateAndCreateWorkingHours(workingHours, userId);
    }

    @RequestMapping(path = "/list", method = RequestMethod.GET)
    public List<AppUserEntity> getAppUserList(@RequestParam(value = "user_id") String userId,
                                              @RequestParam(value = "filter", required = false) String filter) {
        return userService.getAllAppUsers(userId, filter);
    }

    @RequestMapping(method = RequestMethod.GET)
    public AppUserEntity getAppUser(@RequestParam(value = "user_id") String userId) {
        return userService.getAppUser(userId);
    }
}
