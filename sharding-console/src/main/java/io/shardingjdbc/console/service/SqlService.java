/*
 * Copyright 1999-2015 dangdang.com.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.shardingjdbc.console.service;

import io.shardingjdbc.console.domain.AccountInfo;
import io.shardingjdbc.console.domain.ResultInfo;
import io.shardingjdbc.console.domain.SQLResponseResult;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpSession;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.ResultSetMetaData;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.ArrayList;

/**
 * SqlService.
 *
 * @author zhangyonglun
 */
@Service
public class SqlService {
    
    /**
     * execute sql.
     *
     * @param sql sql string
     * @param httpSession httpSession
     * @return sql response result
     */
    public SQLResponseResult execute(final String sql, final HttpSession httpSession) {
        ResultInfo resultInfo = new ResultInfo("", 0L, "", null, null);
        SQLResponseResult sqlResponseResult = new SQLResponseResult(-1, "", resultInfo);
        AccountInfo accountInfo = (AccountInfo) httpSession.getAttribute("accountInfo");
        
        if (null == accountInfo) {
            sqlResponseResult.setErrMsg("please login first.");
            return sqlResponseResult;
        }
        long startTime = System.currentTimeMillis();
        Connection connection = null;
        Statement statement = null;
        ResultSet resultSet = null;
        
        try {
            Class.forName(accountInfo.getDriver());
            connection = DriverManager.getConnection(accountInfo.getUrl(), accountInfo.getUsername(), accountInfo.getPassword());
            statement = connection.createStatement();
            
            if (statement.execute(sql)) {
                resultSet = statement.getResultSet();
                return setsFormatResult(sqlResponseResult, resultInfo, resultSet, startTime, sql);
            } else {
                return countsFormatResult(sqlResponseResult, resultInfo, statement, startTime, sql);
            }
        } catch (SQLException | ClassNotFoundException ex) {
            sqlResponseResult.setErrMsg(ex.getMessage());
            return sqlResponseResult;
        } finally {
            closeQuietly(connection, statement, resultSet);
        }
    }
    
    private SQLResponseResult countsFormatResult(final SQLResponseResult sqlResponseResult, final ResultInfo resultInfo, final Statement statement,
                                                 final long startTime, final String sql) throws SQLException {
        resultInfo.setAffectedRows(statement.getUpdateCount() + " rows affected");
        resultInfo.setSql(sql);
        sqlResponseResult.setStatusCode(0);
        resultInfo.setDurationMilliseconds(System.currentTimeMillis() - startTime);
        return sqlResponseResult;
    }
    
    private SQLResponseResult setsFormatResult(final SQLResponseResult sqlResponseResult, final ResultInfo resultInfo, final ResultSet resultSet,
                                               final long startTime, final String sql) throws SQLException {
        ResultSetMetaData resultSetMetaData = resultSet.getMetaData();
        int columnCount = resultSetMetaData.getColumnCount();
        Map<String, String> types = new LinkedHashMap<>();
        
        for (int i = 1; i <= columnCount; i++) {
            types.put(resultSetMetaData.getColumnName(i),
                    resultSetMetaData.getColumnTypeName(i) + "(" + resultSetMetaData.getColumnDisplaySize(i) + ")");
        }
        List<Map<String, String>> dataList = new ArrayList<>();
        
        while (resultSet.next()) {
            Map<String, String> data = new LinkedHashMap<>();
            for (int i = 1; i <= columnCount; i++) {
                data.put(resultSetMetaData.getColumnName(i), resultSet.getString(i));
            }
            dataList.add(data);
            resultInfo.setAffectedRows(resultSet.getRow() + " rows affected");
        }
        resultInfo.setSql(sql);
        resultInfo.setTypes(types);
        resultInfo.setData(dataList);
        sqlResponseResult.setStatusCode(0);
        resultInfo.setDurationMilliseconds(System.currentTimeMillis() - startTime);
        return sqlResponseResult;
    }
    
    private void closeQuietly(final Connection connection, final Statement statement, final ResultSet resultSet) {
        if (resultSet != null) {
            try {
                resultSet.close();
            } catch (SQLException ignore) {
            }
        }
        
        if (statement != null) {
            try {
                statement.close();
            } catch (SQLException ignore) {
            }
        }
        
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException ignore) {
            }
        }
    }
}
