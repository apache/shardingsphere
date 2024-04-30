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

import org.apache.shardingsphere.infra.algorithm.core.config.AlgorithmConfiguration;
import org.apache.shardingsphere.infra.util.yaml.datanode.RepositoryTuple;
import org.apache.shardingsphere.infra.yaml.config.swapper.rule.YamlRuleConfigurationSwapperEngine;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingAutoTableRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingTableReferenceRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingTableRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.audit.ShardingAuditStrategyConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.keygen.KeyGenerateStrategyConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.StandardShardingStrategyConfiguration;
import org.apache.shardingsphere.sharding.yaml.config.YamlShardingRuleConfiguration;
import org.apache.shardingsphere.test.util.PropertiesBuilder;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Optional;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ShardingRuleConfigurationRepositoryTupleSwapperTest {
    
    private final ShardingRuleConfigurationRepositoryTupleSwapper swapper = new ShardingRuleConfigurationRepositoryTupleSwapper();
    
    @Test
    void assertSwapToRepositoryTuplesWithEmptyRule() {
        assertTrue(swapper.swapToRepositoryTuples(new YamlShardingRuleConfiguration()).isEmpty());
    }
    
    @Test
    void assertSwapToRepositoryTuples() {
        YamlShardingRuleConfiguration yamlRuleConfig = (YamlShardingRuleConfiguration) new YamlRuleConfigurationSwapperEngine().swapToYamlRuleConfiguration(createMaximumShardingRule());
        Collection<RepositoryTuple> actual = swapper.swapToRepositoryTuples(yamlRuleConfig);
        assertThat(actual.size(), is(15));
        Iterator<RepositoryTuple> iterator = actual.iterator();
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
        ShardingAutoTableRuleConfiguration autoTableRuleConfig = new ShardingAutoTableRuleConfiguration("auto_table", "ds_1,ds_2");
        autoTableRuleConfig.setShardingStrategy(new StandardShardingStrategyConfiguration("user_id", "hash_mod"));
        autoTableRuleConfig.setKeyGenerateStrategy(new KeyGenerateStrategyConfiguration("id", "auto_increment"));
        autoTableRuleConfig.setAuditStrategy(new ShardingAuditStrategyConfiguration(Collections.singleton("audit_algorithm"), true));
        result.getAutoTables().add(autoTableRuleConfig);
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
    void assertSwapToObjectWithEmptyTuple() {
        assertFalse(swapper.swapToObject0(Collections.emptyList()).isPresent());
    }
    
    @Test
    void assertSwapToObject() {
        Collection<RepositoryTuple> repositoryTuples = new LinkedList<>();
        repositoryTuples.add(new RepositoryTuple("/metadata/foo_db/rules/sharding/tables/LOGIC_TABLE/versions/0", "actualDataNodes: ds_${0..1}.table_${0..2}\n"
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
        repositoryTuples.add(new RepositoryTuple("/metadata/foo_db/rules/sharding/tables/SUB_LOGIC_TABLE/versions/0", "actualDataNodes: ds_${0..1}.sub_table_${0..2}\n"
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
        repositoryTuples.add(new RepositoryTuple("/metadata/foo_db/rules/sharding/auto_tables/auto_table/versions/0", "actualDataSources: ds_1,ds_2\n"
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
        repositoryTuples.add(new RepositoryTuple("/metadata/foo_db/rules/sharding/binding_tables/foo/versions/0", "foo:LOGIC_TABLE,SUB_LOGIC_TABLE"));
        repositoryTuples.add(new RepositoryTuple("/metadata/foo_db/rules/sharding/default_strategies/default_database_strategy/versions/0", "standard:\n"
                + "  shardingAlgorithmName: standard\n"
                + "  shardingColumn: ds_id\n"));
        repositoryTuples.add(new RepositoryTuple("/metadata/foo_db/rules/sharding/default_strategies/default_table_strategy/versions/0", "standard:\n"
                + "  shardingAlgorithmName: standard\n"
                + "  shardingColumn: table_id\n"));
        repositoryTuples.add(new RepositoryTuple("/metadata/foo_db/rules/sharding/default_strategies/default_key_generate_strategy/versions/0", "column: id\n"
                + "keyGeneratorName: default\n"));
        repositoryTuples.add(new RepositoryTuple("/metadata/foo_db/rules/sharding/default_strategies/default_audit_strategy/versions/0", "allowHintDisable: false\n"
                + "auditorNames:\n"
                + "- audit_algorithm\n"));
        repositoryTuples.add(new RepositoryTuple("/metadata/foo_db/rules/sharding/algorithms/core_standard_fixture/versions/0", "type: CORE.STANDARD.FIXTURE\n"));
        repositoryTuples.add(new RepositoryTuple("/metadata/foo_db/rules/sharding/algorithms/hash_mod/versions/0", "props:\n"
                + "  sharding-count: '4'\n"
                + "type: hash_mod\n"));
        repositoryTuples.add(new RepositoryTuple("/metadata/foo_db/rules/sharding/key_generators/uuid/versions/0", "type: UUID\n"));
        repositoryTuples.add(new RepositoryTuple("/metadata/foo_db/rules/sharding/key_generators/default/versions/0", "type: UUID\n"));
        repositoryTuples.add(new RepositoryTuple("/metadata/foo_db/rules/sharding/key_generators/auto_increment/versions/0", "type: AUTO_INCREMENT.FIXTURE\n"));
        repositoryTuples.add(new RepositoryTuple("/metadata/foo_db/rules/sharding/auditors/audit_algorithm/versions/0", "type: DML_SHARDING_CONDITIONS\n"));
        repositoryTuples.add(new RepositoryTuple("/metadata/foo_db/rules/sharding/default_strategies/default_sharding_column/versions/0", "table_id"));
        Optional<YamlShardingRuleConfiguration> yamlRuleConfig = swapper.swapToObject0(repositoryTuples);
        assertTrue(yamlRuleConfig.isPresent());
        ShardingRuleConfiguration actual = (ShardingRuleConfiguration) new YamlRuleConfigurationSwapperEngine().swapToRuleConfiguration(yamlRuleConfig.get());
        assertThat(actual.getTables().size(), is(2));
        assertThat(actual.getTables().iterator().next().getLogicTable(), is("LOGIC_TABLE"));
        assertThat(actual.getTables().iterator().next().getActualDataNodes(), is("ds_${0..1}.table_${0..2}"));
        assertInstanceOf(StandardShardingStrategyConfiguration.class, actual.getTables().iterator().next().getDatabaseShardingStrategy());
        assertThat(((StandardShardingStrategyConfiguration) actual.getTables().iterator().next().getDatabaseShardingStrategy()).getShardingColumn(), is("user_id"));
        assertThat(actual.getTables().iterator().next().getDatabaseShardingStrategy().getShardingAlgorithmName(), is("database_inline"));
        assertThat(actual.getTables().iterator().next().getDatabaseShardingStrategy().getType(), is("STANDARD"));
        assertInstanceOf(StandardShardingStrategyConfiguration.class, actual.getTables().iterator().next().getTableShardingStrategy());
        assertThat(((StandardShardingStrategyConfiguration) actual.getTables().iterator().next().getTableShardingStrategy()).getShardingColumn(), is("order_id"));
        assertThat(actual.getTables().iterator().next().getTableShardingStrategy().getShardingAlgorithmName(), is("table_inline"));
        assertThat(actual.getTables().iterator().next().getTableShardingStrategy().getType(), is("STANDARD"));
        assertThat(actual.getTables().iterator().next().getKeyGenerateStrategy().getColumn(), is("id"));
        assertThat(actual.getTables().iterator().next().getKeyGenerateStrategy().getKeyGeneratorName(), is("uuid"));
        assertThat(actual.getTables().iterator().next().getAuditStrategy().getAuditorNames().size(), is(1));
        assertThat(actual.getTables().iterator().next().getAuditStrategy().getAuditorNames().iterator().next(), is("audit_algorithm"));
        assertFalse(actual.getTables().iterator().next().getAuditStrategy().isAllowHintDisable());
        assertThat(actual.getAutoTables().size(), is(1));
        assertThat(actual.getAutoTables().iterator().next().getLogicTable(), is("auto_table"));
        assertThat(actual.getAutoTables().iterator().next().getActualDataSources(), is("ds_1,ds_2"));
        assertInstanceOf(StandardShardingStrategyConfiguration.class, actual.getAutoTables().iterator().next().getShardingStrategy());
        assertThat(((StandardShardingStrategyConfiguration) actual.getAutoTables().iterator().next().getShardingStrategy()).getShardingColumn(), is("user_id"));
        assertThat(actual.getAutoTables().iterator().next().getShardingStrategy().getShardingAlgorithmName(), is("hash_mod"));
        assertThat(actual.getAutoTables().iterator().next().getShardingStrategy().getType(), is("STANDARD"));
        assertThat(actual.getAutoTables().iterator().next().getKeyGenerateStrategy().getColumn(), is("id"));
        assertThat(actual.getAutoTables().iterator().next().getKeyGenerateStrategy().getKeyGeneratorName(), is("auto_increment"));
        assertThat(actual.getAutoTables().iterator().next().getAuditStrategy().getAuditorNames().size(), is(1));
        assertThat(actual.getAutoTables().iterator().next().getAuditStrategy().getAuditorNames().iterator().next(), is("audit_algorithm"));
        assertTrue(actual.getAutoTables().iterator().next().getAuditStrategy().isAllowHintDisable());
        assertThat(actual.getBindingTableGroups().size(), is(1));
        assertThat(actual.getBindingTableGroups().iterator().next().getName(), is("foo"));
        assertThat(actual.getBindingTableGroups().iterator().next().getReference(), is("LOGIC_TABLE,SUB_LOGIC_TABLE"));
        assertInstanceOf(StandardShardingStrategyConfiguration.class, actual.getDefaultDatabaseShardingStrategy());
        assertThat(((StandardShardingStrategyConfiguration) actual.getDefaultDatabaseShardingStrategy()).getType(), is("STANDARD"));
        assertThat(((StandardShardingStrategyConfiguration) actual.getDefaultDatabaseShardingStrategy()).getShardingColumn(), is("ds_id"));
        assertThat(actual.getDefaultDatabaseShardingStrategy().getShardingAlgorithmName(), is("standard"));
        assertInstanceOf(StandardShardingStrategyConfiguration.class, actual.getDefaultTableShardingStrategy());
        assertThat(((StandardShardingStrategyConfiguration) actual.getDefaultTableShardingStrategy()).getType(), is("STANDARD"));
        assertThat(((StandardShardingStrategyConfiguration) actual.getDefaultTableShardingStrategy()).getShardingColumn(), is("table_id"));
        assertThat(actual.getDefaultTableShardingStrategy().getShardingAlgorithmName(), is("standard"));
        assertThat(actual.getDefaultKeyGenerateStrategy().getColumn(), is("id"));
        assertThat(actual.getDefaultKeyGenerateStrategy().getKeyGeneratorName(), is("default"));
        assertThat(actual.getDefaultAuditStrategy().getAuditorNames().size(), is(1));
        assertThat(actual.getDefaultAuditStrategy().getAuditorNames().iterator().next(), is("audit_algorithm"));
        assertFalse(actual.getDefaultAuditStrategy().isAllowHintDisable());
        assertThat(actual.getDefaultShardingColumn(), is("table_id"));
        assertThat(actual.getShardingAlgorithms().size(), is(2));
        assertThat(actual.getShardingAlgorithms().get("core_standard_fixture").getType(), is("CORE.STANDARD.FIXTURE"));
        assertThat(actual.getShardingAlgorithms().get("hash_mod").getType(), is("hash_mod"));
        assertThat(actual.getShardingAlgorithms().get("hash_mod").getProps().size(), is(1));
        assertThat(actual.getShardingAlgorithms().get("hash_mod").getProps().get("sharding-count"), is("4"));
        assertThat(actual.getKeyGenerators().size(), is(3));
        assertThat(actual.getKeyGenerators().get("uuid").getType(), is("UUID"));
        assertTrue(actual.getKeyGenerators().get("uuid").getProps().isEmpty());
        assertThat(actual.getKeyGenerators().get("auto_increment").getType(), is("AUTO_INCREMENT.FIXTURE"));
        assertThat(actual.getAuditors().size(), is(1));
        assertThat(actual.getAuditors().get("audit_algorithm").getType(), is("DML_SHARDING_CONDITIONS"));
        assertTrue(actual.getAuditors().get("audit_algorithm").getProps().isEmpty());
        assertNull(actual.getShardingCache());
    }
}
