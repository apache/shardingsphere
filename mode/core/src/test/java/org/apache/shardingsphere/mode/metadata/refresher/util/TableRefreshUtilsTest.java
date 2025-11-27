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

package org.apache.shardingsphere.mode.metadata.refresher.util;

import org.apache.shardingsphere.database.connector.core.metadata.database.enums.QuoteCharacter;
import org.apache.shardingsphere.database.connector.core.metadata.database.metadata.DialectDatabaseMetaData;
import org.apache.shardingsphere.database.connector.core.metadata.database.metadata.option.IdentifierPatternType;
import org.apache.shardingsphere.database.connector.core.spi.DatabaseTypedSPILoader;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.config.rule.RuleConfiguration;
import org.apache.shardingsphere.infra.datanode.DataNode;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.rule.RuleMetaData;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.infra.rule.attribute.RuleAttributes;
import org.apache.shardingsphere.infra.rule.attribute.datanode.MutableDataNodeRuleAttribute;
import org.apache.shardingsphere.infra.rule.attribute.table.TableMapperRuleAttribute;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.single.config.SingleRuleConfiguration;
import org.apache.shardingsphere.single.constant.SingleTableConstants;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;
import org.apache.shardingsphere.test.infra.framework.extension.mock.AutoMockExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(AutoMockExtension.class)
class TableRefreshUtilsTest {
    
    private final DatabaseType fixtureDatabaseType = TypedSPILoader.getService(DatabaseType.class, "FIXTURE");
    
    @Test
    void assertGetTableNameFormatsWithIdentifierPattern() {
        assertThat(TableRefreshUtils.getTableName(new IdentifierValue("Foo_Table"), fixtureDatabaseType), is("Foo_Table"));
    }
    
    @Test
    void assertGetTableNameWithQuotedIdentifierReturnsOriginal() {
        assertThat(TableRefreshUtils.getTableName(new IdentifierValue("FooTable", QuoteCharacter.QUOTE), fixtureDatabaseType), is("FooTable"));
    }
    
    @Test
    void assertGetTableNameFormatsUpperCase() {
        DatabaseType upperCaseDatabaseType = mock(DatabaseType.class);
        DialectDatabaseMetaData dialectDatabaseMetaData = mock(DialectDatabaseMetaData.class);
        when(dialectDatabaseMetaData.getIdentifierPatternType()).thenReturn(IdentifierPatternType.UPPER_CASE);
        try (MockedStatic<DatabaseTypedSPILoader> mockedStatic = org.mockito.Mockito.mockStatic(DatabaseTypedSPILoader.class)) {
            mockedStatic.when(() -> DatabaseTypedSPILoader.getService(DialectDatabaseMetaData.class, upperCaseDatabaseType)).thenReturn(dialectDatabaseMetaData);
            assertThat(TableRefreshUtils.getTableName(new IdentifierValue("foo_table"), upperCaseDatabaseType), is("FOO_TABLE"));
        }
    }
    
    @Test
    void assertGetTableNameFormatsLowerCase() {
        DatabaseType lowerCaseDatabaseType = mock(DatabaseType.class);
        DialectDatabaseMetaData dialectDatabaseMetaData = mock(DialectDatabaseMetaData.class);
        when(dialectDatabaseMetaData.getIdentifierPatternType()).thenReturn(IdentifierPatternType.LOWER_CASE);
        try (MockedStatic<DatabaseTypedSPILoader> mockedStatic = org.mockito.Mockito.mockStatic(DatabaseTypedSPILoader.class)) {
            mockedStatic.when(() -> DatabaseTypedSPILoader.getService(DialectDatabaseMetaData.class, lowerCaseDatabaseType)).thenReturn(dialectDatabaseMetaData);
            assertThat(TableRefreshUtils.getTableName(new IdentifierValue("Foo_Table"), lowerCaseDatabaseType), is("foo_table"));
        }
    }
    
    @Test
    void assertIsSingleTableWhenDistributedTableExists() {
        TableMapperRuleAttribute tableMapperRuleAttribute = mock(TableMapperRuleAttribute.class);
        when(tableMapperRuleAttribute.getDistributedTableNames()).thenReturn(Collections.singleton("foo_tbl"));
        RuleMetaData ruleMetaData = mock(RuleMetaData.class);
        when(ruleMetaData.getAttributes(TableMapperRuleAttribute.class)).thenReturn(Collections.singleton(tableMapperRuleAttribute));
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class);
        when(database.getRuleMetaData()).thenReturn(ruleMetaData);
        assertFalse(TableRefreshUtils.isSingleTable("foo_tbl", database));
    }
    
    @Test
    void assertIsSingleTableWhenNoDistributedTables() {
        RuleMetaData ruleMetaData = mock(RuleMetaData.class);
        when(ruleMetaData.getAttributes(TableMapperRuleAttribute.class)).thenReturn(Collections.emptyList());
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class);
        when(database.getRuleMetaData()).thenReturn(ruleMetaData);
        assertTrue(TableRefreshUtils.isSingleTable("foo_tbl", database));
    }
    
    @Test
    void assertIsNeedRefreshWhenNoMutableRuleAttributes() {
        ShardingSphereRule rule = mock(ShardingSphereRule.class);
        when(rule.getAttributes()).thenReturn(new RuleAttributes());
        assertFalse(TableRefreshUtils.isNeedRefresh(new RuleMetaData(Collections.singleton(rule)), "foo_schema", "foo_tbl"));
    }
    
    @Test
    void assertIsNeedRefreshWhenRuleConfigurationIsNotSingle() {
        MutableDataNodeRuleAttribute mutableDataNodeRuleAttribute = mock(MutableDataNodeRuleAttribute.class);
        ShardingSphereRule rule = mock(ShardingSphereRule.class);
        when(rule.getConfiguration()).thenReturn(mock(RuleConfiguration.class));
        when(rule.getAttributes()).thenReturn(new RuleAttributes(mutableDataNodeRuleAttribute));
        assertFalse(TableRefreshUtils.isNeedRefresh(new RuleMetaData(Collections.singleton(rule)), "foo_schema", "foo_tbl"));
    }
    
    @Test
    void assertIsNeedRefreshWhenAllTablesConfigured() {
        MutableDataNodeRuleAttribute mutableDataNodeRuleAttribute = mock(MutableDataNodeRuleAttribute.class);
        SingleRuleConfiguration ruleConfig = new SingleRuleConfiguration();
        ruleConfig.setTables(Collections.singletonList(SingleTableConstants.ALL_TABLES));
        ShardingSphereRule rule = mock(ShardingSphereRule.class);
        when(rule.getConfiguration()).thenReturn(ruleConfig);
        when(rule.getAttributes()).thenReturn(new RuleAttributes(mutableDataNodeRuleAttribute));
        assertFalse(TableRefreshUtils.isNeedRefresh(new RuleMetaData(Collections.singleton(rule)), "foo_schema", "foo_tbl"));
    }
    
    @Test
    void assertIsNeedRefreshWhenAllSchemaTablesConfigured() {
        MutableDataNodeRuleAttribute mutableDataNodeRuleAttribute = mock(MutableDataNodeRuleAttribute.class);
        SingleRuleConfiguration ruleConfig = new SingleRuleConfiguration();
        ruleConfig.setTables(Collections.singletonList(SingleTableConstants.ALL_SCHEMA_TABLES));
        ShardingSphereRule rule = mock(ShardingSphereRule.class);
        when(rule.getConfiguration()).thenReturn(ruleConfig);
        when(rule.getAttributes()).thenReturn(new RuleAttributes(mutableDataNodeRuleAttribute));
        assertFalse(TableRefreshUtils.isNeedRefresh(new RuleMetaData(Collections.singleton(rule)), "foo_schema", "foo_tbl"));
    }
    
    @Test
    void assertIsNeedRefreshWhenTableDataNodeMissing() {
        MutableDataNodeRuleAttribute mutableDataNodeRuleAttribute = mock(MutableDataNodeRuleAttribute.class);
        when(mutableDataNodeRuleAttribute.findTableDataNode("foo_schema", "foo_tbl")).thenReturn(Optional.empty());
        SingleRuleConfiguration ruleConfig = new SingleRuleConfiguration();
        ShardingSphereRule rule = mock(ShardingSphereRule.class);
        when(rule.getConfiguration()).thenReturn(ruleConfig);
        when(rule.getAttributes()).thenReturn(new RuleAttributes(mutableDataNodeRuleAttribute));
        assertFalse(TableRefreshUtils.isNeedRefresh(new RuleMetaData(Collections.singleton(rule)), "foo_schema", "foo_tbl"));
    }
    
    @Test
    void assertIsNeedRefreshBlockedByDataSourceWildcard() {
        MutableDataNodeRuleAttribute mutableDataNodeRuleAttribute = mock(MutableDataNodeRuleAttribute.class);
        DataNode dataNode = new DataNode("foo_ds", "foo_schema", "foo_tbl");
        when(mutableDataNodeRuleAttribute.findTableDataNode("foo_schema", "foo_tbl")).thenReturn(Optional.of(dataNode));
        SingleRuleConfiguration ruleConfig = new SingleRuleConfiguration();
        ruleConfig.setTables(Collections.singletonList("foo_ds.*"));
        ShardingSphereRule rule = mock(ShardingSphereRule.class);
        when(rule.getConfiguration()).thenReturn(ruleConfig);
        when(rule.getAttributes()).thenReturn(new RuleAttributes(mutableDataNodeRuleAttribute));
        assertFalse(TableRefreshUtils.isNeedRefresh(new RuleMetaData(Collections.singleton(rule)), "foo_schema", "foo_tbl"));
    }
    
    @Test
    void assertIsNeedRefreshWhenTableShouldBeRefreshed() {
        MutableDataNodeRuleAttribute mutableDataNodeRuleAttribute = mock(MutableDataNodeRuleAttribute.class);
        DataNode dataNode = new DataNode("foo_ds", "foo_schema", "foo_tbl");
        when(mutableDataNodeRuleAttribute.findTableDataNode("foo_schema", "foo_tbl")).thenReturn(Optional.of(dataNode));
        SingleRuleConfiguration ruleConfig = new SingleRuleConfiguration();
        ShardingSphereRule rule = mock(ShardingSphereRule.class);
        when(rule.getConfiguration()).thenReturn(ruleConfig);
        when(rule.getAttributes()).thenReturn(new RuleAttributes(mutableDataNodeRuleAttribute));
        assertTrue(TableRefreshUtils.isNeedRefresh(new RuleMetaData(Collections.singleton(rule)), "foo_schema", "foo_tbl"));
    }
    
    @Test
    void assertIsNeedRefreshWithTableCollection() {
        MutableDataNodeRuleAttribute mutableDataNodeRuleAttribute = mock(MutableDataNodeRuleAttribute.class);
        when(mutableDataNodeRuleAttribute.findTableDataNode("foo_schema", "bar_tbl")).thenReturn(Optional.empty());
        DataNode dataNode = new DataNode("foo_ds", "foo_schema", "foo_tbl");
        when(mutableDataNodeRuleAttribute.findTableDataNode("foo_schema", "foo_tbl")).thenReturn(Optional.of(dataNode));
        SingleRuleConfiguration ruleConfig = new SingleRuleConfiguration();
        ShardingSphereRule rule = mock(ShardingSphereRule.class);
        when(rule.getConfiguration()).thenReturn(ruleConfig);
        when(rule.getAttributes()).thenReturn(new RuleAttributes(mutableDataNodeRuleAttribute));
        assertTrue(TableRefreshUtils.isNeedRefresh(new RuleMetaData(Collections.singleton(rule)), "foo_schema", Arrays.asList("bar_tbl", "foo_tbl")));
    }
    
    @Test
    void assertIsNeedRefreshBlockedByDataSourceSchemaWildcard() {
        MutableDataNodeRuleAttribute mutableDataNodeRuleAttribute = mock(MutableDataNodeRuleAttribute.class);
        DataNode dataNode = new DataNode("foo_ds", "foo_schema", "foo_tbl");
        when(mutableDataNodeRuleAttribute.findTableDataNode("foo_schema", "foo_tbl")).thenReturn(Optional.of(dataNode));
        SingleRuleConfiguration ruleConfig = new SingleRuleConfiguration();
        ruleConfig.setTables(Collections.singletonList("foo_ds.*.*"));
        ShardingSphereRule rule = mock(ShardingSphereRule.class);
        when(rule.getConfiguration()).thenReturn(ruleConfig);
        when(rule.getAttributes()).thenReturn(new RuleAttributes(mutableDataNodeRuleAttribute));
        assertFalse(TableRefreshUtils.isNeedRefresh(new RuleMetaData(Collections.singleton(rule)), "foo_schema", "foo_tbl"));
    }
    
    @Test
    void assertIsNeedRefreshBlockedBySchemaSpecificWildcard() {
        MutableDataNodeRuleAttribute mutableDataNodeRuleAttribute = mock(MutableDataNodeRuleAttribute.class);
        DataNode dataNode = new DataNode("foo_ds", "foo_schema", "foo_tbl");
        when(mutableDataNodeRuleAttribute.findTableDataNode("foo_schema", "foo_tbl")).thenReturn(Optional.of(dataNode));
        SingleRuleConfiguration ruleConfig = new SingleRuleConfiguration();
        ruleConfig.setTables(Collections.singletonList("foo_ds.foo_schema.*"));
        ShardingSphereRule rule = mock(ShardingSphereRule.class);
        when(rule.getConfiguration()).thenReturn(ruleConfig);
        when(rule.getAttributes()).thenReturn(new RuleAttributes(mutableDataNodeRuleAttribute));
        assertFalse(TableRefreshUtils.isNeedRefresh(new RuleMetaData(Collections.singleton(rule)), "foo_schema", "foo_tbl"));
    }
    
    @Test
    void assertIsNeedRefreshWithTableCollectionWhenNoTableNeedsRefresh() {
        ShardingSphereRule rule = mock(ShardingSphereRule.class);
        when(rule.getAttributes()).thenReturn(new RuleAttributes());
        assertFalse(TableRefreshUtils.isNeedRefresh(new RuleMetaData(Collections.singleton(rule)), "foo_schema", Arrays.asList("bar_tbl", "foo_tbl")));
    }
}
