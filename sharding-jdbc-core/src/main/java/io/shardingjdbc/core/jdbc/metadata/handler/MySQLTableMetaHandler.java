package io.shardingjdbc.core.jdbc.metadata.handler;

import io.shardingjdbc.core.jdbc.metadata.entity.ColumnMeta;
import io.shardingjdbc.core.jdbc.metadata.entity.TableMeta;
import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * The MySQL table metadata handler.
 *
 * @author panjuan
 */
public final class MySQLTableMetaHandler extends AbstractTableMetaHandler {
    
    public MySQLTableMetaHandler(final DataSource dataSource, final String actualTableName) throws SQLException {
        super(dataSource, actualTableName);
    }
    
    /**
     * To get metadata of actual table of MySQL.
     *
     * @return table metadata
     * @throws SQLException SQL exception.
     */
    public TableMeta getActualTableMeta() throws SQLException {
        try (Statement statement = dataSource.getConnection().createStatement()) {
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
}
