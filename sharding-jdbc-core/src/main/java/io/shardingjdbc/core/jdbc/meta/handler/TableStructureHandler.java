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
public final class TableStructureHandler {
    
    private DataSource dataSource;
    
    private String actualTableName;
    
    private DatabaseType databaseType;
    
    public TableStructureHandler(final DataSource dataSource, final String actualTableName) throws SQLException {
        
        this.dataSource = dataSource instanceof MasterSlaveDataSource ?
            ((MasterSlaveDataSource) dataSource).getMasterDataSource().values().iterator().next() : dataSource;
        this.dataSource = dataSource;
        this.actualTableName = actualTableName;
        databaseType = DatabaseType.valueFrom(dataSource.getConnection().getMetaData().getDatabaseProductName());
    }
    
    public TableMeta getActualTableStructure() throws SQLException {
        switch (databaseType) {
            case MySQL:
                return getMySQLTableStructure();
            default:
                throw new UnsupportedOperationException(String.format("Cannot support database [%s].", databaseType));
        }
    }
    
    private TableMeta getMySQLTableStructure() throws SQLException {
        Connection connection = dataSource.getConnection();
        Statement statement = connection.createStatement();
        statement.executeQuery(String.format("desc %s;", actualTableName));
        ResultSet resultSet = statement.getResultSet();
        final TableMeta tableMeta = new TableMeta();
        while (resultSet.next()) {
            String columnName = resultSet.getString("Field");
            String columnType = resultSet.getString("Type");
            String columnKey = resultSet.getString("Key");
            final ColumnMeta columnMeta = new ColumnMeta(columnName, columnType, columnKey);
            tableMeta.getColumnMetas().add(columnMeta);
        }
        return tableMeta;
    }
}
