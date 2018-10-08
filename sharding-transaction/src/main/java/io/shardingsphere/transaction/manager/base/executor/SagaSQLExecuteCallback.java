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

package io.shardingsphere.transaction.manager.base.executor;

import com.google.common.eventbus.EventBus;
import io.shardingsphere.core.constant.DatabaseType;
import io.shardingsphere.core.constant.SQLType;
import io.shardingsphere.core.constant.transaction.TransactionType;
import io.shardingsphere.core.event.ShardingEventBusInstance;
import io.shardingsphere.core.event.transaction.base.SagaSQLExecutionEvent;
import io.shardingsphere.core.executor.StatementExecuteUnit;
import io.shardingsphere.core.executor.sql.execute.SQLExecuteCallback;
import io.shardingsphere.transaction.manager.ShardingTransactionManagerRegistry;
import io.shardingsphere.transaction.manager.base.BASETransactionManager;

import java.sql.SQLException;
import java.util.Map;

/**
 * Saga transaction sql exeucte callback.
 *
 * @author yangyi
 */
public abstract class SagaSQLExecuteCallback<T> extends SQLExecuteCallback<T> {
    
    private final String transactionId;
    
    private final EventBus shardingEventBus = ShardingEventBusInstance.getInstance();
    
    public SagaSQLExecuteCallback(final DatabaseType databaseType, final SQLType sqlType, final boolean isExceptionThrown) {
        super(databaseType, sqlType, isExceptionThrown);
        this.transactionId = ((BASETransactionManager) ShardingTransactionManagerRegistry.getInstance().getShardingTransactionManager(TransactionType.BASE)).getTransactionId();
        shardingEventBus.post(new SagaSQLExecutionEvent(null, transactionId));
    }
    
    /**
     * Saga transaction don't execute sql immediately, but send event to listener.
     *
     * @param executeUnit exeucte unit
     * @return return false if T is Boolean, 0 if T is Integer
     * @throws SQLException sql exception
     */
    @Override
    protected T executeSQL(final StatementExecuteUnit executeUnit) throws SQLException {
        SagaSQLExecutionEvent event = new SagaSQLExecutionEvent(executeUnit.getRouteUnit(), transactionId);
        event.setExecuteSuccess();
        shardingEventBus.post(event);
        return executeResult();
    }
    
    protected abstract T executeResult();
}
