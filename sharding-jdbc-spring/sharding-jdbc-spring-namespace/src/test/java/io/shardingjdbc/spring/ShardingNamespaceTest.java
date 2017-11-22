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

package io.shardingjdbc.spring;

import io.shardingjdbc.core.api.ConfigMapContext;
import io.shardingjdbc.core.api.config.strategy.ComplexShardingStrategyConfiguration;
import io.shardingjdbc.core.api.config.strategy.HintShardingStrategyConfiguration;
import io.shardingjdbc.core.api.config.strategy.InlineShardingStrategyConfiguration;
import io.shardingjdbc.core.api.config.strategy.NoneShardingStrategyConfiguration;
import io.shardingjdbc.core.api.config.strategy.StandardShardingStrategyConfiguration;
import io.shardingjdbc.core.constant.ShardingProperties;
import io.shardingjdbc.core.constant.ShardingPropertiesConstant;
import io.shardingjdbc.core.jdbc.core.datasource.ShardingDataSource;
import io.shardingjdbc.core.rule.BindingTableRule;
import io.shardingjdbc.core.rule.DataNode;
import io.shardingjdbc.core.rule.ShardingRule;
import io.shardingjdbc.core.rule.TableRule;
import io.shardingjdbc.spring.datasource.SpringShardingDataSource;
import io.shardingjdbc.spring.util.FieldValueUtil;
import org.junit.Test;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

@ContextConfiguration(locations = "classpath:META-INF/rdb/shardingNamespace.xml")
public class ShardingNamespaceTest extends AbstractJUnit4SpringContextTests {
    
    @Test
    public void assertStandardStrategy() {
        StandardShardingStrategyConfiguration standardStrategy = this.applicationContext.getBean("standardStrategy", StandardShardingStrategyConfiguration.class);
        assertThat(standardStrategy.getShardingColumn(), is("user_id"));
        assertThat(standardStrategy.getPreciseAlgorithmClassName(), is("io.shardingjdbc.spring.algorithm.PreciseModuloDatabaseShardingAlgorithm"));
    }
    
    @Test
    public void assertRangeStandardStrategy() {
        StandardShardingStrategyConfiguration rangeStandardStrategy = this.applicationContext.getBean("rangeStandardStrategy", StandardShardingStrategyConfiguration.class);
        assertThat(rangeStandardStrategy.getShardingColumn(), is("order_id"));
        assertThat(rangeStandardStrategy.getPreciseAlgorithmClassName(), is("io.shardingjdbc.spring.algorithm.PreciseModuloTableShardingAlgorithm"));
        assertThat(rangeStandardStrategy.getRangeAlgorithmClassName(), is("io.shardingjdbc.spring.algorithm.RangeModuloTableShardingAlgorithm"));
    }
    
    @Test
    public void assertComplexStrategy() {
        ComplexShardingStrategyConfiguration complexStrategy = this.applicationContext.getBean("complexStrategy", ComplexShardingStrategyConfiguration.class);
        assertThat(complexStrategy.getShardingColumns(), is("order_id,user_id"));
        assertThat(complexStrategy.getAlgorithmClassName(), is("io.shardingjdbc.spring.algorithm.DefaultComplexKeysShardingAlgorithm"));
    }
    
    @Test
    public void assertInlineStrategy() {
        InlineShardingStrategyConfiguration inlineStrategy = this.applicationContext.getBean("inlineStrategy", InlineShardingStrategyConfiguration.class);
        assertThat(inlineStrategy.getShardingColumn(), is("order_id"));
        assertThat(inlineStrategy.getAlgorithmExpression(), is("t_order_${order_id % 4}"));
    }
    
    @Test
    public void assertHintStrategy() {
        HintShardingStrategyConfiguration hintStrategy = this.applicationContext.getBean("hintStrategy", HintShardingStrategyConfiguration.class);
        assertThat(hintStrategy.getAlgorithmClassName(), is("io.shardingjdbc.spring.algorithm.DefaultHintShardingAlgorithm"));
    }
    
    @Test
    public void assertNoneStrategy() {
        this.applicationContext.getBean("noneStrategy", NoneShardingStrategyConfiguration.class);
    }
    
    @Test
    public void assertSimpleShardingDataSource() {
        ShardingRule shardingRule = getShardingRule("simpleShardingDataSource");
        assertNotNull(shardingRule.getDataSourceMap().get("dbtbl_0"));
        assertThat(shardingRule.getTableRules().size(), is(1));
        assertThat(shardingRule.getTableRules().iterator().next().getLogicTable(), is("t_order"));
    }
    
    @Test
    public void assertShardingRuleWithAttributesDataSource() {
        ShardingRule shardingRule = getShardingRule("shardingRuleWithAttributesDataSource");
        assertNotNull(shardingRule.getDataSourceMap().get("dbtbl_0"));
        assertNotNull(shardingRule.getDataSourceMap().get("dbtbl_1"));
        assertThat(shardingRule.getDefaultDataSourceName(), is("dbtbl_0"));
        assertTrue(Arrays.equals(shardingRule.getDefaultDatabaseShardingStrategy().getShardingColumns().toArray(new String[]{}), 
                new String[]{this.applicationContext.getBean("standardStrategy", StandardShardingStrategyConfiguration.class).getShardingColumn()}));
        assertTrue(Arrays.equals(shardingRule.getDefaultTableShardingStrategy().getShardingColumns().toArray(new String[]{}), 
                new String[]{this.applicationContext.getBean("inlineStrategy", InlineShardingStrategyConfiguration.class).getShardingColumn()}));
        assertThat(shardingRule.getDefaultKeyGenerator().getClass().getName(), is("io.shardingjdbc.spring.fixture.IncrementKeyGenerator"));
    }
    
    @Test
    public void assertTableRuleWithAttributesDataSource() {
        ShardingRule shardingRule = getShardingRule("tableRuleWithAttributesDataSource");
        assertThat(shardingRule.getTableRules().size(), is(1));
        TableRule tableRule = shardingRule.getTableRules().iterator().next();
        assertThat(tableRule.getLogicTable(), is("t_order"));
        assertThat(tableRule.getActualDataNodes().size(), is(8));
        assertTrue(tableRule.getActualDataNodes().contains(new DataNode("dbtbl_0", "t_order_0")));
        assertTrue(tableRule.getActualDataNodes().contains(new DataNode("dbtbl_0", "t_order_1")));
        assertTrue(tableRule.getActualDataNodes().contains(new DataNode("dbtbl_0", "t_order_2")));
        assertTrue(tableRule.getActualDataNodes().contains(new DataNode("dbtbl_0", "t_order_3")));
        assertTrue(tableRule.getActualDataNodes().contains(new DataNode("dbtbl_1", "t_order_0")));
        assertTrue(tableRule.getActualDataNodes().contains(new DataNode("dbtbl_1", "t_order_1")));
        assertTrue(tableRule.getActualDataNodes().contains(new DataNode("dbtbl_1", "t_order_2")));
        assertTrue(tableRule.getActualDataNodes().contains(new DataNode("dbtbl_1", "t_order_3")));
        assertTrue(Arrays.equals(tableRule.getDatabaseShardingStrategy().getShardingColumns().toArray(new String[]{}), 
                new String[]{this.applicationContext.getBean("standardStrategy", StandardShardingStrategyConfiguration.class).getShardingColumn()}));
        assertTrue(Arrays.equals(tableRule.getTableShardingStrategy().getShardingColumns().toArray(new String[]{}), 
                new String[]{this.applicationContext.getBean("inlineStrategy", InlineShardingStrategyConfiguration.class).getShardingColumn()}));
        assertThat(tableRule.getGenerateKeyColumn(), is("order_id"));
        assertThat(tableRule.getKeyGenerator().getClass().getName(), is("io.shardingjdbc.spring.fixture.IncrementKeyGenerator"));
    }
    
    @Test
    public void assertMultiTableRulesDataSource() {
        ShardingRule shardingRule = getShardingRule("multiTableRulesDataSource");
        assertThat(shardingRule.getTableRules().size(), is(2));
        Iterator<TableRule> iter = shardingRule.getTableRules().iterator();
        assertThat(iter.next().getLogicTable(), is("t_order"));
        assertThat(iter.next().getLogicTable(), is("t_order_item"));
    }
    
    @Test
    public void assertBindingTableRuleDatasource() {
        ShardingRule shardingRule = getShardingRule("bindingTableRuleDatasource");
        assertThat(shardingRule.getBindingTableRules().size(), is(1));
        BindingTableRule bindingTableRule = shardingRule.getBindingTableRules().iterator().next();
        assertThat(bindingTableRule.getBindingActualTable("dbtbl_0", "t_order", "t_order_item"), is("t_order"));
        assertThat(bindingTableRule.getBindingActualTable("dbtbl_1", "t_order", "t_order_item"), is("t_order"));
    }
    
    @Test
    public void assertMultiBindingTableRulesDatasource() {
        ShardingRule shardingRule = getShardingRule("multiBindingTableRulesDatasource");
        assertThat(shardingRule.getBindingTableRules().size(), is(2));
        Iterator<BindingTableRule> iter = shardingRule.getBindingTableRules().iterator();
        BindingTableRule orderRule = iter.next();
        assertThat(orderRule.getBindingActualTable("dbtbl_0", "t_order", "t_order_item"), is("t_order"));
        assertThat(orderRule.getBindingActualTable("dbtbl_1", "t_order", "t_order_item"), is("t_order"));
        BindingTableRule userRule = iter.next();
        assertThat(userRule.getBindingActualTable("dbtbl_0", "t_user", "t_user_detail"), is("t_user"));
        assertThat(userRule.getBindingActualTable("dbtbl_1", "t_user", "t_user_detail"), is("t_user"));
    }
    
    @Test
    public void assertPropsDataSource() {
        ShardingDataSource shardingDataSource = this.applicationContext.getBean("propsDataSource", ShardingDataSource.class);
        Map<String, Object> configMap = new HashMap<>();
        configMap.put("key1", "value1");
        assertThat(ConfigMapContext.getInstance().getShardingConfig(), is(configMap));
        Object shardingContext = FieldValueUtil.getFieldValue(shardingDataSource, "shardingContext", true);
        assertTrue((boolean) FieldValueUtil.getFieldValue(shardingContext, "showSQL"));
        ShardingProperties shardingProperties = (ShardingProperties) FieldValueUtil.getFieldValue(shardingDataSource, "shardingProperties", true);
        boolean showSql = shardingProperties.getValue(ShardingPropertiesConstant.SQL_SHOW);
        assertTrue(showSql);
        int executorSize = shardingProperties.getValue(ShardingPropertiesConstant.EXECUTOR_SIZE);
        assertThat(executorSize, is(10));
        assertNull(ShardingPropertiesConstant.findByKey("foo"));
    }
    
    @Test
    public void assertShardingDataSourceType() {
        assertTrue(this.applicationContext.getBean("simpleShardingDataSource", ShardingDataSource.class) instanceof SpringShardingDataSource);
    }
    
    @Test
    public void assertDefaultActualDataNodes() {
        ShardingDataSource multiTableRulesDataSource = this.applicationContext.getBean("multiTableRulesDataSource", ShardingDataSource.class);
        Object shardingContext = FieldValueUtil.getFieldValue(multiTableRulesDataSource, "shardingContext", true);
        ShardingRule shardingRule = (ShardingRule) FieldValueUtil.getFieldValue(shardingContext, "shardingRule");
        assertThat(shardingRule.getTableRules().size(), is(2));
        Iterator<TableRule> iter = shardingRule.getTableRules().iterator();
        TableRule orderRule = iter.next();
        assertThat(orderRule.getActualDataNodes().size(), is(2));
        assertTrue(orderRule.getActualDataNodes().contains(new DataNode("dbtbl_0", "t_order")));
        assertTrue(orderRule.getActualDataNodes().contains(new DataNode("dbtbl_1", "t_order")));
        TableRule orderItemRule = iter.next();
        assertThat(orderItemRule.getActualDataNodes().size(), is(2));
        assertTrue(orderItemRule.getActualDataNodes().contains(new DataNode("dbtbl_0", "t_order_item")));
        assertTrue(orderItemRule.getActualDataNodes().contains(new DataNode("dbtbl_1", "t_order_item")));
    }
    
    private ShardingRule getShardingRule(final String shardingDataSourceName) {
        ShardingDataSource shardingDataSource = this.applicationContext.getBean(shardingDataSourceName, ShardingDataSource.class);
        Object shardingContext = FieldValueUtil.getFieldValue(shardingDataSource, "shardingContext", true);
        return (ShardingRule) FieldValueUtil.getFieldValue(shardingContext, "shardingRule");
    }
}
