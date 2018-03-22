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
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpSession;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * SqlService.
 *
 * @author zhangyonglun
 */
@Service
public class SqlService {

    public List<String> execute(String sql, HttpSession httpSession) {

        String driver = (String) httpSession.getAttribute(LoginInfo.DATASOURCE_DRIVER);


        String url = (String) httpSession.getAttribute(LoginInfo.DATASOURCE_URL);

        String username = (String) httpSession.getAttribute(LoginInfo.DATASOURCE_USERNAME);

        String password = (String) httpSession.getAttribute(LoginInfo.DATASOURCE_PASSWORD);

        List<String> result = new ArrayList<>();

        Connection conn = null;

        Statement stmt = null;

        ResultSet resultSet = null;

        ResultSetMetaData resultSetMetaData = null;

        try {
            Class.forName(driver);
            conn = DriverManager.getConnection(url, username, password);
            stmt = conn.createStatement();
            resultSet = stmt.executeQuery(sql);
            resultSetMetaData = resultSet.getMetaData();
            int columnCount = resultSetMetaData.getColumnCount();
            for (int i = 1; i <= columnCount; i++) {
                result.add(resultSetMetaData.getColumnName(i)
                        + " : " + resultSetMetaData.getColumnTypeName(i)
                        + "(" + resultSetMetaData.getColumnDisplaySize(i) + ")");
            }
            while (resultSet.next()) {
                for (int i = 1; i <= columnCount; i++) {
                    result.add(resultSet.getString(i));
                }
            }
            resultSet.close();
            stmt.close();
            conn.close();
        } catch (SQLException sqe) {
            return null;
        } catch (Exception e) {
            return null;
        } finally {
            try {
                if (resultSet != null) {
                    resultSet.close();
                }
            } catch (SQLException rse) {
            }
            try {
                if (stmt != null)
                    stmt.close();
            } catch (SQLException sse) {
            }
            try {
                if (conn != null)
                    conn.close();
            } catch (SQLException cse) {
            }
        }
        return result;
    }
}
