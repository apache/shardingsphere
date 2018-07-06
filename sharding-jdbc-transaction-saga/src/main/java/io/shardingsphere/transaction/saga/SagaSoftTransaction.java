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

package io.shardingsphere.transaction.saga;

import com.google.common.base.Preconditions;
import io.shardingsphere.core.jdbc.core.connection.ShardingConnection;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.UUID;

/**
 * Saga soft transaction.
 * 
 * @author zhangyonglun
 */
@Getter
@AllArgsConstructor
public abstract class SagaSoftTransaction {
    
    private boolean previousAutoCommit;
    
    private ShardingConnection connection;
    
    private String transactionId;
    
    /**
     * Begin transaction.
     *
     * @param connection connection
     * @throws SQLException SQL exception
     */
    public final void begin(final Connection connection) throws SQLException {
        Preconditions.checkArgument(connection instanceof ShardingConnection, "Only ShardingConnection can support Saga transaction.");
//        ExecutorExceptionHandler.setExceptionThrown(false);
        previousAutoCommit = connection.getAutoCommit();
        this.connection = (ShardingConnection) connection;
        this.connection.setAutoCommit(true);
        transactionId = UUID.randomUUID().toString();
    }
    
    /**
     * End transaction.
     * 
     * @throws SQLException SQL exception
     */
    public final void end() throws SQLException {
        if (null != connection) {
//            ExecutorExceptionHandler.setExceptionThrown(true);
            connection.setAutoCommit(previousAutoCommit);
        }
    }
}
