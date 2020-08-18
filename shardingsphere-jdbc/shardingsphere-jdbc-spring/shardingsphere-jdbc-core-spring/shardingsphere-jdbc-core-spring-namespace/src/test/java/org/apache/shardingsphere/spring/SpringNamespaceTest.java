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

package org.apache.shardingsphere.spring;

import org.apache.shardingsphere.driver.jdbc.core.datasource.ShardingSphereDataSource;
import org.apache.shardingsphere.encrypt.rule.EncryptRule;
import org.apache.shardingsphere.infra.config.properties.ConfigurationPropertyKey;
import org.apache.shardingsphere.infra.datanode.DataNode;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.kernel.context.SchemaContexts;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.ComplexShardingStrategyConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.HintShardingStrategyConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.NoneShardingStrategyConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.StandardShardingStrategyConfiguration;
import org.apache.shardingsphere.sharding.rule.BindingTableRule;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.apache.shardingsphere.sharding.rule.TableRule;
import org.apache.shardingsphere.sharding.spi.KeyGenerateAlgorithm;
import org.apache.shardingsphere.sharding.strategy.none.NoneShardingStrategy;
import org.apache.shardingsphere.spring.fixture.IncrementKeyGenerateAlgorithm;
import org.apache.shardingsphere.spring.transaction.ShardingTransactionTypeScanner;
import org.junit.Test;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;

import javax.sql.DataSource;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

@ContextConfiguration(locations = "classpath:META-INF/spring/application-context.xml")
public final class SpringNamespaceTest extends AbstractJUnit4SpringContextTests {
    
    @Test
    public void assertKeyGenerateAlgorithm() {
        assertThat(applicationContext.getBean("incrementAlgorithm"), instanceOf(KeyGenerateAlgorithm.class));
        KeyGenerateAlgorithm incrementKeyGenerateAlgorithm = (KeyGenerateAlgorithm) applicationContext.getBean("incrementAlgorithm");
        KeyGenerateAlgorithm directIncrementKeyGenerateAlgorithm = new IncrementKeyGenerateAlgorithm();
        assertThat(directIncrementKeyGenerateAlgorithm.generateKey(), is(incrementKeyGenerateAlgorithm.generateKey()));
    }
    
    @Test
    public void assertStandardStrategy() {
        StandardShardingStrategyConfiguration standardStrategy = applicationContext.getBean("standardStrategy", StandardShardingStrategyConfiguration.class);
        assertThat(standardStrategy.getShardingColumn(), is("user_id"));
        assertThat(standardStrategy.getShardingAlgorithmName(), is("standardModuloDatabaseShardingAlgorithm"));
    }
    
    @Test
    public void assertRangeStandardStrategy() {
        StandardShardingStrategyConfiguration rangeStandardStrategy = applicationContext.getBean("rangeStandardStrategy", StandardShardingStrategyConfiguration.class);
        assertThat(rangeStandardStrategy.getShardingColumn(), is("order_id"));
        assertThat(rangeStandardStrategy.getShardingAlgorithmName(), is("standardModuloTableShardingAlgorithm"));
    }
    
    @Test
    public void assertComplexStrategy() {
        ComplexShardingStrategyConfiguration complexStrategy = applicationContext.getBean("complexStrategy", ComplexShardingStrategyConfiguration.class);
        assertThat(complexStrategy.getShardingColumns(), is("order_id,user_id"));
        assertThat(complexStrategy.getShardingAlgorithmName(), is("defaultComplexKeysShardingAlgorithm"));
    }
    
    @Test
    public void assertInlineStrategy() {
        StandardShardingStrategyConfiguration inlineStrategy = applicationContext.getBean("inlineStrategy", StandardShardingStrategyConfiguration.class);
        assertThat(inlineStrategy.getShardingColumn(), is("order_id"));
        assertThat(inlineStrategy.getShardingAlgorithmName(), is("inlineStrategyShardingAlgorithm"));
    }
    
    @Test
    public void assertHintStrategy() {
        HintShardingStrategyConfiguration hintStrategy = applicationContext.getBean("hintStrategy", HintShardingStrategyConfiguration.class);
        assertThat(hintStrategy.getShardingAlgorithmName(), is("defaultHintShardingAlgorithm"));
    }
    
    @Test
    public void assertNoneStrategy() {
        applicationContext.getBean("noneStrategy", NoneShardingStrategyConfiguration.class);
    }
    
    @Test
    public void assertSimpleShardingSphereDataSource() {
        Map<String, DataSource> dataSourceMap = getDataSourceMap("simpleShardingDataSource");
        assertNotNull(dataSourceMap.get("dbtbl_0"));
        Iterator<ShardingSphereRule> rules = getRules("simpleShardingDataSource").iterator();
        ShardingRule shardingRule = (ShardingRule) rules.next();
        assertThat(shardingRule.getTableRules().size(), is(1));
        assertThat(shardingRule.getTableRules().iterator().next().getLogicTable(), is("t_order"));
        EncryptRule encryptRule = (EncryptRule) rules.next();
        assertThat(encryptRule.getEncryptTableNames().size(), is(1));
        assertTrue(encryptRule.getEncryptTableNames().contains("t_order"));
        assertFalse(rules.hasNext());
    }
    
    @Test
    public void assertShardingRuleWithAttributesDataSource() {
        Map<String, DataSource> dataSourceMap = getDataSourceMap("shardingRuleWithAttributesDataSource");
        assertNotNull(dataSourceMap.get("dbtbl_0"));
        assertNotNull(dataSourceMap.get("dbtbl_1"));
        ShardingRule shardingRule = getShardingRule("shardingRuleWithAttributesDataSource");
        assertThat(shardingRule.getDefaultDatabaseShardingStrategy().getShardingColumns().toArray(new String[]{}), 
                is(new String[]{applicationContext.getBean("standardStrategy", StandardShardingStrategyConfiguration.class).getShardingColumn()}));
        assertThat(shardingRule.getDefaultTableShardingStrategy().getShardingColumns().toArray(new String[]{}), 
                is(new String[]{applicationContext.getBean("inlineStrategy", StandardShardingStrategyConfiguration.class).getShardingColumn()}));
        assertThat(shardingRule.getDefaultKeyGenerateAlgorithm(), instanceOf(IncrementKeyGenerateAlgorithm.class));
    }
    
    @Test
    public void assertAutoShardingDataSource() {
        Map<String, DataSource> dataSourceMap = getDataSourceMap("autoShardingDataSource");
        assertNotNull(dataSourceMap.get("dbtbl_0"));
        assertNotNull(dataSourceMap.get("dbtbl_1"));
        ShardingRule shardingRule = getShardingRule("autoShardingDataSource");
        assertThat(shardingRule.getTableRules().size(), is(1));
        TableRule tableRule = shardingRule.getTableRules().iterator().next();
        assertThat(tableRule.getLogicTable(), is("t_order"));
        assertThat(tableRule.getActualDataNodes().size(), is(4));
        assertTrue(tableRule.getActualDataNodes().contains(new DataNode("dbtbl_0", "t_order_0")));
        assertTrue(tableRule.getActualDataNodes().contains(new DataNode("dbtbl_1", "t_order_1")));
        assertTrue(tableRule.getActualDataNodes().contains(new DataNode("dbtbl_0", "t_order_2")));
        assertTrue(tableRule.getActualDataNodes().contains(new DataNode("dbtbl_1", "t_order_3")));
        assertThat(tableRule.getDatabaseShardingStrategy(), instanceOf(NoneShardingStrategy.class));
        assertThat(tableRule.getTableShardingStrategy().getShardingColumns().toArray(new String[]{}),
                is(new String[]{applicationContext.getBean("inlineStrategy", StandardShardingStrategyConfiguration.class).getShardingColumn()}));
        assertTrue(tableRule.getGenerateKeyColumn().isPresent());
        assertThat(tableRule.getGenerateKeyColumn().get(), is("order_id"));
        assertThat(tableRule.getKeyGeneratorName(), is("incrementAlgorithm"));
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
    public void assertBroadcastTableRuleDatasource() {
        ShardingRule shardingRule = getShardingRule("broadcastTableRuleDatasource");
        assertThat(shardingRule.getBroadcastTables().size(), is(1));
        assertThat(shardingRule.getBroadcastTables().iterator().next(), is("t_config"));
    }
    
    @Test
    public void assertMultiBroadcastTableRulesDatasource() {
        ShardingRule shardingRule = getShardingRule("multiBroadcastTableRulesDatasource");
        assertThat(shardingRule.getBroadcastTables().size(), is(2));
        assertThat(((LinkedList<String>) shardingRule.getBroadcastTables()).get(0), is("t_config1"));
        assertThat(((LinkedList<String>) shardingRule.getBroadcastTables()).get(1), is("t_config2"));
    }
    
    @Test
    public void assertPropsDataSource() {
        ShardingSphereDataSource shardingSphereDataSource = applicationContext.getBean("propsDataSource", ShardingSphereDataSource.class);
        SchemaContexts schemaContexts = shardingSphereDataSource.getSchemaContexts();
        assertTrue(schemaContexts.getProps().<Boolean>getValue(ConfigurationPropertyKey.SQL_SHOW));
        boolean showSql = schemaContexts.getProps().getValue(ConfigurationPropertyKey.SQL_SHOW);
        assertTrue(showSql);
        int executorSize = schemaContexts.getProps().getValue(ConfigurationPropertyKey.EXECUTOR_SIZE);
        assertThat(executorSize, is(10));
    }
    
    @Test
    public void assertShardingSphereDataSourceType() {
        assertNotNull(applicationContext.getBean("simpleShardingDataSource", ShardingSphereDataSource.class));
    }
    
    @Test
    public void assertDefaultActualDataNodes() {
        ShardingSphereDataSource shardingSphereDataSource = applicationContext.getBean("multiTableRulesDataSource", ShardingSphereDataSource.class);
        SchemaContexts schemaContexts = shardingSphereDataSource.getSchemaContexts();
        ShardingRule shardingRule = (ShardingRule) schemaContexts.getDefaultSchemaContext().getSchema().getRules().iterator().next();
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
    
    @Test
    public void assertShardingTransactionTypeScanner() {
        assertNotNull(applicationContext.getBean(ShardingTransactionTypeScanner.class));
    }
    
    private Map<String, DataSource> getDataSourceMap(final String dataSourceName) {
        ShardingSphereDataSource shardingSphereDataSource = applicationContext.getBean(dataSourceName, ShardingSphereDataSource.class);
        return shardingSphereDataSource.getDataSourceMap();
    }
    
    private ShardingRule getShardingRule(final String dataSourceName) {
        ShardingSphereDataSource shardingSphereDataSource = applicationContext.getBean(dataSourceName, ShardingSphereDataSource.class);
        return (ShardingRule) shardingSphereDataSource.getSchemaContexts().getDefaultSchemaContext().getSchema().getRules().iterator().next();
    }
    
    private Collection<ShardingSphereRule> getRules(final String dataSourceName) {
        return applicationContext.getBean(dataSourceName, ShardingSphereDataSource.class).getSchemaContexts().getDefaultSchemaContext().getSchema().getRules();
    }
}
