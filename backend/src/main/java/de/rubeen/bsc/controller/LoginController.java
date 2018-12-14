package de.rubeen.bsc.controller;

import de.rubeen.bsc.entities.web.LoginUser;
import de.rubeen.bsc.service.LoginService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


@RestController
public class LoginController {
    private final LoginService loginService;

    @Autowired
    public LoginController(LoginService loginService) {
        this.loginService = loginService;
    }

    @RequestMapping(path = "/login", method = RequestMethod.POST)
    public void login(@RequestBody LoginUser loginUser, HttpServletResponse response, HttpServletRequest request) {
        Boolean login = loginService.login(loginUser);
        response.setStatus(login ? HttpServletResponse.SC_OK : HttpServletResponse.SC_UNAUTHORIZED);
    }
}
