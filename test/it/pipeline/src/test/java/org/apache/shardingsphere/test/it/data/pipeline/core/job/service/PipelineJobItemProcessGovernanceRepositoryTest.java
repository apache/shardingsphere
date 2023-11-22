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

import org.apache.shardingsphere.data.pipeline.common.context.PipelineContextManager;
import org.apache.shardingsphere.data.pipeline.common.ingest.position.PlaceholderPosition;
import org.apache.shardingsphere.data.pipeline.common.registrycenter.repository.PipelineJobItemProcessGovernanceRepository;
import org.apache.shardingsphere.data.pipeline.core.importer.Importer;
import org.apache.shardingsphere.data.pipeline.core.ingest.dumper.Dumper;
import org.apache.shardingsphere.data.pipeline.core.ingest.dumper.context.InventoryDumperContext;
import org.apache.shardingsphere.data.pipeline.core.task.InventoryTask;
import org.apache.shardingsphere.data.pipeline.core.task.PipelineTaskUtils;
import org.apache.shardingsphere.data.pipeline.scenario.migration.config.MigrationTaskConfiguration;
import org.apache.shardingsphere.data.pipeline.scenario.migration.context.MigrationJobItemContext;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.repository.cluster.ClusterPersistRepository;
import org.apache.shardingsphere.test.it.data.pipeline.core.util.JobConfigurationBuilder;
import org.apache.shardingsphere.test.it.data.pipeline.core.util.PipelineContextUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

class PipelineJobItemProcessGovernanceRepositoryTest {
    
    @BeforeAll
    static void beforeClass() {
        PipelineContextUtils.mockModeConfigAndContextManager();
    }
    
    @Test
    void assertPersist() {
        ClusterPersistRepository clusterPersistRepository = getClusterPersistRepository();
        PipelineJobItemProcessGovernanceRepository repository = new PipelineJobItemProcessGovernanceRepository(clusterPersistRepository);
        MigrationJobItemContext jobItemContext = mockJobItemContext();
        repository.update(jobItemContext.getJobId(), jobItemContext.getShardingItem(), "testValue1");
        assertFalse(repository.get(jobItemContext.getJobId(), jobItemContext.getShardingItem()).isPresent());
        repository.persist(jobItemContext.getJobId(), jobItemContext.getShardingItem(), "testValue1");
        Optional<String> actual = repository.get(jobItemContext.getJobId(), jobItemContext.getShardingItem());
        assertTrue(actual.isPresent());
        assertThat(actual.get(), is("testValue1"));
        repository.update(jobItemContext.getJobId(), jobItemContext.getShardingItem(), "testValue2");
        actual = repository.get(jobItemContext.getJobId(), jobItemContext.getShardingItem());
        assertTrue(actual.isPresent());
        assertThat(actual.get(), is("testValue2"));
    }
    
    private ClusterPersistRepository getClusterPersistRepository() {
        ContextManager contextManager = PipelineContextManager.getContext(PipelineContextUtils.getContextKey()).getContextManager();
        return (ClusterPersistRepository) contextManager.getMetaDataContexts().getPersistService().getRepository();
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
