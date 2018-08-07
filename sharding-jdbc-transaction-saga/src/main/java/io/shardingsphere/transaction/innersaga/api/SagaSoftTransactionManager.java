/*
 * Copyright 2016-2018 shardingsphere.io.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

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
 * Saga Soft transaction manager.
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
    
    /**
     * get a new transaction for current thread.
     *
     * @return new transaction
     */
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
