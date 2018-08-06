package io.shardingsphere.transaction.innersaga.mock;

import io.shardingsphere.core.jdbc.core.datasource.ShardingDataSource;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * Saga transaction configuration
 *
 * @author yangyi
 */


@RequiredArgsConstructor
@Getter
@Setter
public class SagaTransactionConfiguration {

    /**
     * Data source for transaction manager.
     */
    @Getter(AccessLevel.NONE)
    private final DataSource targetDataSource;

    /**
     * Get connection for transaction manager.
     *
     * @param dataSourceName data source name for transaction manager
     * @return connection for transaction manager
     * @throws SQLException SQL exception
     */
    public Connection getTargetConnection(final String dataSourceName) throws SQLException {
        if (!(targetDataSource instanceof ShardingDataSource)) {
            return targetDataSource.getConnection();
        }
        return ((ShardingDataSource) targetDataSource).getConnection().getConnection(dataSourceName);
    }

}
