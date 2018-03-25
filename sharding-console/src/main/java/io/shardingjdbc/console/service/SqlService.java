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
import io.shardingjdbc.console.domain.SqlResponseResult;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpSession;
import java.sql.*;
import java.util.*;

/**
 * SqlService.
 *
 * @author zhangyonglun
 */
@Service
public class SqlService {

    public SqlResponseResult execute(final String sql, final HttpSession httpSession) {
        SqlResponseResult sqlResponseResult = new SqlResponseResult();
        ResultInfo resultInfo = sqlResponseResult.getResultInfo();
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
        } catch (SQLException sqe) {
            sqlResponseResult.setErrMsg(sqe.getMessage());
        } catch (Exception e) {
            sqlResponseResult.setErrMsg(e.getMessage());
        } finally {
            closeQuietly(connection, statement, resultSet);
            return sqlResponseResult;
        }
    }

    private SqlResponseResult countsFormatResult(SqlResponseResult sqlResponseResult, ResultInfo resultInfo, Statement statement, long startTime, String sql) throws SQLException {
        resultInfo.setTip(statement.getUpdateCount() + " rows affected");
        resultInfo.setSql(sql);
        sqlResponseResult.setStatusCode(0);
        resultInfo.setDuration(System.currentTimeMillis() - startTime);
        return sqlResponseResult;
    }

    private SqlResponseResult setsFormatResult(SqlResponseResult sqlResponseResult, ResultInfo resultInfo, ResultSet resultSet, long startTime, String sql) throws SQLException {
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
            resultInfo.setTip(resultSet.getRow() + " rows affected");
        }
        resultInfo.setSql(sql);
        resultInfo.setTypes(types);
        resultInfo.setData(dataList);
        sqlResponseResult.setStatusCode(0);
        resultInfo.setDuration(System.currentTimeMillis() - startTime);
        return sqlResponseResult;
    }

    private void closeQuietly(Connection connection, Statement statement, ResultSet resultSet) {
        if (resultSet != null) {
            try {
                resultSet.close();
            } catch (SQLException rse) {

            }
        }

        if (statement != null) {
            try {
                statement.close();
            } catch (SQLException sse) {

            }
        }

        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException cse) {

            }
        }
    }
}
