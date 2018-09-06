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

package io.shardingsphere.transaction.api;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import io.shardingsphere.core.event.ShardingEventBusInstance;
import io.shardingsphere.core.executor.sql.execute.threadlocal.ExecutorDataMap;
import io.shardingsphere.transaction.api.config.SoftTransactionConfiguration;
import io.shardingsphere.transaction.bed.BEDSoftTransaction;
import io.shardingsphere.transaction.bed.sync.BestEffortsDeliveryListener;
import io.shardingsphere.transaction.constants.SoftTransactionType;
import io.shardingsphere.transaction.constants.TransactionLogDataSourceType;
import io.shardingsphere.transaction.tcc.TCCSoftTransaction;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * B.A.S.E transaction manager.
 * 
 * @author zhangliang
 * @author caohao
 */
@RequiredArgsConstructor
public final class SoftTransactionManager {

    private static final String TRANSACTION = "transaction";

    private static final String TRANSACTION_CONFIG = "transactionConfig";

    @Getter
    private final SoftTransactionConfiguration transactionConfig;
    
    /**
     * Initialize B.A.S.E transaction manager.
     * 
     * @throws SQLException SQL exception
     */
    public void init() throws SQLException {
        ShardingEventBusInstance.getInstance().register(new BestEffortsDeliveryListener());
        if (TransactionLogDataSourceType.RDB == transactionConfig.getStorageType()) {
            Preconditions.checkNotNull(transactionConfig.getTransactionLogDataSource());
            createTable();
        }
    }
    
    private void createTable() throws SQLException {
        String dbSchema = "CREATE TABLE IF NOT EXISTS `transaction_log` ("
                + "`id` VARCHAR(40) NOT NULL, "
                + "`transaction_type` VARCHAR(30) NOT NULL, "
                + "`data_source` VARCHAR(255) NOT NULL, "
                + "`sql` TEXT NOT NULL, "
                + "`parameters` TEXT NOT NULL, "
                + "`creation_time` LONG NOT NULL, "
                + "`async_delivery_try_times` INT NOT NULL DEFAULT 0, "
                + "PRIMARY KEY (`id`));";
        try (
                Connection conn = transactionConfig.getTransactionLogDataSource().getConnection();
                PreparedStatement preparedStatement = conn.prepareStatement(dbSchema)) {
            preparedStatement.executeUpdate();
        }
    }
    
    /**
     * Get B.A.S.E transaction.
     * 
     * @param type transaction type
     * @return B.A.S.E transaction
     */
    public AbstractSoftTransaction getTransaction(final SoftTransactionType type) {
        AbstractSoftTransaction result;
        switch (type) {
            case BestEffortsDelivery: 
                result = new BEDSoftTransaction();
                break;
            case TryConfirmCancel:
                result = new TCCSoftTransaction();
                break;
            default: 
                throw new UnsupportedOperationException(type.toString());
        }
        // TODO don't support nested transaction, should configurable in future
        if (getCurrentTransaction().isPresent()) {
            throw new UnsupportedOperationException("Cannot support nested transaction.");
        }
        ExecutorDataMap.getDataMap().put(TRANSACTION, result);
        ExecutorDataMap.getDataMap().put(TRANSACTION_CONFIG, transactionConfig);
        return result;
    }
    
    /**
     * Get transaction configuration from current thread.
     * 
     * @return transaction configuration from current thread
     */
    public static Optional<SoftTransactionConfiguration> getCurrentTransactionConfiguration() {
        Object transactionConfig = ExecutorDataMap.getDataMap().get(TRANSACTION_CONFIG);
        return (null == transactionConfig)
                ? Optional.<SoftTransactionConfiguration>absent()
                : Optional.of((SoftTransactionConfiguration) transactionConfig);
    }
    
    /**
     * Get current transaction.
     * 
     * @return current transaction
     */
    public static Optional<AbstractSoftTransaction> getCurrentTransaction() {
        Object transaction = ExecutorDataMap.getDataMap().get(TRANSACTION);
        return (null == transaction)
                ? Optional.<AbstractSoftTransaction>absent()
                : Optional.of((AbstractSoftTransaction) transaction);
    }
    
    /**
     * Close transaction manager from current thread.
     */
    static void closeCurrentTransactionManager() {
        ExecutorDataMap.getDataMap().put(TRANSACTION, null);
        ExecutorDataMap.getDataMap().put(TRANSACTION_CONFIG, null);
    }
}
