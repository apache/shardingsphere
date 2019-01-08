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
import io.shardingsphere.transaction.core.context.SagaTransactionContext;
import io.shardingsphere.transaction.core.manager.BASETransactionManager;
import io.shardingsphere.transaction.saga.SagaConfiguration;
import io.shardingsphere.transaction.saga.SagaTransaction;
import io.shardingsphere.transaction.saga.servicecomb.SagaExecutionComponentHolder;
import io.shardingsphere.transaction.saga.servicecomb.transport.ShardingTransportFactory;

import javax.transaction.Status;
import java.util.HashMap;
import java.util.Map;

/**
 * Saga transaction manager.
 *
 * @author zhaojun
 * @author yangyi
 */
public final class SagaTransactionManager implements BASETransactionManager<SagaTransactionContext> {
    
    private static final String TRANSACTION_KEY = "transaction";
    
    private static final SagaTransactionManager INSTANCE = new SagaTransactionManager();
    
    private static final ThreadLocal<SagaTransaction> TRANSACTIONS = new ThreadLocal<>();

    private final SagaExecutionComponentHolder sagaExecutionComponentHolder = new SagaExecutionComponentHolder();
    
    private final SagaConfiguration sagaConfiguration = new SagaConfiguration();
    
    @Override
    public void begin(final SagaTransactionContext transactionContext) {
        SagaTransaction transaction = new SagaTransaction(sagaConfiguration, transactionContext.getDataSourceMap());
        initExecuteDataMap(transaction);
        TRANSACTIONS.set(transaction);
        ShardingTransportFactory.getInstance().cacheTransport(transaction);
    }
    
    @Override
    public void commit(final SagaTransactionContext transactionContext) {
        if (null != TRANSACTIONS.get() && TRANSACTIONS.get().isContainException()) {
            doComponent();
        }
        cleanTransaction();
    }
    
    @Override
    public void rollback(final SagaTransactionContext transactionContext) {
        if (null != TRANSACTIONS.get()) {
            doComponent();
        }
        cleanTransaction();
    }
    
    @Override
    public int getStatus() {
        return null == TRANSACTIONS.get() ? Status.STATUS_NO_TRANSACTION : Status.STATUS_ACTIVE;
    }
    
    @Override
    public String getTransactionId() {
        return null == TRANSACTIONS.get() ? null : TRANSACTIONS.get().getId();
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
     * Remove saga execution component from caches if exist.
     *
     * @param sagaConfiguration saga configuration
     */
    public void removeSagaExecutionComponent(final SagaConfiguration sagaConfiguration) {
        sagaExecutionComponentHolder.removeSagaExecutionComponent(sagaConfiguration);
    }
    
    /**
     * Get saga transaction object for current thread.
     *
     * @return saga transaction object
     */
    public SagaTransaction getTransaction() {
        return TRANSACTIONS.get();
    }
    
    private void initExecuteDataMap(final SagaTransaction transaction) {
        Map<String, Object> sagaExecuteDataMap = new HashMap<>(1);
        sagaExecuteDataMap.put(TRANSACTION_KEY, transaction);
        ShardingExecuteDataMap.setDataMap(sagaExecuteDataMap);
    }
    
    private void doComponent() {
        try {
            String json = TRANSACTIONS.get().getSagaDefinitionBuilder().build();
            sagaExecutionComponentHolder.getSagaExecutionComponent(sagaConfiguration).run(json);
        } catch (JsonProcessingException ignored) {
        }
    }
    
    private void cleanTransaction() {
        ShardingTransportFactory.getInstance().remove();
        ShardingExecuteDataMap.getDataMap().clear();
        TRANSACTIONS.remove();
    }
}
