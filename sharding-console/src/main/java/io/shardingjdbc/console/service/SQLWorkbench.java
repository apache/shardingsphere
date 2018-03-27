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

import com.google.common.base.Optional;
import io.shardingjdbc.console.domain.SQLColumnInformation;
import io.shardingjdbc.console.domain.SQLResultData;
import io.shardingjdbc.console.domain.SQLResponseResult;
import io.shardingjdbc.console.domain.SQLRowData;
import org.springframework.stereotype.Service;
import java.sql.Connection;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.ResultSet;

import java.util.ArrayList;
import java.util.List;

/**
 * SQL workbench.
 * 
 * @author zhangyonglun
 */
@Service
public class SQLWorkbench {
    
    /**
     * Handle https for sqls.
     * 
     * @param sql sql
     * @param connectionOptional database connection optional
     * @return SQLResponseResult
     */
    public SQLResponseResult execute(final String sql, final Optional<Connection> connectionOptional) {
        List<SQLColumnInformation> sqlColumnInformationList = new ArrayList<>();
        List<SQLRowData> sqlRowDataList = new ArrayList<>();
        SQLResultData sqlResultData = new SQLResultData("", 0L, sql, sqlColumnInformationList, sqlRowDataList);
        SQLResponseResult sqlResponseResult = new SQLResponseResult(-1, "", sqlResultData);
        
        if (!connectionOptional.isPresent()) {
            sqlResponseResult.setMessage("please login first.");
            return sqlResponseResult;
        }
        Connection connection = connectionOptional.get();
        
        long startTime = System.currentTimeMillis();
        try (
                Statement statement = connection.createStatement();
        ) {
            if (statement.execute(sql)) {
                ResultSet resultSet = statement.getResultSet();
                return setsFormatResult(sqlResponseResult, sqlResultData, resultSet, startTime);
            } else {
                return countsFormatResult(sqlResponseResult, sqlResultData, statement, startTime);
            }
        } catch (SQLException ex) {
            sqlResponseResult.setMessage(ex.getMessage());
            return sqlResponseResult;
        }
    }
    
    private SQLResponseResult countsFormatResult(final SQLResponseResult sqlResponseResult, final SQLResultData sqlResultData, final Statement statement,
                                                 final long startTime) throws SQLException {
        sqlResultData.setAffectedRows(statement.getUpdateCount() + " rows affected");
        sqlResponseResult.setStatus(200);
        sqlResultData.setDurationMilliseconds(System.currentTimeMillis() - startTime);
        return sqlResponseResult;
    }
    
    private SQLResponseResult setsFormatResult(final SQLResponseResult sqlResponseResult, final SQLResultData sqlResultData, final ResultSet resultSet,
                                               final long startTime) throws SQLException {
        ResultSetMetaData resultSetMetaData = resultSet.getMetaData();
        int columnCount = resultSetMetaData.getColumnCount();
        List<SQLColumnInformation> sqlColumnInformationList = sqlResultData.getSqlColumnInformationList();
    
        getColumnInfo(resultSetMetaData, columnCount, sqlColumnInformationList);
        getRowData(resultSetMetaData, sqlResponseResult, sqlResultData, resultSet, startTime, columnCount);
        return sqlResponseResult;
    }
    
    private void getColumnInfo(final ResultSetMetaData resultSetMetaData, final int columnCount, final List<SQLColumnInformation> sqlColumnInformationList) throws SQLException {
        for (int i = 1; i <= columnCount; i++) {
            sqlColumnInformationList.add(new SQLColumnInformation(resultSetMetaData.getColumnName(i), resultSetMetaData.getColumnTypeName(i), resultSetMetaData.getColumnDisplaySize(i)));
        }
    }
    
    private void getRowData(final ResultSetMetaData resultSetMetaData, final SQLResponseResult sqlResponseResult, final SQLResultData sqlResultData, final ResultSet resultSet, final long startTime, final int columnCount) throws SQLException {
        List<SQLRowData> sqlRowDataList = sqlResultData.getSqlRowDataList();
        Integer rowCount = 0;
        
        while (resultSet.next()) {
            rowCount++;
            SQLRowData sqlRowData = new SQLRowData();
            for (int i = 1; i <= columnCount; i++) {
                sqlRowData.getRowData().put(resultSetMetaData.getColumnName(i), resultSet.getString(i));
            }
            sqlRowDataList.add(sqlRowData);
        }
        sqlResultData.setAffectedRows(rowCount + " rows affected");
        sqlResponseResult.setStatus(200);
        sqlResultData.setDurationMilliseconds(System.currentTimeMillis() - startTime);
    }
}
