package io.shardingjdbc.console.controller;

import io.shardingjdbc.console.constant.ResponseCode;
import io.shardingjdbc.console.entity.DBConnector;
import io.shardingjdbc.console.entity.GlobalSessions;
import io.shardingjdbc.console.entity.ResponseObject;
import io.shardingjdbc.console.entity.UserSession;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.sql.Connection;
import java.util.Map;

@RestController
@RequestMapping("/user")
public class UserController {

    /**
     * handle https for user login.
     * 
     * @param userSession user info
     * @param userUUID uuid
     * @param response response
     * @return ResponseObject
     */
    @RequestMapping(path = "/login", method = RequestMethod.POST)
    public ResponseObject login(final UserSession userSession, final @CookieValue(value = "userUUID", required = false, defaultValue = "") String userUUID, final HttpServletResponse response) {
        if (!userUUID.equals("")) {
            return new ResponseObject(ResponseCode.SUCCESS);
        }
        Connection connection = DBConnector.getConnection(userSession.getUserName(), userSession.getPassWord(), userSession.getTargetURL(), userSession.getDriver());
        if (null == connection) {
            return new ResponseObject(ResponseCode.FAILURE);
        }
        Map<String, Connection> connectionMap = GlobalSessions.getSessionInfo();
        connectionMap.put(userSession.getUuid(), connection);
        Cookie cookie = new Cookie("userUUID", userSession.getUuid());
        cookie.setMaxAge(120 * 60);
        cookie.setPath("/");
        response.addCookie(cookie);
        return new ResponseObject(ResponseCode.SUCCESS);
    }

    /**
     * handle http for user's exiting.
     * @param userUUID
     * @param response
     * @return ResponseObject
     */
    @RequestMapping(value = "/exit", method = RequestMethod.POST)
    public ResponseObject exit(final @CookieValue(value = "userUUID", required = false, defaultValue = "") String userUUID, HttpServletResponse response) {
        if (!userUUID.equals("")) {
            Map<String, Connection> connectionMap = GlobalSessions.getSessionInfo();
            connectionMap.remove(userUUID);
            Cookie cookie = new Cookie("userUUID",null);
            cookie.setMaxAge(0);
            cookie.setPath("/");
            response.addCookie(cookie);
        }
        return new ResponseObject(ResponseCode.SUCCESS);
    }
}