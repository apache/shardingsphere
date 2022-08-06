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
import org.apache.shardingsphere.sharding.yaml.config.rule.YamlTableRuleConfiguration;
import org.apache.shardingsphere.sharding.yaml.config.strategy.audit.YamlShardingAuditStrategyConfiguration;
import org.apache.shardingsphere.sharding.yaml.config.strategy.keygen.YamlKeyGenerateStrategyConfiguration;
import org.apache.shardingsphere.sharding.yaml.config.strategy.sharding.YamlShardingStrategyConfiguration;
import org.apache.shardingsphere.sharding.yaml.config.strategy.sharding.YamlStandardShardingStrategyConfiguration;
import org.junit.Test;

import java.util.Collections;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

public final class YamlShardingTableRuleConfigurationSwapperTest {
    
    private final YamlShardingTableRuleConfigurationSwapper swapper = new YamlShardingTableRuleConfigurationSwapper();
    
    @Test
    public void assertSwapToYamlConfiguration() {
        YamlTableRuleConfiguration actual = swapper.swapToYamlConfiguration(createShardingTableRuleConfiguration());
        assertThat(actual.getLogicTable(), is("tbl"));
        assertThat(actual.getActualDataNodes(), is("ds_$->{0..1}.tbl_$->{0..1}"));
        assertNotNull(actual.getDatabaseStrategy());
        assertNotNull(actual.getTableStrategy());
        assertNotNull(actual.getKeyGenerateStrategy());
        assertNotNull(actual.getAuditStrategy());
    }
    
    private ShardingTableRuleConfiguration createShardingTableRuleConfiguration() {
        ShardingTableRuleConfiguration result = new ShardingTableRuleConfiguration("tbl", "ds_$->{0..1}.tbl_$->{0..1}");
        result.setDatabaseShardingStrategy(mock(StandardShardingStrategyConfiguration.class));
        result.setTableShardingStrategy(mock(StandardShardingStrategyConfiguration.class));
        result.setKeyGenerateStrategy(mock(KeyGenerateStrategyConfiguration.class));
        result.setAuditStrategy(mock(ShardingAuditStrategyConfiguration.class));
        return result;
    }
    
    @Test(expected = NullPointerException.class)
    public void assertSwapToObjectWithoutLogicTable() {
        new YamlShardingTableRuleConfigurationSwapper().swapToObject(new YamlTableRuleConfiguration());
    }
    
    @Test
    public void assertSwapToObject() {
        ShardingTableRuleConfiguration actual = swapper.swapToObject(createYamlTableRuleConfiguration());
        assertThat(actual.getLogicTable(), is("tbl"));
        assertThat(actual.getActualDataNodes(), is("ds_$->{0..1}.tbl_$->{0..1}"));
        assertNotNull(actual.getDatabaseShardingStrategy());
        assertNotNull(actual.getTableShardingStrategy());
        assertNotNull(actual.getKeyGenerateStrategy());
        assertNotNull(actual.getAuditStrategy());
    }
    
    private YamlTableRuleConfiguration createYamlTableRuleConfiguration() {
        YamlTableRuleConfiguration result = new YamlTableRuleConfiguration();
        result.setLogicTable("tbl");
        result.setActualDataNodes("ds_$->{0..1}.tbl_$->{0..1}");
        YamlShardingStrategyConfiguration yamlShardingStrategyConfig = createYamlShardingStrategyConfiguration();
        result.setDatabaseStrategy(yamlShardingStrategyConfig);
        result.setTableStrategy(yamlShardingStrategyConfig);
        result.setKeyGenerateStrategy(createYamlKeyGenerateStrategyConfiguration());
        YamlShardingAuditStrategyConfiguration shardingAuditStrategyConfig = createYamlShardingAuditStrategyConfiguration();
        result.setAuditStrategy(shardingAuditStrategyConfig);
        return result;
    }
    
    private YamlShardingStrategyConfiguration createYamlShardingStrategyConfiguration() {
        YamlShardingStrategyConfiguration result = new YamlShardingStrategyConfiguration();
        YamlStandardShardingStrategyConfiguration standardShardingStrategyConfig = new YamlStandardShardingStrategyConfiguration();
        standardShardingStrategyConfig.setShardingColumn("col");
        standardShardingStrategyConfig.setShardingAlgorithmName("foo_sharding_algo");
        result.setStandard(standardShardingStrategyConfig);
        return result;
    }
    
    private YamlKeyGenerateStrategyConfiguration createYamlKeyGenerateStrategyConfiguration() {
        YamlKeyGenerateStrategyConfiguration result = new YamlKeyGenerateStrategyConfiguration();
        result.setColumn("col");
        result.setKeyGeneratorName("foo_keygen");
        return result;
    }
    
    private YamlShardingAuditStrategyConfiguration createYamlShardingAuditStrategyConfiguration() {
        YamlShardingAuditStrategyConfiguration result = new YamlShardingAuditStrategyConfiguration();
        result.setAuditorNames(Collections.singleton("foo_audit"));
        return result;
    }
}
