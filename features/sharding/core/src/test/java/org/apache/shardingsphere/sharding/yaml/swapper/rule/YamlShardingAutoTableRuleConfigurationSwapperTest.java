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

import org.apache.shardingsphere.sharding.api.config.rule.ShardingAutoTableRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.audit.ShardingAuditStrategyConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.keygen.KeyGenerateStrategyConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.StandardShardingStrategyConfiguration;
import org.apache.shardingsphere.sharding.yaml.config.rule.YamlShardingAutoTableRuleConfiguration;
import org.apache.shardingsphere.sharding.yaml.config.strategy.audit.YamlShardingAuditStrategyConfiguration;
import org.apache.shardingsphere.sharding.yaml.config.strategy.keygen.YamlKeyGenerateStrategyConfiguration;
import org.apache.shardingsphere.sharding.yaml.config.strategy.sharding.YamlShardingStrategyConfiguration;
import org.apache.shardingsphere.sharding.yaml.config.strategy.sharding.YamlStandardShardingStrategyConfiguration;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

class YamlShardingAutoTableRuleConfigurationSwapperTest {
    
    @Test
    void assertSwapToYamlConfiguration() {
        YamlShardingAutoTableRuleConfigurationSwapper swapper = new YamlShardingAutoTableRuleConfigurationSwapper();
        YamlShardingAutoTableRuleConfiguration actual = swapper.swapToYamlConfiguration(createShardingAutoTableRuleConfiguration());
        assertThat(actual.getShardingStrategy().getStandard().getShardingAlgorithmName(), is("hash_mod"));
        assertThat(actual.getKeyGenerateStrategy().getKeyGeneratorName(), is("auto_increment"));
        assertThat(actual.getAuditStrategy().getAuditorNames(), is(Collections.singletonList("audit_algorithm")));
    }
    
    private ShardingAutoTableRuleConfiguration createShardingAutoTableRuleConfiguration() {
        ShardingAutoTableRuleConfiguration result = new ShardingAutoTableRuleConfiguration("auto_table", "ds_1,ds_2");
        result.setShardingStrategy(new StandardShardingStrategyConfiguration("user_id", "hash_mod"));
        result.setKeyGenerateStrategy(new KeyGenerateStrategyConfiguration("id", "auto_increment"));
        result.setAuditStrategy(new ShardingAuditStrategyConfiguration(Collections.singletonList("audit_algorithm"), true));
        return result;
    }
    
    @Test
    void assertSwapToObject() {
        YamlShardingAutoTableRuleConfigurationSwapper swapper = new YamlShardingAutoTableRuleConfigurationSwapper();
        ShardingAutoTableRuleConfiguration actual = swapper.swapToObject(createYamlShardingAutoTableRuleConfiguration());
        assertThat(actual.getShardingStrategy().getShardingAlgorithmName(), is("hash_mod"));
        assertThat(actual.getKeyGenerateStrategy().getKeyGeneratorName(), is("auto_increment"));
        assertThat(actual.getAuditStrategy().getAuditorNames(), is(Collections.singletonList("audit_algorithm")));
    }
    
    private YamlShardingAutoTableRuleConfiguration createYamlShardingAutoTableRuleConfiguration() {
        YamlShardingAutoTableRuleConfiguration result = new YamlShardingAutoTableRuleConfiguration();
        result.setLogicTable("auto_table");
        result.setActualDataSources("ds_1,ds_2");
        result.setShardingStrategy(createYamlShardingStrategyConfiguration());
        result.setKeyGenerateStrategy(createYamlKeyGenerateStrategyConfiguration());
        result.setAuditStrategy(createYamlShardingAuditStrategyConfiguration());
        return result;
    }
    
    private YamlShardingStrategyConfiguration createYamlShardingStrategyConfiguration() {
        YamlShardingStrategyConfiguration result = new YamlShardingStrategyConfiguration();
        YamlStandardShardingStrategyConfiguration yamlStandardShardingStrategyConfig = new YamlStandardShardingStrategyConfiguration();
        yamlStandardShardingStrategyConfig.setShardingColumn("user_id");
        yamlStandardShardingStrategyConfig.setShardingAlgorithmName("hash_mod");
        result.setStandard(yamlStandardShardingStrategyConfig);
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
        result.setAllowHintDisable(true);
        return result;
    }
}
