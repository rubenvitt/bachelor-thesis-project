package de.rubeen.bsc.office365tests.auth.microsoft;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.UUID;

@RestController
public class AuthorizationController {

    @RequestMapping(value = "/auth", method = RequestMethod.POST)
    public String authorize(
            @RequestParam("code") String code,
            @RequestParam("id_token") String idToken,
            @RequestParam("state") UUID state,
            HttpServletRequest request,
            HttpServletResponse response) throws IOException {
        // Get the expected state value from the session
        HttpSession session = request.getSession();
        UUID expectedState = (UUID) session.getAttribute("expected_state");
        UUID expectedNonce = (UUID) session.getAttribute("expected_nonce");

        // Make sure that the state query parameter returned matches
        // the expected state
        if (state.equals(expectedState)) {
            IdToken idTokenObj = IdToken.parseEncodedToken(idToken, expectedNonce.toString());
            if (idTokenObj != null) {
                TokenResponse tokenResponse = AuthHelper.getTokenFromAuthCode(code, idTokenObj.getTenantId());
                session.setAttribute("tokens", tokenResponse);
                session.setAttribute("userConnected", true);
                session.setAttribute("userName", idTokenObj.getName());
                session.setAttribute("userTenantId", idTokenObj.getTenantId());
                System.out.println("TOKEN::::");
                System.out.println(tokenResponse.getAccessToken());
                System.out.println("Expires: " + tokenResponse.getExpirationTime());
                Cookie cookie = new Cookie("microsoft-access-key", tokenResponse.getAccessToken());
                response.addCookie(cookie);
                response.addCookie(new Cookie("google-access-key", "this-will–be-my-token"));
                //response.setHeader("test", "abc");
                response.sendRedirect("http://localhost:3333/settings");
            } else {
                session.setAttribute("error", "ID token failed validation.");
            }
        } else {
            session.setAttribute("error", "Unexpected state returned from authority.");
        }
        System.out.println(session.getAttribute("userName"));
        System.out.println("code: " + code + " - idToken: " + idToken + " - state: " + state);
        System.out.println("SCOPE: " + ((TokenResponse) (session.getAttribute("tokens"))).getScope());
        return "<h1 style=\"align=center\">Hello " + session.getAttribute("userName") + "</h1>";
    }

    @RequestMapping("/logout")
    public String logout(HttpServletRequest request) {
        HttpSession session = request.getSession();
        session.invalidate();
        return "redirect:/";
    }

    @RequestMapping("/login")
    public String index(HttpServletRequest request, HttpServletResponse response) throws IOException {
        UUID state = UUID.randomUUID();
        UUID nonce = UUID.randomUUID();

        // Save the state and nonce in the session so we can
        // verify after the auth process redirects back
        HttpSession session = request.getSession();
        session.setAttribute("expected_state", state);
        session.setAttribute("expected_nonce", nonce);

        response.sendRedirect(AuthHelper.getLoginUrl(state, nonce));
        return AuthHelper.getLoginUrl(state, nonce);
        // Name of a definition in WEB-INF/defs/pages.xml
    }
}
