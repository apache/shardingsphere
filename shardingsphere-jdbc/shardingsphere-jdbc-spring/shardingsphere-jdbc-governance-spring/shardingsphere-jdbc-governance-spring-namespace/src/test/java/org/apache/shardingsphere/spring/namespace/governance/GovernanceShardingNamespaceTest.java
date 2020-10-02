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

package org.apache.shardingsphere.spring.namespace.governance;

import org.apache.shardingsphere.driver.governance.internal.datasource.GovernanceShardingSphereDataSource;
import org.apache.shardingsphere.infra.config.properties.ConfigurationPropertyKey;
import org.apache.shardingsphere.infra.context.schema.SchemaContexts;
import org.apache.shardingsphere.infra.datanode.DataNode;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.StandardShardingStrategyConfiguration;
import org.apache.shardingsphere.sharding.rule.BindingTableRule;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.apache.shardingsphere.sharding.rule.TableRule;
import org.apache.shardingsphere.spring.namespace.governance.fixture.IncrementKeyGenerateAlgorithm;
import org.apache.shardingsphere.spring.namespace.governance.util.EmbedTestingServer;
import org.apache.shardingsphere.spring.namespace.governance.util.FieldValueUtil;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;

import javax.sql.DataSource;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

@ContextConfiguration(locations = "classpath:META-INF/rdb/sharding-governance.xml")
public class GovernanceShardingNamespaceTest extends AbstractJUnit4SpringContextTests {
    
    @BeforeClass
    public static void init() {
        EmbedTestingServer.start();
    }
    
    @Test
    public void assertSimpleShardingSphereDataSource() {
        Map<String, DataSource> dataSourceMap = getDataSourceMap("simpleShardingGovernance");
        ShardingRule shardingRule = getShardingRule("simpleShardingGovernance");
        assertNotNull(dataSourceMap.get("dbtbl_0"));
        assertThat(shardingRule.getTableRules().size(), is(1));
        assertThat(shardingRule.getTableRules().iterator().next().getLogicTable(), is("t_order"));
    }
    
    @Test
    public void assertShardingRuleWithAttributesDataSource() {
        Map<String, DataSource> dataSourceMap = getDataSourceMap("shardingRuleWithAttributesDataSourceGovernance");
        ShardingRule shardingRule = getShardingRule("shardingRuleWithAttributesDataSourceGovernance");
        assertNotNull(dataSourceMap.get("dbtbl_0"));
        assertNotNull(dataSourceMap.get("dbtbl_1"));
        assertThat(shardingRule.getDefaultDatabaseShardingStrategy().getShardingColumns().toArray(new String[]{}), 
                is(new String[]{applicationContext.getBean("standardStrategy", StandardShardingStrategyConfiguration.class).getShardingColumn()}));
        assertThat(shardingRule.getDefaultTableShardingStrategy().getShardingColumns().toArray(new String[]{}),
                is(new String[]{applicationContext.getBean("inlineStrategy", StandardShardingStrategyConfiguration.class).getShardingColumn()}));
        assertThat(shardingRule.getDefaultKeyGenerateAlgorithm().getClass().getName(), is(IncrementKeyGenerateAlgorithm.class.getCanonicalName()));
    }
    
    @Test
    public void assertTableRuleWithAttributesDataSource() {
        ShardingRule shardingRule = getShardingRule("tableRuleWithAttributesDataSourceGovernance");
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
        assertThat(tableRule.getDatabaseShardingStrategy().getShardingColumns().toArray(new String[]{}),
                is(new String[]{applicationContext.getBean("standardStrategy", StandardShardingStrategyConfiguration.class).getShardingColumn()}));
        assertThat(tableRule.getTableShardingStrategy().getShardingColumns().toArray(new String[]{}),
                is(new String[]{applicationContext.getBean("inlineStrategy", StandardShardingStrategyConfiguration.class).getShardingColumn()}));
        assertTrue(tableRule.getGenerateKeyColumn().isPresent());
        assertThat(tableRule.getGenerateKeyColumn().get(), is("order_id"));
        assertThat(tableRule.getKeyGeneratorName(), is("incrementAlgorithm"));
    }
    
    @Test
    public void assertMultiTableRulesDataSource() {
        ShardingRule shardingRule = getShardingRule("multiTableRulesDataSourceGovernance");
        assertThat(shardingRule.getTableRules().size(), is(2));
        Iterator<TableRule> tableRules = shardingRule.getTableRules().iterator();
        assertThat(tableRules.next().getLogicTable(), is("t_order"));
        assertThat(tableRules.next().getLogicTable(), is("t_order_item"));
    }
    
    @Test
    public void assertBindingTableRuleDatasource() {
        ShardingRule shardingRule = getShardingRule("bindingTableRuleDatasourceGovernance");
        assertThat(shardingRule.getBindingTableRules().size(), is(1));
        BindingTableRule bindingTableRule = shardingRule.getBindingTableRules().iterator().next();
        assertThat(bindingTableRule.getBindingActualTable("dbtbl_0", "t_order", "t_order_item"), is("t_order"));
        assertThat(bindingTableRule.getBindingActualTable("dbtbl_1", "t_order", "t_order_item"), is("t_order"));
    }
    
    @Test
    public void assertMultiBindingTableRulesDatasource() {
        ShardingRule shardingRule = getShardingRule("multiBindingTableRulesDatasourceGovernance");
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
    public void assertBroadcastTableRuleDatasource() {
        ShardingRule shardingRule = getShardingRule("broadcastTableRuleDatasourceGovernance");
        assertThat(shardingRule.getBroadcastTables().size(), is(1));
        assertThat(shardingRule.getBroadcastTables().iterator().next(), is("t_config"));
    }
    
    @Test
    public void assertMultiBroadcastTableRulesDatasource() {
        ShardingRule shardingRule = getShardingRule("multiBroadcastTableRulesDatasourceGovernance");
        assertThat(shardingRule.getBroadcastTables().size(), is(2));
        assertThat(((LinkedList<String>) shardingRule.getBroadcastTables()).get(0), is("t_config1"));
        assertThat(((LinkedList<String>) shardingRule.getBroadcastTables()).get(1), is("t_config2"));
    }
    
    @Test
    public void assertPropsDataSource() {
        GovernanceShardingSphereDataSource shardingSphereDataSource = applicationContext.getBean("propsDataSourceGovernance", GovernanceShardingSphereDataSource.class);
        SchemaContexts schemaContexts = (SchemaContexts) FieldValueUtil.getFieldValue(shardingSphereDataSource, "schemaContexts");
        assertTrue(schemaContexts.getProps().<Boolean>getValue(ConfigurationPropertyKey.SQL_SHOW));
        boolean showSql = schemaContexts.getProps().getValue(ConfigurationPropertyKey.SQL_SHOW);
        assertTrue(showSql);
        int executorSize = schemaContexts.getProps().getValue(ConfigurationPropertyKey.EXECUTOR_SIZE);
        assertThat(executorSize, is(10));
    }
    
    @Test
    public void assertShardingSphereDataSourceType() {
        assertTrue(applicationContext.getBean("simpleShardingGovernance") instanceof GovernanceShardingSphereDataSource);
    }
    
    @Test
    public void assertDefaultActualDataNodes() {
        GovernanceShardingSphereDataSource multiTableRulesDataSource = applicationContext.getBean("multiTableRulesDataSourceGovernance", GovernanceShardingSphereDataSource.class);
        SchemaContexts schemaContexts = (SchemaContexts) FieldValueUtil.getFieldValue(multiTableRulesDataSource, "schemaContexts");
        ShardingRule shardingRule = (ShardingRule) schemaContexts.getDefaultSchema().getRules().iterator().next();
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
    
    private Map<String, DataSource> getDataSourceMap(final String dataSourceName) {
        GovernanceShardingSphereDataSource shardingSphereDataSource = applicationContext.getBean(dataSourceName, GovernanceShardingSphereDataSource.class);
        SchemaContexts schemaContexts = (SchemaContexts) FieldValueUtil.getFieldValue(shardingSphereDataSource, "schemaContexts");
        return schemaContexts.getDefaultSchema().getDataSources();
    }
    
    private ShardingRule getShardingRule(final String dataSourceName) {
        GovernanceShardingSphereDataSource shardingSphereDataSource = applicationContext.getBean(dataSourceName, GovernanceShardingSphereDataSource.class);
        SchemaContexts schemaContexts = (SchemaContexts) FieldValueUtil.getFieldValue(shardingSphereDataSource, "schemaContexts");
        return (ShardingRule) schemaContexts.getDefaultSchema().getRules().iterator().next();
    }
}
