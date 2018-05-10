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

package io.shardingsphere.core.integrate.type.sharding;

import com.google.common.base.Joiner;
import io.shardingsphere.core.api.config.MasterSlaveRuleConfiguration;
import io.shardingsphere.core.api.config.ShardingRuleConfiguration;
import io.shardingsphere.core.api.config.TableRuleConfiguration;
import io.shardingsphere.core.api.config.strategy.StandardShardingStrategyConfiguration;
import io.shardingsphere.core.common.base.AbstractSQLAssertTest;
import io.shardingsphere.core.common.env.ShardingTestStrategy;
import io.shardingsphere.core.constant.DatabaseType;
import io.shardingsphere.core.hint.HintManagerHolder;
import io.shardingsphere.core.integrate.fixture.PreciseModuloDatabaseShardingAlgorithm;
import io.shardingsphere.core.integrate.fixture.RangeModuloDatabaseShardingAlgorithm;
import io.shardingsphere.core.integrate.jaxb.SQLShardingRule;
import io.shardingsphere.core.jdbc.core.datasource.ShardingDataSource;
import io.shardingsphere.core.routing.router.masterslave.MasterVisitedManager;
import io.shardingsphere.core.rule.ShardingRule;
import org.junit.After;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public abstract class AbstractShardingMasterSlaveTest extends AbstractSQLAssertTest {
    
    public AbstractShardingMasterSlaveTest(final String testCaseName, final String sql, final DatabaseType type, final List<SQLShardingRule> sqlShardingRules) {
        super(testCaseName, sql, type, sqlShardingRules);
    }
    
    protected static List<String> getInitFiles() {
        return Arrays.asList(
                "integrate/dataset/sharding/masterslave/init/master_0.xml",
                "integrate/dataset/sharding/masterslave/init/master_1.xml",
                "integrate/dataset/sharding/masterslave/init/master_2.xml",
                "integrate/dataset/sharding/masterslave/init/master_3.xml",
                "integrate/dataset/sharding/masterslave/init/master_4.xml",
                "integrate/dataset/sharding/masterslave/init/master_5.xml",
                "integrate/dataset/sharding/masterslave/init/master_6.xml",
                "integrate/dataset/sharding/masterslave/init/master_7.xml",
                "integrate/dataset/sharding/masterslave/init/master_8.xml",
                "integrate/dataset/sharding/masterslave/init/master_9.xml",
                "integrate/dataset/sharding/masterslave/init/slave_0.xml",
                "integrate/dataset/sharding/masterslave/init/slave_1.xml",
                "integrate/dataset/sharding/masterslave/init/slave_2.xml",
                "integrate/dataset/sharding/masterslave/init/slave_3.xml",
                "integrate/dataset/sharding/masterslave/init/slave_4.xml",
                "integrate/dataset/sharding/masterslave/init/slave_5.xml",
                "integrate/dataset/sharding/masterslave/init/slave_6.xml",
                "integrate/dataset/sharding/masterslave/init/slave_7.xml",
                "integrate/dataset/sharding/masterslave/init/slave_8.xml",
                "integrate/dataset/sharding/masterslave/init/slave_9.xml");
    }
    
    @Override
    protected ShardingTestStrategy getShardingStrategy() {
        return ShardingTestStrategy.masterslave;
    }
    
    @Override
    protected List<String> getInitDataSetFiles() {
        return AbstractShardingMasterSlaveTest.getInitFiles();
    }
    
    @Override
    protected final Map<DatabaseType, ShardingDataSource> getDataSources() throws SQLException {
        if (!getShardingDataSources().isEmpty()) {
            return getShardingDataSources();
        }
        Map<DatabaseType, Map<String, DataSource>> dataSourceMap = createDataSourceMap();
        for (Entry<DatabaseType, Map<String, DataSource>> entry : dataSourceMap.entrySet()) {
            final ShardingRuleConfiguration shardingRuleConfig = new ShardingRuleConfiguration();
            TableRuleConfiguration orderTableRuleConfig = new TableRuleConfiguration();
            orderTableRuleConfig.setLogicTable("t_order");
            orderTableRuleConfig.setLogicIndex("t_order_index");
            List<String> orderActualDataNodes = new LinkedList<>();
            Collection<MasterSlaveRuleConfiguration> masterSlaveRuleConfigs = getMasterSlaveRuleConfigurations();
            for (MasterSlaveRuleConfiguration each : masterSlaveRuleConfigs) {
                orderActualDataNodes.add(each.getName() + ".t_order_${0..9}");
            }
            orderTableRuleConfig.setActualDataNodes(Joiner.on(",").join(orderActualDataNodes));
            shardingRuleConfig.getTableRuleConfigs().add(orderTableRuleConfig);
            TableRuleConfiguration orderItemTableRuleConfig = new TableRuleConfiguration();
            orderItemTableRuleConfig.setLogicTable("t_order_item");
            List<String> itemOrderActualDataNodes = new LinkedList<>();
            for (MasterSlaveRuleConfiguration each : masterSlaveRuleConfigs) {
                itemOrderActualDataNodes.add(each.getName() + ".t_order_item_${0..9}");
            }
            orderItemTableRuleConfig.setActualDataNodes(Joiner.on(",").join(itemOrderActualDataNodes));
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
            shardingRuleConfig.setDefaultTableShardingStrategyConfig(
                    new StandardShardingStrategyConfiguration("t_order_item", new PreciseModuloDatabaseShardingAlgorithm(), new RangeModuloDatabaseShardingAlgorithm()));
            shardingRuleConfig.setDefaultDatabaseShardingStrategyConfig(
                    new StandardShardingStrategyConfiguration("user_id", new PreciseModuloDatabaseShardingAlgorithm(), new RangeModuloDatabaseShardingAlgorithm()));
            shardingRuleConfig.getMasterSlaveRuleConfigs().addAll(masterSlaveRuleConfigs);
            Map<String, DataSource> masterSlaveDataSourceMap = entry.getValue();
            ShardingRule shardingRule = new ShardingRule(shardingRuleConfig, masterSlaveDataSourceMap.keySet());
            getShardingDataSources().put(entry.getKey(), new ShardingDataSource(masterSlaveDataSourceMap, shardingRule));
        }
        return getShardingDataSources();
    }
    
    private Collection<MasterSlaveRuleConfiguration> getMasterSlaveRuleConfigurations() {
        Collection<MasterSlaveRuleConfiguration> result = new LinkedList<>();
        result.add(new MasterSlaveRuleConfiguration("ms_0", "dataSource_master_0", Collections.singletonList("dataSource_slave_0")));
        result.add(new MasterSlaveRuleConfiguration("ms_1", "dataSource_master_1", Collections.singletonList("dataSource_slave_1")));
        result.add(new MasterSlaveRuleConfiguration("ms_2", "dataSource_master_2", Collections.singletonList("dataSource_slave_2")));
        result.add(new MasterSlaveRuleConfiguration("ms_3", "dataSource_master_3", Collections.singletonList("dataSource_slave_3")));
        result.add(new MasterSlaveRuleConfiguration("ms_4", "dataSource_master_4", Collections.singletonList("dataSource_slave_4")));
        result.add(new MasterSlaveRuleConfiguration("ms_5", "dataSource_master_5", Collections.singletonList("dataSource_slave_5")));
        result.add(new MasterSlaveRuleConfiguration("ms_6", "dataSource_master_6", Collections.singletonList("dataSource_slave_6")));
        result.add(new MasterSlaveRuleConfiguration("ms_7", "dataSource_master_7", Collections.singletonList("dataSource_slave_7")));
        result.add(new MasterSlaveRuleConfiguration("ms_8", "dataSource_master_8", Collections.singletonList("dataSource_slave_8")));
        result.add(new MasterSlaveRuleConfiguration("ms_9", "dataSource_master_9", Collections.singletonList("dataSource_slave_9")));
        return result;
    }
    
    @After
    public final void clearFlag() {
        HintManagerHolder.clear();
        MasterVisitedManager.clear();
    }
}
