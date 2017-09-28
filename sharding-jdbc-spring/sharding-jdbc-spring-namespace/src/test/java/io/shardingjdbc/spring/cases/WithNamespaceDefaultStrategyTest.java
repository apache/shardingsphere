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
import io.shardingjdbc.core.rule.TableRule;
import io.shardingjdbc.spring.AbstractShardingBothDataBasesAndTablesSpringDBUnitTest;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.Collection;
import java.util.Iterator;

import javax.annotation.Resource;

import org.springframework.test.context.ContextConfiguration;

@ContextConfiguration(locations = "classpath:META-INF/rdb/withNamespaceDefaultStrategy.xml")
public final class WithNamespaceDefaultStrategyTest extends AbstractShardingBothDataBasesAndTablesSpringDBUnitTest {
    
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
    }
    
    private void assertShardingRule() {
        assertDataSourceName(shardingDataSource);
        assertDefaultDataSourceName(shardingDataSource);
        assertDefaultDatabaseShardingStrategy(shardingDataSource, databaseStrategy);
        assertDefaultTableShardingStrategy(shardingDataSource, tableStrategy);
    }
    
    private void assertShardingTableRule() {
        Collection<TableRule> tableRules = getTableRules(shardingDataSource);
        assertThat(tableRules.size(), is(2));
        Iterator<TableRule> iter = tableRules.iterator();
        TableRule orderRule = iter.next();
        assertLogicTable(orderRule, "t_order");
        assertActualTables(orderRule, "t_order_");
        TableRule orderItemRule = iter.next();
        assertLogicTable(orderItemRule, "t_order_item");
        assertActualTables(orderItemRule, "t_order_item_");
    }
}
