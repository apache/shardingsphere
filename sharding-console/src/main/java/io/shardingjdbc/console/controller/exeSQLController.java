package io.shardingjdbc.console.controller;


import io.shardingjdbc.console.domain.SqlResponseResult;
import io.shardingjdbc.console.entity.GlobalSessions;
import io.shardingjdbc.console.service.SqlServer;
import io.shardingjdbc.console.service.SqlService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import io.shardingjdbc.console.constant.RespCode;
import io.shardingjdbc.console.entity.RespObj;
import org.springframework.web.bind.annotation.*;
import sun.jvm.hotspot.runtime.ResultTypeFinder;

import java.sql.Connection;
import java.util.*;

/**
 * SelectController.
 *
 * @author zhangyonglun
 */

@RestController
public class exeSQLController {
    @Autowired
    private SqlServer sqlServer;

    /**
     *
     * @return result
     */


    @RequestMapping(value = "/sql", method = RequestMethod.POST)
    public SqlResponseResult executeSql(String sql, @CookieValue(value = "userUUID", required = false,
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
