/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.shardingsphere.test.integration.engine.junit;

import com.google.common.base.Charsets;
import com.google.common.hash.Hasher;
import com.google.common.hash.Hashing;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.lmax.disruptor.BlockingWaitStrategy;
import com.lmax.disruptor.EventFactory;
import com.lmax.disruptor.EventHandler;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.Sequence;
import com.lmax.disruptor.SequenceReportingEventHandler;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.test.integration.engine.param.domain.ParameterizedWrapper;
import org.apache.shardingsphere.test.integration.env.IntegrationTestEnvironment;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadFactory;

/**
 * IT runner scenarios executor.
 */
@Slf4j
@Getter
public class ITRunnerScenariosExecutor implements ITRunnerExecutor {
    
    private final Disruptor<CaseEntryEvent> disruptor;
    
    private final RingBuffer<CaseEntryEvent> ringBuffer;
    
    private final List<CaseEventHandler> caseEventHandlers;
    
    @SneakyThrows
    public ITRunnerScenariosExecutor() {
        EventFactory<CaseEntryEvent> eventFactory = () -> new CaseEntryEvent();
        ThreadFactory threadFactory = new ThreadFactoryBuilder().setDaemon(false).setNameFormat("disruptor-processors-%d")
                .setUncaughtExceptionHandler((t, e) -> log.error("disruptor handler thread exception", e)).build();
        disruptor = new Disruptor<>(eventFactory, 16384, threadFactory, ProducerType.SINGLE, new BlockingWaitStrategy());
        IntegrationTestEnvironment integrationTestEnvironment = IntegrationTestEnvironment.getInstance();
        Collection<String> adapters = integrationTestEnvironment.getAdapters();
        Collection<String> scenarios = integrationTestEnvironment.getScenarios();
        Set<DatabaseType> databaseTypes = integrationTestEnvironment.getDataSourceEnvironments().keySet();
        caseEventHandlers = new ArrayList<>();
        initCaseEventHandlers(adapters, scenarios, databaseTypes);
        CaseEventHandler[] caseEventHandlerArray = new CaseEventHandler[caseEventHandlers.size()];
        caseEventHandlers.toArray(caseEventHandlerArray);
        disruptor.handleEventsWith(caseEventHandlerArray).then(new CleanupEventHandler());
        disruptor.start();
        ringBuffer = disruptor.getRingBuffer();
    }
    
    private void initCaseEventHandlers(final Collection<String> adapters, final Collection<String> scenarios, final Set<DatabaseType> databaseTypes) {
        for (String each : adapters) {
            initCaseEventHandlers(each, scenarios, databaseTypes);
        }
    }
    
    private void initCaseEventHandlers(final String adapter, final Collection<String> scenarios, final Set<DatabaseType> databaseTypes) {
        for (String each : scenarios) {
            initCaseEventHandlers(adapter, each, databaseTypes);
        }
    }
    
    private void initCaseEventHandlers(final String adapter, final String scenario, final Collection<DatabaseType> databaseTypes) {
        for (DatabaseType each : databaseTypes) {
            caseEventHandlers.add(new CaseEventHandler(new CaseKey(adapter, scenario, each.getName()).hashCode()));
        }
    }
    
    @Override
    public void execute(final ParameterizedWrapper parameterizedWrapper, final Runnable childStatement) {
        ringBuffer.publishEvent((e, seq) -> {
            e.reset();
            e.setCaseKey(new CaseKey(parameterizedWrapper.getAdapter(), parameterizedWrapper.getScenario(), parameterizedWrapper.getDatabaseType().getName()));
            e.setChildStatement(childStatement);
        });
    }
    
    @Override
    public void finished() {
        if (null != disruptor) {
            disruptor.shutdown();
        }
    }
    
    @RequiredArgsConstructor
    public static final class CaseKey {
    
        private final String adapter;
    
        private final String scenario;
    
        private final String databaseTypeName;
    
        @Override
        public int hashCode() {
            Hasher hasher = Hashing.murmur3_32().newHasher();
            hasher.putString(adapter, Charsets.UTF_8);
            hasher.putString(scenario, Charsets.UTF_8);
            hasher.putString(databaseTypeName, Charsets.UTF_8);
            return hasher.hash().asInt();
        }
    }
    
    /**
     * Case entry event.
     */
    @Setter
    private static final class CaseEntryEvent {
        
        private CaseKey caseKey;
    
        private Runnable childStatement;
    
        @Override
        public int hashCode() {
            return caseKey.hashCode();
        }
        
        public void reset() {
            caseKey = null;
            childStatement = null;
        }
    }
    
    @RequiredArgsConstructor
    private static final class CaseEventHandler implements SequenceReportingEventHandler<CaseEntryEvent> {
        
        private final int hashCode;
        
        private Sequence reportingSeq;
    
        @Override
        public void onEvent(final CaseEntryEvent event, final long sequence, final boolean endOfBatch) {
            if (null == event.caseKey || event.hashCode() != hashCode) {
                return;
            }
            event.childStatement.run();
            if (reportingSeq != null) {
                reportingSeq.set(sequence);
            }
        }
        
        @Override
        public void setSequenceCallback(final Sequence sequenceCallback) {
            this.reportingSeq = sequenceCallback;
        }
    }
    
    private static final class CleanupEventHandler implements EventHandler<CaseEntryEvent> {
        
        @Override
        public void onEvent(final CaseEntryEvent event, final long sequence, final boolean endOfBatch) {
            event.reset();
        }
    }
}
