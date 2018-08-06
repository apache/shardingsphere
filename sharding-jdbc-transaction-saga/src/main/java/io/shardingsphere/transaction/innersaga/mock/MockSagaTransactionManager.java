package io.shardingsphere.transaction.innersaga.mock;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * Saga transaction mock implement
 *
 * @author yangyi
 */

@NoArgsConstructor
public class MockSagaTransactionManager implements SagaTransactionManager {

    @Getter
    @Setter
    private SagaTransactionConfiguration sagaTransactionConfiguration;

    @Override
    public SagaTransaction getTransaction() {
        return new MockSagaTransaction(this);
    }

    @Override
    public Connection getTargetConnection(String dataSourceName) throws SQLException {
        return sagaTransactionConfiguration.getTargetConnection(dataSourceName);
    }
}
