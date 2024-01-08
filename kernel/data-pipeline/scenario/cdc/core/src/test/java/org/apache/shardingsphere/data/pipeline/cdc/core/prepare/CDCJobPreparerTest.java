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

import org.apache.shardingsphere.data.pipeline.api.type.ShardingSpherePipelineDataSourceConfiguration;
import org.apache.shardingsphere.data.pipeline.cdc.config.job.CDCJobConfiguration;
import org.apache.shardingsphere.data.pipeline.cdc.config.task.CDCTaskConfiguration;
import org.apache.shardingsphere.data.pipeline.cdc.context.CDCJobItemContext;
import org.apache.shardingsphere.data.pipeline.core.context.TransmissionProcessContext;
import org.apache.shardingsphere.data.pipeline.core.datasource.PipelineDataSourceManager;
import org.apache.shardingsphere.data.pipeline.core.importer.ImporterConfiguration;
import org.apache.shardingsphere.data.pipeline.core.importer.sink.PipelineSink;
import org.apache.shardingsphere.data.pipeline.core.ingest.dumper.DumperCommonContext;
import org.apache.shardingsphere.data.pipeline.core.ingest.dumper.incremental.DialectIncrementalDumperCreator;
import org.apache.shardingsphere.data.pipeline.core.ingest.dumper.incremental.IncrementalDumperContext;
import org.apache.shardingsphere.data.pipeline.core.ingest.dumper.mapper.ActualAndLogicTableNameMapper;
import org.apache.shardingsphere.data.pipeline.core.ingest.dumper.mapper.TableAndSchemaNameMapper;
import org.apache.shardingsphere.data.pipeline.core.ingest.position.DialectIngestPositionManager;
import org.apache.shardingsphere.data.pipeline.core.ingest.position.type.pk.type.IntegerPrimaryKeyIngestPosition;
import org.apache.shardingsphere.data.pipeline.core.job.api.PipelineAPIFactory;
import org.apache.shardingsphere.data.pipeline.core.job.progress.TransmissionJobItemProgress;
import org.apache.shardingsphere.data.pipeline.core.registrycenter.repository.PipelineGovernanceFacade;
import org.apache.shardingsphere.data.pipeline.core.registrycenter.repository.item.PipelineJobItemFacade;
import org.apache.shardingsphere.data.pipeline.core.registrycenter.repository.item.PipelineJobItemProcessGovernanceRepository;
import org.apache.shardingsphere.infra.database.core.spi.DatabaseTypedSPILoader;
import org.apache.shardingsphere.infra.database.core.type.DatabaseType;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.sql.SQLException;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

class CDCJobPreparerTest {
    
    @Test
    void assertPrepare() throws SQLException {
        CDCJobPreparer jobPreparer = new CDCJobPreparer();
        DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "MySQL");
        ShardingSpherePipelineDataSourceConfiguration mockDataSourceConfig = mock(ShardingSpherePipelineDataSourceConfiguration.class);
        when(mockDataSourceConfig.getDatabaseType()).thenReturn(databaseType);
        DumperCommonContext dumperCommonContext = new DumperCommonContext("test", mockDataSourceConfig, new ActualAndLogicTableNameMapper(Collections.emptyMap()),
                new TableAndSchemaNameMapper(Collections.emptyMap()));
        String jobId = "j0302p00007a8bf46da145dc155ba25c710b550220";
        CDCTaskConfiguration taskConfig = new CDCTaskConfiguration(new IncrementalDumperContext(dumperCommonContext, jobId, false), new ImporterConfiguration(null, Collections.emptyMap(), null,
                100, null, 1, 1));
        TransmissionJobItemProgress itemProgress = new TransmissionJobItemProgress();
        itemProgress.setDataSourceName("ds_0");
        itemProgress.setSourceDatabaseType(databaseType);
        CDCJobConfiguration jobConfig = new CDCJobConfiguration(jobId, "test", Collections.singletonList("test.t_order"), true, databaseType, null, null, null, false, null, 1, 1);
        TransmissionProcessContext transmissionProcessContext = new TransmissionProcessContext(jobId, null);
        CDCJobItemContext cdcJobItemContext = new CDCJobItemContext(jobConfig, 1, itemProgress, transmissionProcessContext, taskConfig, mock(PipelineDataSourceManager.class),
                mock(PipelineSink.class));
        try (
                MockedStatic<PipelineAPIFactory> mockStatic = mockStatic(PipelineAPIFactory.class);
                MockedStatic<DatabaseTypedSPILoader> mockSPIStatic = mockStatic(DatabaseTypedSPILoader.class)) {
            DialectIngestPositionManager mockPositionManager = mock(DialectIngestPositionManager.class);
            when(mockPositionManager.init(any(), anyString())).thenReturn(new IntegerPrimaryKeyIngestPosition(1, 100));
            mockSPIStatic.when(() -> DatabaseTypedSPILoader.getService(DialectIngestPositionManager.class, databaseType)).thenReturn(mockPositionManager);
            DialectIncrementalDumperCreator mockDumper = mock(DialectIncrementalDumperCreator.class);
            mockSPIStatic.when(() -> DatabaseTypedSPILoader.getService(DialectIncrementalDumperCreator.class, databaseType)).thenReturn(mockDumper);
            PipelineJobItemFacade mockItemFacade = mock(PipelineJobItemFacade.class);
            PipelineJobItemProcessGovernanceRepository mockRepository = mock(PipelineJobItemProcessGovernanceRepository.class);
            when(mockRepository.load(anyString(), anyInt())).thenReturn(Optional.empty());
            when(mockItemFacade.getProcess()).thenReturn(mockRepository);
            PipelineGovernanceFacade mockGovernanceFacade = mock(PipelineGovernanceFacade.class);
            when(mockGovernanceFacade.getJobItemFacade()).thenReturn(mockItemFacade);
            mockStatic.when(() -> PipelineAPIFactory.getPipelineGovernanceFacade(any())).thenReturn(mockGovernanceFacade);
            jobPreparer.initTasks(Collections.singletonList(cdcJobItemContext));
            assertFalse(cdcJobItemContext.getIncrementalTasks().isEmpty());
        }
    }
}
