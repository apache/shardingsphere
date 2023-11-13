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

import org.apache.shardingsphere.data.pipeline.core.ingest.dumper.context.InventoryDumperContext;
import org.apache.shardingsphere.data.pipeline.core.ingest.dumper.Dumper;
import org.apache.shardingsphere.data.pipeline.common.metadata.node.PipelineNodePath;
import org.apache.shardingsphere.data.pipeline.common.ingest.position.PlaceholderPosition;
import org.apache.shardingsphere.data.pipeline.common.registrycenter.repository.GovernanceRepositoryAPI;
import org.apache.shardingsphere.data.pipeline.core.consistencycheck.result.TableDataConsistencyCheckResult;
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
import static org.junit.jupiter.api.Assertions.assertEquals;
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
        governanceRepositoryAPI.watch(PipelineNodePath.DATA_PIPELINE_ROOT, event -> {
            if ((PipelineNodePath.DATA_PIPELINE_ROOT + "/1").equals(event.getKey())) {
                EVENT_ATOMIC_REFERENCE.set(event);
                COUNT_DOWN_LATCH.countDown();
            }
        });
    }
    
    @Test
    void assertIsExisted() {
        String testKey = "/testKey1";
        assertFalse(governanceRepositoryAPI.isExisted(testKey));
        governanceRepositoryAPI.persist(testKey, "testValue1");
        assertTrue(governanceRepositoryAPI.isExisted(testKey));
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
        actual.put("test", new TableDataConsistencyCheckResult(true));
        governanceRepositoryAPI.persistCheckJobResult(jobItemContext.getJobId(), "j02123", actual);
        Map<String, TableDataConsistencyCheckResult> checkResult = governanceRepositoryAPI.getCheckJobResult(jobItemContext.getJobId(), "j02123");
        assertThat(checkResult.size(), is(1));
        assertTrue(checkResult.get("test").isMatched());
    }
    
    @Test
    void assertDeleteJob() {
        governanceRepositoryAPI.persist(PipelineNodePath.DATA_PIPELINE_ROOT + "/1", "");
        governanceRepositoryAPI.deleteJob("1");
        Optional<String> actual = governanceRepositoryAPI.getJobItemProgress("1", 0);
        assertFalse(actual.isPresent());
    }
    
    @Test
    void assertGetChildrenKeys() {
        governanceRepositoryAPI.persist(PipelineNodePath.DATA_PIPELINE_ROOT + "/1", "");
        List<String> actual = governanceRepositoryAPI.getChildrenKeys(PipelineNodePath.DATA_PIPELINE_ROOT);
        assertFalse(actual.isEmpty());
        assertTrue(actual.contains("1"));
    }
    
    @Test
    void assertWatch() throws InterruptedException {
        String key = PipelineNodePath.DATA_PIPELINE_ROOT + "/1";
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
    
    @Test
    void assertPersistJobOffsetInfo() {
        assertFalse(governanceRepositoryAPI.getJobOffsetInfo("1").isPresent());
        governanceRepositoryAPI.persistJobOffsetInfo("1", "testValue");
        Optional<String> actual = governanceRepositoryAPI.getJobOffsetInfo("1");
        assertTrue(actual.isPresent());
        assertThat(actual.get(), is("testValue"));
    }
    
    @Test
    void assertLatestCheckJobIdPersistenceDeletion() {
        String parentJobId = "testParentJob";
        String expectedCheckJobId = "testCheckJob";
        governanceRepositoryAPI.persistLatestCheckJobId(parentJobId, expectedCheckJobId);
        Optional<String> actualCheckJobIdOpt = governanceRepositoryAPI.getLatestCheckJobId(parentJobId);
        assertTrue(actualCheckJobIdOpt.isPresent(), "Expected a checkJobId to be present");
        assertEquals(expectedCheckJobId, actualCheckJobIdOpt.get(), "The retrieved checkJobId does not match the expected one");
        governanceRepositoryAPI.deleteLatestCheckJobId(parentJobId);
        assertFalse(governanceRepositoryAPI.getLatestCheckJobId(parentJobId).isPresent(), "Expected no checkJobId to be present after deletion");
    }
    
    private MigrationJobItemContext mockJobItemContext() {
        MigrationJobItemContext result = PipelineContextUtils.mockMigrationJobItemContext(JobConfigurationBuilder.createJobConfiguration());
        MigrationTaskConfiguration taskConfig = result.getTaskConfig();
        result.getInventoryTasks().add(mockInventoryTask(taskConfig));
        return result;
    }
    
    private InventoryTask mockInventoryTask(final MigrationTaskConfiguration taskConfig) {
        InventoryDumperContext dumperContext = new InventoryDumperContext(taskConfig.getDumperContext().getCommonContext());
        dumperContext.getCommonContext().setPosition(new PlaceholderPosition());
        dumperContext.setActualTableName("t_order");
        dumperContext.setLogicTableName("t_order");
        dumperContext.setUniqueKeyColumns(Collections.singletonList(PipelineContextUtils.mockOrderIdColumnMetaData()));
        dumperContext.setShardingItem(0);
        return new InventoryTask(PipelineTaskUtils.generateInventoryTaskId(dumperContext), PipelineContextUtils.getExecuteEngine(), PipelineContextUtils.getExecuteEngine(),
                mock(Dumper.class), mock(Importer.class), new AtomicReference<>(new PlaceholderPosition()));
    }
}
