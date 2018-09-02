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

package io.shardingsphere.transaction.listener;

import io.shardingsphere.core.event.ShardingEventBusInstance;
import io.shardingsphere.core.event.transaction.ShardingTransactionEvent;
import io.shardingsphere.transaction.manager.ShardingTransactionManager;

import java.sql.SQLException;

/**
 * Sharding transaction listener adapter.
 *
 * @author zhangliang
 * 
 * @param <T> transaction event type
 */
public abstract class ShardingTransactionListenerAdapter<T extends ShardingTransactionEvent> implements ShardingTransactionListener<T> {
    
    @Override
    public final void register() {
        ShardingEventBusInstance.getInstance().register(this);
    }
    
    @SuppressWarnings("unchecked")
    protected final void doTransaction(final ShardingTransactionManager shardingTransactionManager, final T transactionEvent) throws SQLException {
        switch (transactionEvent.getOperationType()) {
            case BEGIN:
                shardingTransactionManager.begin(transactionEvent);
                break;
            case COMMIT:
                shardingTransactionManager.commit(transactionEvent);
                break;
            case ROLLBACK:
                shardingTransactionManager.rollback(transactionEvent);
                break;
            default:
        }
    }
}
