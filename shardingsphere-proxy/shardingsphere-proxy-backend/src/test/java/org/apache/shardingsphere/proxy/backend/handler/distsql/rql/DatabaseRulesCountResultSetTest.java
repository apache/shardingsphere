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

import org.apache.shardingsphere.dbdiscovery.rule.DatabaseDiscoveryRule;
import org.apache.shardingsphere.distsql.parser.statement.rql.show.CountDatabaseRulesStatement;
import org.apache.shardingsphere.encrypt.api.config.EncryptRuleConfiguration;
import org.apache.shardingsphere.encrypt.api.config.rule.EncryptTableRuleConfiguration;
import org.apache.shardingsphere.encrypt.rule.EncryptRule;
import org.apache.shardingsphere.infra.distsql.query.DatabaseDistSQLResultSet;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.rule.ShardingSphereRuleMetaData;
import org.apache.shardingsphere.proxy.backend.handler.distsql.rql.rule.DatabaseRulesCountResultSet;
import org.apache.shardingsphere.readwritesplitting.api.ReadwriteSplittingRuleConfiguration;
import org.apache.shardingsphere.readwritesplitting.api.rule.ReadwriteSplittingDataSourceRuleConfiguration;
import org.apache.shardingsphere.readwritesplitting.api.strategy.StaticReadwriteSplittingStrategyConfiguration;
import org.apache.shardingsphere.readwritesplitting.rule.ReadwriteSplittingRule;
import org.apache.shardingsphere.shadow.rule.ShadowRule;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingAutoTableRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingTableRuleConfiguration;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.apache.shardingsphere.singletable.rule.SingleTableRule;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class DatabaseRulesCountResultSetTest {
    
    @Test
    public void assertGetRowData() {
        DatabaseDistSQLResultSet resultSet = new DatabaseRulesCountResultSet();
        resultSet.init(mockDatabase(), mock(CountDatabaseRulesStatement.class));
        Collection<Object> actual = resultSet.getRowData();
        assertThat(actual.size(), is(2));
        Iterator<Object> rowData = actual.iterator();
        assertThat(rowData.next(), is("single_table"));
        assertThat(rowData.next(), is(2));
        resultSet.next();
        actual = resultSet.getRowData();
        rowData = actual.iterator();
        assertThat(rowData.next(), is("sharding_table"));
        assertThat(rowData.next(), is(2));
        resultSet.next();
        actual = resultSet.getRowData();
        rowData = actual.iterator();
        assertThat(rowData.next(), is("sharding_binding_table"));
        assertThat(rowData.next(), is(1));
        resultSet.next();
        actual = resultSet.getRowData();
        rowData = actual.iterator();
        assertThat(rowData.next(), is("sharding_broadcast_table"));
        assertThat(rowData.next(), is(2));
        resultSet.next();
        actual = resultSet.getRowData();
        rowData = actual.iterator();
        assertThat(rowData.next(), is("readwrite_splitting"));
        assertThat(rowData.next(), is(1));
        resultSet.next();
        actual = resultSet.getRowData();
        rowData = actual.iterator();
        assertThat(rowData.next(), is("db_discovery"));
        assertThat(rowData.next(), is(0));
        resultSet.next();
        actual = resultSet.getRowData();
        rowData = actual.iterator();
        assertThat(rowData.next(), is("encrypt"));
        assertThat(rowData.next(), is(1));
    }
    
    private ShardingSphereDatabase mockDatabase() {
        ShardingSphereDatabase result = mock(ShardingSphereDatabase.class);
        ShardingSphereRuleMetaData ruleMetaData = mock(ShardingSphereRuleMetaData.class);
        SingleTableRule singleTableRule = mockSingleTableRule();
        when(ruleMetaData.getSingleRule(SingleTableRule.class)).thenReturn(singleTableRule);
        ShardingRule shardingRule = mockShardingTableRule();
        when(ruleMetaData.findSingleRule(ShardingRule.class)).thenReturn(Optional.of(shardingRule));
        ReadwriteSplittingRule readwriteSplittingRule = mockReadwriteSplittingRule();
        when(ruleMetaData.findSingleRule(ReadwriteSplittingRule.class)).thenReturn(Optional.of(readwriteSplittingRule));
        when(ruleMetaData.findSingleRule(DatabaseDiscoveryRule.class)).thenReturn(Optional.empty());
        when(ruleMetaData.findSingleRule(ShadowRule.class)).thenReturn(Optional.empty());
        EncryptRule encryptRule = mockEncryptRule();
        when(ruleMetaData.findSingleRule(EncryptRule.class)).thenReturn(Optional.of(encryptRule));
        when(result.getRuleMetaData()).thenReturn(ruleMetaData);
        return result;
    }
    
    private SingleTableRule mockSingleTableRule() {
        SingleTableRule result = mock(SingleTableRule.class);
        when(result.getAllTables()).thenReturn(Arrays.asList("single_table_1", "single_table_2"));
        return result;
    }
    
    private ShardingRule mockShardingTableRule() {
        ShardingRule result = mock(ShardingRule.class);
        ShardingRuleConfiguration config = mock(ShardingRuleConfiguration.class);
        when(config.getTables()).thenReturn(Collections.singletonList(new ShardingTableRuleConfiguration("sharding_table")));
        when(config.getAutoTables()).thenReturn(Collections.singletonList(new ShardingAutoTableRuleConfiguration("sharding_auto_table", null)));
        when(config.getBindingTableGroups()).thenReturn(Collections.singletonList("binding_table_1,binding_table_2"));
        when(config.getBroadcastTables()).thenReturn(Arrays.asList("broadcast_table_1", "broadcast_table_2"));
        when(result.getConfiguration()).thenReturn(config);
        return result;
    }
    
    private ReadwriteSplittingRule mockReadwriteSplittingRule() {
        ReadwriteSplittingRule result = mock(ReadwriteSplittingRule.class);
        ReadwriteSplittingRuleConfiguration config = mock(ReadwriteSplittingRuleConfiguration.class);
        when(config.getDataSources()).thenReturn(Collections.singletonList(new ReadwriteSplittingDataSourceRuleConfiguration("readwrite_splitting",
                new StaticReadwriteSplittingStrategyConfiguration("", Collections.emptyList()), null, "")));
        when(result.getConfiguration()).thenReturn(config);
        return result;
    }
    
    private EncryptRule mockEncryptRule() {
        EncryptRule result = mock(EncryptRule.class);
        EncryptRuleConfiguration config = mock(EncryptRuleConfiguration.class);
        when(config.getTables()).thenReturn(Collections.singletonList(new EncryptTableRuleConfiguration("encrypt_table", Collections.emptyList(), false)));
        when(result.getConfiguration()).thenReturn(config);
        return result;
    }
    
    @Test
    public void assertGetRowDataWithoutConfiguration() {
        DatabaseDistSQLResultSet resultSet = new DatabaseRulesCountResultSet();
        resultSet.init(mockEmptyDatabase(), mock(CountDatabaseRulesStatement.class));
        Collection<Object> actual = resultSet.getRowData();
        assertThat(actual.size(), is(2));
        Iterator<Object> rowData = actual.iterator();
        assertThat(rowData.next(), is("single_table"));
        assertThat(rowData.next(), is(2));
        resultSet.next();
        actual = resultSet.getRowData();
        rowData = actual.iterator();
        assertThat(rowData.next(), is("sharding_table"));
        assertThat(rowData.next(), is(0));
        resultSet.next();
        actual = resultSet.getRowData();
        rowData = actual.iterator();
        assertThat(rowData.next(), is("sharding_binding_table"));
        assertThat(rowData.next(), is(0));
        resultSet.next();
        actual = resultSet.getRowData();
        rowData = actual.iterator();
        assertThat(rowData.next(), is("sharding_broadcast_table"));
        assertThat(rowData.next(), is(0));
        resultSet.next();
        actual = resultSet.getRowData();
        rowData = actual.iterator();
        assertThat(rowData.next(), is("readwrite_splitting"));
        assertThat(rowData.next(), is(0));
        resultSet.next();
        actual = resultSet.getRowData();
        rowData = actual.iterator();
        assertThat(rowData.next(), is("db_discovery"));
        assertThat(rowData.next(), is(0));
        resultSet.next();
        actual = resultSet.getRowData();
        rowData = actual.iterator();
        assertThat(rowData.next(), is("encrypt"));
        assertThat(rowData.next(), is(0));
    }
    
    private ShardingSphereDatabase mockEmptyDatabase() {
        ShardingSphereDatabase result = mock(ShardingSphereDatabase.class);
        ShardingSphereRuleMetaData ruleMetaData = new ShardingSphereRuleMetaData(Collections.singleton(mockSingleTableRule()));
        when(result.getRuleMetaData()).thenReturn(ruleMetaData);
        return result;
    }
}
