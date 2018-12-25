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
import io.shardingsphere.api.config.SagaConfiguration;
import io.shardingsphere.core.constant.SagaRecoveryPolicy;
import io.shardingsphere.transaction.core.internal.context.SagaSQLExecutionContext;
import io.shardingsphere.transaction.core.internal.context.SagaTransactionContext;
import io.shardingsphere.transaction.core.internal.manager.BASETransactionManager;
import io.shardingsphere.transaction.saga.handler.SagaSQLExecutionContextHandler;
import io.shardingsphere.transaction.saga.revert.EmptyRevertEngine;
import io.shardingsphere.transaction.saga.revert.RevertEngine;
import io.shardingsphere.transaction.saga.revert.RevertEngineImpl;
import io.shardingsphere.transaction.saga.servicecomb.SagaExecutionComponentHolder;
import io.shardingsphere.transaction.saga.servicecomb.definition.SagaDefinitionBuilder;
import io.shardingsphere.transaction.saga.servicecomb.transport.ShardingTransportFactory;

import javax.transaction.Status;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Saga transaction manager.
 *
 * @author zhaojun
 * @author yangyi
 */
public final class SagaTransactionManager implements BASETransactionManager<SagaTransactionContext> {
    
    private static final SagaTransactionManager INSTANCE = new SagaTransactionManager();
    
    private static final ThreadLocal<String> TRANSACTION_IDS = new ThreadLocal<>();
    
    private final SagaSQLExecutionContextHandler sagaSQLExecutionContextHandler = new SagaSQLExecutionContextHandler();

    private final SagaExecutionComponentHolder sagaExecutionComponentHolder = new SagaExecutionComponentHolder();
    
    private final Map<String, SagaDefinitionBuilder> sagaDefinitionBuilderMap = new ConcurrentHashMap<>();
    
    private final Map<String, RevertEngine> revertEngineMap = new ConcurrentHashMap<>();
    
    @Override
    public void begin(final SagaTransactionContext transactionContext) {
        TRANSACTION_IDS.set(UUID.randomUUID().toString());
        ShardingTransportFactory.getInstance().cacheTransport(transactionContext, getTransactionId());
        SagaConfiguration sagaConfiguration = transactionContext.getSagaConfiguration();
        sagaDefinitionBuilderMap.put(getTransactionId(), new SagaDefinitionBuilder(sagaConfiguration.getRecoveryPolicy().getName(),
            sagaConfiguration.getTransactionMaxRetries(), sagaConfiguration.getCompensationMaxRetries(), sagaConfiguration.getTransactionRetryDelay()));
        revertEngineMap.put(getTransactionId(),
            SagaRecoveryPolicy.FORWARD == sagaConfiguration.getRecoveryPolicy() ? new EmptyRevertEngine() : new RevertEngineImpl(transactionContext.getDataSourceMap()));
    }
    
    @Override
    public void commit(final SagaTransactionContext transactionContext) {
        cleanTransaction();
    }
    
    @Override
    public void rollback(final SagaTransactionContext transactionContext) {
        if (null != getTransactionId()) {
            try {
                String json = sagaDefinitionBuilderMap.get(getTransactionId()).build();
                sagaExecutionComponentHolder.getSagaExecutionComponent(transactionContext.getSagaConfiguration()).run(json);
            } catch (JsonProcessingException ignored) {
            }
        }
        cleanTransaction();
    }
    
    @Override
    public int getStatus() {
        if (null != getTransactionId()) {
            return Status.STATUS_ACTIVE;
        }
        return Status.STATUS_NO_TRANSACTION;
    }
    
    @Override
    public String getTransactionId() {
        return TRANSACTION_IDS.get();
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
     * Handle saga SQL execution event.
     *
     * @param sagaSQLExecutionContext saga SQL execution event
     */
    public void handleSQLExecutionEvent(final SagaSQLExecutionContext sagaSQLExecutionContext) {
        sagaSQLExecutionContextHandler.handle(sagaSQLExecutionContext);
    }
    
    /**
     * Get saga definition builder for special transaction.
     *
     * @param transactionId transaction Id
     * @return saga definition builder
     */
    public SagaDefinitionBuilder getSagaDefinitionBuilder(final String transactionId) {
        return sagaDefinitionBuilderMap.get(transactionId);
    }
    
    /**
     * Get revert engine for special transaction.
     *
     * @param transactionId transaction Id
     * @return revert engine
     */
    public RevertEngine getReverEngine(final String transactionId) {
        return revertEngineMap.get(transactionId);
    }
    
    private void cleanTransaction() {
        ShardingTransportFactory.getInstance().remove(getTransactionId());
        if (null != getTransactionId()) {
            sagaDefinitionBuilderMap.remove(getTransactionId());
            revertEngineMap.remove(getTransactionId());
            sagaSQLExecutionContextHandler.clean();
        }
        TRANSACTION_IDS.remove();
    }
}
