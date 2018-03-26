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
 * SelectController.
 *
 * @author zhangyonglun
 */

@RestController
public class ExecuteSQLController {
    @Autowired
    private SqlServer sqlServer;

    /**
     *
     * @return result
     */
    @RequestMapping(value = "/sql", method = RequestMethod.POST)
    public SqlResponseResult executeSql(final String sql,final @CookieValue(value = "userUUID", required = false,
            defaultValue = "") String userUUID) {
        System.out.println("");
        System.out.println(sql);
        System.out.println(userUUID);
        System.out.println("");
        Map<String, Connection> connectionMap = GlobalSessions.getSessionInfos();
        Connection connection = connectionMap.get(userUUID);
        return sqlServer.execute(sql, connection);
    }
}
