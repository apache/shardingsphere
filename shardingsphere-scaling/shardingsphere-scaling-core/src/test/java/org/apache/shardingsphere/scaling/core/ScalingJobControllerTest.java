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

package org.apache.shardingsphere.scaling.core;

import org.apache.shardingsphere.scaling.core.config.DataSourceConfiguration;
import org.apache.shardingsphere.scaling.core.config.DumperConfiguration;
import org.apache.shardingsphere.scaling.core.config.ImporterConfiguration;
import org.apache.shardingsphere.scaling.core.config.JDBCDataSourceConfiguration;
import org.apache.shardingsphere.scaling.core.config.ScalingContext;
import org.apache.shardingsphere.scaling.core.config.ServerConfiguration;
import org.apache.shardingsphere.scaling.core.config.SyncConfiguration;
import org.apache.shardingsphere.scaling.core.exception.ScalingJobNotFoundException;
import org.apache.shardingsphere.scaling.core.job.ScalingJobProgress;
import org.apache.shardingsphere.scaling.core.job.ShardingScalingJob;
import org.apache.shardingsphere.scaling.core.job.SyncProgress;
import org.apache.shardingsphere.scaling.core.schedule.SyncTaskControlStatus;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public final class ScalingJobControllerTest {
    
    private static final String DATA_SOURCE_URL = "jdbc:h2:mem:test_db;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false;MODE=MySQL";
    
    private static final String USERNAME = "root";
    
    private static final String PASSWORD = "password";
    
    private ScalingJobController scalingJobController;
    
    private ShardingScalingJob shardingScalingJob;
    
    @Before
    public void setUp() {
        ScalingContext.getInstance().init(mockServerConfiguration());
        scalingJobController = new ScalingJobController();
        shardingScalingJob = mockShardingScalingJob();
        scalingJobController.start(shardingScalingJob);
    }
    
    private ServerConfiguration mockServerConfiguration() {
        ServerConfiguration result = new ServerConfiguration();
        result.setBlockQueueSize(1000);
        result.setPort(8080);
        result.setPushTimeout(1000);
        result.setWorkerThread(30);
        return result;
    }
    
    @Test
    public void assertStartPreparedJob() {
        SyncProgress progress = scalingJobController.getProgresses(shardingScalingJob.getJobId());
        assertTrue(progress instanceof ScalingJobProgress);
        assertThat(((ScalingJobProgress) progress).getId(), is(shardingScalingJob.getJobId()));
        assertThat(((ScalingJobProgress) progress).getJobName(), is(shardingScalingJob.getJobName()));
        assertThat(((ScalingJobProgress) progress).getIncrementalDataTasks().size(), is(1));
        assertThat(((ScalingJobProgress) progress).getInventoryDataTasks().size(), is(1));
    }
    
    @Test
    public void assertStartPreparingFailureJob() {
        ShardingScalingJob shardingScalingJob = mockPreparingFailureShardingScalingJob();
        scalingJobController.start(shardingScalingJob);
        SyncProgress progress = scalingJobController.getProgresses(shardingScalingJob.getJobId());
        assertTrue(progress instanceof ScalingJobProgress);
        assertThat(((ScalingJobProgress) progress).getIncrementalDataTasks().size(), is(0));
        assertThat(((ScalingJobProgress) progress).getInventoryDataTasks().size(), is(0));
    }
    
    @Test
    public void assertStopExistJob() {
        scalingJobController.stop(shardingScalingJob.getJobId());
        SyncProgress progress = scalingJobController.getProgresses(shardingScalingJob.getJobId());
        assertTrue(progress instanceof ScalingJobProgress);
        assertThat(((ScalingJobProgress) progress).getStatus(), not("RUNNING"));
    }
    
    @Test(expected = ScalingJobNotFoundException.class)
    public void assertStopNotExistJob() {
        scalingJobController.stop(9);
        scalingJobController.getProgresses(9);
    }
    
    @Test
    public void assertListShardingScalingJobs() {
        List<ShardingScalingJob> shardingScalingJobs = scalingJobController.listShardingScalingJobs();
        assertThat(shardingScalingJobs.size(), is(1));
    }
    
    private ShardingScalingJob mockShardingScalingJob() {
        ShardingScalingJob result = new ShardingScalingJob("ScalingJob", 0);
        result.getSyncConfigurations().add(new SyncConfiguration(3, mockDumperConfig(), mockImporterConfiguration()));
        return result;
    }
    
    private ShardingScalingJob mockPreparingFailureShardingScalingJob() {
        ShardingScalingJob result = new ShardingScalingJob("ScalingJob", 0);
        result.getSyncConfigurations().add(new SyncConfiguration(3, mockDumperConfig(), mockImporterConfiguration()));
        result.setStatus(SyncTaskControlStatus.PREPARING_FAILURE.name());
        return result;
    }
    
    private ImporterConfiguration mockImporterConfiguration() {
        ImporterConfiguration importerConfig = new ImporterConfiguration();
        importerConfig.setDataSourceConfiguration(new JDBCDataSourceConfiguration(DATA_SOURCE_URL, USERNAME, PASSWORD));
        return importerConfig;
    }
    
    private DumperConfiguration mockDumperConfig() {
        DataSourceConfiguration dataSourceConfiguration = new JDBCDataSourceConfiguration(DATA_SOURCE_URL, USERNAME, PASSWORD);
        DumperConfiguration result = new DumperConfiguration();
        result.setDataSourceName("ds0");
        result.setDataSourceConfiguration(dataSourceConfiguration);
        Map<String, String> tableMap = new HashMap<>();
        tableMap.put("t_order", "t_order");
        result.setTableNameMap(tableMap);
        return result;
    }
}
