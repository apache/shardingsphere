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

package io.shardingsphere.proxy.util;

import io.shardingsphere.core.constant.TransactionType;
import io.shardingsphere.core.transaction.listener.TransactionListener;
import io.shardingsphere.core.transaction.spi.TransactionManager;
import io.shardingsphere.core.util.EventBusInstance;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.Iterator;
import java.util.ServiceLoader;

/**
 * Transaction manager loader via SPI.
 *
 * @author zhaojun
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ProxyTransactionLoader {
    
    /**
     * Create transaction manager via SPI.
     *
     * @param transactionType transaction type
     * @return transaction manager
     */
    public static TransactionManager load(final TransactionType transactionType) {
        TransactionManager result = null;
        switch (transactionType) {
            case XA:
                result = doXaTransactionConfiguration();
                break;
            case BASE:
                break;
            default:
        }
        EventBusInstance.getInstance().register(TransactionListener.getInstance());
        return result;
    }
    
    // TODO if read more transaction manager, log chosen one, if no transaction manager, throw exception
    private static TransactionManager doXaTransactionConfiguration() {
        Iterator<TransactionManager> iterator = ServiceLoader.load(TransactionManager.class).iterator();
        return iterator.hasNext() ? iterator.next() : null;
    }
}
