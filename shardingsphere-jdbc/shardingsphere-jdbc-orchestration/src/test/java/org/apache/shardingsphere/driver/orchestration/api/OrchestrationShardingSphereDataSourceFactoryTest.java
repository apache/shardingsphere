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

package org.apache.shardingsphere.driver.orchestration.api;

import lombok.SneakyThrows;
import org.apache.shardingsphere.cluster.configuration.config.ClusterConfiguration;
import org.apache.shardingsphere.driver.orchestration.internal.datasource.OrchestrationShardingSphereDataSource;
import org.apache.shardingsphere.infra.config.RuleConfiguration;
import org.apache.shardingsphere.metrics.configuration.config.MetricsConfiguration;
import org.apache.shardingsphere.orchestration.repository.api.config.OrchestrationCenterConfiguration;
import org.apache.shardingsphere.orchestration.repository.api.config.OrchestrationConfiguration;
import org.junit.Test;

import javax.sql.DataSource;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class OrchestrationShardingSphereDataSourceFactoryTest {
    private static final String TABLE_TYPE = "TABLE";
    
    @SneakyThrows
    @Test
    public void assertCreateDataSourceWhenRuleConfigurationsNotEmpty() {
        DataSource dataSource = OrchestrationShardingSphereDataSourceFactory.createDataSource(createDataSourceMap(), Collections.singletonList(mock(RuleConfiguration.class)),
                new Properties(), createOrchestrationConfig(), mock(ClusterConfiguration.class), mock(MetricsConfiguration.class));
        assertTrue(dataSource instanceof OrchestrationShardingSphereDataSource);
    }
    
    @SneakyThrows
    @Test
    public void assertCreateDataSourceWhenRuleConfigurationsNotEmptyWithClusterConfigurationAndMetricsConfigurationBothDefault() {
        DataSource dataSource = OrchestrationShardingSphereDataSourceFactory.createDataSource(createDataSourceMap(), Collections.singletonList(mock(RuleConfiguration.class)),
                new Properties(), createOrchestrationConfig());
        assertTrue(dataSource instanceof OrchestrationShardingSphereDataSource);
    }
    
    @SneakyThrows
    @Test
    public void assertCreateDataSourceWithGivenDataSource() {
        DataSource dataSource = OrchestrationShardingSphereDataSourceFactory.createDataSource(createDataSource(), Collections.singletonList(mock(RuleConfiguration.class)),
                new Properties(), createOrchestrationConfig(), mock(ClusterConfiguration.class), mock(MetricsConfiguration.class));
        assertTrue(dataSource instanceof OrchestrationShardingSphereDataSource);
    }
    
    @SneakyThrows
    @Test
    public void assertCreateDataSourceWithGivenDataSourceWithClusterConfigurationAndMetricsConfigurationBothDefault() {
        DataSource dataSource = OrchestrationShardingSphereDataSourceFactory.createDataSource(createDataSource(), Collections.singletonList(mock(RuleConfiguration.class)),
                new Properties(), createOrchestrationConfig());
        assertTrue(dataSource instanceof OrchestrationShardingSphereDataSource);
    }
    
    private Map<String, DataSource> createDataSourceMap() {
        Map<String, DataSource> dataSourceMap = new HashMap<>();
        dataSourceMap.put("dataSourceMapKey", createDataSource());
        return dataSourceMap;
    }
    
    @SneakyThrows
    private DataSource createDataSource() {
        DataSource baseDataSource = mock(DataSource.class);
        Connection connection = mock(Connection.class);
        DatabaseMetaData databaseMetaData = mock(DatabaseMetaData.class);
        when(connection.getMetaData()).thenReturn(databaseMetaData);
        when(databaseMetaData.getURL()).thenReturn("jdbc:mysql://localhost:3306/mysql?serverTimezone=GMT%2B8");
        ResultSet resultSet = mock(ResultSet.class);
        when(databaseMetaData.getTables(null, null, null, new String[]{TABLE_TYPE})).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(false);
        when(baseDataSource.getConnection()).thenReturn(connection);
        return baseDataSource;
    }
    
    private OrchestrationConfiguration createOrchestrationConfig() {
        OrchestrationConfiguration orchestrationConfig = mock(OrchestrationConfiguration.class);
        OrchestrationCenterConfiguration orchestrationCenterConfiguration = mock(OrchestrationCenterConfiguration.class);
        when(orchestrationConfig.getRegistryCenterConfiguration()).thenReturn(orchestrationCenterConfiguration);
        when(orchestrationCenterConfiguration.getType()).thenReturn("REG_TEST");
        return orchestrationConfig;
    }
}
