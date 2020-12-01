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

package org.apache.shardingsphere.scaling.core.service.impl;

import lombok.SneakyThrows;
import org.apache.shardingsphere.governance.core.yaml.config.YamlGovernanceCenterConfiguration;
import org.apache.shardingsphere.governance.core.yaml.config.YamlGovernanceConfiguration;
import org.apache.shardingsphere.governance.repository.api.RegistryRepository;
import org.apache.shardingsphere.scaling.core.config.ScalingConfiguration;
import org.apache.shardingsphere.scaling.core.config.ScalingContext;
import org.apache.shardingsphere.scaling.core.config.ServerConfiguration;
import org.apache.shardingsphere.scaling.core.constant.ScalingConstant;
import org.apache.shardingsphere.scaling.core.job.JobProgress;
import org.apache.shardingsphere.scaling.core.job.ScalingJob;
import org.apache.shardingsphere.scaling.core.job.task.incremental.IncrementalTaskProgress;
import org.apache.shardingsphere.scaling.core.job.task.inventory.InventoryTaskProgress;
import org.apache.shardingsphere.scaling.core.service.RegistryRepositoryHolder;
import org.apache.shardingsphere.scaling.core.service.ScalingJobService;
import org.apache.shardingsphere.scaling.core.util.ScalingConfigurationUtil;
import org.apache.shardingsphere.scaling.core.utils.ReflectionUtil;
import org.apache.shardingsphere.scaling.core.utils.ScalingTaskUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public final class DistributedScalingJobServiceTest {
    
    private ScalingJobService scalingJobService;
    
    private RegistryRepository registryRepository;
    
    @Before
    public void setUp() {
        ScalingContext.getInstance().init(mockServerConfiguration());
        scalingJobService = new DistributedScalingJobService();
        registryRepository = RegistryRepositoryHolder.getInstance();
        registryRepository.persist(ScalingConstant.SCALING_LISTENER_PATH, "");
    }
    
    @Test
    public void assertListJobs() {
        assertThat(scalingJobService.listJobs().size(), is(0));
        scalingJobService.start(mockScalingConfiguration());
        assertThat(scalingJobService.listJobs().size(), is(1));
    }
    
    @Test
    @SneakyThrows(IOException.class)
    public void assertShouldScaling() {
        String oldConfig = ScalingConfigurationUtil.getConfig("/proxy_config-sharding_1.yaml");
        String newConfig = ScalingConfigurationUtil.getConfig("/proxy_config-sharding_2.yaml");
        assertFalse(scalingJobService.shouldScaling(oldConfig, oldConfig));
        assertTrue(scalingJobService.shouldScaling(oldConfig, newConfig));
    }
    
    @Test
    public void assertStartWithScalingConfig() {
        Optional<ScalingJob> shardingScalingJob = scalingJobService.start(mockScalingConfiguration());
        assertTrue(shardingScalingJob.isPresent());
        assertTrue(registryRepository.get(ScalingTaskUtil.getScalingListenerPath(shardingScalingJob.get().getJobId(), ScalingConstant.CONFIG)).contains("\"running\":true"));
    }
    
    @Test
    @SneakyThrows(IOException.class)
    public void assertStartWithProxyConfig() {
        String oldConfig = ScalingConfigurationUtil.getConfig("/proxy_config-sharding_1.yaml");
        String newConfig = ScalingConfigurationUtil.getConfig("/proxy_config-sharding_2.yaml");
        assertFalse(scalingJobService.start(oldConfig, oldConfig).isPresent());
        Optional<ScalingJob> shardingScalingJob = scalingJobService.start(oldConfig, newConfig);
        assertTrue(shardingScalingJob.isPresent());
        assertTrue(registryRepository.get(ScalingTaskUtil.getScalingListenerPath(shardingScalingJob.get().getJobId(), ScalingConstant.CONFIG)).contains("\"running\":true"));
    }
    
    @Test
    public void assertStop() {
        Optional<ScalingJob> shardingScalingJob = scalingJobService.start(mockScalingConfiguration());
        assertTrue(shardingScalingJob.isPresent());
        scalingJobService.stop(shardingScalingJob.get().getJobId());
        assertTrue(registryRepository.get(ScalingTaskUtil.getScalingListenerPath(shardingScalingJob.get().getJobId(), ScalingConstant.CONFIG)).contains("\"running\":false"));
    }
    
    @Test
    public void assertGetProgress() {
        registryRepository.persist(ScalingTaskUtil.getScalingListenerPath("1/config"), "{'ruleConfiguration':{'source':{},'target':{}},'jobConfiguration':{'running':true}}");
        registryRepository.persist(ScalingTaskUtil.getScalingListenerPath("1/position/0/inventory"),
                "{'unfinished': {'ds1.table1#1':[0,100],'ds1.table1#2':[160,200],'ds1.table3':[]},'finished':['ds1.table2#1','ds1.table2#2']}");
        registryRepository.persist(ScalingTaskUtil.getScalingListenerPath("1/position/0/incremental"),
                "{'ds1':{'filename':binlog1,'position':4,'delay':1},'ds3':{'filename':binlog2,'position':4,'delay':3}}");
        registryRepository.persist(ScalingTaskUtil.getScalingListenerPath("1/position/1/inventory"),
                "{'unfinished': {'ds2.table1#1':[0,100],'ds2.table1#2':[160,200],'ds2.table3':[]},'finished':['ds2.table2#1','ds2.table2#2']}");
        registryRepository.persist(ScalingTaskUtil.getScalingListenerPath("1/position/1/incremental"),
                "{'ds2':{'filename':binlog1,'position':4,'delay':2},'ds4':{'filename':binlog2,'position':4,'delay':4}}");
        JobProgress actual = scalingJobService.getProgress(1);
        assertThat(actual.getInventoryTaskProgress().get("0").stream()
                .map(each -> (InventoryTaskProgress) each)
                .filter(InventoryTaskProgress::isFinished).count(), is(2L));
        assertTrue(actual.getIncrementalTaskProgress().get("1").stream()
                .map(each -> (IncrementalTaskProgress) each)
                .filter(each -> "ds2".equals(each.getId()))
                .allMatch(each -> 2 == each.getDelayMillisecond()));
    }
    
    @Test
    public void assertRemove() {
        registryRepository.persist(ScalingTaskUtil.getScalingListenerPath("1/config"), "config");
        scalingJobService.remove(1);
        assertNull(registryRepository.get("1"));
    }
    
    @After
    public void tearDown() {
        registryRepository.close();
        resetRegistryRepositoryAvailable();
    }
    
    private ServerConfiguration mockServerConfiguration() {
        resetRegistryRepositoryAvailable();
        YamlGovernanceConfiguration distributedScalingService = new YamlGovernanceConfiguration();
        distributedScalingService.setName("test");
        YamlGovernanceCenterConfiguration registryCenter = new YamlGovernanceCenterConfiguration();
        registryCenter.setType("REG_FIXTURE");
        registryCenter.setServerLists("");
        distributedScalingService.setRegistryCenter(registryCenter);
        ServerConfiguration result = new ServerConfiguration();
        result.setDistributedScalingService(distributedScalingService);
        return result;
    }
    
    @SneakyThrows(ReflectiveOperationException.class)
    private void resetRegistryRepositoryAvailable() {
        ReflectionUtil.setStaticFieldValue(RegistryRepositoryHolder.class, "available", null);
    }
    
    @SneakyThrows(IOException.class)
    private ScalingConfiguration mockScalingConfiguration() {
        return ScalingConfigurationUtil.initConfig("/config.json");
    }
}
