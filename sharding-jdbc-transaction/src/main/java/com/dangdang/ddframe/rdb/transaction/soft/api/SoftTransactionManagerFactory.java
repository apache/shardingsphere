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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import com.dangdang.ddframe.rdb.sharding.executor.event.DMLExecutionEventBus;
import com.dangdang.ddframe.rdb.transaction.soft.bed.BestEffortsDeliveryListener;
import com.dangdang.ddframe.rdb.transaction.soft.storage.TransactionLogStroageType;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 柔性事务管理器工厂.
 * 
 * @author zhangliang
 */
@RequiredArgsConstructor
public final class SoftTransactionManagerFactory {
    
    private static ThreadLocal<SoftTransactionManager> currentTransactionManager = new ThreadLocal<>();
    
    private static ThreadLocal<SoftTransactionConfiguration> currentTransactionConfig = new ThreadLocal<>();
    
    @Getter
    private final SoftTransactionConfiguration transactionConfig;
    
    /**
     * 初始化事务管理器工厂.
     */
    public void init() throws SQLException {
        DMLExecutionEventBus.register(new BestEffortsDeliveryListener());
        if (TransactionLogStroageType.DATABASE == transactionConfig.getStroageType()) {
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
                + "PRIMARY KEY (`id`));";
        try (
                Connection conn = transactionConfig.getTransactionLogDataSource().getConnection();
                PreparedStatement psmt = conn.prepareStatement(dbSchema)) {
            psmt.executeUpdate();
        }
    }
    
    /**
     * 获取柔性事务管理器.
     * 
     * @return 柔性事务管理器
     */
    public SoftTransactionManager getTransactionManager() {
        SoftTransactionManager result = new SoftTransactionManager();
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
    public static Optional<SoftTransactionManager> getCurrentTransactionManager() {
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
