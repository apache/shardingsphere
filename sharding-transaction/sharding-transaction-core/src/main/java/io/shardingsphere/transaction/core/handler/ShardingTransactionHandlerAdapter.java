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

package io.shardingsphere.transaction.core.handler;

import io.shardingsphere.transaction.core.context.ShardingTransactionContext;
import io.shardingsphere.transaction.core.manager.ShardingTransactionManager;
import io.shardingsphere.transaction.spi.ShardingTransactionHandler;

/**
 * Abstract class for sharding transaction handler.
 *
 * @author zhaojun
 *
 * @param <T> type of sharding transaction context
 */
public abstract class ShardingTransactionHandlerAdapter<T extends ShardingTransactionContext> implements ShardingTransactionHandler<T> {
    
    @Override
    @SuppressWarnings("unchecked")
    public final void doInTransaction(final T context) {
        switch (context.getOperationType()) {
            case BEGIN:
                getShardingTransactionManager().begin(context);
                break;
            case COMMIT:
                getShardingTransactionManager().commit(context);
                break;
            case ROLLBACK:
                getShardingTransactionManager().rollback(context);
                break;
            default:
        }
    }
    
    protected abstract ShardingTransactionManager getShardingTransactionManager();
}
