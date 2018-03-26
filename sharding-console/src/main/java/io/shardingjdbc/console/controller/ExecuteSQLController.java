package io.shardingjdbc.console.controller;

import io.shardingjdbc.console.domain.SqlResponseResult;
import io.shardingjdbc.console.entity.GlobalSessions;
import io.shardingjdbc.console.service.SqlServer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.*;
import java.sql.Connection;
import java.util.*;

/**
 * ExecuteSQLController.
 *
 * @author zhangyonglun
 */

@RestController
public class ExecuteSQLController {
    
    @Autowired
    private SqlServer sqlServer;

    /**
     * execute sql.
     * @param sql
     * @param userUUID
     * @return SqlResponseResult
     */
    @RequestMapping(value = "/sql", method = RequestMethod.POST)
    public SqlResponseResult executeSql(final String sql, final @CookieValue(value = "userUUID", required = false, defaultValue = "") String userUUID) {
        Map<String, Connection> connectionMap = GlobalSessions.getSessionInfo();
        Connection connection = connectionMap.get(userUUID);
        return sqlServer.execute(sql, connection);
    }
}
