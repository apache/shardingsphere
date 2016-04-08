/**
 * Copyright 1999-2015 dangdang.com.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package com.dangdang.ddframe.rdb.transaction.soft.api;

import com.dangdang.ddframe.rdb.sharding.executor.event.DMLExecutionEventBus;
import com.dangdang.ddframe.rdb.transaction.soft.api.config.SoftTransactionConfiguration;
import com.dangdang.ddframe.rdb.transaction.soft.bed.BEDSoftTransactionManager;
import com.dangdang.ddframe.rdb.transaction.soft.bed.async.NestedBestEffortsDeliveryJobFactory;
import com.dangdang.ddframe.rdb.transaction.soft.bed.sync.BestEffortsDeliveryListener;
import com.dangdang.ddframe.rdb.transaction.soft.storage.TransactionLogStorageType;
import com.dangdang.ddframe.rdb.transaction.soft.tcc.TCCSoftTransactionManager;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * 柔性事务管理器工厂.
 * 
 * @author zhangliang
 */
@RequiredArgsConstructor
public final class SoftTransactionManagerFactory {
    
    private static ThreadLocal<AbstractSoftTransactionManager> currentTransactionManager = new ThreadLocal<>();
    
    private static ThreadLocal<SoftTransactionConfiguration> currentTransactionConfig = new ThreadLocal<>();
    
    @Getter
    private final SoftTransactionConfiguration transactionConfig;
    
    /**
     * 初始化事务管理器工厂.
     */
    public void init() throws SQLException {
        DMLExecutionEventBus.register(new BestEffortsDeliveryListener());
        if (TransactionLogStorageType.DATABASE == transactionConfig.getStorageType()) {
            Preconditions.checkNotNull(transactionConfig.getTransactionLogDataSource());
            createTable();
        }
        if (transactionConfig.isNestedJob()) {
            new NestedBestEffortsDeliveryJobFactory(transactionConfig).init();
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
     * 获取柔性事务管理器.
     * 
     * @param type 柔性事务类型
     * @return 柔性事务管理器
     */
    public AbstractSoftTransactionManager getTransactionManager(final SoftTransactionType type) {
        AbstractSoftTransactionManager result;
        switch (type) {
            case BestEffortsDelivery: 
                result = new BEDSoftTransactionManager();
                break;
            case TryConfirmCancel:
                result = new TCCSoftTransactionManager();
                break;
            default: 
                throw new UnsupportedOperationException(type.toString());
        }
        // TODO 目前使用不支持嵌套事务，以后这里需要可配置
        if (getCurrentTransactionManager().isPresent()) {
            throw new UnsupportedOperationException("Cannot support nested transaction.");
        }
        currentTransactionManager.set(result);
        currentTransactionConfig.set(transactionConfig);
        return result;
    }
    
    /**
     * 获取当前线程的柔性事务管理器配置.
     * 
     * @return 当前线程的柔性事务管理器配置
     */
    public static Optional<SoftTransactionConfiguration> getCurrentTransactionConfiguration() {
        return Optional.fromNullable(currentTransactionConfig.get());
    }
    
    /**
     * 获取当前的柔性事务管理器.
     * 
     * @return 当前的柔性事务管理器
     */
    public static Optional<AbstractSoftTransactionManager> getCurrentTransactionManager() {
        return Optional.fromNullable(currentTransactionManager.get());
    }
    
    /**
     * 关闭当前的柔性事务管理器.
     */
    public static void closeCurrentTransactionManager() {
        currentTransactionManager.remove();
        currentTransactionConfig.remove();
    }
}
