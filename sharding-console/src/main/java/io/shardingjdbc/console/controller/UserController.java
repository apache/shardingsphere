package io.shardingjdbc.console.controller;


import com.sun.deploy.net.HttpResponse;
import io.shardingjdbc.console.entity.DBConnector;
import io.shardingjdbc.console.entity.GlobalSessions;
import io.shardingjdbc.console.entity.RespObj;
import io.shardingjdbc.console.entity.UserSession;
import org.springframework.web.bind.annotation.*;
import io.shardingjdbc.console.constant.RespCode;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.sql.Connection;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/user")
public class UserController {

    @RequestMapping(path="/login", method=RequestMethod.POST)
    public RespObj login(UserSession userInfo, @CookieValue(value="userUUID", required = false,
            defaultValue = "") String userUUID, HttpServletResponse response) {

        System.out.println("-------------------");
        System.out.println(userInfo.toString());
        System.out.println(userUUID);
        System.out.println(userInfo.getPassWord());
        System.out.println("-------------------");

        if(userUUID.equals("")){
            Connection conn = DBConnector.getConnection(userInfo.getUserName(),userInfo.getPassWord(),userInfo.getTargetURL(),userInfo.getDriver());
            if(null == conn){
                return new RespObj(RespCode.ERR_USER);
            }else{
                System.out.println("---------ok----------");
                System.out.println(userInfo.getUuid());
                System.out.println(conn);
                Map<String, Connection> globalSess = GlobalSessions.getSessionInfos();

                globalSess.put(userInfo.getUuid(), conn);
                Cookie cookie = new Cookie("userUUID",userInfo.getUuid());
                cookie.setMaxAge(120 * 60);
                cookie.setPath("/");
                response.addCookie(cookie);
                System.out.println(conn);
                return new RespObj(RespCode.SUCCESS);
            }
        }else{
            System.out.println("---------else----------");
            System.out.println(userUUID);
            System.out.println(GlobalSessions.getSessionInfos().get(userUUID));

            return new RespObj(RespCode.SUCCESS);

        }


    }

    @RequestMapping(value="/exit", method= RequestMethod.POST)
    public RespObj exit(@CookieValue(value="userUUID", required = false,
            defaultValue = "") String userUUID, HttpServletResponse response) {
        System.out.println("-------------------");
        System.out.println(userUUID);
        System.out.println("-------------------");


        if(! userUUID.equals("")){
            System.out.println("user uuid exists");
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
