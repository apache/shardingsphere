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

import org.apache.shardingsphere.database.connector.core.metadata.identifier.IdentifierCasePolicyFactory;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.config.rule.RuleConfiguration;
import org.apache.shardingsphere.infra.datanode.DataNode;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.resource.ResourceMetaData;
import org.apache.shardingsphere.infra.metadata.database.rule.RuleMetaData;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereColumn;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereIndex;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereTable;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereView;
import org.apache.shardingsphere.infra.metadata.identifier.DatabaseIdentifierContext;
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

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;
import java.util.Properties;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(AutoMockExtension.class)
class TableRefreshUtilsTest {
    
    private final DatabaseType fixtureDatabaseType = TypedSPILoader.getService(DatabaseType.class, "FIXTURE");
    
    @Test
    void assertGetActualTableNameUsesExistingTableName() {
        ShardingSphereDatabase database = createDatabase();
        assertThat(TableRefreshUtils.getActualTableName(database, "foo_schema", new IdentifierValue("foo_tbl")), is("Foo_Tbl"));
    }
    
    @Test
    void assertGetActualTableNamesUsesExistingTableNames() {
        ShardingSphereDatabase database = createDatabase();
        assertThat(TableRefreshUtils.getActualTableNames(database, "foo_schema", Arrays.asList(new IdentifierValue("foo_tbl"), new IdentifierValue("bar_tbl"))),
                is(Arrays.asList("Foo_Tbl", "Bar_Tbl")));
    }
    
    @Test
    void assertGetActualViewNamesUsesExistingViewNames() {
        ShardingSphereDatabase database = createDatabase();
        assertThat(TableRefreshUtils.getActualViewNames(database, "foo_schema", Arrays.asList(new IdentifierValue("foo_view"), new IdentifierValue("bar_view"))),
                is(Arrays.asList("Foo_View", "Bar_View")));
    }
    
    @Test
    void assertGetActualViewNameUsesExistingViewName() {
        assertThat(TableRefreshUtils.getActualViewName(createDatabase(), "foo_schema", new IdentifierValue("foo_view")), is("Foo_View"));
    }
    
    @Test
    void assertGetActualIndexNameUsesExistingIndexName() {
        assertThat(TableRefreshUtils.getActualIndexName(createDatabase(), "foo_schema", "foo_tbl", new IdentifierValue("idx_foo")), is("Idx_Foo"));
    }
    
    @Test
    void assertGetActualColumnNamesUsesExistingColumnNames() {
        assertThat(TableRefreshUtils.getActualColumnNames(createDatabase(), "foo_schema", "foo_tbl",
                Arrays.asList(new IdentifierValue("order_id"), new IdentifierValue("user_id"))),
                is(Arrays.asList("Order_ID", "User_ID")));
    }
    
    @Test
    void assertFindActualTableNameByIndexUsesExistingIndexName() {
        assertThat(TableRefreshUtils.findActualTableNameByIndex(createDatabase(), "foo_schema", new IdentifierValue("idx_foo")).get(), is("Foo_Tbl"));
    }
    
    @Test
    void assertGetTableLoadCandidateNameUsesNormalizedRule() {
        assertThat(TableRefreshUtils.getTableLoadCandidateName(createDatabase(), new IdentifierValue("Foo_Tbl")), is("foo_tbl"));
    }
    
    @Test
    void assertGetViewLoadCandidateNameUsesNormalizedRule() {
        assertThat(TableRefreshUtils.getViewLoadCandidateName(createDatabase(), new IdentifierValue("Foo_View")), is("foo_view"));
    }
    
    @Test
    void assertGetTableLoadCandidateNameUsesStoragePolicy() {
        assertThat(TableRefreshUtils.getTableLoadCandidateName(createDatabaseWithDistinctPolicies(), new IdentifierValue("Foo_Tbl")), is("FOO_TBL"));
    }
    
    @Test
    void assertGetActualTableNameUsesStoragePolicyWhenMissing() {
        assertThat(TableRefreshUtils.getActualTableName(createDatabaseWithDistinctPolicies(), "foo_schema", new IdentifierValue("Foo_Tbl")), is("FOO_TBL"));
    }
    
    @Test
    void assertGetActualIndexNameUsesStoragePolicyWhenMissing() {
        assertThat(TableRefreshUtils.getActualIndexName(createDatabaseWithDistinctPolicies(), "foo_schema", "foo_tbl", new IdentifierValue("Idx_Foo")), is("IDX_FOO"));
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
    
    private ShardingSphereDatabase createDatabase() {
        ShardingSphereSchema schema = new ShardingSphereSchema("Foo_Schema", fixtureDatabaseType,
                Arrays.asList(new ShardingSphereTable("Foo_Tbl",
                        Arrays.asList(new ShardingSphereColumn("Order_ID", 0, false, false, false, true, false, true),
                                new ShardingSphereColumn("User_ID", 0, false, false, false, true, false, true)),
                        Collections.singletonList(new ShardingSphereIndex("Idx_Foo", Collections.singletonList("Order_ID"), false)),
                        Collections.emptyList()),
                        new ShardingSphereTable("Bar_Tbl", Collections.emptyList(), Collections.emptyList(), Collections.emptyList())),
                Arrays.asList(new ShardingSphereView("Foo_View", "SELECT 1"), new ShardingSphereView("Bar_View", "SELECT 1")));
        return new ShardingSphereDatabase("foo_db", fixtureDatabaseType, new ResourceMetaData(Collections.emptyMap()),
                new RuleMetaData(Collections.emptyList()), Collections.singletonList(schema), new ConfigurationProperties(new Properties()));
    }
    
    private ShardingSphereDatabase createDatabaseWithDistinctPolicies() {
        ShardingSphereDatabase result = mock(ShardingSphereDatabase.class);
        when(result.getIdentifierContext()).thenReturn(new DatabaseIdentifierContext(IdentifierCasePolicyFactory.newSensitivePolicySet(),
                IdentifierCasePolicyFactory.newUpperCasePolicySet(), IdentifierCasePolicyFactory.newInsensitivePolicySet(), false));
        when(result.getAllSchemas()).thenReturn(Collections.emptyList());
        return result;
    }
}
