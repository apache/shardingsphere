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

package org.apache.shardingsphere.test.integration.engine.junit.parallel.impl;

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
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.test.integration.engine.junit.parallel.ParallelRunnerExecutor;
import org.apache.shardingsphere.test.integration.engine.param.model.ParameterizedArray;
import org.apache.shardingsphere.test.integration.env.IntegrationTestEnvironment;

import java.util.Collection;
import java.util.LinkedList;
import java.util.concurrent.ThreadFactory;
import java.util.stream.Collectors;

/**
 * Parallel runner executor with scenario.
 */
@Slf4j
public final class ScenarioParallelRunnerExecutor implements ParallelRunnerExecutor {
    
    private final Disruptor<CaseEntryEvent> disruptor;
    
    private final RingBuffer<CaseEntryEvent> ringBuffer;
    
    public ScenarioParallelRunnerExecutor() {
        disruptor = createDisruptor();
        disruptor.handleEventsWith(createEventHandlers()).then(new CleanupEventHandler());
        disruptor.start();
        ringBuffer = disruptor.getRingBuffer();
    }
    
    private Disruptor<CaseEntryEvent> createDisruptor() {
        EventFactory<CaseEntryEvent> eventFactory = CaseEntryEvent::new;
        ThreadFactory threadFactory = new ThreadFactoryBuilder()
                .setDaemon(false).setNameFormat("disruptor-processors-%d").setUncaughtExceptionHandler((thread, ex) -> log.error("Disruptor handler thread exception", ex)).build();
        return new Disruptor<>(eventFactory, 16384, threadFactory, ProducerType.SINGLE, new BlockingWaitStrategy());
    }
    
    private CaseEventHandler[] createEventHandlers() {
        Collection<CaseEventHandler> result = new LinkedList<>();
        for (String each : IntegrationTestEnvironment.getInstance().getAdapters()) {
            result.addAll(createEventHandlers(each, IntegrationTestEnvironment.getInstance().getScenarios(), IntegrationTestEnvironment.getInstance().getDataSourceEnvironments().keySet()));
        }
        return result.toArray(new CaseEventHandler[0]);
    }
    
    private Collection<CaseEventHandler> createEventHandlers(final String adapter, final Collection<String> scenarios, final Collection<DatabaseType> databaseTypes) {
        Collection<CaseEventHandler> result = new LinkedList<>();
        for (String each : scenarios) {
            result.addAll(createEventHandlers(adapter, each, databaseTypes));
        }
        return result;
    }
    
    private Collection<CaseEventHandler> createEventHandlers(final String adapter, final String scenario, final Collection<DatabaseType> databaseTypes) {
        return databaseTypes.stream().map(each -> new CaseEventHandler(new CaseKey(adapter, scenario, each.getName()).hashCode())).collect(Collectors.toList());
    }
    
    @Override
    public void execute(final ParameterizedArray parameterizedArray, final Runnable childStatement) {
        ringBuffer.publishEvent((event, sequence) -> {
            event.reset();
            event.setCaseKey(new CaseKey(parameterizedArray.getAdapter(), parameterizedArray.getScenario(), parameterizedArray.getDatabaseType().getName()));
            event.setChildStatement(childStatement);
        });
    }
    
    @Override
    public void finished() {
        if (null != disruptor) {
            disruptor.shutdown();
        }
    }
    
    @RequiredArgsConstructor
    private static final class CaseKey {
        
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
        
        private Sequence reportingSequence;
        
        @Override
        public void onEvent(final CaseEntryEvent event, final long sequence, final boolean endOfBatch) {
            if (null == event.caseKey || event.hashCode() != hashCode) {
                return;
            }
            event.childStatement.run();
            if (null != reportingSequence) {
                reportingSequence.set(sequence);
            }
        }
        
        @Override
        public void setSequenceCallback(final Sequence sequenceCallback) {
            reportingSequence = sequenceCallback;
        }
    }
    
    private static final class CleanupEventHandler implements EventHandler<CaseEntryEvent> {
        
        @Override
        public void onEvent(final CaseEntryEvent event, final long sequence, final boolean endOfBatch) {
            event.reset();
        }
    }
}
