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

package org.apache.shardingsphere.sharding.yaml.swapper.rule;

import org.apache.shardingsphere.sharding.api.config.rule.ShardingTableRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.audit.ShardingAuditStrategyConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.keygen.KeyGenerateStrategyConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.StandardShardingStrategyConfiguration;
import org.apache.shardingsphere.sharding.exception.metadata.MissingRequiredShardingConfigurationException;
import org.apache.shardingsphere.sharding.yaml.config.rule.YamlTableRuleConfiguration;
import org.apache.shardingsphere.sharding.yaml.config.strategy.audit.YamlShardingAuditStrategyConfiguration;
import org.apache.shardingsphere.sharding.yaml.config.strategy.keygen.YamlKeyGenerateStrategyConfiguration;
import org.apache.shardingsphere.sharding.yaml.config.strategy.sharding.YamlShardingStrategyConfiguration;
import org.apache.shardingsphere.sharding.yaml.config.strategy.sharding.YamlStandardShardingStrategyConfiguration;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class YamlShardingTableRuleConfigurationSwapperTest {
    
    @Test
    void assertSwapToYamlConfiguration() {
        YamlShardingTableRuleConfigurationSwapper swapper = new YamlShardingTableRuleConfigurationSwapper();
        YamlTableRuleConfiguration actual = swapper.swapToYamlConfiguration(createShardingTableRuleConfiguration());
        assertThat(actual.getDatabaseStrategy().getStandard().getShardingAlgorithmName(), is("standard"));
        assertThat(actual.getTableStrategy().getStandard().getShardingAlgorithmName(), is("standard"));
        assertThat(actual.getKeyGenerateStrategy().getKeyGeneratorName(), is("auto_increment"));
        assertThat(actual.getAuditStrategy().getAuditorNames(), is(Collections.singletonList("audit_algorithm")));
    }
    
    private ShardingTableRuleConfiguration createShardingTableRuleConfiguration() {
        ShardingTableRuleConfiguration result = new ShardingTableRuleConfiguration("logic_table", "ds_${0..1}.table_${0..2}");
        result.setDatabaseShardingStrategy(new StandardShardingStrategyConfiguration("ds_id", "standard"));
        result.setTableShardingStrategy(new StandardShardingStrategyConfiguration("table_id", "standard"));
        result.setKeyGenerateStrategy(new KeyGenerateStrategyConfiguration("id", "auto_increment"));
        result.setAuditStrategy(new ShardingAuditStrategyConfiguration(Collections.singletonList("audit_algorithm"), false));
        return result;
    }
    
    @Test
    void assertSwapToObject() {
        YamlShardingTableRuleConfigurationSwapper swapper = new YamlShardingTableRuleConfigurationSwapper();
        ShardingTableRuleConfiguration actual = swapper.swapToObject(createYamlTableRuleConfiguration());
        assertThat(actual.getDatabaseShardingStrategy().getShardingAlgorithmName(), is("standard"));
        assertThat(actual.getTableShardingStrategy().getShardingAlgorithmName(), is("standard"));
        assertThat(actual.getKeyGenerateStrategy().getKeyGeneratorName(), is("auto_increment"));
        assertThat(actual.getAuditStrategy().getAuditorNames(), is(Collections.singletonList("audit_algorithm")));
    }
    
    private YamlTableRuleConfiguration createYamlTableRuleConfiguration() {
        YamlTableRuleConfiguration result = new YamlTableRuleConfiguration();
        result.setLogicTable("logic_table");
        result.setActualDataNodes("ds_${0..1}.table_${0..2}");
        result.setDatabaseStrategy(createYamlShardingStrategyConfiguration());
        result.setTableStrategy(createYamlShardingStrategyConfiguration());
        result.setKeyGenerateStrategy(createYamlKeyGenerateStrategyConfiguration());
        result.setAuditStrategy(createYamlShardingAuditStrategyConfiguration());
        return result;
    }
    
    private YamlShardingStrategyConfiguration createYamlShardingStrategyConfiguration() {
        YamlShardingStrategyConfiguration result = new YamlShardingStrategyConfiguration();
        YamlStandardShardingStrategyConfiguration yamlStandardShardingStrategyConfiguration = new YamlStandardShardingStrategyConfiguration();
        yamlStandardShardingStrategyConfiguration.setShardingColumn("user_id");
        yamlStandardShardingStrategyConfiguration.setShardingAlgorithmName("standard");
        result.setStandard(yamlStandardShardingStrategyConfiguration);
        return result;
    }
    
    private YamlKeyGenerateStrategyConfiguration createYamlKeyGenerateStrategyConfiguration() {
        YamlKeyGenerateStrategyConfiguration result = new YamlKeyGenerateStrategyConfiguration();
        result.setColumn("id");
        result.setKeyGeneratorName("auto_increment");
        return result;
    }
    
    private YamlShardingAuditStrategyConfiguration createYamlShardingAuditStrategyConfiguration() {
        YamlShardingAuditStrategyConfiguration result = new YamlShardingAuditStrategyConfiguration();
        result.setAuditorNames(Collections.singletonList("audit_algorithm"));
        result.setAllowHintDisable(false);
        return result;
    }
    
    @Test
    void assertSwapToObjectWithNullLogicTable() {
        YamlShardingTableRuleConfigurationSwapper swapper = new YamlShardingTableRuleConfigurationSwapper();
        assertThrows(MissingRequiredShardingConfigurationException.class, () -> swapper.swapToObject(new YamlTableRuleConfiguration()));
    }
}
