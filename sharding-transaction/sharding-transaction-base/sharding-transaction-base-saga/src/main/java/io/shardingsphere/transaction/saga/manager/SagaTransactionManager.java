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

package io.shardingsphere.transaction.saga.manager;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.shardingsphere.core.executor.ShardingExecuteDataMap;
import io.shardingsphere.transaction.core.manager.ShardingTransactionManager;
import io.shardingsphere.transaction.saga.SagaTransaction;
import io.shardingsphere.transaction.saga.servicecomb.transport.ShardingTransportFactory;
import lombok.Getter;

import javax.transaction.Status;

/**
 * Saga transaction manager.
 *
 * @author zhaojun
 * @author yangyi
 */
public final class SagaTransactionManager implements ShardingTransactionManager {
    
    private static final String TRANSACTION_KEY = "transaction";
    
    private static final SagaTransactionManager INSTANCE = new SagaTransactionManager();
    
    private static final ThreadLocal<SagaTransaction> TRANSACTIONS = new ThreadLocal<>();
    
    @Getter
    private final SagaResourceManager resourceManager = new SagaResourceManager();
    
    @Override
    public void begin() {
        if (null == TRANSACTIONS.get()) {
            SagaTransaction transaction = new SagaTransaction(resourceManager.getSagaConfiguration(), resourceManager.getDataSourceMap());
            ShardingExecuteDataMap.getDataMap().put(TRANSACTION_KEY, transaction);
            TRANSACTIONS.set(transaction);
            ShardingTransportFactory.getInstance().cacheTransport(transaction);
        }
    }
    
    @Override
    public void commit() {
        if (null != TRANSACTIONS.get() && TRANSACTIONS.get().isContainException()) {
            submitToActuator();
        }
        cleanTransaction();
    }
    
    @Override
    public void rollback() {
        cleanTransaction();
    }
    
    @Override
    public int getStatus() {
        return null == TRANSACTIONS.get() ? Status.STATUS_NO_TRANSACTION : Status.STATUS_ACTIVE;
    }
    
    /**
     * Get saga transaction manager instance.
     *
     * @return saga transaction manager
     */
    public static SagaTransactionManager getInstance() {
        return INSTANCE;
    }
    
    /**
     * Get saga transaction object for current thread.
     *
     * @return saga transaction object
     */
    public SagaTransaction getTransaction() {
        return TRANSACTIONS.get();
    }
    
    private void submitToActuator() {
        try {
            String json = TRANSACTIONS.get().getSagaDefinitionBuilder().build();
            resourceManager.getSagaExecutionComponent().run(json);
        } catch (JsonProcessingException ignored) {
        }
    }
    
    private void cleanTransaction() {
        ShardingTransportFactory.getInstance().remove();
        ShardingExecuteDataMap.getDataMap().remove(TRANSACTION_KEY);
        TRANSACTIONS.remove();
    }
}
