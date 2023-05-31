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

package org.apache.shardingsphere.sharding.yaml.swapper;

import org.apache.shardingsphere.infra.config.algorithm.AlgorithmConfiguration;
import org.apache.shardingsphere.infra.util.yaml.datanode.YamlDataNode;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingTableReferenceRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingTableRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.audit.ShardingAuditStrategyConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.keygen.KeyGenerateStrategyConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.StandardShardingStrategyConfiguration;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

class NewYamlShardingRuleConfigurationSwapperTest {
    
    private final NewYamlShardingRuleConfigurationSwapper swapper = new NewYamlShardingRuleConfigurationSwapper();
    
    @Test
    void assertSwapEmptyConfigToDataNodes() {
        ShardingRuleConfiguration config = new ShardingRuleConfiguration();
        Collection<YamlDataNode> result = swapper.swapToDataNodes(config);
        assertThat(result.size(), is(0));
    }
    
    @Test
    void assertSwapFullConfigToDataNodesEmpty() {
        ShardingRuleConfiguration config = createMaximumShardingRule();
        Collection<YamlDataNode> result = swapper.swapToDataNodes(config);
        assertThat(result.size(), is(14));
        Iterator<YamlDataNode> iterator = result.iterator();
        assertThat(iterator.next().getKey(), is("tables/table_LOGIC_TABLE"));
        assertThat(iterator.next().getKey(), is("tables/table_SUB_LOGIC_TABLE"));
        assertThat(iterator.next().getKey(), is("binding_tables/binding_table_foo"));
        assertThat(iterator.next().getKey(), is("broadcast_tables"));
        assertThat(iterator.next().getKey(), is("default_strategy/default_database_strategy"));
        assertThat(iterator.next().getKey(), is("default_strategy/default_table_strategy"));
        assertThat(iterator.next().getKey(), is("default_strategy/default_key_generate_strategy"));
        assertThat(iterator.next().getKey(), is("default_strategy/default_audit_strategy"));
        assertThat(iterator.next().getKey(), is("sharding_algorithms/core_standard_fixture"));
        assertThat(iterator.next().getKey(), is("key_generators/uuid"));
        assertThat(iterator.next().getKey(), is("key_generators/default"));
        assertThat(iterator.next().getKey(), is("key_generators/auto_increment"));
        assertThat(iterator.next().getKey(), is("auditors/audit_algorithm"));
        assertThat(iterator.next().getKey(), is("default_strategy/default_sharding_column"));
    }
    
    private ShardingRuleConfiguration createMaximumShardingRule() {
        ShardingRuleConfiguration result = new ShardingRuleConfiguration();
        ShardingTableRuleConfiguration shardingTableRuleConfig = createTableRuleConfiguration("LOGIC_TABLE", "ds_${0..1}.table_${0..2}");
        shardingTableRuleConfig.setKeyGenerateStrategy(new KeyGenerateStrategyConfiguration("id", "uuid"));
        ShardingTableRuleConfiguration subTableRuleConfig = createTableRuleConfiguration("SUB_LOGIC_TABLE", "ds_${0..1}.sub_table_${0..2}");
        subTableRuleConfig.setKeyGenerateStrategy(new KeyGenerateStrategyConfiguration("id", "auto_increment"));
        result.getTables().add(shardingTableRuleConfig);
        result.getTables().add(subTableRuleConfig);
        result.getBindingTableGroups().add(new ShardingTableReferenceRuleConfiguration("foo", shardingTableRuleConfig.getLogicTable() + "," + subTableRuleConfig.getLogicTable()));
        result.getBroadcastTables().add("BROADCAST_TABLE");
        result.setDefaultDatabaseShardingStrategy(new StandardShardingStrategyConfiguration("ds_id", "standard"));
        result.setDefaultTableShardingStrategy(new StandardShardingStrategyConfiguration("table_id", "standard"));
        result.setDefaultShardingColumn("table_id");
        result.setDefaultKeyGenerateStrategy(new KeyGenerateStrategyConfiguration("id", "default"));
        result.setDefaultAuditStrategy(new ShardingAuditStrategyConfiguration(Collections.singletonList("audit_algorithm"), false));
        result.getShardingAlgorithms().put("core_standard_fixture", new AlgorithmConfiguration("CORE.STANDARD.FIXTURE", new Properties()));
        result.getKeyGenerators().put("uuid", new AlgorithmConfiguration("UUID", new Properties()));
        result.getKeyGenerators().put("default", new AlgorithmConfiguration("UUID", new Properties()));
        result.getKeyGenerators().put("auto_increment", new AlgorithmConfiguration("AUTO_INCREMENT.FIXTURE", new Properties()));
        result.getAuditors().put("audit_algorithm", new AlgorithmConfiguration("DML_SHARDING_CONDITIONS", new Properties()));
        return result;
    }
    
    private ShardingTableRuleConfiguration createTableRuleConfiguration(final String logicTableName, final String actualDataNodes) {
        ShardingTableRuleConfiguration result = new ShardingTableRuleConfiguration(logicTableName, actualDataNodes);
        result.setDatabaseShardingStrategy(new StandardShardingStrategyConfiguration("user_id", "database_inline"));
        result.setTableShardingStrategy(new StandardShardingStrategyConfiguration("order_id", "table_inline"));
        return result;
    }
}
