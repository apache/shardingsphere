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

package org.apache.shardingsphere.proxy.backend.handler.distsql.rql.rule;

import org.apache.shardingsphere.distsql.handler.query.RQLExecutor;
import org.apache.shardingsphere.distsql.parser.statement.rql.show.ShowRulesUsedStorageUnitStatement;
import org.apache.shardingsphere.encrypt.api.config.EncryptRuleConfiguration;
import org.apache.shardingsphere.encrypt.api.config.rule.EncryptTableRuleConfiguration;
import org.apache.shardingsphere.encrypt.rule.EncryptRule;
import org.apache.shardingsphere.infra.merge.result.impl.local.LocalDataQueryResultRow;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.resource.ResourceMetaData;
import org.apache.shardingsphere.infra.metadata.database.rule.RuleMetaData;
import org.apache.shardingsphere.mask.api.config.MaskRuleConfiguration;
import org.apache.shardingsphere.mask.api.config.rule.MaskTableRuleConfiguration;
import org.apache.shardingsphere.mask.rule.MaskRule;
import org.apache.shardingsphere.readwritesplitting.api.ReadwriteSplittingRuleConfiguration;
import org.apache.shardingsphere.readwritesplitting.api.rule.ReadwriteSplittingDataSourceRuleConfiguration;
import org.apache.shardingsphere.readwritesplitting.rule.ReadwriteSplittingRule;
import org.apache.shardingsphere.shadow.api.config.ShadowRuleConfiguration;
import org.apache.shardingsphere.shadow.api.config.datasource.ShadowDataSourceConfiguration;
import org.apache.shardingsphere.shadow.rule.ShadowRule;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingAutoTableRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingTableRuleConfiguration;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.apache.shardingsphere.test.fixture.jdbc.MockedDataSource;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ShowRulesUsedStorageUnitExecutorTest {
    
    @Test
    void assertGetRowData() {
        RQLExecutor<ShowRulesUsedStorageUnitStatement> executor = new ShowRulesUsedStorageUnitExecutor();
        ShowRulesUsedStorageUnitStatement sqlStatement = mock(ShowRulesUsedStorageUnitStatement.class);
        when(sqlStatement.getStorageUnitName()).thenReturn(Optional.of("foo_ds"));
        Collection<LocalDataQueryResultRow> rowData = executor.getRows(mockDatabase(), sqlStatement);
        assertThat(rowData.size(), is(7));
        Iterator<LocalDataQueryResultRow> actual = rowData.iterator();
        LocalDataQueryResultRow row = actual.next();
        assertThat(row.getCell(1), is("sharding"));
        assertThat(row.getCell(2), is("sharding_auto_table"));
        row = actual.next();
        assertThat(row.getCell(1), is("sharding"));
        assertThat(row.getCell(2), is("sharding_table"));
        row = actual.next();
        assertThat(row.getCell(1), is("readwrite_splitting"));
        assertThat(row.getCell(2), is("readwrite_splitting_source"));
        row = actual.next();
        assertThat(row.getCell(1), is("readwrite_splitting"));
        assertThat(row.getCell(2), is("readwrite_splitting_source"));
        row = actual.next();
        assertThat(row.getCell(1), is("encrypt"));
        assertThat(row.getCell(2), is("encrypt_table"));
        row = actual.next();
        assertThat(row.getCell(1), is("shadow"));
        assertThat(row.getCell(2), is("shadow_source"));
        row = actual.next();
        assertThat(row.getCell(1), is("mask"));
        assertThat(row.getCell(2), is("mask_table"));
        assertFalse(actual.hasNext());
    }
    
    private ShardingSphereDatabase mockDatabase() {
        ShardingSphereDatabase result = mock(ShardingSphereDatabase.class);
        RuleMetaData ruleMetaData = new RuleMetaData(
                Arrays.asList(mockShardingRule(), mockReadwriteSplittingRule(), mockEncryptRule(), mockShadowRule(), mockMaskRule()));
        when(result.getRuleMetaData()).thenReturn(ruleMetaData);
        ResourceMetaData resourceMetaData = new ResourceMetaData("sharding_db", Collections.singletonMap("foo_ds", new MockedDataSource()));
        when(result.getResourceMetaData()).thenReturn(resourceMetaData);
        return result;
    }
    
    private ShardingRule mockShardingRule() {
        ShardingRule result = mock(ShardingRule.class);
        ShardingRuleConfiguration config = mock(ShardingRuleConfiguration.class);
        when(config.getTables()).thenReturn(Collections.singleton(new ShardingTableRuleConfiguration("sharding_table", null)));
        when(config.getAutoTables()).thenReturn(Collections.singleton(new ShardingAutoTableRuleConfiguration("sharding_auto_table", null)));
        when(result.getConfiguration()).thenReturn(config);
        return result;
    }
    
    private ReadwriteSplittingRule mockReadwriteSplittingRule() {
        ReadwriteSplittingRule result = mock(ReadwriteSplittingRule.class);
        ReadwriteSplittingRuleConfiguration config = mock(ReadwriteSplittingRuleConfiguration.class);
        when(config.getDataSources())
                .thenReturn(Collections.singleton(new ReadwriteSplittingDataSourceRuleConfiguration("readwrite_splitting_source", "foo_ds", Arrays.asList("foo_ds", "bar_ds"), "")));
        when(result.getConfiguration()).thenReturn(config);
        return result;
    }
    
    private EncryptRule mockEncryptRule() {
        EncryptRule result = mock(EncryptRule.class);
        EncryptRuleConfiguration config = mock(EncryptRuleConfiguration.class);
        when(config.getTables()).thenReturn(Collections.singleton(new EncryptTableRuleConfiguration("encrypt_table", Collections.emptyList())));
        when(result.getConfiguration()).thenReturn(config);
        return result;
    }
    
    private ShadowRule mockShadowRule() {
        ShadowRule result = mock(ShadowRule.class);
        ShadowRuleConfiguration config = mock(ShadowRuleConfiguration.class);
        when(config.getDataSources()).thenReturn(Collections.singletonList(new ShadowDataSourceConfiguration("shadow_source", "foo_ds", "shadow_ds")));
        when(result.getConfiguration()).thenReturn(config);
        return result;
    }
    
    private MaskRule mockMaskRule() {
        MaskRule result = mock(MaskRule.class);
        MaskRuleConfiguration config = mock(MaskRuleConfiguration.class);
        when(config.getTables()).thenReturn(Collections.singleton(new MaskTableRuleConfiguration("mask_table", Collections.emptyList())));
        when(result.getConfiguration()).thenReturn(config);
        return result;
    }
    
    @Test
    void assertGetEmptyRowData() {
        ShardingSphereDatabase database = mockEmptyDatabase();
        RQLExecutor<ShowRulesUsedStorageUnitStatement> executor = new ShowRulesUsedStorageUnitExecutor();
        ShowRulesUsedStorageUnitStatement sqlStatement = mock(ShowRulesUsedStorageUnitStatement.class);
        when(sqlStatement.getStorageUnitName()).thenReturn(Optional.of("empty_ds"));
        Collection<LocalDataQueryResultRow> rowData = executor.getRows(database, sqlStatement);
        assertTrue(rowData.isEmpty());
    }
    
    private ShardingSphereDatabase mockEmptyDatabase() {
        ShardingSphereDatabase result = mock(ShardingSphereDatabase.class);
        when(result.getRuleMetaData()).thenReturn(new RuleMetaData(Collections.emptyList()));
        ResourceMetaData resourceMetaData = new ResourceMetaData("sharding_db", Collections.singletonMap("empty_ds", new MockedDataSource()));
        when(result.getResourceMetaData()).thenReturn(resourceMetaData);
        return result;
    }
    
    @Test
    void assertGetColumnNames() {
        RQLExecutor<ShowRulesUsedStorageUnitStatement> executor = new ShowRulesUsedStorageUnitExecutor();
        Collection<String> columns = executor.getColumnNames();
        assertThat(columns.size(), is(2));
        Iterator<String> iterator = columns.iterator();
        assertThat(iterator.next(), is("type"));
        assertThat(iterator.next(), is("name"));
    }
}
