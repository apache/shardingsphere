package io.shardingjdbc.console.controller;

import io.shardingjdbc.console.entity.DBConnector;
import io.shardingjdbc.console.entity.GlobalSessions;
import io.shardingjdbc.console.entity.RespObj;
import io.shardingjdbc.console.entity.UserSession;
import org.springframework.web.bind.annotation.*;
import io.shardingjdbc.console.constant.RespCode;
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
     * @return RespObj
     */
    @RequestMapping(path = "/login", method = RequestMethod.POST)
    public RespObj login(final UserSession userInfo, final @CookieValue(value = "userUUID", required = false,
            defaultValue = "") String userUUID, HttpServletResponse response) {
        if (userUUID.equals("")) {
            Connection conn = DBConnector.getConnection(userInfo.getUserName(), userInfo.getPassWord(), userInfo.getTargetURL(), userInfo.getDriver());
            if (null == conn) {
                return new RespObj(RespCode.ERR_USER);
            } else {
                Map<String, Connection> globalSess = GlobalSessions.getSessionInfos();
                globalSess.put(userInfo.getUuid(), conn);
                Cookie cookie = new Cookie("userUUID", userInfo.getUuid());
                cookie.setMaxAge(120 * 60);
                cookie.setPath("/");
                response.addCookie(cookie);
                return new RespObj(RespCode.SUCCESS);
            }
        } else {

            return new RespObj(RespCode.SUCCESS);
        }
    }

    /**
     * to handle http for user's exiting.
     * @param userUUID
     * @param response
     * @return RespObj
     */
    @RequestMapping(value = "/exit", method = RequestMethod.POST)
    public RespObj exit(final @CookieValue(value = "userUUID", required = false,
            defaultValue = "") String userUUID, HttpServletResponse response) {

        if (!userUUID.equals("")) {
            Map<String, Connection> globalSess = GlobalSessions.getSessionInfos();
            globalSess.remove(userUUID);

            Cookie cookie = new Cookie("userUUID",null);
            cookie.setMaxAge(0);
            cookie.setPath("/");
            response.addCookie(cookie);

        }
        return new RespObj(RespCode.SUCCESS);
    }
}