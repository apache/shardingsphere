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

package io.shardingsphere.core.transaction.listener;

import com.google.common.eventbus.AllowConcurrentEvents;
import com.google.common.eventbus.Subscribe;
import io.shardingsphere.core.transaction.TransactionContextHolder;
import io.shardingsphere.core.transaction.event.TransactionEvent;
import io.shardingsphere.core.transaction.spi.TransactionManager;
import lombok.RequiredArgsConstructor;

/**
 * Transaction Listener.
 *
 * @author zhaojun
 */
public class TransactionListener {
    
    /**
     * Listen event.
     *
     * @param transactionEvent transaction event
     * @throws Exception exception
     */
    @Subscribe
    @AllowConcurrentEvents
    public void listen(final TransactionEvent transactionEvent) throws Exception {
        TransactionManager transactionManager = TransactionContextHolder.get().getTransactionManager();
        switch (transactionEvent.getTclType()) {
            case BEGIN:
                transactionManager.begin(transactionEvent);
                break;
            case COMMIT:
                transactionManager.commit(transactionEvent);
                break;
            case ROLLBACK:
                transactionManager.rollback(transactionEvent);
                break;
            default:
        }
    }
}
