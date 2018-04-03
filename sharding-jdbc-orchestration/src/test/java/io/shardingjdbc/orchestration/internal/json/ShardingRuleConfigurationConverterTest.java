/*
 * Copyright 1999-2015 dangdang.com.
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

package io.shardingjdbc.orchestration.internal.json;

import io.shardingjdbc.core.api.config.ShardingRuleConfiguration;
import io.shardingjdbc.core.api.config.TableRuleConfiguration;
import io.shardingjdbc.core.api.config.strategy.ComplexShardingStrategyConfiguration;
import io.shardingjdbc.core.api.config.strategy.HintShardingStrategyConfiguration;
import io.shardingjdbc.core.api.config.strategy.InlineShardingStrategyConfiguration;
import io.shardingjdbc.core.api.config.strategy.NoneShardingStrategyConfiguration;
import io.shardingjdbc.core.api.config.strategy.ShardingStrategyConfiguration;
import io.shardingjdbc.core.api.config.strategy.StandardShardingStrategyConfiguration;
import io.shardingjdbc.orchestration.internal.json.fixture.TestComplexKeysShardingAlgorithm;
import io.shardingjdbc.orchestration.internal.json.fixture.TestHintShardingAlgorithm;
import io.shardingjdbc.orchestration.internal.json.fixture.TestPreciseShardingAlgorithm;
import io.shardingjdbc.orchestration.internal.json.fixture.TestRangeShardingAlgorithm;
import org.junit.Test;

import java.util.Iterator;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

public final class ShardingRuleConfigurationConverterTest {
    
    private final String commonShardingRuleConfigJson = "{\"tableRuleConfigs\":[{\"logicTable\":\"t_order\",\"actualDataNodes\":\"t_order_${[0, 1]}\","
            + "\"databaseShardingStrategyConfig\":{},\"tableShardingStrategyConfig\":{},\"keyGenerator\":{}},"
            + "{\"logicTable\":\"t_order_item\",\"actualDataNodes\":\"t_order_item_${[0, 1]}\","
            + "\"databaseShardingStrategyConfig\":{},\"tableShardingStrategyConfig\":{},\"keyGenerator\":{}}"
            + "],\"bindingTableGroups\":[\"t_order, t_order_item\"],\"defaultDatabaseShardingStrategyConfig\":{},";
    
    private final String defaultKeyGenerator = ",\"defaultKeyGenerator\":{}";
    
    private final String masterSlaveRuleConfigJson = ",\"masterSlaveRuleConfigs\":[]}";
    
    @Test
    public void assertToJsonForStandardStrategy() {
        StandardShardingStrategyConfiguration actual = new StandardShardingStrategyConfiguration("order_id", new TestPreciseShardingAlgorithm(), new TestRangeShardingAlgorithm());
        assertThat(ShardingRuleConfigurationConverter.toJson(getCommonShardingRuleConfig(actual)), is(getJsonForStandardStrategy()));
    }
    
    @Test
    public void assertToJsonForComplexStrategy() {
        ComplexShardingStrategyConfiguration actual = new ComplexShardingStrategyConfiguration("order_id,item_id", new TestComplexKeysShardingAlgorithm());
        assertThat(ShardingRuleConfigurationConverter.toJson(getCommonShardingRuleConfig(actual)), is(getJsonForComplexStrategy()));
    }
    
    @Test
    public void assertToJsonForInlineStrategy() {
        InlineShardingStrategyConfiguration actual = new InlineShardingStrategyConfiguration("order_id", "order_${user_id % 2}");
        assertThat(ShardingRuleConfigurationConverter.toJson(getCommonShardingRuleConfig(actual)), is(getJsonForInlineStrategy()));
    }
    
    @Test
    public void assertToJsonForHintStrategy() {
        HintShardingStrategyConfiguration actual = new HintShardingStrategyConfiguration(new TestHintShardingAlgorithm());
        assertThat(ShardingRuleConfigurationConverter.toJson(getCommonShardingRuleConfig(actual)), is(getJsonForHintStrategy()));
    }
    
    @Test
    public void assertToJsonForNoneStrategy() {
        NoneShardingStrategyConfiguration actual = new NoneShardingStrategyConfiguration();
        assertThat(ShardingRuleConfigurationConverter.toJson(getCommonShardingRuleConfig(actual)), is(getJsonForNoneStrategy()));
    }
    
    private ShardingRuleConfiguration getCommonShardingRuleConfig(final ShardingStrategyConfiguration strategyConfig) {
        ShardingRuleConfiguration actual = new ShardingRuleConfiguration();
        TableRuleConfiguration orderTableRuleConfig = new TableRuleConfiguration();
        orderTableRuleConfig.setLogicTable("t_order");
        orderTableRuleConfig.setActualDataNodes("t_order_${[0, 1]}");
        actual.getTableRuleConfigs().add(orderTableRuleConfig);
        TableRuleConfiguration orderItemTableRuleConfig = new TableRuleConfiguration();
        orderItemTableRuleConfig.setLogicTable("t_order_item");
        orderItemTableRuleConfig.setActualDataNodes("t_order_item_${[0, 1]}");
        actual.getTableRuleConfigs().add(orderItemTableRuleConfig);
        actual.getBindingTableGroups().add("t_order, t_order_item");
        actual.setDefaultTableShardingStrategyConfig(strategyConfig);
        return actual;
    }
    
    @Test
    public void assertFromJsonForStandardStrategy() {
        ShardingRuleConfiguration actual = ShardingRuleConfigurationConverter.fromJson(getJsonForStandardStrategy());
        assertCommon(actual);
        StandardShardingStrategyConfiguration actualShardingStrategy = (StandardShardingStrategyConfiguration) actual.getDefaultTableShardingStrategyConfig();
        assertThat(actualShardingStrategy.getShardingColumn(), is("order_id"));
        assertThat(actualShardingStrategy.getPreciseShardingAlgorithm(), instanceOf(TestPreciseShardingAlgorithm.class));
        assertThat(actualShardingStrategy.getRangeShardingAlgorithm(), instanceOf(TestRangeShardingAlgorithm.class));
    }
    
    @Test
    public void assertFromJsonForComplexStrategy() {
        ShardingRuleConfiguration actual = ShardingRuleConfigurationConverter.fromJson(getJsonForComplexStrategy());
        assertCommon(actual);
        ComplexShardingStrategyConfiguration actualShardingStrategy = (ComplexShardingStrategyConfiguration) actual.getDefaultTableShardingStrategyConfig();
        assertThat(actualShardingStrategy.getShardingColumns(), is("order_id,item_id"));
        assertThat(actualShardingStrategy.getShardingAlgorithm(), instanceOf(TestComplexKeysShardingAlgorithm.class));
    }
    
    @Test
    public void assertFromJsonForInlineStrategy() {
        ShardingRuleConfiguration actual = ShardingRuleConfigurationConverter.fromJson(getJsonForInlineStrategy());
        assertCommon(actual);
        InlineShardingStrategyConfiguration actualShardingStrategy = (InlineShardingStrategyConfiguration) actual.getDefaultTableShardingStrategyConfig();
        assertThat(actualShardingStrategy.getShardingColumn(), is("order_id"));
        assertThat(actualShardingStrategy.getAlgorithmExpression(), is("order_${user_id % 2}"));
    }
    
    @Test
    public void assertFromJsonForHintStrategy() {
        ShardingRuleConfiguration actual = ShardingRuleConfigurationConverter.fromJson(getJsonForHintStrategy());
        assertCommon(actual);
        HintShardingStrategyConfiguration actualShardingStrategy = (HintShardingStrategyConfiguration) actual.getDefaultTableShardingStrategyConfig();
        assertThat(actualShardingStrategy.getShardingAlgorithm(), instanceOf(TestHintShardingAlgorithm.class));
    }
    
    @Test
    public void assertFromJsonForNoneStrategy() {
        ShardingRuleConfiguration actual = ShardingRuleConfigurationConverter.fromJson(getJsonForNoneStrategy());
        assertCommon(actual);
        assertThat(actual.getDefaultTableShardingStrategyConfig(), instanceOf(NoneShardingStrategyConfiguration.class));
    }
    
    @Test
    public void assertFromJsonForNullStrategy() {
        String actualJson = commonShardingRuleConfigJson + "\"defaultTableShardingStrategyConfig\":{\"type\":\"XXX\"}}";
        ShardingRuleConfiguration actual = ShardingRuleConfigurationConverter.fromJson(actualJson);
        assertCommon(actual);
        assertNull(actual.getDefaultTableShardingStrategyConfig());
    }
    
    private String getJsonForStandardStrategy() {
        return commonShardingRuleConfigJson
                + "\"defaultTableShardingStrategyConfig\":{\"type\":\"STANDARD\",\"shardingColumn\":\"order_id\","
                + "\"preciseAlgorithmClassName\":\"io.shardingjdbc.orchestration.internal.json.fixture.TestPreciseShardingAlgorithm\","
                + "\"rangeAlgorithmClassName\":\"io.shardingjdbc.orchestration.internal.json.fixture.TestRangeShardingAlgorithm\"}"
                + defaultKeyGenerator + masterSlaveRuleConfigJson;
    }
    
    private String getJsonForComplexStrategy() {
        return commonShardingRuleConfigJson + "\"defaultTableShardingStrategyConfig\":{\"type\":\"COMPLEX\",\"shardingColumns\":\"order_id,item_id\","
                + "\"algorithmClassName\":\"io.shardingjdbc.orchestration.internal.json.fixture.TestComplexKeysShardingAlgorithm\"}"
                + defaultKeyGenerator + masterSlaveRuleConfigJson;
    }
    
    private String getJsonForInlineStrategy() {
        return commonShardingRuleConfigJson + "\"defaultTableShardingStrategyConfig\":{\"type\":\"INLINE\",\"shardingColumn\":\"order_id\",\"algorithmExpression\":\"order_${user_id % 2}\"}"
                + defaultKeyGenerator + masterSlaveRuleConfigJson;
    }
    
    private String getJsonForHintStrategy() {
        return commonShardingRuleConfigJson + "\"defaultTableShardingStrategyConfig\":{\"type\":\"HINT\","
                + "\"algorithmClassName\":\"io.shardingjdbc.orchestration.internal.json.fixture.TestHintShardingAlgorithm\"}" + defaultKeyGenerator + masterSlaveRuleConfigJson;
    }
    
    private String getJsonForNoneStrategy() {
        return commonShardingRuleConfigJson + "\"defaultTableShardingStrategyConfig\":{\"type\":\"NONE\"}" + defaultKeyGenerator + masterSlaveRuleConfigJson;
    }
    
    private void assertCommon(final ShardingRuleConfiguration actual) {
        assertThat(actual.getTableRuleConfigs().size(), is(2));
        Iterator<TableRuleConfiguration> actualTableRuleConfigs = actual.getTableRuleConfigs().iterator();
        TableRuleConfiguration orderTableRuleConfig = actualTableRuleConfigs.next();
        assertThat(orderTableRuleConfig.getLogicTable(), is("t_order"));
        assertThat(orderTableRuleConfig.getActualDataNodes(), is("t_order_${[0, 1]}"));
        TableRuleConfiguration orderItemTableRuleConfig = actualTableRuleConfigs.next();
        assertThat(orderItemTableRuleConfig.getLogicTable(), is("t_order_item"));
        assertThat(orderItemTableRuleConfig.getActualDataNodes(), is("t_order_item_${[0, 1]}"));
        assertThat(actual.getBindingTableGroups().size(), is(1));
        assertThat(actual.getBindingTableGroups().iterator().next(), is("t_order, t_order_item"));
    }
}
