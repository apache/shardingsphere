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

import io.shardingjdbc.console.constant.LoginInfo;
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

        String driver = (String) httpSession.getAttribute(LoginInfo.DATASOURCE_DRIVER);
        String url = httpSession.getAttribute(LoginInfo.DATASOURCE_URL) + "?" + LoginInfo.DATASOURCE_PARAM_USE_AFFECTED_ROWS;
        String username = (String) httpSession.getAttribute(LoginInfo.DATASOURCE_USERNAME);
        String password = (String) httpSession.getAttribute(LoginInfo.DATASOURCE_PASSWORD);
        Map<String, Object> resultInfo = new LinkedHashMap<>();
        SqlResponseResult sqlResponseResult = new SqlResponseResult(resultInfo);

        if (null == driver) {
            sqlResponseResult.setErrMsg("please login first.");
            return sqlResponseResult;
        }

        Connection conn = null;
        Statement stmt = null;
        ResultSet resultSet = null;
        ResultSetMetaData resultSetMetaData;
        long startTime = System.currentTimeMillis();

        try {
            Class.forName(driver);
            conn = DriverManager.getConnection(url, username, password);
            stmt = conn.createStatement();

            if (stmt.execute(sql)) {
                resultSet = stmt.getResultSet();
            } else {
                resultInfo.put("tip", stmt.getUpdateCount() + " rows affected");
                return sqlResponseResult;
            }

            resultSetMetaData = resultSet.getMetaData();
            int columnCount = resultSetMetaData.getColumnCount();
            Map<String, String> types = new LinkedHashMap<>();

            for (int i = 1; i <= columnCount; i++) {
                types.put(resultSetMetaData.getColumnName(i),
                        resultSetMetaData.getColumnTypeName(i) + "(" + resultSetMetaData.getColumnDisplaySize(i) + ")");
            }

            List<Map<String, String>> resList = new ArrayList<>();
            while (resultSet.next()) {
                Map<String, String> data = new LinkedHashMap<>();
                for (int i = 1; i <= columnCount; i++) {
                    data.put(resultSetMetaData.getColumnName(i), resultSet.getString(i));
                }
                resList.add(data);
                resultInfo.put("tip", resultSet.getRow() + " rows affected");
            }

            resultInfo.put("duration", System.currentTimeMillis() - startTime);
            resultInfo.put("sql", sql);
            resultInfo.put("types", types);
            resultInfo.put("data", resList);
            sqlResponseResult.setStatusCode(0);
            resultSet.close();
            stmt.close();
            conn.close();
        } catch (SQLException sqe) {
            sqlResponseResult.setErrMsg(sqe.getMessage());
        } catch (Exception e) {
            sqlResponseResult.setErrMsg(e.getMessage());
        } finally {
            try {
                if (resultSet != null) {
                    resultSet.close();
                }
            } catch (SQLException rse) { }
            try {
                if (stmt != null)
                    stmt.close();
            } catch (SQLException sse) { }
            try {
                if (conn != null)
                    conn.close();
            } catch (SQLException cse) { }
            return sqlResponseResult;
        }
    }
}
