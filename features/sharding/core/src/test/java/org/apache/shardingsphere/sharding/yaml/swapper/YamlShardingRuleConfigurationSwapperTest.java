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
import org.apache.shardingsphere.infra.algorithm.core.yaml.YamlAlgorithmConfiguration;
import org.apache.shardingsphere.infra.spi.type.ordered.OrderedSPILoader;
import org.apache.shardingsphere.infra.yaml.config.swapper.rule.YamlRuleConfigurationSwapper;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.cache.ShardingCacheConfiguration;
import org.apache.shardingsphere.sharding.api.config.cache.ShardingCacheOptionsConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingAutoTableRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingTableReferenceRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingTableRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.audit.ShardingAuditStrategyConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.keygen.KeyGenerateStrategyConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.StandardShardingStrategyConfiguration;
import org.apache.shardingsphere.sharding.yaml.config.YamlShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.yaml.config.cache.YamlShardingCacheConfiguration;
import org.apache.shardingsphere.sharding.yaml.config.cache.YamlShardingCacheOptionsConfiguration;
import org.apache.shardingsphere.sharding.yaml.config.rule.YamlShardingAutoTableRuleConfiguration;
import org.apache.shardingsphere.sharding.yaml.config.rule.YamlTableRuleConfiguration;
import org.apache.shardingsphere.sharding.yaml.config.strategy.audit.YamlShardingAuditStrategyConfiguration;
import org.apache.shardingsphere.sharding.yaml.config.strategy.keygen.YamlKeyGenerateStrategyConfiguration;
import org.apache.shardingsphere.sharding.yaml.config.strategy.sharding.YamlShardingStrategyConfiguration;
import org.apache.shardingsphere.sharding.yaml.config.strategy.sharding.YamlStandardShardingStrategyConfiguration;
import org.apache.shardingsphere.test.util.PropertiesBuilder;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;

class YamlShardingRuleConfigurationSwapperTest {
    
    @Test
    void assertSwapToYamlConfiguration() {
        YamlShardingRuleConfiguration actual = getSwapper().swapToYamlConfiguration(createMaximumShardingRuleConfiguration());
        assertThat(actual.getTables().size(), is(2));
        assertThat(actual.getAutoTables().size(), is(1));
        assertThat(actual.getBindingTables().size(), is(1));
        assertYamlStrategies(actual);
        assertYamlAlgorithms(actual);
        assertThat(actual.getDefaultShardingColumn(), is("table_id"));
        assertThat(actual.getShardingCache().getAllowedMaxSqlLength(), is(100));
    }
    
    private void assertYamlStrategies(final YamlShardingRuleConfiguration actual) {
        assertThat(actual.getDefaultDatabaseStrategy().getStandard(), instanceOf(YamlStandardShardingStrategyConfiguration.class));
        assertThat(actual.getDefaultTableStrategy().getStandard(), instanceOf(YamlStandardShardingStrategyConfiguration.class));
        assertThat(actual.getDefaultKeyGenerateStrategy().getColumn(), is("id"));
        assertThat(actual.getDefaultAuditStrategy().getAuditorNames(), is(Collections.singletonList("audit_algorithm")));
    }
    
    private void assertYamlAlgorithms(final YamlShardingRuleConfiguration actual) {
        assertThat(actual.getShardingAlgorithms().size(), is(2));
        assertThat(actual.getKeyGenerators().size(), is(3));
        assertThat(actual.getAuditors().size(), is(1));
    }
    
    private ShardingRuleConfiguration createMaximumShardingRuleConfiguration() {
        ShardingRuleConfiguration result = new ShardingRuleConfiguration();
        ShardingTableRuleConfiguration shardingTableRuleConfig = createTableRuleConfiguration("logic_table", "ds_${0..1}.table_${0..2}");
        shardingTableRuleConfig.setKeyGenerateStrategy(new KeyGenerateStrategyConfiguration("id", "uuid"));
        shardingTableRuleConfig.setAuditStrategy(new ShardingAuditStrategyConfiguration(Collections.singleton("audit_algorithm"), false));
        result.getTables().add(shardingTableRuleConfig);
        ShardingTableRuleConfiguration subTableRuleConfig = createTableRuleConfiguration("sub_logic_table", "ds_${0..1}.sub_table_${0..2}");
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
        result.setShardingCache(new ShardingCacheConfiguration(100, new ShardingCacheOptionsConfiguration(true, 0, 0)));
        return result;
    }
    
    private ShardingTableRuleConfiguration createTableRuleConfiguration(final String logicTableName, final String actualDataNodes) {
        ShardingTableRuleConfiguration result = new ShardingTableRuleConfiguration(logicTableName, actualDataNodes);
        result.setDatabaseShardingStrategy(new StandardShardingStrategyConfiguration("user_id", "database_inline"));
        result.setTableShardingStrategy(new StandardShardingStrategyConfiguration("order_id", "table_inline"));
        return result;
    }
    
    @Test
    void assertSwapToObject() {
        ShardingRuleConfiguration actual = getSwapper().swapToObject(createYamlShardingRuleConfiguration());
        assertThat(actual.getTables().size(), is(1));
        assertThat(actual.getAutoTables().size(), is(1));
        assertThat(actual.getBindingTableGroups().size(), is(1));
        assertStrategies(actual);
        assertAlgorithms(actual);
        assertThat(actual.getDefaultShardingColumn(), is("table_id"));
        assertThat(actual.getShardingCache().getAllowedMaxSqlLength(), is(100));
    }
    
    private void assertStrategies(final ShardingRuleConfiguration actual) {
        assertThat(actual.getDefaultDatabaseShardingStrategy(), instanceOf(StandardShardingStrategyConfiguration.class));
        assertThat(actual.getDefaultTableShardingStrategy(), instanceOf(StandardShardingStrategyConfiguration.class));
        assertThat(actual.getDefaultKeyGenerateStrategy().getColumn(), is("id"));
        assertThat(actual.getDefaultAuditStrategy().getAuditorNames(), is(Collections.singletonList("audit_algorithm")));
    }
    
    private void assertAlgorithms(final ShardingRuleConfiguration actual) {
        assertThat(actual.getShardingAlgorithms().size(), is(1));
        assertThat(actual.getKeyGenerators().size(), is(1));
        assertThat(actual.getAuditors().size(), is(1));
    }
    
    private YamlShardingRuleConfiguration createYamlShardingRuleConfiguration() {
        YamlShardingRuleConfiguration result = new YamlShardingRuleConfiguration();
        YamlTableRuleConfiguration tableRuleConfig = new YamlTableRuleConfiguration();
        tableRuleConfig.setActualDataNodes("ds_${0..1}.table_${0..2}");
        result.getTables().put("logic_table", tableRuleConfig);
        YamlShardingAutoTableRuleConfiguration autoTableRuleConfig = new YamlShardingAutoTableRuleConfiguration();
        autoTableRuleConfig.setActualDataSources("ds_1,ds_2");
        result.getAutoTables().put("auto_table", autoTableRuleConfig);
        result.getBindingTables().add("foo:logic_table");
        result.setDefaultDatabaseStrategy(createYamlShardingStrategyConfig("ds_id"));
        result.setDefaultTableStrategy(createYamlShardingStrategyConfig("table_id"));
        result.setDefaultKeyGenerateStrategy(createYamlKeyGenerateStrategyConfig());
        result.setDefaultAuditStrategy(createYamlAuditStrategyConfig());
        result.getShardingAlgorithms().put("core_standard_fixture", createYamlAlgorithmConfig("CORE.STANDARD.FIXTURE"));
        result.getKeyGenerators().put("default", createYamlAlgorithmConfig("UUID"));
        result.getAuditors().put("audit_algorithm", createYamlAlgorithmConfig("DML_SHARDING_CONDITIONS"));
        result.setDefaultShardingColumn("table_id");
        result.setShardingCache(createYamlCacheConfig());
        return result;
    }
    
    private YamlShardingStrategyConfiguration createYamlShardingStrategyConfig(final String column) {
        YamlStandardShardingStrategyConfiguration yamlStandardShardingStrategyConfiguration = new YamlStandardShardingStrategyConfiguration();
        yamlStandardShardingStrategyConfiguration.setShardingColumn(column);
        yamlStandardShardingStrategyConfiguration.setShardingAlgorithmName("standard");
        YamlShardingStrategyConfiguration yamlShardingStrategyConfiguration = new YamlShardingStrategyConfiguration();
        yamlShardingStrategyConfiguration.setStandard(yamlStandardShardingStrategyConfiguration);
        return yamlShardingStrategyConfiguration;
    }
    
    private YamlKeyGenerateStrategyConfiguration createYamlKeyGenerateStrategyConfig() {
        YamlKeyGenerateStrategyConfiguration yamlKeyGenerateStrategyConfiguration = new YamlKeyGenerateStrategyConfiguration();
        yamlKeyGenerateStrategyConfiguration.setColumn("id");
        yamlKeyGenerateStrategyConfiguration.setKeyGeneratorName("default");
        return yamlKeyGenerateStrategyConfiguration;
    }
    
    private YamlShardingAuditStrategyConfiguration createYamlAuditStrategyConfig() {
        YamlShardingAuditStrategyConfiguration yamlShardingAuditStrategyConfiguration = new YamlShardingAuditStrategyConfiguration();
        yamlShardingAuditStrategyConfiguration.setAuditorNames(Collections.singletonList("audit_algorithm"));
        yamlShardingAuditStrategyConfiguration.setAllowHintDisable(false);
        return yamlShardingAuditStrategyConfiguration;
    }
    
    private YamlAlgorithmConfiguration createYamlAlgorithmConfig(final String type) {
        YamlAlgorithmConfiguration algorithmConfig = new YamlAlgorithmConfiguration();
        algorithmConfig.setType(type);
        return algorithmConfig;
    }
    
    private YamlShardingCacheConfiguration createYamlCacheConfig() {
        YamlShardingCacheConfiguration yamlShardingCacheConfiguration = new YamlShardingCacheConfiguration();
        yamlShardingCacheConfiguration.setAllowedMaxSqlLength(100);
        YamlShardingCacheOptionsConfiguration yamlShardingCacheOptionsConfiguration = new YamlShardingCacheOptionsConfiguration();
        yamlShardingCacheOptionsConfiguration.setSoftValues(true);
        yamlShardingCacheOptionsConfiguration.setMaximumSize(0);
        yamlShardingCacheOptionsConfiguration.setInitialCapacity(0);
        yamlShardingCacheConfiguration.setRouteCache(yamlShardingCacheOptionsConfiguration);
        return yamlShardingCacheConfiguration;
    }
    
    private YamlShardingRuleConfigurationSwapper getSwapper() {
        ShardingRuleConfiguration ruleConfig = mock(ShardingRuleConfiguration.class);
        return (YamlShardingRuleConfigurationSwapper) OrderedSPILoader.getServices(YamlRuleConfigurationSwapper.class, Collections.singleton(ruleConfig)).get(ruleConfig);
    }
}
