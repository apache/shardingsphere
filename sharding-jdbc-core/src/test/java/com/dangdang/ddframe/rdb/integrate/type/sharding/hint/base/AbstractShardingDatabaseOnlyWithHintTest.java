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

package com.dangdang.ddframe.rdb.integrate.type.sharding.hint.base;

import com.dangdang.ddframe.rdb.integrate.fixture.ComplexKeysModuloDatabaseShardingAlgorithm;
import com.dangdang.ddframe.rdb.sharding.api.config.BindingTableRuleConfig;
import com.dangdang.ddframe.rdb.sharding.api.config.DataSourceRuleConfig;
import com.dangdang.ddframe.rdb.sharding.api.config.ShardingRuleConfig;
import com.dangdang.ddframe.rdb.sharding.api.config.TableRuleConfig;
import com.dangdang.ddframe.rdb.sharding.api.config.strategy.ComplexShardingStrategyConfig;
import com.dangdang.ddframe.rdb.sharding.api.config.strategy.NoneShardingStrategyConfig;
import com.dangdang.ddframe.rdb.sharding.api.rule.ShardingRule;
import com.dangdang.ddframe.rdb.sharding.constant.DatabaseType;

import javax.sql.DataSource;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public abstract class AbstractShardingDatabaseOnlyWithHintTest extends AbstractHintTest {
    
    @Override
    protected ShardingRule getShardingRule(final Map.Entry<DatabaseType, Map<String, DataSource>> dataSourceEntry) {
        ShardingRuleConfig shardingRuleConfig = new ShardingRuleConfig();
        DataSourceRuleConfig dataSourceRuleConfig = new DataSourceRuleConfig();
        dataSourceRuleConfig.setDataSources(dataSourceEntry.getValue());
        shardingRuleConfig.setDataSourceRule(dataSourceRuleConfig);
        Map<String, TableRuleConfig> tableRuleConfigMap = new HashMap<>(2, 1);
        TableRuleConfig orderTableRuleConfig = new TableRuleConfig();
        orderTableRuleConfig.setLogicTable("t_order");
        TableRuleConfig orderItemTableRuleConfig = new TableRuleConfig();
        orderItemTableRuleConfig.setLogicTable("t_order_item");
        tableRuleConfigMap.put("t_order", orderTableRuleConfig);
        tableRuleConfigMap.put("t_order_item", orderItemTableRuleConfig);
        shardingRuleConfig.setTableRules(tableRuleConfigMap);
        BindingTableRuleConfig bindingTableRuleConfig = new BindingTableRuleConfig();
        bindingTableRuleConfig.setTableNames("t_order, t_order_item");
        shardingRuleConfig.setBindingTableRules(Collections.singletonList(bindingTableRuleConfig));
        ComplexShardingStrategyConfig databaseShardingStrategyConfig = new ComplexShardingStrategyConfig();
        databaseShardingStrategyConfig.setShardingColumns("user_id");
        databaseShardingStrategyConfig.setAlgorithmClassName(ComplexKeysModuloDatabaseShardingAlgorithm.class.getName());
        shardingRuleConfig.setDefaultDatabaseShardingStrategy(databaseShardingStrategyConfig);
        shardingRuleConfig.setDefaultTableShardingStrategy(new NoneShardingStrategyConfig());
        return new ShardingRule(shardingRuleConfig);
    }
}
