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

package org.apache.shardingsphere.driver.governance.api;

import org.apache.shardingsphere.driver.governance.internal.datasource.GovernanceShardingSphereDataSource;
import org.apache.shardingsphere.governance.repository.api.config.GovernanceCenterConfiguration;
import org.apache.shardingsphere.governance.repository.api.config.GovernanceConfiguration;
import org.apache.shardingsphere.infra.config.RuleConfiguration;
import org.junit.Test;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class GovernanceShardingSphereDataSourceFactoryTest {
    
    private static final String TABLE_TYPE = "TABLE";
    
    @Test
    public void assertCreateDataSourceWhenRuleConfigurationsNotEmpty() throws SQLException {
        DataSource dataSource = GovernanceShardingSphereDataSourceFactory.createDataSource(createDataSourceMap(), Collections.singletonList(mock(RuleConfiguration.class)),
                new Properties(), createGovernanceConfiguration());
        assertTrue(dataSource instanceof GovernanceShardingSphereDataSource);
    }
    
    @Test
    public void assertCreateDataSourceWithGivenDataSource() throws SQLException {
        DataSource dataSource = GovernanceShardingSphereDataSourceFactory.createDataSource(createDataSource(), Collections.singletonList(mock(RuleConfiguration.class)),
                new Properties(), createGovernanceConfiguration());
        assertTrue(dataSource instanceof GovernanceShardingSphereDataSource);
    }
    
    private Map<String, DataSource> createDataSourceMap() throws SQLException {
        Map<String, DataSource> result = new HashMap<>(1, 1);
        result.put("dataSourceMapKey", createDataSource());
        return result;
    }
    
    private DataSource createDataSource() throws SQLException {
        DataSource result = mock(DataSource.class);
        Connection connection = mock(Connection.class);
        DatabaseMetaData databaseMetaData = mock(DatabaseMetaData.class);
        when(connection.getMetaData()).thenReturn(databaseMetaData);
        when(databaseMetaData.getURL()).thenReturn("jdbc:mysql://localhost:3306/mysql?serverTimezone=GMT%2B8");
        ResultSet resultSet = mock(ResultSet.class);
        when(databaseMetaData.getTables(null, null, null, new String[]{TABLE_TYPE})).thenReturn(resultSet);
        when(result.getConnection()).thenReturn(connection);
        return result;
    }
    
    private GovernanceConfiguration createGovernanceConfiguration() {
        GovernanceConfiguration result = mock(GovernanceConfiguration.class);
        GovernanceCenterConfiguration governanceCenterConfig = mock(GovernanceCenterConfiguration.class);
        when(result.getRegistryCenterConfiguration()).thenReturn(governanceCenterConfig);
        when(governanceCenterConfig.getType()).thenReturn("REG_TEST");
        return result;
    }
}

