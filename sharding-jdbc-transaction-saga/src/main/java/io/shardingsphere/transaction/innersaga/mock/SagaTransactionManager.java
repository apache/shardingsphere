package io.shardingsphere.transaction.innersaga.mock;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Actual saga transaction manager
 * need service comb implement
 *
 * @author yangyi
 */

public interface SagaTransactionManager {

    /**
     * create a new saga transaction for current thread
     *
     * @return new transaction
     */
    SagaTransaction getTransaction();

    /**
     * Get connection for transaction manager.
     *
     * @param dataSourceName data source name for transaction manager
     * @return connection for transaction manager
     * @throws SQLException SQL exception
     */
    Connection getTargetConnection(final String dataSourceName) throws SQLException;

}
