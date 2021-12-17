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

package org.apache.shardingsphere.data.pipeline.api.impl;

import org.apache.shardingsphere.data.pipeline.api.config.ingest.DumperConfiguration;
import org.apache.shardingsphere.data.pipeline.api.config.ingest.InventoryDumperConfiguration;
import org.apache.shardingsphere.data.pipeline.api.config.rulealtered.TaskConfiguration;
import org.apache.shardingsphere.data.pipeline.api.config.server.ServerConfiguration;
import org.apache.shardingsphere.data.pipeline.api.ingest.position.PlaceholderPosition;
import org.apache.shardingsphere.data.pipeline.api.job.progress.JobProgress;
import org.apache.shardingsphere.data.pipeline.core.api.GovernanceRepositoryAPI;
import org.apache.shardingsphere.data.pipeline.core.api.PipelineAPIFactory;
import org.apache.shardingsphere.data.pipeline.core.constant.DataPipelineConstants;
import org.apache.shardingsphere.data.pipeline.core.fixture.EmbedTestingServer;
import org.apache.shardingsphere.data.pipeline.core.job.progress.yaml.JobProgressYamlSwapper;
import org.apache.shardingsphere.data.pipeline.core.task.IncrementalTask;
import org.apache.shardingsphere.data.pipeline.core.task.InventoryTask;
import org.apache.shardingsphere.data.pipeline.core.task.PipelineTaskFactory;
import org.apache.shardingsphere.data.pipeline.core.util.ReflectionUtil;
import org.apache.shardingsphere.data.pipeline.core.util.ResourceUtil;
import org.apache.shardingsphere.data.pipeline.scenario.rulealtered.RuleAlteredContext;
import org.apache.shardingsphere.data.pipeline.scenario.rulealtered.RuleAlteredJobContext;
import org.apache.shardingsphere.infra.config.mode.ModeConfiguration;
import org.apache.shardingsphere.infra.yaml.engine.YamlEngine;
import org.apache.shardingsphere.mode.repository.cluster.ClusterPersistRepositoryConfiguration;
import org.apache.shardingsphere.mode.repository.cluster.listener.DataChangedEvent;
import org.apache.shardingsphere.mode.repository.cluster.listener.DataChangedEvent.Type;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.hamcrest.CoreMatchers.anyOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public final class GovernanceRepositoryAPIImplTest {
    
    private static final JobProgressYamlSwapper JOB_PROGRESS_YAML_SWAPPER = new JobProgressYamlSwapper();
    
    private static GovernanceRepositoryAPI governanceRepositoryAPI;
    
    @BeforeClass
    public static void beforeClass() throws Exception {
        EmbedTestingServer.start();
        ReflectionUtil.setFieldValue(RuleAlteredContext.getInstance(), "serverConfig", mockServerConfig());
        governanceRepositoryAPI = PipelineAPIFactory.getGovernanceRepositoryAPI();
    }
    
    @Test
    public void assertPersistJobProgress() {
        RuleAlteredJobContext jobContext = mockJobContext();
        governanceRepositoryAPI.persistJobProgress(jobContext);
        JobProgress actual = governanceRepositoryAPI.getJobProgress(jobContext.getJobId(), jobContext.getShardingItem());
        assertThat(YamlEngine.marshal(JOB_PROGRESS_YAML_SWAPPER.swapToYaml(actual)), is(ResourceUtil.readFileAndIgnoreComments("governance-repository.yaml")));
    }
    
    @Test
    public void assertPersistJobCheckResult() {
        RuleAlteredJobContext jobContext = mockJobContext();
        governanceRepositoryAPI.persistJobCheckResult(jobContext.getJobId(), true);
        Optional<Boolean> checkResult = governanceRepositoryAPI.getJobCheckResult(jobContext.getJobId());
        assertTrue(checkResult.isPresent() && checkResult.get());
    }
    
    @Test
    public void assertDeleteJob() {
        governanceRepositoryAPI.persist(DataPipelineConstants.DATA_PIPELINE_ROOT + "/1", "");
        governanceRepositoryAPI.deleteJob(1L);
        JobProgress actual = governanceRepositoryAPI.getJobProgress(0L, 0);
        assertNull(actual);
    }
    
    @Test
    public void assertGetChildrenKeys() {
        governanceRepositoryAPI.persist(DataPipelineConstants.DATA_PIPELINE_ROOT + "/1", "");
        List<String> actual = governanceRepositoryAPI.getChildrenKeys(DataPipelineConstants.DATA_PIPELINE_ROOT);
        assertFalse(actual.isEmpty());
    }
    
    @Test
    public void assertWatch() throws InterruptedException {
        AtomicReference<DataChangedEvent> eventReference = new AtomicReference<>();
        CountDownLatch countDownLatch = new CountDownLatch(1);
        String key = DataPipelineConstants.DATA_PIPELINE_ROOT + "/1";
        governanceRepositoryAPI.watch(DataPipelineConstants.DATA_PIPELINE_ROOT, event -> {
            if (event.getKey().equals(key)) {
                eventReference.set(event);
                countDownLatch.countDown();
            }
        });
        governanceRepositoryAPI.persist(key, "");
        boolean awaitResult = countDownLatch.await(10, TimeUnit.SECONDS);
        assertTrue(awaitResult);
        DataChangedEvent event = eventReference.get();
        assertNotNull(event);
        assertThat(event.getType(), anyOf(is(Type.ADDED), is(Type.UPDATED)));
    }
    
    private static ServerConfiguration mockServerConfig() {
        ServerConfiguration result = new ServerConfiguration();
        result.setModeConfiguration(new ModeConfiguration("Cluster", new ClusterPersistRepositoryConfiguration("Zookeeper", "test", EmbedTestingServer.getConnectionString(), null), true));
        return result;
    }
    
    private RuleAlteredJobContext mockJobContext() {
        RuleAlteredJobContext result = new RuleAlteredJobContext(ResourceUtil.mockJobConfig());
        TaskConfiguration taskConfig = result.getTaskConfigs().iterator().next();
        result.getInventoryTasks().add(mockInventoryTask(taskConfig));
        result.getIncrementalTasks().add(mockIncrementalTask(taskConfig));
        return result;
    }
    
    private InventoryTask mockInventoryTask(final TaskConfiguration taskConfig) {
        InventoryDumperConfiguration dumperConfig = new InventoryDumperConfiguration(taskConfig.getDumperConfig());
        dumperConfig.setPosition(new PlaceholderPosition());
        dumperConfig.setTableName("t_order");
        dumperConfig.setPrimaryKey("order_id");
        dumperConfig.setShardingItem(0);
        return PipelineTaskFactory.createInventoryTask(dumperConfig, taskConfig.getImporterConfig());
    }
    
    private IncrementalTask mockIncrementalTask(final TaskConfiguration taskConfig) {
        DumperConfiguration dumperConfig = taskConfig.getDumperConfig();
        dumperConfig.setPosition(new PlaceholderPosition());
        return PipelineTaskFactory.createIncrementalTask(3, dumperConfig, taskConfig.getImporterConfig());
    }
}
