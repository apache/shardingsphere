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

package io.shardingsphere.transaction.handler;

import io.shardingsphere.core.event.transaction.ShardingTransactionEvent;
import io.shardingsphere.spi.transaction.ShardingTransactionHandler;
import io.shardingsphere.transaction.manager.ShardingTransactionManager;

/**
 * Abstract class for sharding transaction handler.
 *
 * @author zhaojun
 */
public abstract class ShardingTransactionHandlerAdapter<T extends ShardingTransactionEvent> implements ShardingTransactionHandler<T> {
    
    @Override
    @SuppressWarnings("unchecked")
    public final void doInTransaction(final T event) {
        switch (event.getOperationType()) {
            case BEGIN:
                getShardingTransactionManager().begin(event);
                break;
            case COMMIT:
                getShardingTransactionManager().commit(event);
                break;
            case ROLLBACK:
                getShardingTransactionManager().rollback(event);
                break;
            default:
        }
    }
    
    /**
     * Get sharding transaction manager.
     *
     * @return sharding transaction manager
     */
    protected abstract ShardingTransactionManager getShardingTransactionManager();
}
