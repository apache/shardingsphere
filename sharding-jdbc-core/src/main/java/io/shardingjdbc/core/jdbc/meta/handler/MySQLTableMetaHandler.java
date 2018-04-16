package io.shardingjdbc.core.jdbc.meta.handler;

import io.shardingjdbc.core.jdbc.meta.entity.ColumnMeta;
import io.shardingjdbc.core.jdbc.meta.entity.TableMeta;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * The MySQL table meta handler.
 *
 * @author panjuan
 */
public final class MySQLTableMetaHandler extends AbstractTableMetaHandler {
    
    public MySQLTableMetaHandler(final DataSource dataSource, final String actualTableName) throws SQLException {
        super(dataSource, actualTableName);
    }
    
    /**
     * To get meta of actual table of MySQL.
     *
     * @return table meta
     * @throws SQLException SQL exception.
     */
    public TableMeta getActualTableMeta() throws SQLException {
        Statement statement = dataSource.getConnection().createStatement();
        statement.executeQuery(String.format("desc %s;", actualTableName));
        ResultSet resultSet = statement.getResultSet();
        List<ColumnMeta> columnMetaList = new ArrayList<>();
        while (resultSet.next()) {
            String columnName = resultSet.getString("Field");
            String columnType = resultSet.getString("Type");
            String columnKey = resultSet.getString("Key");
            columnMetaList.add(new ColumnMeta(columnName, columnType, columnKey));
        }
        return new TableMeta(columnMetaList);
    }
}
