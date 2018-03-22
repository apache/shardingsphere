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

package io.shardingjdbc.core.util;

import io.shardingjdbc.core.common.env.DatabaseEnvironment;
import io.shardingjdbc.core.constant.DatabaseType;
import io.shardingjdbc.core.jdbc.adapter.AbstractDataSourceAdapter;
import lombok.RequiredArgsConstructor;
import org.dbunit.Assertion;
import org.dbunit.DatabaseUnitException;
import org.dbunit.database.CachedResultSetTable;
import org.dbunit.database.ForwardOnlyResultSetTable;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.database.ResultSetTableMetaData;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.ITable;
import org.dbunit.dataset.ITableIterator;
import org.dbunit.dataset.ITableMetaData;
import org.dbunit.dataset.ReplacementDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSetBuilder;

import java.io.File;
import java.net.MalformedURLException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

@RequiredArgsConstructor
public final class SQLAssertHelper {
    
    private final String sql;
    
    /**
     * Execute with statement.
     *
     * @param isExecute is execute method
     * @param abstractDataSourceAdapter data source adapter
     * @param parameters parameters
     * @throws SQLException SQL exception
     */
    public void executeWithStatement(final boolean isExecute, final AbstractDataSourceAdapter abstractDataSourceAdapter, final List<String> parameters) throws SQLException {
        try (Connection connection = abstractDataSourceAdapter.getConnection();
             Statement statement = connection.createStatement()) {
            if (isExecute) {
                statement.execute(SQLPlaceholderUtil.replaceStatement(sql, parameters.toArray()));
            } else {
                statement.executeUpdate(SQLPlaceholderUtil.replaceStatement(sql, parameters.toArray()));
            }
        }
    }
    
    /**
     * Execute with prepared statement.
     * 
     * @param isExecute is execute method
     * @param abstractDataSourceAdapter data source adapter
     * @param parameters parameters
     * @throws SQLException SQL exception
     */
    public void executeWithPreparedStatement(final boolean isExecute, final AbstractDataSourceAdapter abstractDataSourceAdapter, final List<String> parameters) throws SQLException {
        try (Connection connection = abstractDataSourceAdapter.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(SQLPlaceholderUtil.replacePreparedStatement(sql))) {
            setParameters(preparedStatement, parameters);
            if (isExecute) {
                preparedStatement.execute();
            } else {
                preparedStatement.executeUpdate();
            }
        }
    }
    
    /**
     * Execute query with statement.
     * 
     * @param abstractDataSourceAdapter data source adapter
     * @param parameters parameters
     * @param file file
     * 
     * @throws MalformedURLException malformed URL exception
     * @throws SQLException SQL exception
     * @throws DatabaseUnitException database unit exception
     */
    public void executeQueryWithStatement(final AbstractDataSourceAdapter abstractDataSourceAdapter, final List<String> parameters, final File file)
            throws MalformedURLException, SQLException, DatabaseUnitException {
        try (Connection conn = abstractDataSourceAdapter.getConnection()) {
            String querySql = SQLPlaceholderUtil.replaceStatement(sql, parameters.toArray());
            ReplacementDataSet expectedDataSet = new ReplacementDataSet(new FlatXmlDataSetBuilder().build(file));
            expectedDataSet.addReplacementObject("[null]", null);
            for (ITable each : expectedDataSet.getTables()) {
                String tableName = each.getTableMetaData().getTableName();
                IDatabaseConnection connection = DBUnitUtil.getConnection(new DatabaseEnvironment(DatabaseType.valueFrom(conn.getMetaData().getDatabaseProductName())), conn);
                ITable actualTable = connection.createQueryTable(tableName, querySql);
                // TODO customized CachedResultSetTable with statement 
                Assertion.assertEquals(expectedDataSet.getTable(tableName), actualTable);
            }
        }
    }
    
    /**
     * Execute query with prepared statement.
     * 
     * @param isExecute is execute method
     * @param abstractDataSourceAdapter data source adapter
     * @param parameters parameters
     * @param file file
     *
     * @throws MalformedURLException malformed URL exception
     * @throws SQLException SQL exception
     * @throws DatabaseUnitException database unit exception
     */
    public void executeQueryWithPreparedStatement(final boolean isExecute, final AbstractDataSourceAdapter abstractDataSourceAdapter, final List<String> parameters, final File file)
            throws MalformedURLException, SQLException, DatabaseUnitException {
        try (Connection conn = abstractDataSourceAdapter.getConnection();
             PreparedStatement preparedStatement = conn.prepareStatement(SQLPlaceholderUtil.replacePreparedStatement(sql))) {
            setParameters(preparedStatement, parameters);
            ReplacementDataSet expectedDataSet = new ReplacementDataSet(new FlatXmlDataSetBuilder().build(file));
            expectedDataSet.addReplacementObject("[null]", null);
            for (ITable each : expectedDataSet.getTables()) {
                String tableName = each.getTableMetaData().getTableName();
                IDatabaseConnection connection = DBUnitUtil.getConnection(new DatabaseEnvironment(DatabaseType.valueFrom(conn.getMetaData().getDatabaseProductName())), conn);
                ITable actualTable;
                if (isExecute) {
                    actualTable = createTable(tableName, preparedStatement, connection);
                } else {
                    actualTable = connection.createTable(tableName, preparedStatement);
                }
                Assertion.assertEquals(expectedDataSet.getTable(tableName), actualTable);
            }
        }
    }
    
    private CachedResultSetTable createTable(final String tableName, final PreparedStatement preparedStatement, final IDatabaseConnection connection) throws SQLException, DataSetException {
        preparedStatement.execute();
        ResultSet rs = preparedStatement.getResultSet();
        ITableMetaData metaData = new ResultSetTableMetaData(tableName, rs, connection, false);
        ForwardOnlyResultSetTable table = new ForwardOnlyResultSetTable(metaData, rs);
        return new CachedResultSetTable(table);
    }
    
    private void setParameters(final PreparedStatement preparedStatement, final List<String> parameters) throws SQLException {
        int index = 1;
        for (String each : parameters) {
            if (each.contains("'")) {
                preparedStatement.setString(index++, each.replace("'", ""));
            } else if ("null".equalsIgnoreCase(each)) {
                preparedStatement.setObject(index++, null);
            } else {
                preparedStatement.setInt(index++, Integer.valueOf(each));
            }
        }
    }
    
    public void assertResult(final Connection connection, final File file) throws MalformedURLException, SQLException, DatabaseUnitException {
        if (sql.contains("TEMP")) {
            return;
        }
        ITableIterator expectedTableIterator = new FlatXmlDataSetBuilder().build(file).iterator();
        try (Connection conn = connection) {
            while (expectedTableIterator.next()) {
                ITable expectedTable = expectedTableIterator.getTable();
                String actualTableName = expectedTable.getTableMetaData().getTableName();
                String verifySql = "SELECT * FROM " + actualTableName + " WHERE status = '" + getStatus(file) + "'" + getOrderByCondition(actualTableName);
                ITable actualTable = DBUnitUtil.getConnection(new DatabaseEnvironment(DatabaseType.valueFrom(conn.getMetaData().getDatabaseProductName())), conn)
                        .createQueryTable(actualTableName, verifySql);
                Assertion.assertEquals(expectedTable, actualTable);
            }
        }
    }
    
    private String getOrderByCondition(final String actualTableName) {
        switch (actualTableName) {
            case "t_order": return " ORDER BY order_id ASC";
            case "t_order_item": return " ORDER BY item_id ASC";
            default: return "";
        }
    }
    
    private String getStatus(final File file) {
        return sql.toUpperCase().startsWith("DELETE") ? "init" : file.getParentFile().getName();
    }
}
