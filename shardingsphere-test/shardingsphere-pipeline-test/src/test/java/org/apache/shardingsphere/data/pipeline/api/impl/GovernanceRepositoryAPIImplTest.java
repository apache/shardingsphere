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

import org.apache.shardingsphere.data.pipeline.api.check.consistency.DataConsistencyCheckResult;
import org.apache.shardingsphere.data.pipeline.api.check.consistency.DataConsistencyContentCheckResult;
import org.apache.shardingsphere.data.pipeline.api.check.consistency.DataConsistencyCountCheckResult;
import org.apache.shardingsphere.data.pipeline.api.config.ingest.DumperConfiguration;
import org.apache.shardingsphere.data.pipeline.api.config.ingest.InventoryDumperConfiguration;
import org.apache.shardingsphere.data.pipeline.api.datasource.PipelineDataSourceWrapper;
import org.apache.shardingsphere.data.pipeline.api.ingest.position.PlaceholderPosition;
import org.apache.shardingsphere.data.pipeline.api.metadata.loader.PipelineTableMetaDataLoader;
import org.apache.shardingsphere.data.pipeline.core.api.GovernanceRepositoryAPI;
import org.apache.shardingsphere.data.pipeline.core.api.PipelineAPIFactory;
import org.apache.shardingsphere.data.pipeline.core.constant.DataPipelineConstants;
import org.apache.shardingsphere.data.pipeline.core.datasource.DefaultPipelineDataSourceManager;
import org.apache.shardingsphere.data.pipeline.core.fixture.FixturePipelineJobProgressListener;
import org.apache.shardingsphere.data.pipeline.core.metadata.loader.StandardPipelineTableMetaDataLoader;
import org.apache.shardingsphere.data.pipeline.core.task.IncrementalTask;
import org.apache.shardingsphere.data.pipeline.core.task.InventoryTask;
import org.apache.shardingsphere.data.pipeline.core.util.JobConfigurationBuilder;
import org.apache.shardingsphere.data.pipeline.core.util.PipelineContextUtil;
import org.apache.shardingsphere.data.pipeline.scenario.migration.MigrationJobItemContext;
import org.apache.shardingsphere.data.pipeline.scenario.migration.MigrationTaskConfiguration;
import org.apache.shardingsphere.mode.repository.cluster.listener.DataChangedEvent;
import org.apache.shardingsphere.mode.repository.cluster.listener.DataChangedEvent.Type;
import org.junit.BeforeClass;
import org.junit.Test;

import java.sql.Types;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.hamcrest.CoreMatchers.anyOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

public final class GovernanceRepositoryAPIImplTest {
    
    private static GovernanceRepositoryAPI governanceRepositoryAPI;
    
    @BeforeClass
    public static void beforeClass() {
        PipelineContextUtil.mockModeConfigAndContextManager();
        governanceRepositoryAPI = PipelineAPIFactory.getGovernanceRepositoryAPI();
    }
    
    @Test
    public void assertPersistJobProgress() {
        MigrationJobItemContext jobItemContext = mockJobItemContext();
        governanceRepositoryAPI.persistJobItemProgress(jobItemContext.getJobId(), jobItemContext.getShardingItem(), "testValue");
        String actual = governanceRepositoryAPI.getJobItemProgress(jobItemContext.getJobId(), jobItemContext.getShardingItem());
        assertThat(actual, is("testValue"));
    }
    
    @Test
    public void assertPersistJobCheckResult() {
        MigrationJobItemContext jobItemContext = mockJobItemContext();
        Map<String, DataConsistencyCheckResult> actual = new HashMap<>();
        actual.put("test", new DataConsistencyCheckResult(new DataConsistencyCountCheckResult(1, 1), new DataConsistencyContentCheckResult(true)));
        governanceRepositoryAPI.persistCheckJobResult(jobItemContext.getJobId(), "j02123", actual);
        Map<String, DataConsistencyCheckResult> checkResult = governanceRepositoryAPI.getCheckJobResult(jobItemContext.getJobId(), "j02123");
        assertThat(checkResult.size(), is(1));
        assertTrue(checkResult.get("test").getContentCheckResult().isMatched());
    }
    
    @Test
    public void assertDeleteJob() {
        governanceRepositoryAPI.persist(DataPipelineConstants.DATA_PIPELINE_ROOT + "/1", "");
        governanceRepositoryAPI.deleteJob("1");
        String actual = governanceRepositoryAPI.getJobItemProgress("1", 0);
        assertNull(actual);
    }
    
    @Test
    public void assertGetChildrenKeys() {
        governanceRepositoryAPI.persist(DataPipelineConstants.DATA_PIPELINE_ROOT + "/1", "");
        List<String> actual = governanceRepositoryAPI.getChildrenKeys(DataPipelineConstants.DATA_PIPELINE_ROOT);
        assertFalse(actual.isEmpty());
        assertTrue(actual.contains("1"));
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
    
    @Test
    public void assertGetShardingItems() {
        MigrationJobItemContext jobItemContext = mockJobItemContext();
        governanceRepositoryAPI.persistJobItemProgress(jobItemContext.getJobId(), jobItemContext.getShardingItem(), "testValue");
        List<Integer> shardingItems = governanceRepositoryAPI.getShardingItems(jobItemContext.getJobId());
        assertThat(shardingItems.size(), is(1));
        assertThat(shardingItems.get(0), is(jobItemContext.getShardingItem()));
    }
    
    private MigrationJobItemContext mockJobItemContext() {
        MigrationJobItemContext result = PipelineContextUtil.mockMigrationJobItemContext(JobConfigurationBuilder.createJobConfiguration());
        MigrationTaskConfiguration taskConfig = result.getTaskConfig();
        result.getInventoryTasks().add(mockInventoryTask(taskConfig));
        result.getIncrementalTasks().add(mockIncrementalTask(taskConfig));
        return result;
    }
    
    private InventoryTask mockInventoryTask(final MigrationTaskConfiguration taskConfig) {
        InventoryDumperConfiguration dumperConfig = new InventoryDumperConfiguration(taskConfig.getDumperConfig());
        dumperConfig.setPosition(new PlaceholderPosition());
        dumperConfig.setActualTableName("t_order");
        dumperConfig.setLogicTableName("t_order");
        dumperConfig.setUniqueKey("order_id");
        dumperConfig.setUniqueKeyDataType(Types.INTEGER);
        dumperConfig.setShardingItem(0);
        PipelineDataSourceWrapper dataSource = mock(PipelineDataSourceWrapper.class);
        PipelineTableMetaDataLoader metaDataLoader = new StandardPipelineTableMetaDataLoader(dataSource);
        return new InventoryTask(dumperConfig, taskConfig.getImporterConfig(), PipelineContextUtil.getPipelineChannelCreator(), new DefaultPipelineDataSourceManager(),
                dataSource, metaDataLoader, PipelineContextUtil.getExecuteEngine(), PipelineContextUtil.getExecuteEngine(), new FixturePipelineJobProgressListener());
    }
    
    private IncrementalTask mockIncrementalTask(final MigrationTaskConfiguration taskConfig) {
        DumperConfiguration dumperConfig = taskConfig.getDumperConfig();
        dumperConfig.setPosition(new PlaceholderPosition());
        PipelineTableMetaDataLoader metaDataLoader = new StandardPipelineTableMetaDataLoader(mock(PipelineDataSourceWrapper.class));
        return new IncrementalTask(3, dumperConfig, taskConfig.getImporterConfig(), PipelineContextUtil.getPipelineChannelCreator(), new DefaultPipelineDataSourceManager(),
                metaDataLoader, PipelineContextUtil.getExecuteEngine(), PipelineContextUtil.getExecuteEngine(), new FixturePipelineJobProgressListener());
    }
}
