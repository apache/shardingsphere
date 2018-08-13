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

import com.google.common.eventbus.AllowConcurrentEvents;
import com.google.common.eventbus.Subscribe;
import io.shardingsphere.core.constant.TransactionType;
import io.shardingsphere.core.util.EventBusInstance;
import io.shardingsphere.transaction.event.LocalTransactionEvent;
import io.shardingsphere.transaction.event.TransactionEvent;
import io.shardingsphere.transaction.event.XATransactionEvent;
import io.shardingsphere.transaction.manager.ShardingTransactionManager;
import io.shardingsphere.transaction.manager.ShardingTransactionManagerRegistry;

import java.sql.SQLException;

/**
 * Transaction Listener.
 *
 * @author zhaojun
 */
public final class TransactionListener {
    
    /**
     * Register transaction listener into event bus.
     */
    public void register() {
        EventBusInstance.getInstance().register(this);
    }
    
    /**
     * Listen event.
     *
     * @param transactionEvent transaction event
     * @throws SQLException SQL exception
     */
    @Subscribe
    @AllowConcurrentEvents
    public void listen(final TransactionEvent transactionEvent) throws SQLException {
        ShardingTransactionManager shardingTransactionManager = ShardingTransactionManagerRegistry.getInstance().getShardingTransactionManager(getTransactionType(transactionEvent));
        switch (transactionEvent.getTclType()) {
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
    
    private TransactionType getTransactionType(final TransactionEvent transactionEvent) {
        if (transactionEvent instanceof LocalTransactionEvent) {
            return TransactionType.LOCAL;
        }
        if (transactionEvent instanceof XATransactionEvent) {
            return TransactionType.XA;
        }
        throw new UnsupportedOperationException(transactionEvent.getClass().getName());
    }
}
