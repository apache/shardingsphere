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

package org.apache.shardingsphere.data.pipeline.core.lock;

import org.apache.shardingsphere.data.pipeline.core.context.PipelineContext;
import org.apache.shardingsphere.infra.instance.InstanceContext;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.lock.service.LockNode;
import org.apache.shardingsphere.mode.metadata.MetaDataContexts;
import org.apache.shardingsphere.mode.metadata.persist.MetaDataPersistService;
import org.apache.shardingsphere.mode.repository.cluster.ClusterPersistRepository;
import org.apache.shardingsphere.transaction.context.TransactionContexts;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.concurrent.TimeUnit;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public final class PipelineSimpleLockTest {

    @Mock
    private ClusterPersistRepository clusterPersistRepository;

    @Mock
    private MetaDataPersistService metaDataPersistService;

    @Mock
    private MetaDataContexts metaDataContexts;

    private PipelineSimpleLock pipelineSimpleLock;

    private String decoratedLockName;

    @Before
    public void setUp() throws ReflectiveOperationException {
        decoratedLockName = "scaling-test";
        metaDataPersistService = new MetaDataPersistService(clusterPersistRepository);
        metaDataContexts = new MetaDataContexts(metaDataPersistService);
        ContextManager contextManager = new ContextManager();
        contextManager.init(metaDataContexts, mock(TransactionContexts.class), mock(InstanceContext.class));
        PipelineContext.initContextManager(contextManager);
        pipelineSimpleLock = PipelineSimpleLock.getInstance();
        when(clusterPersistRepository.tryLock(LockNode.getLockNodePath(decoratedLockName), 50L, TimeUnit.MILLISECONDS)).thenReturn(true);
    }

    @Test
    public void assertTryLockAndReleaseLock() {
        pipelineSimpleLock.tryLock("test", 50L);
        verify(clusterPersistRepository).tryLock(LockNode.getLockNodePath(decoratedLockName), 50L, TimeUnit.MILLISECONDS);
        pipelineSimpleLock.releaseLock("test");
        verify(clusterPersistRepository).releaseLock(LockNode.getLockNodePath(decoratedLockName));
    }

}
