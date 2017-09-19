/*
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

package io.shardingjdbc.transaction.api;

import io.shardingjdbc.core.executor.threadlocal.ExecutorExceptionHandler;
import io.shardingjdbc.core.jdbc.core.connection.ShardingConnection;
import io.shardingjdbc.transaction.constants.SoftTransactionType;
import com.google.common.base.Preconditions;
import lombok.Getter;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.UUID;

/**
 * B.A.S.E transaction abstract class.
 * 
 * @author zhangliang 
 */
public abstract class AbstractSoftTransaction {
    
    private boolean previousAutoCommit;
    
    @Getter
    private ShardingConnection connection;
    
    @Getter
    private SoftTransactionType transactionType;
    
    @Getter
    private String transactionId;
    
    protected final void beginInternal(final Connection conn, final SoftTransactionType type) throws SQLException {
        // TODO if in traditional transaction, then throw exception
        Preconditions.checkArgument(conn instanceof ShardingConnection, "Only ShardingConnection can support eventual consistency transaction.");
        ExecutorExceptionHandler.setExceptionThrown(false);
        connection = (ShardingConnection) conn;
        transactionType = type;
        previousAutoCommit = connection.getAutoCommit();
        connection.setAutoCommit(true);
        // TODO replace to snowflake
        transactionId = UUID.randomUUID().toString();
    }
    
    /**
     * End transaction.
     * 
     * @throws SQLException SQL exception
     */
    public final void end() throws SQLException {
        if (connection != null) {
            ExecutorExceptionHandler.setExceptionThrown(true);
            connection.setAutoCommit(previousAutoCommit);
            SoftTransactionManager.closeCurrentTransactionManager();
        }
    }
}
