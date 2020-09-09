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

package org.apache.shardingsphere.infra.metadata.schema;

import org.apache.shardingsphere.infra.config.properties.ConfigurationProperties;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.metadata.fixture.rule.CommonFixtureRule;
import org.apache.shardingsphere.infra.metadata.fixture.rule.DataNodeRoutedFixtureRule;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class RuleSchemaMetaDataLoaderTest {
    
    @Mock
    private DatabaseType databaseType;
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private DataSource dataSource;
    
    @Mock
    private ConfigurationProperties props;
    
    private final RuleSchemaMetaDataLoader loader = new RuleSchemaMetaDataLoader(Arrays.asList(new CommonFixtureRule(), new DataNodeRoutedFixtureRule()));
    
    @Before
    public void setUp() throws SQLException {
        ResultSet resultSet = mockResultSet();
        when(dataSource.getConnection().getMetaData().getTables(any(), any(), any(), any())).thenReturn(resultSet);
    }
    
    private ResultSet mockResultSet() throws SQLException {
        ResultSet result = mock(ResultSet.class);
        when(result.next()).thenReturn(true, true, false);
        when(result.getString("TABLE_NAME")).thenReturn("unconfigured_table_0", "unconfigured_table_1");
        return result;
    }
    
    @Test
    public void assertSyncLoadFullDatabase() throws SQLException {
        assertRuleSchemaMetaData(loader.load(databaseType, dataSource, props));
    }
    
    @Test
    public void assertAsyncLoadFullDatabase() throws SQLException {
        assertRuleSchemaMetaData(loader.load(databaseType, dataSource, props));
    }
    
    private void assertRuleSchemaMetaData(final RuleSchemaMetaData actual) {
        assertThat(actual.getConfiguredSchemaMetaData().getAllTableNames().size(), is(4));
        assertTrue(actual.getConfiguredSchemaMetaData().containsTable("common_table_0"));
        assertTrue(actual.getConfiguredSchemaMetaData().containsTable("common_table_1"));
        assertTrue(actual.getConfiguredSchemaMetaData().containsTable("data_node_routed_table_0"));
        assertTrue(actual.getConfiguredSchemaMetaData().get("data_node_routed_table_0").getColumns().containsKey("id"));
        assertTrue(actual.getConfiguredSchemaMetaData().containsTable("data_node_routed_table_1"));
        assertTrue(actual.getConfiguredSchemaMetaData().get("data_node_routed_table_1").getColumns().containsKey("id"));
        assertThat(actual.getUnconfiguredSchemaMetaDataMap().size(), is(1));
        assertTrue(actual.getUnconfiguredSchemaMetaDataMap().containsKey("logic_db"));
        assertTrue(actual.getUnconfiguredSchemaMetaDataMap().get("logic_db").containsTable("unconfigured_table_0"));
        assertTrue(actual.getUnconfiguredSchemaMetaDataMap().get("logic_db").containsTable("unconfigured_table_1"));
    }
    
    @Test
    public void assertLoadWithExistedTableName() throws SQLException {
        assertTrue(loader.load(databaseType, dataSource, "data_node_routed_table_0", props).isPresent());
    }
    
    @Test
    public void assertLoadWithNotExistedTableName() throws SQLException {
        assertFalse(loader.load(databaseType, dataSource, "invalid_table", props).isPresent());
    }
}
