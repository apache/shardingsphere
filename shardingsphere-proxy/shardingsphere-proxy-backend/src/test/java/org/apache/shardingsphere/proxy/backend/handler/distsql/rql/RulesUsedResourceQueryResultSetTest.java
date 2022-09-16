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

package org.apache.shardingsphere.proxy.backend.handler.distsql.rql;

import org.apache.shardingsphere.dbdiscovery.api.config.DatabaseDiscoveryRuleConfiguration;
import org.apache.shardingsphere.dbdiscovery.api.config.rule.DatabaseDiscoveryDataSourceRuleConfiguration;
import org.apache.shardingsphere.dbdiscovery.rule.DatabaseDiscoveryRule;
import org.apache.shardingsphere.distsql.parser.statement.rql.show.ShowRulesUsedResourceStatement;
import org.apache.shardingsphere.encrypt.api.config.EncryptRuleConfiguration;
import org.apache.shardingsphere.encrypt.api.config.rule.EncryptTableRuleConfiguration;
import org.apache.shardingsphere.encrypt.rule.EncryptRule;
import org.apache.shardingsphere.infra.distsql.query.DatabaseDistSQLResultSet;
import org.apache.shardingsphere.infra.distsql.query.DistSQLResultSet;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.resource.ShardingSphereResource;
import org.apache.shardingsphere.infra.metadata.database.rule.ShardingSphereRuleMetaData;
import org.apache.shardingsphere.proxy.backend.handler.distsql.rql.rule.RulesUsedResourceQueryResultSet;
import org.apache.shardingsphere.readwritesplitting.api.ReadwriteSplittingRuleConfiguration;
import org.apache.shardingsphere.readwritesplitting.api.rule.ReadwriteSplittingDataSourceRuleConfiguration;
import org.apache.shardingsphere.readwritesplitting.api.strategy.StaticReadwriteSplittingStrategyConfiguration;
import org.apache.shardingsphere.readwritesplitting.rule.ReadwriteSplittingRule;
import org.apache.shardingsphere.shadow.api.config.ShadowRuleConfiguration;
import org.apache.shardingsphere.shadow.api.config.datasource.ShadowDataSourceConfiguration;
import org.apache.shardingsphere.shadow.rule.ShadowRule;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingAutoTableRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingTableRuleConfiguration;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.apache.shardingsphere.test.mock.MockedDataSource;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class RulesUsedResourceQueryResultSetTest {
    
    @Test
    public void assertGetRowData() {
        DatabaseDistSQLResultSet resultSet = new RulesUsedResourceQueryResultSet();
        ShowRulesUsedResourceStatement sqlStatement = mock(ShowRulesUsedResourceStatement.class);
        when(sqlStatement.getResourceName()).thenReturn(Optional.of("foo_ds"));
        resultSet.init(mockDatabase(), sqlStatement);
        assertShardingTableData(resultSet);
        assertReadwriteSplittingData(resultSet);
        assertDatabaseDiscoveryData(resultSet);
        assertEncryptData(resultSet);
        assertShadowData(resultSet);
        assertFalse(resultSet.next());
    }
    
    private ShardingSphereDatabase mockDatabase() {
        ShardingSphereDatabase result = mock(ShardingSphereDatabase.class);
        ShardingSphereRuleMetaData ruleMetaData = new ShardingSphereRuleMetaData(
                Arrays.asList(mockShardingRule(), mockReadwriteSplittingRule(), mockDatabaseDiscoveryRule(), mockEncryptRule(), mockShadowRule()));
        when(result.getRuleMetaData()).thenReturn(ruleMetaData);
        ShardingSphereResource resource = new ShardingSphereResource("sharding_db", Collections.singletonMap("foo_ds", new MockedDataSource()));
        when(result.getResource()).thenReturn(resource);
        return result;
    }
    
    private ShardingRule mockShardingRule() {
        ShardingRule result = mock(ShardingRule.class);
        ShardingRuleConfiguration config = mock(ShardingRuleConfiguration.class);
        when(config.getTables()).thenReturn(Collections.singleton(new ShardingTableRuleConfiguration("sharding_table")));
        when(config.getAutoTables()).thenReturn(Collections.singleton(new ShardingAutoTableRuleConfiguration("sharding_auto_table", null)));
        when(result.getConfiguration()).thenReturn(config);
        return result;
    }
    
    private ReadwriteSplittingRule mockReadwriteSplittingRule() {
        ReadwriteSplittingRule result = mock(ReadwriteSplittingRule.class);
        ReadwriteSplittingRuleConfiguration config = mock(ReadwriteSplittingRuleConfiguration.class);
        when(config.getDataSources()).thenReturn(Collections.singleton(new ReadwriteSplittingDataSourceRuleConfiguration("readwrite_splitting_source",
                new StaticReadwriteSplittingStrategyConfiguration("foo_ds", Arrays.asList("foo_ds", "bar_ds")), null, "")));
        when(result.getConfiguration()).thenReturn(config);
        return result;
    }
    
    private DatabaseDiscoveryRule mockDatabaseDiscoveryRule() {
        DatabaseDiscoveryRule result = mock(DatabaseDiscoveryRule.class);
        DatabaseDiscoveryRuleConfiguration config = mock(DatabaseDiscoveryRuleConfiguration.class);
        when(config.getDataSources()).thenReturn(Collections.singleton(new DatabaseDiscoveryDataSourceRuleConfiguration("db_discovery_group_name", Arrays.asList("foo_ds", "bar_ds"), "", "")));
        when(result.getConfiguration()).thenReturn(config);
        return result;
    }
    
    private EncryptRule mockEncryptRule() {
        EncryptRule result = mock(EncryptRule.class);
        EncryptRuleConfiguration config = mock(EncryptRuleConfiguration.class);
        when(config.getTables()).thenReturn(Collections.singleton(new EncryptTableRuleConfiguration("encrypt_table", Collections.emptyList(), false)));
        when(result.getConfiguration()).thenReturn(config);
        return result;
    }
    
    private ShadowRule mockShadowRule() {
        ShadowRule result = mock(ShadowRule.class);
        ShadowRuleConfiguration config = mock(ShadowRuleConfiguration.class);
        when(config.getDataSources()).thenReturn(Collections.singletonMap("shadow_source", new ShadowDataSourceConfiguration("foo_ds", "shadow_ds")));
        when(result.getConfiguration()).thenReturn(config);
        return result;
    }
    
    private void assertShardingTableData(final DistSQLResultSet resultSet) {
        Iterator<Object> actual = getActualRowData(resultSet);
        assertThat(actual.next(), is("sharding"));
        assertThat(actual.next(), is("sharding_auto_table"));
        actual = getActualRowData(resultSet);
        assertThat(actual.next(), is("sharding"));
        assertThat(actual.next(), is("sharding_table"));
    }
    
    private void assertReadwriteSplittingData(final DistSQLResultSet resultSet) {
        Iterator<Object> actual = getActualRowData(resultSet);
        assertThat(actual.next(), is("readwrite_splitting"));
        assertThat(actual.next(), is("readwrite_splitting_source"));
        actual = getActualRowData(resultSet);
        assertThat(actual.next(), is("readwrite_splitting"));
        assertThat(actual.next(), is("readwrite_splitting_source"));
    }
    
    private void assertDatabaseDiscoveryData(final DistSQLResultSet resultSet) {
        Iterator<Object> actual = getActualRowData(resultSet);
        assertThat(actual.next(), is("db_discovery"));
        assertThat(actual.next(), is("db_discovery_group_name"));
    }
    
    private void assertEncryptData(final DistSQLResultSet resultSet) {
        Iterator<Object> actual = getActualRowData(resultSet);
        assertThat(actual.next(), is("encrypt"));
        assertThat(actual.next(), is("encrypt_table"));
    }
    
    private void assertShadowData(final DistSQLResultSet resultSet) {
        Iterator<Object> actual = getActualRowData(resultSet);
        assertThat(actual.next(), is("shadow"));
        assertThat(actual.next(), is("shadow_source"));
    }
    
    private Iterator<Object> getActualRowData(final DistSQLResultSet resultSet) {
        assertTrue(resultSet.next());
        Collection<Object> actual = resultSet.getRowData();
        assertThat(actual.size(), is(2));
        return actual.iterator();
    }
    
    @Test
    public void assertGetEmptyRowData() {
        ShardingSphereDatabase database = mockEmptyDatabase();
        DatabaseDistSQLResultSet resultSet = new RulesUsedResourceQueryResultSet();
        ShowRulesUsedResourceStatement sqlStatement = mock(ShowRulesUsedResourceStatement.class);
        when(sqlStatement.getResourceName()).thenReturn(Optional.of("empty_ds"));
        resultSet.init(database, sqlStatement);
        assertFalse(resultSet.next());
    }
    
    private ShardingSphereDatabase mockEmptyDatabase() {
        ShardingSphereDatabase result = mock(ShardingSphereDatabase.class);
        when(result.getRuleMetaData()).thenReturn(new ShardingSphereRuleMetaData(Collections.emptyList()));
        ShardingSphereResource resource = new ShardingSphereResource("sharding_db", Collections.singletonMap("empty_ds", new MockedDataSource()));
        when(result.getResource()).thenReturn(resource);
        return result;
    }
}
