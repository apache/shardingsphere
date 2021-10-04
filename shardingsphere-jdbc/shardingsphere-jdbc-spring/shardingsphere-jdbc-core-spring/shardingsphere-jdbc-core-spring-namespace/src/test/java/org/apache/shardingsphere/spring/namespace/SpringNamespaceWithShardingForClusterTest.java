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

package org.apache.shardingsphere.spring.namespace;

import org.apache.shardingsphere.driver.jdbc.core.datasource.ShardingSphereDataSource;
import org.apache.shardingsphere.infra.config.properties.ConfigurationPropertyKey;
import org.apache.shardingsphere.infra.database.DefaultSchema;
import org.apache.shardingsphere.infra.datanode.DataNode;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.sharding.rule.BindingTableRule;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.apache.shardingsphere.sharding.rule.TableRule;
import org.apache.shardingsphere.spring.namespace.fixture.keygen.IncrementKeyGenerateAlgorithm;
import org.apache.shardingsphere.spring.namespace.util.EmbedTestingServer;
import org.apache.shardingsphere.spring.namespace.util.FieldValueUtil;
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

@ContextConfiguration(locations = "classpath:META-INF/rdb/cluster-application-context-sharding.xml")
public final class SpringNamespaceWithShardingForClusterTest extends AbstractJUnit4SpringContextTests {
    
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
        assertThat(shardingRule.getTableRules().values().iterator().next().getLogicTable(), is("t_order"));
    }
    
    @Test
    public void assertShardingRuleWithAttributesDataSource() {
        Map<String, DataSource> dataSourceMap = getDataSourceMap("shardingRuleWithAttributesDataSourceGovernance");
        ShardingRule shardingRule = getShardingRule("shardingRuleWithAttributesDataSourceGovernance");
        assertNotNull(dataSourceMap.get("dbtbl_0"));
        assertNotNull(dataSourceMap.get("dbtbl_1"));
        assertThat(shardingRule.getDefaultKeyGenerateAlgorithm().getClass().getName(), is(IncrementKeyGenerateAlgorithm.class.getCanonicalName()));
    }
    
    @Test
    public void assertTableRuleWithAttributesDataSource() {
        ShardingRule shardingRule = getShardingRule("tableRuleWithAttributesDataSourceGovernance");
        assertThat(shardingRule.getTableRules().size(), is(1));
        TableRule tableRule = shardingRule.getTableRules().values().iterator().next();
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
        assertTrue(tableRule.getGenerateKeyColumn().isPresent());
        assertThat(tableRule.getGenerateKeyColumn().get(), is("order_id"));
        assertThat(tableRule.getKeyGeneratorName(), is("incrementAlgorithm"));
    }
    
    @Test
    public void assertMultiTableRulesDataSource() {
        ShardingRule shardingRule = getShardingRule("multiTableRulesDataSourceGovernance");
        assertThat(shardingRule.getTableRules().size(), is(2));
        Iterator<TableRule> tableRules = shardingRule.getTableRules().values().iterator();
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
        assertThat(new LinkedList<>(shardingRule.getBroadcastTables()).get(0), is("t_config1"));
        assertThat(new LinkedList<>(shardingRule.getBroadcastTables()).get(1), is("t_config2"));
    }
    
    @Test
    public void assertPropsDataSource() {
        ShardingSphereDataSource shardingSphereDataSource = applicationContext.getBean("propsDataSourceGovernance", ShardingSphereDataSource.class);
        ContextManager contextManager = (ContextManager) FieldValueUtil.getFieldValue(shardingSphereDataSource, "contextManager");
        assertTrue(contextManager.getMetaDataContexts().getProps().<Boolean>getValue(ConfigurationPropertyKey.SQL_SHOW));
        boolean showSql = contextManager.getMetaDataContexts().getProps().getValue(ConfigurationPropertyKey.SQL_SHOW);
        assertTrue(showSql);
        int executorSize = contextManager.getMetaDataContexts().getProps().getValue(ConfigurationPropertyKey.KERNEL_EXECUTOR_SIZE);
        assertThat(executorSize, is(10));
    }
    
    @Test
    public void assertShardingSphereDataSourceType() {
        assertTrue(applicationContext.getBean("simpleShardingGovernance") instanceof ShardingSphereDataSource);
    }
    
    @Test
    public void assertDefaultActualDataNodes() {
        ShardingSphereDataSource multiTableRulesDataSource = applicationContext.getBean("multiTableRulesDataSourceGovernance", ShardingSphereDataSource.class);
        ContextManager contextManager = (ContextManager) FieldValueUtil.getFieldValue(multiTableRulesDataSource, "contextManager");
        Iterator<ShardingSphereRule> iterator = contextManager.getMetaDataContexts().getMetaData(DefaultSchema.LOGIC_NAME).getRuleMetaData().getRules().iterator();
        ShardingRule shardingRule = (ShardingRule) iterator.next();
        assertThat(shardingRule.getTableRules().size(), is(2));
        Iterator<TableRule> tableRules = shardingRule.getTableRules().values().iterator();
        TableRule orderRule = tableRules.next();
        assertThat(orderRule.getActualDataNodes().size(), is(2));
        assertTrue(orderRule.getActualDataNodes().contains(new DataNode("dbtbl_0", "t_order")));
        assertTrue(orderRule.getActualDataNodes().contains(new DataNode("dbtbl_1", "t_order")));
        TableRule orderItemRule = tableRules.next();
        assertThat(orderItemRule.getActualDataNodes().size(), is(2));
        assertTrue(orderItemRule.getActualDataNodes().contains(new DataNode("dbtbl_0", "t_order_item")));
        assertTrue(orderItemRule.getActualDataNodes().contains(new DataNode("dbtbl_1", "t_order_item")));
    }
    
    @Test
    public void assertEmptyConfigurationShardingSphereDataSource() {
        assertTrue(applicationContext.getBean("emptyConfigurationDataSourceGovernance") instanceof ShardingSphereDataSource);
    }
    
    private Map<String, DataSource> getDataSourceMap(final String dataSourceName) {
        ShardingSphereDataSource shardingSphereDataSource = applicationContext.getBean(dataSourceName, ShardingSphereDataSource.class);
        ContextManager contextManager = (ContextManager) FieldValueUtil.getFieldValue(shardingSphereDataSource, "contextManager");
        return contextManager.getMetaDataContexts().getMetaData(DefaultSchema.LOGIC_NAME).getResource().getDataSources();
    }
    
    private ShardingRule getShardingRule(final String dataSourceName) {
        ShardingSphereDataSource shardingSphereDataSource = applicationContext.getBean(dataSourceName, ShardingSphereDataSource.class);
        ContextManager contextManager = (ContextManager) FieldValueUtil.getFieldValue(shardingSphereDataSource, "contextManager");
        Iterator<ShardingSphereRule> iterator = contextManager.getMetaDataContexts().getMetaData(DefaultSchema.LOGIC_NAME).getRuleMetaData().getRules().iterator();
        return (ShardingRule) iterator.next();
    }
}
