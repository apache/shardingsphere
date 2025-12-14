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

package org.apache.shardingsphere.data.pipeline.cdc.context;

import org.apache.shardingsphere.data.pipeline.cdc.config.CDCJobConfiguration;
import org.apache.shardingsphere.data.pipeline.cdc.config.CDCTaskConfiguration;
import org.apache.shardingsphere.data.pipeline.core.context.TransmissionProcessContext;
import org.apache.shardingsphere.data.pipeline.core.datasource.PipelineDataSource;
import org.apache.shardingsphere.data.pipeline.core.datasource.PipelineDataSourceManager;
import org.apache.shardingsphere.data.pipeline.core.importer.ImporterConfiguration;
import org.apache.shardingsphere.data.pipeline.core.importer.sink.PipelineSink;
import org.apache.shardingsphere.data.pipeline.core.ingest.dumper.DumperCommonContext;
import org.apache.shardingsphere.data.pipeline.core.ingest.dumper.incremental.IncrementalDumperContext;
import org.apache.shardingsphere.data.pipeline.core.ingest.dumper.mapper.ActualAndLogicTableNameMapper;
import org.apache.shardingsphere.data.pipeline.core.ingest.dumper.mapper.TableAndSchemaNameMapper;
import org.apache.shardingsphere.data.pipeline.core.job.JobStatus;
import org.apache.shardingsphere.data.pipeline.core.job.progress.TransmissionJobItemProgress;
import org.apache.shardingsphere.data.pipeline.core.job.progress.listener.PipelineJobUpdateProgress;
import org.apache.shardingsphere.data.pipeline.core.job.progress.persist.PipelineJobProgressPersistService;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.metadata.identifier.ShardingSphereIdentifier;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

class CDCJobItemContextTest {
    
    @Test
    void assertContextWithInitProgressAndLazyLoaders() {
        PipelineDataSource pipelineDataSource = mock(PipelineDataSource.class);
        PipelineDataSourceManager dataSourceManager = mock(PipelineDataSourceManager.class);
        when(dataSourceManager.getDataSource(any())).thenReturn(pipelineDataSource);
        TransmissionJobItemProgress initProgress = new TransmissionJobItemProgress();
        initProgress.setProcessedRecordsCount(5L);
        initProgress.setInventoryRecordsCount(7L);
        CDCJobItemContext context = new CDCJobItemContext(createJobConfiguration(), 1, initProgress, mock(TransmissionProcessContext.class), createTaskConfiguration(), dataSourceManager, mock());
        assertThat(context.getJobId(), is("foo_job"));
        assertThat(context.getDataSourceName(), is("foo_ds"));
        assertThat(context.getProcessedRecordsCount(), is(5L));
        assertThat(context.getInventoryRecordsCount(), is(7L));
        assertThat(context.getSourceDataSource(), is(pipelineDataSource));
        assertNotNull(context.getSourceMetaDataLoader());
        assertFalse(context.isStopping());
        assertThat(context.getStatus(), is(JobStatus.RUNNING));
    }
    
    @Test
    void assertProgressAndInventoryUpdate() {
        try (MockedStatic<PipelineJobProgressPersistService> persistService = mockStatic(PipelineJobProgressPersistService.class)) {
            CDCJobItemContext context = new CDCJobItemContext(createJobConfiguration(), 2, null, mock(), createTaskConfiguration(), mock(), mock());
            context.onProgressUpdated(new PipelineJobUpdateProgress(3));
            assertThat(context.getProcessedRecordsCount(), is(3L));
            persistService.verify(() -> PipelineJobProgressPersistService.notifyPersist("foo_job", 2));
            context.updateInventoryRecordsCount(4L);
            assertThat(context.getInventoryRecordsCount(), is(4L));
        }
    }
    
    @Test
    void assertInitWithoutInitialProgress() {
        PipelineSink sink = mock(PipelineSink.class);
        CDCJobItemContext context = new CDCJobItemContext(createJobConfiguration(), 3, null, mock(), createTaskConfiguration(), mock(), sink);
        assertThat(context.getProcessedRecordsCount(), is(0L));
        assertThat(context.getInventoryRecordsCount(), is(0L));
        assertTrue(context.getInventoryTasks().isEmpty());
        assertTrue(context.getIncrementalTasks().isEmpty());
        assertThat(context.getJobConfig().getJobId(), is("foo_job"));
        assertThat(context.getSink(), is(sink));
    }
    
    private CDCJobConfiguration createJobConfiguration() {
        return new CDCJobConfiguration(
                "foo_job", "foo_db", Collections.emptyList(), true, TypedSPILoader.getService(DatabaseType.class, "FIXTURE"), mock(), null, Collections.emptyList(), true, null, 1, 0);
    }
    
    private CDCTaskConfiguration createTaskConfiguration() {
        DumperCommonContext commonContext = new DumperCommonContext("foo_ds", mock(), new ActualAndLogicTableNameMapper(Collections.emptyMap()), new TableAndSchemaNameMapper(Collections.emptyMap()));
        IncrementalDumperContext dumperContext = new IncrementalDumperContext(commonContext, "foo_job", true);
        Map<ShardingSphereIdentifier, Collection<String>> tableAndRequiredColumns = Collections.singletonMap(new ShardingSphereIdentifier("t_order"), Collections.singleton("id"));
        ImporterConfiguration importerConfig = new ImporterConfiguration(mock(), tableAndRequiredColumns, new TableAndSchemaNameMapper(Collections.emptyMap()), 1, null, 1, 1);
        return new CDCTaskConfiguration(dumperContext, importerConfig);
    }
}
