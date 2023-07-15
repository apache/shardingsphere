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
import org.apache.shardingsphere.sharding.api.config.rule.ShardingAutoTableRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingTableReferenceRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingTableRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.audit.ShardingAuditStrategyConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.keygen.KeyGenerateStrategyConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.StandardShardingStrategyConfiguration;
import org.apache.shardingsphere.test.util.PropertiesBuilder;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NewYamlShardingRuleConfigurationSwapperTest {
    
    private final NewYamlShardingRuleConfigurationSwapper swapper = new NewYamlShardingRuleConfigurationSwapper();
    
    @Test
    void assertSwapEmptyConfigToDataNodes() {
        ShardingRuleConfiguration config = new ShardingRuleConfiguration();
        Collection<YamlDataNode> result = swapper.swapToDataNodes(config);
        assertThat(result.size(), is(0));
    }
    
    @Test
    void assertSwapFullConfigToDataNodes() {
        ShardingRuleConfiguration config = createMaximumShardingRule();
        Collection<YamlDataNode> result = swapper.swapToDataNodes(config);
        assertThat(result.size(), is(15));
        Iterator<YamlDataNode> iterator = result.iterator();
        assertThat(iterator.next().getKey(), is("algorithms/core_standard_fixture"));
        assertThat(iterator.next().getKey(), is("algorithms/hash_mod"));
        assertThat(iterator.next().getKey(), is("key_generators/uuid"));
        assertThat(iterator.next().getKey(), is("key_generators/default"));
        assertThat(iterator.next().getKey(), is("key_generators/auto_increment"));
        assertThat(iterator.next().getKey(), is("auditors/audit_algorithm"));
        assertThat(iterator.next().getKey(), is("default_strategies/default_database_strategy"));
        assertThat(iterator.next().getKey(), is("default_strategies/default_table_strategy"));
        assertThat(iterator.next().getKey(), is("default_strategies/default_key_generate_strategy"));
        assertThat(iterator.next().getKey(), is("default_strategies/default_audit_strategy"));
        assertThat(iterator.next().getKey(), is("default_strategies/default_sharding_column"));
        assertThat(iterator.next().getKey(), is("tables/LOGIC_TABLE"));
        assertThat(iterator.next().getKey(), is("tables/SUB_LOGIC_TABLE"));
        assertThat(iterator.next().getKey(), is("auto_tables/auto_table"));
        assertThat(iterator.next().getKey(), is("binding_tables/foo"));
    }
    
    private ShardingRuleConfiguration createMaximumShardingRule() {
        ShardingRuleConfiguration result = new ShardingRuleConfiguration();
        ShardingTableRuleConfiguration shardingTableRuleConfig = createTableRuleConfiguration("LOGIC_TABLE", "ds_${0..1}.table_${0..2}");
        shardingTableRuleConfig.setKeyGenerateStrategy(new KeyGenerateStrategyConfiguration("id", "uuid"));
        shardingTableRuleConfig.setAuditStrategy(new ShardingAuditStrategyConfiguration(Collections.singleton("audit_algorithm"), false));
        result.getTables().add(shardingTableRuleConfig);
        ShardingTableRuleConfiguration subTableRuleConfig = createTableRuleConfiguration("SUB_LOGIC_TABLE", "ds_${0..1}.sub_table_${0..2}");
        subTableRuleConfig.setKeyGenerateStrategy(new KeyGenerateStrategyConfiguration("id", "auto_increment"));
        result.getTables().add(subTableRuleConfig);
        ShardingAutoTableRuleConfiguration autoTableRuleConfiguration = new ShardingAutoTableRuleConfiguration("auto_table", "ds_1,ds_2");
        autoTableRuleConfiguration.setShardingStrategy(new StandardShardingStrategyConfiguration("user_id", "hash_mod"));
        autoTableRuleConfiguration.setKeyGenerateStrategy(new KeyGenerateStrategyConfiguration("id", "auto_increment"));
        autoTableRuleConfiguration.setAuditStrategy(new ShardingAuditStrategyConfiguration(Collections.singleton("audit_algorithm"), true));
        result.getAutoTables().add(autoTableRuleConfiguration);
        result.getBindingTableGroups().add(new ShardingTableReferenceRuleConfiguration("foo", shardingTableRuleConfig.getLogicTable() + "," + subTableRuleConfig.getLogicTable()));
        result.setDefaultDatabaseShardingStrategy(new StandardShardingStrategyConfiguration("ds_id", "standard"));
        result.setDefaultTableShardingStrategy(new StandardShardingStrategyConfiguration("table_id", "standard"));
        result.setDefaultShardingColumn("table_id");
        result.setDefaultKeyGenerateStrategy(new KeyGenerateStrategyConfiguration("id", "default"));
        result.setDefaultAuditStrategy(new ShardingAuditStrategyConfiguration(Collections.singletonList("audit_algorithm"), false));
        result.getShardingAlgorithms().put("core_standard_fixture", new AlgorithmConfiguration("CORE.STANDARD.FIXTURE", new Properties()));
        result.getShardingAlgorithms().put("hash_mod", new AlgorithmConfiguration("hash_mod", PropertiesBuilder.build(new PropertiesBuilder.Property("sharding-count", "4"))));
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
    
    @Test
    void assertSwapToObjectEmpty() {
        Collection<YamlDataNode> config = new LinkedList<>();
        assertFalse(swapper.swapToObject(config).isPresent());
    }
    
    @Test
    void assertSwapToObject() {
        Collection<YamlDataNode> config = new LinkedList<>();
        config.add(new YamlDataNode("/metadata/foo_db/rules/sharding/tables/LOGIC_TABLE/versions/0", "actualDataNodes: ds_${0..1}.table_${0..2}\n"
                + "auditStrategy:\n"
                + "  allowHintDisable: false\n"
                + "  auditorNames:\n"
                + "  - audit_algorithm\n"
                + "databaseStrategy:\n"
                + "  standard:\n"
                + "    shardingAlgorithmName: database_inline\n"
                + "    shardingColumn: user_id\n"
                + "keyGenerateStrategy:\n"
                + "  column: id\n"
                + "  keyGeneratorName: uuid\n"
                + "logicTable: LOGIC_TABLE\n"
                + "tableStrategy:\n"
                + "  standard:\n"
                + "    shardingAlgorithmName: table_inline\n"
                + "    shardingColumn: order_id\n"));
        config.add(new YamlDataNode("/metadata/foo_db/rules/sharding/tables/SUB_LOGIC_TABLE/versions/0", "actualDataNodes: ds_${0..1}.sub_table_${0..2}\n"
                + "databaseStrategy:\n"
                + "  standard:\n"
                + "    shardingAlgorithmName: database_inline\n"
                + "    shardingColumn: user_id\n"
                + "keyGenerateStrategy:\n"
                + "  column: id\n"
                + "  keyGeneratorName: auto_increment\n"
                + "logicTable: SUB_LOGIC_TABLE\n"
                + "tableStrategy:\n"
                + "  standard:\n"
                + "    shardingAlgorithmName: table_inline\n"
                + "    shardingColumn: order_id\n"));
        config.add(new YamlDataNode("/metadata/foo_db/rules/sharding/auto_tables/auto_table/versions/0", "actualDataSources: ds_1,ds_2\n"
                + "auditStrategy:\n"
                + "  allowHintDisable: true\n"
                + "  auditorNames:\n"
                + "  - audit_algorithm\n"
                + "keyGenerateStrategy:\n"
                + "  column: id\n"
                + "  keyGeneratorName: auto_increment\n"
                + "logicTable: auto_table\n"
                + "shardingStrategy:\n"
                + "  standard:\n"
                + "    shardingAlgorithmName: hash_mod\n"
                + "    shardingColumn: user_id\n"));
        config.add(new YamlDataNode("/metadata/foo_db/rules/sharding/binding_tables/foo/versions/0", "foo:LOGIC_TABLE,SUB_LOGIC_TABLE"));
        config.add(new YamlDataNode("/metadata/foo_db/rules/sharding/default_strategies/default_database_strategy/versions/0", "standard:\n"
                + "  shardingAlgorithmName: standard\n"
                + "  shardingColumn: ds_id\n"));
        config.add(new YamlDataNode("/metadata/foo_db/rules/sharding/default_strategies/default_table_strategy/versions/0", "standard:\n"
                + "  shardingAlgorithmName: standard\n"
                + "  shardingColumn: table_id\n"));
        config.add(new YamlDataNode("/metadata/foo_db/rules/sharding/default_strategies/default_key_generate_strategy/versions/0", "column: id\n"
                + "keyGeneratorName: default\n"));
        config.add(new YamlDataNode("/metadata/foo_db/rules/sharding/default_strategies/default_audit_strategy/versions/0", "allowHintDisable: false\n"
                + "auditorNames:\n"
                + "- audit_algorithm\n"));
        config.add(new YamlDataNode("/metadata/foo_db/rules/sharding/algorithms/core_standard_fixture/versions/0", "type: CORE.STANDARD.FIXTURE\n"));
        config.add(new YamlDataNode("/metadata/foo_db/rules/sharding/algorithms/hash_mod/versions/0", "props:\n"
                + "  sharding-count: '4'\n"
                + "type: hash_mod\n"));
        config.add(new YamlDataNode("/metadata/foo_db/rules/sharding/key_generators/uuid/versions/0", "type: UUID\n"));
        config.add(new YamlDataNode("/metadata/foo_db/rules/sharding/key_generators/default/versions/0", "type: UUID\n"));
        config.add(new YamlDataNode("/metadata/foo_db/rules/sharding/key_generators/auto_increment/versions/0", "type: AUTO_INCREMENT.FIXTURE\n"));
        config.add(new YamlDataNode("/metadata/foo_db/rules/sharding/auditors/audit_algorithm/versions/0", "type: DML_SHARDING_CONDITIONS\n"));
        config.add(new YamlDataNode("/metadata/foo_db/rules/sharding/default_strategies/default_sharding_column/versions/0", "table_id"));
        ShardingRuleConfiguration result = swapper.swapToObject(config).get();
        assertThat(result.getTables().size(), is(2));
        assertThat(result.getTables().iterator().next().getLogicTable(), is("LOGIC_TABLE"));
        assertThat(result.getTables().iterator().next().getActualDataNodes(), is("ds_${0..1}.table_${0..2}"));
        assertTrue(result.getTables().iterator().next().getDatabaseShardingStrategy() instanceof StandardShardingStrategyConfiguration);
        assertThat(((StandardShardingStrategyConfiguration) result.getTables().iterator().next().getDatabaseShardingStrategy()).getShardingColumn(), is("user_id"));
        assertThat(result.getTables().iterator().next().getDatabaseShardingStrategy().getShardingAlgorithmName(), is("database_inline"));
        assertThat(result.getTables().iterator().next().getDatabaseShardingStrategy().getType(), is("STANDARD"));
        assertTrue(result.getTables().iterator().next().getTableShardingStrategy() instanceof StandardShardingStrategyConfiguration);
        assertThat(((StandardShardingStrategyConfiguration) result.getTables().iterator().next().getTableShardingStrategy()).getShardingColumn(), is("order_id"));
        assertThat(result.getTables().iterator().next().getTableShardingStrategy().getShardingAlgorithmName(), is("table_inline"));
        assertThat(result.getTables().iterator().next().getTableShardingStrategy().getType(), is("STANDARD"));
        assertThat(result.getTables().iterator().next().getKeyGenerateStrategy().getColumn(), is("id"));
        assertThat(result.getTables().iterator().next().getKeyGenerateStrategy().getKeyGeneratorName(), is("uuid"));
        assertThat(result.getTables().iterator().next().getAuditStrategy().getAuditorNames().size(), is(1));
        assertThat(result.getTables().iterator().next().getAuditStrategy().getAuditorNames().iterator().next(), is("audit_algorithm"));
        assertFalse(result.getTables().iterator().next().getAuditStrategy().isAllowHintDisable());
        assertThat(result.getAutoTables().size(), is(1));
        assertThat(result.getAutoTables().iterator().next().getLogicTable(), is("auto_table"));
        assertThat(result.getAutoTables().iterator().next().getActualDataSources(), is("ds_1,ds_2"));
        assertTrue(result.getAutoTables().iterator().next().getShardingStrategy() instanceof StandardShardingStrategyConfiguration);
        assertThat(((StandardShardingStrategyConfiguration) result.getAutoTables().iterator().next().getShardingStrategy()).getShardingColumn(), is("user_id"));
        assertThat(result.getAutoTables().iterator().next().getShardingStrategy().getShardingAlgorithmName(), is("hash_mod"));
        assertThat(result.getAutoTables().iterator().next().getShardingStrategy().getType(), is("STANDARD"));
        assertThat(result.getAutoTables().iterator().next().getKeyGenerateStrategy().getColumn(), is("id"));
        assertThat(result.getAutoTables().iterator().next().getKeyGenerateStrategy().getKeyGeneratorName(), is("auto_increment"));
        assertThat(result.getAutoTables().iterator().next().getAuditStrategy().getAuditorNames().size(), is(1));
        assertThat(result.getAutoTables().iterator().next().getAuditStrategy().getAuditorNames().iterator().next(), is("audit_algorithm"));
        assertTrue(result.getAutoTables().iterator().next().getAuditStrategy().isAllowHintDisable());
        assertThat(result.getBindingTableGroups().size(), is(1));
        assertThat(result.getBindingTableGroups().iterator().next().getName(), is("foo"));
        assertThat(result.getBindingTableGroups().iterator().next().getReference(), is("LOGIC_TABLE,SUB_LOGIC_TABLE"));
        assertTrue(result.getDefaultDatabaseShardingStrategy() instanceof StandardShardingStrategyConfiguration);
        assertThat(((StandardShardingStrategyConfiguration) result.getDefaultDatabaseShardingStrategy()).getType(), is("STANDARD"));
        assertThat(((StandardShardingStrategyConfiguration) result.getDefaultDatabaseShardingStrategy()).getShardingColumn(), is("ds_id"));
        assertThat(result.getDefaultDatabaseShardingStrategy().getShardingAlgorithmName(), is("standard"));
        assertTrue(result.getDefaultTableShardingStrategy() instanceof StandardShardingStrategyConfiguration);
        assertThat(((StandardShardingStrategyConfiguration) result.getDefaultTableShardingStrategy()).getType(), is("STANDARD"));
        assertThat(((StandardShardingStrategyConfiguration) result.getDefaultTableShardingStrategy()).getShardingColumn(), is("table_id"));
        assertThat(result.getDefaultTableShardingStrategy().getShardingAlgorithmName(), is("standard"));
        assertThat(result.getDefaultKeyGenerateStrategy().getColumn(), is("id"));
        assertThat(result.getDefaultKeyGenerateStrategy().getKeyGeneratorName(), is("default"));
        assertThat(result.getDefaultAuditStrategy().getAuditorNames().size(), is(1));
        assertThat(result.getDefaultAuditStrategy().getAuditorNames().iterator().next(), is("audit_algorithm"));
        assertFalse(result.getDefaultAuditStrategy().isAllowHintDisable());
        assertThat(result.getDefaultShardingColumn(), is("table_id"));
        assertThat(result.getShardingAlgorithms().size(), is(2));
        assertThat(result.getShardingAlgorithms().get("core_standard_fixture").getType(), is("CORE.STANDARD.FIXTURE"));
        assertThat(result.getShardingAlgorithms().get("hash_mod").getType(), is("hash_mod"));
        assertThat(result.getShardingAlgorithms().get("hash_mod").getProps().size(), is(1));
        assertThat(result.getShardingAlgorithms().get("hash_mod").getProps().get("sharding-count"), is("4"));
        assertThat(result.getKeyGenerators().size(), is(3));
        assertThat(result.getKeyGenerators().get("uuid").getType(), is("UUID"));
        assertThat(result.getKeyGenerators().get("uuid").getProps().size(), is(0));
        assertThat(result.getKeyGenerators().get("auto_increment").getType(), is("AUTO_INCREMENT.FIXTURE"));
        assertThat(result.getAuditors().size(), is(1));
        assertThat(result.getAuditors().get("audit_algorithm").getType(), is("DML_SHARDING_CONDITIONS"));
        assertThat(result.getAuditors().get("audit_algorithm").getProps().size(), is(0));
        assertNull(result.getShardingCache());
    }
}
