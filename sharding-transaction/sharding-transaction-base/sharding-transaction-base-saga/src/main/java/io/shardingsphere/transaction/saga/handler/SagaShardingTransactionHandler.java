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

import io.shardingsphere.core.exception.ShardingException;
import io.shardingsphere.transaction.api.TransactionType;
import io.shardingsphere.transaction.core.context.SagaTransactionContext;
import io.shardingsphere.transaction.core.handler.ShardingTransactionHandlerAdapter;
import io.shardingsphere.transaction.core.manager.ShardingTransactionManager;
import io.shardingsphere.transaction.saga.manager.SagaTransactionManager;
import lombok.extern.slf4j.Slf4j;

import javax.transaction.Status;

/**
 * Saga transaction handler.
 *
 * @author yangyi
 */
@Slf4j
public final class SagaShardingTransactionHandler extends ShardingTransactionHandlerAdapter<SagaTransactionContext> {
    
    private final SagaTransactionManager transactionManager = SagaTransactionManager.getInstance();
    
    @Override
    public TransactionType getTransactionType() {
        return TransactionType.BASE;
    }
    
    @Override
    protected ShardingTransactionManager getShardingTransactionManager() {
        return transactionManager;
    }
    
    @Override
    public void doInTransaction(final SagaTransactionContext transactionContext) {
        switch (transactionContext.getOperationType()) {
            case BEGIN:
                if (!isInTransaction()) {
                    super.doInTransaction(transactionContext);
                }
                break;
            case COMMIT:
                if (!isInTransaction()) {
                    throw new ShardingException("No transaction begin in current thread connection");
                }
                super.doInTransaction(transactionContext);
                break;
            case ROLLBACK:
                super.doInTransaction(transactionContext);
                break;
            default:
        }
    }
    
    private boolean isInTransaction() {
        return Status.STATUS_ACTIVE == transactionManager.getStatus();
    }
}
