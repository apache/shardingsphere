package io.shardingsphere.transaction.innersaga.api;

import com.google.common.base.Optional;
import io.shardingsphere.core.executor.threadlocal.ExecutorDataMap;
import io.shardingsphere.core.util.EventBusInstance;
import io.shardingsphere.transaction.innersaga.api.config.SagaSoftTransactionConfiguration;
import io.shardingsphere.transaction.innersaga.mock.MockSagaTransactionManager;
import io.shardingsphere.transaction.innersaga.sync.SagaListener;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Saga Soft transaction manager
 *
 * @author yangyi
 */

@RequiredArgsConstructor
public class SagaSoftTransactionManager {

    private static final String TRANSACTION = "transaction";

    private static final String TRANSACTION_CONFIG = "transactionConfig";

    @Getter
    private final SagaSoftTransactionConfiguration transactionConfig;

    private final MockSagaTransactionManager sagaTransactionManager = new MockSagaTransactionManager();

    /**
     * Initialize Saga soft transaction manager.
     */
    public void init() {
        EventBusInstance.getInstance().register(new SagaListener());
        sagaTransactionManager.setSagaTransactionConfiguration(transactionConfig);
    }

    public SagaSoftTransaction getTransaction() {
        if (getCurrentTransaction().isPresent()) {
            throw new UnsupportedOperationException("Cannot support nested transaction.");
        }
        SagaSoftTransaction result = new SagaSoftTransaction(sagaTransactionManager.getTransaction());
        ExecutorDataMap.getDataMap().put(TRANSACTION, result);
        ExecutorDataMap.getDataMap().put(TRANSACTION_CONFIG, transactionConfig);
        return result;
    }

    /**
     * Get transaction configuration from current thread.
     *
     * @return transaction configuration from current thread
     */
    public static Optional<SagaSoftTransactionConfiguration> getCurrentTransactionConfiguration() {
        Object transactionConfig = ExecutorDataMap.getDataMap().get(TRANSACTION_CONFIG);
        return (null == transactionConfig)
                ? Optional.<SagaSoftTransactionConfiguration>absent()
                : Optional.of((SagaSoftTransactionConfiguration) transactionConfig);
    }

    /**
     * Get current transaction.
     *
     * @return current transaction
     */
    public static Optional<SagaSoftTransaction> getCurrentTransaction() {
        Object transaction = ExecutorDataMap.getDataMap().get(TRANSACTION);
        return (null == transaction)
                ? Optional.<SagaSoftTransaction>absent()
                : Optional.of((SagaSoftTransaction) transaction);
    }

    /**
     * Close transaction manager from current thread.
     */
    static void closeCurrentTransactionManager() {
        ExecutorDataMap.getDataMap().put(TRANSACTION, null);
        ExecutorDataMap.getDataMap().put(TRANSACTION_CONFIG, null);
    }

}
