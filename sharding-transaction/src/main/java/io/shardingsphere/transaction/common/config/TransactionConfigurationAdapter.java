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

package io.shardingsphere.transaction.common.config;

import com.google.common.base.Optional;
import io.shardingsphere.core.constant.TransactionType;
import io.shardingsphere.transaction.TransactionEventBusInstance;
import io.shardingsphere.transaction.common.listener.TransactionListener;
import io.shardingsphere.transaction.common.spi.TransactionManager;

import java.util.Iterator;
import java.util.ServiceLoader;

/**
 * Abstract transaction manager configuration.
 *
 * @author zhaojun
 */
public abstract class TransactionConfigurationAdapter implements TransactionConfiguration {
    
    @Override
    public void configTransactionContext(final TransactionType transactionType) {
        switch (transactionType) {
            case XA:
                doXaTransactionConfiguration();
                break;
            case BASE:
                break;
            default:
        }
    }
    
    @Override
    public void registerListener() {
        TransactionEventBusInstance.getInstance().register(TransactionListener.getInstance());
    }
    
    protected abstract void doXaTransactionConfiguration();
    
    protected Optional<TransactionManager> doSPIConfiguration() {
        Iterator<TransactionManager> iterator = ServiceLoader.load(TransactionManager.class).iterator();
        return iterator.hasNext() ? Optional.of(iterator.next()) : Optional.<TransactionManager>absent();
    }
}
