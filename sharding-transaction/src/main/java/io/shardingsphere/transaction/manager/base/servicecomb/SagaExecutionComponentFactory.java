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

package io.shardingsphere.transaction.manager.base.servicecomb;

import org.apache.servicecomb.saga.core.EventEnvelope;
import org.apache.servicecomb.saga.core.PersistentStore;
import org.apache.servicecomb.saga.core.SagaDefinition;
import org.apache.servicecomb.saga.core.application.SagaExecutionComponent;
import org.apache.servicecomb.saga.core.application.interpreter.FromJsonFormat;
import org.apache.servicecomb.saga.core.dag.GraphBasedSagaFactory;
import org.apache.servicecomb.saga.format.ChildrenExtractor;
import org.apache.servicecomb.saga.format.JacksonFromJsonFormat;
import org.apache.servicecomb.saga.infrastructure.EmbeddedEventStore;
import org.apache.servicecomb.saga.transports.SQLTransport;
import org.apache.servicecomb.saga.transports.TransportFactory;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;

/**
 * SagaExecutionComponent factory.
 *
 * @author yangyi
 */
public final class SagaExecutionComponentFactory {
    
    private static final TransportFactory<SQLTransport> TRANSPORT_FACTORY = new TransportFactory<SQLTransport>() {
        @Override
        public SQLTransport getTransport() {
            return SQLTransportSPILoader.getInstance().getSqlTransport();
        }
    };
    
    /**
     * create new saga execution component.
     *
     * @return saga execution component
     */
    public static SagaExecutionComponent createSagaExecutionComponent() {
        EmbeddedPersistentStore persistentStore = new EmbeddedPersistentStore();
        FromJsonFormat<SagaDefinition> fromJsonFormat = new JacksonFromJsonFormat(TRANSPORT_FACTORY);
        GraphBasedSagaFactory sagaFactory = new GraphBasedSagaFactory(3000, persistentStore, new ChildrenExtractor(), Executors.newFixedThreadPool(5));
        return new SagaExecutionComponent(persistentStore, fromJsonFormat, null, sagaFactory);
    }
    
    private static final class EmbeddedPersistentStore extends EmbeddedEventStore implements PersistentStore {
        
        @Override
        public Map<String, List<EventEnvelope>> findPendingSagaEvents() {
            //TODO find pending saga event form persistent store
            return null;
        }
    }
}
