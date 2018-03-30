package io.shardingjdbc.console.controller;

import com.google.common.base.Optional;
import io.shardingjdbc.console.domain.*;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.CookieValue;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * User controller.
 * 
 * @author panjuan
 */
@RestController
@RequestMapping("/user")
public class SessionController {
    
    /**
     * Handle https for user login.
     * 
     * @param session user info
     * @param userUUID user uuid
     * @param response response
     * @return response object
     */
    @RequestMapping(value = "/login", method = RequestMethod.POST)
    public WorkbenchResponse login(@RequestBody final Session session, final @CookieValue(value = "userUUID", required = false, defaultValue = "") String userUUID,
                                   final HttpServletResponse response) throws UserException {
        if (!"".equals(userUUID)) {
            if (SessionRegistry.getInstance().findSession(userUUID).isPresent()) {
                return new WorkbenchResponse("Logged in.");
            }
            removeSession(userUUID, response);
            throw new UserException("Please login first.");
        }
        Window window = createWindow(session);
        setSession(session, window, response);
        Map<String, String> result = new HashMap<>(1, 1);
        result.put("windowID", window.getId());
        return new WorkbenchResponse("Login succeeded", result);
    }
    
    private Window createWindow(final Session session) throws UserException {
        try {
            Connection connection = DBConnector.getConnection(session.getUserName(), session.getPassWord(), session.getTargetURL(), session.getDriver());
            return new Window(connection);
        } catch (final ClassNotFoundException | SQLException ex) {
            throw new UserException("Login failed.");
        }
    }

    private void setSession(final Session session, final Window window,
                            final HttpServletResponse response) {
        session.addWindowID(window.getId());
        SessionRegistry.getInstance().addSession(session.getId(), session);
        WindowRegistry.getInstance().addSession(window.getId(), window.getConnection());
        Cookie cookie = new Cookie("userUUID", session.getId());
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
    public WorkbenchResponse exit(final @CookieValue(value = "userUUID", required = false, defaultValue = "") String userUUID, final HttpServletResponse response) {
        if (!"".equals(userUUID)) {
            removeSession(userUUID, response);
        }
        return new WorkbenchResponse("Logout succeeded");
    }

    private void removeSession(final String userUUID, final HttpServletResponse response) {
        Optional<Session> userSessionOptional = SessionRegistry.getInstance().findSession(userUUID);
        if (userSessionOptional.isPresent()) {
            List<String> windowIDList = userSessionOptional.get().getWindowIDList();
            for (String windowID : windowIDList) {
                WindowRegistry.getInstance().removeSession(windowID);
            }
        }
        SessionRegistry.getInstance().removeSession(userUUID);
        Cookie cookie = new Cookie("userUUID", null);
        cookie.setMaxAge(0);
        cookie.setPath("/");
        response.addCookie(cookie);
    }
    
    @RequestMapping(value = "/addWindow", method = RequestMethod.POST)
    public WorkbenchResponse addWindow(final @CookieValue(value = "userUUID",
        required = false, defaultValue = "") String userUUID) throws UserException {
        Optional<Session> userSessionOptional = SessionRegistry.getInstance().findSession(userUUID);
        if (!userSessionOptional.isPresent()) {
            throw new UserException("Please login first.");
        }
        
        Session session = userSessionOptional.get();
        Window window = createWindow(session);
        setWindow(session, window);
        
        Map<String, String> result = new HashMap<>();
        result.put("windowID", window.getId());
        return new WorkbenchResponse("Open new window OK.", result);
    }
    
    private void setWindow(final Session session, final Window window) {
        session.addWindowID(window.getId());
        WindowRegistry.getInstance().addSession(window.getId(), window.getConnection());
    }
    
    @RequestMapping(value = "/delWindow", method = RequestMethod.POST)
    public WorkbenchResponse delWindow(@RequestBody final Map<String, String> windowInfo, final @CookieValue(value = "userUUID",
        required = false, defaultValue = "") String userUUID) throws UserException {
        Optional<Session> userSessionOptional = SessionRegistry.getInstance().findSession(userUUID);
        if (!userSessionOptional.isPresent()) {
            throw new UserException("Please login first.");
        }
        
        Session session = userSessionOptional.get();
        removeWindow(session, windowInfo);
        return new WorkbenchResponse("Close window OK.");
    }
    
    private void removeWindow(final Session session, final Map<String, String> windowInfo) {
        session.delWindowID(windowInfo.get("windowID"));
        WindowRegistry.getInstance().removeSession(windowInfo.get("windowID")); }
}
