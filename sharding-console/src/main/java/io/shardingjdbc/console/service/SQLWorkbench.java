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
import io.shardingjdbc.console.domain.WorkbenchResponse;
import io.shardingjdbc.console.domain.SQLColumnInformation;
import io.shardingjdbc.console.domain.SQLRowData;
import io.shardingjdbc.console.domain.SQLResultData;
import io.shardingjdbc.console.domain.WindowSessionRegistry;
import io.shardingjdbc.console.domain.SQLExecuteException;
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
     * @param windowID window uuid
     * @return response
     */
    public WorkbenchResponse execute(final String sql, final String windowID) throws SQLExecuteException {
        List<SQLColumnInformation> sqlColumnInformationList = new ArrayList<>();
        List<SQLRowData> sqlRowDataList = new ArrayList<>();
        SQLResultData sqlResultData = new SQLResultData(0, 0L, sql, sqlColumnInformationList, sqlRowDataList);
        WorkbenchResponse result = new WorkbenchResponse(sqlResultData);
        Optional<Connection> connectionOptional = WindowSessionRegistry.getInstance().findSession(windowID);

        if (!connectionOptional.isPresent()) {
            throw new SQLExecuteException("The SQL execute window does not exist.");
        }
        Connection connection = connectionOptional.get();

        long startTime = System.currentTimeMillis();
        try (
                Statement statement = connection.createStatement()
        ) {
            if (statement.execute(sql)) {
                ResultSet resultSet = statement.getResultSet();
                return setsFormatResult(result, sqlResultData, resultSet, startTime);
            } else {
                return countsFormatResult(result, sqlResultData, statement, startTime);
            }
        } catch (final SQLException ex) {
            result.setMessage(ex.getMessage());
            return result;
        }
    }

    private WorkbenchResponse countsFormatResult(final WorkbenchResponse result, final SQLResultData sqlResultData, final Statement statement,
                                                 final long startTime) throws SQLException {
        sqlResultData.setAffectedRows(statement.getUpdateCount());
        sqlResultData.setDurationMilliseconds(System.currentTimeMillis() - startTime);
        return result;
    }

    private WorkbenchResponse setsFormatResult(final WorkbenchResponse result, final SQLResultData sqlResultData, final ResultSet resultSet,
                                               final long startTime) throws SQLException {
        ResultSetMetaData resultSetMetaData = resultSet.getMetaData();
        int columnCount = resultSetMetaData.getColumnCount();
        getColumnInfo(resultSetMetaData, columnCount, sqlResultData);
        getRowData(resultSetMetaData, sqlResultData, resultSet, startTime, columnCount);
        return result;
    }
    
    private void getColumnInfo(final ResultSetMetaData resultSetMetaData, final int columnCount, final SQLResultData sqlResultData) throws SQLException {
        List<SQLColumnInformation> sqlColumnInformationList = sqlResultData.getSqlColumnInformationList();
        SQLColumnInformation sqlColumnInformation;
        for (int i = 1; i <= columnCount; i++) {
            sqlColumnInformation = new SQLColumnInformation(resultSetMetaData.getColumnLabel(i), resultSetMetaData.getColumnTypeName(i), resultSetMetaData.getColumnDisplaySize(i));
            int maxTryTimes = 32;
            while (sqlColumnInformationList.contains(sqlColumnInformation) && maxTryTimes > 0) {
                changeColumnLabel(sqlColumnInformation);
                maxTryTimes--;
            }
            sqlColumnInformationList.add(sqlColumnInformation);
        }
    }
    
    private void changeColumnLabel(final SQLColumnInformation sqlColumnInformation) {
        sqlColumnInformation.setColumnLabel(sqlColumnInformation.getColumnLabel() + "1");
    }
    
    private void getRowData(final ResultSetMetaData resultSetMetaData, final SQLResultData sqlResultData, final ResultSet resultSet,
                            final long startTime, final int columnCount) throws SQLException {
        List<SQLColumnInformation> sqlColumnInformationList = sqlResultData.getSqlColumnInformationList();
        List<SQLRowData> sqlRowDataList = sqlResultData.getSqlRowDataList();
        int rowCount = 0;
        
        while (resultSet.next()) {
            rowCount++;
            SQLRowData sqlRowData = new SQLRowData();
            for (int i = 1; i <= columnCount; i++) {
                sqlRowData.getRowData().put(resultSetMetaData.getColumnLabel(i), resultSet.getString(i));
                sqlRowData.getRowData().put(sqlColumnInformationList.get(i - 1).getColumnLabel(), resultSet.getString(i));
            }
            sqlRowDataList.add(sqlRowData);
        }
        sqlResultData.setAffectedRows(rowCount);
        sqlResultData.setDurationMilliseconds(System.currentTimeMillis() - startTime);
    }
}
