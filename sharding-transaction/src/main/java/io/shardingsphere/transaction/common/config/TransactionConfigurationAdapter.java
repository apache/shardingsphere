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
import com.google.common.collect.Lists;
import io.shardingsphere.core.constant.TransactionType;
import io.shardingsphere.transaction.api.TransactionManager;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;

/**
 * Abstract transaction manager configuration.
 *
 * @author zhaojun
 */
@Slf4j
public abstract class TransactionConfigurationAdapter implements TransactionConfiguration {
    
    private static final Map<TransactionType, Optional<TransactionManager>> SPI_RESOURCES = new HashMap<>();
    
    @Override
    public final TransactionManager getTransactionManager(final TransactionType transactionType) {
        switch (transactionType) {
            case XA:
                return getXATransactionManager(transactionType);
            default:
                return null;
        }
    }
    
    protected abstract TransactionManager getXATransactionManager(TransactionType transactionType);
    
    protected final Optional<TransactionManager> getXATransactionManagerFromSPI(final TransactionType transactionType) {
        if (SPI_RESOURCES.containsKey(transactionType)) {
            return SPI_RESOURCES.get(transactionType);
        }
        synchronized (SPI_RESOURCES) {
            List<TransactionManager> transactionManagerList = Lists.newArrayList(ServiceLoader.load(TransactionManager.class).iterator());
            if (transactionManagerList.size() > 1) {
                log.info("there is more than one transaction manger existing, chosen first one default.");
            }
            Optional<TransactionManager> transactionManager = transactionManagerList.isEmpty() ? Optional.<TransactionManager>absent() : Optional.of(transactionManagerList.get(0));
            SPI_RESOURCES.put(transactionType, transactionManager);
            return transactionManager;
        }
    }
}
