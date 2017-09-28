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

import io.shardingjdbc.core.api.algorithm.masterslave.RandomMasterSlaveLoadBalanceAlgorithm;
import io.shardingjdbc.core.api.config.strategy.InlineShardingStrategyConfiguration;
import io.shardingjdbc.core.jdbc.core.datasource.MasterSlaveDataSource;
import io.shardingjdbc.core.jdbc.core.datasource.ShardingDataSource;
import io.shardingjdbc.core.rule.TableRule;
import io.shardingjdbc.spring.AbstractShardingBothDataBasesAndTablesSpringDBUnitTest;

import org.springframework.test.context.ContextConfiguration;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.annotation.Resource;

@ContextConfiguration(locations = "classpath:META-INF/rdb/withNamespaceForMasterSlaveWithStrategyRef.xml")
public final class WithNamespaceForMasterSlaveWithStrategyRefTest extends AbstractShardingBothDataBasesAndTablesSpringDBUnitTest {
    
    @Resource
    private InlineShardingStrategyConfiguration databaseStrategy;
    
    @Resource
    private InlineShardingStrategyConfiguration orderTableStrategy;
    
    @Resource
    private InlineShardingStrategyConfiguration orderItemTableStrategy;
    
    @Resource(name = "dbtbl_0")
    private MasterSlaveDataSource masterSlaveDataSource0;
    
    @Resource(name = "dbtbl_1")
    private MasterSlaveDataSource masterSlaveDataSource1;
    
    @Resource
    private ShardingDataSource shardingDataSource;
    
    @Override
    protected List<String> getSchemaFiles() {
        return Arrays.asList("schema/dbtbl_0_master.sql", "schema/dbtbl_0_slave_0.sql", "schema/dbtbl_0_slave_1.sql",
                "schema/dbtbl_1_master.sql", "schema/dbtbl_1_slave_0.sql", "schema/dbtbl_1_slave_1.sql");
    }
    
    public void testShardingNamespace() {
        assertMasterSlaveDataSource();
        assertShardingStrategy();
        assertShardingDataSource();
    }
    
    private void assertMasterSlaveDataSource() {
        assertMasterDataSource(masterSlaveDataSource0, "dbtbl_0_master");
        assertSlaveDataSourceMap(masterSlaveDataSource0, new String[]{"dbtbl_0_slave_0", "dbtbl_0_slave_1"});
        assertSlaveDataSourceMap(masterSlaveDataSource0, RandomMasterSlaveLoadBalanceAlgorithm.class);
        assertMasterDataSource(masterSlaveDataSource1, "dbtbl_1_master");
        assertSlaveDataSourceMap(masterSlaveDataSource1, new String[]{"dbtbl_1_slave_0", "dbtbl_1_slave_1"});
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
