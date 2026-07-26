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

package org.apache.shardingsphere.sharding.metadata;

import org.apache.shardingsphere.database.connector.core.metadata.data.model.ColumnMetaData;
import org.apache.shardingsphere.database.connector.core.metadata.data.model.SchemaMetaData;
import org.apache.shardingsphere.database.connector.core.metadata.data.model.TableMetaData;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.algorithm.core.config.AlgorithmConfiguration;
import org.apache.shardingsphere.infra.config.keygen.impl.ColumnKeyGenerateStrategiesRuleConfiguration;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.instance.ComputeNodeInstanceContext;
import org.apache.shardingsphere.infra.metadata.database.schema.builder.GenericSchemaBuilderMaterial;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereTable;
import org.apache.shardingsphere.infra.metadata.database.schema.reviser.MetaDataReviseEngine;
import org.apache.shardingsphere.infra.metadata.database.schema.reviser.schema.SchemaMetaDataReviseEngine;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingTableRuleConfiguration;
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
import java.util.Properties;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ShardingSchemaMetaDataReviseEngineTest {
    
    @Test
    void assertSchemaMetaDataReviseWithSameActualTableNameInDifferentStorageUnits() {
        ShardingRule rule = createShardingRule();
        SchemaMetaData schemaMetaData = createLoadedSchemaMetaData();
        SchemaMetaData actual = new SchemaMetaDataReviseEngine(Collections.singleton(rule), new ConfigurationProperties(new Properties()), Collections.emptyList()).revise(schemaMetaData);
        assertThat(actual.getTables().size(), is(2));
        assertThat(actual.getTables().stream().map(TableMetaData::getName).collect(Collectors.toList()), containsInAnyOrder("t_order0", "t_order1"));
    }
    
    @Test
    void assertMetaDataReviseWithSameActualTableNameInDifferentStorageUnits() {
        ShardingRule rule = createShardingRule();
        SchemaMetaData schemaMetaData = createLoadedSchemaMetaData();
        GenericSchemaBuilderMaterial material = mock(GenericSchemaBuilderMaterial.class);
        when(material.getProps()).thenReturn(new ConfigurationProperties(new Properties()));
        when(material.getRevisionCandidateSchemas()).thenReturn(Collections.emptyList());
        Map<String, ShardingSphereSchema> actual = new MetaDataReviseEngine(Collections.singleton(rule), mock(DatabaseType.class))
                .revise(Collections.singletonMap("sharding_db", schemaMetaData), material);
        Collection<ShardingSphereTable> tables = actual.get("sharding_db").getAllTables();
        assertThat(tables.size(), is(2));
        assertThat(tables.stream().map(ShardingSphereTable::getName).collect(Collectors.toList()), containsInAnyOrder("t_order0", "t_order1"));
    }
    
    private SchemaMetaData createLoadedSchemaMetaData() {
        return new SchemaMetaData("sharding_db", Arrays.asList(createPhysicalTableMetaData("ds_0"), createPhysicalTableMetaData("ds_1")));
    }
    
    private TableMetaData createPhysicalTableMetaData(final String storageUnitName) {
        Collection<ColumnMetaData> columns = Collections.singleton(new ColumnMetaData("order_id", Types.BIGINT, true, false, true, true, false, false));
        TableMetaData result = new TableMetaData("t_order", columns, Collections.emptyList(), Collections.emptyList());
        result.setStorageUnitName(storageUnitName);
        return result;
    }
    
    private ShardingRule createShardingRule() {
        ShardingRuleConfiguration ruleConfig = new ShardingRuleConfiguration();
        ruleConfig.getTables().add(new ShardingTableRuleConfiguration("t_order0", "ds_0.t_order"));
        ruleConfig.getTables().add(new ShardingTableRuleConfiguration("t_order1", "ds_1.t_order"));
        ruleConfig.getKeyGenerateStrategies().put("t_order0_order_id", new ColumnKeyGenerateStrategiesRuleConfiguration("uuid", "t_order0", "order_id"));
        ruleConfig.getKeyGenerateStrategies().put("t_order1_order_id", new ColumnKeyGenerateStrategiesRuleConfiguration("uuid", "t_order1", "order_id"));
        ruleConfig.getKeyGenerators().put("uuid", new AlgorithmConfiguration("UUID", new Properties()));
        ComputeNodeInstanceContext computeNodeInstanceContext = mock(ComputeNodeInstanceContext.class);
        when(computeNodeInstanceContext.getWorkerId()).thenReturn(0);
        Map<String, DataSource> dataSources = new LinkedHashMap<>(2, 1F);
        dataSources.put("ds_0", new MockedDataSource());
        dataSources.put("ds_1", new MockedDataSource());
        return new ShardingRule(ruleConfig, dataSources, computeNodeInstanceContext, Collections.emptyList());
    }
}
