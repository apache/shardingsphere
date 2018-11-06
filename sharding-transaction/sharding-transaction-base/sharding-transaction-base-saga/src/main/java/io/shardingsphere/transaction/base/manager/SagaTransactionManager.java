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

import io.shardingsphere.core.constant.transaction.TransactionType;
import io.shardingsphere.core.event.transaction.base.SagaTransactionEvent;
import io.shardingsphere.transaction.manager.base.BASETransactionManager;
import io.shardingsphere.transaction.base.manager.servicecomb.SagaExecutionComponentHolder;
import io.shardingsphere.transaction.base.manager.servicecomb.ShardingTransportFactorySPILoader;
import org.apache.servicecomb.saga.core.application.SagaExecutionComponent;

import javax.transaction.Status;
import java.util.UUID;

/**
 * Saga transaction manager.
 *
 * @author zhaojun
 * @author yangyi
 */
public final class SagaTransactionManager implements BASETransactionManager<SagaTransactionEvent> {
    
    private static final SagaTransactionManager INSTANCE = new SagaTransactionManager();
    
    private static final ThreadLocal<String> TRANSACTION_IDS = new ThreadLocal<>();
    
    private final SagaExecutionComponent coordinator;
    
    private SagaTransactionManager() {
        this.coordinator = SagaExecutionComponentHolder.getInstance().getSagaExecutionComponent();
    }
    
    @Override
    public void begin(final SagaTransactionEvent transactionEvent) {
        TRANSACTION_IDS.set(UUID.randomUUID().toString());
        ShardingTransportFactorySPILoader.getInstance().getTransportFactory().cacheTransport(transactionEvent);
    }
    
    @Override
    public void commit(final SagaTransactionEvent transactionEvent) {
        // TODO Analyse the result of saga coordinator.run, if run failed, throw exception
        coordinator.run(transactionEvent.getSagaJson());
        TRANSACTION_IDS.remove();
        ShardingTransportFactorySPILoader.getInstance().getTransportFactory().remove();
    }
    
    @Override
    public void rollback(final SagaTransactionEvent transactionEvent) {
        TRANSACTION_IDS.remove();
        ShardingTransportFactorySPILoader.getInstance().getTransportFactory().remove();
    }
    
    @Override
    public int getStatus() {
        // TODO :zhaojun need confirm, return Status.STATUS_NO_TRANSACTION or zero?
        if (null != getTransactionId()) {
            return Status.STATUS_ACTIVE;
        }
        return Status.STATUS_NO_TRANSACTION;
    }
    
    @Override
    public TransactionType getTransactionType() {
        return TransactionType.BASE;
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
}
