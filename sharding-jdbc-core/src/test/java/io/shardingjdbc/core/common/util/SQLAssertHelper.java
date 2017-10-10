package io.shardingjdbc.core.common.util;

import io.shardingjdbc.core.common.env.DatabaseEnvironment;
import io.shardingjdbc.core.constant.DatabaseType;
import io.shardingjdbc.core.jdbc.adapter.AbstractDataSourceAdapter;
import lombok.RequiredArgsConstructor;
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

import static io.shardingjdbc.core.common.util.SQLPlaceholderUtil.replacePreparedStatement;
import static io.shardingjdbc.core.common.util.SQLPlaceholderUtil.replaceStatement;
import static org.dbunit.Assertion.assertEquals;

@RequiredArgsConstructor
public class SQLAssertHelper {
    
    private final String sql;
    
    public void executeWithPreparedStatement(final boolean isExecute, final AbstractDataSourceAdapter abstractDataSourceAdapter, final List<String> parameters) throws SQLException {
        try (Connection connection = abstractDataSourceAdapter.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(replacePreparedStatement(sql))) {
            setParameters(preparedStatement, parameters);
            if (isExecute) {
                preparedStatement.execute();
            } else {
                preparedStatement.executeUpdate();
            }
        }
    }
    
    public void executeWithStatement(final boolean isExecute, final AbstractDataSourceAdapter abstractDataSourceAdapter, final List<String> parameters) throws SQLException {
        try (Connection connection = abstractDataSourceAdapter.getConnection();
             Statement statement = connection.createStatement()) {
            if (isExecute) {
                statement.execute(replaceStatement(sql, parameters.toArray()));
            } else {
                statement.executeUpdate(replaceStatement(sql, parameters.toArray()));
            }
        }
    }
    
    public void executeQueryWithPreparedStatement(final boolean isExecute, final AbstractDataSourceAdapter abstractDataSourceAdapter, final List<String> parameters, final File file)
            throws MalformedURLException, SQLException, DatabaseUnitException {
        try (Connection conn = abstractDataSourceAdapter.getConnection();
             PreparedStatement preparedStatement = conn.prepareStatement(replacePreparedStatement(sql))) {
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
                assertEquals(expectedDataSet.getTable(tableName), actualTable);
            }
        }
    }
    
    public void executeQueryWithStatement(final AbstractDataSourceAdapter abstractDataSourceAdapter, final List<String> parameters, final File file)
            throws MalformedURLException, SQLException, DatabaseUnitException {
        try (Connection conn = abstractDataSourceAdapter.getConnection()) {
            String querySql = replaceStatement(sql, parameters.toArray());
            ReplacementDataSet expectedDataSet = new ReplacementDataSet(new FlatXmlDataSetBuilder().build(file));
            expectedDataSet.addReplacementObject("[null]", null);
            for (ITable each : expectedDataSet.getTables()) {
                String tableName = each.getTableMetaData().getTableName();
                IDatabaseConnection connection = DBUnitUtil.getConnection(new DatabaseEnvironment(DatabaseType.valueFrom(conn.getMetaData().getDatabaseProductName())), conn);
                ITable actualTable = connection.createQueryTable(tableName, querySql);
                // TODO customized CachedResultSetTable with statement 
                assertEquals(expectedDataSet.getTable(tableName), actualTable);
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
                assertEquals(expectedTable, actualTable);
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
