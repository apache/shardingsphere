package io.shardingjdbc.core.jdbc.metadata.handler;

import io.shardingjdbc.core.jdbc.core.datasource.MasterSlaveDataSource;
import io.shardingjdbc.core.jdbc.metadata.entity.TableMeta;
import lombok.Getter;
import javax.sql.DataSource;
import java.sql.*;

/**
 * The abstract table structure handler.
 *
 * @author panjuan
 */
@Getter
public abstract class AbstractTableMetaHandler {
    
    protected final DataSource dataSource;
    
    protected final String actualTableName;
    
    public AbstractTableMetaHandler(final DataSource dataSource, final String actualTableName) {
        
        this.dataSource = dataSource instanceof MasterSlaveDataSource
            ? ((MasterSlaveDataSource) dataSource).getMasterDataSource().values().iterator().next() : dataSource;
        this.actualTableName = actualTableName;
    }
    
    /**
     * To get actual table meta.
     *
     * @return Table meta.
     * @throws SQLException SQL exception.
     */
    public abstract TableMeta getActualTableMeta() throws SQLException;
}
