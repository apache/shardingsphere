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

import org.apache.shardingsphere.data.pipeline.api.config.rulealtered.PipelineConfiguration;
import org.apache.shardingsphere.data.pipeline.api.datanode.JobDataNodeLine;
import org.apache.shardingsphere.data.pipeline.api.datasource.config.PipelineDataSourceConfigurationFactory;
import org.apache.shardingsphere.data.pipeline.api.datasource.config.impl.ShardingSpherePipelineDataSourceConfiguration;
import org.apache.shardingsphere.data.pipeline.api.datasource.config.yaml.YamlPipelineDataSourceConfiguration;
import org.apache.shardingsphere.data.pipeline.api.prepare.datasource.PrepareTargetTablesParameter;
import org.apache.shardingsphere.data.pipeline.core.datasource.creator.PipelineDataSourceCreatorFactory;
import org.apache.shardingsphere.data.pipeline.core.exception.PipelineJobPrepareFailedException;
import org.apache.shardingsphere.driver.config.datasource.ShardingSpherePipelineDataSourceCreator;
import org.apache.shardingsphere.infra.config.datasource.DataSourceConfiguration;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.MockitoJUnitRunner;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.RETURNS_MOCKS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

// FIX test cases
@RunWith(MockitoJUnitRunner.class)
public final class MySQLDataSourcePreparerTest {
    
    @Mock
    private PrepareTargetTablesParameter prepareTargetTablesParameter;
    
    @Mock
    private PipelineConfiguration pipelineConfig;
    
    private YamlPipelineDataSourceConfiguration sourceYamlPipelineDataSourceConfiguration;
    
    private YamlPipelineDataSourceConfiguration targetYamlPipelineDataSourceConfiguration;
    
    @Mock
    private ShardingSpherePipelineDataSourceConfiguration sourceScalingDataSourceConfig;
    
    @Mock
    private ShardingSpherePipelineDataSourceConfiguration targetScalingDataSourceConfig;
    
    @Mock(extraInterfaces = AutoCloseable.class)
    private DataSource sourceDataSource;
    
    @Mock(extraInterfaces = AutoCloseable.class)
    private DataSource targetDataSource;
    
    @Mock
    private ShardingSpherePipelineDataSourceCreator mockShardingSpherePipelineDataSourceCreator;
    
    @Before
    public void setUp() throws SQLException {
        sourceYamlPipelineDataSourceConfiguration = new YamlPipelineDataSourceConfiguration();
        targetYamlPipelineDataSourceConfiguration = new YamlPipelineDataSourceConfiguration();
        sourceYamlPipelineDataSourceConfiguration.setParameter("source");
        sourceYamlPipelineDataSourceConfiguration.setType("ShardingSphereJDBC");
        targetYamlPipelineDataSourceConfiguration.setParameter("target");
        targetYamlPipelineDataSourceConfiguration.setType("ShardingSphereJDBC");
        when(pipelineConfig.getSource()).thenReturn(sourceYamlPipelineDataSourceConfiguration);
        when(pipelineConfig.getTarget()).thenReturn(targetYamlPipelineDataSourceConfiguration);
        when(prepareTargetTablesParameter.getTablesFirstDataNodes()).thenReturn(new JobDataNodeLine(Collections.emptyList()));
        when(prepareTargetTablesParameter.getPipelineConfiguration()).thenReturn(pipelineConfig);
        when(sourceScalingDataSourceConfig.getType()).thenReturn("ShardingSphereJDBC");
        DataSourceConfiguration mockSourceDataSourceConfig = mock(DataSourceConfiguration.class);
//        when(mockSourceDataSourceConfig.getDataSourceClassName()).thenReturn("com.zaxxer.hikari.HikariDataSource");
        when(sourceScalingDataSourceConfig.getDataSourceConfiguration()).thenReturn(mockSourceDataSourceConfig);
        when(mockShardingSpherePipelineDataSourceCreator.createPipelineDataSource(eq(mockSourceDataSourceConfig))).thenReturn(sourceDataSource);
        when(targetScalingDataSourceConfig.getType()).thenReturn("ShardingSphereJDBC");
        DataSourceConfiguration mockTargetDataSourceConfig = mock(DataSourceConfiguration.class);
//        when(mockTargetDataSourceConfig.getDataSourceClassName()).thenReturn("com.zaxxer.hikari.HikariDataSource");
        when(targetScalingDataSourceConfig.getDataSourceConfiguration()).thenReturn(mockTargetDataSourceConfig);
        when(mockShardingSpherePipelineDataSourceCreator.createPipelineDataSource(eq(mockTargetDataSourceConfig))).thenReturn(targetDataSource);
    }
    
    @After
    public void cleanUp() {
//        Mockito.framework().clearInlineMocks();
    }
    
    @Test
    public void assertGetConnection() throws SQLException {
        try (MockedStatic<PipelineDataSourceConfigurationFactory> mockedStaticPipelineDataSourceConfigurationFactory = mockStatic(PipelineDataSourceConfigurationFactory.class, RETURNS_MOCKS);
             MockedStatic<PipelineDataSourceCreatorFactory> mockedStaticPipelineDataSourceCreatorFactory = mockStatic(PipelineDataSourceCreatorFactory.class, RETURNS_MOCKS)) {
            mockedStaticPipelineDataSourceConfigurationFactory.when(() -> PipelineDataSourceConfigurationFactory.newInstance(eq("ShardingSphereJDBC"), eq("source")))
                    .thenReturn(sourceScalingDataSourceConfig);
            mockedStaticPipelineDataSourceConfigurationFactory.when(() -> PipelineDataSourceConfigurationFactory.newInstance(eq("ShardingSphereJDBC"), eq("target")))
                    .thenReturn(targetScalingDataSourceConfig);
            mockedStaticPipelineDataSourceCreatorFactory.when(() -> PipelineDataSourceCreatorFactory.getInstance(eq("ShardingSphereJDBC")))
                    .thenReturn(mockShardingSpherePipelineDataSourceCreator);
            MySQLDataSourcePreparer mySQLDataSourcePreparer = new MySQLDataSourcePreparer();
            mySQLDataSourcePreparer.prepareTargetTables(prepareTargetTablesParameter);
            verify(sourceDataSource).getConnection();
            verify(targetDataSource).getConnection();
        }
    }
    
    @Test(expected = PipelineJobPrepareFailedException.class)
    public void assertThrowPrepareFailedException() throws SQLException {
        try (MockedStatic<PipelineDataSourceConfigurationFactory> mockedStaticPipelineDataSourceConfigurationFactory = mockStatic(PipelineDataSourceConfigurationFactory.class, RETURNS_MOCKS);
            MockedStatic<PipelineDataSourceCreatorFactory> mockedStaticPipelineDataSourceCreatorFactory = mockStatic(PipelineDataSourceCreatorFactory.class, RETURNS_MOCKS)) {
            mockedStaticPipelineDataSourceConfigurationFactory.when(() -> PipelineDataSourceConfigurationFactory.newInstance(eq("ShardingSphereJDBC"), eq("source")))
                   .thenReturn(sourceScalingDataSourceConfig);
            mockedStaticPipelineDataSourceConfigurationFactory.when(() -> PipelineDataSourceConfigurationFactory.newInstance(eq("ShardingSphereJDBC"), eq("target")))
                   .thenReturn(targetScalingDataSourceConfig);
            mockedStaticPipelineDataSourceCreatorFactory.when(() -> PipelineDataSourceCreatorFactory.getInstance(eq("ShardingSphereJDBC")))
                   .thenReturn(mockShardingSpherePipelineDataSourceCreator);
            when(sourceDataSource.getConnection()).thenThrow(SQLException.class);
            MySQLDataSourcePreparer mySQLDataSourcePreparer = new MySQLDataSourcePreparer();
            mySQLDataSourcePreparer.prepareTargetTables(prepareTargetTablesParameter);
        }
    }
}
