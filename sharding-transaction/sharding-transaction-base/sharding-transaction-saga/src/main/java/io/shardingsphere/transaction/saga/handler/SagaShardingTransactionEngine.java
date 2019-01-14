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

package io.shardingsphere.transaction.saga.handler;

import io.shardingsphere.core.constant.DatabaseType;
import io.shardingsphere.transaction.api.TransactionType;
import io.shardingsphere.transaction.saga.manager.SagaTransactionManager;
import io.shardingsphere.transaction.spi.ShardingTransactionEngine;
import lombok.extern.slf4j.Slf4j;

import javax.sql.DataSource;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;

/**
 * Saga transaction handler.
 *
 * @author yangyi
 */
@Slf4j
public final class SagaShardingTransactionEngine implements ShardingTransactionEngine {
    
    private final SagaTransactionManager transactionManager = SagaTransactionManager.getInstance();
    
    @Override
    public TransactionType getTransactionType() {
        return TransactionType.BASE;
    }
    
    @Override
    public void registerTransactionalResource(final DatabaseType databaseType, final Map<String, DataSource> dataSourceMap) {
        transactionManager.getResourceManager().registerDataSourceMap(dataSourceMap);
    }
    
    @Override
    public void clearTransactionalResources() {
        transactionManager.getResourceManager().releaseDataSourceMap();
    }
    
    @Override
    public Connection createConnection(final String dataSourceName, final DataSource dataSource) throws SQLException {
        Connection result = dataSource.getConnection();
        if (null != transactionManager.getTransaction() && !transactionManager.getTransaction().getConnectionMap().containsKey(dataSourceName)) {
            transactionManager.getTransaction().getConnectionMap().put(dataSourceName, result);
        }
        return result;
    }
    
    @Override
    public void begin() {
        transactionManager.begin();
    }
    
    @Override
    public void commit() {
        transactionManager.commit();
    }
    
    @Override
    public void rollback() {
        transactionManager.rollback();
    }
}
