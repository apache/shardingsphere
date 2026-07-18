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

package org.apache.shardingsphere.sharding.metadata.reviser.table;

import org.apache.shardingsphere.database.connector.core.metadata.data.model.ColumnMetaData;
import org.apache.shardingsphere.database.connector.core.metadata.data.model.TableMetaData;
import org.apache.shardingsphere.infra.algorithm.core.config.AlgorithmConfiguration;
import org.apache.shardingsphere.infra.config.keygen.impl.ColumnKeyGenerateStrategiesRuleConfiguration;
import org.apache.shardingsphere.infra.instance.ComputeNodeInstanceContext;
import org.apache.shardingsphere.infra.metadata.database.schema.reviser.table.TableMetaDataReviseEngine;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.infra.rule.attribute.RuleAttributes;
import org.apache.shardingsphere.infra.rule.attribute.datasource.DataSourceMapperRuleAttribute;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingTableRuleConfiguration;
import org.apache.shardingsphere.sharding.metadata.reviser.ShardingMetaDataReviseEntry;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.apache.shardingsphere.test.infra.fixture.jdbc.MockedDataSource;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.sql.Types;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ShardingQualifiedTableMetaDataReviseTest {
    
    private final ShardingMetaDataReviseEntry reviseEntry = new ShardingMetaDataReviseEntry();
    
    @Test
    void assertCreateTableMetaDataRevisionContextResolvesOwningShardingTableOnce() {
        ShardingRule rule = createShardingRule();
        Optional<ShardingTableMetaDataRevisionContext> actual = reviseEntry.createTableMetaDataRevisionContext(rule, "t_order", "ds_1");
        assertTrue(actual.isPresent());
        assertThat(actual.get().reviseTableName("t_order"), is("t_order1"));
        assertTrue(actual.get().getColumnGeneratedReviser().isPresent());
        assertTrue(actual.get().getIndexReviser().isPresent());
        assertTrue(actual.get().getConstraintReviser().isPresent());
    }
    
    @Test
    void assertReviseWithDifferentGeneratedKeyColumnsPerStorageUnit() {
        ShardingRule rule = createShardingRule();
        TableMetaDataReviseEngine<ShardingRule> engine = new TableMetaDataReviseEngine<>(rule, reviseEntry);
        TableMetaData revisedFromDs0 = engine.revise(createPhysicalTableMetaData("ds_0"));
        TableMetaData revisedFromDs1 = engine.revise(createPhysicalTableMetaData("ds_1"));
        assertThat(revisedFromDs0.getName(), is("t_order0"));
        assertThat(revisedFromDs1.getName(), is("t_order1"));
        assertColumnGenerated(revisedFromDs0, "order_id_0", true);
        assertColumnGenerated(revisedFromDs0, "order_id_1", false);
        assertColumnGenerated(revisedFromDs1, "order_id_0", false);
        assertColumnGenerated(revisedFromDs1, "order_id_1", true);
    }
    
    @Test
    void assertRevisePreservesOriginalNameWhenQualifiedLookupMisses() {
        ShardingRule rule = createPartialShardingRule();
        assertThat(new ShardingTableNameReviser().revise("t_order", rule), is("t_order0"));
        assertThat(new ShardingTableNameReviser().revise("t_order", rule, "ds_0"), is("t_order0"));
        assertThat(new ShardingTableNameReviser().revise("t_order", rule, "ds_1"), is("t_order"));
        assertFalse(reviseEntry.createTableMetaDataRevisionContext(rule, "t_order", "ds_1").isPresent());
        TableMetaData revisedFromDs1 = new TableMetaDataReviseEngine<>(rule, reviseEntry).revise(createPhysicalTableMetaData("ds_1"));
        assertThat(revisedFromDs1.getName(), is("t_order"));
    }
    
    @Test
    void assertReviseWithReadwriteSplittingStorageUnitAlias() {
        ShardingRuleConfiguration ruleConfig = new ShardingRuleConfiguration();
        ruleConfig.getTables().add(new ShardingTableRuleConfiguration("t_order", "readwrite_ds_0.t_order_0"));
        Map<String, Collection<String>> dataSourceMapper = Collections.singletonMap("readwrite_ds_0", Arrays.asList("write_ds_0", "read_ds_0"));
        ShardingRule rule = createShardingRule(ruleConfig, dataSourceMapper);
        assertThat(new ShardingTableNameReviser().revise("t_order_0", rule, "write_ds_0"), is("t_order"));
        assertTrue(reviseEntry.createTableMetaDataRevisionContext(rule, "t_order_0", "write_ds_0").isPresent());
        TableMetaData revised = new TableMetaDataReviseEngine<>(rule, reviseEntry).revise(createPhysicalTableMetaData("t_order_0", "write_ds_0"));
        assertThat(revised.getName(), is("t_order"));
    }
    
    @Test
    void assertReviseWithShadowStorageUnitAlias() {
        ShardingRuleConfiguration ruleConfig = new ShardingRuleConfiguration();
        ruleConfig.getTables().add(new ShardingTableRuleConfiguration("t_shadow", "shadowDataSource_0.t_shadow_0"));
        Map<String, Collection<String>> dataSourceMapper = Collections.singletonMap("shadowDataSource_0", Arrays.asList("db_0", "shadow_db_0"));
        ShardingRule rule = createShardingRule(ruleConfig, dataSourceMapper);
        assertThat(new ShardingTableNameReviser().revise("t_shadow_0", rule, "shadow_db_0"), is("t_shadow"));
        assertThat(new ShardingTableNameReviser().revise("t_shadow_0", rule, "db_0"), is("t_shadow"));
        assertTrue(reviseEntry.createTableMetaDataRevisionContext(rule, "t_shadow_0", "shadow_db_0").isPresent());
        TableMetaData revised = new TableMetaDataReviseEngine<>(rule, reviseEntry).revise(createPhysicalTableMetaData("t_shadow_0", "shadow_db_0"));
        assertThat(revised.getName(), is("t_shadow"));
    }
    
    private void assertColumnGenerated(final TableMetaData tableMetaData, final String columnName, final boolean generated) {
        ColumnMetaData actual = tableMetaData.getColumns().stream().filter(each -> columnName.equals(each.getName())).findFirst().orElseThrow(IllegalStateException::new);
        if (generated) {
            assertTrue(actual.isGenerated());
        } else {
            assertFalse(actual.isGenerated());
        }
    }
    
    private TableMetaData createPhysicalTableMetaData(final String storageUnitName) {
        return createPhysicalTableMetaData("t_order", storageUnitName);
    }
    
    private TableMetaData createPhysicalTableMetaData(final String tableName, final String storageUnitName) {
        Collection<ColumnMetaData> columns = Arrays.asList(
                new ColumnMetaData("order_id_0", Types.BIGINT, true, false, true, true, false, false),
                new ColumnMetaData("order_id_1", Types.BIGINT, false, false, true, true, false, false));
        TableMetaData result = new TableMetaData(tableName, columns, Collections.emptyList(), Collections.emptyList());
        result.setStorageUnitName(storageUnitName);
        return result;
    }
    
    private ShardingRule createShardingRule() {
        ShardingRuleConfiguration ruleConfig = new ShardingRuleConfiguration();
        ruleConfig.getTables().add(new ShardingTableRuleConfiguration("t_order0", "ds_0.t_order"));
        ruleConfig.getTables().add(new ShardingTableRuleConfiguration("t_order1", "ds_1.t_order"));
        ruleConfig.getKeyGenerateStrategies().put("t_order0_order_id_0", new ColumnKeyGenerateStrategiesRuleConfiguration("uuid", "t_order0", "order_id_0"));
        ruleConfig.getKeyGenerateStrategies().put("t_order1_order_id_1", new ColumnKeyGenerateStrategiesRuleConfiguration("uuid", "t_order1", "order_id_1"));
        ruleConfig.getKeyGenerators().put("uuid", new AlgorithmConfiguration("UUID", new Properties()));
        return createShardingRule(ruleConfig);
    }
    
    private ShardingRule createShardingRule(final ShardingRuleConfiguration ruleConfig) {
        return createShardingRule(ruleConfig, Collections.emptyMap());
    }
    
    private ShardingRule createShardingRule(final ShardingRuleConfiguration ruleConfig, final Map<String, Collection<String>> dataSourceMapper) {
        ComputeNodeInstanceContext computeNodeInstanceContext = mock(ComputeNodeInstanceContext.class);
        when(computeNodeInstanceContext.getWorkerId()).thenReturn(0);
        Map<String, DataSource> dataSources = new LinkedHashMap<>(4, 1F);
        dataSources.put("ds_0", new MockedDataSource());
        dataSources.put("ds_1", new MockedDataSource());
        dataSources.put("write_ds_0", new MockedDataSource());
        dataSources.put("read_ds_0", new MockedDataSource());
        dataSources.put("db_0", new MockedDataSource());
        dataSources.put("shadow_db_0", new MockedDataSource());
        Collection<ShardingSphereRule> builtRules = dataSourceMapper.isEmpty() ? Collections.emptyList() : Collections.singleton(mockDataSourceMapperRule(dataSourceMapper));
        return new ShardingRule(ruleConfig, dataSources, computeNodeInstanceContext, builtRules);
    }
    
    private ShardingRule createPartialShardingRule() {
        ShardingRuleConfiguration ruleConfig = new ShardingRuleConfiguration();
        ruleConfig.getTables().add(new ShardingTableRuleConfiguration("t_order0", "ds_0.t_order"));
        return createShardingRule(ruleConfig);
    }
    
    private ShardingSphereRule mockDataSourceMapperRule(final Map<String, Collection<String>> dataSourceMapper) {
        ShardingSphereRule result = mock(ShardingSphereRule.class);
        RuleAttributes attributes = mock(RuleAttributes.class);
        DataSourceMapperRuleAttribute ruleAttribute = () -> dataSourceMapper;
        when(result.getAttributes()).thenReturn(attributes);
        when(attributes.findAttribute(DataSourceMapperRuleAttribute.class)).thenReturn(Optional.of(ruleAttribute));
        return result;
    }
}
