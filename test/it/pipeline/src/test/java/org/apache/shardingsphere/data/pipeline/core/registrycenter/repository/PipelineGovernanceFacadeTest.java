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

package org.apache.shardingsphere.data.pipeline.core.registrycenter.repository;

import org.apache.shardingsphere.data.pipeline.core.consistencycheck.result.TableDataConsistencyCheckResult;
import org.apache.shardingsphere.data.pipeline.core.context.PipelineContextManager;
import org.apache.shardingsphere.data.pipeline.core.importer.Importer;
import org.apache.shardingsphere.data.pipeline.core.ingest.dumper.Dumper;
import org.apache.shardingsphere.data.pipeline.core.ingest.dumper.inventory.InventoryDumperContext;
import org.apache.shardingsphere.data.pipeline.core.ingest.position.type.placeholder.IngestPlaceholderPosition;
import org.apache.shardingsphere.data.pipeline.core.job.api.PipelineAPIFactory;
import org.apache.shardingsphere.data.pipeline.core.job.progress.JobOffsetInfo;
import org.apache.shardingsphere.data.pipeline.core.metadata.node.PipelineNodePath;
import org.apache.shardingsphere.data.pipeline.core.registrycenter.repository.item.PipelineJobItemErrorMessageGovernanceRepository;
import org.apache.shardingsphere.data.pipeline.core.registrycenter.repository.item.PipelineJobItemProcessGovernanceRepository;
import org.apache.shardingsphere.data.pipeline.core.task.InventoryTask;
import org.apache.shardingsphere.data.pipeline.core.task.PipelineTaskUtils;
import org.apache.shardingsphere.data.pipeline.scenario.migration.config.MigrationTaskConfiguration;
import org.apache.shardingsphere.data.pipeline.scenario.migration.context.MigrationJobItemContext;
import org.apache.shardingsphere.elasticjob.infra.pojo.JobConfigurationPOJO;
import org.apache.shardingsphere.infra.util.yaml.YamlEngine;
import org.apache.shardingsphere.mode.event.DataChangedEvent;
import org.apache.shardingsphere.mode.event.DataChangedEvent.Type;
import org.apache.shardingsphere.mode.repository.cluster.ClusterPersistRepository;
import org.apache.shardingsphere.test.it.data.pipeline.core.util.JobConfigurationBuilder;
import org.apache.shardingsphere.test.it.data.pipeline.core.util.PipelineContextUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
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

class PipelineGovernanceFacadeTest {
    
    private static PipelineGovernanceFacade governanceFacade;
    
    private static final AtomicReference<DataChangedEvent> EVENT_ATOMIC_REFERENCE = new AtomicReference<>();
    
    private static final CountDownLatch COUNT_DOWN_LATCH = new CountDownLatch(1);
    
    @BeforeAll
    static void beforeClass() {
        PipelineContextUtils.initPipelineContextManager();
        governanceFacade = PipelineAPIFactory.getPipelineGovernanceFacade(PipelineContextUtils.getContextKey());
        watch();
    }
    
    private static void watch() {
        governanceFacade.watchPipeLineRootPath(event -> {
            if ((PipelineNodePath.DATA_PIPELINE_ROOT + "/1").equals(event.getKey())) {
                EVENT_ATOMIC_REFERENCE.set(event);
                COUNT_DOWN_LATCH.countDown();
            }
        });
    }
    
    @Test
    void assertWatch() throws InterruptedException {
        String key = PipelineNodePath.DATA_PIPELINE_ROOT + "/1";
        getClusterPersistRepository().persist(key, "");
        boolean awaitResult = COUNT_DOWN_LATCH.await(10L, TimeUnit.SECONDS);
        assertTrue(awaitResult);
        DataChangedEvent event = EVENT_ATOMIC_REFERENCE.get();
        assertNotNull(event);
        assertThat(event.getType(), anyOf(is(Type.ADDED), is(Type.UPDATED)));
    }
    
    @Test
    void assertDeleteJob() {
        ClusterPersistRepository clusterPersistRepository = getClusterPersistRepository();
        clusterPersistRepository.persist(PipelineNodePath.DATA_PIPELINE_ROOT + "/1", "");
        governanceFacade.getJobFacade().getJob().delete("1");
        Optional<String> actual = new PipelineJobItemProcessGovernanceRepository(clusterPersistRepository).load("1", 0);
        assertFalse(actual.isPresent());
    }
    
    @Test
    void assertIsExistedJobConfiguration() {
        assertFalse(governanceFacade.getJobFacade().getConfiguration().isExisted("foo_job"));
        JobConfigurationPOJO value = new JobConfigurationPOJO();
        value.setJobName("foo_job");
        value.setShardingTotalCount(1);
        ClusterPersistRepository clusterPersistRepository = getClusterPersistRepository();
        clusterPersistRepository.persist("/pipeline/jobs/foo_job/config", YamlEngine.marshal(value));
        assertTrue(governanceFacade.getJobFacade().getConfiguration().isExisted("foo_job"));
        clusterPersistRepository.delete("/pipeline/jobs/foo_job/config");
        assertFalse(governanceFacade.getJobFacade().getConfiguration().isExisted("foo_job"));
    }
    
    @Test
    void assertLatestCheckJobIdPersistenceDeletion() {
        String parentJobId = "testParentJob";
        String expectedCheckJobId = "testCheckJob";
        governanceFacade.getJobFacade().getCheck().persistLatestCheckJobId(parentJobId, expectedCheckJobId);
        Optional<String> actualCheckJobIdOpt = governanceFacade.getJobFacade().getCheck().findLatestCheckJobId(parentJobId);
        assertTrue(actualCheckJobIdOpt.isPresent());
        assertThat(actualCheckJobIdOpt.get(), is(expectedCheckJobId));
        governanceFacade.getJobFacade().getCheck().deleteLatestCheckJobId(parentJobId);
        assertFalse(governanceFacade.getJobFacade().getCheck().findLatestCheckJobId(parentJobId).isPresent());
    }
    
    @Test
    void assertInitCheckJobResult() {
        MigrationJobItemContext jobItemContext = mockJobItemContext();
        String checkJobId = "j02101";
        governanceFacade.getJobFacade().getCheck().initCheckJobResult(jobItemContext.getJobId(), checkJobId);
        Collection<String> actualCheckJobIds = governanceFacade.getJobFacade().getCheck().listCheckJobIds(jobItemContext.getJobId());
        assertThat(actualCheckJobIds.size(), is(1));
        assertThat(actualCheckJobIds.iterator().next(), is(checkJobId));
        governanceFacade.getJobFacade().getCheck().deleteCheckJobResult(jobItemContext.getJobId(), checkJobId);
        assertTrue(governanceFacade.getJobFacade().getCheck().listCheckJobIds(jobItemContext.getJobId()).isEmpty());
    }
    
    @Test
    void assertPersistJobCheckResult() {
        MigrationJobItemContext jobItemContext = mockJobItemContext();
        Map<String, TableDataConsistencyCheckResult> actual = new HashMap<>();
        actual.put("test", new TableDataConsistencyCheckResult(true));
        governanceFacade.getJobFacade().getCheck().persistCheckJobResult(jobItemContext.getJobId(), "j02123", actual);
        Map<String, TableDataConsistencyCheckResult> checkResult = governanceFacade.getJobFacade().getCheck().getCheckJobResult(jobItemContext.getJobId(), "j02123");
        assertThat(checkResult.size(), is(1));
        assertTrue(checkResult.get("test").isMatched());
    }
    
    @Test
    void assertPersistJobItemProcess() {
        MigrationJobItemContext jobItemContext = mockJobItemContext();
        governanceFacade.getJobItemFacade().getProcess().update(jobItemContext.getJobId(), jobItemContext.getShardingItem(), "testValue1");
        assertFalse(governanceFacade.getJobItemFacade().getProcess().load(jobItemContext.getJobId(), jobItemContext.getShardingItem()).isPresent());
        governanceFacade.getJobItemFacade().getProcess().persist(jobItemContext.getJobId(), jobItemContext.getShardingItem(), "testValue1");
        Optional<String> actual = governanceFacade.getJobItemFacade().getProcess().load(jobItemContext.getJobId(), jobItemContext.getShardingItem());
        assertTrue(actual.isPresent());
        assertThat(actual.get(), is("testValue1"));
        governanceFacade.getJobItemFacade().getProcess().update(jobItemContext.getJobId(), jobItemContext.getShardingItem(), "testValue2");
        actual = governanceFacade.getJobItemFacade().getProcess().load(jobItemContext.getJobId(), jobItemContext.getShardingItem());
        assertTrue(actual.isPresent());
        assertThat(actual.get(), is("testValue2"));
    }
    
    @Test
    void assertPersistJobItemErrorMessage() {
        MigrationJobItemContext jobItemContext = mockJobItemContext();
        PipelineJobItemErrorMessageGovernanceRepository jobItemErrorMessageRepository = governanceFacade.getJobItemFacade().getErrorMessage();
        jobItemErrorMessageRepository.clean(jobItemContext.getJobId(), jobItemContext.getShardingItem());
        assertThat(jobItemErrorMessageRepository.load(jobItemContext.getJobId(), jobItemContext.getShardingItem()), is(""));
        jobItemErrorMessageRepository.update(jobItemContext.getJobId(), jobItemContext.getShardingItem(), new Exception("__DEBUG__"));
        String actual = jobItemErrorMessageRepository.load(jobItemContext.getJobId(), jobItemContext.getShardingItem());
        assertNotNull(actual, "Error message is null");
        assertTrue(actual.contains("__DEBUG__"), "Error message does not contain __DEBUG__");
        jobItemErrorMessageRepository.clean(jobItemContext.getJobId(), jobItemContext.getShardingItem());
        assertThat(jobItemErrorMessageRepository.load(jobItemContext.getJobId(), jobItemContext.getShardingItem()), is(""));
    }
    
    @Test
    void assertPersistJobOffset() {
        assertFalse(governanceFacade.getJobFacade().getOffset().load("1").isTargetSchemaTableCreated());
        governanceFacade.getJobFacade().getOffset().persist("1", new JobOffsetInfo(true));
        assertTrue(governanceFacade.getJobFacade().getOffset().load("1").isTargetSchemaTableCreated());
    }
    
    private ClusterPersistRepository getClusterPersistRepository() {
        return (ClusterPersistRepository) PipelineContextManager.getContext(PipelineContextUtils.getContextKey()).getPersistServiceFacade().getRepository();
    }
    
    private MigrationJobItemContext mockJobItemContext() {
        MigrationJobItemContext result = PipelineContextUtils.mockMigrationJobItemContext(JobConfigurationBuilder.createJobConfiguration());
        MigrationTaskConfiguration taskConfig = result.getTaskConfig();
        result.getInventoryTasks().add(mockInventoryTask(taskConfig));
        return result;
    }
    
    private InventoryTask mockInventoryTask(final MigrationTaskConfiguration taskConfig) {
        InventoryDumperContext dumperContext = new InventoryDumperContext(taskConfig.getDumperContext().getCommonContext());
        dumperContext.getCommonContext().setPosition(new IngestPlaceholderPosition());
        dumperContext.setActualTableName("t_order");
        dumperContext.setLogicTableName("t_order");
        dumperContext.setUniqueKeyColumns(Collections.singletonList(PipelineContextUtils.mockOrderIdColumnMetaData()));
        dumperContext.setShardingItem(0);
        return new InventoryTask(PipelineTaskUtils.generateInventoryTaskId(dumperContext), PipelineContextUtils.getExecuteEngine(), PipelineContextUtils.getExecuteEngine(),
                mock(Dumper.class), mock(Importer.class), new AtomicReference<>(new IngestPlaceholderPosition()));
    }
}
