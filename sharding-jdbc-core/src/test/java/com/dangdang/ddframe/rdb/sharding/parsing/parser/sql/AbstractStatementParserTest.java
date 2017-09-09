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

package com.dangdang.ddframe.rdb.sharding.parsing.parser.sql;

import com.dangdang.ddframe.rdb.sharding.api.config.DataSourceRuleConfig;
import com.dangdang.ddframe.rdb.sharding.api.config.ShardingRuleConfig;
import com.dangdang.ddframe.rdb.sharding.api.config.TableRuleConfig;
import com.dangdang.ddframe.rdb.sharding.api.config.strategy.ComplexShardingStrategyConfig;
import com.dangdang.ddframe.rdb.sharding.api.rule.ShardingRule;
import com.dangdang.ddframe.rdb.sharding.api.strategy.fixture.TestComplexKeysShardingAlgorithm;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public abstract class AbstractStatementParserTest {
    
    protected final ShardingRule createShardingRule() {
        DataSource dataSource = mock(DataSource.class);
        Connection connection = mock(Connection.class);
        DatabaseMetaData databaseMetaData = mock(DatabaseMetaData.class);
        try {
            when(dataSource.getConnection()).thenReturn(connection);
            when(connection.getMetaData()).thenReturn(databaseMetaData);
            when(databaseMetaData.getDatabaseProductName()).thenReturn("H2");
        } catch (final SQLException ex) {
            throw new RuntimeException(ex);
        }
        Map<String, DataSource> dataSourceMap = new HashMap<>(1, 1);
        dataSourceMap.put("ds", dataSource);
        TableRuleConfig tableRuleConfig = new TableRuleConfig();
        tableRuleConfig.setLogicTable("TABLE_XXX");
        tableRuleConfig.setActualTables("table_0, table_1, table_2");
        ComplexShardingStrategyConfig complexShardingStrategyConfig = new ComplexShardingStrategyConfig();
        complexShardingStrategyConfig.setShardingColumns("field1, field2, field3, field4, field5, field6, field7");
        complexShardingStrategyConfig.setAlgorithmClassName(TestComplexKeysShardingAlgorithm.class.getName());
        tableRuleConfig.setTableShardingStrategy(complexShardingStrategyConfig);
        Map<String, TableRuleConfig> tableRuleConfigMap = new HashMap<>(1, 1);
        tableRuleConfigMap.put("TABLE_XXX", tableRuleConfig);
        ShardingRuleConfig shardingRuleConfig = new ShardingRuleConfig();
        shardingRuleConfig.setTableRules(tableRuleConfigMap);
        DataSourceRuleConfig dataSourceRuleConfig = new DataSourceRuleConfig();
        dataSourceRuleConfig.setDataSources(dataSourceMap);
        shardingRuleConfig.setDataSourceRule(dataSourceRuleConfig);
        return new ShardingRule(shardingRuleConfig);
    }
}
