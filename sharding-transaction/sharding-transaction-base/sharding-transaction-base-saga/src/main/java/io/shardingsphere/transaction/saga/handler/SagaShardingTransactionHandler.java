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
import io.shardingsphere.transaction.core.TransactionOperationType;
import io.shardingsphere.transaction.core.handler.ShardingTransactionHandlerAdapter;
import io.shardingsphere.transaction.core.manager.ShardingTransactionManager;
import io.shardingsphere.transaction.saga.manager.SagaTransactionManager;
import lombok.extern.slf4j.Slf4j;

import javax.sql.DataSource;
import java.util.Map;

/**
 * Saga transaction handler.
 *
 * @author yangyi
 */
@Slf4j
public final class SagaShardingTransactionHandler extends ShardingTransactionHandlerAdapter {
    
    private final SagaTransactionManager transactionManager = SagaTransactionManager.getInstance();
    
    @Override
    public void doInTransaction(final TransactionOperationType transactionOperationType) {
        switch (transactionOperationType) {
            case CLOSE:
                transactionManager.cleanTransaction();
                break;
            default:
                super.doInTransaction(transactionOperationType);
        }
    }
    
    @Override
    public ShardingTransactionManager getShardingTransactionManager() {
        return transactionManager;
    }
    
    @Override
    public TransactionType getTransactionType() {
        return TransactionType.BASE;
    }
    
    @Override
    public void registerTransactionalResource(final DatabaseType databaseType, final Map<String, DataSource> dataSourceMap) {
        transactionManager.getResourceManager().registerDataSourceMap(dataSourceMap);
    }
    
    @Override
    public void clearTransactionalResource(final Map<String, DataSource> dataSourceMap) {
        transactionManager.getResourceManager().releaseDataSourceMap(dataSourceMap);
    }
}
