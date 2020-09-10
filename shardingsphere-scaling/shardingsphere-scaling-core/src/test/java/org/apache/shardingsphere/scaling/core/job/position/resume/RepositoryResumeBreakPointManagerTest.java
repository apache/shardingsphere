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

package org.apache.shardingsphere.scaling.core.job.position.resume;

import lombok.SneakyThrows;
import org.apache.shardingsphere.governance.repository.api.RegistryRepository;
import org.apache.shardingsphere.scaling.core.config.ScalingContext;
import org.apache.shardingsphere.scaling.core.config.ServerConfiguration;
import org.apache.shardingsphere.scaling.core.util.ReflectionUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public final class RepositoryResumeBreakPointManagerTest {
    
    @Mock
    private RegistryRepository registryRepository;
    
    private RepositoryResumeBreakPointManager repositoryResumeBreakPointManager;
    
    @Before
    @SneakyThrows
    public void setUp() {
        ScalingContext.getInstance().init(new ServerConfiguration());
        ReflectionUtil.setFieldValue(RepositoryResumeBreakPointManager.class, null, "registryRepository", registryRepository);
        repositoryResumeBreakPointManager = new RepositoryResumeBreakPointManager("H2", "/base");
    }
    
    @Test
    public void assertPersistIncrementalPosition() {
        repositoryResumeBreakPointManager.persistIncrementalPosition();
        verify(registryRepository).persist("/base/incremental", "{}");
    }
    
    @Test
    public void assertPersistInventoryPosition() {
        repositoryResumeBreakPointManager.persistInventoryPosition();
        verify(registryRepository).persist("/base/inventory", "{\"unfinished\":{},\"finished\":[]}");
    }
    
    @After
    public void tearDown() {
        repositoryResumeBreakPointManager.close();
    }
}
