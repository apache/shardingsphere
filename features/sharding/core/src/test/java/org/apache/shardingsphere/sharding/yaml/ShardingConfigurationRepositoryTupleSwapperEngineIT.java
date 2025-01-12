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

package org.apache.shardingsphere.sharding.yaml;

import org.apache.shardingsphere.mode.node.tuple.RepositoryTuple;
import org.apache.shardingsphere.infra.yaml.config.pojo.rule.YamlRuleConfiguration;
import org.apache.shardingsphere.sharding.yaml.config.YamlShardingRuleConfiguration;
import org.apache.shardingsphere.test.it.yaml.RepositoryTupleSwapperEngineIT;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

class ShardingConfigurationRepositoryTupleSwapperEngineIT extends RepositoryTupleSwapperEngineIT {
    
    ShardingConfigurationRepositoryTupleSwapperEngineIT() {
        super("yaml/sharding-rule-for-tuple.yaml");
    }
    
    @Override
    protected void assertRepositoryTuples(final List<RepositoryTuple> actualRepositoryTuples, final YamlRuleConfiguration expectedYamlRuleConfig) {
        assertThat(actualRepositoryTuples.size(), is(19));
        assertRepositoryTuple(actualRepositoryTuples.get(0),
                "sharding_algorithms/core_standard_fixture", ((YamlShardingRuleConfiguration) expectedYamlRuleConfig).getShardingAlgorithms().get("core_standard_fixture"));
        assertRepositoryTuple(actualRepositoryTuples.get(1),
                "sharding_algorithms/core_complex_fixture", ((YamlShardingRuleConfiguration) expectedYamlRuleConfig).getShardingAlgorithms().get("core_complex_fixture"));
        assertRepositoryTuple(actualRepositoryTuples.get(2),
                "sharding_algorithms/core_hint_fixture", ((YamlShardingRuleConfiguration) expectedYamlRuleConfig).getShardingAlgorithms().get("core_hint_fixture"));
        assertRepositoryTuple(actualRepositoryTuples.get(3),
                "sharding_algorithms/database_inline", ((YamlShardingRuleConfiguration) expectedYamlRuleConfig).getShardingAlgorithms().get("database_inline"));
        assertRepositoryTuple(actualRepositoryTuples.get(4),
                "sharding_algorithms/table_inline", ((YamlShardingRuleConfiguration) expectedYamlRuleConfig).getShardingAlgorithms().get("table_inline"));
        assertRepositoryTuple(actualRepositoryTuples.get(5), "key_generators/snowflake", ((YamlShardingRuleConfiguration) expectedYamlRuleConfig).getKeyGenerators().get("snowflake"));
        assertRepositoryTuple(actualRepositoryTuples.get(6),
                "auditors/sharding_key_required_auditor", ((YamlShardingRuleConfiguration) expectedYamlRuleConfig).getAuditors().get("sharding_key_required_auditor"));
        assertRepositoryTuple(actualRepositoryTuples.get(7),
                "default_strategies/default_database_strategy", ((YamlShardingRuleConfiguration) expectedYamlRuleConfig).getDefaultDatabaseStrategy());
        assertRepositoryTuple(actualRepositoryTuples.get(8),
                "default_strategies/default_table_strategy", ((YamlShardingRuleConfiguration) expectedYamlRuleConfig).getDefaultTableStrategy());
        assertRepositoryTuple(actualRepositoryTuples.get(9),
                "default_strategies/default_key_generate_strategy", ((YamlShardingRuleConfiguration) expectedYamlRuleConfig).getDefaultKeyGenerateStrategy());
        assertRepositoryTuple(actualRepositoryTuples.get(10),
                "default_strategies/default_audit_strategy", ((YamlShardingRuleConfiguration) expectedYamlRuleConfig).getDefaultAuditStrategy());
        assertRepositoryTuple(actualRepositoryTuples.get(11),
                "tables/t_user", ((YamlShardingRuleConfiguration) expectedYamlRuleConfig).getTables().get("t_user"));
        assertRepositoryTuple(actualRepositoryTuples.get(12),
                "tables/t_stock", ((YamlShardingRuleConfiguration) expectedYamlRuleConfig).getTables().get("t_stock"));
        assertRepositoryTuple(actualRepositoryTuples.get(13),
                "tables/t_order", ((YamlShardingRuleConfiguration) expectedYamlRuleConfig).getTables().get("t_order"));
        assertRepositoryTuple(actualRepositoryTuples.get(14),
                "tables/t_order_item", ((YamlShardingRuleConfiguration) expectedYamlRuleConfig).getTables().get("t_order_item"));
        assertRepositoryTuple(actualRepositoryTuples.get(15),
                "binding_tables" + actualRepositoryTuples.get(15).getKey().substring(actualRepositoryTuples.get(15).getKey().indexOf("/")),
                new ArrayList<>(((YamlShardingRuleConfiguration) expectedYamlRuleConfig).getBindingTables()).get(0));
        assertRepositoryTuple(actualRepositoryTuples.get(16), "binding_tables/foo", new ArrayList<>(((YamlShardingRuleConfiguration) expectedYamlRuleConfig).getBindingTables()).get(1));
        assertRepositoryTuple(actualRepositoryTuples.get(17),
                "default_strategies/default_sharding_column", ((YamlShardingRuleConfiguration) expectedYamlRuleConfig).getDefaultShardingColumn());
        assertRepositoryTuple(actualRepositoryTuples.get(18),
                "sharding_cache", ((YamlShardingRuleConfiguration) expectedYamlRuleConfig).getShardingCache());
    }
}
