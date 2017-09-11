/*
 * Copyright 1999-2015 dangdang.com.
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

package com.dangdang.ddframe.rdb.integrate.type.sharding;

import com.dangdang.ddframe.rdb.common.base.AbstractSQLAssertTest;
import com.dangdang.ddframe.rdb.common.env.ShardingTestStrategy;
import com.dangdang.ddframe.rdb.integrate.fixture.PreciseModuloTableShardingAlgorithm;
import com.dangdang.ddframe.rdb.integrate.fixture.RangeModuloTableShardingAlgorithm;
import com.dangdang.ddframe.rdb.integrate.jaxb.SQLShardingRule;
import com.dangdang.ddframe.rdb.sharding.api.config.ShardingRuleConfig;
import com.dangdang.ddframe.rdb.sharding.api.config.TableRuleConfig;
import com.dangdang.ddframe.rdb.sharding.api.config.strategy.NoneShardingStrategyConfig;
import com.dangdang.ddframe.rdb.sharding.api.config.strategy.StandardShardingStrategyConfig;
import com.dangdang.ddframe.rdb.sharding.constant.DatabaseType;
import com.dangdang.ddframe.rdb.sharding.jdbc.core.datasource.ShardingDataSource;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ShardingTableOnlyTest extends AbstractSQLAssertTest {
    
    private static Map<DatabaseType, ShardingDataSource> shardingDataSources = new HashMap<>();
    
    public ShardingTableOnlyTest(final String testCaseName, final String sql, final DatabaseType type, final List<SQLShardingRule> sqlShardingRules) {
        super(testCaseName, sql, type, sqlShardingRules);
    }
    
    @Override
    protected ShardingTestStrategy getShardingStrategy() {
        return ShardingTestStrategy.tbl;
    }
    
    @Override
    protected List<String> getInitDataSetFiles() {
        return Collections.singletonList("integrate/dataset/sharding/tbl/init/tbl.xml");
    }
    
    @Override
    protected final Map<DatabaseType, ShardingDataSource> getDataSources() throws SQLException {
        if (!shardingDataSources.isEmpty()) {
            return shardingDataSources;
        }
        Map<DatabaseType, Map<String, DataSource>> dataSourceMap = createDataSourceMap();
        for (Map.Entry<DatabaseType, Map<String, DataSource>> each : dataSourceMap.entrySet()) {
            ShardingRuleConfig shardingRuleConfig = new ShardingRuleConfig();
            TableRuleConfig orderTableRuleConfig = new TableRuleConfig();
            orderTableRuleConfig.setLogicTable("t_order");
            orderTableRuleConfig.setActualTables("t_order_${0..9}");
            shardingRuleConfig.getTableRuleConfigs().add(orderTableRuleConfig);
            TableRuleConfig orderItemTableRuleConfig = new TableRuleConfig();
            orderItemTableRuleConfig.setLogicTable("t_order_item");
            orderItemTableRuleConfig.setActualTables("t_order_item_${0..9}");
            orderItemTableRuleConfig.setKeyGeneratorClass("item_id");
            shardingRuleConfig.getTableRuleConfigs().add(orderItemTableRuleConfig);
            shardingRuleConfig.getBindingTableGroups().add("t_order, t_order_item");
            shardingRuleConfig.setDefaultDatabaseShardingStrategyConfig(new NoneShardingStrategyConfig());
            StandardShardingStrategyConfig tableShardingStrategyConfig = new StandardShardingStrategyConfig();
            tableShardingStrategyConfig.setShardingColumn("order_id");
            tableShardingStrategyConfig.setPreciseAlgorithmClassName(PreciseModuloTableShardingAlgorithm.class.getName());
            tableShardingStrategyConfig.setRangeAlgorithmClassName(RangeModuloTableShardingAlgorithm.class.getName());
            shardingRuleConfig.setDefaultTableShardingStrategyConfig(tableShardingStrategyConfig);
            shardingDataSources.put(each.getKey(), new ShardingDataSource(shardingRuleConfig.build(each.getValue())));
        }
        return shardingDataSources;
    }
    
    @Before
    public void initDDLTables() throws SQLException {
        if (getSql().startsWith("ALTER") || getSql().startsWith("TRUNCATE") || getSql().startsWith("DROP")) {
            if (getSql().contains("TEMP")) {
                executeSql("CREATE TEMPORARY TABLE t_log(id int, status varchar(10))");
            } else {
                executeSql("CREATE TABLE t_log(id int, status varchar(10))");
            }
        }
    }
    
    @After
    public void cleanupDdlTables() throws SQLException {
        if (getSql().contains("TEMP") && DatabaseType.Oracle != getCurrentDatabaseType()) {
            return;
        }
        if (getSql().startsWith("CREATE") || getSql().startsWith("ALTER") || getSql().startsWith("TRUNCATE")) {
            executeSql("DROP TABLE t_log");
        }
    }
    
    private void executeSql(final String sql) throws SQLException {
        for (Map.Entry<DatabaseType, ShardingDataSource> each : getDataSources().entrySet()) {
            if (getCurrentDatabaseType() == each.getKey()) {
                try (Connection conn = each.getValue().getConnection();
                     Statement statement = conn.createStatement()) {
                    statement.execute(sql);
                    //CHECKSTYLE:OFF
                } catch (final Exception ex) {
                    //CHECKSTYLE:ON
                    if (null != ex.getMessage() && ex.getMessage().startsWith("Dynamic table")) {
                        continue;
                    }
                    ex.printStackTrace();
                    throw new RuntimeException(ex);
                }
            }
        }
    }
    
    @AfterClass
    public static void clear() {
        if (!shardingDataSources.isEmpty()) {
            for (ShardingDataSource each : shardingDataSources.values()) {
                each.close();
            }
        }
    }
}
