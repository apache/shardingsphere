/*
 * Copyright 2016-2018 shardingsphere.io.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.shardingsphere.core.yaml.sharding;

import io.shardingsphere.api.config.rule.TableRuleConfiguration;
import io.shardingsphere.api.config.strategy.NoneShardingStrategyConfiguration;
import io.shardingsphere.core.keygen.DefaultKeyGenerator;
import io.shardingsphere.core.yaml.sharding.strategy.YamlNoneShardingStrategyConfiguration;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

public final class YamlTableRuleConfigurationTest {
    
    @Test(expected = NullPointerException.class)
    public void assertBuildWithoutLogicTable() {
        new YamlTableRuleConfiguration().build();
    }
    
    @Test
    public void assertBuildWithoutShardingStrategy() {
        TableRuleConfiguration actual = createYamlTableRuleConfig().build();
        assertTableRuleConfig(actual);
        assertWithoutShardingStrategy(actual);
    }
    
    @Test
    public void assertBuildWithShardingStrategy() {
        TableRuleConfiguration actual = createYamlTableRuleConfigWithShardingStrategy().build();
        assertTableRuleConfig(actual);
        assertWithShardingStrategy(actual);
    }
    
    private YamlTableRuleConfiguration createYamlTableRuleConfig() {
        YamlTableRuleConfiguration result = new YamlTableRuleConfiguration();
        result.setLogicTable("t_order");
        result.setActualDataNodes("ds_${0..1}.t_order_${0..1}");
        result.setKeyGeneratorColumnName("order_id");
        result.setKeyGeneratorClassName(DefaultKeyGenerator.class.getName());
        result.setLogicIndex("order_index");
        return result;
    }
    
    private YamlTableRuleConfiguration createYamlTableRuleConfigWithShardingStrategy() {
        YamlTableRuleConfiguration result = createYamlTableRuleConfig();
        YamlShardingStrategyConfiguration yamlShardingStrategyConfig = new YamlShardingStrategyConfiguration();
        yamlShardingStrategyConfig.setNone(new YamlNoneShardingStrategyConfiguration());
        result.setDatabaseStrategy(yamlShardingStrategyConfig);
        result.setTableStrategy(yamlShardingStrategyConfig);
        return result;
    }
    
    private void assertTableRuleConfig(final TableRuleConfiguration actual) {
        assertThat(actual.getLogicTable(), is("t_order"));
        assertThat(actual.getActualDataNodes(), is("ds_${0..1}.t_order_${0..1}"));
        assertThat(actual.getKeyGeneratorColumnName(), is("order_id"));
        assertThat(actual.getKeyGenerator(), instanceOf(DefaultKeyGenerator.class));
        assertThat(actual.getLogicIndex(), is("order_index"));
    }
    
    private void assertWithoutShardingStrategy(final TableRuleConfiguration actual) {
        assertNull(actual.getDatabaseShardingStrategyConfig());
        assertNull(actual.getTableShardingStrategyConfig());
    }
    
    private void assertWithShardingStrategy(final TableRuleConfiguration actual) {
        assertThat(actual.getDatabaseShardingStrategyConfig(), instanceOf(NoneShardingStrategyConfiguration.class));
        assertThat(actual.getTableShardingStrategyConfig(), instanceOf(NoneShardingStrategyConfiguration.class));
    }
}
