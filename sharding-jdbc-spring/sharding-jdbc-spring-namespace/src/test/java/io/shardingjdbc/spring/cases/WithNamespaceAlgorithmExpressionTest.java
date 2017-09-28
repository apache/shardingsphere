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

import io.shardingjdbc.core.api.config.strategy.InlineShardingStrategyConfiguration;
import io.shardingjdbc.core.jdbc.core.datasource.ShardingDataSource;
import io.shardingjdbc.core.rule.TableRule;
import io.shardingjdbc.spring.AbstractShardingBothDataBasesAndTablesSpringDBUnitTest;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.Collection;
import java.util.Iterator;

import javax.annotation.Resource;

import org.springframework.test.context.ContextConfiguration;

@ContextConfiguration(locations = "classpath:META-INF/rdb/withNamespaceAlgorithmExpression.xml")
public final class WithNamespaceAlgorithmExpressionTest extends AbstractShardingBothDataBasesAndTablesSpringDBUnitTest {
    
    @Resource
    private InlineShardingStrategyConfiguration databaseStrategy;
    
    @Resource
    private InlineShardingStrategyConfiguration orderTableStrategy;
    
    @Resource
    private InlineShardingStrategyConfiguration orderItemTableStrategy;
    
    @Resource
    private ShardingDataSource shardingDataSource;
    
    public void testShardingNamespace() {
        assertShardingStrategy();
        assertShardingDataSource();
    }
    
    private void assertShardingStrategy() {
        assertShardingInlineStrategy(databaseStrategy, "user_id", "dbtbl_${user_id % 2}");
        assertShardingInlineStrategy(orderTableStrategy, "order_id", "t_order_${order_id % 4}");
        assertShardingInlineStrategy(orderItemTableStrategy, "order_id", "t_order_item_${order_id % 4}");
    }
    
    private void assertShardingDataSource() {
        assertShardingRule();
        assertShardingTableRule();
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
        assertShardingRuleStrategy(orderRule.getTableShardingStrategy(), orderTableStrategy);
        TableRule orderItemRule = iter.next();
        assertLogicTable(orderItemRule, "t_order_item");
        assertActualTables(orderItemRule, "t_order_item_");
        assertShardingRuleStrategy(orderItemRule.getDatabaseShardingStrategy(), databaseStrategy);
        assertShardingRuleStrategy(orderItemRule.getTableShardingStrategy(), orderItemTableStrategy);
    }
}
