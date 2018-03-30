package io.shardingjdbc.console.session.controller;

import com.google.common.base.Optional;
import io.shardingjdbc.console.session.domain.*;
import org.springframework.web.bind.annotation.*;
import io.shardingjdbc.console.common.domain.WorkbenchResponse;

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
                                   final HttpServletResponse response) throws SessionException {
        if (!"".equals(userUUID)) {
            if (SessionRegistry.getInstance().findSession(userUUID).isPresent()) {
                return new WorkbenchResponse("Logged in.");
            }
            setCookie(userUUID, response, 0);
            throw new SessionException("Please login first.");
        }
        Window window = new Window(userUUID);
        addWindow(session, window);
        SessionRegistry.getInstance().addSession(session.getId(), session);
        setCookie(session.getId(), response, 1);
        
        Map<String, String> result = new HashMap<>(1, 1);
        result.put("windowID", window.getId());
        return new WorkbenchResponse("Login succeeded", result);
    }

    private void addWindow(final Session session, final Window window) {
        session.addWindowID(window.getId());
        WindowRegistry.getInstance().addWindow(window.getId(), window);
    }
    
    /**
     * Set cookies.
     *
     * @param userUUID session id
     * @param response response
     * @param operationType 1 for add cookie; 0 for del cookie
     */
    private void setCookie(String userUUID, HttpServletResponse response, int operationType) {
        Cookie cookie = new Cookie("userUUID", userUUID);
        Integer cookieTime = (operationType == 1 ? 120 * 60 : 0);
        cookie.setMaxAge(cookieTime);
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
    public WorkbenchResponse exit(final @CookieValue(value = "userUUID",
        required = false, defaultValue = "") String userUUID, final HttpServletResponse response) throws SQLException {
        if (!"".equals(userUUID)) {
            removeSession(userUUID);
            setCookie(userUUID, response, 0);
        }
        return new WorkbenchResponse("Logout succeeded");
    }

    private void removeSession(final String userUUID) throws SQLException {
        Optional<Session> userSessionOptional = SessionRegistry.getInstance().findSession(userUUID);
        if (userSessionOptional.isPresent()) {
            List<String> windowIDList = userSessionOptional.get().getWindowIDList();
            for (String windowID : windowIDList) {
                WindowRegistry.getInstance().removeWindow(windowID);
            }
        }
        SessionRegistry.getInstance().removeSession(userUUID);
    }
    
    @RequestMapping(value = "/addWindow", method = RequestMethod.POST)
    public WorkbenchResponse addWindow(final @CookieValue(value = "userUUID",
        required = false, defaultValue = "") String userUUID) throws SessionException {
        Window window = new Window(userUUID);
        addWindow(SessionRegistry.getInstance().findSession(userUUID).get(), window);
      
        Map<String, String> result = new HashMap<>();
        result.put("windowID", window.getId());
        return new WorkbenchResponse("Open new window OK.", result);
    }
    
    @RequestMapping(value = "/delWindow", method = RequestMethod.POST)
    public WorkbenchResponse delWindow(@RequestBody final Map<String, String> windowInfo, final @CookieValue(value = "userUUID",
        required = false, defaultValue = "") String userUUID) throws SQLException, SessionException {
        Optional<Session> userSessionOptional = SessionRegistry.getInstance().findSession(userUUID);
        if (!userSessionOptional.isPresent()) {
            throw new SessionException("Please login first.");
        }
        
        Session session = userSessionOptional.get();
        Optional<Window> windowOptional = WindowRegistry.getInstance().findWindow(windowInfo.get("windowID"));
        if (windowOptional.isPresent()) {
            Window window = windowOptional.get();
            delWindow(session, window);
        }
        return new WorkbenchResponse("Close window OK.");
    }
    
    private void delWindow(final Session session, final Window window) throws SQLException {
        session.delWindowID(window.getId());
        WindowRegistry.getInstance().removeWindow(window.getId()); }
}
