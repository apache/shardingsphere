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

package io.shardingsphere.shardingjdbc.orchestration.spring;

import io.shardingsphere.api.ConfigMapContext;
import io.shardingsphere.api.config.strategy.InlineShardingStrategyConfiguration;
import io.shardingsphere.api.config.strategy.StandardShardingStrategyConfiguration;
import io.shardingsphere.core.constant.properties.ShardingProperties;
import io.shardingsphere.core.constant.properties.ShardingPropertiesConstant;
import io.shardingsphere.core.rule.BindingTableRule;
import io.shardingsphere.core.rule.DataNode;
import io.shardingsphere.core.rule.ShardingRule;
import io.shardingsphere.core.rule.TableRule;
import io.shardingsphere.shardingjdbc.jdbc.core.ShardingContext;
import io.shardingsphere.shardingjdbc.jdbc.core.datasource.ShardingDataSource;
import io.shardingsphere.shardingjdbc.orchestration.spring.datasource.OrchestrationSpringShardingDataSource;
import io.shardingsphere.shardingjdbc.orchestration.spring.fixture.IncrementKeyGenerator;
import io.shardingsphere.shardingjdbc.orchestration.spring.util.EmbedTestingServer;
import io.shardingsphere.shardingjdbc.orchestration.spring.util.FieldValueUtil;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;

import javax.sql.DataSource;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

@ContextConfiguration(locations = "classpath:META-INF/rdb/shardingOrchestration.xml")
public class OrchestrationShardingNamespaceTest extends AbstractJUnit4SpringContextTests {
    
    @BeforeClass
    public static void init() {
        EmbedTestingServer.start();
        ConfigMapContext.getInstance().getConfigMap().clear();
    }
    
    @Test
    public void assertSimpleShardingDataSource() {
        Map<String, DataSource> dataSourceMap = getDataSourceMap("simpleShardingOrchestration");
        ShardingRule shardingRule = getShardingRule("simpleShardingOrchestration");
        assertNotNull(dataSourceMap.get("dbtbl_0"));
        assertThat(shardingRule.getTableRules().size(), is(1));
        assertThat(shardingRule.getTableRules().iterator().next().getLogicTable(), is("t_order"));
    }
    
    @Test
    public void assertShardingRuleWithAttributesDataSource() {
        Map<String, DataSource> dataSourceMap = getDataSourceMap("shardingRuleWithAttributesDataSourceOrchestration");
        ShardingRule shardingRule = getShardingRule("shardingRuleWithAttributesDataSourceOrchestration");
        assertNotNull(dataSourceMap.get("dbtbl_0"));
        assertNotNull(dataSourceMap.get("dbtbl_1"));
        assertThat(shardingRule.getShardingDataSourceNames().getDefaultDataSourceName(), is("dbtbl_0"));
        assertTrue(Arrays.equals(shardingRule.getDefaultDatabaseShardingStrategy().getShardingColumns().toArray(new String[]{}),
                new String[]{applicationContext.getBean("standardStrategy", StandardShardingStrategyConfiguration.class).getShardingColumn()}));
        assertTrue(Arrays.equals(shardingRule.getDefaultTableShardingStrategy().getShardingColumns().toArray(new String[]{}),
                new String[]{applicationContext.getBean("inlineStrategy", InlineShardingStrategyConfiguration.class).getShardingColumn()}));
        assertThat(shardingRule.getDefaultKeyGenerator().getClass().getName(), is(IncrementKeyGenerator.class.getCanonicalName()));
    }
    
    @Test
    public void assertTableRuleWithAttributesDataSource() {
        ShardingRule shardingRule = getShardingRule("tableRuleWithAttributesDataSourceOrchestration");
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
        assertThat(tableRule.getKeyGenerator().getClass().getName(), is(IncrementKeyGenerator.class.getCanonicalName()));
    }
    
    @Test
    public void assertMultiTableRulesDataSource() {
        ShardingRule shardingRule = getShardingRule("multiTableRulesDataSourceOrchestration");
        assertThat(shardingRule.getTableRules().size(), is(2));
        Iterator<TableRule> tableRules = shardingRule.getTableRules().iterator();
        assertThat(tableRules.next().getLogicTable(), is("t_order"));
        assertThat(tableRules.next().getLogicTable(), is("t_order_item"));
    }
    
    @Test
    public void assertBindingTableRuleDatasource() {
        ShardingRule shardingRule = getShardingRule("bindingTableRuleDatasourceOrchestration");
        assertThat(shardingRule.getBindingTableRules().size(), is(1));
        BindingTableRule bindingTableRule = shardingRule.getBindingTableRules().iterator().next();
        assertThat(bindingTableRule.getBindingActualTable("dbtbl_0", "t_order", "t_order_item"), is("t_order"));
        assertThat(bindingTableRule.getBindingActualTable("dbtbl_1", "t_order", "t_order_item"), is("t_order"));
    }

    @Test
    public void assertMultiBindingTableRulesDatasource() {
        ShardingRule shardingRule = getShardingRule("multiBindingTableRulesDatasourceOrchestration");
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
        OrchestrationSpringShardingDataSource shardingDataSource = applicationContext.getBean("propsDataSourceOrchestration", OrchestrationSpringShardingDataSource.class);
        ShardingDataSource dataSource = (ShardingDataSource) FieldValueUtil.getFieldValue(shardingDataSource, "dataSource", true);
        Map<String, Object> configMap = new HashMap<>();
        configMap.put("key1", "value1");
        assertThat(ConfigMapContext.getInstance().getConfigMap(), is(configMap));
        ShardingContext shardingContext = (ShardingContext) FieldValueUtil.getFieldValue(dataSource, "shardingContext", false);
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
        assertTrue(applicationContext.getBean("simpleShardingOrchestration") instanceof OrchestrationSpringShardingDataSource);
    }
    
    @Test
    public void assertDefaultActualDataNodes() {
        OrchestrationSpringShardingDataSource multiTableRulesDataSource = applicationContext.getBean("multiTableRulesDataSourceOrchestration", OrchestrationSpringShardingDataSource.class);
        ShardingDataSource dataSource = (ShardingDataSource) FieldValueUtil.getFieldValue(multiTableRulesDataSource, "dataSource", true);
        Object shardingContext = FieldValueUtil.getFieldValue(dataSource, "shardingContext", false);
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
        OrchestrationSpringShardingDataSource shardingDataSource = applicationContext.getBean(shardingDataSourceName, OrchestrationSpringShardingDataSource.class);
        ShardingDataSource dataSource = (ShardingDataSource) FieldValueUtil.getFieldValue(shardingDataSource, "dataSource", true);
        return dataSource.getDataSourceMap();
    }
    
    private ShardingRule getShardingRule(final String shardingDataSourceName) {
        OrchestrationSpringShardingDataSource shardingDataSource = applicationContext.getBean(shardingDataSourceName, OrchestrationSpringShardingDataSource.class);
        ShardingDataSource dataSource = (ShardingDataSource) FieldValueUtil.getFieldValue(shardingDataSource, "dataSource", true);
        Object shardingContext = FieldValueUtil.getFieldValue(dataSource, "shardingContext", false);
        return (ShardingRule) FieldValueUtil.getFieldValue(shardingContext, "shardingRule");
    }
}
