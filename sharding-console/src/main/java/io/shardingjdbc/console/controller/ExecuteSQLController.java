package io.shardingjdbc.console.controller;

import io.shardingjdbc.console.domain.SqlResponseResult;
import io.shardingjdbc.console.entity.SessionRegistry;
import io.shardingjdbc.console.service.SqlServer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.RequestBody;

import java.sql.Connection;
import com.google.common.base.Optional;

/**
 * Execute SQL controller.
 * 
 * @author zhangyonglun
 */

@RestController
public class ExecuteSQLController {
    
    @Autowired
    private SqlServer sqlServer;
    
    /**
     * Execute sql.
     * 
     * @param sql sql
     * @param userUUID user id
     * @return sql response result
     */
    @RequestMapping(value = "/sql", method = RequestMethod.POST)
    public SqlResponseResult executeSql(@RequestBody final String sql, final @CookieValue(value = "userUUID", required = false, defaultValue = "") String userUUID) {
        Optional<Connection> session = SessionRegistry.getInstance().findSession(userUUID);
        if (session.isPresent()) {
            return sqlServer.execute(sql, session.get());
        }
        //Todo
        return null;
    }
}
