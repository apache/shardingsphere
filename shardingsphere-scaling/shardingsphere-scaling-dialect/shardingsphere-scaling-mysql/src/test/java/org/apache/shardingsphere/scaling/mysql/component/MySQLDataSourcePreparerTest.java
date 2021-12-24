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

package org.apache.shardingsphere.scaling.mysql.component;

import org.apache.shardingsphere.data.pipeline.api.config.rulealtered.RuleConfiguration;
import org.apache.shardingsphere.data.pipeline.api.datanode.JobDataNodeLine;
import org.apache.shardingsphere.data.pipeline.api.prepare.datasource.PrepareTargetTablesParameter;
import org.apache.shardingsphere.data.pipeline.core.exception.PipelineJobPrepareFailedException;
import org.apache.shardingsphere.infra.config.datasource.jdbc.config.JDBCDataSourceConfigurationWrapper;
import org.apache.shardingsphere.infra.config.datasource.jdbc.config.yaml.JDBCDataSourceYamlConfigurationSwapper;
import org.apache.shardingsphere.infra.config.datasource.jdbc.config.impl.ShardingSphereJDBCDataSourceConfiguration;
import org.apache.shardingsphere.infra.config.datasource.jdbc.creator.JDBCDataSourceCreatorFactory;
import org.apache.shardingsphere.scaling.mysql.component.checker.MySQLDataSourcePreparer;
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
    private RuleConfiguration ruleConfig;
    
    @Mock
    private JDBCDataSourceConfigurationWrapper sourceDataSourceConfigWrapper;
    
    @Mock
    private JDBCDataSourceConfigurationWrapper targetDataSourceConfigWrapper;
    
    @Mock
    private ShardingSphereJDBCDataSourceConfiguration sourceScalingDataSourceConfig;
    
    @Mock
    private ShardingSphereJDBCDataSourceConfiguration targetScalingDataSourceConfig;
    
    @Mock(extraInterfaces = AutoCloseable.class)
    private DataSource sourceDataSource;
    
    @Mock(extraInterfaces = AutoCloseable.class)
    private DataSource targetDataSource;
    
    @Before
    public void setUp() throws SQLException {
        when(prepareTargetTablesParameter.getRuleConfig()).thenReturn(ruleConfig);
        when(prepareTargetTablesParameter.getTablesFirstDataNodes()).thenReturn(new JobDataNodeLine(Collections.emptyList()));
        when(ruleConfig.getSource()).thenReturn(new JDBCDataSourceYamlConfigurationSwapper().swapToYamlConfiguration(sourceDataSourceConfigWrapper));
        when(sourceDataSourceConfigWrapper.unwrap()).thenReturn(sourceScalingDataSourceConfig);
        when(JDBCDataSourceCreatorFactory.getInstance(
                sourceScalingDataSourceConfig.getType()).createDataSource(sourceScalingDataSourceConfig.getDataSourceConfiguration())).thenReturn(sourceDataSource);
        when(ruleConfig.getTarget()).thenReturn(new JDBCDataSourceYamlConfigurationSwapper().swapToYamlConfiguration(targetDataSourceConfigWrapper));
        when(JDBCDataSourceCreatorFactory.getInstance(
                targetScalingDataSourceConfig.getType()).createDataSource(targetScalingDataSourceConfig.getDataSourceConfiguration())).thenReturn(targetDataSource);
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
