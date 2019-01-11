package de.rubeen.bsc.controller;

import de.rubeen.bsc.entities.web.LoginHoursEntity;
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
        LOG.info("LALALALA: {}", userService.getWorkingHours(userId));
        return userService.getWorkingHours(userId);
    }

    @RequestMapping(path = "/workingHours", method = RequestMethod.POST)
    public void setLoginHours(HttpServletResponse response,
                              @RequestParam(value = "user_id") String userId,
                              @RequestBody List<LoginHoursEntity> workingHours) {
        LOG.info("got hours: {}", workingHours);
        userService.updateAndCreateWorkingHours(workingHours, userId);
    }
}
