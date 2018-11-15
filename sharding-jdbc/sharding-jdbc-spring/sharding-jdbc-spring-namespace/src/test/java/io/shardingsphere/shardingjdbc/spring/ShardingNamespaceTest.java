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

package io.shardingsphere.shardingjdbc.spring;

import io.shardingsphere.api.ConfigMapContext;
import io.shardingsphere.api.algorithm.masterslave.RandomMasterSlaveLoadBalanceAlgorithm;
import io.shardingsphere.api.algorithm.masterslave.RoundRobinMasterSlaveLoadBalanceAlgorithm;
import io.shardingsphere.api.config.strategy.ComplexShardingStrategyConfiguration;
import io.shardingsphere.api.config.strategy.HintShardingStrategyConfiguration;
import io.shardingsphere.api.config.strategy.InlineShardingStrategyConfiguration;
import io.shardingsphere.api.config.strategy.NoneShardingStrategyConfiguration;
import io.shardingsphere.api.config.strategy.StandardShardingStrategyConfiguration;
import io.shardingsphere.core.constant.properties.ShardingProperties;
import io.shardingsphere.core.constant.properties.ShardingPropertiesConstant;
import io.shardingsphere.core.rule.BindingTableRule;
import io.shardingsphere.core.rule.DataNode;
import io.shardingsphere.core.rule.ShardingRule;
import io.shardingsphere.core.rule.TableRule;
import io.shardingsphere.shardingjdbc.jdbc.core.ShardingContext;
import io.shardingsphere.shardingjdbc.jdbc.core.datasource.ShardingDataSource;
import io.shardingsphere.shardingjdbc.spring.algorithm.DefaultComplexKeysShardingAlgorithm;
import io.shardingsphere.shardingjdbc.spring.algorithm.DefaultHintShardingAlgorithm;
import io.shardingsphere.shardingjdbc.spring.algorithm.PreciseModuloDatabaseShardingAlgorithm;
import io.shardingsphere.shardingjdbc.spring.algorithm.PreciseModuloTableShardingAlgorithm;
import io.shardingsphere.shardingjdbc.spring.algorithm.RangeModuloTableShardingAlgorithm;
import io.shardingsphere.shardingjdbc.spring.datasource.SpringShardingDataSource;
import io.shardingsphere.shardingjdbc.spring.fixture.IncrementKeyGenerator;
import io.shardingsphere.shardingjdbc.spring.util.FieldValueUtil;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;

import javax.sql.DataSource;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

@ContextConfiguration(locations = "classpath:META-INF/rdb/shardingNamespace.xml")
public class ShardingNamespaceTest extends AbstractJUnit4SpringContextTests {
    
    @BeforeClass
    public static void setUp() {
        ConfigMapContext.getInstance().getConfigMap().clear();
    }
    
    @Test
    public void assertStandardStrategy() {
        StandardShardingStrategyConfiguration standardStrategy = applicationContext.getBean("standardStrategy", StandardShardingStrategyConfiguration.class);
        assertThat(standardStrategy.getShardingColumn(), is("user_id"));
        assertThat(standardStrategy.getPreciseShardingAlgorithm(), instanceOf(PreciseModuloDatabaseShardingAlgorithm.class));
    }
    
    @Test
    public void assertRangeStandardStrategy() {
        StandardShardingStrategyConfiguration rangeStandardStrategy = applicationContext.getBean("rangeStandardStrategy", StandardShardingStrategyConfiguration.class);
        assertThat(rangeStandardStrategy.getShardingColumn(), is("order_id"));
        assertThat(rangeStandardStrategy.getPreciseShardingAlgorithm(), instanceOf(PreciseModuloTableShardingAlgorithm.class));
        assertThat(rangeStandardStrategy.getRangeShardingAlgorithm(), instanceOf(RangeModuloTableShardingAlgorithm.class));
    }
    
    @Test
    public void assertComplexStrategy() {
        ComplexShardingStrategyConfiguration complexStrategy = applicationContext.getBean("complexStrategy", ComplexShardingStrategyConfiguration.class);
        assertThat(complexStrategy.getShardingColumns(), is("order_id,user_id"));
        assertThat(complexStrategy.getShardingAlgorithm(), instanceOf(DefaultComplexKeysShardingAlgorithm.class));
    }
    
    @Test
    public void assertInlineStrategy() {
        InlineShardingStrategyConfiguration inlineStrategy = applicationContext.getBean("inlineStrategy", InlineShardingStrategyConfiguration.class);
        assertThat(inlineStrategy.getShardingColumn(), is("order_id"));
        assertThat(inlineStrategy.getAlgorithmExpression(), is("t_order_${order_id % 4}"));
    }
    
    @Test
    public void assertHintStrategy() {
        HintShardingStrategyConfiguration hintStrategy = applicationContext.getBean("hintStrategy", HintShardingStrategyConfiguration.class);
        assertThat(hintStrategy.getShardingAlgorithm(), instanceOf(DefaultHintShardingAlgorithm.class));
    }
    
    @Test
    public void assertNoneStrategy() {
        applicationContext.getBean("noneStrategy", NoneShardingStrategyConfiguration.class);
    }
    
    @Test
    public void assertSimpleShardingDataSource() {
        Map<String, DataSource> dataSourceMap = getDataSourceMap("simpleShardingDataSource");
        assertNotNull(dataSourceMap.get("dbtbl_0"));
        ShardingRule shardingRule = getShardingRule("simpleShardingDataSource");
        assertThat(shardingRule.getTableRules().size(), is(1));
        assertThat(shardingRule.getTableRules().iterator().next().getLogicTable(), is("t_order"));
    }
    
    @Test
    public void assertMasterSlaveShardingDataSourceByDefaultStrategy() {
        Map<String, DataSource> dataSourceMap = getDataSourceMap("masterSlaveShardingDataSourceByDefaultStrategy");
        assertNotNull(dataSourceMap.get("dbtbl_0_master"));
        assertNotNull(dataSourceMap.get("dbtbl_0_slave_0"));
        assertNotNull(dataSourceMap.get("dbtbl_1_master"));
        assertNotNull(dataSourceMap.get("dbtbl_1_slave_1"));
        ShardingRule shardingRule = getShardingRule("masterSlaveShardingDataSourceByDefaultStrategy");
        assertThat(shardingRule.getMasterSlaveRules().iterator().next().getLoadBalanceAlgorithm(), instanceOf(RoundRobinMasterSlaveLoadBalanceAlgorithm.class));
        assertThat(shardingRule.getTableRules().size(), is(1));
        assertThat(shardingRule.getTableRules().iterator().next().getLogicTable(), is("t_order"));
        assertThat(shardingRule.getDefaultKeyGenerator(), instanceOf(IncrementKeyGenerator.class));
    }
    
    @Test
    public void assertMasterSlaveShardingDataSourceByUserStrategy() {
        Map<String, DataSource> dataSourceMap = getDataSourceMap("masterSlaveShardingDataSourceByUserStrategy");
        assertNotNull(dataSourceMap.get("dbtbl_0_master"));
        assertNotNull(dataSourceMap.get("dbtbl_0_slave_0"));
        assertNotNull(dataSourceMap.get("dbtbl_1_master"));
        assertNotNull(dataSourceMap.get("dbtbl_1_slave_1"));
        ShardingRule shardingRule = getShardingRule("masterSlaveShardingDataSourceByUserStrategy");
        assertThat(shardingRule.getMasterSlaveRules().iterator().next().getLoadBalanceAlgorithm(), instanceOf(RandomMasterSlaveLoadBalanceAlgorithm.class));
        assertThat(shardingRule.getTableRules().size(), is(1));
        assertThat(shardingRule.getTableRules().iterator().next().getLogicTable(), is("t_order"));
        assertThat(shardingRule.getDefaultKeyGenerator(), instanceOf(IncrementKeyGenerator.class));
    }
    
    @Test
    public void assertShardingRuleWithAttributesDataSource() {
        Map<String, DataSource> dataSourceMap = getDataSourceMap("shardingRuleWithAttributesDataSource");
        assertNotNull(dataSourceMap.get("dbtbl_0"));
        assertNotNull(dataSourceMap.get("dbtbl_1"));
        ShardingRule shardingRule = getShardingRule("shardingRuleWithAttributesDataSource");
        assertThat(shardingRule.getShardingDataSourceNames().getDefaultDataSourceName(), is("dbtbl_0"));
        assertTrue(Arrays.equals(shardingRule.getDefaultDatabaseShardingStrategy().getShardingColumns().toArray(new String[]{}), 
                new String[]{applicationContext.getBean("standardStrategy", StandardShardingStrategyConfiguration.class).getShardingColumn()}));
        assertTrue(Arrays.equals(shardingRule.getDefaultTableShardingStrategy().getShardingColumns().toArray(new String[]{}), 
                new String[]{applicationContext.getBean("inlineStrategy", InlineShardingStrategyConfiguration.class).getShardingColumn()}));
        assertThat(shardingRule.getDefaultKeyGenerator(), instanceOf(IncrementKeyGenerator.class));
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
                new String[]{applicationContext.getBean("standardStrategy", StandardShardingStrategyConfiguration.class).getShardingColumn()}));
        assertTrue(Arrays.equals(tableRule.getTableShardingStrategy().getShardingColumns().toArray(new String[]{}), 
                new String[]{applicationContext.getBean("inlineStrategy", InlineShardingStrategyConfiguration.class).getShardingColumn()}));
        assertThat(tableRule.getGenerateKeyColumn(), is("order_id"));
        assertThat(tableRule.getKeyGenerator(), instanceOf(IncrementKeyGenerator.class));
    }
    
    @Test
    public void assertMultiTableRulesDataSource() {
        ShardingRule shardingRule = getShardingRule("multiTableRulesDataSource");
        assertThat(shardingRule.getTableRules().size(), is(2));
        Iterator<TableRule> tableRules = shardingRule.getTableRules().iterator();
        assertThat(tableRules.next().getLogicTable(), is("t_order"));
        assertThat(tableRules.next().getLogicTable(), is("t_order_item"));
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
        Iterator<BindingTableRule> bindingTableRules = shardingRule.getBindingTableRules().iterator();
        BindingTableRule orderRule = bindingTableRules.next();
        assertThat(orderRule.getBindingActualTable("dbtbl_0", "t_order", "t_order_item"), is("t_order"));
        assertThat(orderRule.getBindingActualTable("dbtbl_1", "t_order", "t_order_item"), is("t_order"));
        BindingTableRule userRule = bindingTableRules.next();
        assertThat(userRule.getBindingActualTable("dbtbl_0", "t_user", "t_user_detail"), is("t_user"));
        assertThat(userRule.getBindingActualTable("dbtbl_1", "t_user", "t_user_detail"), is("t_user"));
    }
    
    @Test
    public void assertPropsDataSource() {
        ShardingDataSource shardingDataSource = applicationContext.getBean("propsDataSource", ShardingDataSource.class);
        Map<String, Object> configMap = new HashMap<>();
        configMap.put("key1", "value1");
        assertThat(ConfigMapContext.getInstance().getConfigMap(), is(configMap));
        ShardingContext shardingContext = (ShardingContext) FieldValueUtil.getFieldValue(shardingDataSource, "shardingContext", true);
        assertTrue(shardingContext.getShardingProperties().<Boolean>getValue(ShardingPropertiesConstant.SQL_SHOW));
        ShardingProperties shardingProperties = shardingContext.getShardingProperties();
        boolean showSql = shardingProperties.getValue(ShardingPropertiesConstant.SQL_SHOW);
        assertTrue(showSql);
        int executorSize = shardingProperties.getValue(ShardingPropertiesConstant.EXECUTOR_SIZE);
        assertThat(executorSize, is(10));
        assertNull(ShardingPropertiesConstant.findByKey("foo"));
    }
    
    @Test
    public void assertShardingDataSourceType() {
        assertTrue(applicationContext.getBean("simpleShardingDataSource", ShardingDataSource.class) instanceof SpringShardingDataSource);
    }
    
    @Test
    public void assertDefaultActualDataNodes() {
        ShardingDataSource multiTableRulesDataSource = applicationContext.getBean("multiTableRulesDataSource", ShardingDataSource.class);
        Object shardingContext = FieldValueUtil.getFieldValue(multiTableRulesDataSource, "shardingContext", true);
        ShardingRule shardingRule = (ShardingRule) FieldValueUtil.getFieldValue(shardingContext, "shardingRule");
        assertThat(shardingRule.getTableRules().size(), is(2));
        Iterator<TableRule> tableRules = shardingRule.getTableRules().iterator();
        TableRule orderRule = tableRules.next();
        assertThat(orderRule.getActualDataNodes().size(), is(2));
        assertTrue(orderRule.getActualDataNodes().contains(new DataNode("dbtbl_0", "t_order")));
        assertTrue(orderRule.getActualDataNodes().contains(new DataNode("dbtbl_1", "t_order")));
        TableRule orderItemRule = tableRules.next();
        assertThat(orderItemRule.getActualDataNodes().size(), is(2));
        assertTrue(orderItemRule.getActualDataNodes().contains(new DataNode("dbtbl_0", "t_order_item")));
        assertTrue(orderItemRule.getActualDataNodes().contains(new DataNode("dbtbl_1", "t_order_item")));
    }
    
    @SuppressWarnings("unchecked")
    private Map<String, DataSource> getDataSourceMap(final String shardingDataSourceName) {
        ShardingDataSource shardingDataSource = applicationContext.getBean(shardingDataSourceName, ShardingDataSource.class);
        return shardingDataSource.getDataSourceMap();
    }
    
    private ShardingRule getShardingRule(final String shardingDataSourceName) {
        ShardingDataSource shardingDataSource = applicationContext.getBean(shardingDataSourceName, ShardingDataSource.class);
        Object shardingContext = FieldValueUtil.getFieldValue(shardingDataSource, "shardingContext", true);
        return (ShardingRule) FieldValueUtil.getFieldValue(shardingContext, "shardingRule");
    }
}
