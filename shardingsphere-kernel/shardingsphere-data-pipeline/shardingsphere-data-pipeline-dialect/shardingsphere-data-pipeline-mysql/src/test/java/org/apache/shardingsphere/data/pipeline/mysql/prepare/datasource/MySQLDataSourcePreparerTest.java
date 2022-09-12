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

package org.apache.shardingsphere.data.pipeline.mysql.prepare.datasource;

import org.apache.shardingsphere.data.pipeline.api.config.job.MigrationJobConfiguration;
import org.apache.shardingsphere.data.pipeline.api.datasource.PipelineDataSourceManager;
import org.apache.shardingsphere.data.pipeline.api.datasource.PipelineDataSourceWrapper;
import org.apache.shardingsphere.data.pipeline.api.datasource.config.PipelineDataSourceConfiguration;
import org.apache.shardingsphere.data.pipeline.api.datasource.config.PipelineDataSourceConfigurationFactory;
import org.apache.shardingsphere.data.pipeline.api.datasource.config.impl.ShardingSpherePipelineDataSourceConfiguration;
import org.apache.shardingsphere.data.pipeline.core.exception.job.PipelineJobPrepareFailedException;
import org.apache.shardingsphere.data.pipeline.core.prepare.datasource.PrepareTargetTablesParameter;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.MockitoJUnitRunner;

import java.sql.SQLException;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

// TODO update after new impl ready
@Ignore
@RunWith(MockitoJUnitRunner.class)
public final class MySQLDataSourcePreparerTest {
    
    @Mock
    private PrepareTargetTablesParameter prepareTargetTablesParameter;
    
    @Mock
    private MigrationJobConfiguration jobConfig;
    
    @Mock
    private PipelineDataSourceConfiguration sourcePipelineDataSourceConfig;
    
    @Mock
    private PipelineDataSourceConfiguration targetPipelineDataSourceConfig;
    
    @Mock
    private ShardingSpherePipelineDataSourceConfiguration sourceDataSourceConfig;
    
    @Mock
    private ShardingSpherePipelineDataSourceConfiguration targetDataSourceConfig;
    
    @Mock
    private PipelineDataSourceWrapper sourceDataSourceWrapper;
    
    @Mock
    private PipelineDataSourceWrapper targetDataSourceWrapper;
    
    @Before
    public void setUp() throws SQLException {
        PipelineDataSourceManager mockPipelineDataSourceManager = mock(PipelineDataSourceManager.class);
        when(mockPipelineDataSourceManager.getDataSource(same(sourceDataSourceConfig))).thenReturn(sourceDataSourceWrapper);
        when(mockPipelineDataSourceManager.getDataSource(same(targetDataSourceConfig))).thenReturn(targetDataSourceWrapper);
        when(prepareTargetTablesParameter.getDataSourceManager()).thenReturn(mockPipelineDataSourceManager);
        when(jobConfig.getSource()).thenReturn(sourcePipelineDataSourceConfig);
        when(jobConfig.getSource().getType()).thenReturn("ShardingSphereJDBC");
        when(jobConfig.getSource().getParameter()).thenReturn("source");
        when(jobConfig.getTarget()).thenReturn(targetPipelineDataSourceConfig);
        when(jobConfig.getTarget().getType()).thenReturn("ShardingSphereJDBC");
        when(jobConfig.getTarget().getParameter()).thenReturn("target");
    }
    
    @Test
    public void assertGetConnection() throws SQLException {
        try (MockedStatic<PipelineDataSourceConfigurationFactory> mockedStaticPipelineDataSourceConfigurationFactory = mockStatic(PipelineDataSourceConfigurationFactory.class)) {
            mockedStaticPipelineDataSourceConfigurationFactory.when(() -> PipelineDataSourceConfigurationFactory.newInstance(eq("ShardingSphereJDBC"), eq("source")))
                    .thenReturn(sourceDataSourceConfig);
            mockedStaticPipelineDataSourceConfigurationFactory.when(() -> PipelineDataSourceConfigurationFactory.newInstance(eq("ShardingSphereJDBC"), eq("target")))
                    .thenReturn(targetDataSourceConfig);
            new MySQLDataSourcePreparer().prepareTargetTables(prepareTargetTablesParameter);
            verify(sourceDataSourceWrapper).getConnection();
            verify(targetDataSourceWrapper).getConnection();
        }
    }
    
    @Test(expected = PipelineJobPrepareFailedException.class)
    public void assertThrowPrepareFailedException() throws SQLException {
        try (MockedStatic<PipelineDataSourceConfigurationFactory> mockedStaticPipelineDataSourceConfigurationFactory = mockStatic(PipelineDataSourceConfigurationFactory.class)) {
            mockedStaticPipelineDataSourceConfigurationFactory.when(() -> PipelineDataSourceConfigurationFactory.newInstance(eq("ShardingSphereJDBC"), eq("source")))
                    .thenReturn(sourceDataSourceConfig);
            mockedStaticPipelineDataSourceConfigurationFactory.when(() -> PipelineDataSourceConfigurationFactory.newInstance(eq("ShardingSphereJDBC"), eq("target")))
                    .thenReturn(targetDataSourceConfig);
            when(sourceDataSourceWrapper.getConnection()).thenThrow(SQLException.class);
            new MySQLDataSourcePreparer().prepareTargetTables(prepareTargetTablesParameter);
        }
    }
}
