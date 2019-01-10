package de.rubeen.bsc.controller;

import de.rubeen.bsc.entities.web.LoginHoursEntity;
import de.rubeen.bsc.service.UserService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

@RestController
@RequestMapping(path = "/user")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @RequestMapping(path = "/workingHours", method = RequestMethod.GET)
    public List<LoginHoursEntity> getLoginHours(HttpServletResponse response,
                                                @RequestParam(value = "user_id") String userId) {
        return userService.getWorkingHours(userId);
    }
}
