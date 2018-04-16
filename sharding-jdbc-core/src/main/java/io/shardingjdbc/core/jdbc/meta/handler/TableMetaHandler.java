package io.shardingjdbc.core.jdbc.meta.handler;

import io.shardingjdbc.core.constant.DatabaseType;
import io.shardingjdbc.core.jdbc.core.datasource.MasterSlaveDataSource;
import io.shardingjdbc.core.jdbc.meta.entity.ColumnMeta;
import io.shardingjdbc.core.jdbc.meta.entity.TableMeta;
import lombok.Getter;

import javax.sql.DataSource;
import java.sql.*;

/**
 * The table structure handler.
 *
 * @author panjuan
 */
@Getter
public final class TableMetaHandler {
    
    private final DataSource dataSource;
    
    private final String actualTableName;
    
    private final DatabaseType databaseType;
    
    public TableMetaHandler(final DataSource dataSource, final String actualTableName) throws SQLException {
        
        this.dataSource = dataSource instanceof MasterSlaveDataSource
            ? ((MasterSlaveDataSource) dataSource).getMasterDataSource().values().iterator().next() : dataSource;
        this.actualTableName = actualTableName;
        databaseType = DatabaseType.valueFrom(dataSource.getConnection().getMetaData().getDatabaseProductName());
    }
    
    /**
     *
     *
     * @return
     * @throws SQLException
     */
    public TableMeta getActualTableMeta() throws SQLException {
        switch (databaseType) {
            case MySQL:
                return getMySQLTableMeta();
            default:
                throw new UnsupportedOperationException(String.format("Cannot support database [%s].", databaseType));
        }
    }
    
    private TableMeta getMySQLTableMeta() throws SQLException {
        Connection connection = dataSource.getConnection();
        Statement statement = connection.createStatement();
        statement.executeQuery(String.format("desc %s;", actualTableName));
        ResultSet resultSet = statement.getResultSet();
        TableMeta tableMeta = new TableMeta();
        while (resultSet.next()) {
            String columnName = resultSet.getString("Field");
            String columnType = resultSet.getString("Type");
            String columnKey = resultSet.getString("Key");
            ColumnMeta columnMeta = new ColumnMeta(columnName, columnType, columnKey);
            tableMeta.getColumnMetas().add(columnMeta);
        }
        return tableMeta;
    }
}
