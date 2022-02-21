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

package org.apache.shardingsphere.proxy.backend.text.distsql.rql;

import com.zaxxer.hikari.HikariDataSource;
import org.apache.shardingsphere.dbdiscovery.api.config.DatabaseDiscoveryRuleConfiguration;
import org.apache.shardingsphere.dbdiscovery.api.config.rule.DatabaseDiscoveryDataSourceRuleConfiguration;
import org.apache.shardingsphere.distsql.parser.statement.rql.show.ShowRulesUsedResourceStatement;
import org.apache.shardingsphere.encrypt.api.config.EncryptRuleConfiguration;
import org.apache.shardingsphere.encrypt.api.config.rule.EncryptTableRuleConfiguration;
import org.apache.shardingsphere.infra.config.RuleConfiguration;
import org.apache.shardingsphere.infra.distsql.query.DistSQLResultSet;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.resource.ShardingSphereResource;
import org.apache.shardingsphere.infra.metadata.rule.ShardingSphereRuleMetaData;
import org.apache.shardingsphere.proxy.backend.text.distsql.rql.rule.RulesUsedResourceQueryResultSet;
import org.apache.shardingsphere.readwritesplitting.api.ReadwriteSplittingRuleConfiguration;
import org.apache.shardingsphere.readwritesplitting.api.rule.ReadwriteSplittingDataSourceRuleConfiguration;
import org.apache.shardingsphere.shadow.api.config.ShadowRuleConfiguration;
import org.apache.shardingsphere.shadow.api.config.datasource.ShadowDataSourceConfiguration;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingAutoTableRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingTableRuleConfiguration;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Optional;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class RulesUsedResourceQueryResultSetTest {
    
    @Mock
    private ShardingSphereMetaData shardingSphereMetaData;
    
    @Test
    public void assertGetRowDataForSharding() {
        init(mockShardingTableRule());
        DistSQLResultSet resultSet = new RulesUsedResourceQueryResultSet();
        ShowRulesUsedResourceStatement sqlStatement = mock(ShowRulesUsedResourceStatement.class);
        when(sqlStatement.getResourceName()).thenReturn(Optional.of("ds_0"));
        resultSet.init(shardingSphereMetaData, sqlStatement);
        Collection<Object> actual = resultSet.getRowData();
        assertThat(actual.size(), is(2));
        Iterator<Object> rowData = actual.iterator();
        assertThat(rowData.next(), is("sharding"));
        assertThat(rowData.next(), is("sharding_auto_table"));
        resultSet.next();
        actual = resultSet.getRowData();
        assertThat(actual.size(), is(2));
        rowData = actual.iterator();
        assertThat(rowData.next(), is("sharding"));
        assertThat(rowData.next(), is("sharding_table"));
    }
    
    @Test
    public void assertGetRowDataForReadwriteSplitting() {
        init(mockReadwriteSplittingRule());
        DistSQLResultSet resultSet = new RulesUsedResourceQueryResultSet();
        ShowRulesUsedResourceStatement sqlStatement = mock(ShowRulesUsedResourceStatement.class);
        when(sqlStatement.getResourceName()).thenReturn(Optional.of("ds_0"));
        resultSet.init(shardingSphereMetaData, sqlStatement);
        Collection<Object> actual = resultSet.getRowData();
        assertThat(actual.size(), is(2));
        Iterator<Object> rowData = actual.iterator();
        assertThat(rowData.next(), is("readwrite_splitting"));
        assertThat(rowData.next(), is("readwrite_splitting_source"));
    }
    
    @Test
    public void assertGetRowDataForDBDiscovery() {
        init(mockDBDiscoveryRule());
        DistSQLResultSet resultSet = new RulesUsedResourceQueryResultSet();
        ShowRulesUsedResourceStatement sqlStatement = mock(ShowRulesUsedResourceStatement.class);
        when(sqlStatement.getResourceName()).thenReturn(Optional.of("ds_0"));
        resultSet.init(shardingSphereMetaData, sqlStatement);
        Collection<Object> actual = resultSet.getRowData();
        assertThat(actual.size(), is(2));
        Iterator<Object> rowData = actual.iterator();
        rowData = actual.iterator();
        assertThat(rowData.next(), is("db_discovery"));
        assertThat(rowData.next(), is("db_discovery_group_name"));
    }
    
    @Test
    public void assertGetRowDataForEncryptRule() {
        init(mockEncryptRule());
        DistSQLResultSet resultSet = new RulesUsedResourceQueryResultSet();
        ShowRulesUsedResourceStatement sqlStatement = mock(ShowRulesUsedResourceStatement.class);
        when(sqlStatement.getResourceName()).thenReturn(Optional.of("ds_0"));
        resultSet.init(shardingSphereMetaData, sqlStatement);
        Collection<Object> actual = resultSet.getRowData();
        assertThat(actual.size(), is(2));
        Iterator<Object> rowData = actual.iterator();
        assertThat(rowData.next(), is("encrypt"));
        assertThat(rowData.next(), is("encrypt_table"));
    }
    
    @Test
    public void assertGetRowDataForShadowRule() {
        init(mockShadowRule());
        DistSQLResultSet resultSet = new RulesUsedResourceQueryResultSet();
        ShowRulesUsedResourceStatement sqlStatement = mock(ShowRulesUsedResourceStatement.class);
        when(sqlStatement.getResourceName()).thenReturn(Optional.of("ds_0"));
        resultSet.init(shardingSphereMetaData, sqlStatement);
        Collection<Object> actual = resultSet.getRowData();
        assertThat(actual.size(), is(2));
        Iterator<Object> rowData = actual.iterator();
        assertThat(rowData.next(), is("shadow"));
        assertThat(rowData.next(), is("shadow_source"));
    }
    
    private void init(final RuleConfiguration ruleConfiguration) {
        ShardingSphereRuleMetaData ruleMetaData = mock(ShardingSphereRuleMetaData.class);
        when(ruleMetaData.getConfigurations()).thenReturn(Collections.singletonList(ruleConfiguration));
        when(shardingSphereMetaData.getRuleMetaData()).thenReturn(ruleMetaData);
        when(shardingSphereMetaData.getResource()).thenReturn(createResource());
    }
    
    private ShardingSphereResource createResource() {
        return new ShardingSphereResource(Collections.singletonMap("ds_0", new HikariDataSource()), null, null, null);
    }
    
    private RuleConfiguration mockShardingTableRule() {
        ShardingRuleConfiguration result = mock(ShardingRuleConfiguration.class);
        when(result.getTables()).thenReturn(Collections.singletonList(new ShardingTableRuleConfiguration("sharding_table")));
        when(result.getAutoTables()).thenReturn(Collections.singletonList(new ShardingAutoTableRuleConfiguration("sharding_auto_table")));
        return result;
    }
    
    private RuleConfiguration mockReadwriteSplittingRule() {
        ReadwriteSplittingRuleConfiguration result = mock(ReadwriteSplittingRuleConfiguration.class);
        Properties props = new Properties();
        props.setProperty("write-data-source-name", "ds_0");
        props.setProperty("read-data-source-names", "read_0,read_1");
        when(result.getDataSources()).thenReturn(Collections.singletonList(new ReadwriteSplittingDataSourceRuleConfiguration("readwrite_splitting_source", "", props, "")));
        return result;
    }
    
    private RuleConfiguration mockDBDiscoveryRule() {
        DatabaseDiscoveryRuleConfiguration result = mock(DatabaseDiscoveryRuleConfiguration.class);
        when(result.getDataSources()).thenReturn(Collections.singletonList(new DatabaseDiscoveryDataSourceRuleConfiguration("db_discovery_group_name", Arrays.asList("ds_0", "ds_1"), "", "")));
        return result;
    }
    
    private RuleConfiguration mockEncryptRule() {
        EncryptRuleConfiguration result = mock(EncryptRuleConfiguration.class);
        when(result.getTables()).thenReturn(Collections.singletonList(new EncryptTableRuleConfiguration("encrypt_table", Collections.emptyList(), false)));
        return result;
    }
    
    private RuleConfiguration mockShadowRule() {
        ShadowRuleConfiguration result = mock(ShadowRuleConfiguration.class);
        when(result.getDataSources()).thenReturn(Collections.singletonMap("shadow_source", new ShadowDataSourceConfiguration("ds_0", "shadow_ds")));
        return result;
    }
}
