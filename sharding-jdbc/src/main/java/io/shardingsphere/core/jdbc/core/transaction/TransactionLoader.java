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

package io.shardingsphere.core.jdbc.core.transaction;

import io.shardingsphere.core.constant.TransactionType;
import io.shardingsphere.core.transaction.TransactionContext;
import io.shardingsphere.core.transaction.TransactionContextHolder;
import io.shardingsphere.core.transaction.event.WeakXaTransactionEvent;
import io.shardingsphere.core.transaction.event.XaTransactionEvent;
import io.shardingsphere.core.transaction.listener.TransactionListener;
import io.shardingsphere.core.transaction.spi.TransactionManager;
import io.shardingsphere.core.util.EventBusInstance;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.Iterator;
import java.util.ServiceLoader;

/**
 * Loader Transaction spi with ServiceLoader.
 *
 * @author zhaojun
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class TransactionLoader {
    
    /**
     * Using ServiceLoader to dynamic load spi transaction.
     *
     */
    public static void load() {
        TransactionContext transactionContext = TransactionContextHolder.get();
        switch (transactionContext.getTransactionType()) {
            case XA:
                doXaTransactionConfiguration();
                break;
            case BASE:
                break;
            default:
        }
        EventBusInstance.getInstance().register(TransactionListener.getInstance());
    }
    
    private static void doXaTransactionConfiguration() {
        Iterator<TransactionManager> iterator = ServiceLoader.load(TransactionManager.class).iterator();
        TransactionContext transactionContext;
        if (iterator.hasNext()) {
            transactionContext = new TransactionContext(iterator.next(), TransactionType.XA, XaTransactionEvent.class);
        } else {
            transactionContext = new TransactionContext(new WeakXaTransactionManager(), TransactionType.XA, WeakXaTransactionEvent.class);
        }
        TransactionContextHolder.set(transactionContext);
    }
}
