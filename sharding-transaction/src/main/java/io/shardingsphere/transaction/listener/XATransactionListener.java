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
import io.shardingsphere.transaction.event.XATransactionEvent;
import io.shardingsphere.transaction.manager.ShardingTransactionManager;
import io.shardingsphere.transaction.manager.ShardingTransactionManagerRegistry;

import java.sql.SQLException;

/**
 * XA transaction listener.
 *
 * @author zhangliang
 */
public final class XATransactionListener {
    
    /**
     * Register transaction listener into event bus.
     */
    public void register() {
        EventBusInstance.getInstance().register(this);
    }
    
    /**
     * Listen event.
     *
     * @param xaTransactionEvent XA transaction event
     * @throws SQLException SQL exception
     */
    @Subscribe
    @AllowConcurrentEvents
    public void listen(final XATransactionEvent xaTransactionEvent) throws SQLException {
        ShardingTransactionManager shardingTransactionManager = ShardingTransactionManagerRegistry.getInstance().getShardingTransactionManager(TransactionType.XA);
        switch (xaTransactionEvent.getTclType()) {
            case BEGIN:
                shardingTransactionManager.begin(xaTransactionEvent);
                break;
            case COMMIT:
                shardingTransactionManager.commit(xaTransactionEvent);
                break;
            case ROLLBACK:
                shardingTransactionManager.rollback(xaTransactionEvent);
                break;
            default:
        }
    }
}
