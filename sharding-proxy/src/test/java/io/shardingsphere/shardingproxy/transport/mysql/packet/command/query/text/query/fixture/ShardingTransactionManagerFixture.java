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

package io.shardingsphere.shardingproxy.transport.mysql.packet.command.query.text.query.fixture;

import io.shardingsphere.core.constant.DatabaseType;
import io.shardingsphere.transaction.api.TransactionType;
import io.shardingsphere.transaction.core.TransactionOperationType;
import io.shardingsphere.transaction.spi.ShardingTransactionManager;
import lombok.Getter;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;

public final class ShardingTransactionManagerFixture implements ShardingTransactionManager {
    
    @Getter
    private static Collection<TransactionOperationType> invocations = new LinkedList<>();
    
    @Override
    public void init(final DatabaseType databaseType, final Map<String, DataSource> dataSourceMap) {
    }
    
    @Override
    public TransactionType getTransactionType() {
        return TransactionType.XA;
    }
    
    @Override
    public boolean isInTransaction() {
        return true;
    }
    
    @Override
    public Connection getConnection(final String dataSourceName) {
        return null;
    }
    
    @Override
    public void begin() {
        invocations.add(TransactionOperationType.BEGIN);
    }
    
    @Override
    public void commit() {
        invocations.add(TransactionOperationType.COMMIT);
    }
    
    @Override
    public void rollback() {
        invocations.add(TransactionOperationType.ROLLBACK);
    }
    
    @Override
    public void close() {
    }
}
