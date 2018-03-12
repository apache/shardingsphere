package io.shardingjdbc.server;

import io.shardingjdbc.core.api.ShardingDataSourceFactory;
import io.shardingjdbc.core.exception.ShardingJdbcException;
import io.shardingjdbc.server.packet.command.ComQueryPacket;
import lombok.Getter;

import javax.sql.DataSource;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;

/**
 * Data source manager.
 *
 * @author zhangliang
 */
@Getter
public final class DataSourceManager {
    
    @Getter
    private static DataSourceManager instance = new DataSourceManager();
    
    private final DataSource dataSource;
    
    public DataSourceManager() {
        try {
            dataSource = ShardingDataSourceFactory.createDataSource(new File(ComQueryPacket.class.getResource("/conf/sharding-config.yaml").getFile()));
        } catch (final IOException | SQLException ex) {
            throw new ShardingJdbcException(ex);
        }
    }
}
