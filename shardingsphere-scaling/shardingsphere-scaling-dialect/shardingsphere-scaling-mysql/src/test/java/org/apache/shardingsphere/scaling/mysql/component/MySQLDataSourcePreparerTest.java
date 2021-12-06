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

import org.apache.shardingsphere.infra.config.datasource.typed.ShardingSphereJDBCDataSourceConfiguration;
import org.apache.shardingsphere.infra.config.datasource.typed.TypedDataSourceConfigurationWrap;
import org.apache.shardingsphere.infra.yaml.config.pojo.YamlRootConfiguration;
import org.apache.shardingsphere.scaling.core.common.exception.PrepareFailedException;
import org.apache.shardingsphere.scaling.core.config.JobConfiguration;
import org.apache.shardingsphere.scaling.core.config.RuleConfiguration;
import org.apache.shardingsphere.scaling.mysql.component.checker.MySQLDataSourcePreparer;
import org.apache.shardingsphere.sharding.yaml.config.YamlShardingRuleConfiguration;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Collections;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class MySQLDataSourcePreparerTest {

    @Mock
    private JobConfiguration jobConfiguration;

    @Mock
    private RuleConfiguration ruleConfiguration;

    @Mock
    private TypedDataSourceConfigurationWrap sourceDataSourceConfigurationWrap;

    @Mock
    private TypedDataSourceConfigurationWrap targetDataSourceConfigurationWrap;

    @Mock
    private ShardingSphereJDBCDataSourceConfiguration sourceScalingDataSourceConfiguration;

    @Mock
    private ShardingSphereJDBCDataSourceConfiguration targetScalingDataSourceConfiguration;

    @Mock(extraInterfaces = AutoCloseable.class)
    private DataSource sourceDataSource;

    @Mock(extraInterfaces = AutoCloseable.class)
    private DataSource targetDataSource;

    @Mock
    private YamlRootConfiguration yamlRootConfiguration;

    @Mock
    private YamlShardingRuleConfiguration yamlShardingRuleConfiguration;

    @Before
    public void setUp() throws SQLException {
        when(jobConfiguration.getRuleConfig()).thenReturn(ruleConfiguration);
        when(ruleConfiguration.getSource()).thenReturn(sourceDataSourceConfigurationWrap);
        when(sourceDataSourceConfigurationWrap.unwrap()).thenReturn(sourceScalingDataSourceConfiguration);
        when(sourceScalingDataSourceConfiguration.toDataSource()).thenReturn(sourceDataSource);
        when(sourceScalingDataSourceConfiguration.getRootConfig()).thenReturn(yamlRootConfiguration);
        when(yamlRootConfiguration.getRules()).thenReturn(Collections.singletonList(yamlShardingRuleConfiguration));
        when(ruleConfiguration.getTarget()).thenReturn(targetDataSourceConfigurationWrap);
        when(targetDataSourceConfigurationWrap.unwrap()).thenReturn(targetScalingDataSourceConfiguration);
        when(targetScalingDataSourceConfiguration.toDataSource()).thenReturn(targetDataSource);
    }

    @Test
    public void assertGetConnection() throws SQLException {
        MySQLDataSourcePreparer mySQLDataSourcePreparer = new MySQLDataSourcePreparer();
        mySQLDataSourcePreparer.prepareTargetTables(jobConfiguration);
        verify(sourceDataSource).getConnection();
        verify(targetDataSource).getConnection();
    }

    @Test(expected = PrepareFailedException.class)
    public void assertThrowPrepareFailedException() throws SQLException {
        when(sourceDataSource.getConnection()).thenThrow(SQLException.class);
        MySQLDataSourcePreparer mySQLDataSourcePreparer = new MySQLDataSourcePreparer();
        mySQLDataSourcePreparer.prepareTargetTables(jobConfiguration);
    }
}
