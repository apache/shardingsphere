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

package com.dangdang.ddframe.rdb.sharding.api.fixture;

import com.dangdang.ddframe.rdb.sharding.api.config.ShardingRuleConfig;
import com.dangdang.ddframe.rdb.sharding.api.config.TableRuleConfig;
import com.dangdang.ddframe.rdb.sharding.rule.ShardingRule;
import com.dangdang.ddframe.rdb.sharding.keygen.fixture.IncrementKeyGenerator;
import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import org.mockito.Mockito;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Deprecated
// TODO refactor
public class ShardingRuleMockBuilder {
    
    private final List<TableRuleConfig> tableRuleConfigs = new LinkedList<>();
    
    private final List<String> shardingColumns = new LinkedList<>();
    
    private final Map<String, String> generateKeyColumnsMap = new HashMap<>();
    
    private final Set<String> bindTables = new HashSet<>();
    
    public ShardingRuleMockBuilder addTableRuleConfig(final TableRuleConfig tableRuleConfig) {
        tableRuleConfigs.add(tableRuleConfig);
        return this;
    }
    
    public ShardingRuleMockBuilder addShardingColumns(final String shardingColumnName) {
        shardingColumns.add(shardingColumnName);
        return this;
    }
    
    public ShardingRuleMockBuilder addGenerateKeyColumn(final String tableName, final String columnName) {
        generateKeyColumnsMap.put(tableName, columnName);
        return this;
    }
    
    public ShardingRuleMockBuilder addBindingTable(final String bindingTableName) {
        bindTables.add(bindingTableName);
        return this;
    }
    
    public ShardingRule build() throws SQLException {
        Collection<TableRuleConfig> tableRuleConfigs = Lists.newArrayList(Iterators.transform(generateKeyColumnsMap.keySet().iterator(), new Function<String, TableRuleConfig>() {
            
            @Override
            public TableRuleConfig apply(final String input) {
                TableRuleConfig result = new TableRuleConfig();
                result.setLogicTable(input);
                result.setActualTables(input);
                result.setKeyGeneratorColumnName(generateKeyColumnsMap.get(input));
                return result;
            }
        }));
        tableRuleConfigs.addAll(this.tableRuleConfigs);
        if (tableRuleConfigs.isEmpty()) {
            TableRuleConfig tableRuleConfig = new TableRuleConfig();
            tableRuleConfig.setLogicTable("mock");
            tableRuleConfig.setActualTables("mock");
            tableRuleConfigs.add(tableRuleConfig);
        }
        ShardingRuleConfig shardingRuleConfig = new ShardingRuleConfig();
        for (String each : bindTables) {
            if (existInTableRuleConfig(each)) {
                continue;
            }
            TableRuleConfig tableRuleConfig = new TableRuleConfig();
            tableRuleConfig.setLogicTable(each);
            tableRuleConfigs.add(tableRuleConfig);
        }
        for (TableRuleConfig each : tableRuleConfigs) {
            shardingRuleConfig.getTableRuleConfigs().add(each);
            bindTables.add(each.getLogicTable());
        }
        shardingRuleConfig.getBindingTableGroups().add(Joiner.on(",").join(bindTables));
        shardingRuleConfig.setDefaultKeyGeneratorClass(IncrementKeyGenerator.class.getName());
        return shardingRuleConfig.build(ImmutableMap.of("db0", Mockito.mock(DataSource.class), "db1", Mockito.mock(DataSource.class)));
    }
    
    private boolean existInTableRuleConfig(final String logicTableName) {
        for (TableRuleConfig tableRuleConfig : tableRuleConfigs) {
            if (tableRuleConfig.getLogicTable().equals(logicTableName)) {
                return true;
            }
        }
        return false;
    }
}
