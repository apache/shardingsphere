package com.dangdang.ddframe.rdb.sharding.json;

import com.dangdang.ddframe.rdb.sharding.api.config.ShardingRuleConfig;
import com.dangdang.ddframe.rdb.sharding.api.config.TableRuleConfig;
import com.dangdang.ddframe.rdb.sharding.api.config.strategy.ComplexShardingStrategyConfig;
import com.dangdang.ddframe.rdb.sharding.api.config.strategy.HintShardingStrategyConfig;
import com.dangdang.ddframe.rdb.sharding.api.config.strategy.InlineShardingStrategyConfig;
import com.dangdang.ddframe.rdb.sharding.api.config.strategy.NoneShardingStrategyConfig;
import com.dangdang.ddframe.rdb.sharding.api.config.strategy.ShardingStrategyConfig;
import com.dangdang.ddframe.rdb.sharding.api.config.strategy.StandardShardingStrategyConfig;
import org.junit.Test;

import java.util.Iterator;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

public final class ShardingRuleConfigConverterTest {
    
    private final String commonShardingRuleConfigJson = "{\"tableRuleConfigs\":[{\"logicTable\":\"t_order\",\"dynamic\":false,\"actualTables\":\"t_order_${[0, 1]}\","
            + "\"databaseShardingStrategyConfig\":{},\"tableShardingStrategyConfig\":{}},"
            + "{\"logicTable\":\"t_order_item\",\"dynamic\":false,\"actualTables\":\"t_order_item_${[0, 1]}\","
            + "\"databaseShardingStrategyConfig\":{},\"tableShardingStrategyConfig\":{}}"
            + "],\"bindingTableGroups\":[\"t_order, t_order_item\"],\"defaultDatabaseShardingStrategyConfig\":{},";
    
    private final String masterSlaveRuleConfigJson = ",\"masterSlaveRuleConfigs\":[]}";
    
    @Test
    public void assertToJsonForStandardStrategy() {
        StandardShardingStrategyConfig actual = new StandardShardingStrategyConfig();
        actual.setShardingColumn("order_id");
        actual.setPreciseAlgorithmClassName("xxx.XXXPreciseAlgorithm");
        actual.setRangeAlgorithmClassName("xxx.XXXRangeAlgorithm");
        assertThat(ShardingRuleConfigConverter.toJson(getCommonShardingRuleConfig(actual)), is(getJsonForStandardStrategy()));
    }
    
    @Test
    public void assertToJsonForComplexStrategy() {
        ComplexShardingStrategyConfig actual = new ComplexShardingStrategyConfig();
        actual.setShardingColumns("order_id,item_id");
        actual.setAlgorithmClassName("xxx.XXXAlgorithm");
        assertThat(ShardingRuleConfigConverter.toJson(getCommonShardingRuleConfig(actual)), is(getJsonForComplexStrategy()));
    }
    
    @Test
    public void assertToJsonForInlineStrategy() {
        InlineShardingStrategyConfig actual = new InlineShardingStrategyConfig();
        actual.setShardingColumn("order_id");
        actual.setAlgorithmInlineExpression("order_${user_id % 2}");
        assertThat(ShardingRuleConfigConverter.toJson(getCommonShardingRuleConfig(actual)), is(getJsonForInlineStrategy()));
    }
    
    @Test
    public void assertToJsonForHintStrategy() {
        HintShardingStrategyConfig actual = new HintShardingStrategyConfig();
        actual.setAlgorithmClassName("xxx.XXXAlgorithm");
        assertThat(ShardingRuleConfigConverter.toJson(getCommonShardingRuleConfig(actual)), is(getJsonForHintStrategy()));
    }
    
    @Test
    public void assertToJsonForNoneStrategy() {
        NoneShardingStrategyConfig actual = new NoneShardingStrategyConfig();
        assertThat(ShardingRuleConfigConverter.toJson(getCommonShardingRuleConfig(actual)), is(getJsonForNoneStrategy()));
    }
    
    private ShardingRuleConfig getCommonShardingRuleConfig(final ShardingStrategyConfig strategyConfig) {
        ShardingRuleConfig actual = new ShardingRuleConfig();
        TableRuleConfig orderTableRuleConfig = new TableRuleConfig();
        orderTableRuleConfig.setLogicTable("t_order");
        orderTableRuleConfig.setActualTables("t_order_${[0, 1]}");
        actual.getTableRuleConfigs().add(orderTableRuleConfig);
        TableRuleConfig orderItemTableRuleConfig = new TableRuleConfig();
        orderItemTableRuleConfig.setLogicTable("t_order_item");
        orderItemTableRuleConfig.setActualTables("t_order_item_${[0, 1]}");
        actual.getTableRuleConfigs().add(orderItemTableRuleConfig);
        actual.getBindingTableGroups().add("t_order, t_order_item");
        actual.setDefaultTableShardingStrategyConfig(strategyConfig);
        return actual;
    }
    
    @Test
    public void assertFromJsonForStandardStrategy() {
        ShardingRuleConfig actual = ShardingRuleConfigConverter.fromJson(getJsonForStandardStrategy());
        assertCommon(actual);
        StandardShardingStrategyConfig actualShardingStrategy = (StandardShardingStrategyConfig) actual.getDefaultTableShardingStrategyConfig();
        assertThat(actualShardingStrategy.getShardingColumn(), is("order_id"));
        assertThat(actualShardingStrategy.getPreciseAlgorithmClassName(), is("xxx.XXXPreciseAlgorithm"));
        assertThat(actualShardingStrategy.getRangeAlgorithmClassName(), is("xxx.XXXRangeAlgorithm"));
    }
    
    @Test
    public void assertFromJsonForComplexStrategy() {
        ShardingRuleConfig actual = ShardingRuleConfigConverter.fromJson(getJsonForComplexStrategy());
        assertCommon(actual);
        ComplexShardingStrategyConfig actualShardingStrategy = (ComplexShardingStrategyConfig) actual.getDefaultTableShardingStrategyConfig();
        assertThat(actualShardingStrategy.getShardingColumns(), is("order_id,item_id"));
        assertThat(actualShardingStrategy.getAlgorithmClassName(), is("xxx.XXXAlgorithm"));
    }
    
    @Test
    public void assertFromJsonForInlineStrategy() {
        ShardingRuleConfig actual = ShardingRuleConfigConverter.fromJson(getJsonForInlineStrategy());
        assertCommon(actual);
        InlineShardingStrategyConfig actualShardingStrategy = (InlineShardingStrategyConfig) actual.getDefaultTableShardingStrategyConfig();
        assertThat(actualShardingStrategy.getShardingColumn(), is("order_id"));
        assertThat(actualShardingStrategy.getAlgorithmInlineExpression(), is("order_${user_id % 2}"));
    }
    
    @Test
    public void assertFromJsonForHintStrategy() {
        ShardingRuleConfig actual = ShardingRuleConfigConverter.fromJson(getJsonForHintStrategy());
        assertCommon(actual);
        HintShardingStrategyConfig actualShardingStrategy = (HintShardingStrategyConfig) actual.getDefaultTableShardingStrategyConfig();
        assertThat(actualShardingStrategy.getAlgorithmClassName(), is("xxx.XXXAlgorithm"));
    }
    
    @Test
    public void assertFromJsonForNoneStrategy() {
        ShardingRuleConfig actual = ShardingRuleConfigConverter.fromJson(getJsonForNoneStrategy());
        assertCommon(actual);
        assertThat(actual.getDefaultTableShardingStrategyConfig(), instanceOf(NoneShardingStrategyConfig.class));
    }
    
    @Test
    public void assertFromJsonForNullStrategy() {
        String actualJson = commonShardingRuleConfigJson + "\"defaultTableShardingStrategyConfig\":{\"type\":\"XXX\"}}";
        ShardingRuleConfig actual = ShardingRuleConfigConverter.fromJson(actualJson);
        assertCommon(actual);
        assertNull(actual.getDefaultTableShardingStrategyConfig());
    }
    
    private String getJsonForStandardStrategy() {
        return commonShardingRuleConfigJson
                + "\"defaultTableShardingStrategyConfig\":{\"type\":\"STANDARD\",\"shardingColumn\":\"order_id\","
                + "\"preciseAlgorithmClassName\":\"xxx.XXXPreciseAlgorithm\",\"rangeAlgorithmClassName\":\"xxx.XXXRangeAlgorithm\"}"
                + masterSlaveRuleConfigJson;
    }
    
    private String getJsonForComplexStrategy() {
        return commonShardingRuleConfigJson + "\"defaultTableShardingStrategyConfig\":{\"type\":\"COMPLEX\",\"shardingColumns\":\"order_id,item_id\",\"algorithmClassName\":\"xxx.XXXAlgorithm\"}"
                + masterSlaveRuleConfigJson;
    }
    
    private String getJsonForInlineStrategy() {
        return commonShardingRuleConfigJson + "\"defaultTableShardingStrategyConfig\":{\"type\":\"INLINE\",\"shardingColumn\":\"order_id\",\"algorithmInlineExpression\":\"order_${user_id % 2}\"}"
                + masterSlaveRuleConfigJson;
    }
    
    private String getJsonForHintStrategy() {
        return commonShardingRuleConfigJson + "\"defaultTableShardingStrategyConfig\":{\"type\":\"HINT\",\"algorithmClassName\":\"xxx.XXXAlgorithm\"}" + masterSlaveRuleConfigJson;
    }
    
    private String getJsonForNoneStrategy() {
        return commonShardingRuleConfigJson + "\"defaultTableShardingStrategyConfig\":{\"type\":\"NONE\"}" + masterSlaveRuleConfigJson;
    }
    
    private void assertCommon(final ShardingRuleConfig actual) {
        assertThat(actual.getTableRuleConfigs().size(), is(2));
        Iterator<TableRuleConfig> actualTableRuleConfigs = actual.getTableRuleConfigs().iterator();
        TableRuleConfig orderTableRuleConfig = actualTableRuleConfigs.next();
        assertThat(orderTableRuleConfig.getLogicTable(), is("t_order"));
        assertThat(orderTableRuleConfig.getActualTables(), is("t_order_${[0, 1]}"));
        TableRuleConfig orderItemTableRuleConfig = actualTableRuleConfigs.next();
        assertThat(orderItemTableRuleConfig.getLogicTable(), is("t_order_item"));
        assertThat(orderItemTableRuleConfig.getActualTables(), is("t_order_item_${[0, 1]}"));
        assertThat(actual.getBindingTableGroups().size(), is(1));
        assertThat(actual.getBindingTableGroups().iterator().next(), is("t_order, t_order_item"));
    }
}
