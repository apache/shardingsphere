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

import org.apache.shardingsphere.driver.governance.fixture.TestRuleConfiguration;
import org.apache.shardingsphere.driver.jdbc.core.datasource.ShardingSphereDataSource;
import org.apache.shardingsphere.governance.repository.api.config.RegistryCenterConfiguration;
import org.apache.shardingsphere.infra.mode.config.ModeConfiguration;
import org.junit.Test;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class GovernanceShardingSphereDataSourceFactoryTest {
    
    private static final String TABLE_TYPE = "TABLE";
    
    private static final String VIEW_TYPE = "VIEW";
    
    @Test
    public void assertCreateDataSourceWhenRuleConfigurationsNotEmpty() throws SQLException {
        DataSource dataSource = GovernanceShardingSphereDataSourceFactory.createDataSource(
                createModeConfiguration(), createDataSourceMap(), Collections.singletonList(new TestRuleConfiguration()), new Properties());
        assertTrue(dataSource instanceof ShardingSphereDataSource);
    }
    
    @Test
    public void assertCreateDataSourceWithGivenDataSource() throws SQLException {
        DataSource dataSource = GovernanceShardingSphereDataSourceFactory.createDataSource(
                createModeConfiguration(), createDataSource(), Collections.singletonList(new TestRuleConfiguration()), new Properties());
        assertTrue(dataSource instanceof ShardingSphereDataSource);
    }
    
    private ModeConfiguration createModeConfiguration() {
        return new ModeConfiguration("Cluster", new RegistryCenterConfiguration("GOV_TEST", "test", "", null), false);
    }
    
    private Map<String, DataSource> createDataSourceMap() throws SQLException {
        Map<String, DataSource> result = new HashMap<>(1, 1);
        result.put("dataSourceMapKey", createDataSource());
        return result;
    }
    
    private DataSource createDataSource() throws SQLException {
        DataSource result = mock(DataSource.class, RETURNS_DEEP_STUBS);
        when(result.getConnection().getMetaData().getURL()).thenReturn("jdbc:mysql://localhost:3306/mysql?serverTimezone=GMT%2B8");
        ResultSet resultSet = mock(ResultSet.class);
        when(result.getConnection().getMetaData().getTables(null, null, null, new String[]{TABLE_TYPE, VIEW_TYPE})).thenReturn(resultSet);
        return result;
    }
}
