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

import io.shardingsphere.core.constant.transaction.TransactionType;
import io.shardingsphere.core.event.transaction.base.SagaTransactionEvent;
import io.shardingsphere.core.exception.ShardingException;
import io.shardingsphere.transaction.saga.manager.SagaTransactionManager;
import io.shardingsphere.transaction.handler.ShardingTransactionHandlerAdapter;
import io.shardingsphere.transaction.manager.ShardingTransactionManager;
import lombok.extern.slf4j.Slf4j;

import javax.transaction.Status;

/**
 * Saga transaction handler.
 *
 * @author yangyi
 */
@Slf4j
public final class SagaShardingTransactionHandler extends ShardingTransactionHandlerAdapter<SagaTransactionEvent> {
    
    private final SagaTransactionManager transactionManager = SagaTransactionManager.getInstance();
    
    private final SagaSQLExecutionEventHandler sagaSQLExecutionEventHandler = new SagaSQLExecutionEventHandler();
    
    @Override
    public TransactionType getTransactionType() {
        return TransactionType.BASE;
    }
    
    @Override
    protected ShardingTransactionManager getShardingTransactionManager() {
        return transactionManager;
    }
    
    @Override
    public void doInTransaction(final SagaTransactionEvent transactionEvent) {
        if (transactionEvent.isDestroyComponent()) {
            transactionManager.removeSagaExecutionComponent(transactionEvent.getSagaConfiguration());
            return;
        }
        if (transactionEvent.isExecutionEvent()) {
            sagaSQLExecutionEventHandler.handleSQLExecutionEvent(transactionEvent.getSagaSQLExecutionEvent());
            return;
        }
        switch (transactionEvent.getOperationType()) {
            case BEGIN:
                if (Status.STATUS_NO_TRANSACTION == transactionManager.getStatus()) {
                    super.doInTransaction(transactionEvent);
                }
                break;
            case COMMIT:
                if (Status.STATUS_ACTIVE != transactionManager.getStatus()) {
                    throw new ShardingException("No transaction begin in current thread connection");
                }
                sagaSQLExecutionEventHandler.clean();
                super.doInTransaction(transactionEvent);
                break;
            case ROLLBACK:
                sagaSQLExecutionEventHandler.clean();
                super.doInTransaction(transactionEvent);
                break;
            default:
        }
    }
}
