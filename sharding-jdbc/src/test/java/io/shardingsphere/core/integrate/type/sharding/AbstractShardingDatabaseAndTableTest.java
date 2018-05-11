/*
 * Copyright 2016-2018 shardingsphere.io.
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

package io.shardingsphere.core.integrate.type.sharding;

import com.google.common.base.Joiner;
import io.shardingsphere.core.api.config.ShardingRuleConfiguration;
import io.shardingsphere.core.api.config.TableRuleConfiguration;
import io.shardingsphere.core.api.config.strategy.StandardShardingStrategyConfiguration;
import io.shardingsphere.core.common.base.AbstractSQLAssertTest;
import io.shardingsphere.core.common.env.ShardingTestStrategy;
import io.shardingsphere.core.constant.DatabaseType;
import io.shardingsphere.core.integrate.fixture.PreciseModuloDatabaseShardingAlgorithm;
import io.shardingsphere.core.integrate.fixture.PreciseModuloTableShardingAlgorithm;
import io.shardingsphere.core.integrate.fixture.RangeModuloDatabaseShardingAlgorithm;
import io.shardingsphere.core.integrate.fixture.RangeModuloTableShardingAlgorithm;
import io.shardingsphere.core.integrate.jaxb.SQLShardingRule;
import io.shardingsphere.core.jdbc.core.datasource.ShardingDataSource;
import io.shardingsphere.core.rule.ShardingRule;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public abstract class AbstractShardingDatabaseAndTableTest extends AbstractSQLAssertTest {
    
    public AbstractShardingDatabaseAndTableTest(final String testCaseName, final String sql, final DatabaseType type, final List<SQLShardingRule> sqlShardingRules) {
        super(testCaseName, sql, type, sqlShardingRules);
    }
    
    protected static List<String> getInitFiles() {
        return Arrays.asList(
                "integrate/dataset/sharding/dbtbl/init/dbtbl_0.xml",
                "integrate/dataset/sharding/dbtbl/init/dbtbl_1.xml",
                "integrate/dataset/sharding/dbtbl/init/dbtbl_2.xml",
                "integrate/dataset/sharding/dbtbl/init/dbtbl_3.xml",
                "integrate/dataset/sharding/dbtbl/init/dbtbl_4.xml",
                "integrate/dataset/sharding/dbtbl/init/dbtbl_5.xml",
                "integrate/dataset/sharding/dbtbl/init/dbtbl_6.xml",
                "integrate/dataset/sharding/dbtbl/init/dbtbl_7.xml",
                "integrate/dataset/sharding/dbtbl/init/dbtbl_8.xml",
                "integrate/dataset/sharding/dbtbl/init/dbtbl_9.xml");
    }
    
    @Override
    protected ShardingTestStrategy getShardingStrategy() {
        return ShardingTestStrategy.dbtbl;
    }
    
    @Override
    protected List<String> getInitDataSetFiles() {
        return AbstractShardingDatabaseAndTableTest.getInitFiles();
    }
    
    @Override
    protected final Map<DatabaseType, ShardingDataSource> getDataSources() throws SQLException {
        if (!getShardingDataSources().isEmpty()) {
            return getShardingDataSources();
        }
        Map<DatabaseType, Map<String, DataSource>> dataSourceMap = createDataSourceMap();
        for (Map.Entry<DatabaseType, Map<String, DataSource>> entry : dataSourceMap.entrySet()) {
            ShardingRuleConfiguration shardingRuleConfig = new ShardingRuleConfiguration();
            shardingRuleConfig.setDefaultDataSourceName("dataSource_dbtbl_0");
            TableRuleConfiguration orderTableRuleConfig = new TableRuleConfiguration();
            orderTableRuleConfig.setLogicTable("t_order");
            orderTableRuleConfig.setLogicIndex("t_order_index");
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
            shardingRuleConfig.getTableRuleConfigs().add(orderItemTableRuleConfig);
            TableRuleConfiguration configTableRuleConfig = new TableRuleConfiguration();
            configTableRuleConfig.setLogicTable("t_config");
            TableRuleConfiguration logTableRuleConfig = new TableRuleConfiguration();
            logTableRuleConfig.setLogicIndex("t_log_index");
            logTableRuleConfig.setLogicTable("t_log");
            TableRuleConfiguration tempLogTableRuleConfig = new TableRuleConfiguration();
            tempLogTableRuleConfig.setLogicTable("t_temp_log");
            shardingRuleConfig.getTableRuleConfigs().add(logTableRuleConfig);
            shardingRuleConfig.getTableRuleConfigs().add(tempLogTableRuleConfig);
            shardingRuleConfig.getTableRuleConfigs().add(configTableRuleConfig);
            shardingRuleConfig.getBindingTableGroups().add("t_order, t_order_item");
            shardingRuleConfig.setDefaultTableShardingStrategyConfig(
                    new StandardShardingStrategyConfiguration("order_id", new PreciseModuloTableShardingAlgorithm(), new RangeModuloTableShardingAlgorithm()));
            shardingRuleConfig.setDefaultDatabaseShardingStrategyConfig(
                    new StandardShardingStrategyConfiguration("user_id", new PreciseModuloDatabaseShardingAlgorithm(), new RangeModuloDatabaseShardingAlgorithm()));
            getShardingDataSources().put(entry.getKey(), new ShardingDataSource(entry.getValue(), new ShardingRule(shardingRuleConfig, entry.getValue().keySet())));
        }
        return getShardingDataSources();
    }
}
