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

package io.shardingsphere.shardingproxy.transport.mysql.packet.command.query.text.query;

import io.shardingsphere.core.constant.DatabaseType;
import io.shardingsphere.transaction.api.TransactionType;
import io.shardingsphere.transaction.core.TransactionOperationType;
import io.shardingsphere.transaction.spi.ShardingTransactionHandler;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Fixed base sharding transaction handler.
 *
 * @author zhaojun
 */
public final class FixedXAShardingTransactionHandler implements ShardingTransactionHandler {
    
    private static final Map<String, TransactionOperationType> INVOKES = new HashMap<>();
    
    /**
     * Get invoke map.
     *
     * @return map
     */
    static Map<String, TransactionOperationType> getInvokes() {
        return INVOKES;
    }
    
    @Override
    public void doInTransaction(final TransactionOperationType transactionOperationType) {
        switch (transactionOperationType) {
            case BEGIN:
                INVOKES.put("begin", transactionOperationType);
                return;
            case COMMIT:
                INVOKES.put("commit", transactionOperationType);
                return;
            case ROLLBACK:
                INVOKES.put("rollback", transactionOperationType);
                return;
            default:
        }
    }
    
    @Override
    public TransactionType getTransactionType() {
        return TransactionType.XA;
    }
    
    @Override
    public void registerTransactionDataSource(final DatabaseType databaseType, final Map<String, DataSource> dataSourceMap) {
    
    }
    
    @Override
    public void synchronizeTransactionResource(final String datasourceName, final List<Connection> connections, final Object... properties) {
    
    }
}
