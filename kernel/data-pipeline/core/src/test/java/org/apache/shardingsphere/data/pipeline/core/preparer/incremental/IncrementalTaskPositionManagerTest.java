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

package org.apache.shardingsphere.data.pipeline.core.preparer.incremental;

import lombok.SneakyThrows;
import org.apache.shardingsphere.data.pipeline.api.PipelineDataSourceConfiguration;
import org.apache.shardingsphere.data.pipeline.api.type.ShardingSpherePipelineDataSourceConfiguration;
import org.apache.shardingsphere.data.pipeline.api.type.StandardPipelineDataSourceConfiguration;
import org.apache.shardingsphere.data.pipeline.core.datasource.PipelineDataSource;
import org.apache.shardingsphere.data.pipeline.core.datasource.PipelineDataSourceManager;
import org.apache.shardingsphere.data.pipeline.core.ingest.dumper.incremental.IncrementalDumperContext;
import org.apache.shardingsphere.data.pipeline.core.ingest.position.DialectIncrementalPositionManager;
import org.apache.shardingsphere.data.pipeline.core.ingest.position.IngestPosition;
import org.apache.shardingsphere.data.pipeline.core.job.progress.JobItemIncrementalTasksProgress;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.infra.yaml.config.pojo.YamlRootConfiguration;
import org.apache.shardingsphere.test.infra.fixture.jdbc.MockedDataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.internal.configuration.plugins.Plugins;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class IncrementalTaskPositionManagerTest {
    
    private final DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "H2");
    
    @Mock
    private DialectIncrementalPositionManager dialectPositionManager;
    
    private IncrementalTaskPositionManager incrementalTaskPositionManager;
    
    @SneakyThrows(ReflectiveOperationException.class)
    @BeforeEach
    void setUp() {
        incrementalTaskPositionManager = new IncrementalTaskPositionManager(databaseType);
        Plugins.getMemberAccessor().set(IncrementalTaskPositionManager.class.getDeclaredField("dialectPositionManager"), incrementalTaskPositionManager, dialectPositionManager);
    }
    
    @Test
    void assertGetPositionWithInitialProgress() throws SQLException {
        JobItemIncrementalTasksProgress initialProgress = mock(JobItemIncrementalTasksProgress.class);
        IngestPosition position = mock(IngestPosition.class);
        when(initialProgress.getIncrementalPosition()).thenReturn(Optional.of(position));
        IncrementalDumperContext dumperContext = mockIncrementalDumperContext();
        assertThat(incrementalTaskPositionManager.getPosition(initialProgress, dumperContext, mock(PipelineDataSourceManager.class)), is(position));
    }
    
    @Test
    void assertGetPositionWithoutIncrementalProgress() throws SQLException {
        JobItemIncrementalTasksProgress initialProgress = mock(JobItemIncrementalTasksProgress.class);
        when(initialProgress.getIncrementalPosition()).thenReturn(Optional.empty());
        IncrementalDumperContext dumperContext = mockIncrementalDumperContext();
        PipelineDataSourceManager dataSourceManager = mock(PipelineDataSourceManager.class);
        PipelineDataSource dataSource = mock(PipelineDataSource.class);
        when(dataSourceManager.getDataSource(dumperContext.getCommonContext().getDataSourceConfig())).thenReturn(dataSource);
        IngestPosition position = mock(IngestPosition.class);
        when(dialectPositionManager.init(dataSource, dumperContext.getJobId())).thenReturn(position);
        assertThat(incrementalTaskPositionManager.getPosition(initialProgress, dumperContext, dataSourceManager), is(position));
    }
    
    @Test
    void assertGetPositionWithoutInitialProgress() throws SQLException {
        IncrementalDumperContext dumperContext = mockIncrementalDumperContext();
        PipelineDataSourceManager dataSourceManager = mock(PipelineDataSourceManager.class);
        PipelineDataSource dataSource = mock(PipelineDataSource.class);
        when(dataSourceManager.getDataSource(dumperContext.getCommonContext().getDataSourceConfig())).thenReturn(dataSource);
        IngestPosition position = mock(IngestPosition.class);
        when(dialectPositionManager.init(dataSource, dumperContext.getJobId())).thenReturn(position);
        assertThat(incrementalTaskPositionManager.getPosition(null, dumperContext, dataSourceManager), is(position));
    }
    
    private IncrementalDumperContext mockIncrementalDumperContext() {
        IncrementalDumperContext result = mock(IncrementalDumperContext.class, RETURNS_DEEP_STUBS);
        PipelineDataSourceConfiguration dataSourceConfig = mock(PipelineDataSourceConfiguration.class);
        when(result.getCommonContext().getDataSourceConfig()).thenReturn(dataSourceConfig);
        return result;
    }
    
    @Test
    void assertDestroyPositionWithShardingSpherePipelineDataSourceConfiguration() throws SQLException {
        YamlRootConfiguration rootConfig = new YamlRootConfiguration();
        Map<String, Object> dataSourceProps = new HashMap<>(2, 1F);
        dataSourceProps.put("dataSourceClassName", MockedDataSource.class.getName());
        dataSourceProps.put("url", "jdbc:mock://127.0.0.1/foo_ds");
        rootConfig.getDataSources().put("foo_ds", dataSourceProps);
        ShardingSpherePipelineDataSourceConfiguration pipelineDataSourceConfig = new ShardingSpherePipelineDataSourceConfiguration(rootConfig);
        incrementalTaskPositionManager.destroyPosition("foo_job", pipelineDataSourceConfig);
        verify(dialectPositionManager).destroy(any(), eq("foo_job"));
    }
    
    @Test
    void assertDestroyPositionWithStandardPipelineDataSourceConfiguration() throws SQLException {
        Map<String, Object> dataSourceProps = new HashMap<>(2, 1F);
        dataSourceProps.put("dataSourceClassName", MockedDataSource.class.getName());
        dataSourceProps.put("url", "jdbc:mock://127.0.0.1/foo_ds");
        StandardPipelineDataSourceConfiguration pipelineDataSourceConfig = new StandardPipelineDataSourceConfiguration(dataSourceProps);
        incrementalTaskPositionManager.destroyPosition("foo_job", pipelineDataSourceConfig);
        verify(dialectPositionManager).destroy(any(), eq("foo_job"));
    }
    
    @Test
    void assertDestroyPositionWithUnknownPipelineDataSourceConfiguration() throws SQLException {
        incrementalTaskPositionManager.destroyPosition("foo_job", mock(PipelineDataSourceConfiguration.class));
        verify(dialectPositionManager, never()).destroy(any(), eq("foo_job"));
    }
}
