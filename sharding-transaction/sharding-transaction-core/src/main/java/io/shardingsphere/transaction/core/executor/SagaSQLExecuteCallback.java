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

package io.shardingsphere.transaction.core.executor;

import io.shardingsphere.core.constant.DatabaseType;
import io.shardingsphere.core.executor.StatementExecuteUnit;
import io.shardingsphere.core.executor.sql.execute.SQLExecuteCallback;
import io.shardingsphere.transaction.api.TransactionType;
import io.shardingsphere.transaction.core.context.SagaTransactionContext;
import io.shardingsphere.transaction.core.constant.ExecutionResult;
import io.shardingsphere.transaction.core.context.SagaSQLExecutionContext;
import io.shardingsphere.transaction.core.loader.ShardingTransactionHandlerRegistry;
import io.shardingsphere.transaction.spi.ShardingTransactionHandler;

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
        handler.doInTransaction(SagaTransactionContext
            .createExecutionSagaTransactionContext(new SagaSQLExecutionContext(null, logicSQLId, true)));
    }
    
    /**
     * Saga transaction execute sql immediately, and send result context to transaction handler.
     *
     * @param executeUnit execute unit
     * @return execute result
     * @throws SQLException sql exception
     */
    @Override
    protected T executeSQL(final StatementExecuteUnit executeUnit) throws SQLException {
        try {
            T result = executeResult(executeUnit);
            handler.doInTransaction(SagaTransactionContext.createExecutionSagaTransactionContext(new SagaSQLExecutionContext(executeUnit, logicSQLId, false, ExecutionResult.SUCCESS)));
            return result;
        } catch (SQLException ex) {
            handler.doInTransaction(SagaTransactionContext.createExecutionSagaTransactionContext(new SagaSQLExecutionContext(executeUnit, logicSQLId, false, ExecutionResult.FAILURE)));
            throw ex;
        }
    }
    
    protected abstract T executeResult(StatementExecuteUnit executeUnit) throws SQLException;
}
