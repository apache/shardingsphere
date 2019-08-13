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

package org.apache.shardingsphere.shardingjdbc.common.base;

import com.google.common.base.Joiner;
import com.google.common.base.Predicate;
import com.google.common.collect.Maps;
import org.apache.shardingsphere.api.config.sharding.KeyGeneratorConfiguration;
import org.apache.shardingsphere.api.config.sharding.ShardingRuleConfiguration;
import org.apache.shardingsphere.api.config.sharding.TableRuleConfiguration;
import org.apache.shardingsphere.api.config.sharding.strategy.StandardShardingStrategyConfiguration;
import org.apache.shardingsphere.core.rule.ShardingRule;
import org.apache.shardingsphere.shardingjdbc.fixture.PreciseOrderShardingAlgorithm;
import org.apache.shardingsphere.shardingjdbc.fixture.RangeOrderShardingAlgorithm;
import org.apache.shardingsphere.shardingjdbc.jdbc.core.connection.ShardingConnection;
import org.apache.shardingsphere.shardingjdbc.jdbc.core.datasource.ShardingDataSource;
import org.h2.tools.RunScript;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;

import javax.sql.DataSource;
import java.io.InputStreamReader;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public abstract class AbstractShardingJDBCDatabaseAndTableTest extends AbstractSQLTest {
    
    private static ShardingDataSource shardingDataSource;
    
    private static final List<String> SHARDING_DB_NAMES = Arrays.asList("jdbc_0", "jdbc_1");
    
    @BeforeClass
    public static void initShardingDataSources() throws SQLException {
        if (null != shardingDataSource) {
            return;
        }
        Map<String, DataSource> dataSources = getDataSources();
        final ShardingRuleConfiguration shardingRuleConfig = new ShardingRuleConfiguration();
        List<String> orderActualDataNodes = new LinkedList<>();
        for (String dataSourceName : dataSources.keySet()) {
            orderActualDataNodes.add(dataSourceName + ".t_order_${0..1}");
        }
        TableRuleConfiguration orderTableRuleConfig = new TableRuleConfiguration("t_order", Joiner.on(",").join(orderActualDataNodes));
        shardingRuleConfig.getTableRuleConfigs().add(orderTableRuleConfig);
        List<String> orderItemActualDataNodes = new LinkedList<>();
        for (String dataSourceName : dataSources.keySet()) {
            orderItemActualDataNodes.add(dataSourceName + ".t_order_item_${0..1}");
        }
        TableRuleConfiguration orderItemTableRuleConfig = new TableRuleConfiguration("t_order_item", Joiner.on(",").join(orderItemActualDataNodes));
        orderItemTableRuleConfig.setKeyGeneratorConfig(new KeyGeneratorConfiguration("INCREMENT", "item_id", new Properties()));
        shardingRuleConfig.getTableRuleConfigs().add(orderItemTableRuleConfig);
        TableRuleConfiguration configTableRuleConfig = new TableRuleConfiguration("t_config");
        shardingRuleConfig.getTableRuleConfigs().add(configTableRuleConfig);
        shardingRuleConfig.getBindingTableGroups().add("t_order, t_order_item");
        shardingRuleConfig.setDefaultTableShardingStrategyConfig(new StandardShardingStrategyConfiguration("order_id", new PreciseOrderShardingAlgorithm(), new RangeOrderShardingAlgorithm()));
        shardingRuleConfig.setDefaultDatabaseShardingStrategyConfig(new StandardShardingStrategyConfiguration("user_id", new PreciseOrderShardingAlgorithm(), new RangeOrderShardingAlgorithm()));
        ShardingRule shardingRule = new ShardingRule(shardingRuleConfig, dataSources.keySet());
        shardingDataSource = new ShardingDataSource(dataSources, shardingRule, new Properties());
    }
    
    private static Map<String, DataSource> getDataSources() {
        return Maps.filterKeys(getDatabaseTypeMap().values().iterator().next(), new Predicate<String>() {
            
            @Override
            public boolean apply(final String input) {
                return SHARDING_DB_NAMES.contains(input);
            }
        });
    }
    
    @Before
    public void initTable() {
        try {
            ShardingConnection conn = shardingDataSource.getConnection();
            RunScript.execute(conn, new InputStreamReader(AbstractSQLTest.class.getClassLoader().getResourceAsStream("jdbc_data.sql")));
            conn.close();
        } catch (final SQLException ex) {
            ex.printStackTrace();
        }
    }
    
    protected final ShardingDataSource getShardingDataSource() {
        return shardingDataSource;
    }
    
    @AfterClass
    public static void clear() throws Exception {
        if (null == shardingDataSource) {
            return;
        }
        shardingDataSource.close();
        shardingDataSource = null;
    }
}
