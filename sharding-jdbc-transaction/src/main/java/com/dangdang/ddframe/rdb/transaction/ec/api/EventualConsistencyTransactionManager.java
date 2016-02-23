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
import com.dangdang.ddframe.rdb.sharding.jdbc.ShardingConnection;
import com.google.common.base.Preconditions;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 柔性事务管理器.
 * 
 * @author zhangliang
 */
@RequiredArgsConstructor
public final class EventualConsistencyTransactionManager {
    
    private boolean previousAutoCommit;
    
    @Getter
    private ShardingConnection connection;
    
    @Getter
    private EventualConsistencyTransactionType transactionType;
    
    @Getter
    private String transactionId;
    
    /**
     * 开启柔性事务.
     * 
     * @param connection 数据库连接对象
     * @param type 柔性事务类型
     */
    public void begin(final Connection connection, final EventualConsistencyTransactionType type) throws SQLException {
        // TODO 判断如果在传统事务中，则抛异常
        Preconditions.checkArgument(connection instanceof ShardingConnection, "Only ShardingConnection can support eventual consistency transaction.");
        ExecutorExceptionHandler.setExceptionThrown(false);
        this.connection = (ShardingConnection) connection;
        transactionType = type;
        previousAutoCommit = connection.getAutoCommit();
        connection.setAutoCommit(true);
        // TODO 替换UUID为更有效率的id生成器
        transactionId = UUID.randomUUID().toString();
    }
    
    /**
     * 结束柔性事务.
     * 
     * @param connection 数据库连接对象
     */
    public void end() throws SQLException {
        ExecutorExceptionHandler.setExceptionThrown(true);
        connection.setAutoCommit(previousAutoCommit);
        EventualConsistencyTransactionManagerFactory.closeCurrentTransactionManager();
    }
}
