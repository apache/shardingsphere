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

package io.shardingsphere.transaction.xa;

import com.google.common.eventbus.AllowConcurrentEvents;
import com.google.common.eventbus.Subscribe;
import io.shardingsphere.core.transaction.event.WeakXaTransactionEvent;
import lombok.AllArgsConstructor;

import java.sql.SQLException;

/**
 * Weak-XA Transaction Listener.
 *
 * @author zhaojun
 */
@AllArgsConstructor
public class WeakXaTransactionListener {
    
    private WeakXaTransaction weakXaTransaction;
    
    /**
     * Weak-Xa Transaction Event Listener.
     *
     * @param weakXaTransactionEvent WeakXaTransactionEvent
     * @throws SQLException SQLException
     */
    @Subscribe
    @AllowConcurrentEvents
    public void listen(final WeakXaTransactionEvent weakXaTransactionEvent) throws SQLException {
        switch (weakXaTransactionEvent.getTclType()) {
            case BEGIN:
                weakXaTransaction.begin(weakXaTransactionEvent);
                break;
            case COMMIT:
                weakXaTransaction.commit(weakXaTransactionEvent);
                break;
            case ROLLBACK:
                weakXaTransaction.rollback(weakXaTransactionEvent);
                break;
            default:
        }
    }
}
