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

package org.apache.shardingsphere.data.pipeline.cdc.core.prepare;

import org.apache.shardingsphere.data.pipeline.api.PipelineDataSourceConfiguration;
import org.apache.shardingsphere.data.pipeline.cdc.config.CDCJobConfiguration;
import org.apache.shardingsphere.data.pipeline.cdc.config.CDCTaskConfiguration;
import org.apache.shardingsphere.data.pipeline.cdc.context.CDCJobItemContext;
import org.apache.shardingsphere.data.pipeline.core.channel.IncrementalChannelCreator;
import org.apache.shardingsphere.data.pipeline.core.channel.InventoryChannelCreator;
import org.apache.shardingsphere.data.pipeline.core.channel.PipelineChannel;
import org.apache.shardingsphere.data.pipeline.core.context.TransmissionProcessContext;
import org.apache.shardingsphere.data.pipeline.core.datasource.PipelineDataSourceManager;
import org.apache.shardingsphere.data.pipeline.core.exception.job.PrepareJobWithGetBinlogPositionException;
import org.apache.shardingsphere.data.pipeline.core.importer.ImporterConfiguration;
import org.apache.shardingsphere.data.pipeline.core.importer.sink.PipelineSink;
import org.apache.shardingsphere.data.pipeline.core.ingest.dumper.DumperCommonContext;
import org.apache.shardingsphere.data.pipeline.core.ingest.dumper.incremental.CreateIncrementalDumperParameter;
import org.apache.shardingsphere.data.pipeline.core.ingest.dumper.incremental.IncrementalDumper;
import org.apache.shardingsphere.data.pipeline.core.ingest.dumper.incremental.IncrementalDumperContext;
import org.apache.shardingsphere.data.pipeline.core.ingest.dumper.incremental.IncrementalDumperCreator;
import org.apache.shardingsphere.data.pipeline.core.ingest.dumper.inventory.InventoryDumper;
import org.apache.shardingsphere.data.pipeline.core.ingest.dumper.inventory.InventoryDumperContext;
import org.apache.shardingsphere.data.pipeline.core.ingest.dumper.inventory.position.InventoryDataRecordPositionCreator;
import org.apache.shardingsphere.data.pipeline.core.ingest.dumper.inventory.position.type.PlaceholderInventoryDataRecordPositionCreator;
import org.apache.shardingsphere.data.pipeline.core.ingest.dumper.inventory.position.type.UniqueKeyInventoryDataRecordPositionCreator;
import org.apache.shardingsphere.data.pipeline.core.ingest.dumper.mapper.ActualAndLogicTableNameMapper;
import org.apache.shardingsphere.data.pipeline.core.ingest.dumper.mapper.TableAndSchemaNameMapper;
import org.apache.shardingsphere.data.pipeline.core.ingest.position.DialectIncrementalPositionManager;
import org.apache.shardingsphere.data.pipeline.core.ingest.position.IngestPosition;
import org.apache.shardingsphere.data.pipeline.core.ingest.position.type.finished.IngestFinishedPosition;
import org.apache.shardingsphere.data.pipeline.core.ingest.position.type.placeholder.IngestPlaceholderPosition;
import org.apache.shardingsphere.data.pipeline.core.job.PipelineJobRegistry;
import org.apache.shardingsphere.data.pipeline.core.job.progress.JobItemIncrementalTasksProgress;
import org.apache.shardingsphere.data.pipeline.core.job.progress.TransmissionJobItemProgress;
import org.apache.shardingsphere.data.pipeline.core.job.progress.config.PipelineProcessConfiguration;
import org.apache.shardingsphere.data.pipeline.core.job.progress.config.PipelineReadConfiguration;
import org.apache.shardingsphere.data.pipeline.core.job.progress.config.PipelineWriteConfiguration;
import org.apache.shardingsphere.data.pipeline.core.job.service.PipelineJobItemManager;
import org.apache.shardingsphere.data.pipeline.core.metadata.model.PipelineColumnMetaData;
import org.apache.shardingsphere.data.pipeline.core.preparer.inventory.splitter.InventoryDumperContextSplitter;
import org.apache.shardingsphere.data.pipeline.core.task.PipelineTask;
import org.apache.shardingsphere.data.pipeline.core.task.progress.IncrementalTaskProgress;
import org.apache.shardingsphere.database.connector.core.spi.DatabaseTypedSPILoader;
import org.apache.shardingsphere.infra.algorithm.core.config.AlgorithmConfiguration;
import org.apache.shardingsphere.infra.metadata.identifier.ShardingSphereIdentifier;
import org.apache.shardingsphere.test.infra.framework.extension.mock.AutoMockExtension;
import org.apache.shardingsphere.test.infra.framework.extension.mock.StaticMockSettings;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.MockedConstruction;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isA;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(AutoMockExtension.class)
@StaticMockSettings({PipelineJobRegistry.class, InventoryChannelCreator.class, IncrementalChannelCreator.class, IncrementalDumperCreator.class, DatabaseTypedSPILoader.class})
class CDCJobPreparerTest {
    
    @Mock
    private PipelineJobItemManager<TransmissionJobItemProgress> jobItemManager;
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private PipelineDataSourceManager dataSourceManager;
    
    @Mock
    private PipelineSink sink;
    
    private CDCJobPreparer preparer;
    
    @BeforeEach
    void setUp() {
        preparer = new CDCJobPreparer(jobItemManager);
    }
    
    @SuppressWarnings("unchecked")
    @Test
    void assertInitTasksSuccess() throws SQLException {
        when(InventoryChannelCreator.create(any(AlgorithmConfiguration.class), anyInt(), any())).thenReturn(mock(PipelineChannel.class));
        when(IncrementalChannelCreator.create(any(AlgorithmConfiguration.class), any())).thenReturn(mock(PipelineChannel.class));
        when(IncrementalDumperCreator.create(any(CreateIncrementalDumperParameter.class))).thenReturn(mock(IncrementalDumper.class));
        DialectIncrementalPositionManager dialectPositionManager = mock(DialectIncrementalPositionManager.class);
        IngestPosition ingestPosition = new IngestPlaceholderPosition();
        when(dialectPositionManager.init(any(DataSource.class), any())).thenReturn(ingestPosition);
        when(DatabaseTypedSPILoader.getService(any(), any())).thenReturn(dialectPositionManager);
        PipelineProcessConfiguration processConfig = new PipelineProcessConfiguration(
                new PipelineReadConfiguration(1, 1, 1, null), new PipelineWriteConfiguration(1, 1, null), new AlgorithmConfiguration("MEMORY", new Properties()));
        TransmissionProcessContext processContext = mock(TransmissionProcessContext.class, RETURNS_DEEP_STUBS);
        when(processContext.getProcessConfiguration()).thenReturn(processConfig);
        when(jobItemManager.getProgress(any(), anyInt())).thenReturn(Optional.empty(), Optional.empty(), Optional.of(mock(TransmissionJobItemProgress.class)));
        IncrementalTaskProgress incrementalTaskProgress = new IncrementalTaskProgress(new IngestPlaceholderPosition());
        TransmissionJobItemProgress initProgress = mock(TransmissionJobItemProgress.class);
        when(initProgress.getIncremental()).thenReturn(new JobItemIncrementalTasksProgress(incrementalTaskProgress));
        IngestPosition incrementalPositionFromProgress = new IngestPlaceholderPosition();
        incrementalTaskProgress.setPosition(incrementalPositionFromProgress);
        PipelineDataSourceConfiguration dataSourceConfig = mock(PipelineDataSourceConfiguration.class);
        CDCJobItemContext incrementalOnlyContext = createJobItemContext("job_incremental_only", false, true, initProgress, processContext, dataSourceConfig);
        CDCJobItemContext stoppingContext = createJobItemContext("job_stopping", true, false, null, processContext, dataSourceConfig);
        stoppingContext.setStopping(true);
        CDCJobItemContext fullContext = createJobItemContext("job_full", true, false, null, processContext, dataSourceConfig);
        InventoryDumperContext uniqueKeyContext = createInventoryDumperContext(
                fullContext.getTaskConfig().getDumperContext().getCommonContext(), new IngestPlaceholderPosition(), "actual_table_0", 0, true);
        InventoryDumperContext finishedContext = createInventoryDumperContext(
                fullContext.getTaskConfig().getDumperContext().getCommonContext(), new IngestFinishedPosition(), "actual_table_1", 1, false);
        List<InventoryDataRecordPositionCreator> positionCreators = new LinkedList<>();
        try (
                MockedConstruction<InventoryDumperContextSplitter> ignoredSplitter = mockConstruction(InventoryDumperContextSplitter.class,
                        (mock, context) -> when(mock.split(any())).thenReturn(Arrays.asList(uniqueKeyContext, finishedContext)));
                MockedConstruction<InventoryDumper> ignoredInventoryDumperConstruction = mockConstruction(InventoryDumper.class,
                        (mock, context) -> positionCreators.add((InventoryDataRecordPositionCreator) context.arguments().get(3)))) {
            preparer.initTasks(Arrays.asList(fullContext, incrementalOnlyContext, stoppingContext));
            assertThat(fullContext.getInventoryTasks().size(), is(2));
            assertThat(fullContext.getInventoryTasks().iterator().next().start().size(), is(2));
            assertTrue(((PipelineTask) fullContext.getInventoryTasks().toArray()[1]).start().isEmpty());
            assertThat(positionCreators.get(0), isA(UniqueKeyInventoryDataRecordPositionCreator.class));
            assertThat(positionCreators.get(1), isA(PlaceholderInventoryDataRecordPositionCreator.class));
            assertThat(fullContext.getIncrementalTasks().size(), is(1));
            assertThat(fullContext.getIncrementalTasks().iterator().next().start().size(), is(2));
            assertTrue(incrementalOnlyContext.getInventoryTasks().isEmpty());
            assertThat(incrementalOnlyContext.getIncrementalTasks().iterator().next().start().size(), is(1));
        }
        assertThat(fullContext.getTaskConfig().getDumperContext().getCommonContext().getPosition(), is(ingestPosition));
        assertThat(incrementalOnlyContext.getTaskConfig().getDumperContext().getCommonContext().getPosition(), is(incrementalPositionFromProgress));
        verify(jobItemManager, times(2)).persistProgress(any(CDCJobItemContext.class));
    }
    
    private CDCJobItemContext createJobItemContext(final String jobId, final boolean full, final boolean decodeWithTX, final TransmissionJobItemProgress initProgress,
                                                   final TransmissionProcessContext processContext, final PipelineDataSourceConfiguration dataSourceConfig) {
        CDCJobConfiguration jobConfig = mock(CDCJobConfiguration.class);
        when(jobConfig.getJobId()).thenReturn(jobId);
        lenient().when(jobConfig.isFull()).thenReturn(full);
        DumperCommonContext dumperCommonContext = createDumperCommonContext(dataSourceConfig);
        IncrementalDumperContext incrementalDumperContext = new IncrementalDumperContext(dumperCommonContext, jobId, decodeWithTX);
        ImporterConfiguration importerConfig = new ImporterConfiguration(dataSourceConfig, Collections.emptyMap(), new TableAndSchemaNameMapper(Collections.emptyMap()), 1, null, 0, 1);
        CDCTaskConfiguration taskConfig = new CDCTaskConfiguration(incrementalDumperContext, importerConfig);
        return new CDCJobItemContext(jobConfig, 0, initProgress, processContext, taskConfig, dataSourceManager, sink);
    }
    
    private DumperCommonContext createDumperCommonContext(final PipelineDataSourceConfiguration dataSourceConfig) {
        Map<ShardingSphereIdentifier, ShardingSphereIdentifier> tableNameMap = Collections.singletonMap(new ShardingSphereIdentifier("actual_table_0"),
                new ShardingSphereIdentifier("logic_table_0"));
        return new DumperCommonContext("ds_0", dataSourceConfig, new ActualAndLogicTableNameMapper(tableNameMap),
                new TableAndSchemaNameMapper(Collections.singletonMap("logic_table_0", "logic_schema")));
    }
    
    private InventoryDumperContext createInventoryDumperContext(final DumperCommonContext commonContext,
                                                                final IngestPosition position, final String actualTableName, final int shardingItem, final boolean hasUniqueKey) {
        InventoryDumperContext result = new InventoryDumperContext(commonContext);
        result.getCommonContext().setPosition(position);
        result.setActualTableName(actualTableName);
        result.setLogicTableName("logic_" + actualTableName);
        result.setShardingItem(shardingItem);
        result.setUniqueKeyColumns(hasUniqueKey ? Collections.singletonList(new PipelineColumnMetaData(1, "id", Types.BIGINT, "bigint", false, true, true)) : Collections.emptyList());
        return result;
    }
    
    @Test
    void assertInitTasksFailure() {
        when(DatabaseTypedSPILoader.getService(any(), any())).thenAnswer(invocationOnMock -> {
            DialectIncrementalPositionManager result = mock(DialectIncrementalPositionManager.class);
            when(result.init(any(DataSource.class), any())).thenThrow(SQLException.class);
            return result;
        });
        TransmissionProcessContext processContext = mock(TransmissionProcessContext.class);
        when(jobItemManager.getProgress(any(), anyInt())).thenReturn(Optional.empty());
        CDCJobItemContext jobItemContext = createJobItemContext("failure_job", false, false, null, processContext, mock());
        assertThrows(PrepareJobWithGetBinlogPositionException.class, () -> preparer.initTasks(Collections.singleton(jobItemContext)));
        verify(jobItemManager).persistProgress(jobItemContext);
    }
}
