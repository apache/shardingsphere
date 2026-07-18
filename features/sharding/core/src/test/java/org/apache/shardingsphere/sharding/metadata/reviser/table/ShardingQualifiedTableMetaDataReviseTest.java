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
    
    private void assertColumnGenerated(final TableMetaData tableMetaData, final String columnName, final boolean generated) {
        ColumnMetaData actual = tableMetaData.getColumns().stream().filter(each -> columnName.equals(each.getName())).findFirst().orElseThrow(IllegalStateException::new);
        if (generated) {
            assertTrue(actual.isGenerated());
        } else {
            assertFalse(actual.isGenerated());
        }
    }
    
    private TableMetaData createPhysicalTableMetaData(final String storageUnitName) {
        Collection<ColumnMetaData> columns = Arrays.asList(
                new ColumnMetaData("order_id_0", Types.BIGINT, true, false, true, true, false, false),
                new ColumnMetaData("order_id_1", Types.BIGINT, false, false, true, true, false, false));
        TableMetaData result = new TableMetaData("t_order", columns, Collections.emptyList(), Collections.emptyList());
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
        ComputeNodeInstanceContext computeNodeInstanceContext = mock(ComputeNodeInstanceContext.class);
        when(computeNodeInstanceContext.getWorkerId()).thenReturn(0);
        Map<String, DataSource> dataSources = new LinkedHashMap<>(2, 1F);
        dataSources.put("ds_0", new MockedDataSource());
        dataSources.put("ds_1", new MockedDataSource());
        return new ShardingRule(ruleConfig, dataSources, computeNodeInstanceContext, Collections.emptyList());
    }
}
