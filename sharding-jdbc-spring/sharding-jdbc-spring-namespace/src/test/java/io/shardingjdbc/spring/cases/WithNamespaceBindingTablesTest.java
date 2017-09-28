/*
 * Copyright 1999-2015 dangdang.com.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.shardingjdbc.spring.cases;

import io.shardingjdbc.core.api.config.strategy.StandardShardingStrategyConfiguration;
import io.shardingjdbc.core.jdbc.core.datasource.ShardingDataSource;
import io.shardingjdbc.core.rule.BindingTableRule;
import io.shardingjdbc.core.rule.TableRule;
import io.shardingjdbc.spring.AbstractShardingBothDataBasesAndTablesSpringDBUnitTest;
import io.shardingjdbc.spring.util.FieldValueUtil;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.annotation.Resource;

import org.springframework.test.context.ContextConfiguration;

@ContextConfiguration(locations = "classpath:META-INF/rdb/withNamespaceBindingTables.xml")
public final class WithNamespaceBindingTablesTest extends AbstractShardingBothDataBasesAndTablesSpringDBUnitTest {
    
    @Resource
    private StandardShardingStrategyConfiguration databaseStrategy;
    
    @Resource
    private StandardShardingStrategyConfiguration tableStrategy;
    
    @Resource
    private ShardingDataSource shardingDataSource;
    
    public void testShardingNamespace() {
        assertShardingStrategy();
        assertShardingDataSource();
    }
    
    private void assertShardingStrategy() {
        assertShardingStandardStrategy(databaseStrategy, "user_id", "io.shardingjdbc.spring.algorithm.PreciseModuloDatabaseShardingAlgorithm");
        assertShardingStandardStrategy(tableStrategy, "order_id", "io.shardingjdbc.spring.algorithm.PreciseModuloTableShardingAlgorithm");
    }
    
    private void assertShardingDataSource() {
        assertShardingRule();
        assertShardingTableRule();
        assertShardingBindingTableRule();
    }
    
    private void assertShardingRule() {
        assertDataSourceName(shardingDataSource);
        assertDefaultDataSourceName(shardingDataSource);
    }
    
    private void assertShardingTableRule() {
        Collection<TableRule> tableRules = getTableRules(shardingDataSource);
        assertThat(tableRules.size(), is(2));
        Iterator<TableRule> iter = tableRules.iterator();
        TableRule orderRule = iter.next();
        assertLogicTable(orderRule, "t_order");
        assertActualTables(orderRule, "t_order_");
        assertShardingRuleStrategy(orderRule.getDatabaseShardingStrategy(), databaseStrategy);
        assertShardingRuleStrategy(orderRule.getTableShardingStrategy(), tableStrategy);
        TableRule orderItemRule = iter.next();
        assertLogicTable(orderItemRule, "t_order_item");
        assertActualTables(orderItemRule, "t_order_item_");
        assertShardingRuleStrategy(orderItemRule.getDatabaseShardingStrategy(), databaseStrategy);
        assertShardingRuleStrategy(orderItemRule.getTableShardingStrategy(), tableStrategy);
    }
    
    @SuppressWarnings("unchecked")
    private void assertShardingBindingTableRule() {
        Object shardingContext = FieldValueUtil.getFieldValue(shardingDataSource, "shardingContext", true);
        assertNotNull(shardingContext);
        Object shardingRule = FieldValueUtil.getFieldValue(shardingContext, "shardingRule");
        assertNotNull(shardingRule);
        Object bindingTableRules = FieldValueUtil.getFieldValue(shardingRule, "bindingTableRules");
        assertNotNull(bindingTableRules);
        assertThat(((Collection<BindingTableRule>) bindingTableRules).size(), is(1));
        List<TableRule> tableRules = ((Collection<BindingTableRule>) bindingTableRules).iterator().next().getTableRules();
        assertThat(tableRules.size(), is(2));
        assertThat(tableRules.get(0).getLogicTable(), is("t_order"));
        assertThat(tableRules.get(1).getLogicTable(), is("t_order_item"));
    }
}
