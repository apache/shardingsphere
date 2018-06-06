/*
 * Copyright 2016-2018 shardingsphere.io.
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

package io.shardingsphere.dbtest.common;

import io.shardingsphere.dbtest.cases.dataset.expected.metadata.ExpectedColumn;
import io.shardingsphere.dbtest.cases.dataset.init.DataSetColumnMetadata;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.Assert.assertTrue;

/**
 * Database utility.
 * 
 * <p>
 * Database related operations a series of methods,
 * including pre-compiled sql processing,
 * non-precompiled sql processing and include result set transformation,
 * get table structure, etc.
 * </p>
 * 
 * @author liu ze jian
 * @author zhangliang
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DatabaseUtil {
    
    /**
     * Execute batch.
     *
     * @param connection connection
     * @param sql SQL
     * @param sqlValueGroups SQL value groups
     * @throws SQLException SQL exception
     */
    public static void executeBatch(final Connection connection, final String sql, final List<SQLValueGroup> sqlValueGroups) throws SQLException {
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            for (SQLValueGroup each : sqlValueGroups) {
                setParameters(preparedStatement, each);
                preparedStatement.addBatch();
            }
            preparedStatement.executeBatch();
        }
    }
    
    private static void setParameters(final PreparedStatement preparedStatement, final SQLValueGroup sqlValueGroup) throws SQLException {
        for (SQLValue each : sqlValueGroup.getSqlValues()) {
            preparedStatement.setObject(each.getIndex(), each.getValue());
        }
    }
    
    /**
     * Execute DQL for statement.
     *
     * @param connection connection
     * @param sql SQL
     * @param sqlValues SQL values
     * @return query result set
     * @throws SQLException SQL exception
     */
    public static List<Map<String, String>> executeQueryForStatement(final Connection connection, final String sql, final Collection<SQLValue> sqlValues) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            try (ResultSet resultSet = statement.executeQuery(generateSQL(sql, sqlValues))) {
                return handleResultSet(resultSet, getDataSetColumnMetadataList(resultSet));
            }
        }
    }
    
    /**
     * Execute query for prepared statement.
     *
     * @param connection connection
     * @param sql SQL
     * @param sqlValues SQL values 
     * @return query result set
     * @throws SQLException SQL exception
     */
    public static List<Map<String, String>> executeQueryForPreparedStatement(final Connection connection, final String sql, final Collection<SQLValue> sqlValues) throws SQLException {
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql.replaceAll("%s", "?"))) {
            for (SQLValue each : sqlValues) {
                preparedStatement.setObject(each.getIndex(), each.getValue());
            }
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                return handleResultSet(resultSet, getDataSetColumnMetadataList(resultSet));
            }
        }
    }
    
    /**
     * Use Statement test SQL select.
     *
     * @param connection connection
     * @param sql SQL
     * @param sqlValues SQL values
     * @return query result set
     * @throws SQLException SQL exception
     */
    public static List<Map<String, String>> executeDQLForStatement(final Connection connection, final String sql, final Collection<SQLValue> sqlValues) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            assertTrue("Not a query statement.", statement.execute(generateSQL(sql, sqlValues)));
            try (ResultSet resultSet = statement.getResultSet()) {
                return handleResultSet(resultSet, getDataSetColumnMetadataList(resultSet));
            }
        }
    }
    
    /**
     * Execute DQL for prepared statement.
     *
     * @param connection connection
     * @param sql SQL
     * @param sqlValues SQL values
     * @return query result set
     * @throws SQLException SQL exception
     */
    public static List<Map<String, String>> executeDQLForPreparedStatement(final Connection connection, final String sql, final Collection<SQLValue> sqlValues) throws SQLException {
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql.replaceAll("%s", "?"))) {
            for (SQLValue each : sqlValues) {
                preparedStatement.setObject(each.getIndex(), each.getValue());
            }
            assertTrue("Not a query statement.", preparedStatement.execute());
            try (ResultSet resultSet = preparedStatement.getResultSet()) {
                return handleResultSet(resultSet, getDataSetColumnMetadataList(resultSet));
            }
        }
    }
    
    /**
     * Execute update for statement.
     *
     * @param connection connection
     * @param sql SQL
     * @param sqlValues SQL values
     * @return Number of rows as a result of execution
     * @throws SQLException SQL exception
     */
    public static int executeUpdateForStatement(final Connection connection, final String sql, final Collection<SQLValue> sqlValues) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            return statement.executeUpdate(generateSQL(sql, sqlValues));
        }
    }
    
    /**
     * Execute DML for statement.
     *
     * @param connection connection
     * @param sql SQL
     * @param sqlValues SQL values
     * @return implementation results
     * @throws SQLException SQL exception
     */
    public static int executeDMLForStatement(final Connection connection, final String sql, final Collection<SQLValue> sqlValues) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            if (!statement.execute(generateSQL(sql, sqlValues))) {
                return statement.getUpdateCount();
            }
        }
        return 0;
    }
    
    private static String generateSQL(final String sql, final Collection<SQLValue> sqlValues) {
        if (null == sqlValues) {
            return sql;
        }
        String result = sql;
        for (SQLValue each : sqlValues) {
            result = Pattern.compile("%s", Pattern.LITERAL).matcher(result)
                    .replaceFirst(Matcher.quoteReplacement(each.getValue() instanceof String ? "'" + each.getValue() + "'" : each.getValue().toString()));
        }
        return result;
    }
    
    /**
     * execute update for prepared statement.
     *
     * @param connection connection
     * @param sql SQL
     * @param sqlValues SQL values
     * @return Number of rows as a result of execution
     * @throws SQLException SQL exception
     */
    public static int executeUpdateForPreparedStatement(final Connection connection, final String sql, final Collection<SQLValue> sqlValues) throws SQLException {
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql.replaceAll("%s", "?"))) {
            for (SQLValue each : sqlValues) {
                preparedStatement.setObject(each.getIndex(), each.getValue());
            }
            return preparedStatement.executeUpdate();
        }
    }
    
    /**
     * Execute DML for prepared statement.
     *
     * @param connection connection
     * @param sql SQL
     * @param sqlValues SQL values
     * @return implementation results
     * @throws SQLException   SQL exception
     */
    public static int executeDMLForPreparedStatement(final Connection connection, final String sql, final Collection<SQLValue> sqlValues) throws SQLException {
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql.replaceAll("%s", "?"))) {
            for (SQLValue each : sqlValues) {
                preparedStatement.setObject(each.getIndex(), each.getValue());
            }
            if (!preparedStatement.execute()) {
                return preparedStatement.getUpdateCount();
            }
        }
        return 0;
    }
    
    private static List<DataSetColumnMetadata> getDataSetColumnMetadataList(final ResultSet resultSet) throws SQLException {
        List<DataSetColumnMetadata> result = new LinkedList<>();
        ResultSetMetaData metaData = resultSet.getMetaData();
        int columnCount = metaData.getColumnCount();
        for (int i = 1; i < columnCount + 1; i++) {
            DataSetColumnMetadata each = new DataSetColumnMetadata();
            each.setName(metaData.getColumnName(i));
            each.setType(Types.DATE == metaData.getColumnType(i) || Types.TIMESTAMP == metaData.getColumnType(i) ? "Date" : "Object");
            result.add(each);
        }
        return result;
    }
    
    private static List<Map<String, String>> handleResultSet(final ResultSet resultSet, final Collection<DataSetColumnMetadata> dataSetColumnMetadataList) throws SQLException {
        List<Map<String, String>> result = new LinkedList<>();
        while (resultSet.next()) {
            Map<String, String> data = new HashMap<>();
            for (DataSetColumnMetadata each : dataSetColumnMetadataList) {
                String name = each.getName();
                if ("Date".equals(each.getType())) {
                    data.put(name, new SimpleDateFormat("yyyy-MM-dd").format(new java.util.Date(resultSet.getDate(name).getTime())));
                } else {
                    data.put(name, String.valueOf(resultSet.getObject(name)));
                }
            }
            result.add(data);
        }
        return result;
    }
    
    /**
     * Get expected columns.
     *
     * @param connection connection
     * @param tableName table
     * @return query result set
     * @throws SQLException SQL exception
     */
    public static List<ExpectedColumn> getExpectedColumns(final Connection connection, final String tableName) throws SQLException {
        DatabaseMetaData metaData = connection.getMetaData();
        try (ResultSet resultSet = metaData.getColumns(null, null, tableName, null)) {
            List<ExpectedColumn> result = new LinkedList<>();
            while (resultSet.next()) {
                ExpectedColumn each = new ExpectedColumn();
                each.setName(resultSet.getString("COLUMN_NAME"));
                each.setType(resultSet.getString("TYPE_NAME").toLowerCase());
                result.add(each);
            }
            return result;
        }
    }
}
