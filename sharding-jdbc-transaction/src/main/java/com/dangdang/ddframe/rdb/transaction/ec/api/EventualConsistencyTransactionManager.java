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

package com.dangdang.ddframe.rdb.transaction.ec.api;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.UUID;

import com.dangdang.ddframe.rdb.sharding.executor.ExecutorExceptionHandler;
import com.dangdang.ddframe.rdb.sharding.executor.event.DMLExecutionEventBus;
import com.dangdang.ddframe.rdb.sharding.jdbc.ShardingConnection;
import com.dangdang.ddframe.rdb.transaction.ec.bec.BestEffortsCompensationListener;
import com.dangdang.ddframe.rdb.transaction.ec.config.TransactionConfiguration;
import com.google.common.base.Preconditions;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 柔性事务管理器.
 * 
 * @author zhangliang
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class EventualConsistencyTransactionManager {
    
    static {
        DMLExecutionEventBus.register(new BestEffortsCompensationListener());
    }
    
    @Getter
    private static TransactionConfiguration transactionConfiguration = new TransactionConfiguration();
    
    private static ThreadLocal<ShardingConnection> connection = new ThreadLocal<>();
    
    private static ThreadLocal<Boolean> previousAutoCommit = new ThreadLocal<>();
    
    private static ThreadLocal<EventualConsistencyTransactionType> transactionType = new ThreadLocal<>();
    
    private static ThreadLocal<String> transactionId = new ThreadLocal<>();
    
    /**
     * 开启柔性事务.
     * 
     * @param connection 数据库连接对象
     * @param type 柔性事务类型
     */
    public static void begin(final Connection connection, final EventualConsistencyTransactionType type) throws SQLException {
        // TODO 判断如果在传统事务中，则抛异常
        Preconditions.checkArgument(connection instanceof ShardingConnection, "Only ShardingConnection can support eventual consistency transaction.");
        // TODO 目前使用不支持嵌套事务，以后这里需要可配置
        if (null != transactionType.get() || null != transactionId.get()) {
            throw new UnsupportedOperationException("Cannot support nested transaction.");
        }
        ExecutorExceptionHandler.setExceptionThrown(false);
        EventualConsistencyTransactionManager.connection.set((ShardingConnection) connection);
        previousAutoCommit.set(connection.getAutoCommit());
        connection.setAutoCommit(true);
        transactionType.set(type);
        // TODO 替换UUID为更有效率的id生成器
        transactionId.set(UUID.randomUUID().toString());
    }
    
    /**
     * 结束柔性事务.
     * 
     * @param connection 数据库连接对象
     */
    public static void end() throws SQLException {
        ExecutorExceptionHandler.setExceptionThrown(true);
        transactionType.remove();
        transactionId.remove();
        connection.get().setAutoCommit(previousAutoCommit.get());
        previousAutoCommit.remove();
        EventualConsistencyTransactionManager.connection.remove();
    }
    
    /**
     * 获取被事务管理的数据库连接.
     * 
     * @return 被事务管理的数据库连接
     */
    public static ShardingConnection getConnection() {
        return connection.get();
    }
    
    /**
     * 获取柔性事务类型.
     * 
     * @return 柔性事务类型
     */
    public static EventualConsistencyTransactionType getTransactionType() {
        return transactionType.get();
    }
    
    /**
     * 获取事务主键.
     * 
     * @return 事务主键
     */
    public static String getTransactionId() {
        return transactionId.get();
    }
}
