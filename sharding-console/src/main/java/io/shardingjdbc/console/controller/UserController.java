package io.shardingjdbc.console.controller;

import io.shardingjdbc.console.domain.DBConnector;
import io.shardingjdbc.console.domain.SessionRegistry;
import io.shardingjdbc.console.domain.Response;
import io.shardingjdbc.console.domain.UserSession;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.CookieValue;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * User controller.
 * 
 * @author panjuan
 */
@RestController
@RequestMapping("/user")
public class UserController {
    
    /**
     * Handle https for user login.
     * 
     * @param userSession user info
     * @param userUUID user uuid
     * @param response response
     * @return response object
     */
    @RequestMapping(path = "/login", method = RequestMethod.POST)
    public Response login(final @RequestBody UserSession userSession, final @CookieValue(value = "userUUID", required = false, defaultValue = "") String userUUID,
                          final HttpServletResponse response) {
        if (!"".equals(userUUID)) {
            return new Response(200, "OK");
        }
        Connection connection;
        try {
            connection = DBConnector.getConnection(userSession.getUserName(), userSession.getPassWord(), userSession.getTargetURL(), userSession.getDriver());
        } catch (final ClassNotFoundException | SQLException ex) {
            return new Response(403, "Authorization failed");
        }
        setSession(userSession.getId(), response, connection);
        
        return new Response(200, "Authorization succeeded");
    }
    
    private void setSession(final String uuid, final HttpServletResponse response, final Connection connection) {
        SessionRegistry.getInstance().addSession(uuid, connection);
        Cookie cookie = new Cookie("userUUID", uuid);
        cookie.setMaxAge(120 * 60);
        cookie.setPath("/");
        response.addCookie(cookie);
    }

    /**
     * Handle http for user exit.
     * 
     * @param userUUID user uuid
     * @param response response
     * @return response object
     */
    @RequestMapping(value = "/exit", method = RequestMethod.POST)
    public Response exit(final @CookieValue(value = "userUUID", required = false, defaultValue = "") String userUUID, final HttpServletResponse response) {
        if (!"".equals(userUUID)) {
            removeSession(userUUID, response);
        }
        return new Response(200, "OK");
    }

    private void removeSession(final String userUUID, final HttpServletResponse response) {
        SessionRegistry.getInstance().removeSession(userUUID);
        
        Cookie cookie = new Cookie("userUUID", null);
        cookie.setMaxAge(0);
        cookie.setPath("/");
        response.addCookie(cookie);
    }
}
