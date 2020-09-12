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
import org.apache.shardingsphere.governance.core.yaml.config.YamlGovernanceCenterConfiguration;
import org.apache.shardingsphere.governance.core.yaml.config.YamlGovernanceConfiguration;
import org.apache.shardingsphere.governance.repository.api.RegistryRepository;
import org.apache.shardingsphere.scaling.core.config.ScalingContext;
import org.apache.shardingsphere.scaling.core.config.ServerConfiguration;
import org.apache.shardingsphere.scaling.core.util.ReflectionUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public final class RepositoryResumeBreakPointManagerTest {
    
    private ResumeBreakPointManager resumeBreakPointManager;
    
    private RegistryRepository registryRepository;
    
    @Before
    @SneakyThrows
    public void setUp() {
        ScalingContext.getInstance().init(mockServerConfiguration());
        resumeBreakPointManager = ResumeBreakPointManagerFactory.newInstance("MySQL", "/scalingTest/position/0");
        registryRepository = ReflectionUtil.getFieldValueFromClass(resumeBreakPointManager, "REGISTRY_REPOSITORY", RegistryRepository.class);
    }
    
    private ServerConfiguration mockServerConfiguration() {
        ServerConfiguration result = new ServerConfiguration();
        result.setResumeBreakPoint(new YamlGovernanceConfiguration());
        result.getResumeBreakPoint().setName("scalingJob");
        result.getResumeBreakPoint().setRegistryCenter(new YamlGovernanceCenterConfiguration());
        result.getResumeBreakPoint().getRegistryCenter().setType("REG_FIXTURE");
        return result;
    }
    
    @Test
    @SneakyThrows
    public void assertPersistIncrementalPosition() {
        resumeBreakPointManager.persistIncrementalPosition();
        assertThat(registryRepository.get("/scalingTest/position/0/incremental"), is("{}"));
    }
    
    @Test
    @SneakyThrows
    public void assertPersistInventoryPosition() {
        resumeBreakPointManager.persistInventoryPosition();
        assertThat(registryRepository.get("/scalingTest/position/0/inventory"), is("{\"unfinished\":{},\"finished\":[]}"));
    }
    
    @After
    public void tearDown() {
        resumeBreakPointManager.close();
    }
}
