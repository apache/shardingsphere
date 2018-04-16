package io.shardingjdbc.core.jdbc.meta.handler;

import io.shardingjdbc.core.constant.DatabaseType;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import javax.sql.DataSource;
import java.sql.SQLException;

/**
 * The factory of table meta handler.
 *
 * @author panjuan
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class TableMetaHandlerFactory {
    
    /**
     * To generate table meta handler by data type.
     *
     * @param dataSource data source.
     * @param actualTableName actual table name.
     * @return abstract table meta handler.
     * @throws SQLException SQL exception.
     */
    public static AbstractTableMetaHandler newInstance(final DataSource dataSource, final String actualTableName) throws SQLException {
        DatabaseType databaseType = DatabaseType.valueFrom(dataSource.getConnection().getMetaData().getDatabaseProductName());
        switch (databaseType) {
            case MySQL:
                return new MySQLTableMetaHandler(dataSource, actualTableName);
            default:
                throw new UnsupportedOperationException(String.format("Cannot support database [%s].", databaseType));
        }
    }
    
}
