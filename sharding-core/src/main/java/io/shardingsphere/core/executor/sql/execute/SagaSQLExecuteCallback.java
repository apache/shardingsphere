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

package io.shardingsphere.core.executor.sql.execute;

import io.shardingsphere.core.constant.DatabaseType;
import io.shardingsphere.core.constant.transaction.TransactionType;
import io.shardingsphere.core.event.transaction.base.SagaSQLExecutionEvent;
import io.shardingsphere.core.event.transaction.base.SagaTransactionEvent;
import io.shardingsphere.core.executor.StatementExecuteUnit;
import io.shardingsphere.spi.transaction.ShardingTransactionHandler;
import io.shardingsphere.spi.transaction.ShardingTransactionHandlerRegistry;

import java.sql.SQLException;
import java.util.UUID;

/**
 * Saga transaction sql execute callback.
 *
 * @author yangyi
 */
public abstract class SagaSQLExecuteCallback<T> extends SQLExecuteCallback<T> {
    
    private final String logicSQLId = UUID.randomUUID().toString();
    
    private final ShardingTransactionHandler handler;
    
    public SagaSQLExecuteCallback(final DatabaseType databaseType, final boolean isExceptionThrown) {
        super(databaseType, isExceptionThrown);
        this.handler = ShardingTransactionHandlerRegistry.getInstance().getHandler(TransactionType.BASE);
        handler.doInTransaction(SagaTransactionEvent.createExecutionSagaTransactionEvent(new SagaSQLExecutionEvent(null, logicSQLId, true)));
    }
    
    /**
     * Saga transaction don't execute sql immediately, but send event to handler.
     *
     * @param executeUnit execute unit
     * @return return false if T is Boolean, 0 if T is Integer
     * @throws SQLException sql exception
     */
    @Override
    protected T executeSQL(final StatementExecuteUnit executeUnit) throws SQLException {
        handler.doInTransaction(SagaTransactionEvent.createExecutionSagaTransactionEvent(new SagaSQLExecutionEvent(executeUnit.getRouteUnit(), logicSQLId, false)));
        return executeResult();
    }
    
    protected abstract T executeResult();
}
