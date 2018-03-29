package io.shardingjdbc.console.controller;

import com.google.common.base.Optional;
import io.shardingjdbc.console.domain.*;
import io.shardingjdbc.console.service.SQLWorkbench;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.RequestBody;

import java.sql.Connection;
import java.util.Map;

/**
 * Execute SQL controller.
 * 
 * @author zhangyonglun
 */

@RestController
public class SQLController {
    
    @Autowired
    private SQLWorkbench sqlWorkbench;

    /**
     * Execute sql.
     *
     * @param sqlInfo sql and windowID
     * @param userUUID user id
     * @return sql response result
     */
    @RequestMapping(value = "/sql", method = RequestMethod.POST)
    public WorkbenchResponse executeSql(final @RequestBody Map<String, String> sqlInfo, final @CookieValue(value = "userUUID",
        required = false, defaultValue = "") String userUUID) throws SQLExecuteException, UserException {
        String sql = sqlInfo.get("sql");
        String windowID = sqlInfo.get("windowID");
        Optional<UserSession> userSessionOptional = UserSessionRegistry.getInstance().findSession(userUUID);
    
        if (!userSessionOptional.isPresent()) {
            throw new UserException("Please login first.");
        }
        try {
            return sqlWorkbench.execute(sql, windowID);
        } catch (SQLExecuteException ex) {
            throw ex;
        }
    }
}
