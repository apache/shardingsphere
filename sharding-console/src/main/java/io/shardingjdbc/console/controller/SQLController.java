package io.shardingjdbc.console.controller;

import io.shardingjdbc.console.domain.WorkbenchResponse;
import io.shardingjdbc.console.service.SQLWorkbench;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.RequestBody;

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
     * @param sql sql
     * @param userUUID user id
     * @return sql response result
     */
    @RequestMapping(value = "/sql", method = RequestMethod.POST)
    public WorkbenchResponse executeSql(final @RequestBody String sql, final @CookieValue(value = "userUUID", required = false, defaultValue = "") String userUUID) {
        return sqlWorkbench.execute(sql, userUUID);
    }
}
