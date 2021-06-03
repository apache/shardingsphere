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

package org.apache.shardingsphere.scaling.core.api.impl;

import org.apache.shardingsphere.governance.repository.api.config.RegistryCenterConfiguration;
import org.apache.shardingsphere.governance.repository.api.config.GovernanceConfiguration;
import org.apache.shardingsphere.governance.repository.api.listener.DataChangedEvent;
import org.apache.shardingsphere.scaling.core.api.GovernanceRepositoryAPI;
import org.apache.shardingsphere.scaling.core.api.ScalingAPIFactory;
import org.apache.shardingsphere.scaling.core.common.constant.ScalingConstant;
import org.apache.shardingsphere.scaling.core.config.DumperConfiguration;
import org.apache.shardingsphere.scaling.core.config.InventoryDumperConfiguration;
import org.apache.shardingsphere.scaling.core.config.ScalingContext;
import org.apache.shardingsphere.scaling.core.config.ServerConfiguration;
import org.apache.shardingsphere.scaling.core.config.TaskConfiguration;
import org.apache.shardingsphere.scaling.core.fixture.EmbedTestingServer;
import org.apache.shardingsphere.scaling.core.job.JobContext;
import org.apache.shardingsphere.scaling.core.job.position.PlaceholderPosition;
import org.apache.shardingsphere.scaling.core.job.progress.JobProgress;
import org.apache.shardingsphere.scaling.core.job.task.ScalingTaskFactory;
import org.apache.shardingsphere.scaling.core.job.task.incremental.IncrementalTask;
import org.apache.shardingsphere.scaling.core.job.task.inventory.InventoryTask;
import org.apache.shardingsphere.scaling.core.util.ReflectionUtil;
import org.apache.shardingsphere.scaling.core.util.ResourceUtil;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.List;
import java.util.concurrent.CountDownLatch;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

public final class GovernanceRepositoryAPIImplTest {
    
    private static GovernanceRepositoryAPI governanceRepositoryAPI;
    
    @BeforeClass
    public static void beforeClass() throws Exception {
        EmbedTestingServer.start();
        ReflectionUtil.setFieldValue(ScalingContext.getInstance(), "serverConfig", mockServerConfig());
        governanceRepositoryAPI = ScalingAPIFactory.getGovernanceRepositoryAPI();
    }
    
    @Test
    public void assertPersistJobProgress() {
        JobContext jobContext = mockJobContext();
        governanceRepositoryAPI.persistJobProgress(jobContext);
        JobProgress actual = governanceRepositoryAPI.getJobProgress(jobContext.getJobId(), jobContext.getShardingItem());
        assertThat(actual.toString(), is(mockYamlJobProgress()));
    }
    
    @Test
    public void assertDeleteJob() {
        governanceRepositoryAPI.persist(ScalingConstant.SCALING_ROOT + "/1", "");
        governanceRepositoryAPI.deleteJob(1L);
        JobProgress actual = governanceRepositoryAPI.getJobProgress(0L, 0);
        assertNull(actual);
    }
    
    @Test
    public void assertGetChildrenKeys() {
        governanceRepositoryAPI.persist(ScalingConstant.SCALING_ROOT + "/1", "");
        List<String> actual = governanceRepositoryAPI.getChildrenKeys(ScalingConstant.SCALING_ROOT);
        assertFalse(actual.isEmpty());
    }
    
    @Test
    public void assertWatch() throws InterruptedException {
        CountDownLatch countDownLatch = new CountDownLatch(1);
        String key = ScalingConstant.SCALING_ROOT + "/1";
        governanceRepositoryAPI.persist(key, "");
        governanceRepositoryAPI.watch(ScalingConstant.SCALING_ROOT, event -> {
            if (event.getKey().equals(key)) {
                assertThat(event.getType(), is(DataChangedEvent.Type.ADDED));
                countDownLatch.countDown();
            }
        });
        countDownLatch.await();
    }
    
    private static ServerConfiguration mockServerConfig() {
        ServerConfiguration result = new ServerConfiguration();
        result.setGovernanceConfig(new GovernanceConfiguration("test", new RegistryCenterConfiguration("Zookeeper", EmbedTestingServer.getConnectionString(), null), true));
        return result;
    }
    
    private JobContext mockJobContext() {
        JobContext result = new JobContext(ResourceUtil.mockJobConfig());
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
        return ScalingTaskFactory.createInventoryTask(dumperConfig, taskConfig.getImporterConfig());
    }
    
    private IncrementalTask mockIncrementalTask(final TaskConfiguration taskConfig) {
        DumperConfiguration dumperConfig = taskConfig.getDumperConfig();
        dumperConfig.setPosition(new PlaceholderPosition());
        return ScalingTaskFactory.createIncrementalTask(3, dumperConfig, taskConfig.getImporterConfig());
    }
    
    private String mockYamlJobProgress() {
        return "databaseType: H2\n"
                + "incremental:\n"
                + "  ds_0:\n"
                + "    delay:\n"
                + "      delayMilliseconds: -1\n"
                + "      lastEventTimestamps: 0\n"
                + "    position: ''\n"
                + "inventory:\n"
                + "  unfinished:\n"
                + "    ds_0.t_order#0: ''\n"
                + "status: RUNNING\n";
    }
}
