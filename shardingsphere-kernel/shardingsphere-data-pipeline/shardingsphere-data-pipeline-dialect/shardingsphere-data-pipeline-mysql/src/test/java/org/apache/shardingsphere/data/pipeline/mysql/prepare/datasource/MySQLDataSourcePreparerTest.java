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
import org.apache.shardingsphere.data.pipeline.api.datasource.config.impl.ShardingSpherePipelineDataSourceConfiguration;
import org.apache.shardingsphere.data.pipeline.api.datasource.config.yaml.YamlPipelineDataSourceConfiguration;
import org.apache.shardingsphere.data.pipeline.api.prepare.datasource.PrepareTargetTablesParameter;
import org.apache.shardingsphere.data.pipeline.core.datasource.creator.PipelineDataSourceCreatorFactory;
import org.apache.shardingsphere.data.pipeline.core.exception.PipelineJobPrepareFailedException;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Collections;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

// FIX test cases
@RunWith(MockitoJUnitRunner.class)
public final class MySQLDataSourcePreparerTest {
    
    @Mock
    private PrepareTargetTablesParameter prepareTargetTablesParameter;
    
    @Mock
    private PipelineConfiguration pipelineConfig;
    
    @Mock
    private YamlPipelineDataSourceConfiguration sourceYamlPipelineDataSourceConfiguration;
    
    @Mock
    private YamlPipelineDataSourceConfiguration targetYamlPipelineDataSourceConfiguration;
    
    @Mock
    private ShardingSpherePipelineDataSourceConfiguration sourceScalingDataSourceConfig;
    
    @Mock
    private ShardingSpherePipelineDataSourceConfiguration targetScalingDataSourceConfig;
    
    @Mock(extraInterfaces = AutoCloseable.class)
    private DataSource sourceDataSource;
    
    @Mock(extraInterfaces = AutoCloseable.class)
    private DataSource targetDataSource;
    
    @Before
    public void setUp() throws SQLException {
        when(prepareTargetTablesParameter.getPipelineConfiguration()).thenReturn(pipelineConfig);
        when(prepareTargetTablesParameter.getTablesFirstDataNodes()).thenReturn(new JobDataNodeLine(Collections.emptyList()));
        when(pipelineConfig.getSource()).thenReturn(sourceYamlPipelineDataSourceConfiguration);
        when(PipelineDataSourceCreatorFactory.getInstance(
                sourceScalingDataSourceConfig.getType()).createPipelineDataSource(sourceScalingDataSourceConfig.getDataSourceConfiguration())).thenReturn(sourceDataSource);
        when(pipelineConfig.getTarget()).thenReturn(targetYamlPipelineDataSourceConfiguration);
        when(PipelineDataSourceCreatorFactory.getInstance(
                targetScalingDataSourceConfig.getType()).createPipelineDataSource(targetScalingDataSourceConfig.getDataSourceConfiguration())).thenReturn(targetDataSource);
    }
    
    @Test
    @Ignore
    public void assertGetConnection() throws SQLException {
        MySQLDataSourcePreparer mySQLDataSourcePreparer = new MySQLDataSourcePreparer();
        mySQLDataSourcePreparer.prepareTargetTables(prepareTargetTablesParameter);
        verify(sourceDataSource).getConnection();
        verify(targetDataSource).getConnection();
    }
    
    @Test(expected = PipelineJobPrepareFailedException.class)
    @Ignore
    public void assertThrowPrepareFailedException() throws SQLException {
        when(sourceDataSource.getConnection()).thenThrow(SQLException.class);
        MySQLDataSourcePreparer mySQLDataSourcePreparer = new MySQLDataSourcePreparer();
        mySQLDataSourcePreparer.prepareTargetTables(prepareTargetTablesParameter);
    }
}
