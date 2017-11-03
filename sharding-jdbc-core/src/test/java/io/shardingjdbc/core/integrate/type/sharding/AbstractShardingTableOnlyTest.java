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

package io.shardingjdbc.core.integrate.type.sharding;

import com.google.common.base.Joiner;

import io.shardingjdbc.core.common.base.AbstractSQLAssertTest;
import io.shardingjdbc.core.common.env.ShardingTestStrategy;
import io.shardingjdbc.core.integrate.fixture.PreciseModuloTableShardingAlgorithm;
import io.shardingjdbc.core.integrate.fixture.RangeModuloTableShardingAlgorithm;
import io.shardingjdbc.core.integrate.jaxb.SQLShardingRule;
import io.shardingjdbc.core.api.config.ShardingRuleConfiguration;
import io.shardingjdbc.core.api.config.TableRuleConfiguration;
import io.shardingjdbc.core.api.config.strategy.NoneShardingStrategyConfiguration;
import io.shardingjdbc.core.api.config.strategy.StandardShardingStrategyConfiguration;
import io.shardingjdbc.core.constant.DatabaseType;
import io.shardingjdbc.core.jdbc.core.datasource.ShardingDataSource;

import org.junit.After;
import org.junit.Before;

import javax.sql.DataSource;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public abstract class AbstractShardingTableOnlyTest extends AbstractSQLAssertTest {
    
    public AbstractShardingTableOnlyTest(final String testCaseName, final String sql, final DatabaseType type, final List<SQLShardingRule> sqlShardingRules) {
        super(testCaseName, sql, type, sqlShardingRules);
    }
    
    protected static List<String> getInitFiles() {
        return Collections.singletonList("integrate/dataset/sharding/tbl/init/tbl.xml");
    }
    
    @Override
    protected ShardingTestStrategy getShardingStrategy() {
        return ShardingTestStrategy.tbl;
    }
    
    @Override
    protected List<String> getInitDataSetFiles() {
        return AbstractShardingTableOnlyTest.getInitFiles();
    }
    
    @Override
    protected final Map<DatabaseType, ShardingDataSource> getDataSources() throws SQLException {
        if (!getShardingDataSources().isEmpty()) {
            return getShardingDataSources();
        }
        Map<DatabaseType, Map<String, DataSource>> dataSourceMap = createDataSourceMap();
        for (Map.Entry<DatabaseType, Map<String, DataSource>> entry : dataSourceMap.entrySet()) {
            final ShardingRuleConfiguration shardingRuleConfig = new ShardingRuleConfiguration();
            TableRuleConfiguration orderTableRuleConfig = new TableRuleConfiguration();
            orderTableRuleConfig.setLogicTable("t_order");
            List<String> orderActualDataNodes = new LinkedList<>();
            for (String dataSourceName : entry.getValue().keySet()) {
                orderActualDataNodes.add(dataSourceName + ".t_order_${0..9}");
            }
            orderTableRuleConfig.setActualDataNodes(Joiner.on(",").join(orderActualDataNodes));
            shardingRuleConfig.getTableRuleConfigs().add(orderTableRuleConfig);
            TableRuleConfiguration orderItemTableRuleConfig = new TableRuleConfiguration();
            orderItemTableRuleConfig.setLogicTable("t_order_item");
            List<String> orderItemActualDataNodes = new LinkedList<>();
            for (String dataSourceName : entry.getValue().keySet()) {
                orderItemActualDataNodes.add(dataSourceName + ".t_order_item_${0..9}");
            }
            orderItemTableRuleConfig.setActualDataNodes(Joiner.on(",").join(orderItemActualDataNodes));
            orderItemTableRuleConfig.setKeyGeneratorClass("item_id");
            shardingRuleConfig.getTableRuleConfigs().add(orderItemTableRuleConfig);
            shardingRuleConfig.getBindingTableGroups().add("t_order, t_order_item");
            shardingRuleConfig.setDefaultDatabaseShardingStrategyConfig(new NoneShardingStrategyConfiguration());
            shardingRuleConfig.setDefaultTableShardingStrategyConfig(
                    new StandardShardingStrategyConfiguration("order_id", PreciseModuloTableShardingAlgorithm.class.getName(), RangeModuloTableShardingAlgorithm.class.getName()));
            getShardingDataSources().put(entry.getKey(), new ShardingDataSource(shardingRuleConfig.build(entry.getValue())));
        }
        return getShardingDataSources();
    }
    
    @Before
    public void initDDLTables() throws SQLException {
        if (getSql().startsWith("ALTER") || getSql().startsWith("TRUNCATE") || getSql().startsWith("DROP")) {
            if (getSql().contains("TEMP")) {
                executeSql("CREATE TEMPORARY TABLE t_temp_log(id int, status varchar(10))");
            } else {
                executeSql("CREATE TABLE t_log(id int, status varchar(10))");
            }
        }
    }
    
    @After
    public void cleanupDdlTables() throws SQLException {
        if (getSql().startsWith("CREATE") || getSql().startsWith("ALTER") || getSql().startsWith("TRUNCATE")) {
            if (getSql().contains("TEMP")) {
                executeSql("DROP TABLE t_temp_log");
            } else {
                executeSql("DROP TABLE t_log");
            }
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
                    ex.printStackTrace();
                    throw new RuntimeException(ex);
                }
            }
        }
    }
}
