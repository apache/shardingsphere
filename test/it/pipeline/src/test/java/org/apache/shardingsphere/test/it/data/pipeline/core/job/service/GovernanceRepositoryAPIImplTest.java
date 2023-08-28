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

package org.apache.shardingsphere.test.it.data.pipeline.core.job.service;

import org.apache.shardingsphere.data.pipeline.api.config.ingest.InventoryDumperConfiguration;
import org.apache.shardingsphere.data.pipeline.api.ingest.dumper.Dumper;
import org.apache.shardingsphere.data.pipeline.common.constant.DataPipelineConstants;
import org.apache.shardingsphere.data.pipeline.common.ingest.position.PlaceholderPosition;
import org.apache.shardingsphere.data.pipeline.common.registrycenter.repository.GovernanceRepositoryAPI;
import org.apache.shardingsphere.data.pipeline.core.consistencycheck.result.TableDataConsistencyCheckResult;
import org.apache.shardingsphere.data.pipeline.core.consistencycheck.result.TableDataConsistencyContentCheckResult;
import org.apache.shardingsphere.data.pipeline.core.consistencycheck.result.TableDataConsistencyCountCheckResult;
import org.apache.shardingsphere.data.pipeline.core.importer.Importer;
import org.apache.shardingsphere.data.pipeline.core.job.service.PipelineAPIFactory;
import org.apache.shardingsphere.data.pipeline.core.task.InventoryTask;
import org.apache.shardingsphere.data.pipeline.core.task.PipelineTaskUtils;
import org.apache.shardingsphere.data.pipeline.scenario.migration.config.MigrationTaskConfiguration;
import org.apache.shardingsphere.data.pipeline.scenario.migration.context.MigrationJobItemContext;
import org.apache.shardingsphere.mode.event.DataChangedEvent;
import org.apache.shardingsphere.mode.event.DataChangedEvent.Type;
import org.apache.shardingsphere.test.it.data.pipeline.core.util.JobConfigurationBuilder;
import org.apache.shardingsphere.test.it.data.pipeline.core.util.PipelineContextUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.hamcrest.CoreMatchers.anyOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

class GovernanceRepositoryAPIImplTest {
    
    private static GovernanceRepositoryAPI governanceRepositoryAPI;
    
    private static final AtomicReference<DataChangedEvent> EVENT_ATOMIC_REFERENCE = new AtomicReference<>();
    
    private static final CountDownLatch COUNT_DOWN_LATCH = new CountDownLatch(1);
    
    @BeforeAll
    static void beforeClass() {
        PipelineContextUtils.mockModeConfigAndContextManager();
        governanceRepositoryAPI = PipelineAPIFactory.getGovernanceRepositoryAPI(PipelineContextUtils.getContextKey());
        watch();
    }
    
    private static void watch() {
        governanceRepositoryAPI.watch(DataPipelineConstants.DATA_PIPELINE_ROOT, event -> {
            if ((DataPipelineConstants.DATA_PIPELINE_ROOT + "/1").equals(event.getKey())) {
                EVENT_ATOMIC_REFERENCE.set(event);
                COUNT_DOWN_LATCH.countDown();
            }
        });
    }
    
    @Test
    void assertPersistJobItemProgress() {
        MigrationJobItemContext jobItemContext = mockJobItemContext();
        governanceRepositoryAPI.updateJobItemProgress(jobItemContext.getJobId(), jobItemContext.getShardingItem(), "testValue1");
        assertFalse(governanceRepositoryAPI.getJobItemProgress(jobItemContext.getJobId(), jobItemContext.getShardingItem()).isPresent());
        governanceRepositoryAPI.persistJobItemProgress(jobItemContext.getJobId(), jobItemContext.getShardingItem(), "testValue1");
        Optional<String> actual = governanceRepositoryAPI.getJobItemProgress(jobItemContext.getJobId(), jobItemContext.getShardingItem());
        assertTrue(actual.isPresent());
        assertThat(actual.get(), is("testValue1"));
        governanceRepositoryAPI.updateJobItemProgress(jobItemContext.getJobId(), jobItemContext.getShardingItem(), "testValue2");
        actual = governanceRepositoryAPI.getJobItemProgress(jobItemContext.getJobId(), jobItemContext.getShardingItem());
        assertTrue(actual.isPresent());
        assertThat(actual.get(), is("testValue2"));
    }
    
    @Test
    void assertPersistJobCheckResult() {
        MigrationJobItemContext jobItemContext = mockJobItemContext();
        Map<String, TableDataConsistencyCheckResult> actual = new HashMap<>();
        actual.put("test", new TableDataConsistencyCheckResult(new TableDataConsistencyCountCheckResult(1, 1), new TableDataConsistencyContentCheckResult(true)));
        governanceRepositoryAPI.persistCheckJobResult(jobItemContext.getJobId(), "j02123", actual);
        Map<String, TableDataConsistencyCheckResult> checkResult = governanceRepositoryAPI.getCheckJobResult(jobItemContext.getJobId(), "j02123");
        assertThat(checkResult.size(), is(1));
        assertTrue(checkResult.get("test").getContentCheckResult().isMatched());
    }
    
    @Test
    void assertDeleteJob() {
        governanceRepositoryAPI.persist(DataPipelineConstants.DATA_PIPELINE_ROOT + "/1", "");
        governanceRepositoryAPI.deleteJob("1");
        Optional<String> actual = governanceRepositoryAPI.getJobItemProgress("1", 0);
        assertFalse(actual.isPresent());
    }
    
    @Test
    void assertGetChildrenKeys() {
        governanceRepositoryAPI.persist(DataPipelineConstants.DATA_PIPELINE_ROOT + "/1", "");
        List<String> actual = governanceRepositoryAPI.getChildrenKeys(DataPipelineConstants.DATA_PIPELINE_ROOT);
        assertFalse(actual.isEmpty());
        assertTrue(actual.contains("1"));
    }
    
    @Test
    void assertWatch() throws InterruptedException {
        String key = DataPipelineConstants.DATA_PIPELINE_ROOT + "/1";
        governanceRepositoryAPI.persist(key, "");
        boolean awaitResult = COUNT_DOWN_LATCH.await(10, TimeUnit.SECONDS);
        assertTrue(awaitResult);
        DataChangedEvent event = EVENT_ATOMIC_REFERENCE.get();
        assertNotNull(event);
        assertThat(event.getType(), anyOf(is(Type.ADDED), is(Type.UPDATED)));
    }
    
    @Test
    void assertGetShardingItems() {
        MigrationJobItemContext jobItemContext = mockJobItemContext();
        governanceRepositoryAPI.persistJobItemProgress(jobItemContext.getJobId(), jobItemContext.getShardingItem(), "testValue");
        List<Integer> shardingItems = governanceRepositoryAPI.getShardingItems(jobItemContext.getJobId());
        assertThat(shardingItems.size(), is(1));
        assertThat(shardingItems.get(0), is(jobItemContext.getShardingItem()));
    }
    
    private MigrationJobItemContext mockJobItemContext() {
        MigrationJobItemContext result = PipelineContextUtils.mockMigrationJobItemContext(JobConfigurationBuilder.createJobConfiguration());
        MigrationTaskConfiguration taskConfig = result.getTaskConfig();
        result.getInventoryTasks().add(mockInventoryTask(taskConfig));
        return result;
    }
    
    private InventoryTask mockInventoryTask(final MigrationTaskConfiguration taskConfig) {
        InventoryDumperConfiguration dumperConfig = new InventoryDumperConfiguration(taskConfig.getDumperConfig());
        dumperConfig.setPosition(new PlaceholderPosition());
        dumperConfig.setActualTableName("t_order");
        dumperConfig.setLogicTableName("t_order");
        dumperConfig.setUniqueKeyColumns(Collections.singletonList(PipelineContextUtils.mockOrderIdColumnMetaData()));
        dumperConfig.setShardingItem(0);
        return new InventoryTask(PipelineTaskUtils.generateInventoryTaskId(dumperConfig), PipelineContextUtils.getExecuteEngine(), PipelineContextUtils.getExecuteEngine(),
                mock(Dumper.class), mock(Importer.class), new AtomicReference<>(new PlaceholderPosition()));
    }
}
