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

import com.google.gson.Gson;
import lombok.SneakyThrows;
import org.apache.shardingsphere.governance.core.event.model.rule.RuleConfigurationsAlteredEvent;
import org.apache.shardingsphere.governance.repository.api.RegistryRepository;
import org.apache.shardingsphere.governance.repository.api.config.GovernanceCenterConfiguration;
import org.apache.shardingsphere.governance.repository.api.config.GovernanceConfiguration;
import org.apache.shardingsphere.scaling.core.config.ScalingConfiguration;
import org.apache.shardingsphere.scaling.core.config.ScalingContext;
import org.apache.shardingsphere.scaling.core.config.ServerConfiguration;
import org.apache.shardingsphere.scaling.core.config.datasource.ShardingSphereJDBCDataSourceConfiguration;
import org.apache.shardingsphere.scaling.core.constant.ScalingConstant;
import org.apache.shardingsphere.scaling.core.exception.ScalingJobNotFoundException;
import org.apache.shardingsphere.scaling.core.job.JobProgress;
import org.apache.shardingsphere.scaling.core.job.ScalingJob;
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
    public void assertStartWithScalingConfig() {
        Optional<ScalingJob> scalingJob = scalingJobService.start(mockScalingConfiguration());
        assertTrue(scalingJob.isPresent());
        assertTrue(registryRepository.get(ScalingTaskUtil.getScalingListenerPath(scalingJob.get().getJobId(), ScalingConstant.CONFIG)).contains("\"running\":true"));
    }
    
    @Test
    public void assertStartWithCallbackImmediately() {
        ScalingConfiguration scalingConfig = mockScalingConfiguration();
        ShardingSphereJDBCDataSourceConfiguration source = (ShardingSphereJDBCDataSourceConfiguration) scalingConfig.getRuleConfiguration().getSource().unwrap();
        RuleConfigurationsAlteredEvent event = new RuleConfigurationsAlteredEvent("schema", source.getDataSource(), source.getRule(), source.getRule(), "cacheId");
        Optional<ScalingJob> scalingJob = scalingJobService.start(event);
        assertFalse(scalingJob.isPresent());
    }
    
    @Test
    public void assertStartWithCallbackSuccess() throws IOException {
        ScalingConfiguration scalingConfig = ScalingConfigurationUtil.initConfig("/config_sharding_sphere_jdbc_target.json");
        ShardingSphereJDBCDataSourceConfiguration source = (ShardingSphereJDBCDataSourceConfiguration) scalingConfig.getRuleConfiguration().getSource().unwrap();
        ShardingSphereJDBCDataSourceConfiguration target = (ShardingSphereJDBCDataSourceConfiguration) scalingConfig.getRuleConfiguration().getTarget().unwrap();
        RuleConfigurationsAlteredEvent event = new RuleConfigurationsAlteredEvent(
                "schema", source.getDataSource(), source.getRule(), target.getDataSource(), target.getRule(), "cacheId");
        Optional<ScalingJob> scalingJob = scalingJobService.start(event);
        assertTrue(scalingJob.isPresent());
    }
    
    @Test
    public void assertStop() {
        Optional<ScalingJob> scalingJob = scalingJobService.start(mockScalingConfiguration());
        assertTrue(scalingJob.isPresent());
        scalingJobService.stop(scalingJob.get().getJobId());
        assertTrue(registryRepository.get(ScalingTaskUtil.getScalingListenerPath(scalingJob.get().getJobId(), ScalingConstant.CONFIG)).contains("\"running\":false"));
    }
    
    @Test(expected = ScalingJobNotFoundException.class)
    public void assertGetNotExistJob() {
        scalingJobService.getJob(0);
    }
    
    @Test
    public void assertGetProgress() {
        registryRepository.persist(ScalingTaskUtil.getScalingListenerPath("1/config"), new Gson().toJson(mockScalingConfiguration()));
        registryRepository.persist(ScalingTaskUtil.getScalingListenerPath("1/position/0/inventory"),
                "{'unfinished': {'ds1.table1#1':[0,100],'ds1.table1#2':[160,200],'ds1.table3':[]},'finished':['ds1.table2#1','ds1.table2#2']}");
        registryRepository.persist(ScalingTaskUtil.getScalingListenerPath("1/position/0/incremental"),
                "{'ds1':{'filename':binlog1,'position':4,'delay':1},'ds3':{'filename':binlog2,'position':4,'delay':3}}");
        registryRepository.persist(ScalingTaskUtil.getScalingListenerPath("1/position/1/inventory"),
                "{'unfinished': {'ds2.table1#1':[0,100],'ds2.table1#2':[160,200],'ds2.table3':[]},'finished':['ds2.table2#1','ds2.table2#2']}");
        registryRepository.persist(ScalingTaskUtil.getScalingListenerPath("1/position/1/incremental"),
                "{'ds2':{'filename':binlog1,'position':4,'delay':2},'ds4':{'filename':binlog2,'position':4,'delay':4}}");
        JobProgress actual = scalingJobService.getProgress(1);
        assertThat(actual.getInventoryTaskProgress().size(), is(2));
        assertThat(actual.getIncrementalTaskProgress().size(), is(4));
        assertThat(actual.getInventoryTaskProgress().get(0).getTotal(), is(5));
        assertThat(actual.getInventoryTaskProgress().get(0).getFinished(), is(2));
        assertThat(actual.getIncrementalTaskProgress().get(0).getDelayMillisecond(), is(1L));
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
        ServerConfiguration result = new ServerConfiguration();
        result.setDistributedScalingService(new GovernanceConfiguration("test", new GovernanceCenterConfiguration("REG_FIXTURE", "", null), false));
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
