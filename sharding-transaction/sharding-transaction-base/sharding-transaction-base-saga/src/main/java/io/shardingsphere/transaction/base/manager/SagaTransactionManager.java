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

package io.shardingsphere.transaction.base.manager;

import io.shardingsphere.api.config.SagaConfiguration;
import io.shardingsphere.core.event.transaction.base.SagaTransactionEvent;
import io.shardingsphere.transaction.base.servicecomb.SagaExecutionComponentHolder;
import io.shardingsphere.transaction.base.servicecomb.definition.SagaDefinitionBuilderHolder;
import io.shardingsphere.transaction.base.servicecomb.transport.ShardingTransportFactory;
import io.shardingsphere.transaction.manager.base.BASETransactionManager;

import javax.transaction.Status;
import java.util.UUID;

import com.fasterxml.jackson.core.JsonProcessingException;

/**
 * Saga transaction manager.
 *
 * @author zhaojun
 * @author yangyi
 */
public final class SagaTransactionManager implements BASETransactionManager<SagaTransactionEvent> {
    
    private static final SagaTransactionManager INSTANCE = new SagaTransactionManager();
    
    private static final ThreadLocal<String> TRANSACTION_IDS = new ThreadLocal<>();

    private final SagaExecutionComponentHolder sagaExecutionComponentHolder = new SagaExecutionComponentHolder();
    
    private final SagaDefinitionBuilderHolder sagaDefinitionBuilderHolder = new SagaDefinitionBuilderHolder();
    
    @Override
    public void begin(final SagaTransactionEvent transactionEvent) {
        TRANSACTION_IDS.set(UUID.randomUUID().toString());
        ShardingTransportFactory.getInstance().cacheTransport(transactionEvent);
        sagaDefinitionBuilderHolder.createSagaDefinitionBuilder(getTransactionId(), transactionEvent.getSagaConfiguration());
    }
    
    @Override
    public void commit(final SagaTransactionEvent transactionEvent) {
        try {
            // TODO Analyse the result of saga coordinator.run, if run failed, throw exception
            sagaExecutionComponentHolder.getSagaExecutionComponent(transactionEvent.getSagaConfiguration()).run(sagaDefinitionBuilderHolder.getSagaDefinitionBuilder(getTransactionId()).build());
        } catch (JsonProcessingException ignored) {
        }
        cleanTransaction();
    }
    
    @Override
    public void rollback(final SagaTransactionEvent transactionEvent) {
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
    
    private void cleanTransaction() {
        sagaDefinitionBuilderHolder.removeSagaDefinitionBuilder(getTransactionId());
        TRANSACTION_IDS.remove();
        ShardingTransportFactory.getInstance().remove();
    }
}
