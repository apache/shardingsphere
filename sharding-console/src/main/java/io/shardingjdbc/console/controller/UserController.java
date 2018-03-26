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
     * to handle https for user's login.
     * @param userInfo
     * @param userUUID
     * @param response
     * @return ResponseObject
     */
    @RequestMapping(path = "/login", method = RequestMethod.POST)
    public ResponseObject login(final UserSession userInfo, final @CookieValue(value = "userUUID", required = false,
            defaultValue = "") String userUUID, HttpServletResponse response) {
        if (userUUID.equals("")) {
            Connection connection = DBConnector.getConnection(userInfo.getUserName(), userInfo.getPassWord(), userInfo.getTargetURL(), userInfo.getDriver());
            if (null == connection) {
                return new ResponseObject(ResponseCode.ERR_USER);
            } else {
                Map<String, Connection> connectionMap = GlobalSessions.getSessionInfo();
                connectionMap.put(userInfo.getUuid(), connection);
                Cookie cookie = new Cookie("userUUID", userInfo.getUuid());
                cookie.setMaxAge(120 * 60);
                cookie.setPath("/");
                response.addCookie(cookie);
                return new ResponseObject(ResponseCode.SUCCESS);
            }
        } else {
            return new ResponseObject(ResponseCode.SUCCESS);
        }
    }

    /**
     * to handle http for user's exiting.
     * @param userUUID
     * @param response
     * @return ResponseObject
     */
    @RequestMapping(value = "/exit", method = RequestMethod.POST)
    public ResponseObject exit(final @CookieValue(value = "userUUID", required = false,
            defaultValue = "") String userUUID, HttpServletResponse response) {
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