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

import com.google.common.util.concurrent.MoreExecutors;
import io.shardingsphere.api.config.SagaConfiguration;
import io.shardingsphere.core.event.transaction.base.SagaTransactionEvent;
import io.shardingsphere.core.executor.ShardingThreadFactoryBuilder;
import io.shardingsphere.transaction.manager.base.BASETransactionManager;
import io.shardingsphere.transaction.base.manager.servicecomb.ShardingTransportFactorySPILoader;
import org.apache.servicecomb.saga.core.EventEnvelope;
import org.apache.servicecomb.saga.core.PersistentStore;
import org.apache.servicecomb.saga.core.SagaDefinition;
import org.apache.servicecomb.saga.core.application.SagaExecutionComponent;
import org.apache.servicecomb.saga.core.application.interpreter.FromJsonFormat;
import org.apache.servicecomb.saga.core.dag.GraphBasedSagaFactory;
import org.apache.servicecomb.saga.format.ChildrenExtractor;
import org.apache.servicecomb.saga.format.JacksonFromJsonFormat;
import org.apache.servicecomb.saga.infrastructure.EmbeddedEventStore;

import javax.transaction.Status;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Saga transaction manager.
 *
 * @author zhaojun
 * @author yangyi
 */
public final class SagaTransactionManager implements BASETransactionManager<SagaTransactionEvent> {
    
    private static final SagaTransactionManager INSTANCE = new SagaTransactionManager();
    
    private static final ThreadLocal<String> TRANSACTION_IDS = new ThreadLocal<>();
    
    private final Map<String, SagaExecutionComponent> sagaCaches = new HashMap<>();
    
    private final Map<String, ExecutorService> executorCaches = new HashMap<>();
    
    @Override
    public void begin(final SagaTransactionEvent transactionEvent) {
        TRANSACTION_IDS.set(UUID.randomUUID().toString());
        ShardingTransportFactorySPILoader.getInstance().getTransportFactory().cacheTransport(transactionEvent);
    }
    
    @Override
    public void commit(final SagaTransactionEvent transactionEvent) {
        // TODO Analyse the result of saga coordinator.run, if run failed, throw exception
        getSagaExecutionComponent(transactionEvent.getSagaConfiguration()).run(transactionEvent.getSagaJson());
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
     * @param config saga configuration
     */
    public void removeSagaExecutionComponent(final SagaConfiguration config) {
        synchronized (sagaCaches) {
            if (sagaCaches.containsKey(config.getAlias())) {
                SagaExecutionComponent coordinator = sagaCaches.remove(config.getAlias());
                try {
                    coordinator.terminate();
                    // CHECKSTYLE:OFF
                } catch (Exception ignored) {
                    // CHECKSTYLE:ON
                }
            }
            if (executorCaches.containsKey(config.getAlias())) {
                executorCaches.remove(config.getAlias()).shutdown();
            }
        }
    }
    
    private SagaExecutionComponent getSagaExecutionComponent(final SagaConfiguration config) {
        SagaExecutionComponent result;
        synchronized (sagaCaches) {
            if (!sagaCaches.containsKey(config.getAlias())) {
                sagaCaches.put(config.getAlias(), createSagaExecutionComponent(config, createExecutors(config)));
            }
            result = sagaCaches.get(config.getAlias());
        }
        return result;
    }
    
    private SagaExecutionComponent createSagaExecutionComponent(final SagaConfiguration config, final ExecutorService executors) {
        EmbeddedPersistentStore persistentStore = new EmbeddedPersistentStore();
        FromJsonFormat<SagaDefinition> fromJsonFormat = new JacksonFromJsonFormat(ShardingTransportFactorySPILoader.getInstance().getTransportFactory());
        GraphBasedSagaFactory sagaFactory = new GraphBasedSagaFactory(config.getCompensationRetryDelay(), persistentStore, new ChildrenExtractor(), executors);
        return new SagaExecutionComponent(persistentStore, fromJsonFormat, null, sagaFactory);
    }
    
    private ExecutorService createExecutors(final SagaConfiguration config) {
        ExecutorService result = MoreExecutors.listeningDecorator(Executors.newFixedThreadPool(config.getExecutorSize(), ShardingThreadFactoryBuilder.build("Saga-%d")));
        MoreExecutors.addDelayedShutdownHook(result, 60, TimeUnit.SECONDS);
        executorCaches.put(config.getAlias(), result);
        return result;
    }
    
    private static final class EmbeddedPersistentStore extends EmbeddedEventStore implements PersistentStore {
        
        @Override
        public Map<String, List<EventEnvelope>> findPendingSagaEvents() {
            //TODO find pending saga event from persistent store
            return null;
        }
    }
}
