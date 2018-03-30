package io.shardingjdbc.console.sql.controller;

import com.google.common.base.Optional;
import io.shardingjdbc.console.session.domain.SessionException;
import io.shardingjdbc.console.sql.domain.*;
import io.shardingjdbc.console.session.domain.Session;
import io.shardingjdbc.console.session.domain.SessionRegistry;
import io.shardingjdbc.console.sql.service.SQLWorkbench;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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
        required = false, defaultValue = "") String userUUID) throws SQLExecuteException, SessionException {
        String sql = sqlInfo.get("sql");
        String windowID = sqlInfo.get("windowID");
        Optional<Session> userSessionOptional = SessionRegistry.getInstance().findSession(userUUID);
    
        if (!userSessionOptional.isPresent()) {
            throw new SessionException("Please login first.");
        }
        try {
            return sqlWorkbench.execute(sql, windowID);
        } catch (SQLExecuteException ex) {
            throw ex;
        }
    }
}
