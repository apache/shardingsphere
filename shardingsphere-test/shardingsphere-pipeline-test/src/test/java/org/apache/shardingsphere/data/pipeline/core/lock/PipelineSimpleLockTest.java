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

import org.apache.shardingsphere.mode.manager.cluster.coordinator.lock.service.LockRegistryService;
import org.apache.shardingsphere.mode.repository.cluster.ClusterPersistRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.lang.reflect.Field;

import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public final class PipelineSimpleLockTest {

    @Mock
    private ClusterPersistRepository clusterPersistRepository;

    private LockRegistryService lockService;

    private PipelineSimpleLock pipelineSimpleLock;

    @Before
    public void setUp() throws ReflectiveOperationException {
        lockService = new LockRegistryService(clusterPersistRepository);
        Field field = lockService.getClass().getDeclaredField("repository");
        field.setAccessible(true);
        field.set(lockService, clusterPersistRepository);
        pipelineSimpleLock = PipelineSimpleLock.getInstance();
    }

    @Test
    public void assertTryLock() {
        pipelineSimpleLock.tryLock("test", 50L);
        verify(lockService).tryLock("test", 50L);
    }

    @Test
    public void assertReleaseLock() {
        pipelineSimpleLock.releaseLock("test");
        verify(lockService).releaseLock("test");
    }
}
